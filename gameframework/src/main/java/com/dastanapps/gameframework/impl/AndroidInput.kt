package com.dastanapps.gameframework.impl

import android.content.Context
import android.view.View
import com.dastanapps.gameframework.Input

/**
 * Created by dastaniqbal on 11/10/2017.
 * dastanIqbal@marvelmedia.com
 * 11/10/2017 11:52
 */
class AndroidInput(val context: Context, val view: View, val scaleX: Float, val scaleY: Float) : Input {
    val acceleroMeterHandler = AccelerometerHandler(context)
    val keyboardHandler = KeyboardHandler(view)
    val touchHandler: TouchHandler = MultiTouchHandler(view, scaleX, scaleY)

    override fun getTouchX(pointer: Int): Int {
        return touchHandler.getTouchX(pointer)
    }

    override fun getTouchY(pointer: Int): Int {
        return touchHandler.getTouchY(pointer)
    }

    override fun isKeyPressed(keyCode: Int): Boolean {
        return keyboardHandler.isKeyPressed(keyCode)
    }

    override fun isTouchDown(pointer: Int): Boolean {
        return touchHandler.isTouchDown(pointer)
    }

    override fun getAccelX(): Float {
        return acceleroMeterHandler.accX
    }

    override fun getAccelY(): Float {
        return acceleroMeterHandler.accY
    }

    override fun getAccelZ(): Float {
        return acceleroMeterHandler.accZ
    }

    override fun getKeyEvents(): List<Input.KeyEvent> {
        return keyboardHandler.getKeyEvents()
    }

    override fun getTouchEvents(): List<Input.TouchEvent> {
        return touchHandler.getTouchEvents()
    }
}