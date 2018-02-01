package com.dastanapps.app

import android.content.Context
import android.graphics.SurfaceTexture
import android.hardware.Camera
import android.media.CamcorderProfile
import android.media.MediaRecorder
import android.os.*
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.Surface
import android.view.TextureView
import android.view.WindowManager
import android.widget.Button
import com.dastanapps.camera.R
import java.io.IOException

open class Main2Activity : AppCompatActivity(), TextureView.SurfaceTextureListener {
    override fun onSurfaceTextureSizeChanged(surface: SurfaceTexture?, width: Int, height: Int) {

    }

    override fun onSurfaceTextureUpdated(surface: SurfaceTexture?) {

    }

    override fun onSurfaceTextureDestroyed(surface: SurfaceTexture?): Boolean {
        closeCamera()
        return true
    }

    override fun onSurfaceTextureAvailable(surface: SurfaceTexture?, width: Int, height: Int) {
        openCamera(0)
    }

    private val TAG = "DEBUG"
    var backgroundThread: HandlerThread? = null
    var backgroundHandler: Handler? = null
    var uiHandler = Handler(Looper.getMainLooper())
    var photoSize: Size? = null
    var videoSize: Size? = null
    var previewSize: Size? = null
    var windowSize: Size? = null
    private lateinit var camera: Camera
    private var textureView: TextureView? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.fragment_camera1_video)
        textureView = findViewById(R.id.texture)
        textureView?.surfaceTextureListener = this
        startBackgroundThread()
        findViewById<Button>(R.id.video).setOnClickListener {
            if (!isVideoRecording)
                startVideoRecord()
            else stopVideoRecord()
        }
    }

    override fun onResume() {
        super.onResume()
    }

    fun openCamera(cameraId: Int?) {
        backgroundHandler?.post({
            try {
                camera = Camera.open(cameraId!!)
                prepareCameraOutputs()
            } catch (error: Exception) {
                Log.d(TAG, "Can't open camera: " + error.message)
            }
        })
    }

    fun closeCamera() {
        backgroundHandler?.post({
            camera.release()
        })
    }

    private fun startBackgroundThread() {
        backgroundThread = HandlerThread(TAG, Process.THREAD_PRIORITY_BACKGROUND)
        backgroundThread?.start()
        backgroundHandler = Handler(backgroundThread?.getLooper())
    }

    private fun stopBackgroundThread() {
        if (Build.VERSION.SDK_INT > 17) {
            backgroundThread?.quitSafely()
        } else
            backgroundThread?.quit()

        try {
            backgroundThread?.join()
        } catch (e: InterruptedException) {
            Log.e(TAG, "stopBackgroundThread: ", e)
        } finally {
            backgroundThread = null
            backgroundHandler = null
        }
    }

    var videoRecorder: MediaRecorder? = null
    var camcorderProfile: CamcorderProfile? = null
    val currentCameraId = 0;
    private var displayRotation = 0
    fun prepareCameraOutputs() {
        try {
            camcorderProfile = CamcorderProfile.get(0, CamcorderProfile.QUALITY_HIGH)

            val previewSizes = Size.fromList(camera.getParameters().getSupportedPreviewSizes())
            val pictureSizes = Size.fromList(camera.getParameters().getSupportedPictureSizes())
            val videoSizes: List<Size>?
            videoSizes = if (Build.VERSION.SDK_INT > 10)
                Size.fromList(camera.getParameters().getSupportedVideoSizes())
            else
                previewSizes

            videoSize = getSizeWithClosestRatio(if (videoSizes == null || videoSizes!!.isEmpty()) previewSizes else videoSizes,
                    camcorderProfile?.videoFrameWidth!!, camcorderProfile?.videoFrameHeight!!)

            previewSize = getSizeWithClosestRatio(previewSizes, videoSize?.width!!, videoSize?.height!!)

            val cameraInfo = Camera.CameraInfo()
            Camera.getCameraInfo(currentCameraId, cameraInfo)
            val cameraRotationOffset = cameraInfo.orientation

            val parameters = camera.parameters
//            setAutoFocus(camera, parameters)
//            setFlashMode(configurationProvider.getFlashMode())

            if (parameters.supportedFocusModes.contains(
                            Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO)) {
                parameters.focusMode = Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO
            }
            camera.parameters = parameters

            val rotation = (getSystemService(Context.WINDOW_SERVICE) as WindowManager).defaultDisplay.rotation
            var degrees = 0
            when (rotation) {
                Surface.ROTATION_0 -> degrees = 0
                Surface.ROTATION_90 -> degrees = 90
                Surface.ROTATION_180 -> degrees = 180
                Surface.ROTATION_270 -> degrees = 270
            }// Natural orientation
            // Landscape left
            // Upside down
            // Landscape right

            if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                displayRotation = (cameraRotationOffset + degrees) % 360
                displayRotation = (360 - displayRotation) % 360 // compensate
            } else {
                displayRotation = (cameraRotationOffset - degrees + 360) % 360
            }

            this.camera.setDisplayOrientation(displayRotation)

            parameters.videoStabilization = true

            parameters.setPreviewSize(previewSize?.getWidth()!!, previewSize!!.getHeight())

            camera.parameters = parameters
            camera.setPreviewTexture(textureView?.surfaceTexture)
            camera.startPreview()

        } catch (e: Exception) {
            Log.e(TAG, "Error while setup camera sizes.")
        }

    }

    fun getSizeWithClosestRatio(sizes: List<Size>?, width: Int, height: Int): Size? {

        if (sizes == null) return null

        var MIN_TOLERANCE = 100.0
        val targetRatio = height.toDouble() / width
        var optimalSize: Size? = null
        var minDiff = java.lang.Double.MAX_VALUE

        for (size in sizes) {
            if (size.width === width && size.height === height)
                return size

            val ratio = size.height.toDouble() / size.width.toDouble()

            if (Math.abs(ratio - targetRatio) < MIN_TOLERANCE)
                MIN_TOLERANCE = ratio
            else
                continue

            if (Math.abs(size.height - height) < minDiff) {
                optimalSize = size
                minDiff = Math.abs(size.height - height).toDouble()
            }
        }

        if (optimalSize == null) {
            minDiff = java.lang.Double.MAX_VALUE
            for (size in sizes) {
                if (Math.abs(size.height - height) < minDiff) {
                    optimalSize = size
                    minDiff = Math.abs(size.height - height).toDouble()
                }
            }
        }
        return optimalSize
    }

    protected fun prepareVideoRecorder(): Boolean {
        videoRecorder = MediaRecorder()
        try {
            camera.lock()
            camera.unlock()
            videoRecorder?.setCamera(camera)

            videoRecorder?.setAudioSource(MediaRecorder.AudioSource.DEFAULT)
            videoRecorder?.setVideoSource(MediaRecorder.VideoSource.DEFAULT)

            videoRecorder?.setOutputFormat(camcorderProfile!!.fileFormat)
            videoRecorder?.setVideoFrameRate(camcorderProfile!!.videoFrameRate)
            videoRecorder?.setVideoSize(videoSize?.getWidth()!!, videoSize!!.getHeight())
            videoRecorder?.setVideoEncodingBitRate(camcorderProfile!!.videoBitRate)
            videoRecorder?.setVideoEncoder(camcorderProfile!!.videoCodec)

            videoRecorder?.setAudioEncodingBitRate(camcorderProfile!!.audioBitRate)
            videoRecorder?.setAudioChannels(camcorderProfile!!.audioChannels)
            videoRecorder?.setAudioSamplingRate(camcorderProfile!!.audioSampleRate)
            videoRecorder?.setAudioEncoder(camcorderProfile!!.audioCodec)

            videoRecorder?.setOutputFile("/sdcard/video.mp4")

            videoRecorder?.setOrientationHint(0)
            // videoRecorder?.setPreviewDisplay(surface)

            videoRecorder?.prepare()

            return true
        } catch (error: IllegalStateException) {
            Log.e(TAG, "IllegalStateException preparing MediaRecorder: " + error.message)
        } catch (error: IOException) {
            Log.e(TAG, "IOException preparing MediaRecorder: " + error.message)
        } catch (error: Throwable) {
            Log.e(TAG, "Error during preparing MediaRecorder: " + error.message)
        }

        releaseVideoRecorder()
        return false
    }

    private var isVideoRecording: Boolean = false

    fun startVideoRecord() {
        if (isVideoRecording) return
        backgroundHandler?.post({
            if (prepareVideoRecorder()) {
                videoRecorder?.start()
                isVideoRecording = true
            }
        })
    }

    fun stopVideoRecord() {
        if (isVideoRecording) {
            backgroundHandler?.post({
                try {
                    if (videoRecorder != null) videoRecorder!!.stop()
                } catch (ignore: Exception) {
                    // ignore illegal state.
                    // appear in case time or file size reach limit and stop already called.
                }
                isVideoRecording = false
                releaseVideoRecorder()
            })
        }
    }

    protected fun releaseVideoRecorder() {
        try {
            if (videoRecorder != null) {
                videoRecorder!!.reset()
                videoRecorder!!.release()
            }
        } catch (ignore: Exception) {

        } finally {
            videoRecorder = null
        }
        try {
            camera.lock() // lock camera for later use
        } catch (ignore: Exception) {
        }

    }

}
