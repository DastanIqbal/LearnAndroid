package com.dastanapps.gles

import android.content.Context
import android.graphics.BitmapFactory
import android.opengl.GLES20
import android.opengl.GLUtils
import android.util.Log
import javax.microedition.khronos.egl.EGL10

/**
 * Created by dastaniqbal on 23/01/2018.
 * dastanIqbal@marvelmedia.com
 * 23/01/2018 3:43
 */
object GLUtils {
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