package com.dastanapps.camera2.view;

import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.util.Log;

import com.dastanapps.encoder.MediaVideoEncoder;
import com.dastanapps.gles.GLDrawer2D;
import com.dastanapps.view.AutoFitTextureView;
import com.dastanapps.view.GLTextureView;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public final class CameraSurfaceRenderer implements GLTextureView.Renderer, SurfaceTexture.OnFrameAvailableListener {
    private static String TAG = CameraSurfaceRenderer.class.getSimpleName();
    private volatile boolean requesrUpdateTex = false;
    private EGLSurfaceTextureListener listener;
    private int mCameraSurfaceGlTexture;
    private SurfaceTexture mCameraSurfaceTexture;
    private int surfaceWidth;
    private int surfaceHeight;
    private MediaVideoEncoder mVideoEncoder;
    private GLDrawer2D mDrawer;
    private Camera mCamera;

    public CameraSurfaceRenderer() {
    }

    public void setListener(EGLSurfaceTextureListener listener) {
        this.listener = listener;
    }

    @Override
    public void onFrameAvailable(SurfaceTexture surfaceTexture) {
        requesrUpdateTex = true;
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        mCameraSurfaceGlTexture = GLDrawer2D.initTex();
        mCameraSurfaceTexture = new SurfaceTexture(mCameraSurfaceGlTexture);
        mCameraSurfaceTexture.setOnFrameAvailableListener(this);
        if (listener != null)
            listener.onSurfaceTextureReady(mCameraSurfaceTexture);
        mDrawer = new GLDrawer2D();
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        Log.v(TAG, String.format("onSurfaceChanged:(%d,%d)", width, height));
        this.surfaceWidth = width;
        this.surfaceHeight = height;
        updateViewport(width, height);
    }


    public void updateViewport(int width, int height) {
        this.surfaceWidth = width;
        this.surfaceHeight = height;
        mDrawer.surfaceCreated(width, height);
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        if (requesrUpdateTex) {
            requesrUpdateTex = false;
            // update texture(came from camera)
            mCameraSurfaceTexture.updateTexImage();
            // get texture matrix
            mCameraSurfaceTexture.getTransformMatrix(mDrawer.mStMatrix);
        }
        mDrawer.draw(mCameraSurfaceGlTexture,mDrawer.mStMatrix);
        synchronized (this) {
            if (mVideoEncoder != null) {
                mVideoEncoder.frameAvailableSoon();
            }
        }
    }

    /**
     * when GLSurface context is soon destroyed
     */
    public void onSurfaceDestroyed() {
        if (mDrawer != null) {
            mDrawer.release();
            mDrawer = null;
        }
        mCameraSurfaceTexture.release();
        GLDrawer2D.deleteTex(mCameraSurfaceGlTexture);
    }

    public void setVideoEnocder(AutoFitTextureView mTextureView, final MediaVideoEncoder videoEnocder) {
        mTextureView.queueEvent(new Runnable() {
            @Override
            public void run() {
                if (videoEnocder != null) {
                    videoEnocder.setEglContext(mCameraSurfaceGlTexture);
                    mVideoEncoder = videoEnocder;
                }
            }
        });
    }

    public interface EGLSurfaceTextureListener {
        /**
         * Underlying EGL Context is ready.
         */
        void onSurfaceTextureReady(SurfaceTexture surfaceTexture);
    }

//    public void changeFilter(final CameraFilter filter) {
//        cameraFilter = filter;
//    }
}