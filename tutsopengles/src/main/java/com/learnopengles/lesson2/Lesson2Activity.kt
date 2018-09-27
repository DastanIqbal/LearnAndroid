package com.learnopengles.lesson2

import android.app.Activity
import android.opengl.GLSurfaceView
import android.os.Bundle

/**
 * Created by dastaniqbal on 25/02/2018.

 * 25/02/2018 12:14
 */
class Lesson2Activity : Activity() {
    lateinit var glSurfaceView: GLSurfaceView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        glSurfaceView = GLSurfaceView(this)
        glSurfaceView.setEGLContextClientVersion(2)
        glSurfaceView.setRenderer(MyRenderer())
        setContentView(glSurfaceView)
    }

    override fun onResume() {
        super.onResume()
        glSurfaceView.onResume()
    }

    override fun onPause() {
        super.onPause()
        glSurfaceView.onPause()
    }
}