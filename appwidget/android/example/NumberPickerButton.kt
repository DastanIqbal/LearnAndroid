package com.android.example

import android.content.Context
import android.util.AttributeSet
import android.view.KeyEvent
import android.view.MotionEvent
import androidx.appcompat.widget.AppCompatImageButton
import com.dastanapps.appwidget.R

/* loaded from: classes.dex */
class NumberPickerButton : AppCompatImageButton {
    private var mNumberPicker: NumberPicker? = null

    constructor(context: Context?, attrs: AttributeSet?, defStyle: Int) : super(
        context,
        attrs,
        defStyle
    )

    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)

    constructor(context: Context?) : super(context)

    fun setNumberPicker(picker: NumberPicker?) {
        this.mNumberPicker = picker
    }

    // android.view.View
    override fun onTouchEvent(event: MotionEvent): Boolean {
        cancelLongpressIfRequired(event)
        return super.onTouchEvent(event)
    }

    // android.view.View
    override fun onTrackballEvent(event: MotionEvent): Boolean {
        cancelLongpressIfRequired(event)
        return super.onTrackballEvent(event)
    }

    // android.view.View, android.view.KeyEvent.Callback
    override fun onKeyUp(keyCode: Int, event: KeyEvent): Boolean {
        if (keyCode == 23 || keyCode == 66) {
            cancelLongpress()
        }
        return super.onKeyUp(keyCode, event)
    }

    private fun cancelLongpressIfRequired(event: MotionEvent) {
        if (event.getAction() == 3 || event.getAction() == 1) {
            cancelLongpress()
        }
    }

    private fun cancelLongpress() {
        if (R.id.increment === getId()) {
            mNumberPicker!!.cancelIncrement()
        } else if (R.id.decrement === getId()) {
            mNumberPicker!!.cancelDecrement()
        }
    }
}
