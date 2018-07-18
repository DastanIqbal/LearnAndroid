package com.dastanapps.ffmpegso

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.TextView

//Reference Link: http://www.ihubin.com/blog/android-ffmpeg-demo-1/

class MainActivity : AppCompatActivity() {

    init {
        System.loadLibrary("ffmpegso")
    }

    // JNI

    external fun avformatinfo(): String
    external fun avcodecinfo(): String
    external fun avfilterinfo(): String
    external fun configurationinfo(): String


    lateinit var textView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        textView = findViewById(R.id.textview)
    }

    fun format(view: View) {
        textView.text = avformatinfo()
    }

    fun codec(view: View) {
        textView.text = avcodecinfo()
    }

    fun filter(view: View) {
        textView.text = avfilterinfo()
    }

    fun config(view: View) {
        textView.text = configurationinfo()
    }
}
