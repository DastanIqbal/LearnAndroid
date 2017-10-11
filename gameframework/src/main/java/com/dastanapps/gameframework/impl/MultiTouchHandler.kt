package com.dastanapps.gameframework.impl

import android.view.MotionEvent
import android.view.View
import com.dastanapps.gameframework.Input
import com.dastanapps.gameframework.Input.TouchEvent
import com.dastanapps.gameframework.Pool


/**
 * Created by dastaniqbal on 11/10/2017.
 * dastanIqbal@marvelmedia.com
 * 11/10/2017 12:16
 */
class MultiTouchHandler(view: View, val scaleX: Float, val scaleY: Float) : TouchHandler {
    override fun isTouchDown(pointer: Int): Boolean {
        synchronized(this) {
            val index = getIndex(pointer)
            if (index < 0 || index >= MAX_TOUCHEVENTS) {
                return false
            }
            return isTouched[index]
        }
    }

    override fun getTouchX(pointer: Int): Int {
        synchronized(this) {
            val index = getIndex(pointer)
            if (index < 0 || index >= MAX_TOUCHEVENTS) {
                return 0
            }
            return touchX[index]
        }
    }

    override fun getTouchY(pointer: Int): Int {
        synchronized(this) {
            val index = getIndex(pointer)
            if (index < 0 || index >= MAX_TOUCHEVENTS) {
                return 0
            }
            return touchY[index]
        }
    }

    override fun getTouchEvents(pointer: Int): List<Input.TouchEvent> {
        synchronized(this) {
            val len = touchEvents.size
            for (i in 0 until len)
                touchEventPool.freeObject(touchEvents[i])
            touchEvents.clear()
            touchEvents.addAll(touchEventsBuffer)
            touchEventsBuffer.clear()
            return touchEvents
        }
    }

    override fun onTouch(v: View?, event: MotionEvent?): Boolean {
        synchronized(this) {
            val action = event?.action?.and(MotionEvent.ACTION_MASK)
            val pointerIndex = event?.action?.and(MotionEvent.ACTION_POINTER_ID_MASK shr MotionEvent.ACTION_POINTER_ID_SHIFT)
            val pointerCount = event?.pointerCount
            var touchEvent: TouchEvent
            for (i in 0 until MAX_TOUCHEVENTS) {
                if (i >= pointerCount!!) {
                    isTouched[i] = false
                    id[i] = -1
                    continue
                }
                val pointerId = event.getPointerId(i)
                if (event.action != MotionEvent.ACTION_MOVE && i != pointerIndex) {
                    continue
                }
                when (action) {
                    MotionEvent.ACTION_DOWN, MotionEvent.ACTION_POINTER_DOWN -> {
                        touchEvent = touchEventPool.newObject()
                        touchEvent.type = TouchEvent.TOUCH_DOWN
                        touchEvent.pointer = pointerId
                        touchX[i] = (event.getX(i) * scaleX).toInt()
                        touchEvent.x = touchX[i]
                        touchY[i] = (event.getY(i) * scaleY).toInt()
                        touchEvent.y = touchY[i]
                        isTouched[i] = true
                        id[i] = pointerId
                        touchEventsBuffer.add(touchEvent)
                    }
                    MotionEvent.ACTION_UP, MotionEvent.ACTION_POINTER_UP, MotionEvent.ACTION_CANCEL -> {
                        touchEvent = touchEventPool.newObject()
                        touchEvent.type = TouchEvent.TOUCH_UP
                        touchEvent.pointer = pointerId
                        touchX[i] = (event.getX(i) * scaleX).toInt()
                        touchEvent.x = touchX[i]
                        touchY[i] = (event.getY(i) * scaleY).toInt()
                        touchEvent.y = touchY[i]
                        isTouched[i] = false
                        id[i] = -1
                        touchEventsBuffer.add(touchEvent)
                    }
                    MotionEvent.ACTION_MOVE -> {
                        touchEvent = touchEventPool.newObject()
                        touchEvent.type = TouchEvent.TOUCH_DRAGGED
                        touchEvent.pointer = pointerId
                        touchX[i] = (event.getX(i) * scaleX).toInt()
                        touchEvent.x = touchX[i]
                        touchY[i] = (event.getY(i) * scaleY).toInt()
                        touchEvent.y = touchY[i]
                        isTouched[i] = true
                        id[i] = pointerId
                        touchEventsBuffer.add(touchEvent)
                    }
                }
            }
            return true
        }
    }

    fun getIndex(pointerId: Int): Int {
        return (0 until MAX_TOUCHEVENTS).firstOrNull { id[it] == pointerId }
                ?: -1
    }

    val factory = object : Pool.PoolObjectFactory<Input.TouchEvent> {
        override fun createObject(): Input.TouchEvent {
            return Input.TouchEvent()
        }

    }
    val MAX_TOUCHEVENTS = 10
    val touchX = IntArray(MAX_TOUCHEVENTS)
    val touchY = IntArray(MAX_TOUCHEVENTS)
    val isTouched = BooleanArray(MAX_TOUCHEVENTS)
    val id = IntArray(MAX_TOUCHEVENTS)
    val touchEventPool = Pool(factory, 100)
    val touchEvents = ArrayList<Input.TouchEvent>()
    val touchEventsBuffer = ArrayList<Input.TouchEvent>()

    init {
        view.setOnTouchListener(this)
    }
}