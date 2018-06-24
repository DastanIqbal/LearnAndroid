package com.dastanapps.ffmpegjni

import android.util.Log

/**
 * Created by dastaniqbal on 13/06/2018.
 * dastanIqbal@marvelmedia.com
 * 13/06/2018 2:10
 */
class FFmpegJNI {
    init {
        System.loadLibrary("ffmpegjni")
    }

    fun ffmpegCommand(comand: String){
        val args = comand.split(" ".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        for (i in args.indices) {
            Log.d("ffmpeg-jni", args[i])
        }
        ffmpegMain(args.size,args)
        //run()
    }

    external fun run()
    external fun ffmpegMain(argc: Int,args: Array<String>)
}