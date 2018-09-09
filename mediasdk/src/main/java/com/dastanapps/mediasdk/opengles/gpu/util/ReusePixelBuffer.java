/*
 * Copyright (C) 2012 CyberAgent
 * Copyright (C) 2010 jsemler
 *
 * Original publication without License
 * http://www.anddev.org/android-2d-3d-graphics-opengl-tutorials-f2/possible-to-do-opengl-off-screen-rendering-in-android-t13232.html#p41662
 */

package com.dastanapps.mediasdk.opengles.gpu.util;

import android.graphics.Bitmap;
import android.opengl.GLSurfaceView;
import android.util.Log;

import java.nio.IntBuffer;
import java.util.Arrays;

import javax.microedition.khronos.egl.EGL10;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.egl.EGLContext;
import javax.microedition.khronos.egl.EGLDisplay;
import javax.microedition.khronos.egl.EGLSurface;
import javax.microedition.khronos.opengles.GL10;

import static javax.microedition.khronos.egl.EGL10.EGL_ALPHA_SIZE;
import static javax.microedition.khronos.egl.EGL10.EGL_BLUE_SIZE;
import static javax.microedition.khronos.egl.EGL10.EGL_DEFAULT_DISPLAY;
import static javax.microedition.khronos.egl.EGL10.EGL_DEPTH_SIZE;
import static javax.microedition.khronos.egl.EGL10.EGL_GREEN_SIZE;
import static javax.microedition.khronos.egl.EGL10.EGL_HEIGHT;
import static javax.microedition.khronos.egl.EGL10.EGL_NONE;
import static javax.microedition.khronos.egl.EGL10.EGL_NO_CONTEXT;
import static javax.microedition.khronos.egl.EGL10.EGL_RED_SIZE;
import static javax.microedition.khronos.egl.EGL10.EGL_STENCIL_SIZE;
import static javax.microedition.khronos.egl.EGL10.EGL_WIDTH;
import static javax.microedition.khronos.opengles.GL10.GL_RGBA;
import static javax.microedition.khronos.opengles.GL10.GL_UNSIGNED_BYTE;

public class ReusePixelBuffer {
    final static String TAG = "PixelBuffer";
    final static boolean LIST_CONFIGS = false;

    GLSurfaceView.Renderer mRenderer; // borrow this interface
    int mWidth, mHeight;
    Bitmap mBitmap;

    EGL10 mEGL;
    EGLDisplay mEGLDisplay;
    EGLConfig[] mEGLConfigs;
    EGLConfig mEGLConfig;
    EGLContext mEGLContext;
    EGLSurface mEGLSurface;
    GL10 mGL;

    String mThreadOwner;
    private IntBuffer ib;
    private int[] iat;

    public ReusePixelBuffer(final int width, final int height) {
        mWidth = width;
        mHeight = height;
        int capacity = width * height;
        iat = new int[capacity];
        ib = IntBuffer.wrap(iat);
        mBitmap = Bitmap.createBitmap(mWidth, mHeight, Bitmap.Config.ARGB_8888);

        int[] version = new int[2];
        int[] attribList = new int[]{
                EGL_WIDTH, mWidth,
                EGL_HEIGHT, mHeight,
                EGL_NONE
        };

        // No error checking performed, minimum required code to elucidate logic
        mEGL = (EGL10) EGLContext.getEGL();
        mEGLDisplay = mEGL.eglGetDisplay(EGL_DEFAULT_DISPLAY);
        mEGL.eglInitialize(mEGLDisplay, version);
        mEGLConfig = chooseConfig(); // Choosing a config is a little more
        // complicated

        // mEGLContext = mEGL.eglCreateContext(mEGLDisplay, mEGLConfig,
        // EGL_NO_CONTEXT, null);
        int EGL_CONTEXT_CLIENT_VERSION = 0x3098;
        int[] attrib_list = {
                EGL_CONTEXT_CLIENT_VERSION, 2,
                EGL10.EGL_NONE
        };
        mEGLContext = mEGL.eglCreateContext(mEGLDisplay, mEGLConfig, EGL_NO_CONTEXT, attrib_list);

        mEGLSurface = mEGL.eglCreatePbufferSurface(mEGLDisplay, mEGLConfig, attribList);
        mEGL.eglMakeCurrent(mEGLDisplay, mEGLSurface, mEGLSurface, mEGLContext);

        mGL = (GL10) mEGLContext.getGL();

        // Record thread owner of OpenGL context
        mThreadOwner = Thread.currentThread().getName();
    }

    public void setRenderer(final GLSurfaceView.Renderer renderer) {
        mRenderer = renderer;

        // Does this thread own the OpenGL context?
        if (!Thread.currentThread().getName().equals(mThreadOwner)) {
            Log.e(TAG, "setRenderer: This thread does not own the OpenGL context.");
            return;
        }

        // Call the renderer initialization routines
        mRenderer.onSurfaceCreated(mGL, mEGLConfig);
        mRenderer.onSurfaceChanged(mGL, mWidth, mHeight);
    }

    public Bitmap getBitmap() {
        // Do we have a renderer?
        if (mRenderer == null) {
            Log.e(TAG, "getBitmap: Renderer was not set.");
            return null;
        }

        // Does this thread own the OpenGL context?
        if (Thread.currentThread().getName().equals(mThreadOwner)) {
            if (mBitmap != null) {
                synchronized (mBitmap) {
                    mRenderer.onDrawFrame(mGL);
                    convertToBitmap();
                }
            }
            return mBitmap;
        }
        return null;
    }

    public Bitmap getmBitmap() {  //New method
        return this.mBitmap;
    }


    public void destroy() {
        mRenderer.onDrawFrame(mGL);
        mRenderer.onDrawFrame(mGL);
        mEGL.eglMakeCurrent(mEGLDisplay, EGL10.EGL_NO_SURFACE,
                EGL10.EGL_NO_SURFACE, EGL10.EGL_NO_CONTEXT);

        mEGL.eglDestroySurface(mEGLDisplay, mEGLSurface);
        mEGL.eglDestroyContext(mEGLDisplay, mEGLContext);
        mEGL.eglTerminate(mEGLDisplay);

        if (this.ib != null) {
            this.ib.clear(); //clear IntBuffer
            this.ib = null;
        }
        this.iat = null;
        if (!(this.mBitmap == null || this.mBitmap.isRecycled())) {
            this.mBitmap.recycle();
        }
        this.mBitmap = null;  //Releasing mBitmap
    }

    private EGLConfig chooseConfig() {
        int[] attribList = new int[]{
                EGL_DEPTH_SIZE, 0,
                EGL_STENCIL_SIZE, 0,
                EGL_RED_SIZE, 8,
                EGL_GREEN_SIZE, 8,
                EGL_BLUE_SIZE, 8,
                EGL_ALPHA_SIZE, 8,
                EGL10.EGL_RENDERABLE_TYPE, 4,
                EGL_NONE
        };

        // No error checking performed, minimum required code to elucidate logic
        // Expand on this logic to be more selective in choosing a configuration
        int[] numConfig = new int[1];
        mEGL.eglChooseConfig(mEGLDisplay, attribList, null, 0, numConfig);
        int configSize = numConfig[0];
        mEGLConfigs = new EGLConfig[configSize];
        mEGL.eglChooseConfig(mEGLDisplay, attribList, mEGLConfigs, configSize, numConfig);

        if (LIST_CONFIGS) {
            listConfig();
        }

        return mEGLConfigs[0]; // Best match is probably the first configuration
    }

    private void listConfig() {
        Log.i(TAG, "Config List {");

        for (EGLConfig config : mEGLConfigs) {
            int d, s, r, g, b, a;

            // Expand on this logic to dump other attributes
            d = getConfigAttrib(config, EGL_DEPTH_SIZE);
            s = getConfigAttrib(config, EGL_STENCIL_SIZE);
            r = getConfigAttrib(config, EGL_RED_SIZE);
            g = getConfigAttrib(config, EGL_GREEN_SIZE);
            b = getConfigAttrib(config, EGL_BLUE_SIZE);
            a = getConfigAttrib(config, EGL_ALPHA_SIZE);
            Log.i(TAG, "    <d,s,r,g,b,a> = <" + d + "," + s + "," +
                    r + "," + g + "," + b + "," + a + ">");
        }

        Log.i(TAG, "}");
    }

    private int getConfigAttrib(final EGLConfig config, final int attribute) {
        int[] value = new int[1];
        return mEGL.eglGetConfigAttrib(mEGLDisplay, config,
                attribute, value) ? value[0] : 0;
    }

    private void convertToBitmap() {
        if (iat != null && ib != null) {
            synchronized (iat) {
                Arrays.fill(iat, 0);
                ib.position(0);
                mGL.glReadPixels(0, 0, mWidth, mHeight, GL_RGBA, GL_UNSIGNED_BYTE, ib);
            }
        }

        if (this.mBitmap != null && this.ib != null) {
            mBitmap.copyPixelsFromBuffer(this.ib);
        }
    }
}
