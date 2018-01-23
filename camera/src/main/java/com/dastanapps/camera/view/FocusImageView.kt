package com.dastanapps.camera.view

import android.content.Context
import android.support.v4.content.ContextCompat
import android.support.v7.widget.AppCompatImageView
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.ScaleAnimation
import android.widget.FrameLayout

import com.dastanapps.camera.R

/**
 * Extends View. Just used to draw Rect when the screen is touched
 * for auto focus.
 *
 *
 * Use setHaveTouch function to set the status and the Rect to be drawn.
 * Call invalidate to draw Rect. Call invalidate again after
 * setHaveTouch(false, Rect(0, 0, 0, 0)) to hide the rectangle.
 */
class FocusImageView @JvmOverloads constructor(private val mContext: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : AppCompatImageView(mContext, attrs, defStyleAttr) {
    private val mAnimation: Animation
    private var mRawX: Int = 0
    private var mRawY: Int = 0

    init {
        mAnimation = ScaleAnimation(2.0f, 1.0f, 2.0f, 1.0f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f)
        mAnimation.duration = 200
        this.visibility = View.VISIBLE
    }

    fun startFocusing(event: MotionEvent?) {
        val width = width
        val height = height
        if (event != null && mRawX.toFloat() != event.x && mRawY.toFloat() != event.y) {
            mRawX = event.x.toInt()
            mRawY = event.y.toInt()
            val margin = ViewGroup.MarginLayoutParams(layoutParams)
            margin.setMargins(mRawX - width / 2, mRawY - height / 2, margin.rightMargin, margin.bottomMargin)
            val layoutParams = FrameLayout.LayoutParams(margin)
            setLayoutParams(layoutParams)
        }
        this.visibility = View.VISIBLE
        this.startAnimation(mAnimation)
        this.background = ContextCompat.getDrawable(mContext, R.drawable.focus)
    }

    fun focusFailed() {
        //this.setBackground(ContextCompat.getDrawable(mContext, R.drawable.focus_failed));
        stopFocus()
    }

    fun focusSuccess() {
        this.visibility = View.VISIBLE
        this.background = ContextCompat.getDrawable(mContext, R.drawable.focus_succeed)
    }

    fun stopFocus() {
        this.visibility = View.INVISIBLE
    }
}