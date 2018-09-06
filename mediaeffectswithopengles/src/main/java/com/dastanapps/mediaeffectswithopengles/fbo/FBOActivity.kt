package com.dastanapps.mediaeffectswithopengles.fbo

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.opengl.GLSurfaceView
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.dastanapps.mediaeffectswithopengles.R
import com.dastanapps.mediasdk.opengles.GLTextureView
import com.dastanapps.mediasdk.opengles.filters.BlackNWhiteFilter
import com.dastanapps.mediasdk.opengles.utils.GLDrawer2D
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL
import javax.microedition.khronos.opengles.GL10

/**
 * Created by dastan on 04/09/2018.
 * ask2iqbal@gmail.com
 * 04/09/2018 11:37
 */
class FBOActivity : AppCompatActivity() {

    private val glTextureView: GLTextureView by lazy {
        GLTextureView(this)
    }

    private val glDrawer2D: GLDrawer2D by lazy {
        GLDrawer2D()
    }

    private var txtId: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        glTextureView.setEGLContextClientVersion(2)
        glTextureView.setRenderer(object : GLTextureView.Renderer {

            override fun onDrawFrame(gl: GL10?) {
                if (txtId != -1)
                    glDrawer2D.draw(txtId, glDrawer2D.mStMatrix)
            }

            override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
                glDrawer2D.surfaceCreated(width, height)
                val bmp = BitmapFactory.decodeResource(this@FBOActivity.resources, R.mipmap.ic_launcher)
                txtId = GLDrawer2D.generateTexture(bmp)
            }

            override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
                glDrawer2D.setupShader()
            }
        })
        setContentView(glTextureView)
    }

    override fun onPause() {
        super.onPause()
        glTextureView.onPause()
    }

    override fun onResume() {
        super.onResume()
        glTextureView.onResume()
    }
}