package com.android

import android.app.Activity
import android.os.Bundle

/**
 * Created by dastaniqbal on 12/02/2018.
 * dastanIqbal@marvelmedia.com
 * 12/02/2018 9:50
 */
class OpenGLES20Activity : Activity() {
    lateinit var glSurfaceView: MyGLSurfaceView;
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        glSurfaceView = MyGLSurfaceView(this)
        setContentView(glSurfaceView)
    }

    override fun onPause() {
        super.onPause()
        glSurfaceView.onPause()
    }

    override fun onResume() {
        super.onResume()
        glSurfaceView.onResume()
    }
}