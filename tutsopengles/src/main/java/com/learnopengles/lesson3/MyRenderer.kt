package com.learnopengles.lesson3

import android.opengl.GLES20
import android.opengl.GLSurfaceView
import android.opengl.Matrix
import android.os.SystemClock
import com.raywenderlich.GLUtils
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

/**
 * Created by dastan on 05/03/2018.
 * ask2iqbal@gmail.com
 * 05/03/2018 10:09
 */
class MyRenderer : GLSurfaceView.Renderer {
    val mMVPMatrix = FloatArray(16)
    val mProjectionMatrix = FloatArray(16)
    val mViewMatrix = FloatArray(16)
    val mModelMatrix = FloatArray(16)

    // X, Y, Z
    val mCubeVertices = floatArrayOf(
            // In OpenGL counter-clockwise winding is default. This means that when we look at a triangle,
            // if the points are counter-clockwise we are looking at the "front". If not we are looking at
            // the back. OpenGL has an optimization where all back-facing triangles are culled, since they
            // usually represent the backside of an object and aren't visible anyways.

            // Front face
            -1.0f, 1.0f, 1.0f, -1.0f, -1.0f, 1.0f, 1.0f, 1.0f, 1.0f, -1.0f, -1.0f, 1.0f, 1.0f, -1.0f, 1.0f, 1.0f, 1.0f, 1.0f,

            // Right face
            1.0f, 1.0f, 1.0f, 1.0f, -1.0f, 1.0f, 1.0f, 1.0f, -1.0f, 1.0f, -1.0f, 1.0f, 1.0f, -1.0f, -1.0f, 1.0f, 1.0f, -1.0f,

            // Back face
            1.0f, 1.0f, -1.0f, 1.0f, -1.0f, -1.0f, -1.0f, 1.0f, -1.0f, 1.0f, -1.0f, -1.0f, -1.0f, -1.0f, -1.0f, -1.0f, 1.0f, -1.0f,

            // Left face
            -1.0f, 1.0f, -1.0f, -1.0f, -1.0f, -1.0f, -1.0f, 1.0f, 1.0f, -1.0f, -1.0f, -1.0f, -1.0f, -1.0f, 1.0f, -1.0f, 1.0f, 1.0f,

            // Top face
            -1.0f, 1.0f, -1.0f, -1.0f, 1.0f, 1.0f, 1.0f, 1.0f, -1.0f, -1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f, -1.0f,

            // Bottom face
            1.0f, -1.0f, -1.0f, 1.0f, -1.0f, 1.0f, -1.0f, -1.0f, -1.0f, 1.0f, -1.0f, 1.0f, -1.0f, -1.0f, 1.0f, -1.0f, -1.0f, -1.0f
    )

    val mColorVertices = floatArrayOf(
            // Front face (red)
            1.0f, 0.0f, 0.0f, 1.0f,
            1.0f, 0.0f, 0.0f, 1.0f,
            1.0f, 0.0f, 0.0f, 1.0f,
            1.0f, 0.0f, 0.0f, 1.0f,
            1.0f, 0.0f, 0.0f, 1.0f,
            1.0f, 0.0f, 0.0f, 1.0f,

            // Right face (green)
            0.0f, 1.0f, 0.0f, 1.0f,
            0.0f, 1.0f, 0.0f, 1.0f,
            0.0f, 1.0f, 0.0f, 1.0f,
            0.0f, 1.0f, 0.0f, 1.0f,
            0.0f, 1.0f, 0.0f, 1.0f,
            0.0f, 1.0f, 0.0f, 1.0f,

            // Back face (blue)
            1.0f, 0.0f, 1.0f, 1.0f,
            1.0f, 0.0f, 1.0f, 1.0f,
            1.0f, 0.0f, 1.0f, 1.0f,
            1.0f, 0.0f, 1.0f, 1.0f,
            1.0f, 0.0f, 1.0f, 1.0f,
            1.0f, 0.0f, 1.0f, 1.0f,

            // Left face (yellow)
            1.0f, 1.0f, 0.0f, 1.0f,
            1.0f, 1.0f, 0.0f, 1.0f,
            1.0f, 1.0f, 0.0f, 1.0f,
            1.0f, 1.0f, 0.0f, 1.0f,
            1.0f, 1.0f, 0.0f, 1.0f,
            1.0f, 1.0f, 0.0f, 1.0f,

            // Top face (cyan)
            0.0f, 1.0f, 1.0f, 1.0f,
            0.0f, 1.0f, 1.0f, 1.0f,
            0.0f, 1.0f, 1.0f, 1.0f,
            0.0f, 1.0f, 1.0f, 1.0f,
            0.0f, 1.0f, 1.0f, 1.0f,
            0.0f, 1.0f, 1.0f, 1.0f,

            // Bottom face (magenta)
            0.0f, 0.0f, 1.0f, 1.0f,
            0.0f, 0.0f, 1.0f, 1.0f,
            0.0f, 0.0f, 1.0f, 1.0f,
            0.0f, 0.0f, 1.0f, 1.0f,
            0.0f, 0.0f, 1.0f, 1.0f,
            0.0f, 0.0f, 1.0f, 1.0f
    )

    val mCubeFloatBuffer: FloatBuffer
    val mColorFloatBuffer: FloatBuffer
    private val mBytesPerFloat: Byte = 4

    init {
        mCubeFloatBuffer = ByteBuffer.allocateDirect(mCubeVertices.size * mBytesPerFloat).order(ByteOrder.nativeOrder()).asFloatBuffer()
        mCubeFloatBuffer.put(mCubeVertices).position(0)

        mColorFloatBuffer = ByteBuffer.allocateDirect(mColorVertices.size * mBytesPerFloat).order(ByteOrder.nativeOrder()).asFloatBuffer()
        mColorFloatBuffer.put(mColorVertices).position(0)
    }

    /** Size of the position data in elements.  */
    private val mPositionDataSize = 3
    private val mColorDataSize: Int=4

    override fun onDrawFrame(gl: GL10?) {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT.or(GLES20.GL_DEPTH_BUFFER_BIT))
        /// Do a complete rotation every 10 seconds.
        val time = SystemClock.uptimeMillis() % 10000L;
        val angleInDegrees = (360.0f / 10000.0f) * time.toInt();

        Matrix.setIdentityM(mModelMatrix, 0);
        Matrix.translateM(mModelMatrix, 0, 0f, 0f, -5f)
        Matrix.rotateM(mModelMatrix, 0, angleInDegrees, 1f, 1f, 1f)
        drawCube()

        Matrix.setIdentityM(mModelMatrix, 0);
        Matrix.translateM(mModelMatrix, 0, 5f, 0f, -8f)
        Matrix.rotateM(mModelMatrix, 0, angleInDegrees, 1f, 1f, 1f)
        drawCube()

        Matrix.setIdentityM(mModelMatrix, 0);
        Matrix.translateM(mModelMatrix, 0, -5f, 0f, -8f)
        Matrix.rotateM(mModelMatrix, 0, angleInDegrees, 1f, 1f, 1f)
        drawCube()

        Matrix.setIdentityM(mModelMatrix, 0);
        Matrix.translateM(mModelMatrix, 0, 0f, 5f, -8f)
        Matrix.rotateM(mModelMatrix, 0, angleInDegrees, 1f, 1f, 1f)
        drawCube()

        Matrix.setIdentityM(mModelMatrix, 0);
        Matrix.translateM(mModelMatrix, 0, 0f, -5f, -8f)
        Matrix.rotateM(mModelMatrix, 0, angleInDegrees, 1f, 1f, 1f)
        drawCube()
    }

    private fun drawCube() {
        Matrix.multiplyMM(mMVPMatrix, 0, mViewMatrix, 0, mModelMatrix, 0)
        Matrix.multiplyMM(mMVPMatrix, 0, mProjectionMatrix, 0, mMVPMatrix, 0)

        mColorFloatBuffer.position(0)
        GLES20.glUseProgram(mProgramHandle)
        GLES20.glVertexAttribPointer(mPositionHandle, mPositionDataSize, GLES20.GL_FLOAT, false, 0, mCubeFloatBuffer)
        GLES20.glEnableVertexAttribArray(mPositionHandle)

        GLES20.glVertexAttribPointer(mColorHandle, mColorDataSize, GLES20.GL_FLOAT, false, 0, mColorFloatBuffer)
        GLES20.glEnableVertexAttribArray(mColorHandle)

        GLES20.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, mMVPMatrix, 0)
        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, 36)
    }

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        GLES20.glViewport(0, 0, width, height)
        val aspectRatio: Float = width.toFloat() / height.toFloat()
        Matrix.frustumM(mProjectionMatrix, 0, -aspectRatio, aspectRatio, -1f, 1f, 1f, 10f)
    }

    private var mPositionHandle: Int = -1
    private var mMVPMatrixHandle: Int = -1
    private var mColorHandle: Int = -1

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        GLES20.glClearColor(11f / 255, 19f / 255, 33f / 255, 1f)
        // Use culling to remove back faces.
        GLES20.glEnable(GLES20.GL_CULL_FACE)

        // Enable depth testing
        GLES20.glEnable(GLES20.GL_DEPTH_TEST)

        Matrix.setLookAtM(mViewMatrix, 0, 0f, 1f, 0f, 0f, 0f, -5f, 0f, 1f, 0f)
        mProgramHandle = GLUtils.loadProgram(vs, fs)

        mMVPMatrixHandle = GLES20.glGetUniformLocation(mProgramHandle, "mMVPMatrix")
        mPositionHandle = GLES20.glGetAttribLocation(mProgramHandle, "aPosition")
        mColorHandle = GLES20.glGetAttribLocation(mProgramHandle, "aColor")
    }


    private var mProgramHandle: Int = -1
    val vs = (""
            + "uniform mat4 mMVPMatrix;\n"
            + "attribute vec4 aPosition;\n"
            + " attribute vec4 aColor;\n"
            + "varying vec4 vColor;\n"
            + "void main(){\n"
            + " gl_Position=mMVPMatrix*aPosition;\n"
            + "  vColor=aColor;\n"
            + "}"
            )
    val fs = (""
            + "precision mediump float;\n"
            + "varying vec4 vColor;\n"
            + "void main(){\n"
            + " gl_FragColor=vColor;\n"
            + "}"
            )
}