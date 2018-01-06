package com.armmali.firstnative

/**
 * Created by dastaniqbal on 04/01/2018.
 * dastanIqbal@marvelmedia.com
 * 04/01/2018 11:47
 */
object NativeLibrary {
    init {
        System.loadLibrary("Native")
    }

    external fun init(): String
    external fun init(width: Int, height: Int)
    external fun step()
}