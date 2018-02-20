package com.dastanapps.gles.filters2

import com.dastanapps.gles.GLDrawer2D

/**
 * Created by dastaniqbal on 08/02/2018.
 * dastanIqbal@marvelmedia.com
 * 08/02/2018 7:25
 */
class LookUpFilter:GLDrawer2D(){
    init {
        fss = constructShader(
                "    float colorR = (1.0 - color.r) / 1.0;\n" +
                        "    float colorG = (1.0 - color.g) / 1.0;\n" +
                        "    float colorB = (1.0 - color.b) / 1.0;\n" +
                        "    gl_FragColor = vec4(colorR, colorG, colorB, color.a);\n")
//        hProgram = loadShader(fss)
//        if (hProgram == 0) throw IllegalStateException("Failed to create program")
//        bindShaderValues(hProgram)
    }
}