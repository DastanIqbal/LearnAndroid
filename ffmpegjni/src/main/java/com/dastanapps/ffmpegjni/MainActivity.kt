package com.dastanapps.ffmpegjni

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.TextView
import android.widget.Toast
import com.dastanapps.processing.CmdlineBuilder
import com.dastanapps.processing.FFmpegExecutor
import io.reactivex.disposables.CompositeDisposable
import java.util.*


//Reference Link:
// http://www.ihubin.com/blog/android-ffmpeg-demo-1/
// https://blog.csdn.net/leixiaohua1020/article/details/47008825
//https://voiddog.github.io/archives/

class MainActivity : AppCompatActivity() {
    val compositeDisposable = CompositeDisposable()
    lateinit var textView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        textView = findViewById(R.id.textview)
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

//    fun play(view: View) {
//        val cmd1 = CmdlineBuilder()
//                .addInputPath("/sdcard/TestVideo/big_buck_bunny_720p_stereo.mp4")
//                .customCommand("-c:v libx264 -preset ultrafast -strict -2")
//                .outputPath("/sdcard/TestVideo/Video1.ts")
//                .build()
//        val video1 = FFmpegExecutor.execute(cmd1)
//
//        val cmd2 = CmdlineBuilder()
//                .addInputPath("/sdcard/TestVideo/beyond_the_sea.mp3")
//                .customCommand("-c:v libx264 -preset ultrafast -strict -2")
//                .outputPath("/sdcard/TestVideo/Video2.ts")
//                .build()
//        val video2 = FFmpegExecutor.execute(cmd2)
//
//        val cmd3 = CmdlineBuilder()
//                .addInputPath("/sdcard/TestVideo/beyond_the_sea.mp3")
//                .customCommand("-c:v libx264 -preset ultrafast -strict -2")
//                .outputPath("/sdcard/TestVideo/Video2.ts")
//                .build()
//        val video3 = FFmpegExecutor.execute(cmd3)
//        Observable.concat(video1,video2,video3)
//    }

    val video="/sdcard/TestVideo/VID20171207184038.mp4"//"/storage/emulated/0/DCIM/Camera/Comedy Football  Funniest Moments 2018 ● HD 3.mp4"
    fun play(view: View) {
        textView.text = ""

        val cmds = CmdlineBuilder()
                //.concatInput("/sdcard/TestVideo/merge.txt")
                .addInputPath("/storage/emulated/0/Kruso/Captured/Captured_2019-02-25-07-30-01.mp4")
                .addInputPath("/sdcard/TestVideo/watermark.png")
                .addFilterComplex("[0:v]setpts=PTS-STARTPTS,scale=720.0:1280.0:force_original_aspect_ratio=decrease,pad=720:1280:(ow-iw)/2:(oh-ih)/2:color=#000000[0v];[1:v]scale=193.84616:72.58326:[1v];[0v][1v]overlay=W-w:H-h[video]")
                .customCommand("-map [video] -map [audio] -af volume=1.0 -c:v libx264 -pix_fmt yuv420p -crf 18 -preset superfast -tune zerolatency -strict experimental")
                //.loopInput("/sdcard/TestVideo/inUsepatterns.png")
                .outputPath("/sdcard/TestVideo/MixAudioVideo.mp4")
                .build()
        val cmd2 = ArrayList<String>()
        cmd2.add("ffprobe")
        cmd2.add("-hide_banner")
        cmd2.add("-v")
        cmd2.add("quiet")
        cmd2.add("-print_format")
        cmd2.add("json")
        cmd2.add("-show_streams")
        cmd2.add("-i")
        cmd2.add("/sdcard/TestVideo/merge/MixVideo.mp4")

        compositeDisposable.add(
                FFmpegExecutor.execute(cmds)
                        //FFmpegExecutor.executeProbe(cmd2.toTypedArray())
                        .doOnError {
                            textView.append("Got Exception ${it.message}")
                            Toast.makeText(this@MainActivity, "Got Exception ${it.message}", Toast.LENGTH_SHORT).show()
                        }
                        .subscribe({
                            textView.append(it+"\n")
                            if (it.contains("[bench]")) {
                                textView.append(it)
                            }
                        }, {
                            textView.append("\nGot Error ${it.message}")
                            Toast.makeText(this@MainActivity, "Got Error ${it.message}", Toast.LENGTH_SHORT).show()
                        }, {
                            textView.append(" DONE")
                            Toast.makeText(this@MainActivity, "Done", Toast.LENGTH_SHORT).show()

//                            val cmds = CmdlineBuilder()
//                                    .addInputPath(video)
//                                    .addFilterComplex("[0:v]setpts=PTS-STARTPTS,scale=1080.0:1920.0:force_original_aspect_ratio=decrease,pad=1080:1920:(ow-iw)/2:(oh-ih)/2:color=#000000")
//                                      .outputPath("/sdcard/TestVideo/MixAudioVideo1.mp4")
//                                    .build()
//                            FFmpegExecutor.execute(cmds)
//                                    //FFmpegExecutor.executeProbe(cmd2.toTypedArray())
//                                    .subscribe({
//                                        textView.append(it)
//                                        if (it.contains("[bench]")) {
//                                            textView.append(it)
//                                        }
//                                    }, {
//                                        textView.append("Got Error ${it.message}")
//                                        Toast.makeText(this@MainActivity, "Got Error ${it.message}", Toast.LENGTH_SHORT).show()
//                                    }, {
//                                        textView.append(" DONE")
//                                        Toast.makeText(this@MainActivity, "Done", Toast.LENGTH_SHORT).show()
//
//                                        val cmds = CmdlineBuilder()
//                                                .addInputPath(video)
//                                                .addFilterComplex("[0:v]setpts=PTS-STARTPTS,scale=1080.0:1920.0:force_original_aspect_ratio=decrease,pad=1080:1920:(ow-iw)/2:(oh-ih)/2:color=#000000")
//                                                .outputPath("/sdcard/TestVideo/MixAudioVideo2.mp4")
//                                                .build()
//                                        FFmpegExecutor.execute(cmds)
//                                                //FFmpegExecutor.executeProbe(cmd2.toTypedArray())
//                                                .subscribe({
//                                                    textView.append(it)
//                                                    if (it.contains("[bench]")) {
//                                                        textView.append(it)
//                                                    }
//                                                }, {
//                                                    textView.append("Got Error ${it.message}")
//                                                    Toast.makeText(this@MainActivity, "Got Error ${it.message}", Toast.LENGTH_SHORT).show()
//                                                }, {
//                                                    textView.append(" DONE")
//                                                    Toast.makeText(this@MainActivity, "Done", Toast.LENGTH_SHORT).show()
//                                                })
//
//                                    })
                        }))
    }

    fun stop(view: View) {
        FFmpegExecutor.stop(true)
      //  FFmpegExecutor.stopProbe()
    }

    override fun onDestroy() {
        super.onDestroy()
        compositeDisposable.clear()
    }
}
