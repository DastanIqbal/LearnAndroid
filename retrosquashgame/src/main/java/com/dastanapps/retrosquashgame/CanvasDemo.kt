package com.dastanapps.retrosquashgame

import android.databinding.DataBindingUtil
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.MotionEvent
import com.dastanapps.retrosquashgame.databinding.ActivityMainBinding

class CanvasDemo : AppCompatActivity() {

    internal var binding: ActivityMainBinding? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView<ActivityMainBinding>(this, R.layout.canvas_demo)
        createCanvas()
    }

    private fun createCanvas() {
        //Create a bitmap object to use as our canvas
        val bitmap: Bitmap = Bitmap.createBitmap(300, 600, Bitmap.Config.ARGB_8888);
        val canvas: Canvas = Canvas(bitmap)

        //A paint object that does our drawing, on our canvas
        val paint: Paint = Paint()

        //Set the background color
        canvas.drawColor(Color.BLACK)

        //Change the color of the virtual paint brush
        paint.color = Color.argb(255, 255, 255, 255)

        //Now draw a load of stuff on our canvas
        canvas.drawText("Score: 42 Lives, 3 Hi: 97", 10f, 10f, paint)
        canvas.drawLine(10f, 50f, 200f, 50f, paint)
        canvas.drawCircle(110f, 160f, 100f, paint)
        canvas.drawPoint(10f, 260f, paint)

        //Now put the canvas in the frame
        binding?.imvframe?.setImageBitmap(bitmap)
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {

        return false
    }
}
