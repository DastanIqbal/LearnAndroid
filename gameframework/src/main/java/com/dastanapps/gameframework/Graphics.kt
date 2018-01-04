package com.dastanapps.gameframework

/**
 * Created by dastaniqbal on 21/06/2017.
 * dastanIqbal@marvelmedia.com
 * 21/06/2017 12:16
 */
interface Graphics {
    enum class PixmapFormat {ARGB8888, ARGB4444, RGB565 }

    fun newPixmap(fileName: String, format: PixmapFormat): Pixmap
    fun clear(color: Int)
    fun drawPixel(x: Float, y: Float, color: Int)
    fun drawLine(x: Float, y: Float, x2: Float, y2: Float, color: Int)
    fun drawRect(x: Float, y: Float, width: Float, height: Float, color: Int)
    fun drawPixmap(pixmap: Pixmap, x: Int, y: Int, srcX: Int, srcY: Int, srcWidth: Int, srcHeight: Int)
    fun drawPixmap(pixmap: Pixmap, x: Float, y: Float)
    fun getWidth(): Int
    fun getHeight(): Int
}