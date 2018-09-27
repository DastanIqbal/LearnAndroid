package com.dastanapps.gameframework.impl

import android.content.Context
import android.content.SharedPreferences
import android.content.res.AssetManager
import android.preference.PreferenceManager
import com.dastanapps.gameframework.FileIO
import java.io.*



/**
 * Created by dastaniqbal on 08/10/2017.

 * 08/10/2017 11:48
 */
class AndroidFileIO(val context: Context) : FileIO {
    val assets: AssetManager = context.assets
    var externalStoragePath: String = context.getExternalFilesDir(null).absolutePath + File.separator

    override fun readAssest(fileName: String): InputStream {
        return assets.open(fileName)
    }

    override fun readFile(fileName: String): InputStream {
       return FileInputStream(externalStoragePath+fileName)
    }

    override fun writeFile(fileName: String): OutputStream {
        return FileOutputStream(externalStoragePath+fileName)
    }

    override fun getPreferences(): SharedPreferences {
        return PreferenceManager.getDefaultSharedPreferences(context)
    }
}