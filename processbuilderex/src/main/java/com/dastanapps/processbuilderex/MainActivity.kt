package com.dastanapps.processbuilderex

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.Environment
import android.os.IBinder
import android.os.Messenger
import android.support.v7.app.AppCompatActivity
import android.util.Log
import com.dastanapps.MyService
import com.dastanapps.NativeWrapper
import java.io.BufferedReader
import java.io.File
import java.io.IOException
import java.io.InputStreamReader
import java.util.*


class MainActivity : AppCompatActivity() {
    /** Messenger for communicating with the service.  */
    var mService: Messenger? = null

    /** Flag indicating whether we have called bind on the service.  */
    var mBound: Boolean = false

    /**
     * Class for interacting with the main interface of the service.
     */
    private val mConnection = object : ServiceConnection {
        override fun onServiceConnected(className: ComponentName, service: IBinder) {
            // This is called when the connection with the service has been
            // established, giving us the object we can use to
            // interact with the service.  We are communicating with the
            // service using a Messenger, so here we get a client-side
            // representation of that from the raw IBinder object.
            mService = Messenger(service)
            mBound = true
        }

        override fun onServiceDisconnected(className: ComponentName) {
            // This is called when the connection with the service has been
            // unexpectedly disconnected -- that is, its process crashed.
            mService = null
            mBound = false
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
//        val builder = StrictMode.VmPolicy.Builder()
//        StrictMode.setVmPolicy(builder.build())
        //loadFFMPEGLibrary()
        //processBuilder()
        val ffmpegCommand = arrayOf("ffmpeg", "-y", "-i", Environment.getExternalStorageDirectory().path + "/MP4_20170202_183449.mp4", "-filter_complex", "scale=640:640", "-strict", "experimental", filesDir.path + "/outuput.mp4")
        Log.d("DEBUG", NativeWrapper.init(ffmpegCommand))
        //      startService(Intent(this, MyService::class.java))
//        NativeWrapper.a().a(this)
//        NativeWrapper.a().c()
    }

    override fun onStart() {
        super.onStart()
        bindService(Intent(this, MyService::class.java), mConnection,
                Context.BIND_AUTO_CREATE)
    }

    override fun onStop() {
        super.onStop()
        // Unbind from the service
        if (mBound) {
            unbindService(mConnection);
            mBound = false;
        }
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

    private val libraryAssets = arrayOf("ffmpeg")
    private fun loadFFMPEGLibrary() {
        libraryAssets.forEachIndexed { index, s ->
            try {
                val ffmpegInputStream = this.assets.open(libraryAssets[index]);
                val fm = FileMover(ffmpegInputStream, filesDir.absolutePath + libraryAssets[index]);
                fm.moveIt();
            } catch (e: IOException) {
                e.printStackTrace();
            }
        }

        Log.d("DEBUG", "library load finish")
        val process: java.lang.Process
        try {
            val args = arrayOf("/system/bin/chmod", "755", filesDir.absolutePath + "ffmpeg")
            process = ProcessBuilder(*args).start()
            try {
                process.waitFor()
            } catch (e: InterruptedException) {
                e.printStackTrace()
            }
            process.destroy()

        } catch (e: IOException) {
            e.printStackTrace()
        }
        val savePath = File(Environment.getExternalStorageDirectory().path + packageName)
        savePath.mkdirs()
        Log.d("DEBUG", "setting successfully")
        //  processBuilder()
    }
}
