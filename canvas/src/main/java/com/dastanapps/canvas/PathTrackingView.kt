package com.dastanapps.canvas

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View

class PathTrackingView : View {

    val path = Path()
    val paint = Paint()

    constructor(context: Context) : super(context)
    constructor(context: Context, attributeSet: AttributeSet) : super(context, attributeSet)

    init {
        paint.color = Color.RED
        paint.strokeWidth = 10f
        paint.style = Paint.Style.STROKE
        paint.strokeJoin = Paint.Join.ROUND
        paint.strokeCap = Paint.Cap.ROUND
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.drawPath(path, paint)
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        val pointX = event?.x
        val pointY = event?.y
        when (event?.action) {
            MotionEvent.ACTION_DOWN -> {
                path.moveTo(pointX!!, pointY!!)
            }
            MotionEvent.ACTION_MOVE -> {
                path.lineTo(pointX!!, pointY!!)
            }
            else -> {
                return false
            }
        }
        postInvalidate()
        return true
    }
}
