package com.dastanapps.testframework.test

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.widget.TextView

/**
 * Created by dastaniqbal on 27/06/2017.
 * dastanIqbal@marvelmedia.com
 * 27/06/2017 12:48
 */

class SingleTouchTest : AppCompatActivity(), View.OnTouchListener {
    var stringBuilder = StringBuilder()
    var textView: TextView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        textView = TextView(this)
        textView?.text = "Touch and drag (one finger only)!"
        textView?.setOnTouchListener(this)
        setContentView(textView)
    }

    override fun onTouch(v: View, event: MotionEvent): Boolean {
        stringBuilder.setLength(0)
        when (event.action) {
            MotionEvent.ACTION_UP -> stringBuilder.append("Up, ")
            MotionEvent.ACTION_DOWN -> stringBuilder.append("Down, ")
            MotionEvent.ACTION_MOVE -> stringBuilder.append("Move, ")
            MotionEvent.ACTION_CANCEL -> stringBuilder.append("Cancel, ")
        }

        stringBuilder.append(event.x).append(",").append(event.y)
        Log.d("SingleTouchTest", stringBuilder.toString())
        textView?.text = stringBuilder.toString()

        return true
    }
}
