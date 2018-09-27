package com.armmali.graphicssetup

import android.content.Context
import com.armmali.firstnative.NativeLibrary
import com.iaandroid.tutsopengles.gles.GLTextureView
import javax.microedition.khronos.egl.EGL10
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.egl.EGLContext
import javax.microedition.khronos.egl.EGLDisplay
import javax.microedition.khronos.opengles.GL10


/**
 * Created by dastaniqbal on 06/01/2018.

 * 06/01/2018 4:52
 */
class TutorialView(context: Context) : GLTextureView(context) {
    protected var redSize = 8
    protected var greenSize = 8
    protected var blueSize = 8
    protected var alphaSize = 8
    protected var depthSize = 16
    protected var sampleSize = 4
    protected var stencilSize = 0
    protected var value = IntArray(1)

    init {
        setEGLContextFactory(ContextFactory())
        setEGLConfigChooser(ConfigChooser())
        setRenderer(MyRenderer())
    }

    inner class ContextFactory : GLTextureView.EGLContextFactory {
        override fun createContext(egl: EGL10, display: EGLDisplay?, eglConfig: EGLConfig?): EGLContext {
            val EGL_CONTEXT_CLIENT_VERSION = 0x3098
            val attrib_list = intArrayOf(EGL_CONTEXT_CLIENT_VERSION, 2, EGL10.EGL_NONE)
            return egl.eglCreateContext(display, eglConfig, EGL10.EGL_NO_CONTEXT, attrib_list)
        }

        override fun destroyContext(egl: EGL10, display: EGLDisplay?, context: EGLContext?) {
            egl.eglDestroyContext(display, context)
        }

    }

    inner class ConfigChooser : GLTextureView.EGLConfigChooser {
        override fun chooseConfig(egl: EGL10, display: EGLDisplay?): EGLConfig? {
            val EGL_OPENGL_ES2_BIT = 4
            val configAttributes = intArrayOf(EGL10.EGL_RED_SIZE, redSize,
                    EGL10.EGL_GREEN_SIZE, greenSize,
                    EGL10.EGL_BLUE_SIZE, blueSize,
                    EGL10.EGL_RENDERABLE_TYPE, EGL_OPENGL_ES2_BIT,
                    EGL10.EGL_SAMPLES, sampleSize,
                    EGL10.EGL_DEPTH_SIZE, depthSize,
                    EGL10.EGL_STENCIL_SIZE, stencilSize,
                    EGL10.EGL_NONE)
            val num_config = IntArray(1)
            egl.eglChooseConfig(display, configAttributes, null, 0, num_config)

            val numConfigs = num_config[0]
            val configs = arrayOfNulls<EGLConfig>(numConfigs)
            egl.eglChooseConfig(display, configAttributes, configs, numConfigs, num_config)

            return selectConfig(egl, display, configs)
        }

        private fun selectConfig(egl: EGL10, display: EGLDisplay?, configs: Array<EGLConfig?>): EGLConfig? {
            for (config in configs) {
                val d = getConfigAttrib(egl, display, config, EGL10.EGL_DEPTH_SIZE, 0)
                val s = getConfigAttrib(egl, display, config, EGL10.EGL_GREEN_SIZE, 0)
                val r = getConfigAttrib(egl, display, config, EGL10.EGL_RED_SIZE, 0)
                val g = getConfigAttrib(egl, display, config, EGL10.EGL_GREEN_SIZE, 0)
                val b = getConfigAttrib(egl, display, config, EGL10.EGL_BLUE_SIZE, 0)
                val a = getConfigAttrib(egl, display, config, EGL10.EGL_ALPHA_SIZE, 0)
                if (r == redSize && g == greenSize && b == blueSize && a == alphaSize && d >= depthSize && s >= stencilSize)
                    return config
            }
            return null
        }

        private fun getConfigAttrib(egl: EGL10, display: EGLDisplay?, config: EGLConfig?, attribute: Int, defaultValue: Int): Int {
            if (egl.eglGetConfigAttrib(display, config, attribute, value))
                return value[0]
            return defaultValue
        }

    }

    inner class MyRenderer : GLTextureView.Renderer {
        override fun onDrawFrame(gl: GL10?) {
            NativeLibrary.step()
        }

        override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
            NativeLibrary.init2(width, height)
        }

        override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {

        }

    }
}
