package com.dastanapps.camera;

import android.app.Activity;
import android.content.Context;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v4.util.Pair;
import android.view.MotionEvent;
import android.view.OrientationEventListener;

import com.dastanapps.camera.listeners.Cam1OrientationEventListener;
import com.dastanapps.camera.listeners.Cam1SurfaceTextureListener;
import com.dastanapps.camera.view.Cam1AutoFitTextureView;
import com.dastanapps.camera.view.FocusImageView;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

/**
 * Created by dastaniqbal on 18/01/2018.
 * dastanIqbal@marvelmedia.com
 * 18/01/2018 6:32
 */

public class Camera1 {
    public final static int FOCUSING_FOCUS_VIEW = 0;
    public final static int SUCCESS_FOCUS_VIEW = 1;
    public final static int FAILED_FOCUS_VIEW = 2;
    private Cam1SurfaceTextureListener mCameraSurfaceTextureListener;
    private OrientationEventListener mOrientationEventListener;
    private Cam1AutoFitTextureView mTextureView;
    private Context mContext;
    private Activity mActivity;

    private Camera mCamera;
    private int mDisplayOrientation;
    private int cameraId;
    private Camera.Parameters mCharacteristics;
    private Camera.Size mVideoSize;
    private Camera.Size mPreviewSize;
    /**
     * A {@link Semaphore} to prevent the app from exiting before closing the camera.
     */
    private Semaphore mCameraOpenCloseLock = new Semaphore(1);
    private FocusImageView focusImage;
    private Handler mainHandler = new Handler(Looper.getMainLooper()) {

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case FOCUSING_FOCUS_VIEW:
                    MotionEvent touchEvent = (MotionEvent) msg.obj;
                    if (touchEvent != null && focusImage != null) {
                        focusImage.startFocusing(touchEvent);
                    }
                    break;
                case SUCCESS_FOCUS_VIEW:
                    if (focusImage != null) {
                        focusImage.focusSuccess();
                    }
                    break;
                case FAILED_FOCUS_VIEW:
                    if (focusImage != null) {
                        focusImage.focusFailed();
                    }
                    break;
            }
        }
    };


    public Camera1(Context context, Cam1AutoFitTextureView mTextureView) {
        this.mContext = context;
        this.mActivity = (Activity) context;
        this.mTextureView = mTextureView;
        mCameraSurfaceTextureListener = new Cam1SurfaceTextureListener(this, mTextureView, mActivity);
        this.mTextureView.setSurfaceTextureListener(mCameraSurfaceTextureListener);
        this.mTextureView.setMainHandler(mainHandler);

        mOrientationEventListener = new Cam1OrientationEventListener(mContext);
        setupManager();
    }

    private void setupManager() {
        Pair<Camera.CameraInfo, Integer> backCamera = Camera1Helper.getCameraInfo(Camera.CameraInfo.CAMERA_FACING_BACK);
        cameraId = backCamera.second;
        mDisplayOrientation = backCamera.first.orientation;
    }

    protected void onResume() {
        if (mOrientationEventListener.canDetectOrientation()) {
            mOrientationEventListener.enable();
        }
    }

    protected void onPause() {
        closeCamera();
        mOrientationEventListener.disable();
    }

    public void openCamera() {
        closeCamera();

        try {
            if (!mCameraOpenCloseLock.tryAcquire(2500, TimeUnit.MILLISECONDS)) {
                throw new RuntimeException("Time out waiting to lock camera opening.");
            }
            mCamera = Camera.open(cameraId);
            mTextureView.setCameraSettings(mCamera);
            mCharacteristics = mCamera.getParameters();
            mVideoSize = Camera1Helper.chooseVideoSize(mCharacteristics.getSupportedVideoSizes());
            mPreviewSize = Camera1Helper.chooseOptimalSize(mCharacteristics.getSupportedPreviewSizes(),
                    mTextureView.getWidth(), mTextureView.getHeight(), mVideoSize);
            int orientation = mContext.getResources().getConfiguration().orientation;
//        if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
//            mTextureView.setAspectRatio(mPreviewSize.width, mPreviewSize.height);
//        } else {
//            mTextureView.setAspectRatio(mPreviewSize.height, mPreviewSize.width);
//        }
//            Camera1Helper.configureTransform(mActivity, mPreviewSize, mTextureView, mTextureView.getWidth(), mTextureView.getHeight());
//            mCharacteristics.setPreviewSize(mPreviewSize.width, mPreviewSize.height);
//            mCamera.setParameters(mCharacteristics);

            try {
                SurfaceTexture texture = mTextureView.getSurfaceTexture();
                if (texture != null) {
                    texture.setDefaultBufferSize(mPreviewSize.width, mPreviewSize.height);
                }
                Camera1Helper.setDisplayOrientation(mActivity, mDisplayOrientation, mCamera);
                mCamera.setPreviewTexture(mTextureView.getSurfaceTexture());
                mCamera.startPreview();
            } catch (IOException ioe) {
                // Something bad happened
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void setAutoExposure(int autoExposure) {
        mCharacteristics.setAutoExposureLock(false);
        int maxmax = mCharacteristics.getMaxExposureCompensation();
        int minmin = mCharacteristics.getMinExposureCompensation();
        int all = (-minmin) + maxmax;
        int time = 100 / all;
        int ae = ((autoExposure / time) - maxmax) > maxmax ? maxmax : ((autoExposure / time) - maxmax) < minmin ? minmin : ((autoExposure / time) - maxmax);
        mCharacteristics.setExposureCompensation(ae);
        mCamera.setParameters(mCharacteristics);
        mCharacteristics.setAutoExposureLock(true);
    }

    public void closeCamera() {
        try {
            mCameraOpenCloseLock.release();
            mCameraOpenCloseLock.acquire();
            if (mCamera != null) {
                mCamera.stopPreview();
                mCamera.release();
                mCamera = null;
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            mCameraOpenCloseLock.release();
        }
    }

    public boolean isCameraOpen() {
        return mCamera != null;
    }

    public void switchFaces() {
        if (cameraId == Camera.CameraInfo.CAMERA_FACING_BACK)
            cameraId = Camera.CameraInfo.CAMERA_FACING_FRONT; //Front
        else cameraId = Camera.CameraInfo.CAMERA_FACING_BACK; //Back
        openCamera();
    }

    public DialogHelper.FeatureListDialog toggleFlash() {
        final List<String> flashes = mCharacteristics.getSupportedFlashModes();
        return DialogHelper.FeatureListDialog.newInstance().setFeatures(flashes, new DialogHelper.FeatureListDialog.ISelectItem() {
            @Override
            public void onSelectItem(int which) {
                mCharacteristics.setFlashMode(flashes.get(which));
                mCamera.setParameters(mCharacteristics);
            }
        });
    }

    public DialogHelper.FeatureListDialog showScenesDialog() {
        final List<String> sceneModes = mCharacteristics.getSupportedSceneModes();
        return DialogHelper.FeatureListDialog.newInstance().setFeatures(sceneModes, new DialogHelper.FeatureListDialog.ISelectItem() {
            @Override
            public void onSelectItem(int which) {
                mCharacteristics.setSceneMode(sceneModes.get(which));
                mCamera.setParameters(mCharacteristics);
            }
        });
    }

    public DialogHelper.FeatureListDialog showEffectsDialog() {
        final List<String> colorEffects = mCharacteristics.getSupportedColorEffects();
        return DialogHelper.FeatureListDialog.newInstance().setFeatures(colorEffects, new DialogHelper.FeatureListDialog.ISelectItem() {
            @Override
            public void onSelectItem(int which) {
                mCharacteristics.setColorEffect(colorEffects.get(which));
                mCamera.setParameters(mCharacteristics);
            }
        });
    }

    public DialogHelper.FeatureListDialog showWhiteBalance() {
        final List<String> colorEffects = mCharacteristics.getSupportedWhiteBalance();
        return DialogHelper.FeatureListDialog.newInstance().setFeatures(colorEffects, new DialogHelper.FeatureListDialog.ISelectItem() {
            @Override
            public void onSelectItem(int which) {
                mCharacteristics.setWhiteBalance(colorEffects.get(which));
                mCamera.setParameters(mCharacteristics);
            }
        });
    }

    public void setFocusImage(@NonNull FocusImageView focusImage) {
        this.focusImage = focusImage;
    }
}
