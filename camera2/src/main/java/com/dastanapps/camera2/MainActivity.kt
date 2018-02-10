package com.dastanapps.camera2

import android.app.Activity
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.preference.PreferenceManager
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import com.dastanapps.camera2.Preview.Preview
import com.dastanapps.camera2.settings.PreferenceKeys

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

        // set up window flags for normal operation
        setWindowFlagsForCamera()
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

    /** Sets the window flags for normal operation (when camera preview is visible).
     */
    fun setWindowFlagsForCamera() {
        if (MyDebug.LOG)
            Log.d(TAG, "setWindowFlagsForCamera")
        /*{
    		Intent intent = new Intent(this, MyWidgetProvider.class);
    		intent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
    		AppWidgetManager widgetManager = AppWidgetManager.getInstance(this);
    		ComponentName widgetComponent = new ComponentName(this, MyWidgetProvider.class);
    		int[] widgetIds = widgetManager.getAppWidgetIds(widgetComponent);
    		intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, widgetIds);
    		sendBroadcast(intent);
    	}*/
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)

        // force to landscape mode
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        //setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE); // testing for devices with unusual sensor orientation (e.g., Nexus 5X)
        // keep screen active - see http://stackoverflow.com/questions/2131948/force-screen-on
        if (sharedPreferences.getBoolean(PreferenceKeys.getKeepDisplayOnPreferenceKey(), true)) {
            if (MyDebug.LOG)
                Log.d(TAG, "do keep screen on")
            window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        } else {
            if (MyDebug.LOG)
                Log.d(TAG, "don't keep screen on")
            window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
        if (sharedPreferences.getBoolean(PreferenceKeys.getShowWhenLockedPreferenceKey(), true)) {
            if (MyDebug.LOG)
                Log.d(TAG, "do show when locked")
            // keep Open Camera on top of screen-lock (will still need to unlock when going to gallery or settings)
            window.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED)
        } else {
            if (MyDebug.LOG)
                Log.d(TAG, "don't show when locked")
            window.clearFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED)
        }

        setBrightnessForCamera(false)

        initImmersiveMode()
        //  camera_in_background = false
    }


    fun usingKitKatImmersiveMode(): Boolean {
        // whether we are using a Kit Kat style immersive mode (either hiding GUI, or everything)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
            val immersive_mode = sharedPreferences.getString(PreferenceKeys.ImmersiveModePreferenceKey, "immersive_mode_low_profile")
            if (immersive_mode == "immersive_mode_gui" || immersive_mode == "immersive_mode_everything")
                return true
        }
        return false
    }

    fun usingKitKatImmersiveModeEverything(): Boolean {
        // whether we are using a Kit Kat style immersive mode for everything
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
            val immersive_mode = sharedPreferences.getString(PreferenceKeys.ImmersiveModePreferenceKey, "immersive_mode_low_profile")
            if (immersive_mode == "immersive_mode_everything")
                return true
        }
        return false
    }


    private var immersive_timer_handler: Handler? = null
    private var immersive_timer_runnable: Runnable? = null

    private fun setImmersiveTimer() {
        if (immersive_timer_handler != null && immersive_timer_runnable != null) {
            immersive_timer_handler!!.removeCallbacks(immersive_timer_runnable)
        }
        immersive_timer_handler = Handler()
        immersive_timer_handler!!.postDelayed({
            if (MyDebug.LOG)
                Log.d(TAG, "setImmersiveTimer: run")
            if (/*!camera_in_background && !popupIsOpen() && */usingKitKatImmersiveMode())
                setImmersiveMode(true)
        }, 5000)
    }

    fun initImmersiveMode() {
        if (!usingKitKatImmersiveMode()) {
            setImmersiveMode(true)
        } else {
            // don't start in immersive mode, only after a timer
            setImmersiveTimer()
        }
    }

    internal fun setImmersiveMode(on: Boolean) {
        if (MyDebug.LOG)
            Log.d(TAG, "setImmersiveMode: " + on)
        // n.b., preview.setImmersiveMode() is called from onSystemUiVisibilityChange()
        if (on) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT && usingKitKatImmersiveMode()) {
                window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_IMMERSIVE or View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or View.SYSTEM_UI_FLAG_FULLSCREEN
            } else {
                val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
                val immersive_mode = sharedPreferences.getString(PreferenceKeys.ImmersiveModePreferenceKey, "immersive_mode_low_profile")
                if (immersive_mode == "immersive_mode_low_profile")
                    window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LOW_PROFILE
                else
                    window.decorView.systemUiVisibility = 0
            }
        } else
            window.decorView.systemUiVisibility = 0
    }

    /** Sets the brightness level for normal operation (when camera preview is visible).
     * If force_max is true, this always forces maximum brightness; otherwise this depends on user preference.
     */
    internal fun setBrightnessForCamera(force_max: Boolean) {
        if (MyDebug.LOG)
            Log.d(TAG, "setBrightnessForCamera")
        // set screen to max brightness - see http://stackoverflow.com/questions/11978042/android-screen-brightness-max-value
        // done here rather than onCreate, so that changing it in preferences takes effect without restarting app
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
        val layout = window.attributes
        if (force_max || sharedPreferences.getBoolean(PreferenceKeys.getMaxBrightnessPreferenceKey(), true)) {
            layout.screenBrightness = WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_FULL
        } else {
            layout.screenBrightness = WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_NONE
        }

        // this must be called from the ui thread
        // sometimes this method may be called not on UI thread, e.g., Preview.takePhotoWhenFocused->CameraController2.takePicture
        // ->CameraController2.runFakePrecapture->Preview/onFrontScreenTurnOn->MyApplicationInterface.turnFrontScreenFlashOn
        // -> this.setBrightnessForCamera
        this.runOnUiThread { window.attributes = layout }
    }
}
