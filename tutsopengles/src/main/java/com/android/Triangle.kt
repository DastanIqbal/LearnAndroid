package com.android

import android.opengl.GLES20
import com.raywenderlich.GLUtils
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer

/**
 * Created by dastaniqbal on 12/02/2018.
 * dastanIqbal@marvelmedia.com
 * 12/02/2018 9:58
 */
class Triangle {
    val vertices = floatArrayOf(
            -1f, -1f, 1f,
            1f, -1f, 1f,
            0f, 0f, 1f
    )
    val colors = floatArrayOf(1f, 1f, 1f, 1f)
    val vs = ("" +
            "attribute vec4 vPosition;\n" +
            "void main(){\n" +
            "      gl_Position=vPosition;\n" +
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

    init {
        val bb = ByteBuffer.allocateDirect(vertices.size * 4)
        bb.order(ByteOrder.nativeOrder())

        vertexBuffer = bb.asFloatBuffer()
        vertexBuffer?.put(vertices)
        vertexBuffer?.position(0)

        mProgram = GLUtils.loadProgram(vs, fs);
    }

    fun draw() {
        GLES20.glUseProgram(mProgram)
        mPositionHandler = GLES20.glGetAttribLocation(mProgram, "vPosition")
        GLES20.glEnableVertexAttribArray(mPositionHandler)
        GLES20.glVertexAttribPointer(mPositionHandler, 3, GLES20.GL_FLOAT, false, 3 * 4, vertexBuffer)
//        // get handle to fragment shader's vColor member
//        mColorHandle = GLES20.glGetUniformLocation(mProgram, "vColor")
//
//        // Set color for drawing the triangle
//        GLES20.glUniform4fv(mColorHandle, 1, colors, 0)

        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, vertices.size / 3)
        GLES20.glDisableVertexAttribArray(mPositionHandler)
    }
}