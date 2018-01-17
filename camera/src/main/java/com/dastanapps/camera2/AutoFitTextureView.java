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

package com.dastanapps.camera2;

import android.content.Context;
import android.graphics.Rect;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CaptureRequest;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.TextureView;
import android.view.View;

/**
 * A {@link TextureView} that can be adjusted to a specified aspect ratio.
 */
@RequiresApi(Build.VERSION_CODES.LOLLIPOP)
public class AutoFitTextureView extends TextureView implements View.OnTouchListener {

    private int mRatioWidth = 0;
    private int mRatioHeight = 0;

    private CameraCharacteristics mCharacteristics;
    private CaptureRequest.Builder mPreviewBuilder;
    private CameraCaptureSession mPreviewSession;
    private CameraCaptureSession.CaptureCallback mCaptureCallback;

    public AutoFitTextureView(Context context) {
        this(context, null);
    }

    public AutoFitTextureView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public AutoFitTextureView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    private void init() {
        setOnTouchListener(this);
    }

    /**
     * Sets the aspect ratio for this view. The size of the view will be measured based on the ratio
     * calculated from the parameters. Note that the actual sizes of parameters don't matter, that
     * is, calling setAspectRatio(2, 3) and setAspectRatio(4, 6) make the same result.
     *
     * @param width  Relative horizontal size
     * @param height Relative vertical size
     */
    public void setAspectRatio(int width, int height) {
        if (width < 0 || height < 0) {
            throw new IllegalArgumentException("Size cannot be negative.");
        }
        mRatioWidth = width;
        mRatioHeight = height;
        requestLayout();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int height = MeasureSpec.getSize(heightMeasureSpec);
        if (0 == mRatioWidth || 0 == mRatioHeight) {
            setMeasuredDimension(width, height);
        } else {
            if (width < height * mRatioWidth / mRatioHeight) {
                setMeasuredDimension(width, width * mRatioHeight / mRatioWidth);
            } else {
                setMeasuredDimension(height * mRatioWidth / mRatioHeight, height);
            }
        }
    }

    /**
     * Pinch to Zoom
     */

    public float fingerSpacing = 0;
    public double zoomLevel = 1;
    protected float maximumZoomLevel;
    protected Rect zoom;

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if (mCharacteristics != null && mPreviewBuilder != null && mPreviewSession != null) {
            maximumZoomLevel = mCharacteristics.get(CameraCharacteristics.SCALER_AVAILABLE_MAX_DIGITAL_ZOOM) * 10;
            Rect rect = mCharacteristics.get(CameraCharacteristics.SENSOR_INFO_ACTIVE_ARRAY_SIZE);
            if (rect == null) return false;
            int action = event.getAction();
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
            } else {
                if (action == MotionEvent.ACTION_UP) {
                    //single touch logic
                }
            }

            try {
                mPreviewSession
                        .setRepeatingRequest(mPreviewBuilder.build(), mCaptureCallback, null);
            } catch (CameraAccessException e) {
                e.printStackTrace();
            } catch (NullPointerException ex) {
                ex.printStackTrace();
            }
            return true;
        }
        return false;
    }


    private float getFingerSpacing(MotionEvent event) {
        float x = event.getX(0) - event.getX(1);
        float y = event.getY(0) - event.getY(1);
        return (float) Math.sqrt(x * x + y * y);
    }

    public void setCameraSettings(CameraCharacteristics mCharacteristics,
                                  CameraCaptureSession mPreviewSession,
                                  CaptureRequest.Builder mPreviewBuilder,
                                  CameraCaptureSession.CaptureCallback mCaptureCallback) {
        this.mCharacteristics = mCharacteristics;
        this.mPreviewSession = mPreviewSession;
        this.mPreviewBuilder = mPreviewBuilder;
        this.mCaptureCallback = mCaptureCallback;
    }
}
