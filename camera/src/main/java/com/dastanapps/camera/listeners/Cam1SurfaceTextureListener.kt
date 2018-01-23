package com.dastanapps.camera.listeners

import android.app.Activity
import android.graphics.SurfaceTexture
import android.util.Log
import android.view.TextureView

import com.dastanapps.camera.Camera1
import com.dastanapps.view.AutoFitTextureView

class Cam1SurfaceTextureListener(private val camera1: Camera1, private val mTextureView: AutoFitTextureView, private val mActivity: Activity) : TextureView.SurfaceTextureListener {

    override fun onSurfaceTextureUpdated(surface: SurfaceTexture) {}

    override fun onSurfaceTextureSizeChanged(surface: SurfaceTexture,
                                             width: Int, height: Int) {
    }

    override fun onSurfaceTextureDestroyed(surface: SurfaceTexture): Boolean {
        camera1.closeCamera()
        return true
    }

    override fun onSurfaceTextureAvailable(surface: SurfaceTexture, width: Int, height: Int) {
        Log.d("!!!!", "onSurfaceTextureAvailable!!!")
        camera1.openCamera()
    }
}