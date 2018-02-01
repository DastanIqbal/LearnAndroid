package com.dastanapps.app

import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.content.Context
import android.graphics.Point
import android.graphics.SurfaceTexture
import android.hardware.Camera
import android.hardware.camera2.*
import android.hardware.camera2.params.StreamConfigurationMap
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
import java.util.*

@TargetApi(Build.VERSION_CODES.LOLLIPOP)
open class Cam2Activity : AppCompatActivity(), TextureView.SurfaceTextureListener {
    override fun onSurfaceTextureSizeChanged(surfaceTexture: SurfaceTexture?, width: Int, height: Int) {
        if (surfaceTexture != null) startPreview(surfaceTexture)
    }

    override fun onSurfaceTextureUpdated(surface: SurfaceTexture?) {

    }

    override fun onSurfaceTextureDestroyed(surface: SurfaceTexture?): Boolean {
        return true
    }

    override fun onSurfaceTextureAvailable(surfaceTexture: SurfaceTexture?, width: Int, height: Int) {
        if (surfaceTexture != null) startPreview(surfaceTexture)
    }

    private var manager: CameraManager? = null
    private var cameraDevice: CameraDevice? = null
    private var previewRequest: CaptureRequest? = null
    private var previewRequestBuilder: CaptureRequest.Builder? = null
    private var captureSession: CameraCaptureSession? = null

    private var backCameraStreamConfigurationMap: StreamConfigurationMap? = null
    private val STATE_PREVIEW = 0
    private val STATE_WAITING_LOCK = 1
    private val STATE_WAITING_PRE_CAPTURE = 2
    private val STATE_WAITING_NON_PRE_CAPTURE = 3
    private val STATE_PICTURE_TAKEN = 4
    private var previewState = STATE_PREVIEW

    private var texture: SurfaceTexture? = null

    private var workingSurface: Surface? = null

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
        setContentView(R.layout.fragment_camera2_video)
        textureView = findViewById(R.id.texture)
        textureView?.surfaceTextureListener = this
        startBackgroundThread()
        initializeCameraOptions()
        findViewById<Button>(R.id.video).setOnClickListener {
            if (!isVideoRecording)
                startVideoRecord()
            else stopVideoRecord()
        }
    }

    //--------------------Internal methods------------------

    private fun startPreview(texture: SurfaceTexture?) {
        try {
            if (texture == null) return

            this.texture = texture

            texture.setDefaultBufferSize(previewSize?.width!!, previewSize?.height!!)

            workingSurface = Surface(texture)

            previewRequestBuilder = cameraDevice?.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW)
            previewRequestBuilder?.addTarget(workingSurface)

            cameraDevice?.createCaptureSession(Arrays.asList<Surface>(workingSurface),
                    object : CameraCaptureSession.StateCallback() {
                        override fun onConfigured(cameraCaptureSession: CameraCaptureSession) {
                            updatePreview(cameraCaptureSession)
                        }

                        override fun onConfigureFailed(cameraCaptureSession: CameraCaptureSession) {
                            Log.d(TAG, "Fail while starting preview: ")
                        }

                        override fun onClosed(session: CameraCaptureSession?) {
                            super.onClosed(session)
                            Log.e(TAG, "Session Closed")
                        }
                    }, null)
        } catch (e: Exception) {
            Log.e(TAG, "Error while preparing surface for preview: ", e)
        }

    }

    private fun updatePreview(cameraCaptureSession: CameraCaptureSession) {
        if (null == cameraDevice) {
            return
        }
        captureSession = cameraCaptureSession

        try {
            captureSession?.setRepeatingRequest(previewRequestBuilder?.build(), captureCallback, backgroundHandler)
        } catch (e: Exception) {
            Log.e(TAG, "Error updating preview: ", e)
        }

    }

    private val stateCallback = object : CameraDevice.StateCallback() {
        override fun onOpened(cameraDevice1: CameraDevice) {
            Log.d(TAG, "onOpened")
            cameraDevice = cameraDevice1;
        }

        override fun onDisconnected(cameraDevice: CameraDevice) {
            cameraDevice.close()
            Log.d(TAG, "onDisconnected")
        }

        override fun onError(cameraDevice: CameraDevice, error: Int) {
            cameraDevice.close()
            Log.d(TAG, "onError:" + error)
        }
    }

    private val captureCallback = object : CameraCaptureSession.CaptureCallback() {

        override fun onCaptureProgressed(session: CameraCaptureSession, request: CaptureRequest, partialResult: CaptureResult) {
            processCaptureResult(partialResult)
        }

        override fun onCaptureCompleted(session: CameraCaptureSession, request: CaptureRequest, result: TotalCaptureResult) {
            processCaptureResult(result)
        }
    }

    private fun processCaptureResult(result: CaptureResult) {
        when (previewState) {
            STATE_PREVIEW -> {
            }
            STATE_WAITING_LOCK -> {
                val afState = result.get(CaptureResult.CONTROL_AF_STATE)
                if (afState == null) {
                    //captureStillPicture()
                } else if (CaptureResult.CONTROL_AF_STATE_FOCUSED_LOCKED == afState
                        || CaptureResult.CONTROL_AF_STATE_NOT_FOCUSED_LOCKED == afState
                        || CaptureResult.CONTROL_AF_STATE_INACTIVE == afState
                        || CaptureResult.CONTROL_AF_STATE_PASSIVE_SCAN == afState) {
                    val aeState = result.get(CaptureResult.CONTROL_AE_STATE)
                    if (aeState == null || aeState == CaptureResult.CONTROL_AE_STATE_CONVERGED) {
                        previewState = STATE_PICTURE_TAKEN
                        //  captureStillPicture()
                    } else {
                        runPreCaptureSequence()
                    }
                }
            }
            STATE_WAITING_PRE_CAPTURE -> {
                val aeState = result.get(CaptureResult.CONTROL_AE_STATE)
                if (aeState == null ||
                        aeState == CaptureResult.CONTROL_AE_STATE_PRECAPTURE ||
                        aeState == CaptureRequest.CONTROL_AE_STATE_FLASH_REQUIRED) {
                    previewState = STATE_WAITING_NON_PRE_CAPTURE
                }
            }
            STATE_WAITING_NON_PRE_CAPTURE -> {
                val aeState = result.get(CaptureResult.CONTROL_AE_STATE)
                if (aeState == null || aeState != CaptureResult.CONTROL_AE_STATE_PRECAPTURE) {
                    previewState = STATE_PICTURE_TAKEN
                    // captureStillPicture()
                }
            }
            STATE_PICTURE_TAKEN -> {
            }
        }
    }

    private fun runPreCaptureSequence() {
        try {
            previewRequestBuilder?.set(CaptureRequest.CONTROL_AE_PRECAPTURE_TRIGGER, CaptureRequest.CONTROL_AE_PRECAPTURE_TRIGGER_START)
            previewState = STATE_WAITING_PRE_CAPTURE
            captureSession?.capture(previewRequestBuilder?.build(), captureCallback, backgroundHandler)
        } catch (e: CameraAccessException) {
        }
    }

    private fun initializeCameraOptions() {
        this.manager = getSystemService(Context.CAMERA_SERVICE) as CameraManager?
        val windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val display = windowManager.defaultDisplay
        val size = Point()
        display.getSize(size)
        windowSize = Size(size.x, size.y)
    }

    override fun onResume() {
        super.onResume()
        openCamera()
    }

    override fun onPause() {
        super.onPause()
        closeCamera()
    }

    @SuppressLint("MissingPermission")
    fun openCamera() {
        backgroundHandler?.post({
            prepareCameraOutputs()
            try {
                manager?.openCamera("0", stateCallback, backgroundHandler)
            } catch (e: Exception) {
                Log.e(TAG, "openCamera: ", e)
            }
        })
    }


    private fun startBackgroundThread() {
        backgroundThread = HandlerThread(TAG, Process.THREAD_PRIORITY_BACKGROUND)
        backgroundThread?.start()
        backgroundHandler = Handler(backgroundThread?.looper)
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
            val characteristics = manager?.getCameraCharacteristics("0")
            backCameraStreamConfigurationMap = characteristics?.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP)

            val map = backCameraStreamConfigurationMap
            camcorderProfile = CamcorderProfile.get(0, CamcorderProfile.QUALITY_HIGH)

            videoSize = chooseOptimalSize(Size.fromArray2(map?.getOutputSizes(MediaRecorder::class.java)),
                    windowSize?.width!!, windowSize?.height!!, Size(camcorderProfile?.videoFrameWidth!!, camcorderProfile?.videoFrameHeight!!))

            if (videoSize == null || videoSize?.width!! > camcorderProfile?.videoFrameWidth!!
                    || videoSize?.height!! > camcorderProfile!!.videoFrameHeight)
                videoSize = getSizeWithClosestRatio(Size.fromArray2(map?.getOutputSizes(MediaRecorder::class.java)), camcorderProfile?.videoFrameWidth!!, camcorderProfile?.videoFrameHeight!!)
            else if (videoSize == null || videoSize?.width!! > camcorderProfile?.videoFrameWidth!!
                    || videoSize?.height!! > camcorderProfile?.videoFrameHeight!!)
                videoSize = getSizeWithClosestRatio(Size.fromArray2(map?.getOutputSizes(MediaRecorder::class.java)), camcorderProfile?.videoFrameWidth!!, camcorderProfile?.videoFrameHeight!!)

            previewSize = if (windowSize?.height!! * windowSize?.getWidth()!! > videoSize?.width!! * videoSize!!.getHeight()) {
                getOptimalPreviewSize(Size.fromArray2(map?.getOutputSizes(SurfaceTexture::class.java)), videoSize!!.width, videoSize!!.getHeight())
            } else {
                getOptimalPreviewSize(Size.fromArray2(map?.getOutputSizes(SurfaceTexture::class.java)), windowSize!!.getWidth(), windowSize!!.getHeight())
            }

            if (previewSize == null)
                previewSize = getSizeWithClosestRatio(Size.fromArray2(map?.getOutputSizes(SurfaceTexture::class.java)), videoSize!!.getWidth(), videoSize!!.getHeight())
        } catch (e: Exception) {
            Log.e(TAG, "Error while setup camera sizes.", e)
        }


    }


    fun getOptimalPreviewSize(sizes: Array<Size>?, width: Int, height: Int): Size? {

        if (sizes == null) return null

        val ASPECT_TOLERANCE = 0.1
        val targetRatio = height.toDouble() / width
        var optimalSize: Size? = null
        var minDiff = java.lang.Double.MAX_VALUE

        for (size in sizes) {
            //            if (size.getWidth() == width && size.getHeight() == height)
            //                return size;
            val ratio = size.width.toDouble() / size.height.toDouble()
            if (Math.abs(ratio - targetRatio) > ASPECT_TOLERANCE) continue
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

    fun chooseOptimalSize(choices: Array<Size>, width: Int, height: Int, aspectRatio: Size): Size? {
        // Collect the supported resolutions that are at least as big as the preview Surface
        val w = aspectRatio.width
        val h = aspectRatio.height
        val bigEnough = choices.filter { it.height == it.width * h / w && it.width >= width && it.height >= height }

        // Pick the smallest of those, assuming we found any
        return if (bigEnough.isNotEmpty()) {
            Collections.min(bigEnough, CompareSizesByArea2())
        } else {
            Log.e(TAG, "Couldn't find any suitable preview size")
            null
        }
    }

    private class CompareSizesByArea2 : Comparator<Size> {
        override fun compare(lhs: Size, rhs: Size): Int {
            // We cast here to ensure the multiplications won't overflow
            return java.lang.Long.signum(lhs.width.toLong() * lhs.height - rhs.width.toLong() * rhs.height)
        }

    }

    fun getSizeWithClosestRatio(sizes: Array<Size>?, width: Int, height: Int): Size? {

        if (sizes == null) return null

        var MIN_TOLERANCE = 100.0
        val targetRatio = height.toDouble() / width
        var optimalSize: Size? = null
        var minDiff = java.lang.Double.MAX_VALUE

        for (size in sizes) {
            //            if (size.getWidth() == width && size.getHeight() == height)
            //                return size;

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
            videoRecorder?.setAudioSource(MediaRecorder.AudioSource.CAMCORDER)
            videoRecorder?.setVideoSource(MediaRecorder.VideoSource.SURFACE)

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
            closePreviewSession()
            if (prepareVideoRecorder()) {
                val texture = texture
                texture?.setDefaultBufferSize(videoSize?.getWidth()!!, videoSize?.getHeight()!!)

                try {
                    previewRequestBuilder = cameraDevice?.createCaptureRequest(CameraDevice.TEMPLATE_RECORD)
                    val surfaces = ArrayList<Surface>()

                    val previewSurface = workingSurface
                    surfaces.add(previewSurface!!)
                    previewRequestBuilder?.addTarget(previewSurface)

                    workingSurface = videoRecorder?.getSurface()
                    surfaces.add(workingSurface!!)
                    previewRequestBuilder?.addTarget(workingSurface)

                    cameraDevice?.createCaptureSession(surfaces, object : CameraCaptureSession.StateCallback() {
                        override fun onConfigured(cameraCaptureSession: CameraCaptureSession) {
                            captureSession = cameraCaptureSession

                            previewRequestBuilder?.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO)
                            try {
                                captureSession?.setRepeatingRequest(previewRequestBuilder?.build(), null, backgroundHandler)
                            } catch (e: Exception) {
                            }

                            try {
                                videoRecorder?.start()
                            } catch (ignore: Exception) {
                                Log.e(TAG, "videoRecorder.start(): ", ignore)
                            }

                            isVideoRecording = true
                        }

                        override fun onConfigureFailed(cameraCaptureSession: CameraCaptureSession) {
                            Log.d(TAG, "onConfigureFailed")
                        }

                        override fun onClosed(session: CameraCaptureSession?) {
                            super.onClosed(session)
                            Log.e(TAG, "Session Closed")
                        }
                    }, backgroundHandler)
                } catch (e: Exception) {
                    Log.e(TAG, "startVideoRecord: ", e)
                }
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

    private fun closePreviewSession() {
        if (captureSession != null) {
            captureSession?.close()
            try {
                captureSession?.abortCaptures()
            } catch (ignore: Exception) {
            } finally {
                captureSession = null
            }
        }
    }

    private fun closeCamera() {
        closePreviewSession()
        releaseTexture()
        closeCameraDevice()
        releaseVideoRecorder()
    }

    private fun releaseTexture() {
        if (null != texture) {
            texture?.release()
            texture = null
        }
    }

    private fun closeCameraDevice() {
        if (null != cameraDevice) {
            cameraDevice?.close()
            cameraDevice = null
        }
    }

}
