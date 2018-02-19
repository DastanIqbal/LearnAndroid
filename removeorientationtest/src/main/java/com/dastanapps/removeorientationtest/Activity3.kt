package com.dastanapps.removeorientationtest

import android.content.Intent
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_main.*

class Activity3 : NoOrientationAnimationActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        tv_one.text = "Activity 3"
        tv_one.setOnClickListener { startActivity(Intent(this, MainActivity::class.java)) }
    }
}
