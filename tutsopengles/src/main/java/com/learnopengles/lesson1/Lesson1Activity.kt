package com.learnopengles.lesson1

import android.app.Activity
import android.opengl.GLES20
import android.opengl.GLSurfaceView
import android.opengl.Matrix
import android.os.Bundle
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

/**
 * Created by dastaniqbal on 14/02/2018.
 * dastanIqbal@marvelmedia.com
 * 14/02/2018 11:03
 */
class Lesson1Activity : Activity() {
    lateinit var glSurfaceView: GLSurfaceView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        glSurfaceView = GLSurfaceView(this)
        setContentView(glSurfaceView)

        glSurfaceView.setEGLContextClientVersion(2)
        glSurfaceView.setRenderer(Lesson1Renderer())
        glSurfaceView.renderMode = GLSurfaceView.RENDERMODE_WHEN_DIRTY
    }

    override fun onResume() {
        super.onResume()
        glSurfaceView.onResume()
    }

    override fun onPause() {
        super.onPause()
        glSurfaceView.onPause()
    }

    inner class Lesson1Renderer : GLSurfaceView.Renderer {
        val mProjectionMatrix = FloatArray(16)
        val triangle1 = floatArrayOf(
                //X, Y, Z
                // R, G, B, A
                -0.5f, -0.25f, 0.0f,
                1.0f, 0.0f, 0.0f, 1.0f,

                0.5f, -0.25f, 0.0f,
                0.0f, 0.0f, 1.0f, 1.0f,

                0.0f, 0.559016994f, 0.0f,
                0.0f, 1.0f, 0.0f, 1.0f
        )
        val triangle2 = floatArrayOf(
                -0.5f, -0.25f, 0.0f,
                1.0f, 1.0f, 0.0f, 1.0f,

                0.5f, -0.25f, 0.0f,
                0.0f, 1.0f, 1.0f, 1.0f,

                0.0f, 0.559016994f, 0.0f,
                1.0f, 0.0f, 1.0f, 1.0f
        )
        val triangle3 = floatArrayOf(
                -0.5f, -0.25f, 0.0f,
                1.0f, 1.0f, 1.0f, 1.0f,

                0.5f, -0.25f, 0.0f,
                0.5f, 0.5f, 0.5f, 1.0f,

                0.0f, 0.559016994f, 0.0f,
                0.0f, 0.0f, 0.0f, 1.0f
        )

        var tri1FloatBuffer: FloatBuffer
        var tri2FloatBuffer: FloatBuffer
        var tri3FloatBuffer: FloatBuffer

        init {
            var byteBuffer = ByteBuffer.allocateDirect(triangle1.size * 4).order(ByteOrder.nativeOrder())

            tri1FloatBuffer = byteBuffer.asFloatBuffer()
            tri1FloatBuffer.put(triangle1)
            tri1FloatBuffer.position(0)

            byteBuffer = ByteBuffer.allocateDirect(triangle2.size * 4).order(ByteOrder.nativeOrder())

            tri2FloatBuffer = byteBuffer.asFloatBuffer()
            tri2FloatBuffer.put(triangle2)
            tri2FloatBuffer.position(0)

            byteBuffer = ByteBuffer.allocateDirect(triangle3.size * 4).order(ByteOrder.nativeOrder())

            tri3FloatBuffer = byteBuffer.asFloatBuffer()
            tri3FloatBuffer.put(triangle3)
            tri3FloatBuffer.position(0)

        }

        override fun onDrawFrame(p0: GL10?) {
            GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT.or(GLES20.GL_DEPTH_BUFFER_BIT))
        }

        override fun onSurfaceChanged(unused: GL10?, width: Int, height: Int) {
            GLES20.glViewport(0, 0, width, height)
            val aspect: Float = width.toFloat() / height.toFloat()
            Matrix.frustumM(mProjectionMatrix, 0, -aspect, aspect, -1f, 1f, 3f, 7f)
        }

        override fun onSurfaceCreated(unused: GL10?, config: EGLConfig?) {
            GLES20.glClearColor(125f/255f, 253f/255f, 254f/255f, 1f)
        }

    }
}