package com.dastanapps.gles.filters

import android.graphics.SurfaceTexture
import com.dastanapps.gles.GLUtils

/**
 * Created by dastaniqbal on 23/01/2018.
 * dastanIqbal@marvelmedia.com
 * 23/01/2018 6:10
 */
class NoneFilter : CameraFilter() {
    private var program: Int = 0;
    private val sSimpleFS = "#extension GL_OES_EGL_image_external : require\n" +
            "\n" +
            "precision mediump float;\n" +
            "\n" +
            "uniform samplerExternalOES camTex;\n" +
            "varying vec2 camTexCoordinate;\n" +
            "\n" +
            "void main () {\n" +
            "    vec4 color = texture2D(camTex, camTexCoordinate);\n" +
            "    gl_FragColor = color;\n" +
            "}"

    init {
        program = GLUtils.buildProgram(sSimpleFS)
        if (program == 0) throw IllegalStateException("Failed to create program")
    }

    override fun onDraw(surfaceWidth: Int, surfaceHeight: Int, eglSurfaceTexture: SurfaceTexture) {
        setupShaders(program, surfaceWidth, surfaceHeight, eglSurfaceTexture)
    }
}