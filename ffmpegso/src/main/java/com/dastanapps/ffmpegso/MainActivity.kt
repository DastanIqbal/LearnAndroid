package com.dastanapps.ffmpegso

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.TextView
import android.widget.Toast
import com.dastanapps.processing.CmdlineBuilder
import com.dastanapps.processing.FFmpegExecutor
import io.reactivex.disposables.CompositeDisposable
import processing.async.FFmpeg
import processing.async.LoadBinaryResponseHandler
import processing.async.exceptions.FFmpegNotSupportedException


//Reference Link:
// http://www.ihubin.com/blog/android-ffmpeg-demo-1/
// https://blog.csdn.net/leixiaohua1020/article/details/47008825

class MainActivity : AppCompatActivity() {
    val compositeDisposable = CompositeDisposable()
    lateinit var textView: TextView
    lateinit var ffmpeg: FFmpeg

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        textView = findViewById(R.id.textview)
        ffmpeg= FFmpeg.getInstance(this)
        loadFFMpegBinary()
    }

    private fun loadFFMpegBinary() {
        try {
            ffmpeg.loadBinary(object : LoadBinaryResponseHandler() {
                override fun onFailure() {
                    showMsg("Failed")
                }
            })
        } catch (e: FFmpegNotSupportedException) {
            e.printStackTrace()
            showMsg(e.message)
        }

    }

    fun showMsg(msg: String?) {
        Toast.makeText(this@MainActivity, msg, Toast.LENGTH_SHORT).show()
    }

    fun format(view: View) {
        compositeDisposable.add(FFmpegExecutor.avformatinfo()
                .subscribe({
                    textView.text = it
                }, {
                    Toast.makeText(this@MainActivity, "Got Error", Toast.LENGTH_SHORT).show()
                }, {
                    Toast.makeText(this@MainActivity, "Done", Toast.LENGTH_SHORT).show()
                })
        )
    }

    fun codec(view: View) {
        compositeDisposable.add(FFmpegExecutor.avcodecinfo()
                .subscribe({
                    textView.text = it
                }, {
                    Toast.makeText(this@MainActivity, "Got Error", Toast.LENGTH_SHORT).show()
                }, {
                    Toast.makeText(this@MainActivity, "Done", Toast.LENGTH_SHORT).show()
                }))
    }

    fun filter(view: View) {
        compositeDisposable.add(FFmpegExecutor.avfilterinfo()
                .subscribe({
                    textView.text = it
                }, {
                    Toast.makeText(this@MainActivity, "Got Error", Toast.LENGTH_SHORT).show()
                }, {
                    Toast.makeText(this@MainActivity, "Done", Toast.LENGTH_SHORT).show()
                }))
    }

    fun config(view: View) {
        compositeDisposable.add(FFmpegExecutor.configurationinfo()
                .subscribe({
                    textView.text = it
                }, {
                    Toast.makeText(this@MainActivity, "Got Error", Toast.LENGTH_SHORT).show()
                }, {
                    Toast.makeText(this@MainActivity, "Done", Toast.LENGTH_SHORT).show()
                }))
    }


    fun play(view: View) {
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

        val cmds = CmdlineBuilder()
                .addInputPath("/sdcard/TestVideo/big_buck_bunny_720p_stereo.mp4")
                //.customCommand("-filter_complex drawtext=fontfile=/system/fonts/Roboto-Bold.ttf:text='iqbal':fontcolor=white:fontsize=96")
                //.customCommand("-vcodec libx264 -acodec aac ")
                .outputPath("/sdcard/TestVideo/TranscodingVideo/FFmpegDrawText.mp4")
                .build()

        /*ffmpeg.execute(cmds,object :FFmpegExecuteResponseHandler {
            override fun onFinish() {
                showMsg("onFinish")
            }

            override fun onSuccess(message: String?) {
                showMsg("onSuccess")
                Log.d("DEBUG",message)
            }

            override fun onFailure(message: String?) {
                showMsg("onFailure $message")
                Log.d("DEBUG",message)
            }

            override fun onStart() {
                showMsg("onStart")
            }

            override fun onProgress(message: String?) {
                showMsg("onProgess $message")
                Log.d("DEBUG",message)
            }

        })*/

        compositeDisposable.add(FFmpegExecutor.execute(cmds)
                .subscribe({
                    textView.text = it
                }, {
                    Toast.makeText(this@MainActivity, "Got Error", Toast.LENGTH_SHORT).show()
                }, {
                    textView.text = "Done"
                    Toast.makeText(this@MainActivity, "Done", Toast.LENGTH_SHORT).show()
                }))


        //     startService(Intent(this, TranscodingService::class.java))

//        val videoKit = VideoKit()
//        val command = videoKit.createCommand()
//                .overwriteOutput()
//                .inputPath("/sdcard/KrusoTestVideo/ezgif-3-704253d805.mp4")
//                .inputPath("/sdcard/KrusoTestVideo/repeat.png")
//                .outputPath("/sdcard/KrusoTestVideo/FFmpegDrawText.mp4")
//                .customCommand("-filter_complex [0:v][1:v]overlay=(W-w)/2:(H-h)/2:enable='between(t,3,6)")
//                //.copyVideoCodec()
//                .experimentalFlag()
//                .build()
//
//        AsyncCommandExecutor(command, object : ProcessingListener {
//            override fun onSuccess(path: String?) {
//                runOnUiThread {
//                    Toast.makeText(this@MainActivity, "Done", Toast.LENGTH_SHORT).show()
//                }
//            }
//
//            override fun onFailure(returnCode: Int) {
//                runOnUiThread {
//                    Toast.makeText(this@MainActivity, "Got Error", Toast.LENGTH_SHORT).show()
//                }
//            }
//
//        }).execute()
    }

    override fun onDestroy() {
        super.onDestroy()
        compositeDisposable.clear()
    }
}
