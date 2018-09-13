package com.iaandroid.tutsopengles.fbo

import android.graphics.BitmapFactory
import android.opengl.GLSurfaceView
import com.iaandroid.tutsopengles.R
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

/**
 * Created by dastan on 11/09/2018.
 * ask2iqbal@gmail.com
 * 11/09/2018 10:06
 */
class EZRenderer(val glSurfaceView: GLSurfaceView) : GLSurfaceView.Renderer {
    private val glRender = SimpleFBORender()

    override fun onDrawFrame(gl: GL10?) {
        glRender.onDrawFrame()
    }

    override fun onSurfaceChanged(gl: GL10?, w: Int, h: Int) {
        glRender.setRenderSize(w, h)
    }

    override fun onSurfaceCreated(gl: GL10?, p1: EGLConfig?) {
        glRender.initHandlers(BitmapFactory.decodeResource(glSurfaceView.resources, R.mipmap.ic_launcher))
        glRender.initFBO()
        //glRender.setTexture(BitmapFactory.decodeResource(glSurfaceView.resources, R.mipmap.ic_launcher), false)
    }
}