package com.dastanapps.camera2.Preview.CameraSurface;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.media.MediaRecorder;
import android.opengl.GLSurfaceView;
import android.os.Handler;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import com.dastanapps.camera2.CameraController.CameraController;
import com.dastanapps.camera2.CameraController.CameraControllerException;
import com.dastanapps.camera2.MyDebug;
import com.dastanapps.camera2.Preview.Preview;
import com.dastanapps.mediasdk.opengles.CameraSurfaceRenderer;
import com.dastanapps.mediasdk.opengles.encoder.MediaVideoEncoder;

/**
 * Provides support for the surface used for the preview, using a SurfaceView.
 */
public class MySurfaceView extends GLSurfaceView implements CameraSurface {
    private static final String TAG = "MySurfaceView";

    private Preview preview;
    private int[] measure_spec = new int[2];
    private Handler handler = new Handler();
    private Runnable tick;
    private MyCameraSurfaceRenderer mRenderer;

    @SuppressWarnings("deprecation")
    public MySurfaceView(Context context, final Preview preview) {
        super(context);
        this.preview = preview;
        if (MyDebug.LOG) {
            Log.d(TAG, "new MySurfaceView");
        }

        // Install a SurfaceHolder.Callback so we get notified when the
        // underlying surface is created and destroyed.
        // getHolder().addCallback(preview);
        // deprecated setting, but required on Android versions prior to 3.0
        // getHolder().setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS); // deprecated

        tick = new Runnable() {
            public void run() {
                /*if( MyDebug.LOG )
                    Log.d(TAG, "invalidate()");*/
                preview.test_ticker_called = true;
                invalidate();
                handler.postDelayed(this, preview.getFrameRate());
            }
        };

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
            // camera_controller.setPreviewDisplay(this.getHolder());
            camera_controller.setPreviewTexture(camera_controller.getEGLSurfaceTexture());
        } catch (CameraControllerException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void setVideoRecorder(MediaRecorder video_recorder) {
        video_recorder.setPreviewDisplay(this.getHolder().getSurface());
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return preview.touchEvent(event);
    }

    @Override
    public void onDraw(Canvas canvas) {
        preview.draw(canvas);
    }

    @Override
    protected void onMeasure(int widthSpec, int heightSpec) {
        if (MyDebug.LOG)
            Log.d(TAG, "onMeasure: " + widthSpec + " x " + heightSpec);
        preview.getMeasureSpec(measure_spec, widthSpec, heightSpec);
        super.onMeasure(measure_spec[0], measure_spec[1]);
    }

    @Override
    public void setTransform(Matrix matrix) {
        if (MyDebug.LOG)
            Log.d(TAG, "setting transforms not supported for MySurfaceView");
        throw new RuntimeException();
    }

    @Override
    public void onPause() {
        if (MyDebug.LOG)
            Log.d(TAG, "onPause()");
        handler.removeCallbacks(tick);
    }

    @Override
    public void onResume() {
        if (MyDebug.LOG)
            Log.d(TAG, "onResume()");
        tick.run();
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
