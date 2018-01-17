package com.dastanapps.camera2;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Rect;
import android.graphics.SurfaceTexture;
import android.hardware.SensorManager;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.CaptureResult;
import android.hardware.camera2.TotalCaptureResult;
import android.hardware.camera2.params.Face;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.MediaRecorder;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.util.Size;
import android.util.SparseIntArray;
import android.view.OrientationEventListener;
import android.view.Surface;
import android.view.TextureView;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.dastanapps.view.AnimationImageView;
import com.dastanapps.view.FaceOverlayView;
import com.dastanapps.view.Util;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

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
    public static final SparseIntArray DEFAULT_ORIENTATIONS = new SparseIntArray();
    public static final SparseIntArray INVERSE_ORIENTATIONS = new SparseIntArray();

    static {
        DEFAULT_ORIENTATIONS.append(Surface.ROTATION_0, 90);
        DEFAULT_ORIENTATIONS.append(Surface.ROTATION_90, 0);
        DEFAULT_ORIENTATIONS.append(Surface.ROTATION_180, 270);
        DEFAULT_ORIENTATIONS.append(Surface.ROTATION_270, 180);
    }

    static {
        INVERSE_ORIENTATIONS.append(Surface.ROTATION_0, 270);
        INVERSE_ORIENTATIONS.append(Surface.ROTATION_90, 180);
        INVERSE_ORIENTATIONS.append(Surface.ROTATION_180, 90);
        INVERSE_ORIENTATIONS.append(Surface.ROTATION_270, 0);
    }

    public static int FLASH_OFF = 0;
    public static int FLASH_ON = 1;
    public static int FLASH_AUTO = 2;

    private int flashMode = FLASH_OFF;
    /**
     * The current state of camera state for taking pictures.
     *
     * @see #mCaptureCallback
     */
    private int mState = STATE_PREVIEW;

    /**
     * Camera state: Showing camera preview.
     */
    private static final int STATE_PREVIEW = 0;

    /**
     * Camera state: Waiting for the focus to be locked.
     */
    private static final int STATE_WAITING_LOCK = 1;

    /**
     * Camera state: Waiting for the exposure to be precapture state.
     */
    private static final int STATE_WAITING_PRECAPTURE = 2;

    /**
     * Camera state: Waiting for the exposure state to be something other than precapture.
     */
    private static final int STATE_WAITING_NON_PRECAPTURE = 3;

    /**
     * Whether or not the currently configured camera device is fixed-focus.
     */
    private boolean mNoAFRun = false;

    /**
     * A reference to the opened {@link CameraDevice}.
     */
    private CameraDevice mCameraDevice;

    /**
     * A reference to the current {@link CameraCaptureSession} for
     * preview.
     */
    private CameraCaptureSession mPreviewSession;

    /**
     * An {@link OrientationEventListener} used to determine when device rotation has occurred.
     * This is mainly necessary for when the device is rotated by 180 degrees, in which case
     * onCreate or onConfigurationChanged is not called as the view dimensions remain the same,
     * but the orientation of the has changed, and thus the preview rotation must be updated.
     */
    private OrientationEventListener mOrientationListener;

    /**
     * {@link TextureView.SurfaceTextureListener} handles several lifecycle events on a
     * {@link TextureView}.
     */

    /**
     * The {@link Size} of camera preview.
     */
    private Size mPreviewSize;

    /**
     * The {@link Size} of video recording.
     */
    private Size mVideoSize;

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
            camera2Listener.cameraOperned(mPreviewSize);
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
    private AutoFitTextureView mTextureView;
    private AnimationImageView focusImage;

    public Camera2(Context context, AutoFitTextureView mTextureView, ICamera2 camera2Listener) {
        this.context = context;
        this.activity = (Activity) context;
        this.mTextureView = mTextureView;
        this.camera2Listener = camera2Listener;
        setupManager();
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

        // Setup a new OrientationEventListener.  This is used to handle rotation events like a
        // 180 degree rotation that do not normally trigger a call to onCreate to do view re-layout
        // or otherwise cause the preview TextureView's size to change.
        mOrientationListener = new OrientationEventListener(context,
                SensorManager.SENSOR_DELAY_NORMAL) {
            @Override
            public void onOrientationChanged(int orientation) {
                if (mTextureView != null && mTextureView.isAvailable()) {
                    Camera2Helper.configureTransform(activity, mPreviewSize, mTextureView, mTextureView.getWidth(), mTextureView.getHeight());
                    // We keep the last known orientation. So if the user first orient
                    // the camera then point the camera to floor or sky, we still have
                    // the correct orientation.
                    if (orientation == ORIENTATION_UNKNOWN) return;
                    mOrientation = Util.roundOrientation(orientation, mOrientation);
                    // When the screen is unlocked, display rotation may change. Always
                    // calculate the up-to-date orientationCompensation.
                    int orientationCompensation = mOrientation
                            + Util.getDisplayRotation(activity);
                    if (mOrientationCompensation != orientationCompensation) {
                        mOrientationCompensation = orientationCompensation;
                        mFaceView.setOrientation(mOrientationCompensation);
                    }
                }
            }
        };
    }

    public void onResume() {
        if (mOrientationListener != null && mOrientationListener.canDetectOrientation()) {
            mOrientationListener.enable();
        }
    }

    public void onPause() {
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
        if (null == activity || activity.isFinishing()) {
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
            mVideoSize = Camera2Helper.chooseVideoSize(map.getOutputSizes(MediaRecorder.class));
            mPreviewSize = Camera2Helper.chooseOptimalSize(map.getOutputSizes(SurfaceTexture.class),
                    width, height, mVideoSize);

            int orientation = context.getResources().getConfiguration().orientation;
            if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
                mTextureView.setAspectRatio(mPreviewSize.getWidth(), mPreviewSize.getHeight());
            } else {
                mTextureView.setAspectRatio(mPreviewSize.getHeight(), mPreviewSize.getWidth());
            }
            Camera2Helper.configureTransform(activity, mPreviewSize, mTextureView, width, height);
            mMediaRecorder = new MediaRecorder();
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
            closePreviewSession();
            SurfaceTexture texture = mTextureView.getSurfaceTexture();
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

        Float minFocusDist =
                mCharacteristics.get(CameraCharacteristics.LENS_INFO_MINIMUM_FOCUS_DISTANCE);

        // If MINIMUM_FOCUS_DISTANCE is 0, lens is fixed-focus and we need to skip the AF run.
        mNoAFRun = (minFocusDist == null || minFocusDist == 0);

        if (!mNoAFRun) {
            // If there is a "continuous picture" mode available, use it, otherwise default to AUTO.
            if (Camera2Helper.contains(mCharacteristics.get(
                    CameraCharacteristics.CONTROL_AF_AVAILABLE_MODES),
                    CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE)) {
                builder.set(CaptureRequest.CONTROL_AF_MODE,
                        CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE);
            } else {
                builder.set(CaptureRequest.CONTROL_AF_MODE,
                        CaptureRequest.CONTROL_AF_MODE_AUTO);
            }
        }

        // If there is an auto-magical flash control mode available, use it, otherwise default to
        // the "on" mode, which is guaranteed to always be available.
        if (Camera2Helper.contains(mCharacteristics.get(
                CameraCharacteristics.CONTROL_AE_AVAILABLE_MODES),
                CaptureRequest.CONTROL_AE_MODE_ON_AUTO_FLASH)) {
            builder.set(CaptureRequest.CONTROL_AE_MODE,
                    CaptureRequest.CONTROL_AE_MODE_ON_AUTO_FLASH);
        } else {
            builder.set(CaptureRequest.CONTROL_AE_MODE,
                    CaptureRequest.CONTROL_AE_MODE_ON);
        }

        // If there is an auto-magical white balance control mode available, use it.
        if (Camera2Helper.contains(mCharacteristics.get(
                CameraCharacteristics.CONTROL_AWB_AVAILABLE_MODES),
                CaptureRequest.CONTROL_AWB_MODE_AUTO)) {
            // Allow AWB to run auto-magically if this device supports this
            builder.set(CaptureRequest.CONTROL_AWB_MODE,
                    CaptureRequest.CONTROL_AWB_MODE_AUTO);
        }
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
    private Integer afState = CameraMetadata.CONTROL_AF_STATE_INACTIVE;
    ;
    /**
     * A {@link CameraCaptureSession.CaptureCallback} that handles events related to JPEG capture.
     */
    private CameraCaptureSession.CaptureCallback mCaptureCallback
            = new CameraCaptureSession.CaptureCallback() {

        private void process(CaptureResult result) {
            switch (mState) {
                case STATE_PREVIEW: {
//                    Integer mode = result.get(CaptureResult.STATISTICS_FACE_DETECT_MODE);
//                    faces = result.get(CaptureResult.STATISTICS_FACES);
//                    if (faces != null && mode != null) {
//                        Log.e(TAG, "faces : " + faces.length + " , mode : " + mode);
//                    }
//                    // We have nothing to do when the camera preview is working normally.
//                    //But we can for example detect faces
//                    Face face[] = result.get(CaptureResult.STATISTICS_FACES);
//                    if (face != null && face.length > 0) {
//                        detectedFace = faces[0];
//                        rectangleFace = detectedFace.getBounds();
//                        Log.d(TAG, "face detected " + Integer.toString(face.length));
//                        activity.runOnUiThread(new Runnable() {
//                            @Override
//                            public void run() {
//                                // Update the view now!
//                                //mFaceView.setFaces(faces);
//                            }
//                        });
//                    }
                    break;
                }

                case STATE_WAITING_LOCK: {
                    afState = result.get(CaptureResult.CONTROL_AF_STATE);
                    if (afState == null) {
                    } else if (CaptureResult.CONTROL_AF_STATE_FOCUSED_LOCKED == afState ||
                            CaptureResult.CONTROL_AF_STATE_NOT_FOCUSED_LOCKED == afState) {
                        // CONTROL_AE_STATE can be null on some devices
                        Integer aeState = result.get(CaptureResult.CONTROL_AE_STATE);
                        if (aeState == null ||
                                aeState == CaptureResult.CONTROL_AE_STATE_CONVERGED) {
                        } else {
                        }
                    }
                    break;
                }
                case STATE_WAITING_PRECAPTURE: {
                    // CONTROL_AE_STATE can be null on some devices
                    Integer aeState = result.get(CaptureResult.CONTROL_AE_STATE);
                    if (aeState == null ||
                            aeState == CaptureResult.CONTROL_AE_STATE_PRECAPTURE ||
                            aeState == CaptureRequest.CONTROL_AE_STATE_FLASH_REQUIRED) {
                        mState = STATE_WAITING_NON_PRECAPTURE;
                    }
                    break;
                }
                case STATE_WAITING_NON_PRECAPTURE: {
                    boolean readyToCapture = true;
                    if (!mNoAFRun) {
                        afState = result.get(CaptureResult.CONTROL_AF_STATE);
                        if (afState == null) {
                            break;
                        }

                        // If auto-focus has reached locked state, we are ready to capture
                        readyToCapture =
                                (afState == CaptureResult.CONTROL_AF_STATE_FOCUSED_LOCKED ||
                                        afState == CaptureResult.CONTROL_AF_STATE_NOT_FOCUSED_LOCKED);
                    }

                    // If we are running on an non-legacy device, we should also wait until
                    // auto-exposure and auto-white-balance have converged as well before
                    // taking a picture.
                    if (!isLegacyLocked()) {
                        Integer aeState = result.get(CaptureResult.CONTROL_AE_STATE);
                        Integer awbState = result.get(CaptureResult.CONTROL_AWB_STATE);
                        if (aeState == null || awbState == null) {
                            break;
                        }

                        readyToCapture = readyToCapture &&
                                aeState == CaptureResult.CONTROL_AE_STATE_CONVERGED &&
                                awbState == CaptureResult.CONTROL_AWB_STATE_CONVERGED;
                    }

                    // If we haven't finished the pre-capture sequence but have hit our maximum
                    // wait timeout, too bad! Begin capture anyway.
//                    if (!readyToCapture && hitTimeoutLocked()) {
//                        Log.w(TAG, "Timed out waiting for pre-capture sequence to complete.");
//                        readyToCapture = true;
//                    }

                    if (readyToCapture) {
                        // After this, the camera will go back to the normal state of preview.
                        mState = STATE_PREVIEW;
                    }
                }
            }
        }

        @Override
        public void onCaptureProgressed(@NonNull CameraCaptureSession session,
                                        @NonNull CaptureRequest request,
                                        @NonNull CaptureResult partialResult) {
            process(partialResult);
        }

        @Override
        public void onCaptureCompleted(@NonNull CameraCaptureSession session,
                                       @NonNull CaptureRequest request,
                                       @NonNull TotalCaptureResult result) {
            process(result);
            Integer nowAfState = result.get(CaptureResult.CONTROL_AF_STATE);
            if (nowAfState == null) {
                return;
            }
            if (nowAfState.intValue() == afState) {
                return;
            }
            afState = nowAfState.intValue();
            mBackgroundHandler.post(new Runnable() {
                @Override
                public void run() {
                    judgeFocus();
                }
            });
        }

    };

    /**
     * Check if we are using a device that only supports the LEGACY hardware level.
     * <p/>
     *
     * @return true if this is a legacy device.
     */
    private boolean isLegacyLocked() {
        return mCharacteristics.get(CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL) ==
                CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL_LEGACY;
    }

    public void startRecordingVideo() {
        if (null == mCameraDevice || !mTextureView.isAvailable() || null == mPreviewSize) {
            return;
        }
        try {
            closePreviewSession();
            setUpMediaRecorder();
            SurfaceTexture texture = mTextureView.getSurfaceTexture();
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
            mCameraDevice.createCaptureSession(surfaces, cameraSessionStateCallback, mBackgroundHandler);
        } catch (CameraAccessException | IOException e) {
            e.printStackTrace();
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
    public void stopBackgroundThread() {
        mBackgroundThread.quitSafely();
        try {
            mBackgroundThread.join();
            mBackgroundThread = null;
            mBackgroundHandler = null;
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public Size getPreviewSize() {
        return mPreviewSize;
    }

    private boolean hasPermissionsGranted(String[] permissions) {
        for (String permission : permissions) {
            if (ActivityCompat.checkSelfPermission(activity, permission)
                    != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    private void setUpMediaRecorder() throws IOException {
        if (null == activity) {
            return;
        }
        mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mMediaRecorder.setVideoSource(MediaRecorder.VideoSource.SURFACE);
        mMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
        if (mNextVideoAbsolutePath == null || mNextVideoAbsolutePath.isEmpty()) {
            mNextVideoAbsolutePath = getVideoFilePath(activity);
        }
        mMediaRecorder.setOutputFile(mNextVideoAbsolutePath);
        mMediaRecorder.setVideoEncodingBitRate(10000000);
        mMediaRecorder.setVideoFrameRate(30);
        mMediaRecorder.setVideoSize(mVideoSize.getWidth(), mVideoSize.getHeight());
        mMediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264);
        mMediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
        int rotation = activity.getWindowManager().getDefaultDisplay().getRotation();
        switch (mSensorOrientation) {
            case Camera2.SENSOR_ORIENTATION_DEFAULT_DEGREES:
                mMediaRecorder.setOrientationHint(Camera2.DEFAULT_ORIENTATIONS.get(rotation));
                break;
            case Camera2.SENSOR_ORIENTATION_INVERSE_DEGREES:
                mMediaRecorder.setOrientationHint(Camera2.INVERSE_ORIENTATIONS.get(rotation));
                break;
        }
        mMediaRecorder.prepare();
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
                    mTextureView.setCameraSettings(mCharacteristics, mPreviewSession, mPreviewBuilder, mCaptureCallback);
                    setFaceDetect(mPreviewBuilder, mFaceDetectMode);
                    updatePreview();
                    if (mIsRecordingVideo) {
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

    private Runnable stopRecodingRunnable = new Runnable() {
        @Override
        public void run() {
            // / Stop recording
            mMediaRecorder.stop();
            mMediaRecorder.reset();
        }
    };

    private long focusSleepTime = 800;

    private void judgeFocus() {
        switch (afState) {
            case CameraMetadata.CONTROL_AF_STATE_ACTIVE_SCAN:
            case CameraMetadata.CONTROL_AF_STATE_PASSIVE_SCAN:
                mBackgroundHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        focusFocusing();
                        mPreviewBuilder.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_OFF);
                        try {
                            mPreviewSession.setRepeatingRequest(mPreviewBuilder.build(), null, null);
                        } catch (CameraAccessException e) {
                            e.printStackTrace();
                        }
                    }
                }, focusSleepTime);
                break;
            case CameraMetadata.CONTROL_AF_STATE_FOCUSED_LOCKED:
            case CameraMetadata.CONTROL_AF_STATE_PASSIVE_FOCUSED:
                mBackgroundHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        focusSucceed();
                    }
                }, focusSleepTime);
                break;
            case CameraMetadata.CONTROL_AF_STATE_INACTIVE:
                mBackgroundHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        focusInactive();
                    }
                }, focusSleepTime);
                break;
            case CameraMetadata.CONTROL_AF_STATE_NOT_FOCUSED_LOCKED:
            case CameraMetadata.CONTROL_AF_STATE_PASSIVE_UNFOCUSED:
                mBackgroundHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        focusFailed();
                    }
                }, focusSleepTime);
                break;
        }
    }

    private int mRawX, mRawY;

    private void focusFocusing() {
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                int width = focusImage.getWidth();
                int height = focusImage.getHeight();
                if (mTextureView.getPosition() != null &&
                        mRawX != mTextureView.getPosition().getX() &&
                        mRawY != mTextureView.getPosition().getY()) {
                    mRawX = (int) mTextureView.getPosition().getX();
                    mRawY = (int) mTextureView.getPosition().getY();
                    ViewGroup.MarginLayoutParams margin = new ViewGroup.MarginLayoutParams(focusImage.getLayoutParams());
                    margin.setMargins(mRawX - width / 2, mRawY - height / 2, margin.rightMargin, margin.bottomMargin);
                    FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(margin);
                    focusImage.setLayoutParams(layoutParams);
                    focusImage.startFocusing();
                    Log.d(TAG, "focusFocusing");
                }
            }
        });
    }

    private void focusSucceed() {
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                focusImage.focusSuccess();
                Log.d(TAG, "focusSucceed");
            }
        });
    }

    private void focusInactive() {
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                focusImage.stopFocus();
                Log.d(TAG, "focusInactive");
            }
        });
    }

    private void focusFailed() {
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                focusImage.focusFailed();
                Log.d(TAG, "focusFailed");
            }
        });
    }

    public void setFocusImage(AnimationImageView focusImage) {
        this.focusImage = focusImage;
    }
}
