package com.iaandroid.tutsopengles.frames

import android.opengl.GLSurfaceView
import android.os.Bundle
import android.support.v7.app.AppCompatActivity

/**
 * Created by dastan on 05/09/2018.
 * ask2iqbal@gmail.com
 * 05/09/2018 11:23
 */
class FramesActivity : AppCompatActivity() {
    private val glSurfaceView: GLSurfaceView by lazy {
        GLSurfaceView(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(glSurfaceView)
        glSurfaceView.setEGLContextClientVersion(2)
        glSurfaceView.setRenderer(MyRenderer(this))
    }
}