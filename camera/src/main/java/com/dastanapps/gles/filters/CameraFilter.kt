package com.dastanapps.gles.filters

import android.graphics.SurfaceTexture
import android.opengl.GLES20
import android.renderscript.Matrix4f
import com.dastanapps.gles.GLUtils
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import java.nio.ShortBuffer

/**
 * Created by dastaniqbal on 23/01/2018.
 * dastanIqbal@marvelmedia.com
 * 23/01/2018 6:01
 */
abstract class CameraFilter {
    private var positionBuffer: FloatBuffer? = null
    private var texturePositionBuffer: FloatBuffer? = null
    private var drawOrderBuffer: ShortBuffer? = null

    private var program = 0
    private var positionHandle: Int = 0
    private var texturePositionHandle: Int = 0
    private var camTexMatrixHandle: Int = 0
    private var mvpMatrixHandle: Int = 0


    private val cameraTextureMatrix = Matrix4f()
    private val mvpMatrix = Matrix4f()

    //We are drawing two triangles for the texture
    val vertexOrder = shortArrayOf(0, 1, 2, 1, 3, 2)
    val vertexCoordinates = floatArrayOf(-1f, +1f, +1f, +1f, -1f, -1f, +1f, -1f)

    //Tex coordinates are flipped vertically
    val vertexTextureCoordinates = floatArrayOf(0.0f, 1.0f, 1.0f, 1.0f, 0.0f, 0.0f, 1.0f, 0.0f)
    private val sSimpleFS = "#extension GL_OES_EGL_image_external : require\n" +
            "\n" +
            "precision mediump float;\n" +
            "\n" +
            "uniform samplerExternalOES camTex;\n" +
            "varying vec2 camTexCoordinate;\n" +
            "\n" +
            "void main () {\n" +
            "    vec4 color = texture2D(camTex, camTexCoordinate);\n" +
            "    gl_FragColor = color;\n" +
            "}"

    init {
        var bb: ByteBuffer = ByteBuffer.allocateDirect(vertexOrder.size * 2)

        if (drawOrderBuffer == null) {
            // Draw list buffer
            //2 bytes short
            bb.order(ByteOrder.nativeOrder())
            drawOrderBuffer = bb.asShortBuffer()
            drawOrderBuffer?.put(vertexOrder)
            drawOrderBuffer?.position(0)
        }

        if (positionBuffer == null) {
            // Initialize the texture holder
            bb = ByteBuffer.allocateDirect(vertexCoordinates.size * 4) //4 bytes/float
            bb.order(ByteOrder.nativeOrder())
            positionBuffer = bb.asFloatBuffer()
            positionBuffer?.put(vertexCoordinates)
            positionBuffer?.position(0)
        }

        if (texturePositionBuffer == null) {
            bb = ByteBuffer.allocateDirect(vertexTextureCoordinates.size * 4) //4 bytes/float
            bb.order(ByteOrder.nativeOrder())
            texturePositionBuffer = bb.asFloatBuffer()
            texturePositionBuffer?.put(vertexTextureCoordinates)
            texturePositionBuffer?.position(0)
        }

        if (program == 0) {
            program = GLUtils.buildProgram(sSimpleFS)
            if (program == 0) throw IllegalStateException("Failed to create program")
        }
    }

    fun draw(surfaceWidth: Int, surfaceHeight: Int, eglSurfaceTexture: SurfaceTexture) {
        GLES20.glUseProgram(program)
        camTexMatrixHandle = GLES20.glGetUniformLocation(program, "camTexMatrix")
        mvpMatrixHandle = GLES20.glGetUniformLocation(program, "mvpMatrix")
        positionHandle = GLES20.glGetAttribLocation(program, "position")
        texturePositionHandle = GLES20.glGetAttribLocation(program, "texturePosition")
        GLUtils.checkGLError("getLocations")

        onDraw(surfaceHeight, surfaceHeight, eglSurfaceTexture)
    }

    abstract fun onDraw(surfaceWidth: Int, surfaceHeight: Int, eglSurfaceTexture: SurfaceTexture)

    fun setupShaders(program: Int, surfaceWidth: Int, surfaceHeight: Int, eglSurfaceTexture: SurfaceTexture) {
        GLES20.glUseProgram(program)

        //Make the texture available to the shader
        GLES20.glViewport(0, 0, surfaceWidth, surfaceHeight)
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 0.0f)
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT)

        //Update texture
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0)
        eglSurfaceTexture.updateTexImage()

        //Update transform matrix
        eglSurfaceTexture.getTransformMatrix(cameraTextureMatrix.array)
        GLES20.glUniformMatrix4fv(camTexMatrixHandle, 1, false, cameraTextureMatrix.array, 0)

        //Send position
        GLES20.glEnableVertexAttribArray(positionHandle)
        GLES20.glVertexAttribPointer(positionHandle, 2, GLES20.GL_FLOAT, false, 4 * 2, positionBuffer)

        //Send texture positions
        GLES20.glEnableVertexAttribArray(texturePositionHandle)
        GLES20.glVertexAttribPointer(texturePositionHandle, 2, GLES20.GL_FLOAT, false, 4 * 2, texturePositionBuffer)
//
//      //Send Mvp Matrix
        GLES20.glUniformMatrix4fv(mvpMatrixHandle, 1, false, mvpMatrix.array, 0)
        //And draw
        GLES20.glDrawElements(GLES20.GL_TRIANGLES, drawOrderBuffer?.remaining()!!, GLES20.GL_UNSIGNED_SHORT, drawOrderBuffer)
    }

    fun release() {
        GLES20.glDeleteProgram(program)
    }
}