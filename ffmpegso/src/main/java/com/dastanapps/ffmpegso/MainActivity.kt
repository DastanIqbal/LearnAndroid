package com.dastanapps.ffmpegso

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.TextView
import android.widget.Toast
import processing.ffmpeg.videokit.AsyncCommandExecutor
import processing.ffmpeg.videokit.ProcessingListener


//Reference Link:
// http://www.ihubin.com/blog/android-ffmpeg-demo-1/
// https://blog.csdn.net/leixiaohua1020/article/details/47008825

class MainActivity : AppCompatActivity(), ProcessingListener {
    override fun onSuccess(path: String?) {
        runOnUiThread {
            Toast.makeText(this@MainActivity, "onSuccess", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onFailure(returnCode: Int) {
        runOnUiThread {
            Toast.makeText(this@MainActivity, "onFailure", Toast.LENGTH_SHORT).show()
        }
    }

    init {
        System.loadLibrary("ffmpegso")
    }

    private val videoKit = VideoKit()

    lateinit var textView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        textView = findViewById(R.id.textview)
    }

    fun format(view: View) {
        textView.text = videoKit.avformatinfo()
    }

    fun codec(view: View) {
        textView.text = videoKit.avcodecinfo()
    }

    fun filter(view: View) {
        textView.text = videoKit.avfilterinfo()
    }

    fun config(view: View) {
        textView.text = videoKit.configurationinfo()
    }


    fun play(view: View) {
        //Thread {
        //MP3
        //val comand="ffmpeg -y -i /sdcard/KrusoTestVideo/big_buck_bunny_720p_stereo.mp4 -codec:a libmp3lame -qscale:a 2 /sdcard/KrusoTestVideo/Mp3Test.mp3"
        //SRT
        //val comand="ffmpeg -y -i /sdcard/KrusoTestVideo/sample.srt /sdcard/KrusoTestVideo/sample.ass"
        //Xval comand ="ffmpeg -y -i /sdcard/KrusoTestVideo/ezgif-3-704253d805.mp4 -vf ass=/sdcard/KrusoTestVideo/sample.ass /sdcard/KrusoTestVideo/SampleASS.mp4"
        //Xval comand ="ffmpeg -y -i /sdcard/KrusoTestVideo/big_buck_bunny_720p_stereo.mp4 -vf subtitles=/sdcard/KrusoTestVideo/sample.srt /sdcard/KrusoTestVideo/SampleASS.mp4"
        //PNG
        //val comand="ffmpeg -y -ss 0 -t 1 -i /sdcard/KrusoTestVideo/ezgif-3-704253d805.mp4 -r 1 -f image2 /sdcard/Pictures/png-%2d.png"
        //DrawText
        val comand = "ffmpeg -y -i /sdcard/KrusoTestVideo/ezgif-3-704253d805.mp4 -filter_complex drawtext=fontfile=/system/fonts/Roboto-Bold.ttf:text='iqbal':fontcolor=white:fontsize=96 -strict -2 /sdcard/KrusoTestVideo/FFmpegDrawText.mp4"
        //Sticker
        //val comand = "ffmpeg -y -i /sdcard/KrusoTestVideo/ezgif-3-704253d805.mp4 -i /sdcard/KrusoTestVideo/repeat.png -filter_complex [0:v][1:v]overlay=(W-w)/2:(H-h)/2:enable='between(t,3,6)' -strict -2 /sdcard/KrusoTestVideo/ffmpegso.mp4"
        //Scale Video
        //val comand = "ffmpeg -y -i /sdcard/KrusoTestVideo/ezgif-3-704253d805.mp4 -filter_complex scale=250:250,pad=300:300:color=#0000ff -strict -2 /sdcard/ffmpegso.mp4"
        //Libx264
        // val comand = "ffmpeg -i /sdcard/KrusoTestVideo/ezgif-3-704253d805.mp4 -vcodec libx264 -acodec aac -strict -2 /sdcard/ffmpegso.mp4"
//        val args = comand.split(" ".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
//        for (i in args.indices) {
//            Log.d("ffmpeg-jni", args[i])
//        }
//        val result = videoKit.run(args)
//        if (result == 0) {
//            Log.i("JNI::", "$result Done")
//            runOnUiThread {
//                Toast.makeText(this@MainActivity, "Done", Toast.LENGTH_SHORT).show()
//            }
//        }
        //}.start()
        val command = videoKit.createCommand()
                .overwriteOutput()
                .inputPath("/sdcard/KrusoTestVideo/ezgif-3-704253d805.mp4")
                .outputPath("/sdcard/KrusoTestVideo/FFmpegDrawText.mp4")
                .customCommand("-filter_complex drawtext=fontfile=/system/fonts/Roboto-Bold.ttf:text='iqbal':fontcolor=white:fontsize=96")
                .experimentalFlag()
                .build()

        AsyncCommandExecutor(command, this).execute()
        textView.text = videoKit.configurationinfo()
    }
}
