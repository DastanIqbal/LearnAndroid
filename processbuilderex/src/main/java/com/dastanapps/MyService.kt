package com.dastanapps

import android.app.Service
import android.content.Intent
import android.os.*
import android.util.Log
import android.widget.Toast
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.util.*

class MyService : Service() {
    val TAG = this.javaClass.simpleName
    /** Command to the service to display a message  */
    val MSG_SAY_HELLO = 1

    /**
     * Handler of incoming messages from clients.
     */
    internal inner class IncomingHandler : Handler() {
        override fun handleMessage(msg: Message) {
            when (msg.what) {
                MSG_SAY_HELLO -> Toast.makeText(applicationContext, "hello!", Toast.LENGTH_SHORT).show()
                else -> super.handleMessage(msg)
            }
        }
    }

    /**
     * Target we publish for clients to send messages to IncomingHandler.
     */
    val mMessenger = Messenger(IncomingHandler())

    override fun onBind(intent: Intent): IBinder? {
        return mMessenger.binder
    }

    override fun onCreate() {
        super.onCreate()
        Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { _, _ ->
            Log.e(TAG, "EXCEPTION IN FFMPEG SERVICE...............")
        }
        //NativeWrapper.init()
    }

    val ffmpegProcess: java.lang.Process? = null
    fun processBuilder(): Unit? {
        try {

            val ffmpegCommand = arrayOf(filesDir.absolutePath + "ffmpeg", "-y", "-i", Environment.getExternalStorageDirectory().path + "/MP4_20170202_183449.mp4", "-filter_complex", "scale=640:640", "-strict", "experimental", filesDir.path + "/outuput.mp4")
            Log.d("DEBUG", Arrays.toString(ffmpegCommand))
            val ffmpegProcess = ProcessBuilder(*ffmpegCommand).redirectErrorStream(true).start()

            val ffmpegOutStream = ffmpegProcess.outputStream
            val reader = BufferedReader(InputStreamReader(ffmpegProcess.inputStream))

            var line: String? = null
            Log.v("DEBUG", "***Starting FFMPEG***")
            line = reader.readLine()
            while (line != null) {
                Log.v("DEBUG", "***$line***")
                line = reader.readLine()
            }
            Log.v("DEBUG", "***Ending FFMPEG***")

        } catch (e: IOException) {
            e.printStackTrace()
        }


        ffmpegProcess?.destroy()
        return null
    }

    override fun onUnbind(intent: Intent?): Boolean {
        dest()
        return super.onUnbind(intent)
    }

    override fun onDestroy() {
        super.onDestroy()
        dest()
    }

    private fun dest() {
        stopForeground(true)
        stopSelf()
        Process.killProcess(Process.myPid())
    }
}
