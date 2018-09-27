package com.dastanapps.ffmpegso

import android.util.Log
import processing.ffmpeg.videokit.CommandBuilder
import processing.ffmpeg.videokit.VideoCommandBuilder

/**
 * Created by dastaniqbal on 19/07/2018.

 * 19/07/2018 11:02
 */
class VideoKit {
    init {
        try {
            System.loadLibrary("ffmpegso")
        } catch (e: UnsatisfiedLinkError) {
            e.printStackTrace()
        }
    }

    var videoKitListener: IVideoKit? = null

    external fun avformatinfo(): String

    external fun avcodecinfo(): String

    external fun avfilterinfo(): String

    external fun configurationinfo(): String

    external fun run(args: Array<String>): Int

    fun showProgress(progress: String) {
        Log.d("JNI:VidKit", progress)
        videoKitListener?.run {
            progress(progress)
        }
    }

    fun process(args: Array<String>): Int {
        return run(args)
    }

    fun createCommand(): CommandBuilder {
        return VideoCommandBuilder(this)
    }

    interface IVideoKit {
        fun progress(progress: String)
    }
}