package com.dastanapps.testframework.test

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Rect
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import android.view.Window
import android.view.WindowManager

/**
 * Created by dastaniqbal on 25/07/2017.
 * dastanIqbal@marvelmedia.com
 * 25/07/2017 6:50
 */
class BitmapTest : AppCompatActivity() {
    inner class RenderView(context: Context?) : View(context) {
        var bitmap565: Bitmap
        var bitmap444: Bitmap
        var dst = Rect()

        init {
            var inputStream = assets.open("bobrgb888.png")
            bitmap565 = BitmapFactory.decodeStream(inputStream)
            inputStream.close()
            Log.d("BitmapText", "bobrgb888.png format: " + bitmap565.config)

            inputStream = assets.open("bobargb8888.png")
            val bitmapOpts = BitmapFactory.Options()
            bitmapOpts.inPreferredConfig = Bitmap.Config.ARGB_4444
            bitmap444 = BitmapFactory.decodeStream(inputStream, null, bitmapOpts)
            inputStream.close()
            Log.d("BitmapText", "bobrgb888.png format: " + bitmap444.config)
        }

        override fun onDraw(canvas: Canvas) {
            super.onDraw(canvas)
            canvas.drawRGB(0, 0, 0)
            dst.set(50, 50, 350, 350)
            canvas.drawBitmap(bitmap565, null, dst, null)
            canvas.drawBitmap(bitmap444, 100f, 100f, null)
            invalidate()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN)
        setContentView(RenderView(this))
    }
}