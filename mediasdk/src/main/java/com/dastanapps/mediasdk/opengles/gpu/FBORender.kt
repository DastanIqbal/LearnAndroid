package com.dastanapps.mediasdk.opengles.gpu

import android.graphics.Bitmap
import android.graphics.Canvas
import android.opengl.GLES20
import android.opengl.GLSurfaceView
import com.dastanapps.mediasdk.opengles.gpu.filter.ImageFilter
import com.dastanapps.mediasdk.opengles.gpu.util.TextureRotationUtil.TEXTURE_NO_ROTATION
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.util.*
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

/**
 * Created by dastaniqbal on 06/09/2018.

 * 06/09/2018 11:20
 */
open class FBORender : GLSurfaceView.Renderer {
    private val SQUARE = floatArrayOf(
            -1.0f, -1.0f,
            1.0f, -1.0f,
            -1.0f, 1.0f,
            1.0f, 1.0f
    )

    protected val squareBuffer by lazy {
        ByteBuffer.allocateDirect(SQUARE.size * 4)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer()
    }
    protected val textureBuffer by lazy {
        ByteBuffer.allocateDirect(TEXTURE_NO_ROTATION.size * 4)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer()
    }
    private val filter: ImageFilter by lazy {
        ImageFilter(ImageFilter.defalutVS, ImageFilter.defalutFS)
    }
    private var textId = -1
    private val mRunOnDraw: Queue<Runnable> = LinkedList<Runnable>()

    private var glSurfaceView: GLSurfaceView? = null

    init {
        squareBuffer.put(SQUARE).position(0)
        textureBuffer.put(TEXTURE_NO_ROTATION).position(0)
    }

    override fun onDrawFrame(p0: GL10?) {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT or GLES20.GL_DEPTH_BUFFER_BIT)
        runAll(mRunOnDraw)
        fboDraw();
    }

    private fun fboDraw() {
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, mFrameBuffers[0])
        filter.onDraw(textId, squareBuffer, textureBuffer)
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0)
    }

    private var mOutputWidth: Int = 0
    private var mOutputHeight: Int = 0

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        mOutputWidth = width
        mOutputHeight = height
        GLES20.glViewport(0, 0, width, height)
        GLES20.glUseProgram(filter.progId)
    }

    override fun onSurfaceCreated(p0: GL10?, p1: EGLConfig?) {
        GLES20.glClearColor(1f, 0f, 0f, 1f)
        initFBO()
        filter.init()
    }

    private fun runAll(queue: Queue<Runnable>) {
        synchronized(queue) {
            while (!queue.isEmpty())
                queue.poll().run()
        }
    }

    fun runOnDraw(runnable: Runnable) {
        synchronized(mRunOnDraw) {
            mRunOnDraw.add(runnable)
        }
    }

    fun setTexture(bitmap: Bitmap, recycle: Boolean) {
        runOnDraw(Runnable {
            var resizedBitmap: Bitmap? = null
            if (bitmap.width % 2 == 1) {
                resizedBitmap = Bitmap.createBitmap(bitmap.width + 1, bitmap.height,
                        Bitmap.Config.ARGB_8888)
                val can = Canvas(resizedBitmap!!)
                can.drawARGB(0x00, 0x00, 0x00, 0x00)
                can.drawBitmap(bitmap, 0f, 0f, null)
            }

            textId = OpenGlUtils.loadTexture(resizedBitmap ?: bitmap, textId, recycle)
        })
    }

    private val mFrameBuffers = IntArray(1)
    private val mFrameBufferTextures = IntArray(1)

    fun initFBO() {
        val i = 0
        GLES20.glGenFramebuffers(1, mFrameBuffers, i)
        GLES20.glGenTextures(1, mFrameBufferTextures, i)
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mFrameBufferTextures[i])
        GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_RGBA, mOutputWidth, mOutputWidth, 0,
                GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, null)
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D,
                GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR.toFloat())
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D,
                GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR.toFloat())
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D,
                GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE.toFloat())
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D,
                GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE.toFloat())

        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, mFrameBuffers[i])
        GLES20.glFramebufferTexture2D(GLES20.GL_FRAMEBUFFER, GLES20.GL_COLOR_ATTACHMENT0,
                GLES20.GL_TEXTURE_2D, mFrameBufferTextures[i], 0)

        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0)
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0)

        OpenGlUtils.checkGlError()
    }
}