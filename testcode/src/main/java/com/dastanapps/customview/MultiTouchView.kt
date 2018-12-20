package com.dastanapps.customview

import android.content.Context
import android.graphics.Matrix
import android.graphics.PointF
import android.util.AttributeSet
import android.util.SparseArray
import android.view.MotionEvent
import android.view.MotionEvent.ACTION_CANCEL
import android.view.ScaleGestureDetector
import android.view.ScaleGestureDetector.SimpleOnScaleGestureListener
import android.widget.FrameLayout
import com.dastanapps.dastanlib.log.Logger


/**
 * Created by dastaniqbal on 19/12/2018.
 * 19/12/2018 3:38
 */
class MultiTouchView @JvmOverloads constructor(
        context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {
    private val TAG = this::class.java.simpleName
    private var scaling = false
    private var oldDist = 1f
    private val NONE = 0
    private val DRAG = 1
    private val ZOOM = 2
    private val currentDragPosition = PointF()
    var mode = NONE

    // Remember some things for zooming
    var start = PointF()
    var mid = PointF()

    private var onScaleMove = false
    private var scale: Float = 1.0f

    // These matrices will be used to move and zoom image
    var savedMatrix = Matrix()
    private val mActivePointers = SparseArray<PointF>()

    var scaleGestureDetector: ScaleGestureDetector = ScaleGestureDetector(context, MyOnScaleGestureListener())
    override fun onTouchEvent(event: MotionEvent): Boolean {
//        val processed = scaleGestureDetector.onTouchEvent(event)
//        Logger.d(TAG, "gesture processed $processed")
        var pointerIndex = event.actionIndex
        var pointerId = event.getPointerId(pointerIndex)

        when (event.action and MotionEvent.ACTION_MASK) {
            MotionEvent.ACTION_DOWN -> {
                scaling = false
                savedMatrix.set(matrix)
                start.set(event.x, event.y)
                currentDragPosition.set(x - event.rawX, y - event.rawY)
                mode = DRAG
                Logger.d(TAG, "ACTION_DOWN $mode")
            }
            MotionEvent.ACTION_POINTER_DOWN -> {
                onScaleMove = true
                if (event.pointerCount >= 2) {
                    oldDist = spacing(event)
                    Logger.d(TAG, "oldDist=$oldDist")
                    if (oldDist > 10f) {
                        savedMatrix.set(matrix)
                        midPoint(mid, event)
                        mode = ZOOM
                        mActivePointers.put(pointerId, getPoint(pointerIndex, event))
                        Logger.d(TAG, "mode=ZOOM")
                    }
                }
            }
            MotionEvent.ACTION_UP, ACTION_CANCEL, MotionEvent.ACTION_POINTER_UP -> {
                mode = NONE
                onScaleMove = false
                mActivePointers.remove(pointerId)
            }
            MotionEvent.ACTION_MOVE -> {
                if (mode == DRAG) {
                    animate()
                            .x(event.rawX + currentDragPosition.x)
                            .y(event.rawY + currentDragPosition.y)
                            .setDuration(0)
                            .start()
//                    matrix.set(savedMatrix)
//                    matrix.postTranslate(event.x - start.x, event.y - start.y)
//                    invalidate()
                    Logger.d(TAG, "ACTION_MOVE DRAG X:${currentDragPosition.x}, Y:${currentDragPosition.y}")
                } else if (onScaleMove && mode == ZOOM && event.pointerCount >= 2) {
                    val newDist = spacing(event)
                    Logger.e(TAG, "newDist=$newDist, oldDist=$oldDist")
//
                    if (newDist > 10f && newDist.toInt() != oldDist.toInt()) {
                        scale = newDist / oldDist
                        scaleX = scale
                        scaleY = scale
//                            matrix.set(savedMatrix)
//                            matrix.postScale(scale, scale, mid.x, mid.y)
                        invalidate()
//                            requestLayout()
                        Logger.d(TAG, "scaled=$scale")//, MotionEvent=$event")
                    }
                }
            }
        }
        return true
    }

    inner class MyOnScaleGestureListener : SimpleOnScaleGestureListener() {

        override fun onScale(detector: ScaleGestureDetector): Boolean {
            val scaleFactor = detector.scaleFactor
            if (scaleFactor > 1) {
                Logger.d(TAG, "Zooming Out $scaleFactor")
            } else {
                Logger.d(TAG, "Zooming In $scaleFactor")
            }
            // if (!detector.isInProgress) {
            scaleX = scaleFactor
            scaleY = scaleFactor
            //    Logger.d(TAG, "Scaled $scaleFactor")
            //    return false
            // }
            return true
        }

        override fun onScaleBegin(detector: ScaleGestureDetector): Boolean {
            Logger.d(TAG, "onScaleBegin")
            return true
        }

        override fun onScaleEnd(detector: ScaleGestureDetector) {
            Logger.d(TAG, "onScaleEnd")
        }
    }

    /**
     * space between the first two fingers
     */
    private fun spacing(event: MotionEvent): Float {
        val x = event.getX(1) - event.getX(0)
        val y = event.getY(1) - event.getY(0)
        return Math.sqrt((x * x + y * y).toDouble()).toFloat()
    }

    private fun spacingPoint(event: MotionEvent): PointF {
        val f = PointF()
        f.x = event.getX(0) - event.getX(1)
        f.y = event.getY(0) - event.getY(1)
        return f
    }

    private fun getPoint(pointerIndex: Int, event: MotionEvent): PointF {
        val f = PointF()
        f.x = event.getX(pointerIndex)
        f.y = event.getY(pointerIndex)
        return f
    }

    /**
     * the mid point of the first two fingers
     */
    private fun midPoint(point: PointF, event: MotionEvent) {
        val x = event.getX(0) + event.getX(1)
        val y = event.getY(0) + event.getY(1)
        point.set(x / 2, y / 2)
    }
}