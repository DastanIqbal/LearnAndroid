package com.dastanapps.gles.filters

import android.graphics.SurfaceTexture

/**
 * Created by dastaniqbal on 23/01/2018.

 * 23/01/2018 6:10
 */
class BlackNWhiteFilter : CameraFilter() {
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
            "   float colorR = (color.r + color.g + color.b) / 3.0;\n" +
            "   float colorG = (color.r + color.g + color.b) / 3.0;\n" +
            "   float colorB = (color.r + color.g + color.b) / 3.0;\n" +
            "   gl_FragColor = vec4(colorR, colorG, colorB, color.a);\n" +
            "}\n"

    init {
        program = glUtils.buildProgram(sSimpleFS)
        if (program == 0) throw IllegalStateException("Failed to create program")
    }

    override fun onDraw(surfaceWidth: Int, surfaceHeight: Int, eglSurfaceTexture: SurfaceTexture, texId: Int) {
        setupShaders(program, surfaceWidth, surfaceHeight, eglSurfaceTexture,texId)
    }
}