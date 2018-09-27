package com.dastanapps.ffmpegjni

import android.util.Log

/**
 * Created by dastaniqbal on 19/07/2018.

 * 19/07/2018 11:02
 */
class VideoKit {
    init {
        try {
            System.loadLibrary("ffmpeg")
            System.loadLibrary("ffprobe")
        } catch (e: UnsatisfiedLinkError) {
            e.printStackTrace()
        }
    }

    var videoKitListener: IVideoKit? = null
    var videoKitProbeListener: IVideoKitProbe? = null

    external fun avformatinfo(): String

    external fun avcodecinfo(): String

    external fun avfilterinfo(): String

    external fun configurationinfo(): String

    external fun run(args: Array<String>): Int

    external fun runprobe(args: Array<String>): Int

    external fun setDebug(debug: Boolean)

    external fun stopFFprobe()
    external fun stopTranscoding(stop: Boolean)
    fun error(error: String) {
        val fFmpegError = FFmpegError(error)
        Log.d("DEBUG", "Error: $error ${fFmpegError.message}")
        videoKitListener?.run {
            benchmark(error)
        }
    }

    fun showBenchmark(bench: String) {
        videoKitListener?.run {
            benchmark(bench)
        }
    }

    fun showProgress(progress: String) {
        videoKitListener?.run {
            progress(progress)
        }
    }

    fun probeOutput(output: String) {
        videoKitProbeListener?.run {
            output(output)
        }
    }

    fun probeError(error: String) {
        videoKitProbeListener?.run {
            error(error)
        }
    }

    fun process(args: Array<String>): Int {
        return run(args)
    }


    interface IVideoKit {
        fun progress(progress: String)
        fun benchmark(bench: String)
        fun error(error: String)
    }

    interface IVideoKitProbe {
        fun output(output: String)
        fun error(error: String)
    }
}