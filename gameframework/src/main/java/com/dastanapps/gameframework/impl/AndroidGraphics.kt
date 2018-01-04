package com.dastanapps.gameframework.impl

import android.content.res.AssetManager
import android.graphics.*
import com.dastanapps.gameframework.Graphics
import com.dastanapps.gameframework.Pixmap

/**
 * Created by dastaniqbal on 12/10/2017.
 * dastanIqbal@marvelmedia.com
 * 12/10/2017 11:22
 */
class AndroidGraphics(val assets: AssetManager, val frameBuffer: Bitmap) : Graphics {
    val canvas = Canvas(frameBuffer)
    val paint = Paint()
    val srcRect = Rect()
    val destRect = Rect()

    override fun newPixmap(fileName: String, format: Graphics.PixmapFormat): Pixmap {
        val config: Bitmap.Config = when (format) {
            Graphics.PixmapFormat.RGB565 -> Bitmap.Config.RGB_565
            Graphics.PixmapFormat.ARGB4444 -> Bitmap.Config.ARGB_4444
            else -> Bitmap.Config.ARGB_8888
        }

        val bitmap: Bitmap
        val options = BitmapFactory.Options()
        options.inPreferredConfig = config
        try {
            val inputStream = assets.open(fileName);
            bitmap = BitmapFactory.decodeStream(inputStream)
            if (bitmap == null) {
                throw RuntimeException("Found invalid Bitmap " + fileName)
            }
        } catch (e: Exception) {
            throw RuntimeException("Couldn't load bitmap from asset " + fileName)
        }

        val pixFormat = when {
            bitmap.config == Bitmap.Config.RGB_565 -> Graphics.PixmapFormat.RGB565
            bitmap.config == Bitmap.Config.ARGB_4444 -> Graphics.PixmapFormat.ARGB4444
            else -> Graphics.PixmapFormat.ARGB8888
        }
        return AndroidPixmap(bitmap, pixFormat)
    }

    override fun clear(color: Int) {
        canvas.drawRGB(color.and(0xff0000).shr(16),
                color.and(0xff00).shr(8),
                color.and(0xff))
    }

    override fun drawPixel(x: Float, y: Float, color: Int) {
        paint.color = color
        canvas.drawPoint(x, y, paint)
    }

    override fun drawLine(x: Float, y: Float, x2: Float, y2: Float, color: Int) {
        paint.color = color
        canvas.drawLine(x, y, x2, y2, paint)
    }

    override fun drawRect(x: Float, y: Float, width: Float, height: Float, color: Int) {
        paint.color = color
        paint.style = Paint.Style.FILL
        canvas.drawRect(x, y, x + width - 1, y + height - 1, paint)
    }

    override fun drawPixmap(pixmap: Pixmap, x: Int, y: Int, srcX: Int, srcY: Int, srcWidth: Int, srcHeight: Int) {
        srcRect.left = srcX
        srcRect.top = srcY
        srcRect.right = srcX + srcWidth - 1
        srcRect.bottom = srcY + srcHeight - 1

        destRect.left = x
        destRect.top = y
        destRect.right = x + srcWidth - 1
        destRect.bottom = y + srcHeight - 1
        canvas.drawBitmap((pixmap as AndroidPixmap).bitmap, srcRect, destRect, null)
    }

    override fun drawPixmap(pixmap: Pixmap, x: Float, y: Float) {
        canvas.drawBitmap((pixmap as AndroidPixmap).bitmap, x, y, null)
    }

    override fun getWidth(): Int {
        return frameBuffer.width
    }

    override fun getHeight(): Int {
        return frameBuffer.height
    }
}