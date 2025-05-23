package com.dastanapps.camera2.Preview.CameraSurface;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Matrix;
import android.media.MediaRecorder;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import com.dastanapps.camera2.CameraController.CameraController;
import com.dastanapps.camera2.CameraController.CameraControllerException;
import com.dastanapps.camera2.MyDebug;
import com.dastanapps.camera2.Preview.Preview;
import com.dastanapps.mediasdk.opengles.CameraSurfaceRenderer;
import com.dastanapps.mediasdk.opengles.GLTextureView;
import com.dastanapps.mediasdk.opengles.encoder.MediaVideoEncoder;


/**
 * Provides support for the surface used for the preview, using a TextureView.
 */
public class MyTextureView extends GLTextureView implements CameraSurface {
    private static final String TAG = "MyTextureView";

    private final Preview preview;
    private final int[] measure_spec = new int[2];
    private MyCameraSurfaceRenderer mRenderer;

    public MyTextureView(Context context, Preview preview) {
        super(context);
        this.preview = preview;
        if (MyDebug.LOG) {
            Log.d(TAG, "new MyTextureView");
        }

        // Install a TextureView.SurfaceTextureListener so we get notified when the
        // underlying surface is created and destroyed.
        //this.setSurfaceTextureListener(preview);
        mRenderer = new MyCameraSurfaceRenderer();
        setEGLContextClientVersion(2);    // GLES 2.0, API >= 8
        setRenderer(mRenderer);
    }

    @Override
    public View getView() {
        return this;
    }

    @Override
    public void setPreviewDisplay(CameraController camera_controller) {
        if (MyDebug.LOG)
            Log.d(TAG, "setPreviewDisplay");
        try {
            //camera_controller.setPreviewTexture(this.getSurfaceTexture());
            camera_controller.setPreviewTexture(camera_controller.getEGLSurfaceTexture());
        } catch (CameraControllerException e) {
            if (MyDebug.LOG)
                Log.e(TAG, "Failed to set preview display: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public void setVideoRecorder(MediaRecorder video_recorder) {
        // should be no need to do anything (see documentation for MediaRecorder.setPreviewDisplay())
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return preview.touchEvent(event);
    }

	/*@Override
    public void onDraw(Canvas canvas) {
		preview.draw(canvas);
	}*/

    @Override
    protected void onMeasure(int widthSpec, int heightSpec) {
        if (MyDebug.LOG)
            Log.d(TAG, "onMeasure: " + widthSpec + " x " + heightSpec);
        preview.getMeasureSpec(measure_spec, widthSpec, heightSpec);
        super.onMeasure(measure_spec[0], measure_spec[1]);
    }

    @Override
    public void setTransform(Matrix matrix) {
        super.setTransform(matrix);
    }

    @Override
    public void onPause() {
    }

    @Override
    public void onResume() {
    }

    @Override
    public void setEGLSurfaceTextureListener(CameraSurfaceRenderer.EGLSurfaceTextureListener eglSurfaceTextureListener) {
        mRenderer.setListener(eglSurfaceTextureListener);
    }

    @Override
    public void setVideoEncoder(MediaVideoEncoder encoder) {
        mRenderer.setVideoEnocder(this, encoder);
    }

    @Override
    public void changeFilter() {
        queueEvent(new Runnable() {
            @Override
            public void run() {
                mRenderer.changeFilter();
            }
        });
    }

    @Override
    public int currentFilter() {
        return mRenderer.getCurrentFilter();
    }
}
