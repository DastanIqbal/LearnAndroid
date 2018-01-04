package com.dastanapps.testframework.test

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Environment
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.widget.TextView
import android.widget.Toast
import java.io.*


class ExternalStorageTest : AppCompatActivity() {
    private val REQUEST_READ_EXTERNAL_STORAGE = 123
    private val REQUEST_WRITE_EXTERNAL_STORAGE = 123
    var textView: TextView? = null

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        textView = TextView(this)
        setContentView(textView)

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                    REQUEST_READ_EXTERNAL_STORAGE)

            ActivityCompat.requestPermissions(this,
                    arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                    REQUEST_WRITE_EXTERNAL_STORAGE)

        } else {
            ReadExternalStorage()
        }


    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        when (requestCode) {
            REQUEST_READ_EXTERNAL_STORAGE -> for (i in permissions.indices) {
                val permission = permissions[i]
                val grantResult = grantResults[i]

                if (permission == Manifest.permission.READ_EXTERNAL_STORAGE || permission == Manifest.permission.WRITE_EXTERNAL_STORAGE) {
                    if (grantResult == PackageManager.PERMISSION_GRANTED) {
                        ReadExternalStorage()
                    } else {
                        Toast.makeText(this@ExternalStorageTest, "Permission Denied", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            else -> super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        }
    }


    fun ReadExternalStorage() {
        val state = Environment.getExternalStorageState()
        if (state != Environment.MEDIA_MOUNTED) {
            textView?.text = "No external storage mounted"
        } else {
            val externalDir = Environment.getExternalStorageDirectory()
            val textFile = File(externalDir.absolutePath
                    + File.separator + "text.txt")
            try {
                writeTextFile(textFile, "This is a test. Roger")
                val text = readTextFile(textFile)
                textView?.text = text
                if (!textFile.delete()) {
                    textView?.text = "Couldn't remove temporary file"
                }
            } catch (e: IOException) {
                textView?.text = "Something went wrong! " + e.message
            }

        }
    }

    @Throws(IOException::class)
    private fun writeTextFile(file: File, text: String) {
        val writer = BufferedWriter(FileWriter(file))
        writer.write(text)
        writer.close()
    }

    @Throws(IOException::class)
    private fun readTextFile(file: File): String {
        val reader = BufferedReader(FileReader(file))
        val text = StringBuilder()
        var line: String? = reader.readLine()
        while (line != null) {
            text.append(line)
            text.append("\n")
            line = reader.readLine()
        }
        reader.close()
        return text.toString()
    }

}
