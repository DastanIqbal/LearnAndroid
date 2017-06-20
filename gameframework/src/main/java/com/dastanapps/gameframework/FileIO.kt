package com.dastanapps.gameframework

import java.io.InputStream
import java.io.OutputStream

/**
 * Created by dastaniqbal on 21/06/2017.
 * dastanIqbal@marvelmedia.com
 * 21/06/2017 12:07
 */
interface FileIO{
    fun readAssest():InputStream
    fun readFile():InputStream
    fun writeFile():OutputStream
}