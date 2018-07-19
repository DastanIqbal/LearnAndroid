package com.dastanapps.ffmpegso

/**
 * Created by dastaniqbal on 19/07/2018.
 * dastanIqbal@marvelmedia.com
 * 19/07/2018 11:02
 */
class VideoKit {
    init {
        try {
            System.loadLibrary("ffmpegso");
        } catch (e: UnsatisfiedLinkError) {
            e.printStackTrace()
        }
    }

    external fun avformatinfo(): String

    external fun avcodecinfo(): String

    external fun avfilterinfo(): String

    external fun configurationinfo(): String

    external fun run(args: Array<String>): Int
}