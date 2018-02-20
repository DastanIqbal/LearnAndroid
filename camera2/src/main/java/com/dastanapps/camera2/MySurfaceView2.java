package com.dastanapps.camera2;

import android.content.Context;
import android.graphics.Matrix;
import android.media.MediaRecorder;
import android.opengl.GLSurfaceView;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import com.dastanapps.camera2.CameraController.CameraController;
import com.dastanapps.camera2.CameraController.CameraControllerException;
import com.dastanapps.camera2.Preview.CameraSurface.CameraSurface;
import com.dastanapps.camera2.opengles.CameraSurfaceRenderer;
import com.dastanapps.camera2.opengles.encoder.MediaVideoEncoder;

/**
 * Provides support for the surface used for the preview, using a SurfaceView.
 */
public class MySurfaceView2 extends GLSurfaceView implements CameraSurface {
    private static final String TAG = "MySurfaceView";

    private int[] measure_spec = new int[2];
    private Handler handler = new Handler();
    private Runnable tick;
    private CameraSurfaceRenderer mRenderer;


    public MySurfaceView2(final Context context) {
        this(context, null, 0);
    }

    public MySurfaceView2(final Context context, final AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MySurfaceView2(final Context context, final AttributeSet attrs, final int defStyle) {
        super(context, attrs);
        Log.v(TAG, "CameraGLView:");
        mRenderer = new CameraSurfaceRenderer();
        setEGLContextClientVersion(2);    // GLES 2.0, API >= 8
        setRenderer(mRenderer);
/*		// the frequency of refreshing of camera preview is at most 15 fps
        // and RENDERMODE_WHEN_DIRTY is better to reduce power consumption
		setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY); */
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
       // mRenderer.setVideoEnocder(this, encoder);
    }

    @Override
    public void changeFilter() {

    }
}
