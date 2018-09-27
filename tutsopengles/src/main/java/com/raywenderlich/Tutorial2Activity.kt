package com.raywenderlich

import android.opengl.GLES20
import android.opengl.GLSurfaceView
import android.opengl.Matrix
import com.iaandroid.tutsopengles.R
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

/**
 * Created by dastaniqbal on 11/02/2018.

 * 11/02/2018 1:32
 */
class Tutorial2Activity : BaseActivity(), GLSurfaceView.Renderer {
    var vertexBuffer: FloatBuffer? = null
    val buffer = intArrayOf(1)
    val vertices = floatArrayOf(
            -1f, -1f, 0f,
            -1f, 1f, 0f,
            0f, 0f, 0f
    )
    val color = floatArrayOf(1f, 0f, 1f, 1f)

    private var program: Int = -1
    var attribPositionHandler = 0
    var uniformColorHandler = 0
    private var mMVPMatrixHandle: Int = 0
    private val mMVPMatrix = FloatArray(16)
    private val mProjectionMatrix = FloatArray(16)
    private val mViewMatrix = FloatArray(16)
    private val mRotationMatrix = FloatArray(16)
    private var mAngle: Float = 0.toFloat()

    override fun onDrawFrame(p0: GL10?) {
        val scratch = FloatArray(16)
        // Draw background color
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT or GLES20.GL_DEPTH_BUFFER_BIT)

        // Set the camera position (View matrix)
        Matrix.setLookAtM(mViewMatrix, 0, 0f, 0f, -3f, 0f, 0f, 0f, 0f, 1.0f, 0.0f)
        Matrix.multiplyMM(mMVPMatrix, 0, mProjectionMatrix, 0, mViewMatrix, 0)

        Matrix.setRotateM(mRotationMatrix, 0, mAngle, 0f, 0f, 1.0f)
        // Calculate the projection and view transformation

        Matrix.multiplyMM(scratch, 0, mMVPMatrix, 0, mRotationMatrix, 0);
        // Add program to OpenGL environment
        GLES20.glUseProgram(program)

        attribPositionHandler = GLES20.glGetAttribLocation(program, "vPosition")
        GLES20.glEnableVertexAttribArray(attribPositionHandler)
        GLES20.glVertexAttribPointer(attribPositionHandler, 3, GLES20.GL_FLOAT, false,
                3 * 4, vertexBuffer)

        uniformColorHandler = GLES20.glGetUniformLocation(program, "vColor")
        GLES20.glUniform4fv(uniformColorHandler, 1, color, 0)

        // get handle to shape's transformation matrix
        mMVPMatrixHandle = GLES20.glGetUniformLocation(program, "uMVPMatrix")
        GLUtils.checkGlError("glGetUniformLocation")

        // Apply the projection and view transformation
        GLES20.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, scratch, 0)
        GLUtils.checkGlError("glUniformMatrix4fv")

        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, vertices.size / 3)
        GLES20.glDisableVertexAttribArray(attribPositionHandler)

    }

    override fun onSurfaceChanged(p0: GL10?, width: Int, height: Int) {
        GLES20.glViewport(0, 0, width, height);
        val ratio = width.toFloat() / height.toFloat()

        // this projection matrix is applied to object coordinates
        // in the onDrawFrame() method
        Matrix.frustumM(mProjectionMatrix, 0, -ratio, ratio, -1f, 1f, 3f, 7f)
    }

    override fun onSurfaceCreated(p0: GL10?, p1: EGLConfig?) {
        GLES20.glClearColor(0f, 0f, 0f, 1f)

        val byteBuffer = ByteBuffer.allocateDirect(vertices.size * 4)
        byteBuffer.order(ByteOrder.nativeOrder())
        vertexBuffer = byteBuffer.asFloatBuffer()
        vertexBuffer?.put(vertices)
        vertexBuffer?.position(0)

        val fs = GLUtils.readShader(this, R.raw.tut2fs)
        val vs = GLUtils.readShader(this, R.raw.tut2vs)
        program = GLUtils.loadProgram(vs, fs)
    }
}