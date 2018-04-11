package com.dastanapps

/**
 * Created by dastaniqbal on 11/04/2018.
 * dastanIqbal@marvelmedia.com
 * 11/04/2018 11:40
 */

object NativeWrapper {
    init {
        System.loadLibrary("Native")
    }

    external fun init(cmdArray: Array<String>): String
}
