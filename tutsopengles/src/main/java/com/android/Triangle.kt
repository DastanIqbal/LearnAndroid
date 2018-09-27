package com.android

import android.opengl.GLES20
import com.raywenderlich.GLUtils
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer

/**
 * Created by dastaniqbal on 12/02/2018.

 * 12/02/2018 9:58
 */
class Triangle {
    val vertices = floatArrayOf(
            -1f, -1f, 1f,
            1f, -1f, 1f,
            0f, 0f, 1f
    )
    val colors = floatArrayOf(1f, 1f, 1f, 1f)
    // This matrix member variable provides a hook to manipulate
    // the coordinates of the objects that use this vertex shader
    val vs = ("" +
            "uniform mat4 uMVPMatrix;\n" +
            "attribute vec4 vPosition;\n" +
            "void main(){\n" +
            // the matrix must be included as a modifier of gl_Position
            // Note that the uMVPMatrix factor *must be first* in order
            // for the matrix multiplication product to be correct.
            "      gl_Position=uMVPMatrix*vPosition;\n" +
            "}\n" +
            "")
    val fs = ("" +
            "precision mediump float;" +
            "uniform vec4 vColor;" +
            "void main(){\n" +
            "       gl_FragColor=vec4(1,0,1,1);" +
            "}\n" +
            "")

    private var vertexBuffer: FloatBuffer? = null
    private var mProgram: Int = -1
    private var mPositionHandler: Int = -1
    private var mColorHandle: Int = -1
    // Use to access and set the view transformation
    private var mMVPMatrixHandle: Int = -1

    init {
        val bb = ByteBuffer.allocateDirect(vertices.size * 4)
        bb.order(ByteOrder.nativeOrder())

        vertexBuffer = bb.asFloatBuffer()
        vertexBuffer?.put(vertices)
        vertexBuffer?.position(0)

        mProgram = GLUtils.loadProgram(vs, fs);
    }

    fun draw(mvpMatrix: FloatArray) {
        GLES20.glUseProgram(mProgram)
        mPositionHandler = GLES20.glGetAttribLocation(mProgram, "vPosition")
        GLES20.glEnableVertexAttribArray(mPositionHandler)
        GLES20.glVertexAttribPointer(mPositionHandler, 3, GLES20.GL_FLOAT, false, 3 * 4, vertexBuffer)
//        // get handle to fragment shader's vColor member
//        mColorHandle = GLES20.glGetUniformLocation(mProgram, "vColor")
//
//        // Set color for drawing the triangle
//        GLES20.glUniform4fv(mColorHandle, 1, colors, 0)

        // get handle to shape's transformation matrix
        mMVPMatrixHandle = GLES20.glGetUniformLocation(mProgram, "uMVPMatrix")
        // Pass the projection and view transformation to the shader
        GLES20.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, mvpMatrix,0)

        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, vertices.size / 3)
        GLES20.glDisableVertexAttribArray(mPositionHandler)
    }
}