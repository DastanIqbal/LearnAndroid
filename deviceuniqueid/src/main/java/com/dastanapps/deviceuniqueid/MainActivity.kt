package com.dastanapps.deviceuniqueid

import android.os.Bundle
import android.support.v7.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
//        Controller.getInstance().run(arrayOf(
//                "-y",
//                "-i",
//                "/sdcard/TestVideo/big_buck_bunny.mp4",
//                "-filter_complex",
//                "drawtext=text='Testing':fontcolor=white",
//                "/sdcard/TestVideo/output1.mp4"
//        ))
    }
}
