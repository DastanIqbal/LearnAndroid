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

    fun play(view: View) {
        textView.text = ""
        //MP3
        //val comand="ffmpeg -y -i /sdcard/TestVideo/big_buck_bunny_720p_stereo.mp4 -codec:a libmp3lame -qscale:a 2 /sdcard/TestVideo/Mp3Test.mp3"
        //SRT
        //val comand="ffmpeg -y -i /sdcard/TestVideo/sample.srt /sdcard/TestVideo/sample.ass"
        //Xval comand ="ffmpeg -y -i /sdcard/TestVideo/ezgif-3-704253d805.mp4 -vf ass=/sdcard/TestVideo/sample.ass /sdcard/TestVideo/SampleASS.mp4"
        //Xval comand ="ffmpeg -y -i /sdcard/TestVideo/big_buck_bunny_720p_stereo.mp4 -vf subtitles=/sdcard/TestVideo/sample.srt /sdcard/TestVideo/SampleASS.mp4"
        //PNG
        //val comand="ffmpeg -y -ss 0 -t 1 -i /sdcard/TestVideo/ezgif-3-704253d805.mp4 -r 1 -f image2 /sdcard/Pictures/png-%2d.png"
        //DrawText
        val comand = "ffmpeg -y -i /sdcard/TestVideo/ezgif-3-704253d805.mp4 -filter_complex drawtext=fontfile=/system/fonts/Roboto-Bold.ttf:text='iqbal':fontcolor=white:fontsize=96 -strict -2 /sdcard/TestVideo/FFmpegDrawText.mp4"
        //Sticker
        //val comand = "ffmpeg -y -i /sdcard/TestVideo/ezgif-3-704253d805.mp4 -i /sdcard/TestVideo/repeat.png -filter_complex [0:v][1:v]overlay=(W-w)/2:(H-h)/2:enable='between(t,3,6)' -strict -2 /sdcard/TestVideo/ffmpegso.mp4"
        //Scale Video
        //val comand = "ffmpeg -y -i /sdcard/TestVideo/ezgif-3-704253d805.mp4 -filter_complex scale=250:250,pad=300:300:color=#0000ff -strict -2 /sdcard/ffmpegso.mp4"
        //Libx264
        // val comand = "ffmpeg -i /sdcard/TestVideo/ezgif-3-704253d805.mp4 -vcodec libx264 -acodec aac -strict -2 /sdcard/ffmpegso.mp4"

        val cmds = CmdlineBuilder()
                //.addInputPath("/sdcard/TestVideo/big_buck_bunny_720p_stereo.mp4")
                //.addInputPath("/storage/emulated/0/DCIM/Camera/VID_20180815_153644.mp4")
                //.concatInput("/sdcard/TestVideo/merge.txt")
                .addInputPath("/storage/emulated/0/DCIM/Camera/VID_20190128_152411492_HDR.mp4")
                .addFilterComplex("[0:v]setpts=PTS-STARTPTS,scale=1080.0:1920.0:force_original_aspect_ratio=decrease,pad=1080:1920:(ow-iw)/2:(oh-ih)/2:color=#000000")
                //.customCommand("-vcodec libx264 -pix_fmt yuv420p -crf 18 -preset superfast -tune zerolatency -strict experimental")
                //.addInputPath("/sdcard/TestVideo/merge/concat_audio__20180918091350.mp3")
                //.addFilterComplex("[0:a]volume=1.0[0a];[1:a]volume=1.0[0b];[0a][0b]amix=inputs=2:duration=shortest")
                //.customCommand("-map 0:v:0 -map 1:a:0 -shortest -c:v copy -strict -2")
                //.loopInput("/sdcard/TestVideo/inUsepatterns.png")
                //.addInputPath("/sdcard/TestVideo/beyond_the_sea.mp3")
                //.customCommand("-filter_complex drawtext=fontfile=/system/fonts/Roboto-Bold.ttf:text='iqbal':fontcolor=white:fontsize=96")
                //.customCommand("-filter_complex [0:v]scale=996:996:force_original_aspect_ratio=decrease,transpose=1,pad=996:996:0:(oh-ih)/2:color=#000000[0v];[1:v]scale=200.0:60.0:force_original_aspect_ratio=increase[1v];[0v][1v]overlay=W-w-10:H-h-10;[2:0]volume=0.5[a];[2:0]volume=0.1[b];[a][b]amix=inputs=2:duration=shortest -strict -2")
                //.addFilterComplex("[0:v]setpts=PTS-STARTPTS,scale=640:640:force_original_aspect_ratio=decrease,pad=640:640:(ow-iw)/2:(oh-ih)/2:color=#00000000[0v];[1:v]scale=96.0:36.0:force_original_aspect_ratio=increase[1v];[0v][1v]overlay=W-w-10:H-h-10[bg];[2:v][bg]overlay=(W-w)/2:(H-h)/2:shortest=1 -vcodec libx264 -crf 22 -tune zerolatency -strict -2")
                //.customCommand("[0:v]setpts=PTS-STARTPTS,scale=1088:1088:force_original_aspect_ratio=decrease,pad=1088:1088:(ow-iw)/2:(oh-ih)/2:color=#000000[0v];[1:v]scale=163.0:61.0:force_original_aspect_ratio=increase[1v];[0v][1v]overlay=W-w-10:H-h-10 -ac 2 -ar 44100 -vcodec libx264 -bf 2 -g 75 -b:v 10M -bufsize 1M -profile:v baseline -level 3.0 -preset -ultrafast -strict 2")
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
                        .subscribe({
                            textView.append(it)
                            if (it.contains("[bench]")) {
                                textView.append(it)
                            }
                        }, {
                            textView.append("Got Error ${it.message}")
                            Toast.makeText(this@MainActivity, "Got Error ${it.message}", Toast.LENGTH_SHORT).show()
                        }, {
                            textView.append(" DONE")
                            Toast.makeText(this@MainActivity, "Done", Toast.LENGTH_SHORT).show()

                            val cmds = CmdlineBuilder()
                                    .addInputPath("/storage/emulated/0/DCIM/Camera/VID_20190128_152411492_HDR.mp4")
                                    .addFilterComplex("[0:v]setpts=PTS-STARTPTS,scale=1080.0:1920.0:force_original_aspect_ratio=decrease,pad=1080:1920:(ow-iw)/2:(oh-ih)/2:color=#000000")
                                      .outputPath("/sdcard/TestVideo/MixAudioVideo1.mp4")
                                    .build()
                            FFmpegExecutor.execute(cmds)
                                    //FFmpegExecutor.executeProbe(cmd2.toTypedArray())
                                    .subscribe({
                                        textView.append(it)
                                        if (it.contains("[bench]")) {
                                            textView.append(it)
                                        }
                                    }, {
                                        textView.append("Got Error ${it.message}")
                                        Toast.makeText(this@MainActivity, "Got Error ${it.message}", Toast.LENGTH_SHORT).show()
                                    }, {
                                        textView.append(" DONE")
                                        Toast.makeText(this@MainActivity, "Done", Toast.LENGTH_SHORT).show()

                                        val cmds = CmdlineBuilder()
                                                .addInputPath("/storage/emulated/0/DCIM/Camera/VID_20190128_152411492_HDR.mp4")
                                                .addFilterComplex("[0:v]setpts=PTS-STARTPTS,scale=1080.0:1920.0:force_original_aspect_ratio=decrease,pad=1080:1920:(ow-iw)/2:(oh-ih)/2:color=#000000")
                                                .outputPath("/sdcard/TestVideo/MixAudioVideo2.mp4")
                                                .build()
                                        FFmpegExecutor.execute(cmds)
                                                //FFmpegExecutor.executeProbe(cmd2.toTypedArray())
                                                .subscribe({
                                                    textView.append(it)
                                                    if (it.contains("[bench]")) {
                                                        textView.append(it)
                                                    }
                                                }, {
                                                    textView.append("Got Error ${it.message}")
                                                    Toast.makeText(this@MainActivity, "Got Error ${it.message}", Toast.LENGTH_SHORT).show()
                                                }, {
                                                    textView.append(" DONE")
                                                    Toast.makeText(this@MainActivity, "Done", Toast.LENGTH_SHORT).show()
                                                })

                                    })
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
