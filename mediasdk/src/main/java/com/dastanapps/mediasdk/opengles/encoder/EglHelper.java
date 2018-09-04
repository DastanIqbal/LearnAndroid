package com.dastanapps.mediasdk.opengles.encoder;

import android.graphics.SurfaceTexture;
import android.opengl.GLES20;

import javax.microedition.khronos.egl.EGL10;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.egl.EGLContext;
import javax.microedition.khronos.egl.EGLDisplay;
import javax.microedition.khronos.egl.EGLSurface;


class EglHelper {
    private static final int EGL_OPENGL_ES2_BIT = 4;
    private EGLContext eglContext;
    private EGLDisplay eglDisplay;
    private EGLSurface eglSurface;
    private SurfaceTexture eglSurfaceTexture;
    private int[] eglTextures = new int[1];
    private EGL10 mEgl;

    EglHelper() {
        mEgl = (EGL10) EGLContext.getEGL();
    }

    SurfaceTexture getEglSurfaceTexture() {
        return eglSurfaceTexture;
    }

    SurfaceTexture createSurface(SurfaceTexture surfaceTexture, boolean isVideo) {
        this.eglDisplay = mEgl.eglGetDisplay(EGL10.EGL_DEFAULT_DISPLAY);
        int[] unusedEglVersion = new int[2];
        if (!mEgl.eglInitialize(eglDisplay, unusedEglVersion)) {
            throw new RuntimeException("Unable to initialize egl");
        }

        //Prepare the context
        int[] eglContextAttributes = {
                0x3098, 3, //Version 3
                EGL10.EGL_NONE //Null
        };

        EGLConfig eglConfig = createEGLConfig(3, isVideo);
        if (eglConfig != null) {
            eglContext = mEgl.eglCreateContext(eglDisplay, eglConfig, EGL10.EGL_NO_CONTEXT, eglContextAttributes);
            if (mEgl.eglGetError() != EGL10.EGL_SUCCESS) {
                eglContext = EGL10.EGL_NO_CONTEXT;
            }
        }

        if (eglContext == EGL10.EGL_NO_CONTEXT) {
            eglContextAttributes[1] = 2; //Fall back to version 2
            eglConfig = createEGLConfig(2, isVideo);
            eglContext = mEgl.eglCreateContext(eglDisplay, eglConfig, EGL10.EGL_NO_CONTEXT, eglContextAttributes);
        }

        // Confirm with query.
//        int[] values = new int[1];
//        mEgl.eglQueryContext(eglDisplay, eglContext, EGL10.EGL_CONTEXT_CLIENT_VERSION, values, 0);

        // Prepare the surface
        int[] surfaceAttributes = {
                EGL10.EGL_NONE //Null
        };
        eglSurface = mEgl.eglCreateWindowSurface(eglDisplay, eglConfig, surfaceTexture, surfaceAttributes);
        checkEGLError("eglCreateWindowSurface");
        if (!mEgl.eglMakeCurrent(eglDisplay, eglSurface, eglSurface, eglContext)) {
            throw new RuntimeException("eglMakeCurrent failed");
        }

        //Create eglTextures
        GLES20.glGenTextures(eglTextures.length, eglTextures, 0);
        checkGLError("Texture bind");
        eglSurfaceTexture = new SurfaceTexture(eglTextures[0]);

        return eglSurfaceTexture;
    }

    void checkGLError(String op) {
        int error = GLES20.glGetError();
        if (error != GLES20.GL_NO_ERROR) {
            String msg = op + ": glError 0x" + Integer.toHexString(error);
            throw new RuntimeException(msg);
        }
    }

    void destroySurface() {
        if (eglDisplay != EGL10.EGL_NO_DISPLAY) {
            boolean released;
            released = mEgl.eglTerminate(eglDisplay);
            released = mEgl.eglMakeCurrent(eglDisplay, EGL10.EGL_NO_SURFACE, EGL10.EGL_NO_SURFACE, EGL10.EGL_NO_CONTEXT);
            released = mEgl.eglDestroyContext(eglDisplay, eglContext);
        }

        eglDisplay = EGL10.EGL_NO_DISPLAY;
        eglContext = EGL10.EGL_NO_CONTEXT;
        eglSurface = EGL10.EGL_NO_SURFACE;
        eglSurfaceTexture = null;
    }

    private EGLConfig createEGLConfig(int version, boolean isVideo) {
        // The actual surface is generally RGBA, so omitting alpha
        // doesn't really help.  It can also lead to a huge performance hit on glReadPixels()
        // when reading into a GL_RGBA buffer.
        int renderType = EGL_OPENGL_ES2_BIT;
        int[] attributeList = {
                EGL10.EGL_RED_SIZE, 8,
                EGL10.EGL_GREEN_SIZE, 8,
                EGL10.EGL_BLUE_SIZE, 8,
                EGL10.EGL_ALPHA_SIZE, 8,
                //EGL10.EGL_DEPTH_SIZE, 16, //We are not going to use depth buffers
                //EGL10.EGL_STENCIL_SIZE, 8,
                EGL10.EGL_RENDERABLE_TYPE, renderType,
                EGL10.EGL_NONE, 0,      // placeholder for video, if set
                EGL10.EGL_NONE //Null terminated
        };
        if (isVideo) {
            //Custom flag to allow recording video from openGL texture
            attributeList[attributeList.length - 3] = 0x3142; //Magic
            attributeList[attributeList.length - 2] = 1;
        }
        EGLConfig[] configs = new EGLConfig[1];
        int[] numConfigs = new int[1];
        if (!mEgl.eglChooseConfig(eglDisplay, attributeList, configs, configs.length, numConfigs)) {
            return null;
        }
        return configs[0];
    }


    boolean makeCurrent() {
        boolean success = mEgl.eglMakeCurrent(eglDisplay, eglSurface, eglSurface, eglContext);
        if (!success) {
        }
        return success;
    }

    boolean swapBuffers() {
        boolean success = mEgl.eglSwapBuffers(eglDisplay, eglSurface);
        if (!success) {
        }
        return success;
    }

    /**
     * Checks for EGL errors.  Throws an exception if an error has been raised.
     */
    private void checkEGLError(String msg) {
        int error;
        if ((error = mEgl.eglGetError()) != EGL10.EGL_SUCCESS) {
            throw new RuntimeException(msg + ": EGL error: 0x" + Integer.toHexString(error));
        }
    }

    void bind() {
        makeCurrent();
    }

    void unbind() {
        mEgl.eglMakeCurrent(eglDisplay, EGL10.EGL_NO_SURFACE, EGL10.EGL_NO_SURFACE, EGL10.EGL_NO_CONTEXT);
    }
}
