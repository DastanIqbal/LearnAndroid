package com.dastanapps.testframework.test

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.MotionEvent
import android.view.View
import android.widget.TextView

/**
 * Created by dastaniqbal on 27/06/2017.

 * 27/06/2017 12:48
 */
class MultiTouchTest : AppCompatActivity(), View.OnTouchListener {
    var stringBuilder = StringBuilder()
    var textView: TextView? = null
    var x = FloatArray(10)
    var y = FloatArray(10)
    var touched = BooleanArray(10)
    var id = IntArray(10)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        textView = TextView(this)
        textView?.text = "Touch and drag (one finger only)!"
        textView?.setOnTouchListener(this)
        setContentView(textView)
        repeat(10) { i -> id[i] = -1 }
        updateTextView()
    }

    private fun updateTextView() {
        stringBuilder.setLength(0)
        repeat(10) { i ->
            stringBuilder.append(touched[i]).append(", ")
            stringBuilder.append(id[i]).append(", ")
            stringBuilder.append(x[i]).append(", ").append(y[i]).append("\n")
        }
        textView?.text = stringBuilder.toString()
    }

    override fun onTouch(v: View, event: MotionEvent): Boolean {
        val action = event.action and MotionEvent.ACTION_MASK
        val pointerIndex = event.action and MotionEvent.ACTION_POINTER_INDEX_MASK shr MotionEvent.ACTION_POINTER_INDEX_SHIFT
        val pointerCount = event.pointerCount
        for (i in 0..9) {
            if (i >= pointerCount) {
                touched[i] = false
                id [i] = -1
                continue
            }
            if (event.action != MotionEvent.ACTION_MOVE && i != pointerIndex) continue
            val pointerId = event.getPointerId(i)
            when (action) {
                MotionEvent.ACTION_DOWN, MotionEvent.ACTION_POINTER_DOWN -> {
                    touched[i] = true
                    id[i] = pointerId
                    x[i] = event.getX(i)
                    y[i] = event.getY(i)
                }
                MotionEvent.ACTION_UP, MotionEvent.ACTION_POINTER_UP,
                MotionEvent.ACTION_OUTSIDE, MotionEvent.ACTION_CANCEL -> {
                    touched[i] = false
                    id[i] = -1
                    x[i] = event.getX(i)
                    y[i] = event.getY(i)
                }
                MotionEvent.ACTION_MOVE -> {
                    touched[i] = true
                    id[i] = pointerId
                    x[i] = event.getX(i)
                    y[i] = event.getY(i)
                }
            }
        }
        updateTextView()
        return true
    }
}