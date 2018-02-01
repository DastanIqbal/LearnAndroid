package com.dastanapps.camera2;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Rect;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.params.Face;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.hardware.camera2.params.TonemapCurve;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.util.Range;
import android.util.Size;
import android.view.Surface;
import android.widget.Toast;

import com.dastanapps.CameraHelper;
import com.dastanapps.camera2.callback.PreviewSessionCallback;
import com.dastanapps.camera2.listeners.AwbSeekBarChangeListener;
import com.dastanapps.camera2.listeners.Cam2OrientationEventListener;
import com.dastanapps.camera2.listeners.CamSurfaceTextureListener;
import com.dastanapps.camera2.view.AwbSeekBar;
import com.dastanapps.camera2.view.Cam2AutoFitTextureView;
import com.dastanapps.camera2.view.FocusImageView;
import com.dastanapps.gles.DefaultCameraRenderer;
import com.dastanapps.gles.TextureViewGLWrapper;
import com.dastanapps.view.FaceOverlayView;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import static android.hardware.camera2.CameraMetadata.INFO_SUPPORTED_HARDWARE_LEVEL_FULL;
import static android.hardware.camera2.CameraMetadata.INFO_SUPPORTED_HARDWARE_LEVEL_LEGACY;
import static android.hardware.camera2.CameraMetadata.INFO_SUPPORTED_HARDWARE_LEVEL_LIMITED;

/**
 * Created by dastaniqbal on 17/01/2018.
 * dastanIqbal@marvelmedia.com
 * 17/01/2018 11:53
 */
@RequiresApi(Build.VERSION_CODES.LOLLIPOP)
public class Camera2 {
    private static final String TAG = "Camera2";
    public static final int SENSOR_ORIENTATION_DEFAULT_DEGREES = 90;
    public static final int SENSOR_ORIENTATION_INVERSE_DEGREES = 270;
    public static int FLASH_OFF = 0;
    public static int FLASH_ON = 1;
    public static int FLASH_AUTO = 2;

    private int flashMode = FLASH_OFF;

    /**
     * Whether or not the currently configured camera device is fixed-focus.
     */
    private boolean mNoAFRun = false;
    private CameraDevice mCameraDevice;
    private CameraCaptureSession mPreviewSession;
    private Cam2OrientationEventListener mOrientationListener;

    /**
     * The {@link Size} of camera preview.
     */
    private com.dastanapps.app.Size mPreviewSize;
    private float[][] channels;
    private CamcorderProfile camCorderProfile;

    public com.dastanapps.app.Size getmVideoSize() {
        return mVideoSize;
    }

    /**
     * The {@link Size} of video recording.
     */
    private com.dastanapps.app.Size mVideoSize;

    /**
     * An additional thread for running tasks that shouldn't block the UI.
     */
    private HandlerThread mBackgroundThread;

    /**
     * A {@link Handler} for running tasks in the background.
     */
    private Handler mBackgroundHandler;

    /**
     * A {@link Semaphore} to prevent the app from exiting before closing the camera.
     */
    private Semaphore mCameraOpenCloseLock = new Semaphore(1);

    private ICamera2 camera2Listener;
    /**
     * {@link CameraDevice.StateCallback} is called when {@link CameraDevice} changes its status.
     */
    private CameraDevice.StateCallback mStateCallback = new CameraDevice.StateCallback() {

        @Override
        public void onOpened(@NonNull CameraDevice cameraDevice) {
            mCameraDevice = cameraDevice;
            startPreview();
            mCameraOpenCloseLock.release();
          //  camera2Listener.cameraOperned(mPreviewSize);
        }

        @Override
        public void onDisconnected(@NonNull CameraDevice cameraDevice) {
            mCameraOpenCloseLock.release();
            cameraDevice.close();
            mCameraDevice = null;
        }

        @Override
        public void onError(@NonNull CameraDevice cameraDevice, int error) {
            mCameraOpenCloseLock.release();
            cameraDevice.close();
            mCameraDevice = null;
            camera2Listener.cameraError(error);
        }

    };

    private CaptureRequest.Builder mPreviewBuilder;
    private CameraManager manager;

    private CameraCharacteristics mCharacteristics;
    private boolean mFaceDetectSupported;
    private int mFaceDetectMode;
    Face[] faces = null;
    // Draw rectangles and other fancy stuff:
    private FaceOverlayView mFaceView;
    public int cameraWidth, cameraHeight;
    // We need the phone orientation to correctly draw the overlay:
    private int mOrientation;
    private int mOrientationCompensation;
    private Integer mSensorOrientation;

    private Context context;
    private Activity activity;

    /**
     * MediaRecorder
     */
    private MediaRecorder mMediaRecorder;

    /**
     * Whether the app is recording video now
     */
    private boolean mIsRecordingVideo;

    private String cameraId;
    private String mNextVideoAbsolutePath;
    private Cam2AutoFitTextureView mTextureView;
    private FocusImageView focusImage;
    private int autoExposure;
    private CamSurfaceTextureListener mSurfaceTextureListener;
    private AwbSeekBar awbView;
    private PreviewSessionCallback mCaptureCallback;
    private int screenCurrentRotation;
    private Handler mMainhandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 1:
                    screenCurrentRotation = (int) msg.obj;
                    camera2Listener.orientationChanged(screenCurrentRotation);
                    break;
            }
        }
    };

    public Camera2(Context context, Cam2AutoFitTextureView mTextureView, ICamera2 camera2Listener) {
        this.context = context;
        this.activity = (Activity) context;
        this.mTextureView = mTextureView;
        this.camera2Listener = camera2Listener;

        // Setup a new OrientationEventListener.  This is used to handle rotation events like a
        // 180 degree rotation that do not normally trigger a call to onCreate to do view re-layout
        // or otherwise cause the preview TextureView's size to change.
      //  mOrientationListener = new Cam2OrientationEventListener(context, mTextureView, mPreviewSize, mMainhandler);
        setupManager();
        startBackgroundThread();
        setUpFilters();
        mSurfaceTextureListener = new CamSurfaceTextureListener(this, mTextureView, activity);
        mSurfaceTextureListener.setFilterTextureGL(mTextureViewGLWrapper);
    }

    public void setFaceView(FaceOverlayView mFaceView) {
        this.mFaceView = mFaceView;
    }

    private void setupManager() {
        manager = (CameraManager) context.getSystemService(Context.CAMERA_SERVICE);
        try {
            if (manager != null) {
                for (String cameraId : manager.getCameraIdList()) {
                    Log.d(TAG, cameraId + " CameraId");
                }
            }
            cameraId = manager.getCameraIdList()[0];
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    public void onResume() {
        startBackgroundThread();
        if (mTextureView.isAvailable()) {
            openCamera(mTextureView.getWidth(), mTextureView.getHeight());
        } else {
            mTextureView.setSurfaceTextureListener(mSurfaceTextureListener);
        }
        if (mOrientationListener != null && mOrientationListener.canDetectOrientation()) {
            mOrientationListener.enable();
        }
    }

    public void onPause() {
        closeCamera();
        stopBackgroundThread();
        if (mOrientationListener != null) {
            mOrientationListener.disable();
        }
    }

    /**
     * Tries to open a {@link CameraDevice}. The result is listened by `mStateCallback`.
     */
    @SuppressWarnings("MissingPermission")
    public void openCamera(int width, int height) {
        if (!hasPermissionsGranted(DialogHelper.VIDEO_PERMISSIONS)) {
            camera2Listener.requestVideoPermissions();
            return;
        }
        if (null == activity || activity.isFinishing() || filterTexture == null) {
            return;
        }
        try {
            Log.d(TAG, "tryAcquire");
            if (!mCameraOpenCloseLock.tryAcquire(2500, TimeUnit.MILLISECONDS)) {
                throw new RuntimeException("Time out waiting to lock camera opening.");
            }

            // Choose the sizes for camera preview and video recording
            mCharacteristics = manager.getCameraCharacteristics(cameraId);
            StreamConfigurationMap map = mCharacteristics
                    .get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);

            //Face Detection
//            int[] FD = mCharacteristics.get(CameraCharacteristics.STATISTICS_INFO_AVAILABLE_FACE_DETECT_MODES);
//            Integer faceCount = mCharacteristics.get(CameraCharacteristics.STATISTICS_INFO_MAX_FACE_COUNT);
//            int maxFD = faceCount != null ? faceCount : 0;
//
//            if (FD != null && FD.length > 0) {
//                List<Integer> fdList = new ArrayList<>();
//                for (int FaceD : FD
//                        ) {
//                    fdList.add(FaceD);
//                    Log.d(TAG, "setUpCameraOutputs: FD type:" + Integer.toString(FaceD));
//                }
//                Log.d(TAG, "setUpCameraOutputs: FD count" + Integer.toString(maxFD));
//
//                if (maxFD > 0) {
//                    mFaceDetectSupported = true;
//                    mFaceDetectMode = Collections.max(fdList);
//                }
//            }

            mSensorOrientation = mCharacteristics.get(CameraCharacteristics.SENSOR_ORIENTATION);
            if (mSensorOrientation != null)
                mFaceView.setDisplayOrientation(mSensorOrientation);
            if (map == null) {
                throw new RuntimeException("Cannot get available preview/video sizes");
            }

            camCorderProfile = CamcorderProfile.get(CamcorderProfile.QUALITY_HIGH);
//            mVideoSize = Camera2Helper.chooseVideoSize(map.getOutputSizes(MediaRecorder.class));
//            mPreviewSize = Camera2Helper.chooseOptimalSize(map.getOutputSizes(SurfaceTexture.class),
//                    width, height, mVideoSize);
            mVideoSize = chooseOptimalSize(com.dastanapps.app.Size.fromArray2(map.getOutputSizes(MediaRecorder.class)),
                    mTextureView.getWidth(), mTextureView.getHeight(), new com.dastanapps.app.Size(camCorderProfile.videoFrameWidth, camCorderProfile.videoFrameHeight));
            if (mVideoSize == null || mVideoSize.getWidth() > camCorderProfile.videoFrameWidth ||
                    mVideoSize.getHeight() > camCorderProfile.videoFrameHeight) {
                mVideoSize = getSizeWithClosestRatio(com.dastanapps.app.Size.fromArray2(map.getOutputSizes(MediaRecorder.class)), camCorderProfile.videoFrameWidth, camCorderProfile.videoFrameHeight);
            } else if (mVideoSize == null || mVideoSize.getWidth() > camCorderProfile.videoFrameWidth
                    || mVideoSize.getHeight() > camCorderProfile.videoFrameHeight)
                mVideoSize = getSizeWithClosestRatio(com.dastanapps.app.Size.fromArray2(map.getOutputSizes(MediaRecorder.class)), camCorderProfile.videoFrameWidth, camCorderProfile.videoFrameHeight);

            if (mTextureView.getHeight() * mTextureView.getWidth() > mVideoSize.getWidth() * mVideoSize.getHeight()) {
                mPreviewSize = getOptimalPreviewSize(com.dastanapps.app.Size.fromArray2(map.getOutputSizes(SurfaceTexture.class)), mVideoSize.getWidth(), mVideoSize.getHeight());
            } else {
                mPreviewSize = getOptimalPreviewSize(com.dastanapps.app.Size.fromArray2(map.getOutputSizes(SurfaceTexture.class)), mTextureView.getWidth(), mTextureView.getHeight());
            }

            if (mPreviewSize == null)
                mPreviewSize = getSizeWithClosestRatio(com.dastanapps.app.Size.fromArray2(map.getOutputSizes(SurfaceTexture.class)), mVideoSize.getWidth(), mVideoSize.getHeight());
            int orientation = context.getResources().getConfiguration().orientation;
            if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
                mTextureView.setAspectRatio(mPreviewSize.getWidth(), mPreviewSize.getHeight());
            } else {
                mTextureView.setAspectRatio(mPreviewSize.getHeight(), mPreviewSize.getWidth());
            }
           // Camera2Helper.configureTransform(activity, mPreviewSize, mTextureView, width, height);
            manager.openCamera(cameraId, mStateCallback, null);
        } catch (CameraAccessException e) {
            Toast.makeText(activity, "Cannot access the camera.", Toast.LENGTH_SHORT).show();
            activity.finish();
        } catch (NullPointerException e) {
            camera2Listener.cameraError(0);
        } catch (InterruptedException e) {
            throw new RuntimeException("Interrupted while trying to lock camera opening.");
        }
    }

    public void closeCamera() {
        try {
            mCameraOpenCloseLock.acquire();
            closePreviewSession();
            if (null != mCameraDevice) {
                mCameraDevice.close();
                mCameraDevice = null;
            }
            if (null != mMediaRecorder) {
                mMediaRecorder.release();
                mMediaRecorder = null;
            }
            filterTexture = null;
        } catch (InterruptedException e) {
            throw new RuntimeException("Interrupted while trying to lock camera closing.");
        } finally {
            mCameraOpenCloseLock.release();
        }
    }

    /**
     * Start the camera preview.
     */
    public void startPreview() {
        if (null == mCameraDevice || !mTextureView.isAvailable() || null == mPreviewSize) {
            return;
        }
        try {
            SurfaceTexture texture = filterTexture;//mTextureView.getSurfaceTexture();
            texture.setDefaultBufferSize(mPreviewSize.getWidth(), mPreviewSize.getHeight());
            mPreviewBuilder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            CameraCharacteristics cameraCharacteristics = manager.getCameraCharacteristics(cameraId);
            int effects[] = cameraCharacteristics.get(CameraCharacteristics.CONTROL_AVAILABLE_EFFECTS);
            if (effects != null && effects.length > 0) {
                for (int effect : effects) {
                    Log.d(TAG, effect + " Effects");
                }
            }

            Surface previewSurface = new Surface(texture);
            mPreviewBuilder.addTarget(previewSurface);

            mCameraDevice.createCaptureSession(Collections.singletonList(previewSurface), cameraSessionStateCallback, mBackgroundHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private void setFaceDetect(CaptureRequest.Builder requestBuilder, int faceDetectMode) {
        if (mFaceDetectSupported) {
            //requestBuilder.set(CaptureRequest.STATISTICS_FACE_DETECT_MODE, faceDetectMode);
        }

    }

    /**
     * Update the camera preview. {@link #startPreview()} needs to be called in advance.
     */
    public void updatePreview() {
        if (null == mCameraDevice) {
            return;
        }
        try {
            setUpCaptureRequestBuilder(mPreviewBuilder);
            HandlerThread thread = new HandlerThread("CameraPreview");
            thread.start();
            mPreviewSession.setRepeatingRequest(mPreviewBuilder.build(), mCaptureCallback, mBackgroundHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    /**
     * Configure the given {@link CaptureRequest.Builder} to use auto-focus, auto-exposure, and
     * auto-white-balance controls if available.
     * <p/>
     *
     * @param builder the builder to configure.
     */
    private void setUpCaptureRequestBuilder(CaptureRequest.Builder builder) {
        // Enable auto-magical 3A run by camera device
        builder.set(CaptureRequest.CONTROL_MODE, CaptureRequest.CONTROL_MODE_AUTO);

//        Float minFocusDist =
//                mCharacteristics.get(CameraCharacteristics.LENS_INFO_MINIMUM_FOCUS_DISTANCE);
//
//        // If MINIMUM_FOCUS_DISTANCE is 0, lens is fixed-focus and we need to skip the AF run.
//        mNoAFRun = (minFocusDist == null || minFocusDist == 0);
//
//        if (!mNoAFRun) {
//            // If there is a "continuous picture" mode available, use it, otherwise default to AUTO.
//            if (Camera2Helper.contains(mCharacteristics.get(
//                    CameraCharacteristics.CONTROL_AF_AVAILABLE_MODES),
//                    CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE)) {
//                builder.set(CaptureRequest.CONTROL_AF_MODE,
//                        CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE);
//            } else {
//                builder.set(CaptureRequest.CONTROL_AF_MODE,
//                        CaptureRequest.CONTROL_AF_MODE_AUTO);
//            }
//        }
//
//        // If there is an auto-magical flash control mode available, use it, otherwise default to
//        // the "on" mode, which is guaranteed to always be available.
//        if (Camera2Helper.contains(mCharacteristics.get(
//                CameraCharacteristics.CONTROL_AE_AVAILABLE_MODES),
//                CaptureRequest.CONTROL_AE_MODE_ON_AUTO_FLASH)) {
//            builder.set(CaptureRequest.CONTROL_AE_MODE,
//                    CaptureRequest.CONTROL_AE_MODE_ON_AUTO_FLASH);
//        } else {
//            builder.set(CaptureRequest.CONTROL_AE_MODE,
//                    CaptureRequest.CONTROL_AE_MODE_ON);
//        }
//
//        // If there is an auto-magical white balance control mode available, use it.
//        if (Camera2Helper.contains(mCharacteristics.get(
//                CameraCharacteristics.CONTROL_AWB_AVAILABLE_MODES),
//                CaptureRequest.CONTROL_AWB_MODE_AUTO)) {
//            // Allow AWB to run auto-magically if this device supports this
//            builder.set(CaptureRequest.CONTROL_AWB_MODE,
//                    CaptureRequest.CONTROL_AWB_MODE_AUTO);
//        }
    }

    private void closePreviewSession() {
        mIsRecordingVideo = false;
        if (mPreviewSession != null) {
            mPreviewSession.close();
            mPreviewSession = null;
        }
    }

    public void stopPreviewSession() {
        try {
            mPreviewSession.stopRepeating();
            mPreviewSession.abortCaptures();
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private Face detectedFace;
    private Rect rectangleFace;

    public void startRecordingVideo() {
        if (null == mCameraDevice || !mTextureView.isAvailable() || null == mPreviewSize) {
            return;
        }
        closePreviewSession();
        if (prepareMediaRecorder()) {
            try {
                SurfaceTexture texture = filterTexture;//mTextureView.getSurfaceTexture();
                texture.setDefaultBufferSize(mPreviewSize.getWidth(), mPreviewSize.getHeight());
                mPreviewBuilder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_RECORD);
                List<Surface> surfaces = new ArrayList<>();

                // Set up Surface for the camera preview
                Surface previewSurface = new Surface(texture);
                surfaces.add(previewSurface);
                mPreviewBuilder.addTarget(previewSurface);

                // Set up Surface for the MediaRecorder
                Surface recorderSurface = mMediaRecorder.getSurface();
                surfaces.add(recorderSurface);
                mPreviewBuilder.addTarget(recorderSurface);

                mIsRecordingVideo = true;

                // Start a capture session
                // Once the session starts, we can update the UI and start recording
                mCameraDevice.createCaptureSession(surfaces, new CameraCaptureSession.StateCallback() {
                    @Override
                    public void onConfigured(@NonNull CameraCaptureSession session) {
                        mPreviewSession = session;
                        updatePreview();
                    }

                    @Override
                    public void onConfigureFailed(@NonNull CameraCaptureSession session) {

                    }
                }, mBackgroundHandler);
            } catch (CameraAccessException e) {
                e.printStackTrace();
            }
        } else {
            Toast.makeText(context, "Video prepare failed", Toast.LENGTH_LONG).show();
            Log.e(TAG, "media recorder prepared failed");
        }

    }

    public void setCameraWidthHeight() {
        try {
            CameraCharacteristics charac = manager.getCameraCharacteristics(cameraId);

            Rect recCameraBounds = charac.get(
                    CameraCharacteristics.SENSOR_INFO_ACTIVE_ARRAY_SIZE);
            if (recCameraBounds != null) {
                cameraWidth = recCameraBounds.right;
                cameraHeight = recCameraBounds.bottom;
            }
            mFaceView.setCameraBounds(recCameraBounds);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    public void toggleFlash() {
        if (flashMode == FLASH_OFF && cameraId.equals("0")) {
            flashMode = FLASH_ON;
            mPreviewBuilder.set(CaptureRequest.CONTROL_AE_MODE, CaptureRequest.CONTROL_AE_MODE_ON);
            mPreviewBuilder.set(CaptureRequest.FLASH_MODE, CameraMetadata.FLASH_MODE_TORCH);
            camera2Listener.updateFlashMode(FLASH_ON);
        } else if (flashMode == FLASH_ON) {
            flashMode = FLASH_AUTO;
            mPreviewBuilder.set(CaptureRequest.CONTROL_AE_MODE, CaptureRequest.CONTROL_AE_MODE_ON);
            mPreviewBuilder.set(CaptureRequest.FLASH_MODE, CameraMetadata.FLASH_MODE_OFF);
            camera2Listener.updateFlashMode(FLASH_OFF);
        } else if (flashMode == FLASH_AUTO) {
            flashMode = FLASH_OFF;
            // If there is an auto-magical flash control mode available, use it, otherwise default to
            // the "on" mode, which is guaranteed to always be available.
            if (Camera2Helper.contains(mCharacteristics.get(CameraCharacteristics.CONTROL_AE_AVAILABLE_MODES),
                    CaptureRequest.CONTROL_AE_MODE_ON_AUTO_FLASH)) {
                mPreviewBuilder.set(CaptureRequest.CONTROL_AE_MODE, CaptureRequest.CONTROL_AE_MODE_ON);
                mPreviewBuilder.set(CaptureRequest.CONTROL_AE_MODE, CaptureRequest.CONTROL_AE_MODE_ON_AUTO_FLASH);
            } else {
                mPreviewBuilder.set(CaptureRequest.CONTROL_AE_MODE, CaptureRequest.CONTROL_AE_MODE_ON);
                mPreviewBuilder.set(CaptureRequest.FLASH_MODE, CameraMetadata.FLASH_MODE_OFF);
            }
            camera2Listener.updateFlashMode(FLASH_AUTO);
        }
        try {
            mPreviewSession.setRepeatingRequest(mPreviewBuilder.build(), mCaptureCallback, mBackgroundHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    public DialogHelper.EffectsDialog showEffectsDialog() {
        int effects[] = mCharacteristics.get(CameraCharacteristics.CONTROL_AVAILABLE_EFFECTS);
        String[] intList = new String[effects.length];
        for (int index = 0; index < effects.length; index++) {
            intList[index] = effects[index] + "|" + Camera2Helper.getEffectName(effects[index]);
        }
        return DialogHelper.EffectsDialog.newInstance().setEffects(mPreviewSession, mPreviewBuilder, intList);
    }

    public DialogHelper.SceneDialog showScenesDialog() {
        int scenes[] = mCharacteristics.get(CameraCharacteristics.CONTROL_AVAILABLE_SCENE_MODES);
        String[] intList = new String[scenes.length];
        for (int index = 0; index < scenes.length; index++) {
            intList[index] = scenes[index] + "|" + Camera2Helper.getSceneNames(scenes[index]);
        }
        return DialogHelper.SceneDialog.newInstance().setScenes(mPreviewSession, mPreviewBuilder, intList);
    }

    /**
     * Starts a background thread and its {@link Handler}.
     */
    public void startBackgroundThread() {
        mBackgroundThread = new HandlerThread("CameraBackground");
        mBackgroundThread.start();
        mBackgroundHandler = new Handler(mBackgroundThread.getLooper());
    }

    /**
     * Stops the background thread and its {@link Handler}.
     */
    private void stopBackgroundThread() {
        mBackgroundThread.quitSafely();
        try {
            mBackgroundThread.join();
            mBackgroundThread = null;
            mBackgroundHandler = null;
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /*public Size getPreviewSize() {
        return mPreviewSize;
    }*/

    private boolean hasPermissionsGranted(String[] permissions) {
        for (String permission : permissions) {
            if (ActivityCompat.checkSelfPermission(activity, permission)
                    != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    private boolean prepareMediaRecorder() {
        try {
            mMediaRecorder = new MediaRecorder();
            mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.CAMCORDER);
            mMediaRecorder.setVideoSource(MediaRecorder.VideoSource.SURFACE);

            mMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
            mMediaRecorder.setVideoFrameRate(camCorderProfile.videoFrameRate);
            mMediaRecorder.setVideoSize(mVideoSize.getWidth(), mVideoSize.getHeight());
            mMediaRecorder.setVideoEncodingBitRate(camCorderProfile.videoBitRate);
            mMediaRecorder.setVideoEncoder(camCorderProfile.videoCodec);

            mMediaRecorder.setAudioEncodingBitRate(camCorderProfile.audioBitRate);
            mMediaRecorder.setAudioChannels(camCorderProfile.audioChannels);
            mMediaRecorder.setAudioSamplingRate(camCorderProfile.audioSampleRate);
            mMediaRecorder.setAudioEncoder(camCorderProfile.audioCodec);

            if (mNextVideoAbsolutePath == null || mNextVideoAbsolutePath.isEmpty()) {
                mNextVideoAbsolutePath = getVideoFilePath(activity);
            }
            mMediaRecorder.setOutputFile(mNextVideoAbsolutePath);

            mMediaRecorder.setMaxDuration(30 * 1000);
            int rotation = activity.getWindowManager().getDefaultDisplay().getRotation();
            switch (mSensorOrientation) {
                case Camera2.SENSOR_ORIENTATION_DEFAULT_DEGREES:
                    mMediaRecorder.setOrientationHint(CameraHelper.Companion.getDEFAULT_ORIENTATIONS().get(rotation));
                    break;
                case Camera2.SENSOR_ORIENTATION_INVERSE_DEGREES:
                    mMediaRecorder.setOrientationHint(CameraHelper.Companion.getINVERSE_ORIENTATIONS().get(rotation));
                    break;
            }
            mMediaRecorder.setOrientationHint(CameraHelper.Companion.getDEFAULT_ORIENTATIONS().get(screenCurrentRotation));
            mMediaRecorder.setOnInfoListener(new MediaRecorder.OnInfoListener() {
                @Override
                public void onInfo(MediaRecorder mr, int what, int extra) {
                    switch (what) {
                        case MediaRecorder.MEDIA_RECORDER_INFO_UNKNOWN:
                            // NOP
                            break;
                        case MediaRecorder.MEDIA_RECORDER_INFO_MAX_DURATION_REACHED:
                            stopRecordingVideo();
                            break;
                        case MediaRecorder.MEDIA_RECORDER_INFO_MAX_FILESIZE_REACHED:
                            stopRecordingVideo();
                            break;
                        default:
                            break;
                    }
                }
            });
            mMediaRecorder.prepare();
            return true;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    private String getVideoFilePath(Context context) {
        final File dir = context.getExternalFilesDir(null);
        return (dir == null ? "" : (dir.getAbsolutePath() + "/"))
                + System.currentTimeMillis() + ".mp4";
    }

    private Handler stopRecodeingHandler = new Handler();

    private void stopRecordingVideo() {
        // UI
        mIsRecordingVideo = false;
        stopPreviewSession();

        stopRecodeingHandler.removeCallbacks(stopRecodingRunnable);
        stopRecodeingHandler.postDelayed(stopRecodingRunnable, 100);

        if (null != activity) {
            Toast.makeText(activity, "Video saved: " + mNextVideoAbsolutePath,
                    Toast.LENGTH_SHORT).show();
            Log.d(TAG, "Video saved: " + mNextVideoAbsolutePath);
        }
        mNextVideoAbsolutePath = null;
        startPreview();
        camera2Listener.cameraRecordingStopped();
    }

    public void switchFaces() {
        if (cameraId.equals("0")) cameraId = "1"; //Front
        else cameraId = "0"; //Back
        closeCamera();
        openCamera(mTextureView.getWidth(), mTextureView.getHeight());
    }

    public void toggleRecording() {
        if (mIsRecordingVideo) {
            stopRecordingVideo();
        } else {
            startRecordingVideo();
        }
    }

    private CameraCaptureSession.StateCallback cameraSessionStateCallback =
            new CameraCaptureSession.StateCallback() {

                @Override
                public void onConfigured(@NonNull CameraCaptureSession cameraCaptureSession) {
                    mPreviewSession = cameraCaptureSession;
                    newSession();
                    updatePreview();
                    mPreviewBuilder.set(CaptureRequest.CONTROL_AE_EXPOSURE_COMPENSATION, 12);
                    if (mIsRecordingVideo) {
                        mPreviewBuilder.set(CaptureRequest.CONTROL_AWB_MODE, CaptureRequest.CONTROL_AWB_MODE_AUTO);
                        mPreviewBuilder.set(CaptureRequest.CONTROL_AE_MODE, CaptureRequest.CONTROL_AE_MODE_ON);
                        mPreviewBuilder.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_AUTO);
                        camera2Listener.cameraRecordingStarted();
                        // Start recording
                        mMediaRecorder.start();
                    }
                }

                @Override
                public void onConfigureFailed(@NonNull CameraCaptureSession cameraCaptureSession) {
                    if (null != activity) {
                        Toast.makeText(activity, "Failed", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onClosed(@NonNull CameraCaptureSession session) {
                    super.onClosed(session);
                    Log.e(TAG, "Session Closed");
                }
            };

    private void newSession() {
        mCaptureCallback = new PreviewSessionCallback(focusImage, mBackgroundHandler, mTextureView);
        mCaptureCallback.setCameraSettings(mCharacteristics, mPreviewSession, mPreviewBuilder);

        mTextureView.setCameraSettings(mCharacteristics, mPreviewSession, mPreviewBuilder, mCaptureCallback);
        if (awbView != null)
            awbView.setmOnAwbSeekBarChangeListener(new AwbSeekBarChangeListener(mPreviewBuilder, mPreviewSession, mBackgroundHandler, mCaptureCallback));
        setFaceDetect(mPreviewBuilder, mFaceDetectMode);
    }

    private Runnable stopRecodingRunnable = new Runnable() {
        @Override
        public void run() {
            // / Stop recording
            mMediaRecorder.stop();
            mMediaRecorder.reset();
        }
    };

    public void setFocusImage(FocusImageView focusImage) {
        this.focusImage = focusImage;
    }

    public void setAutoExposure(int autoExposure) {
        Log.d(TAG, mCharacteristics.get(CameraCharacteristics.CONTROL_AE_COMPENSATION_STEP) + " Auto Exposure Steps");
        Log.d(TAG, " Auto Exposure Steps: " + autoExposure);
        Range<Integer> range1 = mCharacteristics.get(CameraCharacteristics.CONTROL_AE_COMPENSATION_RANGE);
        int maxmax = range1.getUpper();
        int minmin = range1.getLower();
        int all = (-minmin) + maxmax;
        int time = 100 / all;
        int ae = ((autoExposure / time) - maxmax) > maxmax ? maxmax : ((autoExposure / time) - maxmax) < minmin ? minmin : ((autoExposure / time) - maxmax);
        mPreviewBuilder.set(CaptureRequest.CONTROL_AE_MODE, CameraMetadata.CONTROL_AE_MODE_ON);
        mPreviewBuilder.set(CaptureRequest.CONTROL_AE_EXPOSURE_COMPENSATION, ae);
        try {
            mPreviewSession.setRepeatingRequest(mPreviewBuilder.build(), null, null);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    public void setContrast(int value) {
        //set def channels (used for contrast)
        TonemapCurve tc = mPreviewBuilder.get(CaptureRequest.TONEMAP_CURVE);
        if (tc != null) {
            channels = new float[3][];
            for (int chanel = TonemapCurve.CHANNEL_RED; chanel <= TonemapCurve.CHANNEL_BLUE; chanel++) {
                float[] array = new float[tc.getPointCount(chanel) * 2];
                tc.copyColorCurve(chanel, array, 0);
                channels[chanel] = array;
            }
        }
        final int minContrast = 0;
        final int maxContrast = 1;

        if (channels == null || value > 100 || value < 0) {
            return;
        }

        float contrast = minContrast + (maxContrast - minContrast) * (value / 100f);

        float[][] newValues = new float[3][];
        for (int chanel = TonemapCurve.CHANNEL_RED; chanel <= TonemapCurve.CHANNEL_BLUE; chanel++) {
            float[] array = new float[channels[chanel].length];
            System.arraycopy(channels[chanel], 0, array, 0, array.length);
            for (int i = 0; i < array.length; i++) {
                array[i] *= contrast;
            }
            newValues[chanel] = array;
        }
        tc = new TonemapCurve(newValues[TonemapCurve.CHANNEL_RED], newValues[TonemapCurve.CHANNEL_GREEN], newValues[TonemapCurve.CHANNEL_BLUE]);
        mPreviewBuilder.set(CaptureRequest.TONEMAP_MODE, CaptureRequest.TONEMAP_MODE_CONTRAST_CURVE);
        mPreviewBuilder.set(CaptureRequest.TONEMAP_CURVE, tc);
        try {
            mPreviewSession.setRepeatingRequest(mPreviewBuilder.build(), null, null);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    /**
     * Image Sensor sensitivity
     *
     * @param isoValue sensitivity value
     */
    public void setISO(int isoValue) {
        Log.d(TAG, Camera2Helper.isHardwareLevelSupported(mCharacteristics, INFO_SUPPORTED_HARDWARE_LEVEL_FULL) + " FULL Capablities");
        Log.d(TAG, Camera2Helper.isHardwareLevelSupported(mCharacteristics, INFO_SUPPORTED_HARDWARE_LEVEL_LIMITED) + " Limited Capablities");
        Log.d(TAG, Camera2Helper.isHardwareLevelSupported(mCharacteristics, INFO_SUPPORTED_HARDWARE_LEVEL_LEGACY) + " Legacy Capablities");
        // if (Camera2Helper.isHardwareLevelSupported(mCharacteristics, INFO_SUPPORTED_HARDWARE_LEVEL_FULL)) {
        mPreviewBuilder.set(CaptureRequest.CONTROL_AE_MODE, CameraMetadata.CONTROL_AE_MODE_OFF);
        mPreviewBuilder.set(CaptureRequest.CONTROL_MODE, INFO_SUPPORTED_HARDWARE_LEVEL_LEGACY);
        Range<Integer> range = mCharacteristics.get(CameraCharacteristics.SENSOR_INFO_SENSITIVITY_RANGE);
        if (range != null) {
            int max1 = range.getUpper();//10000
            int min1 = range.getLower();//100
            int iso = ((isoValue * (max1 - min1)) / 100 + min1);
            mPreviewBuilder.set(CaptureRequest.SENSOR_SENSITIVITY, iso);
        }
        //}
        try {
            mPreviewSession.setRepeatingRequest(mPreviewBuilder.build(), null, null);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    public void setAwbView(AwbSeekBar awbView) {
        this.awbView = awbView;
    }

    private TextureViewGLWrapper mTextureViewGLWrapper;
    private SurfaceTexture filterTexture;

    private void setUpFilters() {
        mTextureViewGLWrapper = new TextureViewGLWrapper(new DefaultCameraRenderer(context));
        mTextureViewGLWrapper.setListener(new TextureViewGLWrapper.EGLSurfaceTextureListener() {
            @Override
            public void onSurfaceTextureReady(SurfaceTexture surfaceTexture) {
                filterTexture = surfaceTexture;
                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        openCamera(mTextureView.getWidth(), mTextureView.getHeight());
                    }
                });
            }
        }, mBackgroundHandler);
    }

    public void changeFilter() {
        if (mTextureViewGLWrapper != null) {
            mTextureViewGLWrapper.changeFragmentShader();
        }
    }

    public static com.dastanapps.app.Size chooseOptimalSize(com.dastanapps.app.Size[] choices, int width, int height, com.dastanapps.app.Size aspectRatio) {
        // Collect the supported resolutions that are at least as big as the preview Surface
        List<com.dastanapps.app.Size> bigEnough = new ArrayList<>();
        int w = aspectRatio.getWidth();
        int h = aspectRatio.getHeight();
        for (com.dastanapps.app.Size option : choices) {
            if (option.getHeight() == option.getWidth() * h / w &&
                    option.getWidth() >= width && option.getHeight() >= height) {
                bigEnough.add(option);
            }
        }

        // Pick the smallest of those, assuming we found any
        if (bigEnough.size() > 0) {
            return Collections.min(bigEnough, new CompareSizesByArea2());
        } else {
            Log.e(TAG, "Couldn't find any suitable preview size");
            return null;
        }
    }

    public static com.dastanapps.app.Size getSizeWithClosestRatio(com.dastanapps.app.Size[] sizes, int width, int height) {

        if (sizes == null) return null;

        double MIN_TOLERANCE = 100;
        double targetRatio = (double) height / width;
        com.dastanapps.app.Size optimalSize = null;
        double minDiff = Double.MAX_VALUE;

        int targetHeight = height;

        for (com.dastanapps.app.Size size : sizes) {
//            if (size.getWidth() == width && size.getHeight() == height)
//                return size;

            double ratio = (double) size.getHeight() / size.getWidth();

            if (Math.abs(ratio - targetRatio) < MIN_TOLERANCE) MIN_TOLERANCE = ratio;
            else continue;

            if (Math.abs(size.getHeight() - targetHeight) < minDiff) {
                optimalSize = size;
                minDiff = Math.abs(size.getHeight() - targetHeight);
            }
        }

        if (optimalSize == null) {
            minDiff = Double.MAX_VALUE;
            for (com.dastanapps.app.Size size : sizes) {
                if (Math.abs(size.getHeight() - targetHeight) < minDiff) {
                    optimalSize = size;
                    minDiff = Math.abs(size.getHeight() - targetHeight);
                }
            }
        }
        return optimalSize;
    }

    public static com.dastanapps.app.Size getOptimalPreviewSize(com.dastanapps.app.Size[] sizes, int width, int height) {

        if (sizes == null) return null;

        final double ASPECT_TOLERANCE = 0.1;
        double targetRatio = (double) height / width;
        com.dastanapps.app.Size optimalSize = null;
        double minDiff = Double.MAX_VALUE;

        int targetHeight = height;

        for (com.dastanapps.app.Size size : sizes) {
//            if (size.getWidth() == width && size.getHeight() == height)
//                return size;
            double ratio = (double) size.getWidth() / size.getHeight();
            if (Math.abs(ratio - targetRatio) > ASPECT_TOLERANCE) continue;
            if (Math.abs(size.getHeight() - targetHeight) < minDiff) {
                optimalSize = size;
                minDiff = Math.abs(size.getHeight() - targetHeight);
            }
        }

        if (optimalSize == null) {
            minDiff = Double.MAX_VALUE;
            for (com.dastanapps.app.Size size : sizes) {
                if (Math.abs(size.getHeight() - targetHeight) < minDiff) {
                    optimalSize = size;
                    minDiff = Math.abs(size.getHeight() - targetHeight);
                }
            }
        }
        return optimalSize;
    }

    private static class CompareSizesByArea2 implements Comparator<com.dastanapps.app.Size> {
        @Override
        public int compare(com.dastanapps.app.Size lhs, com.dastanapps.app.Size rhs) {
            // We cast here to ensure the multiplications won't overflow
            return Long.signum((long) lhs.getWidth() * lhs.getHeight() -
                    (long) rhs.getWidth() * rhs.getHeight());
        }

    }
}
