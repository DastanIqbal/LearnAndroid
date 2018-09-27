package com.dastanapps.gles

import android.content.Context
import android.graphics.BitmapFactory
import android.graphics.SurfaceTexture
import android.opengl.GLES11Ext
import android.opengl.GLES20
import android.opengl.GLUtils
import android.opengl.Matrix
import android.renderscript.Matrix4f
import android.util.Log
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import java.nio.ShortBuffer
import javax.microedition.khronos.egl.EGL10

/**
 * Created by dastaniqbal on 23/01/2018.

 * 23/01/2018 3:43
 */
class GLUtils {
    private val TAG = "DEBUG:" + this::class.java!!.simpleName
    private val verxtexShader = "\n" +
            "//The matrix the camera internally applies to the output it produces\n" +
            "uniform mat4 camTexMatrix;\n" +
            "//MVP matrix for the quad we are drawing\n" +
            "uniform mat4 mvpMatrix;\n" +
            "\n" +
            "attribute vec4 position;\n" +
            "attribute vec4 texturePosition;\n" +
            "\n" +
            "varying vec2 camTexCoordinate;\n" +
            "\n" +
            "void main() {\n" +
            "    camTexCoordinate = (camTexMatrix * texturePosition).xy;\n" +
            "    gl_Position = mvpMatrix * position;\n" +
            "    //gl_Position = mvpMatrix * position;\n" +
            "}"

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
            program = buildProgram(sSimpleFS)
            if (program == 0) throw IllegalStateException("Failed to create program")
        }
    }

    fun draw(texId: Int) {
        GLES20.glUseProgram(program)
        camTexMatrixHandle = GLES20.glGetUniformLocation(program, "camTexMatrix")
        mvpMatrixHandle = GLES20.glGetUniformLocation(program, "mvpMatrix")
        positionHandle = GLES20.glGetAttribLocation(program, "position")
        texturePositionHandle = GLES20.glGetAttribLocation(program, "texturePosition")
        checkGLError("getLocations")

        Matrix.setIdentityM(mvpMatrix.array, 0)
        GLES20.glUniformMatrix4fv(camTexMatrixHandle, 1, false, cameraTextureMatrix.array, 0)

        GLES20.glEnableVertexAttribArray(positionHandle)
        GLES20.glVertexAttribPointer(positionHandle, 2, GLES20.GL_FLOAT, false, 4 * 2, positionBuffer)

        //Send texture positions
        GLES20.glEnableVertexAttribArray(texturePositionHandle)
        GLES20.glVertexAttribPointer(texturePositionHandle, 2, GLES20.GL_FLOAT, false, 4 * 2, texturePositionBuffer)

//      //Send Mvp Matrix
        GLES20.glUniformMatrix4fv(mvpMatrixHandle, 1, false, mvpMatrix.array, 0)

        setupShaders(program, 0, 0, null, texId)
    }

    fun draw(surfaceWidth: Int, surfaceHeight: Int, eglSurfaceTexture: SurfaceTexture, texId: Int) {
        GLES20.glUseProgram(program)
        camTexMatrixHandle = GLES20.glGetUniformLocation(program, "camTexMatrix")
        mvpMatrixHandle = GLES20.glGetUniformLocation(program, "mvpMatrix")
        positionHandle = GLES20.glGetAttribLocation(program, "position")
        texturePositionHandle = GLES20.glGetAttribLocation(program, "texturePosition")
        checkGLError("getLocations")
    }

    fun setupShaders(program: Int, surfaceWidth: Int, surfaceHeight: Int, eglSurfaceTexture: SurfaceTexture?, textId: Int) {
        GLES20.glUseProgram(program)

        //Make the texture available to the shader
        if (surfaceWidth > 0 && surfaceHeight > 0)
            GLES20.glViewport(0, 0, surfaceWidth, surfaceHeight)
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 0.0f)
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT)

        if (eglSurfaceTexture != null) {
            eglSurfaceTexture.updateTexImage()

            //Update transform matrix
            eglSurfaceTexture.getTransformMatrix(cameraTextureMatrix.array)
        }

        //Update texture
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0)
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, textId)
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

    fun buildProgram(fragmentShader: String): Int {
        return buildProgram(verxtexShader, fragmentShader)
    }

    fun buildProgram(vertex: String, fragment: String): Int {
        val vertexShader = buildShader(vertex, GLES20.GL_VERTEX_SHADER)
        if (vertexShader == 0) return 0

        val fragmentShader = buildShader(fragment, GLES20.GL_FRAGMENT_SHADER)
        if (fragmentShader == 0) return 0

        val program = GLES20.glCreateProgram()
        GLES20.glAttachShader(program, vertexShader)
        checkGlError()

        GLES20.glAttachShader(program, fragmentShader)
        checkGlError()

        GLES20.glLinkProgram(program)
        checkGlError()

        val status = IntArray(1)
        GLES20.glGetProgramiv(program, GLES20.GL_LINK_STATUS, status, 0)
        if (status[0] != GLES20.GL_TRUE) {
            val error = GLES20.glGetProgramInfoLog(program)
            Log.d(TAG, "Error while linking program:\n" + error)
            GLES20.glDeleteShader(vertexShader)
            GLES20.glDeleteShader(fragmentShader)
            GLES20.glDeleteProgram(program)
            return 0
        }

        return program
    }

    private fun buildShader(source: String, type: Int): Int {
        val shader = GLES20.glCreateShader(type)

        GLES20.glShaderSource(shader, source)
        checkGlError()

        GLES20.glCompileShader(shader)
        checkGlError()

        val status = IntArray(1)
        GLES20.glGetShaderiv(shader, GLES20.GL_COMPILE_STATUS, status, 0)
        if (status[0] != GLES20.GL_TRUE) {
            val error = GLES20.glGetShaderInfoLog(shader)
            Log.d(TAG, "Error while compiling shader:\n" + error)
            GLES20.glDeleteShader(shader)
            return 0
        }

        return shader
    }

    companion object {
        private val TAG = "DEBUG:" + this::class.java!!.simpleName
        fun initTexture(): Int {
            val tex = IntArray(1)
            GLES20.glGenTextures(1, tex, 0)
            GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, tex[0])

            GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE)
            GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE)

            GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST)
            GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_NEAREST)
            return tex[0]
        }

        fun loadTexture(context: Context, resource: Int): Int {
            val textures = IntArray(1)

            GLES20.glActiveTexture(GLES20.GL_TEXTURE0)
            GLES20.glGenTextures(1, textures, 0)
            checkGlError()

            val texture = textures[0]
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, texture)
            checkGlError()

            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR)
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR)

            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE)
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE)

            val bitmap = BitmapFactory.decodeResource(context.resources, resource)

            GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_RGBA, bitmap, GLES20.GL_UNSIGNED_BYTE, 0)
            checkGlError()

            bitmap.recycle()

            return texture
        }


        fun deleteTextures(texture: Int) {
            val tex = intArrayOf(texture)
            GLES20.glDeleteTextures(1, tex, 0)
        }

        fun checkGlError() {
            val error = GLES20.glGetError()
            if (error != GLES20.GL_NO_ERROR) {
                Log.e(TAG, "GL error = 0x" + Integer.toHexString(error))
                throw RuntimeException("GL ERROR")
            }
        }


        fun checkEglError(error: Int) {
            if (error != EGL10.EGL_SUCCESS) {
                Log.e(TAG, "EGL error = 0x" + Integer.toHexString(error))
            }
        }

        fun checkGLError(op: String) {
            val error = GLES20.glGetError()
            if (error != GLES20.GL_NO_ERROR) {
                val msg = op + ": glError 0x" + Integer.toHexString(error)
                throw RuntimeException(msg)
            }
        }
    }
}