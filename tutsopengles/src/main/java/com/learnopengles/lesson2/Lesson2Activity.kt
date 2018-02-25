package com.learnopengles.lesson2

import android.app.Activity
import android.opengl.GLES20
import android.opengl.GLSurfaceView
import android.opengl.Matrix
import android.os.Bundle
import com.raywenderlich.GLUtils
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import java.nio.ShortBuffer
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

/**
 * Created by dastaniqbal on 25/02/2018.
 * dastanIqbal@marvelmedia.com
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

    inner class MyRenderer : GLSurfaceView.Renderer {
        private var mProgram: Int = -1
        private var mPositionHandle: Int = -1
        private var mMVPMatrixHandle: Int = -1
        val vs = ("" +
                "uniform mat4 mMVPMatrix;\n" +
                "attribute vec4 aPosition;\n" +
                "void main(){\n" +
                "       gl_Position=mMVPMatrix*aPosition;\n" +
                "}\n" + ""
                );
        val fs = ("" +
                "precision mediump float;" +
                "uniform vec4 vColor;" +
                "void main(){\n" +
                "       gl_FragColor=vec4(1,1,1,1);" +
                "}\n" +
                "")

        val mViewMatrix = FloatArray(16)
        val mProjectionMatrix = FloatArray(16)
        val mMVPMatrix = FloatArray(16)
        val mSquare1 = floatArrayOf(
                -0.5f,  0.5f, 0.0f,   // top left
                -0.5f, -0.5f, 0.0f,   // bottom left
                0.5f, -0.5f, 0.0f,   // bottom right
                0.5f,  0.5f, 0.0f  // top right
        )
        private val drawOrder = shortArrayOf(0, 1, 2, 0, 2, 3) // order to draw vertices

        val mSquare1FloatBuffer: FloatBuffer
        val mDrawOrderBuffer: ShortBuffer

        init {
            mSquare1FloatBuffer = ByteBuffer.allocateDirect(mSquare1.size * 4)
                    .order(ByteOrder.nativeOrder())
                    .asFloatBuffer()
            mSquare1FloatBuffer.put(mSquare1).position(0)

            // initialize byte buffer for the draw list
            mDrawOrderBuffer = ByteBuffer.allocateDirect(drawOrder.size * 2).order(ByteOrder.nativeOrder()).asShortBuffer();
            mDrawOrderBuffer.put(drawOrder);
            mDrawOrderBuffer.position(0);
        }

        override fun onDrawFrame(gl: GL10?) {
            GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT)

            Matrix.setLookAtM(mViewMatrix, 0, 0f, 0f, -2f, 0f, 0f, 0f, 0f, 1f, 0f)
            Matrix.multiplyMM(mMVPMatrix, 0, mProjectionMatrix, 0, mViewMatrix, 0)

            GLES20.glUseProgram(mProgram)
            GLES20.glEnableVertexAttribArray(mPositionHandle)
            GLES20.glVertexAttribPointer(mPositionHandle, 3, GLES20.GL_FLOAT, false, 3 * 4, mSquare1FloatBuffer)
            GLUtils.checkGlError("setVertex")

            GLES20.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, mMVPMatrix, 0)
            GLUtils.checkGlError("setMVPMatrix")

//            GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, mSquare1.size / 3)
            GLES20.glDrawElements(GLES20.GL_TRIANGLES, drawOrder.size, GLES20.GL_UNSIGNED_SHORT, mDrawOrderBuffer)
        }

        override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
            GLES20.glViewport(0, 0, width, height)
            val aspectRatio = width.toFloat() / height.toFloat()
            Matrix.frustumM(mProjectionMatrix, 0, -aspectRatio, aspectRatio, -1f, 1f, 1f, 10f)
        }

        override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
            GLES20.glClearColor(11f / 255f, 19f / 255f, 33f / 255f, 1f)

            mProgram = GLUtils.loadProgram(vs, fs)
            mPositionHandle = GLES20.glGetAttribLocation(mProgram, "aPosition")
            GLUtils.checkGlError("getPosition")
            mMVPMatrixHandle = GLES20.glGetUniformLocation(mProgram, "mMVPMatrix")
            GLUtils.checkGlError("getMVPMatrix")
        }
    }
}