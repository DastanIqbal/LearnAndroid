package com.dastanapps.removeorientationtest

import android.os.Bundle
import kotlinx.android.synthetic.main.activity_main.*

class Activity2 : NoOrientationAnimationActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        tv_one.text = "Activity 2"
        tv_one.setOnClickListener { finish() }
    }
}
