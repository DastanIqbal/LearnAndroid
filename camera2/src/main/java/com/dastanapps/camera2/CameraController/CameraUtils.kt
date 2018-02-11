package com.dastanapps.camera2.CameraController

import android.app.Activity
import android.content.Context
import android.content.pm.ActivityInfo
import android.os.Build
import android.os.Handler
import android.preference.PreferenceManager
import android.util.Log
import android.view.View
import android.view.WindowManager

import com.dastanapps.camera2.MyDebug
import com.dastanapps.camera2.settings.PreferenceKeys

/**
 * Created by dastaniqbal on 10/02/2018.
 * dastanIqbal@marvelmedia.com
 * 10/02/2018 5:31
 */

object CameraUtils {
    internal val TAG = "DEBUG:CameraUtils"

    /**
     * Determine whether we support Camera2 API.
     */
    fun initCamera2Support(context: Context): Boolean {
        if (MyDebug.LOG)
            Log.d(TAG, "initCamera2Support")
        var supports_camera2 = false
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            val manager2 = CameraControllerManager2(context)
            supports_camera2 = true
            if (manager2.numberOfCameras == 0) {
                if (MyDebug.LOG)
                    Log.d(TAG, "Camera2 reports 0 cameras")
                supports_camera2 = false
            }
            var i = 0
            while (i < manager2.numberOfCameras && supports_camera2) {
                if (!manager2.allowCamera2Support(i)) {
                    if (MyDebug.LOG)
                        Log.d(TAG, "camera $i doesn't have limited or full support for Camera2 API")
                    supports_camera2 = false
                }
                i++
            }
        }
        if (MyDebug.LOG)
            Log.d(TAG, "supports_camera2? " + supports_camera2)
        return supports_camera2
    }

    /** Sets the window flags for normal operation (when camera preview is visible).
     */
    fun setWindowFlagsForCamera(activity: Activity) {
        if (MyDebug.LOG)
            Log.d(TAG, "setWindowFlagsForCamera")
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(activity)

        // force to landscape mode
        activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        //setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE); // testing for devices with unusual sensor orientation (e.g., Nexus 5X)
        // keep screen active - see http://stackoverflow.com/questions/2131948/force-screen-on
        if (sharedPreferences.getBoolean(PreferenceKeys.getKeepDisplayOnPreferenceKey(), true)) {
            if (MyDebug.LOG)
                Log.d(TAG, "do keep screen on")
            activity.window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        } else {
            if (MyDebug.LOG)
                Log.d(TAG, "don't keep screen on")
            activity.window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
        if (sharedPreferences.getBoolean(PreferenceKeys.getShowWhenLockedPreferenceKey(), true)) {
            if (MyDebug.LOG)
                Log.d(TAG, "do show when locked")
            // keep Open Camera on top of screen-lock (will still need to unlock when going to gallery or settings)
            activity.window.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED)
        } else {
            if (MyDebug.LOG)
                Log.d(TAG, "don't show when locked")
            activity.window.clearFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED)
        }

        setBrightnessForCamera(false, activity)

        initImmersiveMode(activity)
        //  camera_in_background = false
    }


    fun usingKitKatImmersiveMode(activity: Activity): Boolean {
        // whether we are using a Kit Kat style immersive mode (either hiding GUI, or everything)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(activity)
            val immersive_mode = sharedPreferences.getString(PreferenceKeys.ImmersiveModePreferenceKey, "immersive_mode_low_profile")
            if (immersive_mode == "immersive_mode_gui" || immersive_mode == "immersive_mode_everything")
                return true
        }
        return false
    }

    fun usingKitKatImmersiveModeEverything(activity: Activity): Boolean {
        // whether we are using a Kit Kat style immersive mode for everything
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(activity)
            val immersive_mode = sharedPreferences.getString(PreferenceKeys.ImmersiveModePreferenceKey, "immersive_mode_low_profile")
            if (immersive_mode == "immersive_mode_everything")
                return true
        }
        return false
    }


    private var immersive_timer_handler: Handler? = null
    private var immersive_timer_runnable: Runnable? = null

    private fun setImmersiveTimer(activity: Activity) {
        if (immersive_timer_handler != null && immersive_timer_runnable != null) {
            immersive_timer_handler!!.removeCallbacks(immersive_timer_runnable)
        }
        immersive_timer_handler = Handler()
        immersive_timer_handler!!.postDelayed({
            if (MyDebug.LOG)
                Log.d(TAG, "setImmersiveTimer: run")
            if (/*!camera_in_background && !popupIsOpen() && */usingKitKatImmersiveMode(activity))
                setImmersiveMode(true, activity)
        }, 5000)
    }

    fun initImmersiveMode(activity: Activity) {
        if (!usingKitKatImmersiveMode(activity)) {
            setImmersiveMode(true, activity)
        } else {
            // don't start in immersive mode, only after a timer
            setImmersiveTimer(activity)
        }
    }

    internal fun setImmersiveMode(on: Boolean, activity: Activity) {
        if (MyDebug.LOG)
            Log.d(TAG, "setImmersiveMode: " + on)
        // n.b., preview.setImmersiveMode() is called from onSystemUiVisibilityChange()
        if (on) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT && usingKitKatImmersiveMode(activity)) {
                activity.window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_IMMERSIVE or View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or View.SYSTEM_UI_FLAG_FULLSCREEN
            } else {
                val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(activity)
                val immersive_mode = sharedPreferences.getString(PreferenceKeys.ImmersiveModePreferenceKey, "immersive_mode_low_profile")
                if (immersive_mode == "immersive_mode_low_profile")
                    activity.window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LOW_PROFILE
                else
                    activity.window.decorView.systemUiVisibility = 0
            }
        } else
            activity.window.decorView.systemUiVisibility = 0
    }

    /** Sets the brightness level for normal operation (when camera preview is visible).
     * If force_max is true, this always forces maximum brightness; otherwise this depends on user preference.
     */
    internal fun setBrightnessForCamera(force_max: Boolean, activity: Activity) {
        if (MyDebug.LOG)
            Log.d(TAG, "setBrightnessForCamera")
        // set screen to max brightness - see http://stackoverflow.com/questions/11978042/android-screen-brightness-max-value
        // done here rather than onCreate, so that changing it in preferences takes effect without restarting app
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(activity)
        val layout = activity.window.attributes
        if (force_max || sharedPreferences.getBoolean(PreferenceKeys.getMaxBrightnessPreferenceKey(), true)) {
            layout.screenBrightness = WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_FULL
        } else {
            layout.screenBrightness = WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_NONE
        }

        // this must be called from the ui thread
        // sometimes this method may be called not on UI thread, e.g., Preview.takePhotoWhenFocused->CameraController2.takePicture
        // ->CameraController2.runFakePrecapture->Preview/onFrontScreenTurnOn->MyApplicationInterface.turnFrontScreenFlashOn
        // -> this.setBrightnessForCamera
        activity.runOnUiThread { activity.window.attributes = layout }
    }
}
