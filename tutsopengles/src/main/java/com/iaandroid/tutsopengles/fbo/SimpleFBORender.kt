package com.iaandroid.tutsopengles.fbo

import android.graphics.Bitmap
import android.opengl.GLES20
import android.opengl.Matrix
import com.dastanapps.mediasdk.opengles.gpu.OpenGlUtils
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer


/**
 * Reference : http://opengles2learning.blogspot.com/2014/02/render-to-texture-rtt.html?m=1
 */
class SimpleFBORender {
    protected var mVertexShader = DEFAULT_VERTEX_SHADER
    protected var mFragmentShader = DEFAULT_FRAGMENT_SHADER

    protected lateinit var mWorldVertices: FloatBuffer
    protected lateinit var mTextureVertices: FloatBuffer

    @JvmField
    protected var mProgramHandle: Int = 0
    protected var mVertexShaderHandle: Int = 0
    protected var mFragmentShaderHandle: Int = 0
    protected var mTextureHandle: Int = 0
    protected var mPositionHandle: Int = 0
    protected var mTextureCoordHandle: Int = 0

    protected var mTextureIn: Int = 0

    init {
        val vertices = floatArrayOf(-0.5f, -0.5f, 0.5f, -0.5f, -0.5f, 0.5f, 0.5f, 0.5f)
        mWorldVertices = ByteBuffer.allocateDirect(vertices.size * 4)
                .order(ByteOrder.nativeOrder()).asFloatBuffer()
        mWorldVertices.put(vertices).position(0)

        val texData0 = floatArrayOf(0.0f, 0.0f, 1.0f, 0.0f, 0.0f, 1.0f, 1.0f, 1.0f)
        mTextureVertices = ByteBuffer.allocateDirect(texData0.size * 4)
                .order(ByteOrder.nativeOrder()).asFloatBuffer()
        mTextureVertices.put(texData0)?.position(0)
    }

    fun initHandlers(bitmap: Bitmap) {
        mProgramHandle = com.raywenderlich.GLUtils.loadProgram(mVertexShader, mFragmentShader)
        mPositionHandle = GLES20.glGetAttribLocation(mProgramHandle, ATTRIBUTE_POSITION)
        mTextureCoordHandle = GLES20.glGetAttribLocation(mProgramHandle, ATTRIBUTE_TEXTURE_COORD)
        mTextureHandle = GLES20.glGetUniformLocation(mProgramHandle, UNIFORM_TEXTURE_0)

        mTextureIn = OpenGlUtils.loadTexture(bitmap, mTextureIn, true)
    }

    private val mRenderToTextureFBO = IntArray(1)
    val mFBOTextureR = IntArray(1)
    val mRenderBufferFBO = IntArray(1)

    fun initFBO() {
        GLES20.glGenTextures(1, mFBOTextureR, 0)
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mFBOTextureR[0])

        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_REPEAT)
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_REPEAT)
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR)
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST)

        GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_RGBA, 256, 256, 0, GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, null)

        GLES20.glGenFramebuffers(1, mRenderToTextureFBO, 0)
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, mRenderToTextureFBO[0])
        GLES20.glFramebufferTexture2D(GLES20.GL_FRAMEBUFFER, GLES20.GL_COLOR_ATTACHMENT0, GLES20.GL_TEXTURE_2D, mFBOTextureR[0], 0)

        GLES20.glGenRenderbuffers(1, mRenderBufferFBO, 0)
        GLES20.glBindRenderbuffer(GLES20.GL_RENDERBUFFER, mRenderBufferFBO[0])
        GLES20.glRenderbufferStorage(GLES20.GL_RENDERBUFFER, GLES20.GL_DEPTH_COMPONENT16, 256, 256)
        GLES20.glFramebufferRenderbuffer(GLES20.GL_FRAMEBUFFER, GLES20.GL_DEPTH_ATTACHMENT, GLES20.GL_RENDERBUFFER, mRenderBufferFBO[0])

        /** Check FBO is complete and OK */
        val iResult = GLES20.glCheckFramebufferStatus(GLES20.GL_FRAMEBUFFER)
        if (iResult != GLES20.GL_FRAMEBUFFER_COMPLETE) {
            throw RuntimeException("Error: Frame Buffer Status is not complete. Terminated.")
        }

        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0)
        GLES20.glBindRenderbuffer(GLES20.GL_RENDERBUFFER, 0)
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0)
    }

    private var mWidth: Int = 0
    private var mHeight: Int = 0

    fun setRenderSize(width: Int, height: Int) {
        mWidth = width
        mHeight = height
        GLES20.glViewport(0, 0, width, height)
    }

    fun onDrawFrame() {
        prepareFBOTexture()
        /**=========================================**/
        useFBOTexture()
    }

    fun useFBOTexture() {
        /** Use blue background color */
        GLES20.glClearColor(0.0f, 0.2f, 0.3f, 1.0f)
        GLES20.glClear(GLES20.GL_DEPTH_BUFFER_BIT or GLES20.GL_COLOR_BUFFER_BIT)

        GLES20.glViewport(0, 0, mWidth, mHeight)
        GLES20.glUseProgram(mProgramHandle)

        mWorldVertices.position(0)
        GLES20.glVertexAttribPointer(mPositionHandle, 2, GLES20.GL_FLOAT, false, 8, mWorldVertices)
        GLES20.glEnableVertexAttribArray(mPositionHandle)

        mTextureVertices.position(0)
        GLES20.glVertexAttribPointer(mTextureCoordHandle, 2, GLES20.GL_FLOAT, false, 8, mTextureVertices)
        GLES20.glEnableVertexAttribArray(mTextureCoordHandle)

        GLES20.glActiveTexture(GLES20.GL_TEXTURE0)
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mFBOTextureR[0])
        GLES20.glUniform1i(mTextureHandle, 0)

        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4)

        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0)
    }

    fun prepareFBOTexture() {
        /** Enable frame buffer to start rendering into it */
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, mRenderToTextureFBO[0])

        /** Create an Orange back ground */
        GLES20.glClearColor(1.0f, 0.5f, 0.0f, 1.0f)
        GLES20.glClear(GLES20.GL_DEPTH_BUFFER_BIT or GLES20.GL_COLOR_BUFFER_BIT)

        GLES20.glViewport(0, 0, 256, 256)
        GLES20.glUseProgram(mProgramHandle)

        mWorldVertices.position(0)
        GLES20.glVertexAttribPointer(mPositionHandle, 2, GLES20.GL_FLOAT, false, 8, mWorldVertices)
        GLES20.glEnableVertexAttribArray(mPositionHandle)

        mTextureVertices.position(0)
        GLES20.glVertexAttribPointer(mTextureCoordHandle, 2, GLES20.GL_FLOAT, false, 8, mTextureVertices)
        GLES20.glEnableVertexAttribArray(mTextureCoordHandle)

        /** set the Texture unit 0 active and bind the texture to it */
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0)
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mTextureIn)
        GLES20.glUniform1i(mTextureHandle, 0)

        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4)

        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0)
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);
    }

    companion object {
        val ATTRIBUTE_POSITION = "position"
        val ATTRIBUTE_TEXTURE_COORD = "inputTextureCoordinate"
        val VARYING_TEXTURE_COORD = "textureCoordinate"
        val UNIFORM_TEXTURE = "inputImageTexture"
        val UNIFORM_TEXTURE_0 = UNIFORM_TEXTURE

        @JvmField
        var DEFAULT_VERTEX_SHADER = ("attribute vec4 " + ATTRIBUTE_POSITION + ";\n"
                + "attribute vec2 " + ATTRIBUTE_TEXTURE_COORD + ";\n"
                + "varying vec2 " + VARYING_TEXTURE_COORD + ";\n"
                + "void main() {\n"
                + "  " + VARYING_TEXTURE_COORD + " = " + ATTRIBUTE_TEXTURE_COORD + ";\n"
                + "   gl_Position = " + ATTRIBUTE_POSITION + ";\n"
                + "}\n")

        var DEFAULT_FRAGMENT_SHADER = ("precision mediump float;\n"
                + "uniform sampler2D " + UNIFORM_TEXTURE_0 + ";\n"
                + "varying vec2 " + VARYING_TEXTURE_COORD + ";\n"
                + "void main(){\n"
                + "   gl_FragColor = texture2D(" + UNIFORM_TEXTURE_0 + "," + VARYING_TEXTURE_COORD + ")" + ";\n"
                + "}\n")
    }
}