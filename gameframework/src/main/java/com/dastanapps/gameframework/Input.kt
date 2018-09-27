package com.dastanapps.gameframework

/**
 * Created by dastaniqbal on 20/06/2017.

 * 20/06/2017 11:54
 */

interface Input {
    class KeyEvent(var type: Int, var keyCode: Int, var keyChar: Char) {

        constructor() : this(-1, -1, Character.MIN_VALUE)

        companion object {
            val KEY_DOWN: Int = 0
            val KEY_UP: Int = 1
        }
    }

    class TouchEvent(var type: Int, var x: Int, var y: Int, var pointer: Int) {
        constructor() : this(-1, -1, -1, -1)

        companion object {
            val TOUCH_DOWN: Int = 0
            val TOUCH_UP: Int = 1
            val TOUCH_DRAGGED: Int = 2
        }
    }

    fun getTouchX(pointer: Int): Int
    fun getTouchY(pointer: Int): Int

    fun isKeyPressed(keyCode: Int): Boolean
    fun isTouchDown(pointer: Int): Boolean

    fun getAccelX(): Float
    fun getAccelY(): Float
    fun getAccelZ(): Float

    fun getKeyEvents(): List<KeyEvent>
    fun getTouchEvents(): List<TouchEvent>

}
