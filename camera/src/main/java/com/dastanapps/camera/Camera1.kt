package com.dastanapps.camera

import android.app.Activity
import android.content.Context
import android.graphics.SurfaceTexture
import android.hardware.Camera
import android.media.CamcorderProfile
import android.media.MediaRecorder
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.util.Log
import android.view.MotionEvent
import android.view.OrientationEventListener
import android.view.Surface
import android.widget.Toast
import com.dastanapps.CameraHelper
import com.dastanapps.camera.listeners.Cam1OrientationEventListener
import com.dastanapps.camera.listeners.Cam1SurfaceTextureListener
import com.dastanapps.camera.view.Cam1AutoFitTextureView
import com.dastanapps.camera.view.FocusImageView
import com.dastanapps.camera2.view.CameraSurfaceRenderer
import com.dastanapps.encoder.MediaVideoEncoder
import java.io.IOException
import java.util.concurrent.Semaphore
import java.util.concurrent.TimeUnit

/**
 * Created by dastaniqbal on 18/01/2018.

 * 18/01/2018 6:32
 */
@SuppressWarnings("deprecation")
class Camera1(private val mContext: Context, private val mTextureView: Cam1AutoFitTextureView, private val mCamera1Listener: ICamera1) {
    private val mCameraSurfaceTextureListener: Cam1SurfaceTextureListener
    private val mOrientationEventListener: OrientationEventListener
    private val mActivity: Activity?

    private var mCamera: Camera? = null
    private var mDisplayOrientation: Int = 0
    private var cameraId: Int = 0
    private var mCharacteristics: Camera.Parameters? = null
    private var mVideoSize: Camera.Size? = null
    private var mPreviewSize: Camera.Size? = null
    private var mNextVideoAbsolutePath: String? = null
    private var filterTexture: SurfaceTexture? = null
    /**
     * MediaRecorder
     */
    private var mMediaRecorder: MediaRecorder? = null

    /**
     * Whether the app is recording video now
     */
    private var mIsRecordingVideo: Boolean = false

    /**
     * A [Semaphore] to prevent the app from exiting before closing the camera.
     */
    private val mCameraOpenCloseLock = Semaphore(1)
    private var focusImage: FocusImageView? = null
    private var screenCurrentRotation: Int = 0
    private val mainHandler = object : Handler(Looper.getMainLooper()) {

        override fun handleMessage(msg: Message) {
            super.handleMessage(msg)
            when (msg.what) {
                FOCUSING_FOCUS_VIEW -> {
                    val touchEvent = msg.obj as MotionEvent
                    if (touchEvent != null && focusImage != null) {
                        focusImage!!.startFocusing(touchEvent)
                    }
                }
                SUCCESS_FOCUS_VIEW -> if (focusImage != null) {
                    focusImage!!.focusSuccess()
                }
                FAILED_FOCUS_VIEW -> if (focusImage != null) {
                    focusImage!!.focusFailed()
                }
                3 -> {
                    screenCurrentRotation = msg.obj as Int
                    mCamera1Listener.orientationChanged(screenCurrentRotation)
                }
            }
        }
    }

    val isCameraOpen: Boolean
        get() = mCamera != null

    val rotationCorrection: Int
        get() {
            val displayRotation = mDisplayOrientation * 90
            val mCamInfo = Camera.CameraInfo()
            Camera.getCameraInfo(cameraId, mCamInfo)

            if (cameraId == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                val mirroredRotation = (mCamInfo.orientation + displayRotation) % 360
                return (360 - mirroredRotation) % 360
            } else {
                return (mCamInfo.orientation - displayRotation + 360) % 360
            }
        }

    private val stopRecodeingHandler = Handler()
    private val stopRecodingRunnable = Runnable {
        if (mMediaRecorder != null)
            mMediaRecorder!!.stop()
    }


    init {
        this.mActivity = mContext as Activity
        mCameraSurfaceTextureListener = Cam1SurfaceTextureListener(this, mTextureView, mActivity)
        // this.mTextureView.surfaceTextureListener = mCameraSurfaceTextureListener
        this.mTextureView.setMainHandler(mainHandler)

        mOrientationEventListener = Cam1OrientationEventListener(mContext, mainHandler)
        setupManager()
        setUpFilters()
    }

    private fun setupManager() {
        val backCamera = Camera1Helper.getCameraInfo(Camera.CameraInfo.CAMERA_FACING_BACK)
        cameraId = backCamera!!.second
        mDisplayOrientation = backCamera.first.orientation
    }

    fun onResume() {
        if (mOrientationEventListener.canDetectOrientation()) {
            mOrientationEventListener.enable()
        }
    }

    fun onPause() {
        releaseMediaRecorder()
        closeCamera()
        mOrientationEventListener.disable()
    }

    private lateinit var camCoderProfile: CamcorderProfile

    fun openCamera() {
        closeCamera()

        try {
            if (!mCameraOpenCloseLock.tryAcquire(2500, TimeUnit.MILLISECONDS)) {
                throw RuntimeException("Time out waiting to lock camera opening.")
            }
            mCamera = Camera.open(cameraId)
            mCharacteristics = mCamera!!.parameters
            mTextureView.setCameraSettings(mCamera!!)

            camCoderProfile = Camera1Helper.baseRecordingProfile(cameraId)

            val videoSizeList = when {
                mCharacteristics!!.supportedVideoSizes != null -> mCharacteristics!!.supportedVideoSizes
                else -> mCharacteristics!!.supportedPreviewSizes
            }

            mVideoSize = Camera1Helper.getSizeWithClosestRatio(videoSizeList, camCoderProfile.videoFrameWidth, camCoderProfile.videoFrameHeight)
            mPreviewSize = Camera1Helper.getSizeWithClosestRatio(mCharacteristics!!.supportedPreviewSizes, mVideoSize!!.width, mVideoSize!!.height)
            //Camera1Helper.chooseOptimalSize(mCharacteristics!!.supportedPreviewSizes, mTextureView.width, mTextureView.height, mVideoSize!!)

            val orientation = mContext.resources.configuration.orientation
            //        if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
            //            mTextureView.setAspectRatio(mPreviewSize.width, mPreviewSize.height);
            //        } else {
            //            mTextureView.setAspectRatio(mPreviewSize.height, mPreviewSize.width);
            //        }
            //            Camera1Helper.configureTransform(mActivity, mPreviewSize, mTextureView, mTextureView.getWidth(), mTextureView.getHeight());
//            mCharacteristics!!.setPreviewSize(1280, 960)
//            mCamera!!.parameters = mCharacteristics;

            try {
//                val texture = mTextureView.surfaceTexture
//                if (mPreviewSize != null)
//                    texture?.setDefaultBufferSize(mPreviewSize!!.width, mPreviewSize!!.height)
                mCamera!!.setDisplayOrientation(Camera1Helper.setDisplayOrientation(cameraId, mActivity, mDisplayOrientation))
                mCharacteristics!!.focusMode = Camera.Parameters.FOCUS_MODE_AUTO
                mCharacteristics!!.flashMode = Camera.Parameters.FLASH_MODE_AUTO
                mCamera!!.parameters = mCharacteristics!!
                mCamera!!.setPreviewTexture(filterTexture);//mTextureView.surfaceTexture)
                mCamera!!.startPreview()
            } catch (ioe: IOException) {
                // Something bad happened
            }

        } catch (e: InterruptedException) {
            e.printStackTrace()
        }

    }

    fun startPreview() {

    }

    fun setAutoExposure(autoExposure: Int) {
        mCharacteristics!!.autoExposureLock = false
        val maxmax = mCharacteristics!!.maxExposureCompensation
        val minmin = mCharacteristics!!.minExposureCompensation
        val all = -minmin + maxmax
        val time = 100 / all
        val ae = if (autoExposure / time - maxmax > maxmax) maxmax else if (autoExposure / time - maxmax < minmin) minmin else autoExposure / time - maxmax
        mCharacteristics!!.exposureCompensation = ae
        mCamera!!.parameters = mCharacteristics
        mCharacteristics!!.autoExposureLock = true
    }

    fun closeCamera() {
        try {
            mCameraOpenCloseLock.release()
            mCameraOpenCloseLock.acquire()
            if (mCamera != null) {
                mCamera!!.stopPreview()
                mCamera!!.release()
                mCamera = null
            }
        } catch (e: InterruptedException) {
            e.printStackTrace()
        } finally {
            mCameraOpenCloseLock.release()
        }
    }

    fun switchFaces() {
        if (cameraId == Camera.CameraInfo.CAMERA_FACING_BACK)
            cameraId = Camera.CameraInfo.CAMERA_FACING_FRONT //Front
        else
            cameraId = Camera.CameraInfo.CAMERA_FACING_BACK //Back
        openCamera()
    }

    fun toggleFlash(): DialogHelper.FeatureListDialog {
        val flashes = mCharacteristics!!.supportedFlashModes
        return DialogHelper.FeatureListDialog.newInstance().setFeatures(flashes, object : DialogHelper.FeatureListDialog.ISelectItem {
            override fun onSelectItem(which: Int) {
                mCharacteristics!!.flashMode = flashes[which]
                mCamera!!.parameters = mCharacteristics
            }
        })
    }

    fun showScenesDialog(): DialogHelper.FeatureListDialog {
        val sceneModes = mCharacteristics!!.supportedSceneModes
        return DialogHelper.FeatureListDialog.newInstance().setFeatures(sceneModes, object : DialogHelper.FeatureListDialog.ISelectItem {
            override fun onSelectItem(which: Int) {
                mCharacteristics!!.sceneMode = sceneModes[which]
                mCamera!!.parameters = mCharacteristics
            }
        })
    }

    fun showEffectsDialog(): DialogHelper.FeatureListDialog {
        val colorEffects = mCharacteristics!!.supportedColorEffects
        return DialogHelper.FeatureListDialog.newInstance().setFeatures(colorEffects, object : DialogHelper.FeatureListDialog.ISelectItem {
            override fun onSelectItem(which: Int) {
                mCharacteristics!!.colorEffect = colorEffects[which]
                mCamera!!.parameters = mCharacteristics
            }
        })
    }

    fun showWhiteBalance(): DialogHelper.FeatureListDialog {
        val colorEffects = mCharacteristics!!.supportedWhiteBalance
        return DialogHelper.FeatureListDialog.newInstance().setFeatures(colorEffects, object : DialogHelper.FeatureListDialog.ISelectItem {
            override fun onSelectItem(which: Int) {
                mCharacteristics!!.whiteBalance = colorEffects[which]
                mCamera!!.parameters = mCharacteristics
            }
        })
    }

    fun setFocusImage(focusImage: FocusImageView) {
        this.focusImage = focusImage
    }

    fun toggleRecording() {
        when {
            mIsRecordingVideo -> {
                stopRecordingVideo()
                releaseMediaRecorder()
                mCamera!!.lock()
            }
            prepareMediaRecorder() -> startRecordingVideo()
            else -> releaseMediaRecorder()
        }
    }

    fun startRecordingVideo() {
        if (!mTextureView.isAvailable) {
            return
        }
        mMediaRecorder!!.start()
        mIsRecordingVideo = true
    }

    private fun prepareMediaRecorder(): Boolean {
        if (null == mActivity) {
            return false
        }
        val surface = Surface(mTextureView.surfaceTexture)
        mMediaRecorder = MediaRecorder()
        mMediaRecorder!!.setOnInfoListener { mr, what, extra ->
            when (what) {
                MediaRecorder.MEDIA_RECORDER_INFO_UNKNOWN -> {
                }
                MediaRecorder.MEDIA_RECORDER_INFO_MAX_DURATION_REACHED -> stopRecordingVideo()
                MediaRecorder.MEDIA_RECORDER_INFO_MAX_FILESIZE_REACHED -> stopRecordingVideo()
                else -> {
                }
            }// NOP
        }

        // Step 1: Unlock and set camera to MediaRecorder
        mCamera!!.unlock()
        mMediaRecorder!!.setCamera(mCamera)

        // Step 2: Set sources
        mMediaRecorder!!.setAudioSource(MediaRecorder.AudioSource.CAMCORDER)
        mMediaRecorder!!.setVideoSource(MediaRecorder.VideoSource.CAMERA)


        // Step 3: Set a CamcorderProfile (requires API Level 8 or higher)
        // Customise your profile based on a pre-existing profile

//        val profile = Camera1Helper.baseRecordingProfile(cameraId)
//        profile.fileFormat = MediaRecorder.OutputFormat.MPEG_4
//        profile.videoCodec = MediaRecorder.VideoEncoder.H264
//        profile.audioCodec = MediaRecorder.AudioEncoder.AAC
//
//        if (mVideoSize != null) {
//            profile.videoFrameHeight = 960 // mVideoSize!!.height
//            profile.videoFrameWidth = 1280 // mVideoSize!!.width
//        }
//        profile.videoFrameRate = 30
//        profile.videoBitRate = 9000000
//        profile.duration = 30
//
//        if (mVideoSize != null) {
//            mMediaRecorder!!.setProfile(profile);//CamcorderProfile.get(CamcorderProfile.QUALITY_720P));//profile)
//        } else {
//            mMediaRecorder!!.setProfile(CamcorderProfile.get(CamcorderProfile.QUALITY_HIGH))
//        }

        mMediaRecorder!!.setOutputFormat(camCoderProfile.fileFormat)
        mMediaRecorder!!.setVideoFrameRate(camCoderProfile.videoFrameRate)
        mMediaRecorder!!.setVideoSize(mVideoSize!!.width, mVideoSize!!.height)
        mMediaRecorder!!.setVideoEncodingBitRate(camCoderProfile.videoBitRate)
        mMediaRecorder!!.setVideoEncoder(camCoderProfile.videoCodec)

        mMediaRecorder!!.setAudioEncodingBitRate(camCoderProfile.audioBitRate)
        mMediaRecorder!!.setAudioChannels(camCoderProfile.audioChannels)
        mMediaRecorder!!.setAudioSamplingRate(camCoderProfile.audioSampleRate)
        mMediaRecorder!!.setAudioEncoder(camCoderProfile.audioCodec)

        if (mNextVideoAbsolutePath == null || mNextVideoAbsolutePath!!.isEmpty()) {
            mNextVideoAbsolutePath = getVideoFilePath(mActivity)
        }
        // Step 4: Set output file
        mMediaRecorder!!.setOutputFile(mNextVideoAbsolutePath)
        mMediaRecorder!!.setOrientationHint(CameraHelper.DEFAULT_ORIENTATIONS.get(screenCurrentRotation))

        // Step 5: Set the preview output
        //mMediaRecorder!!.setPreviewDisplay(surface) //Not required for TextureView

        // Step 6: Prepare configured MediaRecorder
        try {
            mMediaRecorder!!.prepare()
        } catch (e: Exception) {
            Log.d(TAG, "IllegalStateException preparing MediaRecorder: " + e.message)
            releaseMediaRecorder()
            Toast.makeText(mActivity, "Video Prepare Failed", Toast.LENGTH_LONG).show()
            return false
        }

        return true
    }

    private fun getVideoFilePath(context: Context): String {
        val dir = context.getExternalFilesDir(null)
        return ((if (dir == null) "" else dir.absolutePath + "/")
                + System.currentTimeMillis() + ".mp4")
    }

    private fun releaseMediaRecorder() {
        if (mMediaRecorder != null) {
            mMediaRecorder!!.reset()   // clear recorder configuration
            mMediaRecorder!!.release() // release the recorder object
            mMediaRecorder = null
            mCamera!!.lock()           // lock camera for later use
        }
    }

    private fun stopRecordingVideo() {
        // UI
        mIsRecordingVideo = false

        stopRecodeingHandler.removeCallbacks(stopRecodingRunnable)
        stopRecodeingHandler.postDelayed(stopRecodingRunnable, 100)

        if (null != mActivity) {
            Toast.makeText(mActivity, "Video saved: " + mNextVideoAbsolutePath!!,
                    Toast.LENGTH_SHORT).show()
            Log.d(TAG, "Video saved: " + mNextVideoAbsolutePath!!)
        }
        mNextVideoAbsolutePath = null
        mCamera1Listener.cameraRecordingStopped()
    }

    var cameraSurfaceRenderer: CameraSurfaceRenderer? = null

    fun setVideoEncoder(encoder: MediaVideoEncoder) {
        cameraSurfaceRenderer!!.setVideoEnocder(mTextureView, encoder)
    }

    private fun setUpFilters() {
        cameraSurfaceRenderer = CameraSurfaceRenderer()
        cameraSurfaceRenderer?.setListener({ surfaceTexture ->
            filterTexture = surfaceTexture
            Handler(Looper.getMainLooper()).post {
                openCamera()
            }
        })
        mTextureView.setRenderer(cameraSurfaceRenderer)
    }

    fun changeFilter() {
        if (cameraSurfaceRenderer != null) {
            mTextureView.queueEvent {
                cameraSurfaceRenderer?.changeFilter()
            }
        }
    }

    companion object {
        private val TAG = "Camera1"
        val FOCUSING_FOCUS_VIEW = 0
        val SUCCESS_FOCUS_VIEW = 1
        val FAILED_FOCUS_VIEW = 2
    }
}
