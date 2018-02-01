package com.dastanapps.app

import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.content.Context
import android.graphics.ImageFormat
import android.graphics.Point
import android.graphics.SurfaceTexture
import android.hardware.Camera
import android.hardware.camera2.*
import android.hardware.camera2.params.StreamConfigurationMap
import android.media.CamcorderProfile
import android.media.ImageReader
import android.media.MediaRecorder
import android.os.*
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.Surface
import android.view.TextureView
import android.view.WindowManager
import android.widget.Button
import com.dastanapps.camera.R
import com.dastanapps.camera2.Camera2Helper
import java.io.IOException
import java.util.*

@TargetApi(Build.VERSION_CODES.LOLLIPOP)
open class Cam2Activity2 : AppCompatActivity(), TextureView.SurfaceTextureListener {
    override fun onSurfaceTextureSizeChanged(surfaceTexture: SurfaceTexture?, width: Int, height: Int) {
    }

    override fun onSurfaceTextureUpdated(surface: SurfaceTexture?) {

    }

    override fun onSurfaceTextureDestroyed(surface: SurfaceTexture?): Boolean {
        return true
    }

    override fun onSurfaceTextureAvailable(surfaceTexture: SurfaceTexture?, width: Int, height: Int) {
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
    private var imageReader: ImageReader? = null

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

    private fun startPreview() {
        if (null == mCameraDevice || !textureView?.isAvailable!! || null == mPreviewSize) {
            return
        }
        try {
            closePreviewSession()
            val texture = textureView!!.getSurfaceTexture();
            texture.setDefaultBufferSize(mPreviewSize!!.getWidth(), mPreviewSize!!.getHeight())
            previewRequestBuilder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW)

            val previewSurface = Surface(texture)
            previewRequestBuilder?.addTarget(previewSurface)

            mCameraDevice.createCaptureSession(listOf(previewSurface), object : CameraCaptureSession.StateCallback() {
                override fun onConfigureFailed(session: CameraCaptureSession?) {

                }

                override fun onConfigured(session: CameraCaptureSession?) {
                    updatePreview(session!!)
                }

            }, backgroundHandler)
        } catch (e: CameraAccessException) {
            e.printStackTrace()
        }


    }

    private fun updatePreview(cameraCaptureSession: CameraCaptureSession) {
        captureSession = cameraCaptureSession

        try {
            captureSession?.setRepeatingRequest(previewRequestBuilder?.build(), captureCallback, backgroundHandler)
        } catch (e: Exception) {
            Log.e(TAG, "Error updating preview: ", e)
        }
    }

    private lateinit var mCameraDevice: CameraDevice
    private val stateCallback = object : CameraDevice.StateCallback() {
        override fun onOpened(cameraDevice: CameraDevice) {
            Log.d(TAG, "onOpened")
            mCameraDevice = cameraDevice
            startPreview()
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
        startBackgroundThread()
        if (textureView?.isAvailable!!) {
            openCamera(textureView?.getWidth()!!, textureView?.getHeight()!!)
        } else {
        }
    }

    override fun onPause() {
        super.onPause()
        closeCamera()
    }

    private var mVideoSize: android.util.Size? = null
    private var mPreviewSize: android.util.Size? = null
    @SuppressLint("MissingPermission")
            /**
             * Tries to open a [CameraDevice]. The result is listened by `mStateCallback`.
             */
    fun openCamera(width: Int, height: Int) {
        // Choose the sizes for camera preview and video recording
        val mCharacteristics = manager?.getCameraCharacteristics("0")
        val map = mCharacteristics!!.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP)

        mVideoSize = Camera2Helper.chooseVideoSize(map!!.getOutputSizes(MediaRecorder::class.java))
        mPreviewSize = Camera2Helper.chooseOptimalSize(map!!.getOutputSizes(SurfaceTexture::class.java),
                width, height, mVideoSize)
        manager?.openCamera("0", stateCallback, null)
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

            photoSize = getPictureSize(Size.fromArray2(map?.getOutputSizes(ImageFormat.JPEG)))

            imageReader = ImageReader.newInstance(photoSize?.getWidth()!!, photoSize?.getHeight()!!,
                    ImageFormat.JPEG, 2)
            imageReader?.setOnImageAvailableListener({ }, backgroundHandler)

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

    fun getPictureSize(sizes: Array<Size>?): Size? {
        if (sizes == null || sizes.size == 0) return null

        val choices = Arrays.asList(*sizes)

        if (choices.size == 1) return choices[0]

        var result: Size? = null
        val maxPictureSize = Collections.max(choices, CompareSizesByArea2())
        val minPictureSize = Collections.min(choices, CompareSizesByArea2())

        Collections.sort(choices, CompareSizesByArea2())

        result = maxPictureSize
        return result
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
        closeImageReader()
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

    private fun closeImageReader() {
        if (null != imageReader) {
            imageReader?.close()
            imageReader = null
        }
    }

}
