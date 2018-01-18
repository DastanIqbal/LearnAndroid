package com.dastanapps.camera2.callback;

import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.CaptureResult;
import android.hardware.camera2.TotalCaptureResult;
import android.os.Build;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.dastanapps.camera2.view.AnimationImageView;
import com.dastanapps.camera2.view.AutoFitTextureView;

/**
 * Created by yuyidong on 14-12-19.
 */
@RequiresApi(Build.VERSION_CODES.LOLLIPOP)
public class PreviewSessionCallback extends CameraCaptureSession.CaptureCallback {
    private final AutoFitTextureView mTextureView;
    private Integer mAfState = CameraMetadata.CONTROL_AF_STATE_INACTIVE;
    private AnimationImageView mFocusImage;
    private Handler mBackgroundHandler;
    private int mRawX;
    private int mRawY;
    private Handler mHandler;

    private long focusSleepTime = 800;
    private static String TAG = PreviewSessionCallback.class.getSimpleName();

    /**
     * The current state of camera state for taking pictures.
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

    private CameraCharacteristics mCharacteristics;
    private CaptureRequest.Builder mPreviewBuilder;
    private CameraCaptureSession mPreviewSession;

    public PreviewSessionCallback(AnimationImageView mFocusImage, Handler mMainHandler, AutoFitTextureView mTextureView) {
        this.mFocusImage = mFocusImage;
        this.mBackgroundHandler = mMainHandler;
        this.mTextureView = mTextureView;
        mHandler = new Handler(mFocusImage.getContext().getMainLooper());
    }

    public void setCameraSettings(CameraCharacteristics mCharacteristics, CameraCaptureSession mPreviewSession, CaptureRequest.Builder mPreviewBuilder) {
        this.mCharacteristics = mCharacteristics;
        this.mPreviewSession = mPreviewSession;
        this.mPreviewBuilder = mPreviewBuilder;
    }

    private void process(CaptureResult result) {
        switch (mState) {
            case STATE_PREVIEW: {
                Log.d(TAG, "STATE_PREVIEW");
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

//            case STATE_WAITING_LOCK: {
//                Log.d(TAG, "STATE_WAITING_LOCK");
//                mAfState = result.get(CaptureResult.CONTROL_AF_STATE);
//                if (mAfState == null) {
//                } else if (CaptureResult.CONTROL_AF_STATE_FOCUSED_LOCKED == mAfState ||
//                        CaptureResult.CONTROL_AF_STATE_NOT_FOCUSED_LOCKED == mAfState) {
//                    // CONTROL_AE_STATE can be null on some devices
//                    Integer aeState = result.get(CaptureResult.CONTROL_AE_STATE);
//                    if (aeState == null ||
//                            aeState == CaptureResult.CONTROL_AE_STATE_CONVERGED) {
//                    } else {
//                    }
//                }
//                break;
//            }
//            case STATE_WAITING_PRECAPTURE: {
//                Log.d(TAG, "STATE_WAITING_PRECAPTURE");
//                // CONTROL_AE_STATE can be null on some devices
//                Integer aeState = result.get(CaptureResult.CONTROL_AE_STATE);
//                if (aeState == null ||
//                        aeState == CaptureResult.CONTROL_AE_STATE_PRECAPTURE ||
//                        aeState == CaptureRequest.CONTROL_AE_STATE_FLASH_REQUIRED) {
//                    mState = STATE_WAITING_NON_PRECAPTURE;
//                }
//                break;
//            }
//            case STATE_WAITING_NON_PRECAPTURE: {
//                Log.d(TAG, "STATE_WAITING_NON_PRECAPTURE");
//                boolean readyToCapture = true;
//                if (!mNoAFRun) {
//                    mAfState = result.get(CaptureResult.CONTROL_AF_STATE);
//                    if (mAfState == null) {
//                        break;
//                    }
//
//                    // If auto-focus has reached locked state, we are ready to capture
//                    readyToCapture =
//                            (mAfState == CaptureResult.CONTROL_AF_STATE_FOCUSED_LOCKED ||
//                                    mAfState == CaptureResult.CONTROL_AF_STATE_NOT_FOCUSED_LOCKED);
//                }
//
//                // If we are running on an non-legacy device, we should also wait until
//                // auto-exposure and auto-white-balance have converged as well before
//                // taking a picture.
//                if (!isLegacyLocked()) {
//                    Integer aeState = result.get(CaptureResult.CONTROL_AE_STATE);
//                    Integer awbState = result.get(CaptureResult.CONTROL_AWB_STATE);
//                    if (aeState == null || awbState == null) {
//                        break;
//                    }
//
//                    readyToCapture = readyToCapture &&
//                            aeState == CaptureResult.CONTROL_AE_STATE_CONVERGED &&
//                            awbState == CaptureResult.CONTROL_AWB_STATE_CONVERGED;
//                }
//
//                // If we haven't finished the pre-capture sequence but have hit our maximum
//                // wait timeout, too bad! Begin capture anyway.
////                    if (!readyToCapture && hitTimeoutLocked()) {
////                        Log.w(TAG, "Timed out waiting for pre-capture sequence to complete.");
////                        readyToCapture = true;
////                    }
//
//                if (readyToCapture) {
//                    // After this, the camera will go back to the normal state of preview.
//                    mState = STATE_PREVIEW;
//                }
//            }
        }
    }

    @Override
    public void onCaptureProgressed(@NonNull CameraCaptureSession session,
                                    @NonNull CaptureRequest request,
                                    @NonNull CaptureResult partialResult) {
        process(partialResult);
    }

    @Override
    public void onCaptureCompleted(@NonNull CameraCaptureSession session, @NonNull CaptureRequest request, @NonNull TotalCaptureResult result) {
        super.onCaptureCompleted(session, request, result);
        process(result);
        Integer nowAfState = result.get(CaptureResult.CONTROL_AF_STATE);
        if (nowAfState == null) {
            return;
        }
        if (nowAfState.intValue() == mAfState) {
            return;
        }
        mAfState = nowAfState.intValue();
        judgeFocus();
    }

    private void judgeFocus() {
        switch (mAfState) {
            case CameraMetadata.CONTROL_AF_STATE_ACTIVE_SCAN:
            case CameraMetadata.CONTROL_AF_STATE_PASSIVE_SCAN:
                mBackgroundHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        focusFocusing();
                    }
                }, focusSleepTime);
                break;
            case CameraMetadata.CONTROL_AF_STATE_FOCUSED_LOCKED:
            case CameraMetadata.CONTROL_AF_STATE_PASSIVE_FOCUSED:
                mBackgroundHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        focusSucceed();
                        mState = STATE_PREVIEW;
                        mPreviewBuilder.set(CaptureRequest.CONTROL_AF_TRIGGER, CameraMetadata.CONTROL_AF_TRIGGER_IDLE);
                        try {
                            mPreviewSession.setRepeatingRequest(mPreviewBuilder.build(), PreviewSessionCallback.this, mBackgroundHandler);
                        } catch (CameraAccessException e) {
                            e.printStackTrace();
                        }
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

    private void focusFocusing() {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                int width = mFocusImage.getWidth();
                int height = mFocusImage.getHeight();
                if (mTextureView.getPosition() != null &&
                        mRawX != mTextureView.getPosition().getX() &&
                        mRawY != mTextureView.getPosition().getY()) {
                    mRawX = (int) mTextureView.getPosition().getX();
                    mRawY = (int) mTextureView.getPosition().getY();
                    ViewGroup.MarginLayoutParams margin = new ViewGroup.MarginLayoutParams(mFocusImage.getLayoutParams());
                    margin.setMargins(mRawX - width / 2, mRawY - height / 2, margin.rightMargin, margin.bottomMargin);
                    FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(margin);
                    mFocusImage.setLayoutParams(layoutParams);
                    mFocusImage.startFocusing();
                    Log.d(TAG, "focusFocusing");
                }
            }
        });
    }

    private void focusSucceed() {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                mFocusImage.focusSuccess();
                Log.d(TAG, "focusSucceed");
            }
        });
    }

    private void focusInactive() {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                mFocusImage.stopFocus();
            }
        });
    }

    private void focusFailed() {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                mFocusImage.focusFailed();
            }
        });
    }

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

}
