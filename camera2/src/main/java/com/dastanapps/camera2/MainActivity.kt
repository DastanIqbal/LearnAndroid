package com.dastanapps.camera2

import android.app.Activity
import android.app.ActivityManager
import android.content.Context
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.preference.PreferenceManager
import android.util.Log
import android.view.*
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.RelativeLayout
import com.dastanapps.camera2.CameraController.CameraUtils
import com.dastanapps.camera2.CameraController.CameraUtils.setDeviceDefaults
import com.dastanapps.camera2.CameraController.CameraUtils.setWindowFlagsForCamera
import com.dastanapps.camera2.CameraController.CameraUtils.showPhotoVideoToast
import com.dastanapps.camera2.CameraController.CameraUtils.supports_auto_stabilise
import com.dastanapps.camera2.CameraController.CameraUtils.supports_force_video_4k
import com.dastanapps.camera2.Preview.Preview
import com.dastanapps.camera2.settings.MyPreferenceFragment
import com.dastanapps.camera2.settings.PreferenceKeys
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*

class MainActivity : Activity() {
    private val TAG: String = "DEBUG:MainActivity"
    lateinit var preview: Preview
    lateinit var applicationInterface: MyApplicationInterface
    private var large_heap_memory: Int = 0
    private var orientationEventListener: OrientationEventListener? = null
    private var current_orientation: Int = 0
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
        preview.updateFocus("focus_mode_auto", false, true)

        PreferenceManager.setDefaultValues(this, R.xml.preferences, false) // initialise any unset preferences to their default values

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
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
        val has_done_first_time = sharedPreferences.contains(PreferenceKeys.FirstTimePreferenceKey)
        if (MyDebug.LOG)
            Log.d(TAG, "has_done_first_time: $has_done_first_time")
        if (!has_done_first_time) {
            setDeviceDefaults(this)
            setFirstTimeFlag()
        }

        // listen for orientation event change
        orientationEventListener = object : OrientationEventListener(this) {
            override fun onOrientationChanged(orientation: Int) {
                if (orientation == OrientationEventListener.ORIENTATION_UNKNOWN)
                    return
                var orientation = orientation;
                var diff = Math.abs(orientation - current_orientation)
                if (diff > 180)
                    diff = 360 - diff
                // only change orientation when sufficiently changed
                if (diff > 60) {
                    orientation = (orientation + 45) / 90 * 90
                    orientation = orientation % 360
                    if (orientation != current_orientation) {
                        current_orientation = orientation
                        if (MyDebug.LOG) {
                            Log.d(TAG, "current_orientation is now: " + current_orientation)
                        }
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

                        reLayoutUI()
                    }
                }
            }
        }



        switch_camera.setOnClickListener { v -> clickedSwitchCamera(v) }
        exp.setOnClickListener { v -> clickedExposure(v) }
        flash.setOnClickListener { v -> clickedFlash(v) }
        wb.setOnClickListener { v -> clickedWhiteBalance(v) }
        imv_play.setOnClickListener { v -> clickedRecordVideo(v) }
        filters.setOnClickListener { v-> clickedFilters(v) }
    }

    private fun setFirstTimeFlag() {
        if (MyDebug.LOG)
            Log.d(TAG, "setFirstTimeFlag")
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
        val editor = sharedPreferences.edit()
        editor.putBoolean(PreferenceKeys.FirstTimePreferenceKey, true)
        editor.apply()
    }

    fun reLayoutUI() {
        var view = imv_play
        var layoutParams = view.layoutParams as RelativeLayout.LayoutParams
        layoutParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT, 0)
        layoutParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, RelativeLayout.TRUE)
        view.layoutParams = layoutParams
        setViewRotation(view, preview.uiRotation.toFloat())

        view = imv_setting
        layoutParams = view.layoutParams as RelativeLayout.LayoutParams
        layoutParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT, 0)
        layoutParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, RelativeLayout.TRUE)
        view.layoutParams = layoutParams
        setViewRotation(view, preview.uiRotation.toFloat())
    }

    /** Similar view.setRotation(ui_rotation), but achieves this via an animation.
     */
    private fun setViewRotation(view: View, ui_rotation: Float) {
        //view.setRotation(ui_rotation);
        var rotate_by = ui_rotation - view.rotation
        if (rotate_by > 181.0f)
            rotate_by -= 360.0f
        else if (rotate_by < -181.0f)
            rotate_by += 360.0f
        // view.animate() modifies the view's rotation attribute, so it ends up equivalent to view.setRotation()
        // we use rotationBy() instead of rotation(), so we get the minimal rotation for clockwise vs anti-clockwise
        view.animate().rotationBy(rotate_by).setDuration(100).setInterpolator(AccelerateDecelerateInterpolator()).start()
    }

    override fun onSaveInstanceState(state: Bundle) {
        if (MyDebug.LOG)
            Log.d(TAG, "onSaveInstanceState")
        super.onSaveInstanceState(state)
        preview.onSaveInstanceState(state)
        applicationInterface.onSaveInstanceState(state)
    }


    override fun onConfigurationChanged(newConfig: Configuration?) {
        preview.setCameraDisplayOrientation()
        super.onConfigurationChanged(newConfig)
    }

    override fun onPause() {
        super.onPause()
        preview.onPause()
        orientationEventListener?.disable()
    }

    override fun onResume() {
        super.onResume()
        reLayoutUI()
        preview.onResume()
        orientationEventListener?.enable()
    }

    override fun onDestroy() {
        super.onDestroy()
        preview.onDestroy()
    }

    private fun getPreferenceFragment(): MyPreferenceFragment {
        return fragmentManager.findFragmentByTag("PREFERENCE_FRAGMENT") as MyPreferenceFragment
    }

    override fun onBackPressed() {
        val fragment = getPreferenceFragment()
        if (fragment != null) {
            if (MyDebug.LOG)
                Log.d(TAG, "close settings")
            setWindowFlagsForCamera(this)
            //showPreview(true)

            CameraUtils.stopListeningPreferenceFragment(this)
        } else {
        }
        super.onBackPressed()
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

    var isRecording = false
    fun clickedRecordVideo(view: View) {
        isRecording = if (isRecording) {
            preview.stopRecording()
            false
        } else {
            preview.startRecording()
            true
        }
        //preview.takePicturePressed(false)
    }

    fun clickedOpenSettings(view: View) {
        CameraUtils.openSettings(preview, this)
    }

    var supported_flash_values = ArrayList<String>()
    fun loadFlashValues() {
        supported_flash_values.clear()
        supported_flash_values = preview.supportedFlashValues as ArrayList<String>
        if (preview.isVideo && supported_flash_values != null) {
            // filter flash modes we don't want to show
            val filter = ArrayList<String>()
            for (flash_value in supported_flash_values) {
                if (Preview.isFlashSupportedForVideo(flash_value))
                    filter.add(flash_value)
            }
            supported_flash_values = filter
        }
    }

    fun clickedFlash(view: View) {
        if (supported_flash_values.size == 0) {
            loadFlashValues()
        }
        val n = (Math.random() * supported_flash_values.size).toInt()
        preview.updateFlash(supported_flash_values[n])
    }

    var exposure = -3;
    fun clickedExposure(view: View) {
        preview.setExposure(exposure)
        exposure += 1
        if (exposure > preview.maximumExposure) {
            exposure = preview.minimumExposure;
        }
    }

    fun clickedFilters(view: View){
        preview.changeFilter()
    }

    /* Returns the cameraId that the "Switch camera" button will switch to.
     */
    fun getNextCameraId(): Int {
        if (MyDebug.LOG)
            Log.d(TAG, "getNextCameraId")
        var cameraId = preview.cameraId
        if (MyDebug.LOG)
            Log.d(TAG, "current cameraId: " + cameraId)
        if (this.preview.canSwitchCamera()) {
            val n_cameras = preview.cameraControllerManager.numberOfCameras
            cameraId = (cameraId + 1) % n_cameras
        }
        if (MyDebug.LOG)
            Log.d(TAG, "next cameraId: " + cameraId)
        return cameraId
    }

    fun clickedSwitchCamera(view: View) {
        if (MyDebug.LOG)
            Log.d(TAG, "clickedSwitchCamera")
        if (preview.isOpeningCamera) {
            if (MyDebug.LOG)
                Log.d(TAG, "already opening camera in background thread")
            return
        }
        if (this.preview.canSwitchCamera()) {
            val cameraId = getNextCameraId()
            this.preview.setCamera(cameraId)
        }
    }

    var supported_white_balances = ArrayList<String>()
    fun loadWhiteBalance() {
        supported_white_balances = preview.supportedWhiteBalances as ArrayList<String>
        if (supported_white_balances != null) {
            val supported_white_balances_entries = ArrayList<String>()
            supported_white_balances
                    .map { CameraUtils.getEntryForWhiteBalance(this, it) }
                    .filterNot { it.isEmpty() }
                    .forEach { supported_white_balances_entries.add(it) }
        }
    }

    fun clickedWhiteBalance(view: View) {
        if (supported_white_balances.size == 0) {
            loadWhiteBalance()
        }
        if (preview.cameraController != null) {
            val n = (Math.random() * supported_white_balances.size).toInt()
            preview.cameraController.whiteBalance = supported_white_balances[n]
        }
    }
}
