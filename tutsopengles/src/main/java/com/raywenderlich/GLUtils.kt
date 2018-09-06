package com.raywenderlich

import android.content.Context
import android.graphics.BitmapFactory
import android.opengl.GLES20
import android.opengl.GLUtils
import android.util.Log
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader


/**
 * Created by dastaniqbal on 11/02/2018.
 * dastanIqbal@marvelmedia.com
 * 11/02/2018 1:46
 */


object GLUtils {
    fun loadProgram(strVSource: String, strFSource: String): Int {
        val iVShader: Int = loadShader(strVSource, GLES20.GL_VERTEX_SHADER)
        val iFShader: Int = loadShader(strFSource, GLES20.GL_FRAGMENT_SHADER)
        val iProgId: Int = GLES20.glCreateProgram()
        val link = IntArray(1)
        if (iVShader == 0) {
            Log.d("Load Program", "Vertex Shader Failed")
            return 0
        }
        if (iFShader == 0) {
            Log.d("Load Program", "Fragment Shader Failed")
            return 0
        }

        GLES20.glAttachShader(iProgId, iVShader)
        GLES20.glAttachShader(iProgId, iFShader)
        GLES20.glLinkProgram(iProgId)
        GLES20.glGetProgramiv(iProgId, GLES20.GL_LINK_STATUS, link, 0)
        if (link[0] <= 0) {
            Log.d("Load Program", "Linking Failed")
            return 0
        }
        GLES20.glDeleteShader(iVShader)
        GLES20.glDeleteShader(iFShader)
        return iProgId
    }

    fun loadProgramAndLink(strVSource: String, strFSource: String, attributes: Array<String>?): Int {
        val iVShader: Int = loadShader(strVSource, GLES20.GL_VERTEX_SHADER)
        val iFShader: Int = loadShader(strFSource, GLES20.GL_FRAGMENT_SHADER)
        val iProgId: Int = GLES20.glCreateProgram()
        val link = IntArray(1)
        if (iVShader == 0) {
            Log.d("Load Program", "Vertex Shader Failed")
            return 0
        }
        if (iFShader == 0) {
            Log.d("Load Program", "Fragment Shader Failed")
            return 0
        }

        GLES20.glAttachShader(iProgId, iVShader)
        GLES20.glAttachShader(iProgId, iFShader)
        if (attributes != null && attributes.isNotEmpty()) {
            attributes.forEachIndexed { index, s ->
                GLES20.glBindAttribLocation(iProgId, index, s)
            }
        }
        GLES20.glLinkProgram(iProgId)
        GLES20.glGetProgramiv(iProgId, GLES20.GL_LINK_STATUS, link, 0)
        if (link[0] <= 0) {
            Log.d("Load Program", "Linking Failed")
            return 0
        }
        GLES20.glDeleteShader(iVShader)
        GLES20.glDeleteShader(iFShader)
        return iProgId
    }

    fun loadShader(strSource: String, iType: Int): Int {
        val compiled = IntArray(1)
        val iShader = GLES20.glCreateShader(iType)
        GLES20.glShaderSource(iShader, strSource)
        GLES20.glCompileShader(iShader)
        GLES20.glGetShaderiv(iShader, GLES20.GL_COMPILE_STATUS, compiled, 0)
        if (compiled[0] == 0) {
            Log.e("Load Shader Failed", "Compilation\n $strSource" + GLES20.glGetShaderInfoLog(iShader))
            return 0
        }
        return iShader
    }

    /**
     * Checks to see if a GLES error has been raised.
     */
    fun checkGlError(op: String) {
        val error = GLES20.glGetError()
        if (error != GLES20.GL_NO_ERROR) {
            val msg = op + ": glError 0x" + Integer.toHexString(error) + " " + EGLLogWrapper.getErrorString(error)
            Log.e("OpenGlUtils", msg)
            throw RuntimeException(msg)
        }
    }


    fun readShader(context: Context, glslId: Int): String {
        val stream = context.resources.openRawResource(glslId)
        var br: BufferedReader? = null
        val sb = StringBuilder()

        try {

            br = BufferedReader(InputStreamReader(stream))
            var line = br.readLine()
            while (line != null) {
                sb.append(line)
                line = br.readLine()
            }

        } catch (e: IOException) {
            e.printStackTrace()
        } finally {
            if (br != null) {
                try {
                    br.close()
                } catch (e: IOException) {
                    e.printStackTrace()
                }

            }
        }

        return sb.toString()
    }


    fun readTextFileFromRawResource(context: Context, resourceId: Int): String? {
        val inputStream = context.resources.openRawResource(resourceId)
        val inputStreamReader = InputStreamReader(inputStream)
        val bufferedReader = BufferedReader(inputStreamReader)

        var nextLine: String?
        val body = StringBuilder()

        try {
            nextLine=bufferedReader.readLine()
            while (nextLine != null) {
                body.append(nextLine)
                body.append('\n')
                nextLine=bufferedReader.readLine()
            }
        } catch (e: IOException) {
            return null
        }

        return body.toString()
    }

    fun loadTexture(context: Context, resourceId: Int): Int {
        val textureHandle = IntArray(1)

        GLES20.glGenTextures(1, textureHandle, 0)

        if (textureHandle[0] == 0) {
            throw RuntimeException("Error generating texture name.")
        }

        val options = BitmapFactory.Options()
        options.inScaled = false    // No pre-scaling

        // Read in the resource
        val bitmap = BitmapFactory.decodeResource(context.resources, resourceId, options)

        // Bind to the texture in OpenGL
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureHandle[0])

        // Set filtering
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST)
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_NEAREST)

        // Load the bitmap into the bound texture.
        GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0)

        // Recycle the bitmap, since its data has been loaded into OpenGL.
        bitmap.recycle()

        return textureHandle[0]
    }

}