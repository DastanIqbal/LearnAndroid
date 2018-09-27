package com.dastanapps.testframework.test

import android.content.Context
import android.graphics.*
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.view.Window
import android.view.WindowManager

/**
 * Created by dastaniqbal on 26/07/2017.

 * 26/07/2017 6:27
 */
class FontTest : AppCompatActivity() {

    inner class Renderview(context: Context) : View(context) {
        var typeFont: Typeface = Typeface.createFromAsset(assets, "Adore64.ttf")
        var paint = Paint()
        var bounds = Rect()

        override fun onDraw(canvas: Canvas) {
            super.onDraw(canvas)
            canvas.drawRGB(0, 0, 0)
            paint.color = Color.RED
            paint.textSize = 28f
            paint.typeface = typeFont
            paint.textAlign = Paint.Align.CENTER
            canvas.drawText("This is test string!", canvas.width / 2f, 100f, paint)

            val text = "This is another text O_o"
            paint.color = Color.WHITE
            paint.textSize = 18f
            paint.textAlign = Paint.Align.LEFT
            paint.getTextBounds(text, 0, text.length, bounds)  //Full Text Length including space and area
            canvas.drawText(text, (canvas.width - bounds.width()).toFloat(), 140f, paint)
            invalidate()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN)
        super.onCreate(savedInstanceState)
        setContentView(Renderview(this))
    }
}