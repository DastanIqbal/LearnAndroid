package com.dastanapps.animation

import android.animation.ObjectAnimator
import android.os.Bundle
import android.view.View
import android.view.animation.AccelerateInterpolator
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        val view =findViewById<View>(R.id.tv)
        val checkPoint1 =findViewById<View>(R.id.checkpoint1)
        val checkPoint2 =findViewById<View>(R.id.checkpoint2)
        checkPoint1.setOnClickListener {
            val screenHeight = resources.displayMetrics.heightPixels
            view.slideUpWith(3000, screenHeight.toFloat(), screenHeight/3f).start()
        }

        checkPoint2.setOnClickListener {
            val screenHeight = resources.displayMetrics.heightPixels
            view.slideUpWith(3000, view.translationY, -50f).start()
        }


    }

    fun View.slideUpWith(duration: Long = 500, screenHeight: Float, height: Float): ObjectAnimator {

        // Create the slide up animation
        val slideUp = ObjectAnimator.ofFloat(this, View.TRANSLATION_Y, screenHeight, height)
        slideUp.duration = duration // Adjust the duration as needed
//        slideUp.interpolator = AccelerateInterpolator()
        return slideUp
    }
}