package com.dastanapps.gameframework.impl

import android.graphics.Bitmap
import com.dastanapps.gameframework.Graphics
import com.dastanapps.gameframework.Pixmap

/**
 * Created by dastaniqbal on 12/10/2017.
 * dastanIqbal@marvelmedia.com
 * 12/10/2017 11:16
 */
class AndroidPixmap(val bitmap: Bitmap, val pixmapformat: Graphics.PixmapFormat) : Pixmap {
    override fun getFormat(): Graphics.PixmapFormat {
        return pixmapformat
    }

    override fun getWidth(): Int {
        return bitmap.width
    }

    override fun getHeight(): Int {
        return bitmap.height
    }

    override fun dispose() {
        return bitmap.recycle()
    }
}