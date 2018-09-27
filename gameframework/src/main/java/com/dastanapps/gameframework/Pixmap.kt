package com.dastanapps.gameframework

/**
 * Created by dastaniqbal on 21/06/2017.

 * 21/06/2017 12:17
 */
interface Pixmap {
    fun getWidth(): Int
    fun getHeight(): Int
    fun getFormat(): Graphics.PixmapFormat
    fun dispose()
}