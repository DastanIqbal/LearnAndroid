package com.dastanapps.gameframework

/**
 * Created by dastaniqbal on 20/06/2017.
 * dastanIqbal@marvelmedia.com
 * 20/06/2017 11:54
 */

interface Input {
    class KeyEvent(val type: Int, val keyCode: Int, val keyChar: Char) {
        companion object {
            val KEY_DOWN: Int = 0
            val KEY_UP: Int = 1
        }
    }

    class TouchEvent(val type: Int, val x: Int, val y: Int, val pointer: Int) {
        companion object {
            val TOUCH_DOWN: Int = 0
            val TOUCH_UP: Int = 1
            val TOUCH_DRAGGED: Int = 2
        }
    }

    fun getTouchX(pointer: Int): Int
    fun getTouchY(pointer: Int): Int

    fun isKeyPressed(keyCode: Boolean): Boolean
    fun isTouchDown(keyCode: Boolean): Boolean

    fun getAccelX(): Float
    fun getAccelY(): Float
    fun getAccelZ(): Float

    fun getKeyEvents(): List<KeyEvent>
    fun getTouchEvents(): List<TouchEvent>

}
