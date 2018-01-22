package com.dastanapps.app

import android.os.Build
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.dastanapps.camera.Camera1VideoFragment
import com.dastanapps.camera.R
import com.dastanapps.camera2.Camera2VideoFragment

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        if (savedInstanceState == null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                fragmentManager.beginTransaction().replace(R.id.container, Camera2VideoFragment.newInstance()).commit()
            } else {
                fragmentManager.beginTransaction().replace(R.id.container, Camera1VideoFragment.newInstance()).commit()
            }
        }
    }
}
