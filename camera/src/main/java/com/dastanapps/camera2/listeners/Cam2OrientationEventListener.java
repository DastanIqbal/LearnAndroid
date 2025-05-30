package com.dastanapps.camera2.listeners;

import android.app.Activity;
import android.content.Context;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Handler;
import android.support.annotation.RequiresApi;
import android.util.Log;
import android.util.Size;

import com.dastanapps.CommonUtils;
import com.dastanapps.camera2.Camera2Helper;
import com.dastanapps.camera2.view.Cam2AutoFitTextureView;
import com.dastanapps.view.FaceOverlayView;
import com.dastanapps.view.Util;

/**
 * Created by dastaniqbal on 19/01/2018.

 * 19/01/2018 11:08
 */

@RequiresApi(Build.VERSION_CODES.LOLLIPOP)
public class Cam2OrientationEventListener extends android.view.OrientationEventListener {
    private int mOrientation;
    private int mOrientationCompensation;
    private FaceOverlayView mFaceView;
    private Cam2AutoFitTextureView mTextureView;
    private Activity mActivity;
    private Size mPreviewSize;
    private Handler mMainhandler;

    public Cam2OrientationEventListener(Context context, Cam2AutoFitTextureView textureView, Size previewSize, Handler handler) {
        super(context, SensorManager.SENSOR_DELAY_NORMAL);
        this.mTextureView = textureView;
        this.mActivity = (Activity) context;
        this.mPreviewSize = previewSize;
        this.mMainhandler = handler;
    }

    public void setFaceView(FaceOverlayView mFaceView) {
        this.mFaceView = mFaceView;
    }

    @Override
    public void onOrientationChanged(int orientation) {
        if (mTextureView != null && mTextureView.isAvailable()) {
            Camera2Helper.configureTransform(mActivity, mPreviewSize, mTextureView, mTextureView.getWidth(), mTextureView.getHeight());
            // We keep the last known orientation. So if the user first orient
            // the camera then point the camera to floor or sky, we still have
            // the correct orientation.
            if (orientation == ORIENTATION_UNKNOWN) return;
            mOrientation = Util.roundOrientation(orientation, mOrientation);
            // When the screen is unlocked, display rotation may change. Always
            // calculate the up-to-date orientationCompensation.
            int orientationCompensation = mOrientation
                    + Util.getDisplayRotation(mActivity);
            if (mOrientationCompensation != orientationCompensation) {
                mOrientationCompensation = orientationCompensation;
                if (mFaceView != null)
                    mFaceView.setOrientation(mOrientationCompensation);
            }

            Log.d("!!!!", "rotation!!! mOrientationCompensation:" + orientationCompensation);
            CommonUtils.INSTANCE.sendMessageToHandler(mMainhandler, 1, mOrientationCompensation);
        }
    }
}
