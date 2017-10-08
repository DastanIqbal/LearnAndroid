package com.dastanapps.testframework.test

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.view.Window
import android.view.WindowManager

/**
 * Created by dastaniqbal on 05/07/2017.
 * dastanIqbal@marvelmedia.com
 * 05/07/2017 11:52
 */
class ShapeTest : AppCompatActivity() {
    inner class RenderView(context: Context) : View(context) {
        var paint: Paint? = null

        init {
            paint = Paint()
        }

        override fun onDraw(canvas: Canvas) {
            super.onDraw(canvas)
            canvas.drawRGB(255, 255, 255)
            paint?.color = Color.RED
            canvas.drawLine(0f, 0f, canvas.width - 1f, canvas.height - 1f, paint)

            paint?.style = Paint.Style.STROKE
            paint?.color = Color.BLACK
            canvas.drawCircle(canvas.width / 2f, canvas.height / 2f, 40f, paint)

            paint?.style = Paint.Style.FILL
            paint?.color = 0x770000ff
            canvas.drawRect(100f, 100f, 200f, 200f, paint)
            invalidate()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN)
        super.onCreate(savedInstanceState)
        setContentView(RenderView(this))
    }
}