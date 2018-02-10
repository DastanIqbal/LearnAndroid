package com.dastanapps.gles.filters2

import com.dastanapps.gles.GLDrawer2D

/**
 * Created by dastaniqbal on 23/01/2018.
 * dastanIqbal@marvelmedia.com
 * 23/01/2018 6:10
 */
class NoneFilter : GLDrawer2D() {
    init {
        fss=constructShader("gl_FragColor = color;\n")
        release()
        hProgram = loadShader(fss)
        if (hProgram == 0) throw IllegalStateException("Failed to create program")
    }
}