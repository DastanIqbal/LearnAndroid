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

package com.dastanapps.camera.view;

import android.content.Context;
import android.graphics.Rect;
import android.hardware.Camera;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.TextureView;
import android.view.View;

import com.dastanapps.camera.Camera1;
import com.dastanapps.view.AutoFitTextureView;

import java.util.ArrayList;
import java.util.List;

/**
 * A {@link TextureView} that can be adjusted to a specified aspect ratio.
 */
public class Cam1AutoFitTextureView extends AutoFitTextureView {
    private Camera mCamera;
    float mDist = 0;
    private Handler mainHandler;

    public Cam1AutoFitTextureView(Context context) {
        super(context);
    }

    public Cam1AutoFitTextureView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public Cam1AutoFitTextureView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public void setCameraSettings(Camera camera) {
        this.mCamera = camera;
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        // Get the pointer ID
        Camera.Parameters params = mCamera.getParameters();
        int action = event.getAction();

        if (event.getPointerCount() > 1) {
            // handle multi-touch events
            if (action == MotionEvent.ACTION_POINTER_DOWN) {
                mDist = getFingerSpacing(event);
            } else if (action == MotionEvent.ACTION_MOVE
                    && params.isZoomSupported()) {
                mCamera.cancelAutoFocus();
                pinchToZoom(event);
            }
        } else {
            // handle single touch events
            if (action == MotionEvent.ACTION_DOWN) {
                touchToFocus(event);
            }
        }
        return true;
    }

    protected void pinchToZoom(MotionEvent event) {
        Camera.Parameters params = mCamera.getParameters();
        int maxZoom = params.getMaxZoom();
        int zoom = params.getZoom();
        float newDist = getFingerSpacing(event);
        if (newDist > mDist) {
            // zoom in
            if (zoom < maxZoom)
                zoom++;
        } else if (newDist < mDist) {
            // zoom out
            if (zoom > 0)
                zoom--;
        }
        mDist = newDist;
        params.setZoom(zoom);
        mCamera.setParameters(params);
    }

    protected void touchToFocus(MotionEvent event) {
        Camera.Parameters params = mCamera.getParameters();
        List<String> supportedFocusModes = params.getSupportedFocusModes();
        if (supportedFocusModes != null && supportedFocusModes.contains(Camera.Parameters.FOCUS_MODE_AUTO)) {
            float x = event.getX();
            float y = event.getY();

            final Rect touchRect = new Rect((int) (x - 100), (int) (y - 100), (int) (x + 100), (int) (y + 100));
            final Rect targetFocusRect = new Rect(
                    touchRect.left * 2000 / this.getWidth() - 1000,
                    touchRect.top * 2000 / this.getHeight() - 1000,
                    touchRect.right * 2000 / this.getWidth() - 1000,
                    touchRect.bottom * 2000 / this.getHeight() - 1000);

            if (params.getMaxNumFocusAreas() > 0) {
                List<Camera.Area> focusList = new ArrayList<>();
                Camera.Area focusArea = new Camera.Area(targetFocusRect, 1000);
                focusList.add(focusArea);

                params.setFocusAreas(focusList);
                params.setMeteringAreas(focusList);
                mCamera.setParameters(params);
            }

            final MotionEvent touchEvent = event;

            //Focusing
            Message message = mainHandler.obtainMessage();
            message.what = Camera1.FOCUSING_FOCUS_VIEW;
            message.obj = touchEvent;
            mainHandler.sendMessage(message);
            mCamera.cancelAutoFocus();

            mCamera.autoFocus(new Camera.AutoFocusCallback() {
                @Override
                public void onAutoFocus(boolean success, Camera camera) {
                    if (success) {
                        Message message = mainHandler.obtainMessage();
                        message.what = Camera1.SUCCESS_FOCUS_VIEW;
                        message.obj = touchEvent;
                        mainHandler.sendMessage(message);
                        mCamera.cancelAutoFocus();
                    } else {
                        Message message = mainHandler.obtainMessage();
                        message.what = Camera1.FAILED_FOCUS_VIEW;
                        message.obj = touchEvent;
                        mainHandler.sendMessage(message);
                    }
                }
            });
        }
    }

    public void setMainHandler(@NonNull Handler mainHandler) {
        this.mainHandler = mainHandler;
    }
}
