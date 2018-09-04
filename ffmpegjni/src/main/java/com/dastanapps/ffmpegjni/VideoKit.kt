package com.dastanapps.ffmpegjni

import android.util.Log

/**
 * Created by dastaniqbal on 19/07/2018.
 * dastanIqbal@marvelmedia.com
 * 19/07/2018 11:02
 */
class VideoKit {
    init {
        try {
            System.loadLibrary("ffmpegjni")
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

    external fun setDebug(debug: Boolean)

    fun showProgress(progress: String) {
        Log.d("JNI:VidKit", progress)
        videoKitListener?.run {
            progress(progress)
        }
    }

    fun process(args: Array<String>): Int {
        return run(args)
    }


    interface IVideoKit {
        fun progress(progress: String)
    }
}