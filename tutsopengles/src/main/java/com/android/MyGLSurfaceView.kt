package com.android

import android.content.Context
import android.opengl.GLSurfaceView

/**
 * Created by dastaniqbal on 12/02/2018.
 * dastanIqbal@marvelmedia.com
 * 12/02/2018 9:47
 */

class MyGLSurfaceView(context: Context) : GLSurfaceView(context) {
    init {
        setEGLContextClientVersion(2)
        setEGLConfigChooser(8, 8, 8, 8, 8, 8)
        setRenderer(MyGLRenderer())
       // renderMode = GLSurfaceView.RENDERMODE_WHEN_DIRTY
    }

}
