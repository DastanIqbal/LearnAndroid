package com.dastanapps.testframework.test

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.widget.TextView

/**
 * Created by dastaniqbal on 27/06/2017.
 * dastanIqbal@marvelmedia.com
 * 27/06/2017 12:20
 */
class LifeCycleTest : AppCompatActivity() {
    var stringBuilder: StringBuilder = StringBuilder()
    var textView: TextView? = null

    fun log(text: String) {
        Log.d("LifeCycleTest", text)
        stringBuilder.append(text)
        stringBuilder.append("\n")
        textView?.text = stringBuilder.toString()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        textView = TextView(this)
        log("Creating")
        textView?.text = stringBuilder.toString()
        setContentView(textView)
        log("Created")
    }

    override fun onStart() {
        super.onStart()
        log("Started")
    }

    override fun onResume() {
        super.onResume()
        log("Resumed")
    }

    override fun onPause() {
        super.onPause()
        log("Paused")

        if (isFinishing) {
            log("Finishing")
        }
    }

    override fun onRestart() {
        super.onRestart()
        log("Restarted")
    }

    override fun onStop() {
        super.onStop()
        log("Stopped")
    }

    override fun onDestroy() {
        super.onDestroy()
        log("Destroyed")
    }
}