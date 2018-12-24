package com.dastanapps.customview

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Point
import android.graphics.PointF
import android.graphics.Rect
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.MotionEvent.*
import android.view.View
import android.widget.FrameLayout
import com.dastanapps.dastanlib.log.Logger


/**
 * Created by dastaniqbal on 19/12/2018.
 * 19/12/2018 3:38
 */
@SuppressLint("ClickableViewAccessibility")
class MultiTouchView @JvmOverloads constructor(
        context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {
    private val TAG = this::class.java.simpleName
    private var lastPosX = -1f
    private var lastPosY = -1f
    private val translationPoint = PointF()
    var minWidth = 0
    var minHeight = 0

    private val mTouchListener = object : OnTouchListener {
        var isTwoFinger: Boolean = false
        var hasResetAfterTwoFinger: Boolean = false
        var oldDistance: Float = 0.toFloat()
        var currentViewSize = Point()

        fun save(event: MotionEvent) {
            oldDistance = calculateDistance(event)
            currentViewSize.set(width, height)
        }

        override fun onTouch(v: View?, event: MotionEvent?): Boolean {
            when (event?.actionMasked) {
                ACTION_DOWN -> {
                    save(event)
                    if (!isTwoFinger) {
                        lastPosX = event.rawX
                        lastPosY = event.rawY
                        hasResetAfterTwoFinger = true
                    }
                }
                ACTION_POINTER_DOWN -> {
                    isTwoFinger = true
                    hasResetAfterTwoFinger = false

                    save(event)
                }
                ACTION_MOVE -> {
                    if (isTwoFinger) {
                        val scale = (calculateDistance(event) / oldDistance)

                        val finalWidth = currentViewSize.x * scale
                        val finalHeight = currentViewSize.y * scale
                        if (finalHeight > minHeight && finalWidth > minWidth) {
                            layoutParams.width = finalWidth.toInt()
                            layoutParams.height = finalHeight.toInt()
                        }

                        postInvalidate()
                        requestLayout()
                    } else if (hasResetAfterTwoFinger) {
                        setTranslation(event)
                    }
                }
                ACTION_POINTER_UP -> {
                    Logger.d(TAG, "sticker view action pointer up")
                    isTwoFinger = false
                }
                ACTION_UP -> {
                    val bounds = Rect()
                    getGlobalVisibleRect(bounds)
                }
            }
            return true
        }
    }

    init {
        this.setOnTouchListener(mTouchListener)
        this.addOnLayoutChangeListener { v, left, top, right, bottom, oldLeft, oldTop, oldRight, oldBottom ->
            updateTranslation()
        }
    }

    private fun updateTranslation() {
        translationPoint.set(x, y)
    }

    private fun convertDpToPixel(dp: Float, context: Context): Int {
        val resources = context.resources
        val metrics = resources.displayMetrics
        val px = dp * (metrics.densityDpi / 160f)
        return px.toInt()
    }

    private fun calculateDistance(event: MotionEvent?): Float {
        if (event == null || event.pointerCount < 2) {
            return 0f
        }
        val firstPointer = getRawPoint(event, 0)
        val secondPointer = getRawPoint(event, 1)
        return calculateDistance(firstPointer.x, firstPointer.y, secondPointer.x, secondPointer.y)
    }

    private fun calculateDistance(x1: Float, y1: Float, x2: Float, y2: Float): Float {
        val x = (x1 - x2).toDouble()
        val y = (y1 - y2).toDouble()

        return Math.sqrt(x * x + y * y).toFloat()
    }

    private fun getCenterX(targetX: Float): Float {
        return targetX + width / 2f
    }

    private fun getCenterY(targetY: Float): Float {
        return targetY + height / 2f
    }

    private fun setTranslation(event: MotionEvent) {
        val offsetX = event.rawX - lastPosX
        val offsetY = event.rawY - lastPosY
        translationPoint.x += offsetX
        translationPoint.y += offsetY
        var targetX = translationPoint.x
        var targetY = translationPoint.y
        val targetCenterX = getCenterX(targetX)
        val targetCenterY = getCenterY(targetY)

        val range = convertDpToPixel(10f, context).toFloat()
        val parentCenterPoint = getParentCenterPoint()
        val minX = parentCenterPoint.x - range
        val maxX = parentCenterPoint.x + range
        val minY = parentCenterPoint.y - range
        val maxY = parentCenterPoint.y + range

        val shouldSnapX = targetCenterX in minX..maxX
        val shouldSnapY = targetCenterY in minY..maxY
        val shouldSnapAll = shouldSnapX && shouldSnapY
        when {
            shouldSnapAll -> {
                targetX = getX(parentCenterPoint.x)
                targetY = getY(parentCenterPoint.y)
            }
            shouldSnapX -> targetX = getX(parentCenterPoint.x)
            shouldSnapY -> targetY = getY(parentCenterPoint.y)
            else -> {
            }
        }
        x = targetX
        y = targetY

        lastPosX = event.rawX
        lastPosY = event.rawY
    }

    private fun getX(centerX: Float): Float {
        return centerX - width / 2f
    }

    private fun getY(centerY: Float): Float {
        return centerY - height / 2f
    }

    private fun getParentCenterPoint(): PointF {
        val parent = parent as View
        val parentCenterX = parent.width / 2f
        val parentCenterY = parent.height / 2f
        return PointF(parentCenterX, parentCenterY)
    }

    private fun getRawPoint(ev: MotionEvent, index: Int): PointF {
        val point = PointF()
        val location = intArrayOf(0, 0)
        getLocationOnScreen(location)

        var x = ev.getX(index)
        var y = ev.getY(index)

        var angle = Math.toDegrees(Math.atan2(y.toDouble(), x.toDouble()))
        angle += rotation.toDouble()

        val length = PointF.length(x, y)

        x = (length * Math.cos(Math.toRadians(angle))).toFloat() + location[0]
        y = (length * Math.sin(Math.toRadians(angle))).toFloat() + location[1]

        point.set(x, y)
        return point
    }
}