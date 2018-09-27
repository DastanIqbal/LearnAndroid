package com.dastanapps.mediasdk.opengles.filters

import com.dastanapps.mediasdk.opengles.utils.GLDrawer2D

/**
 * Created by dastaniqbal on 23/01/2018.

 * 23/01/2018 6:10
 */
class BlackNWhiteFilter : GLDrawer2D() {
    init {
        fss = constructShader(
                "   float colorR = (color.r + color.g + color.b) / 3.0;\n" +
                        "   float colorG = (color.r + color.g + color.b) / 3.0;\n" +
                        "   float colorB = (color.r + color.g + color.b) / 3.0;\n" +
                        "   gl_FragColor = vec4(colorR, colorG, colorB, color.a);\n"
        )
    }
}