package com.dastanapps.testframework.test

import android.os.Bundle
import android.view.Window
import android.view.WindowManager

/**
 * Created by dastaniqbal on 05/07/2017.

 * 05/07/2017 11:48
 */
class FullScreenTest : SingleTouchTest() {
    override fun onCreate(savedInstanceState: Bundle?) {
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN)
        super.onCreate(savedInstanceState)
    }
}