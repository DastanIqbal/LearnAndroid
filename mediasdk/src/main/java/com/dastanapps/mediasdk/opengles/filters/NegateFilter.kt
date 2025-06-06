package com.dastanapps.mediasdk.opengles.filters

import com.dastanapps.mediasdk.opengles.utils.GLDrawer2D


/**
 * Created by dastaniqbal on 23/01/2018.

 * 23/01/2018 6:10
 */
class NegateFilter : GLDrawer2D() {

    init {
        fss = constructShader(
                "    float colorR = (1.0 - color.r) / 1.0;\n" +
                        "    float colorG = (1.0 - color.g) / 1.0;\n" +
                        "    float colorB = (1.0 - color.b) / 1.0;\n" +
                        "    gl_FragColor = vec4(colorR, colorG, colorB, color.a);\n")
    }
}