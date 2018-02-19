package com.dastanapps.removeorientationtest

import android.content.Intent
import android.content.pm.ActivityInfo
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : NoOrientationAnimationActivity(){

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        tv_one.setOnClickListener { startActivity(Intent(this, Activity2::class.java)) }
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
    }
}
