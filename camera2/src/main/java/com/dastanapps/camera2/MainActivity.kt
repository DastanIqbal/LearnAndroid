package com.dastanapps.camera2

import android.app.Activity
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.preference.PreferenceManager
import android.view.ViewGroup
import android.view.WindowManager
import com.dastanapps.camera2.Preview.Preview

class MainActivity : Activity() {
    private val TAG: String = "DEBUG:MainActivity"
    lateinit var preview: Preview
    private var applicationInterface: MyApplicationInterface? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            // don't show orientation animations
            val layout = window.attributes
            layout.rotationAnimation = WindowManager.LayoutParams.ROTATION_ANIMATION_CROSSFADE
            window.attributes = layout
        }
        setContentView(R.layout.activity_main)
        applicationInterface = MyApplicationInterface(this, savedInstanceState)
        preview = Preview(applicationInterface, this.findViewById(R.id.preview) as ViewGroup)
        PreferenceManager.setDefaultValues(this, R.xml.preferences, false) // initialise any unset preferences to their default values
    }

    override fun onSaveInstanceState(outState: Bundle?) {
        super.onSaveInstanceState(outState)
        preview.onSaveInstanceState(outState)
    }

    override fun onConfigurationChanged(newConfig: Configuration?) {
        super.onConfigurationChanged(newConfig)
        preview.setCameraDisplayOrientation()
    }

    override fun onPause() {
        super.onPause()
        preview.onPause()
    }

    override fun onResume() {
        super.onResume()
        preview.onResume()
    }

    override fun onDestroy() {
        super.onDestroy()
        preview.onDestroy()
    }

    internal fun cameraSetup() {
    }
}
