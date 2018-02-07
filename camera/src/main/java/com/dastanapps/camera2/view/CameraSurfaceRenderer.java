package com.dastanapps.camera2.view;

import android.graphics.SurfaceTexture;
import android.opengl.GLES20;
import android.opengl.Matrix;
import android.util.Log;

import com.dastanapps.encoder.GLDrawer2D;
import com.dastanapps.encoder.MediaVideoEncoder;
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
    private final float[] mStMatrix = new float[16];
    private final float[] mMvpMatrix = new float[16];

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
        GLES20.glClearColor(1.0f, 1.0f, 0.0f, 1.0f);
        if (listener != null)
            listener.onSurfaceTextureReady(mCameraSurfaceTexture);
        mDrawer = new GLDrawer2D();
        mDrawer.setMatrix(mMvpMatrix, 0);
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        Log.v(TAG, String.format("onSurfaceChanged:(%d,%d)", width, height));
        this.surfaceWidth = width;
        this.surfaceHeight = height;
        updateViewport();
    }


    private final void updateViewport() {
        GLES20.glViewport(0, 0, surfaceWidth, surfaceHeight);
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
        Matrix.setIdentityM(mMvpMatrix, 0);
        if (mDrawer != null)
            mDrawer.setMatrix(mMvpMatrix, 0);
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
        if (requesrUpdateTex) {
            requesrUpdateTex = false;
            // update texture(came from camera)
            mCameraSurfaceTexture.updateTexImage();
            // get texture matrix
            mCameraSurfaceTexture.getTransformMatrix(mStMatrix);
        }
        mDrawer.draw(mCameraSurfaceGlTexture, mStMatrix);
        synchronized (this) {
            if (mVideoEncoder != null) {
                mVideoEncoder.frameAvailableSoon(mStMatrix, mMvpMatrix);
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

    public void setVideoEnocder(Cam2AutoFitTextureView mTextureView, final MediaVideoEncoder videoEnocder) {
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