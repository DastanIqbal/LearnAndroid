package com.dastanapps.removeorientationtest

import android.os.Build
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.WindowManager

/**
 * Created by dastaniqbal on 15/02/2018.

 * 15/02/2018 10:52
 */
open class NoOrientationAnimationActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            val rotationAnimation = WindowManager.LayoutParams.ROTATION_ANIMATION_JUMPCUT
            val winParams = window.attributes
            winParams.rotationAnimation = rotationAnimation
            window.attributes = winParams
        }
    }
}