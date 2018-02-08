package com.dastanapps.app

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import com.dastanapps.CameraHelper
import com.dastanapps.camera.Camera1VideoFragment
import com.dastanapps.camera.R
import com.dastanapps.camera2.Camera2VideoFragment

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        if (savedInstanceState == null) {
            if (CameraHelper.isCamera2Supported()) {
                fragmentManager.beginTransaction().replace(R.id.container, Camera2VideoFragment.newInstance()).commit()
            } else {
                fragmentManager.beginTransaction().replace(R.id.container, Camera2VideoFragment.newInstance()).commit()
            }
        }
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus)
            window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    or View.SYSTEM_UI_FLAG_FULLSCREEN
                    or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY)
    }
}
