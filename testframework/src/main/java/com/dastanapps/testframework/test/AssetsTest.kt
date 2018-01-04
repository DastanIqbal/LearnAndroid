package com.dastanapps.testframework.test

import android.content.res.AssetManager
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.widget.TextView
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader

/**
 * Created by dastaniqbal on 04/07/2017.
 * dastanIqbal@marvelmedia.com
 * 04/07/2017 10:57
 */
class AssetsTest : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val textView = TextView(this)
        setContentView(textView)
        val assetMgr = assets as AssetManager
        val inputStream = assetMgr.open("awesome.txt")
        val text = loadText(inputStream);
        textView.text = text
    }

    @Throws(IOException::class)
    private fun loadText(inputStream: InputStream): String {
        val stringBuilder = StringBuilder()
        val bufReader = BufferedReader(InputStreamReader(inputStream))
        var line: String? = bufReader.readLine()
        while (line != null) {
            stringBuilder.append(line).append("\n")
            line=bufReader.readLine()
        }
        bufReader.close()
        return stringBuilder.toString();
    }
}