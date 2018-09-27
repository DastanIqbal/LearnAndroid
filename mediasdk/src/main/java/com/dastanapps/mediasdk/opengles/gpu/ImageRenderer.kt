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
open class ImageRenderer(private var filter: ImageFilter) : GLSurfaceView.Renderer {
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
        filter.onDraw(textId, squareBuffer, textureBuffer)
    }

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        GLES20.glViewport(0, 0, width, height)
        GLES20.glUseProgram(filter.progId)
    }

    override fun onSurfaceCreated(p0: GL10?, p1: EGLConfig?) {
        GLES20.glClearColor(1f, 0f, 0f, 1f)
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

    fun setFilter(filter: ImageFilter) {
        runOnDraw(Runnable {
            val oldFilter = this.filter
            oldFilter.destroy()

            this.filter = filter
            filter.init()
            GLES20.glUseProgram(filter.progId)
        })
        glSurfaceView?.requestRender()
    }

    fun setGLSurfaceView(glSurfaceView: GLSurfaceView) {
        this.glSurfaceView = glSurfaceView
    }
}