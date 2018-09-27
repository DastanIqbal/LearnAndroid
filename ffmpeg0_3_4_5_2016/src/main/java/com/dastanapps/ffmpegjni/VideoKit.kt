package com.dastanapps.ffmpegjni

/**
 * Created by dastaniqbal on 19/07/2018.

 * 19/07/2018 11:02
 */
class VideoKit {
    init {
        try {
            System.loadLibrary("ffmpeg")
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

    //external fun setDebug(debug: Boolean)

    external fun stopTranscoding(stop:Boolean)

    fun showBenchmark(bench:String){
        videoKitListener?.run {
            benchmark(bench)
        }
    }
    fun showProgress(progress: String) {
        videoKitListener?.run {
            progress(progress)
        }
    }

    fun process(args: Array<String>): Int {
        return run(args)
    }


    interface IVideoKit {
        fun progress(progress: String)
        fun benchmark(bench:String)
    }
}