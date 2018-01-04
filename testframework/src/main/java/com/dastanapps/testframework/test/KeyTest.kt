package com.dastanapps.testframework.test

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.KeyEvent
import android.view.KeyEvent.ACTION_DOWN
import android.view.KeyEvent.ACTION_UP
import android.view.View
import android.widget.TextView
import java.lang.StringBuilder

/**
 * Created by dastaniqbal on 29/06/2017.
 * dastanIqbal@marvelmedia.com
 * 29/06/2017 6:53
 */
class KeyTest : AppCompatActivity(), View.OnKeyListener {
    var stringBuilder = StringBuilder()
    var textView: TextView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        textView = TextView(this)
        textView?.text = "Press Key (if you have some!)"
        textView?.setOnKeyListener(this)
        textView?.isFocusableInTouchMode = true
        setContentView(textView)
    }

    override fun onKey(v: View, keyCode: Int, event: KeyEvent): Boolean {
        stringBuilder.setLength(0)
        when (event.action) {
            ACTION_UP -> stringBuilder.append("up, ")
            ACTION_DOWN -> stringBuilder.append("down, ")
        }

        stringBuilder.append(event.keyCode).append(", ").append(event.unicodeChar)
        textView?.text = stringBuilder.toString()
        return event.keyCode != KeyEvent.KEYCODE_BACK
    }
}