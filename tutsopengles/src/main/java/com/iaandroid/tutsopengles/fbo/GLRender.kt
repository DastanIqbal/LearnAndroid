package com.iaandroid.tutsopengles.fbo

import android.graphics.Bitmap
import android.graphics.Canvas
import android.opengl.GLES20
import com.dastanapps.mediasdk.opengles.gpu.OpenGlUtils

import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer

open class GLRender {

    protected var mVertexShader = DEFAULT_VERTEX_SHADER
    protected var mFragmentShader = DEFAULT_FRAGMENT_SHADER

    protected lateinit var mWorldVertices: FloatBuffer
    protected lateinit var mTextureVertices: Array<FloatBuffer?>

    protected var mProgramHandle: Int = 0
    protected var mVertexShaderHandle: Int = 0
    protected var mFragmentShaderHandle: Int = 0
    protected var mTextureHandle: Int = 0
    protected var mPositionHandle: Int = 0
    protected var mTextureCoordHandle: Int = 0

    protected var mTextureIn: Int = 0

    private var mInitialized: Boolean = false
    protected var mSizeChanged: Boolean = false

    protected var mWidth: Int = 0
        set(value) {
            field = value
            mSizeChanged = true
        }
    protected var mHeight: Int = 0
        set(value) {
            field = value
            mSizeChanged = true
        }


    protected var mFps: Int = 0
    private var mLastTime: Long = 0
    private var mFrameCount: Int = 0

    init {
        initWorldVertices()
        initTextureVertices()
    }

    fun setRenderSize(width: Int, height: Int) {
        this.mWidth = width
        this.mHeight = height
        mSizeChanged = true
    }

    protected fun initWorldVertices() {
        // (-1, 1) -------> (1,1)
        //      ^
        //       \\
        //         (0,0)
        //           \\
        //             \\
        // (-1,-1) -------> (1,-1)
        val vertices = floatArrayOf(-1f, -1f, 1f, -1f, -1f, 1f, 1f, 1f)
        mWorldVertices = ByteBuffer.allocateDirect(vertices.size * 4)
                .order(ByteOrder.nativeOrder()).asFloatBuffer()
        mWorldVertices.put(vertices).position(0)
    }

    /**
     * 初始化纹理坐标系顶点，默认为填充模式
     */
    protected fun initTextureVertices() {
        mTextureVertices = arrayOfNulls(4)

        // (0,1) -------> (1,1)
        //     ^
        //      \\
        //        \\
        //          \\
        //            \\
        // (0,0) -------> (1,0)
        // 正向纹理坐标
        var texData0 = floatArrayOf(0.0f, 0.0f, 1.0f, 0.0f, 0.0f, 1.0f, 1.0f, 1.0f)
        //texData0 = floatArrayOf(0.0f, 1.0f, 1.0f, 1.0f, 0.0f, 0.0f, 1.0f, 0.0f)
        mTextureVertices[0] = ByteBuffer.allocateDirect(texData0.size * 4)
                .order(ByteOrder.nativeOrder()).asFloatBuffer()
        mTextureVertices[0]?.put(texData0)?.position(0)

        // 顺时针旋转90°的纹理坐标
        val texData1 = floatArrayOf(1.0f, 0.0f, 1.0f, 1.0f, 0.0f, 0.0f, 0.0f, 1.0f)
        mTextureVertices[1] = ByteBuffer.allocateDirect(texData1.size * 4)
                .order(ByteOrder.nativeOrder()).asFloatBuffer()
        mTextureVertices[1]?.put(texData1)?.position(0)

        // 顺时针旋转180°的纹理坐标
        val texData2 = floatArrayOf(1.0f, 1.0f, 0.0f, 1.0f, 1.0f, 0.0f, 0.0f, 0.0f)
        mTextureVertices[2] = ByteBuffer.allocateDirect(texData2.size * 4)
                .order(ByteOrder.nativeOrder()).asFloatBuffer()
        mTextureVertices[2]?.put(texData2)?.position(0)

        // 顺时针旋转270°的纹理坐标
        val texData3 = floatArrayOf(0.0f, 1.0f, 0.0f, 0.0f, 1.0f, 1.0f, 1.0f, 0.0f)
        mTextureVertices[3] = ByteBuffer.allocateDirect(texData3.size * 4)
                .order(ByteOrder.nativeOrder()).asFloatBuffer()
        mTextureVertices[3]?.put(texData3)?.position(0)
    }

    /**
     * 初始化OpenGL上下文
     */
    protected fun initGLContext() {
        mProgramHandle = com.raywenderlich.GLUtils.loadProgram(mVertexShader, mFragmentShader)
        initShaderHandles()
    }


    /**
     * 初始化参数句柄
     */
    protected fun initShaderHandles() {
        mPositionHandle = GLES20.glGetAttribLocation(mProgramHandle, ATTRIBUTE_POSITION)
        mTextureCoordHandle = GLES20.glGetAttribLocation(mProgramHandle, ATTRIBUTE_TEXTURE_COORD)

        mTextureHandle = GLES20.glGetUniformLocation(mProgramHandle, UNIFORM_TEXTURE_0)
    }

    /**
     * 绑定顶点、纹理等
     */
    protected fun bindShaderValues() {
        bindShaderVertices()
        bindShaderTextures()
    }

    /**
     * 绑定顶点
     */
    protected fun bindShaderVertices() {
        mWorldVertices.position(0)
        GLES20.glVertexAttribPointer(mPositionHandle, 2, GLES20.GL_FLOAT, false,
                8, mWorldVertices)
        GLES20.glEnableVertexAttribArray(mPositionHandle)

        mTextureVertices[0]?.position(0)
        GLES20.glVertexAttribPointer(mTextureCoordHandle, 2, GLES20.GL_FLOAT, false,
                8, mTextureVertices[0])
        GLES20.glEnableVertexAttribArray(mTextureCoordHandle)
    }

    /**
     * 绑定纹理
     */
    protected fun bindShaderTextures() {
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0)
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mTextureIn)
        GLES20.glUniform1i(mTextureHandle, 0)
    }

    fun setTexture(bitmap: Bitmap, recycle: Boolean) {
        var resizedBitmap: Bitmap? = null
        if (bitmap.width % 2 == 1) {
            resizedBitmap = Bitmap.createBitmap(bitmap.width + 1, bitmap.height,
                    Bitmap.Config.ARGB_8888)
            val can = Canvas(resizedBitmap!!)
            can.drawARGB(0x00, 0x00, 0x00, 0x00)
            can.drawBitmap(bitmap, 0f, 0f, null)
        }

        mTextureIn = OpenGlUtils.loadTexture(resizedBitmap ?: bitmap, mTextureIn, recycle)
    }

    /**
     * 必须在GL线程执行
     */
    fun onDrawFrame() {
        if (!mInitialized) {
            initGLContext()
            mInitialized = true
        }
        if (mSizeChanged) {
            onRenderSizeChanged()
        }
        drawFrame()

        mSizeChanged = false // Reset the state after the drawFrame is executed, because this state may be used in the drawFrame
        calculateFps()
    }

    open fun onRenderSizeChanged() {}

    protected open fun drawFrame() {
        if (mTextureIn == 0) {
            return
        }
        if (mWidth != 0 && mHeight != 0) {
            GLES20.glViewport(0, 0, mWidth, mHeight)
        }

        GLES20.glUseProgram(mProgramHandle)

        GLES20.glClearColor(1f, 0f, 0f, 1f)
        GLES20.glClear(GLES20.GL_DEPTH_BUFFER_BIT or GLES20.GL_COLOR_BUFFER_BIT)

        bindShaderValues()

        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4)
    }

    /**
     * 计算FPS
     */
    private fun calculateFps() {
        if (mLastTime == 0L) {
            mLastTime = System.currentTimeMillis()
        }
        mFrameCount++
        if (System.currentTimeMillis() - mLastTime >= 1000) {
            mLastTime = System.currentTimeMillis()
            mFps = mFrameCount
            mFrameCount = 0
        }
    }

    /**
     * 必须在GL线程执行，释放纹理等OpenGL资源
     */
    open fun destroy() {
        mInitialized = false
        if (mProgramHandle != 0) {
            GLES20.glDeleteProgram(mProgramHandle)
            mProgramHandle = 0
        }
        if (mVertexShaderHandle != 0) {
            GLES20.glDeleteShader(mVertexShaderHandle)
            mVertexShaderHandle = 0
        }
        if (mFragmentShaderHandle != 0) {
            GLES20.glDeleteShader(mFragmentShaderHandle)
            mFragmentShaderHandle = 0
        }
    }

    companion object {

        val ATTRIBUTE_POSITION = "position"
        val ATTRIBUTE_TEXTURE_COORD = "inputTextureCoordinate"
        val VARYING_TEXTURE_COORD = "textureCoordinate"
        val UNIFORM_TEXTURE = "inputImageTexture"
        val UNIFORM_TEXTURE_0 = UNIFORM_TEXTURE

        val DEFAULT_VERTEX_SHADER = ("attribute vec4 " + ATTRIBUTE_POSITION + ";\n"
                + "attribute vec2 " + ATTRIBUTE_TEXTURE_COORD + ";\n"
                + "varying vec2 " + VARYING_TEXTURE_COORD + ";\n"
                + "void main() {\n"
                + "  " + VARYING_TEXTURE_COORD + " = " + ATTRIBUTE_TEXTURE_COORD + ";\n"
                + "   gl_Position = " + ATTRIBUTE_POSITION + ";\n"
                + "}\n")

        val DEFAULT_FRAGMENT_SHADER = ("precision mediump float;\n"
                + "uniform sampler2D " + UNIFORM_TEXTURE_0 + ";\n"
                + "varying vec2 " + VARYING_TEXTURE_COORD + ";\n"
                + "void main(){\n"
                + "   gl_FragColor = texture2D(" + UNIFORM_TEXTURE_0 + "," + VARYING_TEXTURE_COORD + ")" + ";\n"
                + "}\n")
    }
}