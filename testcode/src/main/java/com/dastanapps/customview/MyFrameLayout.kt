package com.dastanapps.customview

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.view.View
import android.widget.FrameLayout

/**
 * Created by dastaniqbal on 19/12/2018.
 * 19/12/2018 3:08
 */
class MyFrameLayout @JvmOverloads constructor(
        context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : GestureView(context, attrs, defStyleAttr) {
    private val TAG = this::class.java.simpleName

    override fun getMainView(): View {
        val frameLayout = FrameLayout(context)
        frameLayout.layoutParams = FrameLayout.LayoutParams(50, 50)
        frameLayout.setBackgroundColor(Color.BLACK)
        return frameLayout
    }

}