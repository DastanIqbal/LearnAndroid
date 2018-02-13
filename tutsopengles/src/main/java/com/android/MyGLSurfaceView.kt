package com.android

import android.content.Context
import android.opengl.GLSurfaceView
import android.view.MotionEvent

/**
 * Created by dastaniqbal on 12/02/2018.
 * dastanIqbal@marvelmedia.com
 * 12/02/2018 9:47
 */

class MyGLSurfaceView(context: Context) : GLSurfaceView(context) {
    private val TOUCH_SCALE_FACTOR = 180.0f / 320
    private var mPreviousX: Float = 0.toFloat()
    private var mPreviousY: Float = 0.toFloat()
    var mRenderer: MyGLRenderer

    init {
        setEGLContextClientVersion(2)
        setEGLConfigChooser(8, 8, 8, 8, 8, 8)
        mRenderer = MyGLRenderer(0f)
        setRenderer(mRenderer)
        renderMode = GLSurfaceView.RENDERMODE_WHEN_DIRTY
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        val x = event.x
        val y = event.y
        when (event.action) {
            MotionEvent.ACTION_MOVE -> {
                var dx = x - mPreviousX
                var dy = y - mPreviousY
                // reverse direction of rotation above the mid-line
                if (y > height / 2) dx *= -1
                // reverse direction of rotation to left of the mid-line
                if (x > width / 2) dy *= -1
                mRenderer.angle = mRenderer.angle + (dx + dy) * TOUCH_SCALE_FACTOR
                requestRender()
            }
        }
        mPreviousX = x
        mPreviousY = y
        return true
    }

}
