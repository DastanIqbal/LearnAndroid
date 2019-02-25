package com.dastanapps.youtubeiframe

import android.content.Context
import android.graphics.Canvas
import android.graphics.Path
import android.graphics.RectF
import android.view.MotionEvent
import android.widget.FrameLayout

/*class MyRelativeLayout @JvmOverloads constructor(context: Context, attributeSet: AttributeSet? = null, defStyleInt: Int = 0) : RelativeLayout(context, attributeSet, defStyleInt) {*/
class MyRelativeLayout constructor(context: Context) : FrameLayout(context) {
    private val path = Path()
    private var a: Int = context.resources.getDimensionPixelSize(com.dastanapps.testcode.R.dimen.inline_video_player_youtube_logo)

    init {
        isInEditMode
    }

    override fun onInterceptTouchEvent(motionEvent: MotionEvent): Boolean {
        return true
    }

    override fun dispatchDraw(canvas: Canvas) {
        val save = canvas.save()
        canvas.clipPath(this.path)
        super.dispatchDraw(canvas)
        canvas.restoreToCount(save)
    }

    override fun onLayout(z: Boolean, i: Int, i2: Int, i3: Int, i4: Int) {
        for (i5 in 0 until childCount) {
            getChildAt(i5).layout(i, i2 - this.a, i3, this.a + i4)
        }
    }

    override fun onSizeChanged(i: Int, i2: Int, i3: Int, i4: Int) {
        super.onSizeChanged(i, i2, i3, i4)
        this.path.reset()
        this.path.addRoundRect(RectF(0.0f, 0.0f, width.toFloat(), height.toFloat()), 10.0f, 10.0f, Path.Direction.CW)
        this.path.close()
    }

    fun a(a: Int) {
        this.a = a
    }
}