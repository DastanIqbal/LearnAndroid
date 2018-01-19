/*
 * Copyright 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.dastanapps.camera2.view;

import android.content.Context;
import android.graphics.Rect;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureFailure;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.TotalCaptureResult;
import android.hardware.camera2.params.MeteringRectangle;
import android.os.Build;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.util.AttributeSet;
import android.util.Log;
import android.util.Size;
import android.view.MotionEvent;
import android.view.TextureView;
import android.view.View;

import com.dastanapps.view.AutoFitTextureView;

import static android.support.v4.math.MathUtils.clamp;

/**
 * A {@link TextureView} that can be adjusted to a specified aspect ratio.
 */
@RequiresApi(Build.VERSION_CODES.LOLLIPOP)
public class Cam2AutoFitTextureView extends AutoFitTextureView {

    private static String TAG = Cam2AutoFitTextureView.class.getSimpleName();
    private int mRatioWidth = 0;
    private int mRatioHeight = 0;

    private CameraCharacteristics mCharacteristics;
    private CaptureRequest.Builder mPreviewBuilder;
    private CameraCaptureSession mPreviewSession;
    private CameraCaptureSession.CaptureCallback mCaptureCallback;

    boolean mManualFocusEngaged = false;

    public Cam2AutoFitTextureView(Context context) {
        super(context);
    }

    public Cam2AutoFitTextureView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public Cam2AutoFitTextureView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if (mCharacteristics != null && mPreviewBuilder != null && mPreviewSession != null) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    touchToFocus(event);
                    pinchToZoom(event);
                    setPosition(event);
                    break;
                case MotionEvent.ACTION_UP:
                    break;
            }

            try {
                mPreviewSession.setRepeatingRequest(mPreviewBuilder.build(), mCaptureCallback, null);
            } catch (CameraAccessException e) {
                e.printStackTrace();
            } catch (NullPointerException ex) {
                ex.printStackTrace();
            }
            return true;
        }
        return true;
    }

    @Nullable
    private Boolean touchTofocus2(MotionEvent event) {
        MotionEvent motionEvent = event;
        final int actionMasked = motionEvent.getActionMasked();
        if (actionMasked != MotionEvent.ACTION_DOWN) {
            return false;
        }
        if (mManualFocusEngaged) {
            Log.d(TAG, "Manual focus already engaged");
            return true;
        }

        final Rect sensorArraySize = mCharacteristics.get(CameraCharacteristics.SENSOR_INFO_ACTIVE_ARRAY_SIZE);

        //TODO: here I just flip x,y, but this needs to correspond with the sensor orientation (via SENSOR_ORIENTATION)
        final int y = (int) ((motionEvent.getX() / (float) getWidth()) * (float) sensorArraySize.height());
        final int x = (int) ((motionEvent.getY() / (float) getHeight()) * (float) sensorArraySize.width());
        final int halfTouchWidth = 150; //(int)motionEvent.getTouchMajor(); //TODO: this doesn't represent actual touch size in pixel. Values range in [3, 10]...
        final int halfTouchHeight = 150; //(int)motionEvent.getTouchMinor();
        MeteringRectangle focusAreaTouch = new MeteringRectangle(Math.max(x - halfTouchWidth, 0),
                Math.max(y - halfTouchHeight, 0),
                halfTouchWidth * 2,
                halfTouchHeight * 2,
                MeteringRectangle.METERING_WEIGHT_MAX - 1);

        CameraCaptureSession.CaptureCallback captureCallbackHandler = new CameraCaptureSession.CaptureCallback() {
            @Override
            public void onCaptureCompleted(CameraCaptureSession session, CaptureRequest request, TotalCaptureResult result) {
                super.onCaptureCompleted(session, request, result);
                mManualFocusEngaged = false;

                if (request.getTag() == "FOCUS_TAG") {
                    //the focus trigger is complete -
                    //resume repeating (preview surface will get frames), clear AF trigger
                    mPreviewBuilder.set(CaptureRequest.CONTROL_AF_TRIGGER, null);
                    try {
                        mPreviewSession.setRepeatingRequest(mPreviewBuilder.build(), null, null);
                    } catch (CameraAccessException e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onCaptureFailed(CameraCaptureSession session, CaptureRequest request, CaptureFailure failure) {
                super.onCaptureFailed(session, request, failure);
                Log.e(TAG, "Manual AF failure: " + failure);
                mManualFocusEngaged = false;
            }
        };

        //first stop the existing repeating request
        try {
            mPreviewSession.stopRepeating();
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }

        //cancel any existing AF trigger (repeated touches, etc.)
        mPreviewBuilder.set(CaptureRequest.CONTROL_AF_TRIGGER, CameraMetadata.CONTROL_AF_TRIGGER_CANCEL);
        mPreviewBuilder.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_OFF);
        try {
            mPreviewSession.capture(mPreviewBuilder.build(), captureCallbackHandler, null);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }

        //Now add a new AF trigger with focus region
        if (isMeteringAreaAFSupported()) {
            mPreviewBuilder.set(CaptureRequest.CONTROL_AF_REGIONS, new MeteringRectangle[]{focusAreaTouch});
        }
        mPreviewBuilder.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO);
        mPreviewBuilder.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_AUTO);
        mPreviewBuilder.set(CaptureRequest.CONTROL_AF_TRIGGER, CameraMetadata.CONTROL_AF_TRIGGER_START);
        mPreviewBuilder.setTag("FOCUS_TAG"); //we'll capture this later for resuming the preview

//            //then we ask for a single request (not repeating!)
//            mPreviewSession.capture(mPreviewBuilder.build(), captureCallbackHandler, mBackgroundHandler);
        return null;
    }

    private boolean isMeteringAreaAFSupported() {
        return mCharacteristics.get(CameraCharacteristics.CONTROL_MAX_REGIONS_AF) >= 1;
    }

    /**
     * Pinch to Zoom
     */

    public float fingerSpacing = 0;
    public double zoomLevel = 1;
    protected float maximumZoomLevel;

    protected void pinchToZoom(MotionEvent event) {
        maximumZoomLevel = mCharacteristics.get(CameraCharacteristics.SCALER_AVAILABLE_MAX_DIGITAL_ZOOM) * 10;
        Rect rect = mCharacteristics.get(CameraCharacteristics.SENSOR_INFO_ACTIVE_ARRAY_SIZE);
        float currentFingerSpacing;

        if (event.getPointerCount() > 1) {
            // Multi touch logic
            currentFingerSpacing = getFingerSpacing(event);
            if (fingerSpacing != 0) {
                if (currentFingerSpacing > fingerSpacing && maximumZoomLevel > zoomLevel) {
                    zoomLevel = zoomLevel + .4;
                } else if (currentFingerSpacing < fingerSpacing && zoomLevel > 1) {
                    zoomLevel = zoomLevel - .4;
                }
                int minW = (int) (rect.width() / maximumZoomLevel);
                int minH = (int) (rect.height() / maximumZoomLevel);
                int difW = rect.width() - minW;
                int difH = rect.height() - minH;
                int cropW = difW / 100 * (int) zoomLevel;
                int cropH = difH / 100 * (int) zoomLevel;
                cropW -= cropW & 3;
                cropH -= cropH & 3;
                Rect zoom = new Rect(cropW, cropH, rect.width() - cropW, rect.height() - cropH);
                mPreviewBuilder.set(CaptureRequest.SCALER_CROP_REGION, zoom);
            }
            fingerSpacing = currentFingerSpacing;
        }
    }

    protected void touchToFocus(MotionEvent event) {
        //first stop the existing repeating request
        try {
            mPreviewSession.stopRepeating();
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
        Rect rect = mCharacteristics.get(CameraCharacteristics.SENSOR_INFO_ACTIVE_ARRAY_SIZE);
        Log.i(TAG, "SENSOR_INFO_ACTIVE_ARRAY_SIZE,,,,,,,,rect.left--->" + rect.left + ",,,rect.top--->" + rect.top + ",,,,rect.right--->" + rect.right + ",,,,rect.bottom---->" + rect.bottom);
        Size size = mCharacteristics.get(CameraCharacteristics.SENSOR_INFO_PIXEL_ARRAY_SIZE);
        Log.i(TAG, "mCameraCharacteristics,,,,size.getWidth()--->" + size.getWidth() + ",,,size.getHeight()--->" + size.getHeight());
        int areaSize = 200;
        int right = rect.right;
        int bottom = rect.bottom;
        int viewWidth = getWidth();
        int viewHeight = getHeight();
        int ll, rr;
        Rect newRect;
        int centerX = (int) event.getX();
        int centerY = (int) event.getY();
        ll = ((centerX * right) - areaSize) / viewWidth;
        rr = ((centerY * bottom) - areaSize) / viewHeight;
        int focusLeft = clamp(ll, 0, right);
        int focusBottom = clamp(rr, 0, bottom);
        Log.i(TAG, "focusLeft--->" + focusLeft + ",,,focusTop--->" + focusBottom + ",,,focusRight--->" + (focusLeft + areaSize) + ",,,focusBottom--->" + (focusBottom + areaSize));
        newRect = new Rect(focusLeft, focusBottom, focusLeft + areaSize, focusBottom + areaSize);
        MeteringRectangle meteringRectangle = new MeteringRectangle(newRect, 500);
        MeteringRectangle[] meteringRectangleArr = {meteringRectangle};
        mPreviewBuilder.set(CaptureRequest.CONTROL_AF_TRIGGER, CameraMetadata.CONTROL_AF_TRIGGER_CANCEL);
        mPreviewBuilder.set(CaptureRequest.CONTROL_AF_REGIONS, meteringRectangleArr);
        mPreviewBuilder.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_AUTO);
        mPreviewBuilder.set(CaptureRequest.CONTROL_AF_TRIGGER, CameraMetadata.CONTROL_AF_TRIGGER_START);
    }

    public void setCameraSettings(CameraCharacteristics mCharacteristics, CameraCaptureSession mPreviewSession,
                                  CaptureRequest.Builder mPreviewBuilder, CameraCaptureSession.CaptureCallback mCaptureCallback) {
        this.mCharacteristics = mCharacteristics;
        this.mPreviewSession = mPreviewSession;
        this.mPreviewBuilder = mPreviewBuilder;
        this.mCaptureCallback = mCaptureCallback;
    }
}
