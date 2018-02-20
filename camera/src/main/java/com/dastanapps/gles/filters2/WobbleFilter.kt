package com.dastanapps.gles.filters2

import com.dastanapps.gles.GLDrawer2D

/**
 * Created by dastaniqbal on 08/02/2018.
 * dastanIqbal@marvelmedia.com
 * 08/02/2018 7:25
 */
class WobbleFilter : GLDrawer2D() {
    init {
        fss = ("#extension GL_OES_EGL_image_external : require\n"
                + "precision mediump float;\n"
                + "uniform samplerExternalOES sTexture;\n"
                + "uniform float offset;\n"
                + "varying highp vec2 vTextureCoord;\n"
                + "void main() {\n" +
                "    vec2 texcoord = vTextureCoord;\n" +
                "    texcoord.x += sin(texcoord.y * 4.0 * 2.0 * 3.14159 + offset) / 100.0;\n" +
                "    gl_FragColor = texture2D(sTexture, texcoord);\n"
                + "}");

//        hProgram = loadShader(fss)
//        if (hProgram == 0) throw IllegalStateException("Failed to create program")
//        bindShaderValues(hProgram)
    }
}