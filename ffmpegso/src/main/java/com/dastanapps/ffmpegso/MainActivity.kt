package com.dastanapps.ffmpegso

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import android.widget.TextView
import android.widget.Toast


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
    external fun run(cmds: Array<String>): Int


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


    fun play(view: View) {
        //DrawText
        val comand = "ffmpeg -i /sdcard/KrusoTestVideo/ezgif-3-704253d805.mp4 -filter_complex drawtext=text=iqbal:fontcolor=white -strict -2 /sdcard/ffmpegso.mp4"
        //Libx264
        // val comand = "ffmpeg -i /sdcard/KrusoTestVideo/ezgif-3-704253d805.mp4 -vcodec libx264 -acodec aac -strict -2 /sdcard/ffmpegso.mp4"
        val args = comand.split(" ".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        for (i in args.indices) {
            Log.d("ffmpeg-jni", args[i])
        }
        val result = run(args)
        if (result == 0) {
            Toast.makeText(this@MainActivity, "命令行执行完成", Toast.LENGTH_SHORT).show()
        }
        textView.text = configurationinfo()
    }
}
