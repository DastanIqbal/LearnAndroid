// Copyright (c) Philipp Wagner. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.dastanapps.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.hardware.camera2.params.Face;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.util.Log;
import android.view.View;

/**
 * This class is a simple View to display the faces.
 */
@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public class FaceOverlayView extends View {

    private Paint mPaint;
    private Paint mTextPaint;
    private int mDisplayOrientation;
    private int mOrientation;
    private Face[] mFaces;
    private Rect cameraBounds;

    public FaceOverlayView(Context context) {
        super(context);
        initialize();
    }

    private void initialize() {
        // We want a green box around the face:
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setDither(true);
        mPaint.setColor(Color.GREEN);
        mPaint.setAlpha(128);
        mPaint.setStyle(Paint.Style.FILL_AND_STROKE);

        mTextPaint = new Paint();
        mTextPaint.setAntiAlias(true);
        mTextPaint.setDither(true);
        mTextPaint.setTextSize(20);
        mTextPaint.setColor(Color.GREEN);
        mTextPaint.setStyle(Paint.Style.FILL);
    }

    public void setFaces(Face[] faces) {
        mFaces = faces;
        invalidate();
    }

    public void setOrientation(int orientation) {
        mOrientation = orientation;
    }

    public void setDisplayOrientation(int displayOrientation) {
        mDisplayOrientation = displayOrientation;
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (mFaces != null && mFaces.length > 0) {
            int canvasWidth = getWidth();
            int canvasHeight = getHeight();
            int cameraWidth = cameraBounds.right;
            int cameraHeight = cameraBounds.bottom;
            Matrix matrix = new Matrix();
            Util.prepareMatrix(matrix, false, mDisplayOrientation, getWidth(), getHeight());
            canvas.save();
            matrix.postRotate(360 - mDisplayOrientation, getWidth() / 2, getHeight() / 2);
            canvas.rotate(360 - mDisplayOrientation, getWidth() / 2, getHeight() / 2);
         //   canvas.rotate(270, getWidth() / 2, getHeight() / 2);
            for (Face face : mFaces) {
                Rect rectangleFace = face.getBounds();
                int r = rectangleFace.right;
                int b = rectangleFace.bottom;
                int l = rectangleFace.left;
                int t = rectangleFace.top;
                int left = (canvasWidth - (canvasWidth * l) / cameraWidth);
                int top = (canvasHeight * t) / cameraHeight;
                int right = (canvasWidth - (canvasWidth * r) / cameraWidth);
                int bottom = (canvasHeight * b) / cameraHeight;

                RectF rectF = new RectF(left, top, right, bottom);
                matrix.mapRect(rectF);

                canvas.drawRect(rectF, mPaint);
                canvas.drawText("Score " + face.getScore(), rectF.right, rectF.top, mTextPaint);
                Log.e("DEBUG", "faces : " + face.toString());
            }
            canvas.restore();
        }
    }

    public void setCameraBounds(Rect cameraBounds) {
        this.cameraBounds = cameraBounds;
    }
}