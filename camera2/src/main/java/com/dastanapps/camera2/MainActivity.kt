package com.dastanapps.camera2

import android.app.Activity
import android.app.ActivityManager
import android.content.Context
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.preference.PreferenceManager
import android.util.Log
import android.view.OrientationEventListener
import android.view.Surface
import android.view.ViewGroup
import android.view.WindowManager
import com.dastanapps.camera2.CameraController.CameraUtils
import com.dastanapps.camera2.CameraController.CameraUtils.openSettings
import com.dastanapps.camera2.CameraController.CameraUtils.showPhotoVideoToast
import com.dastanapps.camera2.CameraController.CameraUtils.supports_auto_stabilise
import com.dastanapps.camera2.CameraController.CameraUtils.supports_force_video_4k
import com.dastanapps.camera2.Preview.Preview
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : Activity() {
    private val TAG: String = "DEBUG:MainActivity"
    lateinit var preview: Preview
    lateinit var applicationInterface: MyApplicationInterface
    private var large_heap_memory: Int = 0
    private var orientationEventListener: OrientationEventListener? = null

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
        imv_setting.setOnClickListener { openSettings(preview, this) }

        val activityManager = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        large_heap_memory = activityManager.largeMemoryClass
        if (large_heap_memory >= 128) {
            supports_auto_stabilise = true
        }
        // hack to rule out phones unlikely to have 4K video, so no point even offering the option!
        // both S5 and Note 3 have 128MB standard and 512MB large heap (tested via Samsung RTL), as does Galaxy K Zoom
        // also added the check for having 128MB standard heap, to support modded LG G2, which has 128MB standard, 256MB large - see https://sourceforge.net/p/opencamera/tickets/9/
        if (activityManager.memoryClass >= 128 || activityManager.largeMemoryClass >= 512) {
            supports_force_video_4k = true
        }

        // listen for orientation event change
        orientationEventListener = object : OrientationEventListener(this) {
            override fun onOrientationChanged(orientation: Int) {
                val rotation = windowManager.defaultDisplay.rotation
                var degrees = 0
                when (rotation) {
                    Surface.ROTATION_0 -> degrees = 0
                    Surface.ROTATION_90 -> degrees = 90
                    Surface.ROTATION_180 -> degrees = 180
                    Surface.ROTATION_270 -> degrees = 270
                    else -> {
                    }
                }
                val relative_orientation = (orientation + degrees) % 360
                val ui_rotation = (360 - relative_orientation) % 360
                preview.uiRotation = ui_rotation
                imv_setting.animate().setDuration(200).rotation(ui_rotation.toFloat()).start()
            }
        }
    }

    override fun onSaveInstanceState(state: Bundle) {
        if (MyDebug.LOG)
            Log.d(TAG, "onSaveInstanceState")
        super.onSaveInstanceState(state)
        preview.onSaveInstanceState(state)
        applicationInterface.onSaveInstanceState(state)
    }


    override fun onConfigurationChanged(newConfig: Configuration?) {
        super.onConfigurationChanged(newConfig)
        preview.setCameraDisplayOrientation()
    }

    override fun onPause() {
        super.onPause()
        preview.onPause()
        orientationEventListener?.disable()
    }

    override fun onResume() {
        super.onResume()
        preview.onResume()
        orientationEventListener?.enable()
    }

    override fun onDestroy() {
        super.onDestroy()
        preview.onDestroy()
    }

    internal fun cameraSetup() {
        if (CameraUtils.supportsForceVideo4K() && preview.usingCamera2API()) {
            if (MyDebug.LOG)
                Log.d(TAG, "using Camera2 API, so can disable the force 4K option")
            CameraUtils.disableForceVideo4K()
        }
        if (CameraUtils.supportsForceVideo4K() && preview.videoQualityHander.supportedVideoSizes != null) {
            for (size in preview.videoQualityHander.supportedVideoSizes) {
                if (size.width >= 3840 && size.height >= 2160) {
                    if (MyDebug.LOG)
                        Log.d(TAG, "camera natively supports 4K, so can disable the force option")
                    CameraUtils.disableForceVideo4K()
                }
            }
        }

        showPhotoVideoToast(false, preview, this, applicationInterface)
    }

    fun getStorageUtils(): StorageUtils {
        return this.applicationInterface.getStorageUtils()
    }
}
