package com.dastanapps.camera2

import android.app.Activity
import android.hardware.Camera
import android.os.Bundle
import com.dastanapps.mediasdk.opengles.CameraSurfaceRenderer
import java.io.IOException

/**
 * Created by dastaniqbal on 19/02/2018.
 * dastanIqbal@marvelmedia.com
 * 19/02/2018 4:20
 */
class CameraActivity : Activity() {

    lateinit var mySurfaceView: MySurfaceView2
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mySurfaceView = MySurfaceView2(this)
        mySurfaceView.setEGLSurfaceTextureListener {
            CameraSurfaceRenderer.EGLSurfaceTextureListener { surfaceTexture ->
                val mCamera = Camera.open(0)
                try {
                    mCamera.setDisplayOrientation(90)
                    mCamera.setPreviewTexture(surfaceTexture)
                    mCamera.startPreview()

                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        }
        setContentView(mySurfaceView)
    }
}