package com.dastanapps.gameframework

import android.content.SharedPreferences
import java.io.InputStream
import java.io.OutputStream

/**
 * Created by dastaniqbal on 21/06/2017.

 * 21/06/2017 12:07
 */
interface FileIO{
    fun readAssest(fileName:String):InputStream
    fun readFile(fileName:String):InputStream
    fun writeFile(fileName:String):OutputStream
    fun getPreferences(): SharedPreferences
}