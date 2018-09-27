package com.dastanapps.mediasdk.opengles.filters

import com.dastanapps.mediasdk.opengles.utils.GLDrawer2D

/**
 * Created by dastaniqbal on 08/02/2018.

 * 08/02/2018 7:25
 */
class MirrorFilter : GLDrawer2D() {
    init {
        fss = constructShaderExecuteMain(
                "float gate = 0.01;\n" +
                        "if(vTextureCoord.x < 0.5-gate)\n" +
                        "{\n" +
                        "   gl_FragColor = texture2D(sTexture,vTextureCoord);\n" +
                        "}\n" +
                        "else if(vTextureCoord.x < 0.5+gate)\n" +
                        "{\n" +
                        "   float weight = (vTextureCoord.x + gate - 0.5) / (2.0 * gate);\n" +
                        "   vec4 color1 = texture2D(sTexture,vTextureCoord);\n" +
                        "   vec4 color2 = texture2D(sTexture,vec2(1.0 - vTextureCoord.x, " +
                        "       vTextureCoord.y));\n" +
                        "   gl_FragColor = mix(color1, color2, weight);\n" +
                        "}\n" +
                        "else\n" +
                        "{\n" +
                        "   gl_FragColor = texture2D(sTexture,vec2(1.0 - vTextureCoord.x, " +
                        "   vTextureCoord.y));\n" +
                        "}\n");
    }
}