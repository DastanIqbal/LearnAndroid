package com.dastanapps.customview

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.MotionEvent.ACTION_DOWN
import android.view.MotionEvent.ACTION_MOVE
import android.view.View
import android.widget.FrameLayout


/**
 * Created by dastaniqbal on 19/12/2018.
 * 19/12/2018 3:38
 */
@SuppressLint("ClickableViewAccessibility")
class DraggableView @JvmOverloads constructor(
        context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {
    private val TAG = this::class.java.simpleName
    var dX: Float = 0.toFloat()
    var dY: Float = 0.toFloat()

    private val mTouchListener = object : OnTouchListener {

        override fun onTouch(view: View, event: MotionEvent?): Boolean {
            when (event?.actionMasked) {
                ACTION_DOWN -> {
                    dX = view.x - event.rawX
                    dY = view.y - event.rawY
                }
                ACTION_MOVE -> {
                    view.y = event.rawY + dY
                    view.x = event.rawX + dX
                }
                else -> return false
            }
            return true
        }
    }

    init {
        this.setOnTouchListener(mTouchListener)
    }

}