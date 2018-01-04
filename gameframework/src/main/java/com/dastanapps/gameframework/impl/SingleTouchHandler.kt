package com.dastanapps.gameframework.impl

import android.view.MotionEvent
import android.view.View
import com.dastanapps.gameframework.Input
import com.dastanapps.gameframework.Pool

/**
 * Created by dastaniqbal on 11/10/2017.
 * dastanIqbal@marvelmedia.com
 * 11/10/2017 12:00
 */
class SingleTouchHandler(view: View, private val scaleX: Float, private val scaleY: Float) : TouchHandler {
    override fun getTouchEvents(): List<Input.TouchEvent> {
        synchronized(this) {
            for (i in 0 until touchEvents.size) touchEventPool.freeObject(touchEvents[i])
            touchEvents.clear()
            touchEvents.addAll(touchEventsBuffer)
            touchEventsBuffer.clear()
            return touchEvents
        }
    }

    override fun getTouchX(pointer: Int): Int {
        synchronized(this) {
            return touchX
        }
    }

    override fun getTouchY(pointer: Int): Int {
        synchronized(this) {
            return touchY
        }
    }

    override fun isTouchDown(pointer: Int): Boolean {
        if (pointer == 0)
            return isTouched
        return false
    }

    private var isTouched: Boolean = false
    private val touchX = 0
    private val touchY = 0

    override fun onTouch(v: View, event: MotionEvent): Boolean {
        synchronized(this) {
            val touchEvent = touchEventPool.newObject()
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    touchEvent.type = MotionEvent.ACTION_DOWN
                    isTouched = true
                }
                MotionEvent.ACTION_MOVE -> {
                    touchEvent.type = MotionEvent.ACTION_MOVE
                    isTouched = true
                }
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    touchEvent.type = MotionEvent.ACTION_UP
                    isTouched = false
                }
            }
            touchEvent.x = (event.x.times(scaleX)).toInt()
            touchEvent.y = (event.y.times(scaleY)).toInt()
            touchEventsBuffer.add(touchEvent)
        }
        return true
    }

    private val factory = object : Pool.PoolObjectFactory<Input.TouchEvent> {
        override fun createObject(): Input.TouchEvent {
            return Input.TouchEvent()
        }

    }
    private val touchEvents = ArrayList<Input.TouchEvent>()
    private val touchEventsBuffer = ArrayList<Input.TouchEvent>()
    private val touchEventPool = Pool(factory, 100)

    init {
        view.setOnTouchListener(this)
    }

}