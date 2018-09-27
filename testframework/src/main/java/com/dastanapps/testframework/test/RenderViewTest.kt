package com.dastanapps.testframework.test

import android.graphics.Canvas
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.view.Window
import android.view.WindowManager
import java.util.*

/**
 * Created by dastaniqbal on 05/07/2017.

 * 05/07/2017 11:52
 */
class RenderViewTest : AppCompatActivity() {
    inner class RenderView() : View(applicationContext) {
        val random = Random()
        override fun onDraw(canvas: Canvas) {
            super.onDraw(canvas)
            canvas.drawRGB(random.nextInt(256), random.nextInt(256), random.nextInt(256))
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN)
        super.onCreate(savedInstanceState)
        setContentView(RenderView())
    }
}