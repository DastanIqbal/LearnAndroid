package com.dastanapps.gles.filters

import android.graphics.SurfaceTexture

/**
 * Created by dastaniqbal on 23/01/2018.

 * 23/01/2018 6:10
 */
class NegateFilter : CameraFilter() {
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
            "    float colorR = (1.0 - color.r) / 1.0;\n" +
            "    float colorG = (1.0 - color.g) / 1.0;\n" +
            "    float colorB = (1.0 - color.b) / 1.0;\n" +
            "    gl_FragColor = vec4(colorR, colorG, colorB, color.a);\n" +
            "}"

    init {
        program = glUtils.buildProgram(sSimpleFS)
        if (program == 0) throw IllegalStateException("Failed to create program")
    }

    override fun onDraw(surfaceWidth: Int, surfaceHeight: Int, eglSurfaceTexture: SurfaceTexture, texId: Int) {
        setupShaders(program, surfaceWidth, surfaceHeight, eglSurfaceTexture, texId)
    }
}