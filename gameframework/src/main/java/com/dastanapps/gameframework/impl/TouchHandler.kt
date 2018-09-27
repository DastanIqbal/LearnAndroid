package com.dastanapps.gameframework.impl

import android.view.View
import com.dastanapps.gameframework.Input

/**
 * Created by dastaniqbal on 10/10/2017.

 * 10/10/2017 11:57
 */
interface TouchHandler : View.OnTouchListener {
    fun isTouchDown(pointer: Int): Boolean
    fun getTouchX(pointer: Int): Int
    fun getTouchY(pointer: Int): Int
    fun getTouchEvents(): List<Input.TouchEvent>
}