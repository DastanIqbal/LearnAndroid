package com.dastanapps.gles.filters

import android.graphics.SurfaceTexture
import com.dastanapps.gles.GLUtils

/**
 * Created by dastaniqbal on 23/01/2018.

 * 23/01/2018 6:01
 */
abstract class CameraFilter {
    val glUtils = GLUtils()
    fun draw(surfaceWidth: Int, surfaceHeight: Int, eglSurfaceTexture: SurfaceTexture, texId: Int) {
        glUtils.draw(surfaceWidth, surfaceHeight, eglSurfaceTexture, texId)
        onDraw(surfaceHeight, surfaceHeight, eglSurfaceTexture, texId)
    }

    abstract fun onDraw(surfaceWidth: Int, surfaceHeight: Int, eglSurfaceTexture: SurfaceTexture, texId: Int)

    fun setupShaders(program: Int, surfaceWidth: Int, surfaceHeight: Int, eglSurfaceTexture: SurfaceTexture?, textId: Int) {
        glUtils.setupShaders(program, surfaceWidth, surfaceHeight, eglSurfaceTexture, textId)
    }

    fun release() {
        glUtils.release()
    }
}