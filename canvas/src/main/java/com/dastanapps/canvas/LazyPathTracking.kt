package com.dastanapps.canvas

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View

/**
 * TODO: document your custom view class.
 */
class LazyPathTracking : View {

    private val pathList = ArrayList<PointF>()
    val paint = Paint()
    val bitmapList = ArrayList<Bitmap>()

    constructor(context: Context) : super(context)
    constructor(context: Context, attributeSet: AttributeSet) : super(context, attributeSet)

    init {
        paint.color = Color.RED
        paint.strokeWidth = 10f
        paint.style = Paint.Style.STROKE
        paint.strokeJoin = Paint.Join.ROUND
        paint.strokeCap = Paint.Cap.ROUND

        bitmapList.add(BitmapFactory.decodeResource(resources, R.drawable.ic_stat_walk))
        bitmapList.add(BitmapFactory.decodeResource(resources, R.drawable.ic_stat_run))
        bitmapList.add(BitmapFactory.decodeResource(resources, R.drawable.ic_stat_rider))
        bitmapList.add(BitmapFactory.decodeResource(resources, R.drawable.ic_stat_wc))
        setBackgroundColor(Color.BLACK)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        pathList.forEach {
            bitmapList.forEach { bitmap ->
                canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR)
                canvas.drawBitmap(bitmap, it.x, it.y, paint)
                invalidate()
                Thread.sleep(500)
            }
        }
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        val pointX = event?.x
        val pointY = event?.y
        pathList.clear()
        pathList.add(PointF(pointX!!, pointY!!))
        postInvalidate()
        return true
    }
}
