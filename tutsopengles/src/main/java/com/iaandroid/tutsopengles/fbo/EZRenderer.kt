package com.iaandroid.tutsopengles.fbo

import android.graphics.BitmapFactory
import android.opengl.GLSurfaceView
import android.view.MotionEvent
import android.view.View
import com.iaandroid.tutsopengles.R
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

/**
 * Created by dastan on 11/09/2018.
 * ask2iqbal@gmail.com
 * 11/09/2018 10:06
 */
class EZRenderer(val glSurfaceView: GLSurfaceView) : GLSurfaceView.Renderer {
    private val glRender = GraffitiStickerRender(glSurfaceView.context, object : GraffitiStickerRender.IStickerTimeController {
        override val currentTime: Float
            get() = 2000f

    })

    init {
        glSurfaceView.setOnTouchListener { v, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    // mTouchingTextureView = true
                    //  startVideo()
                    glRender.start()
                    glRender.setPosition(Math.round(event.x * glRender.getWidth() * 1f / glSurfaceView.getWidth()),
                            Math.round(event.y * glRender.getHeight() * 1f / glSurfaceView.getHeight()))
                    // mRenderPipeline.addFilterRender(mStickerRender)
                }
                MotionEvent.ACTION_MOVE -> glRender.setPosition(Math.round(event.x * glRender.getWidth() * 1f / mRenderView.getWidth()),
                        Math.round(event.y * glRender.getHeight() * 1f / glSurfaceView.getHeight()))
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    // mTouchingTextureView = false
                    //pauseVideo()
                    glRender.pause()
                }
            }
            true
        }
    }

    //private val glRender = FBORender()

    override fun onDrawFrame(gl: GL10?) {
        glRender.onDrawFrame()
    }

    override fun onSurfaceChanged(gl: GL10?, w: Int, h: Int) {
        glRender.setRenderSize(w, h)
    }

    override fun onSurfaceCreated(gl: GL10?, p1: EGLConfig?) {
        glRender.setTexture(BitmapFactory.decodeResource(glSurfaceView.resources, R.mipmap.ic_launcher), false)
    }
}