package com.dastanapps.camera2.opengles.filters

import com.dastanapps.camera2.opengles.utils.GLDrawer2D

/**
 * Created by dastaniqbal on 23/01/2018.
 * dastanIqbal@marvelmedia.com
 * 23/01/2018 6:10
 */
class NoneFilter : GLDrawer2D() {
    init {
        fss=constructShader("gl_FragColor = color;\n")
    }
}