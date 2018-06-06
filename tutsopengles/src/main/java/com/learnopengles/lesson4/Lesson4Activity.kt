package com.learnopengles.lesson4

import android.app.Activity
import android.opengl.GLSurfaceView
import android.os.Bundle

/**
 * Created by dastan on 05/03/2018.
 * ask2iqbal@gmail.com
 * 05/03/2018 10:07
 */
class Lesson4Activity : Activity(){
    lateinit var glSurfaceView:GLSurfaceView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        glSurfaceView= GLSurfaceView(this)
        glSurfaceView.setEGLContextClientVersion(2)
        glSurfaceView.setRenderer(MyRenderer(this))
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