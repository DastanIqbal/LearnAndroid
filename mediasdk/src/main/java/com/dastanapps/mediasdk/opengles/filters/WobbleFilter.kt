package com.dastanapps.mediasdk.opengles.filters

import android.opengl.GLES20
import com.dastanapps.mediasdk.opengles.utils.GLDrawer2D

/**
 * Created by dastaniqbal on 08/02/2018.

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
                + "}")
    }

    private val UNIFORM_OFFSET = "offset"
    private var mOffsetHandler: Int = 0
    private var mOffset: Float = 0.toFloat()
    private var mStartTime: Long = 0

    override fun bindShaderValues(hProgram: Int) {
        super.bindShaderValues(hProgram)
        mOffsetHandler = GLES20.glGetUniformLocation(hProgram, UNIFORM_OFFSET)
        GLES20.glUniform1f(mOffsetHandler, mOffset)
    }

    override fun onDrawFrame() {
        super.onDrawFrame()
        val time = System.currentTimeMillis() - mStartTime
        if (time > 20000) {
            mStartTime = System.currentTimeMillis()
        }

        mOffset = time / 1000f * 2f * 3.14159f * 0.75f
    }
}