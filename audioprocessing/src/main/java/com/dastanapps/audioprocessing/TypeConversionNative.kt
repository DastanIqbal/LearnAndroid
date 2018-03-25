package com.dastanapps.audioprocessing

/**
 * Created by dastaniqbal on 25/03/2018.
 * dastanIqbal@marvelmedia.com
 * 25/03/2018 12:29
 */

object TypeConversionNative {
    init {
        System.loadLibrary("TypeConversion")
    }

    external fun shortToByte(shortArray: ShortArray): ByteArray
}