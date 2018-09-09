package com.dastanapps.mediasdk.opengles.gpu.filter

import android.opengl.GLES20
import com.dastanapps.mediasdk.opengles.gpu.OpenGlUtils
import java.nio.FloatBuffer
import java.util.*

/**
 * Created by dastaniqbal on 06/09/2018.
 * dastanIqbal@marvelmedia.com
 * 06/09/2018 11:38
 */
open class ImageFilter(var vs: String, var fs: String) {
    companion object {
        var defalutVS = ("" +
                "attribute vec4 position;\n" +
                "attribute vec4 inputTextureCoordinate;\n" +
                " \n" +
                "varying vec2 textureCoordinate;\n" +
                " \n" +
                "void main()\n" +
                "{\n" +
                "    gl_Position = position;\n" +
                "    textureCoordinate = inputTextureCoordinate.xy;\n" +
                "}")
        var defalutFS = ("" +
                "varying highp vec2 textureCoordinate;\n" +
                " \n" +
                "uniform sampler2D inputImageTexture;\n" +
                " \n" +
                "void main()\n" +
                "{\n" +
                "     gl_FragColor = texture2D(inputImageTexture, textureCoordinate);\n" +
                "}")
    }

    var progId = -1
    var attribPos = -1
    var uniformTexture = -1
    var attribTextureCoord = -1
    private var runOnDraw: LinkedList<Runnable> = LinkedList()

    fun init() {
        onInit()
    }

    protected open fun onInit() {
        progId = OpenGlUtils.loadProgram(vs, fs)

        attribPos = GLES20.glGetAttribLocation(progId, "position")
        attribTextureCoord = GLES20.glGetAttribLocation(progId, "inputTextureCoordinate")

        uniformTexture = GLES20.glGetUniformLocation(progId, "inputImageTexture")
    }


    fun onDraw(textId: Int, squareBuffer: FloatBuffer, textBuffer: FloatBuffer) {
        GLES20.glUseProgram(progId)

        squareBuffer.position(0)
        GLES20.glVertexAttribPointer(attribPos, 2, GLES20.GL_FLOAT, false, 0, squareBuffer)
        GLES20.glEnableVertexAttribArray(attribPos)

        textBuffer.position(0)
        GLES20.glVertexAttribPointer(attribTextureCoord, 2, GLES20.GL_FLOAT, false, 0, textBuffer)
        GLES20.glEnableVertexAttribArray(attribTextureCoord)

        if (textId != OpenGlUtils.NO_TEXTURE) {
            GLES20.glActiveTexture(GLES20.GL_TEXTURE0)
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textId)
            GLES20.glUniform1i(uniformTexture, 0)
        }

        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4)

        GLES20.glDisableVertexAttribArray(attribPos)
        GLES20.glDisableVertexAttribArray(attribTextureCoord)
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0)
        OpenGlUtils.checkGlError()
    }

    protected fun runOnDraw(runnable: Runnable) {
        synchronized(runOnDraw) {
            runOnDraw.addLast(runnable)
        }
    }

    protected fun setFloat(location: Int, floatValue: Float) {
        runOnDraw(Runnable { GLES20.glUniform1f(location, floatValue) })
    }

    fun destroy() {
        GLES20.glDeleteProgram(progId)
    }
}