package com.dastanapps.ffmpegjni

import android.os.Bundle
import android.support.v7.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    override fun onResume() {
        super.onResume()
        FFmpegJNI()
                .ffmpegCommand("ffmpeg -y -i /sdcard/KrusoTestVideo/ezgif-3-704253d805.mp4" +
                        " -filter_complex drawtext=text=iqbal:fontcolor=white:fontsize=96" +
                        " -strict 2 /sdcard/Test.mp4")
    }
}