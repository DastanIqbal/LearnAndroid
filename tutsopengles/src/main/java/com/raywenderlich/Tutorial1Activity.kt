package com.raywenderlich

import android.app.Activity
import android.opengl.GLES20
import android.opengl.GLSurfaceView
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

/**
 * Created by dastaniqbal on 11/02/2018.

 * 11/02/2018 12:30
 */
class Tutorial1Activity : Activity(), GLSurfaceView.Renderer {
    var isRed = false
    var count = 0
    override fun onDrawFrame(p0: GL10?) {
        if (count >= 1 * 1 * 100) {
            isRed = if (isRed) {
                GLES20.glClearColor(0f, 0f, 0f, 1f)
                false
            } else {
                GLES20.glClearColor(1f, 0f, 0f, 1f)
                true
            }
            count = 0
        }
        count++
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT)
    }

    override fun onSurfaceChanged(p0: GL10?, p1: Int, p2: Int) {

    }

    override fun onSurfaceCreated(p0: GL10?, p1: EGLConfig?) {
        GLES20.glClearColor(0f, 104f / 255f, 55f / 255f, 1f)
    }
}