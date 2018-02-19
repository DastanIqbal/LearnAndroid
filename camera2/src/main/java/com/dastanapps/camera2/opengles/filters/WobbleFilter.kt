package com.dastanapps.camera2.opengles.filters

import android.opengl.GLES20
import com.dastanapps.camera2.opengles.utils.GLDrawer2D


/**
 * Created by dastaniqbal on 08/02/2018.
 * dastanIqbal@marvelmedia.com
 * 08/02/2018 7:25
 */
class WobbleFilter : GLDrawer2D() {
    private var mOffset: Float = 0.toFloat()
    private var mStartTime: Long = 0

    init {
        release()
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
        mStartTime = System.currentTimeMillis()
        hProgram = loadShader(fss)
        if (hProgram == 0) throw IllegalStateException("Failed to create program")
        bindShaderValues(hProgram)
    }

    override fun bindShaderValues(hProgram: Int) {
        super.bindShaderValues(hProgram)
        val mOffsetHandler = GLES20.glGetUniformLocation(hProgram, "offset")
        GLES20.glUniform1f(mOffsetHandler, mOffset)
    }

    override fun draw(tex_id: Int, tex_matrix: FloatArray?) {
        bindShaderValues(hProgram)
        super.draw(tex_id, tex_matrix)
        val time = System.currentTimeMillis() - mStartTime
        if (time > 20000) {
            mStartTime = System.currentTimeMillis()
        }

        mOffset = time / 1000f * 2f * 3.14159f * 0.75f
    }
}