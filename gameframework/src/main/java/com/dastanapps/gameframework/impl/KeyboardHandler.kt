package com.dastanapps.gameframework.impl

import android.view.KeyEvent
import android.view.View
import com.dastanapps.gameframework.Input
import com.dastanapps.gameframework.Pool
import com.dastanapps.gameframework.Pool.PoolObjectFactory


/**
 * Created by dastaniqbal on 10/10/2017.
 * dastanIqbal@marvelmedia.com
 * 10/10/2017 11:12
 */
class KeyboardHandler(val view: View) : View.OnKeyListener {
    var factory: PoolObjectFactory<Input.KeyEvent> = object : PoolObjectFactory<Input.KeyEvent> {
        override fun createObject(): Input.KeyEvent {
            return Input.KeyEvent()
        }
    }

    val pressedKeys = BooleanArray(128)
    val keyEventPool = Pool(factory, 100)
    val keyEvents = ArrayList<Input.KeyEvent>()
    val keyEventsBuffer = ArrayList<Input.KeyEvent>()

    init {
        view.setOnKeyListener(this)
        view.isFocusableInTouchMode = true
        view.requestFocus()
    }

    override fun onKey(v: View?, keyCode: Int, event: KeyEvent?): Boolean {
        if (event?.action == KeyEvent.ACTION_MULTIPLE)
            return false
        synchronized(this) {
            val keyEvent = keyEventPool.newObject()
            keyEvent.keyCode = keyCode
            keyEvent.keyChar = event?.unicodeChar?.toChar()!!
            if (event.action == KeyEvent.ACTION_DOWN) {
                keyEvent.type = KeyEvent.ACTION_DOWN
                if (keyCode in 1..126) {
                    pressedKeys[keyCode] = true
                }
            }
            if (event.action == KeyEvent.ACTION_UP) {
                keyEvent.type = KeyEvent.ACTION_UP
                if (keyCode in 1..126) {
                    pressedKeys[keyCode] = false
                }
            }
            keyEventsBuffer.add(keyEvent)
        }
        return false
    }

    fun isKeyPressed(keyCode: Int): Boolean {
        if (keyCode < 0 || keyCode > 127) {
            return pressedKeys[keyCode]
        }
        return false
    }

    fun getKeyEvents(): List<Input.KeyEvent> {
        synchronized(this) {
            for (index in 0 until keyEvents.size) {
                keyEventPool.freeObject(keyEvents[index])
            }
            keyEvents.clear()
            keyEvents.addAll(keyEventsBuffer)
            keyEventsBuffer.clear()
            return keyEvents
        }
    }
}