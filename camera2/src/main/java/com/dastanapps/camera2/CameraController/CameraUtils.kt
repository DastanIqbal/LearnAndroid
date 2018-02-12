package com.dastanapps.camera2.CameraController

import android.annotation.TargetApi
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.ActivityInfo
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.preference.PreferenceManager
import android.util.Log
import android.view.View
import android.view.WindowManager
import com.dastanapps.camera2.MyApplicationInterface
import com.dastanapps.camera2.MyDebug
import com.dastanapps.camera2.Preview.Preview
import com.dastanapps.camera2.R
import com.dastanapps.camera2.ToastBoxer
import com.dastanapps.camera2.settings.MyPreferenceFragment
import com.dastanapps.camera2.settings.PreferenceKeys
import java.util.*

/**
 * Created by dastaniqbal on 10/02/2018.
 * dastanIqbal@marvelmedia.com
 * 10/02/2018 5:31
 */

object CameraUtils {
    internal val TAG = "DEBUG:CameraUtils"
    var supports_camera2: Boolean = false
    var supports_force_video_4k: Boolean = false
    var supports_auto_stabilise: Boolean = false

    /**
     * Determine whether we support Camera2 API.
     */
    fun initCamera2Support(context: Context): Boolean {
        if (MyDebug.LOG)
            Log.d(TAG, "initCamera2Support")
        supports_camera2 = false
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

    /** Sets the window flags for when the settings window is open.
     */
    fun setWindowFlagsForSettings(activity: Activity) {
        // allow screen rotation
        activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
        // revert to standard screen blank behaviour
        activity.window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        // settings should still be protected by screen lock
        activity.window.clearFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED)

        run {
            val layout = activity.window.attributes
            layout.screenBrightness = WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_NONE
            activity.window.attributes = layout
        }

        setImmersiveMode(false, activity)
    }

    fun putBundleExtra(bundle: Bundle, key: String, values: List<String>?) {
        if (values != null) {
            val values_arr = arrayOfNulls<String>(values.size)
            var i = 0
            for (value in values) {
                values_arr[i] = value
                i++
            }
            bundle.putStringArray(key, values_arr)
        }
    }

    fun supportsAutoStabilise(): Boolean {
        return supports_auto_stabilise
    }

    fun supportsDRO(): Boolean {
        // require at least Android 5, for the Renderscript support in HDRProcessor
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP
    }

    fun supportsHDR(preview: Preview): Boolean {
        // we also require the device have sufficient memory to do the processing, simplest to use the same test as we do for auto-stabilise...
        // also require at least Android 5, for the Renderscript support in HDRProcessor
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && this.supportsAutoStabilise() && preview.supportsExpoBracketing()
    }

    fun supportsExpoBracketing(preview: Preview): Boolean {
        return preview.supportsExpoBracketing()
    }

    fun supportsNoiseReduction(): Boolean {
        //return( Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && preview.usingCamera2API() && large_heap_memory >= 512 && preview.supportsExpoBracketing() );
        return false // currently blocked for release
    }

    private fun maxExpoBracketingNImages(preview: Preview): Int {
        return preview.maxExpoBracketingNImages()
    }

    fun supportsForceVideo4K(): Boolean {
        return supports_force_video_4k;
    }

    fun disableForceVideo4K() {
        supports_force_video_4k = false
    }


    fun openSettings(preview: Preview, context: Context) {
        val activity: Activity = context as Activity
        if (MyDebug.LOG)
            Log.d(TAG, "openSettings")
        // waitUntilImageQueueEmpty() // in theory not needed as we could continue running in the background, but best to be safe
        // closePopup()
        preview.cancelTimer() // best to cancel any timer, in case we take a photo while settings window is open, or when changing settings
        preview.cancelBurst() // similarly cancel the auto-repeat burst mode!
        preview.stopVideo(false) // important to stop video, as we'll be changing camera parameters when the settings window closes
        //  stopAudioListeners()

        val bundle = Bundle()
        bundle.putInt("cameraId", preview.cameraId)
        bundle.putInt("nCameras", preview.cameraControllerManager.numberOfCameras)
        bundle.putString("camera_api", preview.cameraAPI)
        bundle.putBoolean("using_android_l", preview.usingCamera2API())
        bundle.putBoolean("supports_auto_stabilise", supports_auto_stabilise)
        bundle.putBoolean("supports_force_video_4k", supports_force_video_4k)
        bundle.putBoolean("supports_camera2", CameraUtils.supports_camera2)
        bundle.putBoolean("supports_face_detection", preview.supportsFaceDetection())
        bundle.putBoolean("supports_raw", preview.supportsRaw())
        bundle.putBoolean("supports_hdr", supportsHDR(preview))
        bundle.putBoolean("supports_nr", supportsNoiseReduction())
        bundle.putBoolean("supports_expo_bracketing", supportsExpoBracketing(preview))
        bundle.putInt("max_expo_bracketing_n_images", maxExpoBracketingNImages(preview))
        bundle.putBoolean("supports_exposure_compensation", preview.supportsExposures())
        bundle.putInt("exposure_compensation_min", preview.minimumExposure)
        bundle.putInt("exposure_compensation_max", preview.maximumExposure)
        bundle.putBoolean("supports_iso_range", preview.supportsISORange())
        bundle.putInt("iso_range_min", preview.minimumISO)
        bundle.putInt("iso_range_max", preview.maximumISO)
        bundle.putBoolean("supports_exposure_time", preview.supportsExposureTime())
        bundle.putLong("exposure_time_min", preview.minimumExposureTime)
        bundle.putLong("exposure_time_max", preview.maximumExposureTime)
        bundle.putBoolean("supports_white_balance_temperature", preview.supportsWhiteBalanceTemperature())
        bundle.putInt("white_balance_temperature_min", preview.minimumWhiteBalanceTemperature)
        bundle.putInt("white_balance_temperature_max", preview.maximumWhiteBalanceTemperature)
        bundle.putBoolean("supports_video_stabilization", preview.supportsVideoStabilization())
        bundle.putBoolean("can_disable_shutter_sound", preview.canDisableShutterSound())

        putBundleExtra(bundle, "color_effects", preview.supportedColorEffects)
        putBundleExtra(bundle, "scene_modes", preview.supportedSceneModes)
        putBundleExtra(bundle, "white_balances", preview.supportedWhiteBalances)
        putBundleExtra(bundle, "isos", preview.supportedISOs)
        bundle.putString("iso_key", preview.isoKey)
        if (preview.cameraController != null) {
            bundle.putString("parameters_string", preview.cameraController.parametersString)
        }

        val preview_sizes = preview.supportedPreviewSizes
        if (preview_sizes != null) {
            val widths = IntArray(preview_sizes.size)
            val heights = IntArray(preview_sizes.size)
            var i = 0
            for (size in preview_sizes) {
                widths[i] = size.width
                heights[i] = size.height
                i++
            }
            bundle.putIntArray("preview_widths", widths)
            bundle.putIntArray("preview_heights", heights)
        }
        bundle.putInt("preview_width", preview.currentPreviewSize.width)
        bundle.putInt("preview_height", preview.currentPreviewSize.height)

        val sizes = preview.supportedPictureSizes
        if (sizes != null) {
            val widths = IntArray(sizes.size)
            val heights = IntArray(sizes.size)
            var i = 0
            for (size in sizes) {
                widths[i] = size.width
                heights[i] = size.height
                i++
            }
            bundle.putIntArray("resolution_widths", widths)
            bundle.putIntArray("resolution_heights", heights)
        }
        if (preview.currentPictureSize != null) {
            bundle.putInt("resolution_width", preview.currentPictureSize.width)
            bundle.putInt("resolution_height", preview.currentPictureSize.height)
        }

        val video_quality = preview.videoQualityHander.supportedVideoQuality
        if (video_quality != null && preview.cameraController != null) {
            val video_quality_arr = arrayOfNulls<String>(video_quality.size)
            val video_quality_string_arr = arrayOfNulls<String>(video_quality.size)
            var i = 0
            for (value in video_quality) {
                video_quality_arr[i] = value
                video_quality_string_arr[i] = preview.getCamcorderProfileDescription(value)
                i++
            }
            bundle.putStringArray("video_quality", video_quality_arr)
            bundle.putStringArray("video_quality_string", video_quality_string_arr)
        }
        if (preview.videoQualityHander.currentVideoQuality != null) {
            bundle.putString("current_video_quality", preview.videoQualityHander.currentVideoQuality)
        }
        val camcorder_profile = preview.videoProfile
        bundle.putInt("video_frame_width", camcorder_profile.videoFrameWidth)
        bundle.putInt("video_frame_height", camcorder_profile.videoFrameHeight)
        bundle.putInt("video_bit_rate", camcorder_profile.videoBitRate)
        bundle.putInt("video_frame_rate", camcorder_profile.videoFrameRate)

        val video_sizes = preview.videoQualityHander.supportedVideoSizes
        if (video_sizes != null) {
            val widths = IntArray(video_sizes.size)
            val heights = IntArray(video_sizes.size)
            var i = 0
            for (size in video_sizes) {
                widths[i] = size.width
                heights[i] = size.height
                i++
            }
            bundle.putIntArray("video_widths", widths)
            bundle.putIntArray("video_heights", heights)
        }

        putBundleExtra(bundle, "flash_values", preview.supportedFlashValues)
        putBundleExtra(bundle, "focus_values", preview.supportedFocusValues)

        preferencesListener.startListening(context)

        //showPreview(false)
        setWindowFlagsForSettings(context)
        val fragment = MyPreferenceFragment()
        fragment.setArguments(bundle)
        // use commitAllowingStateLoss() instead of commit(), does to "java.lang.IllegalStateException: Can not perform this action after onSaveInstanceState" crash seen on Google Play
        // see http://stackoverflow.com/questions/7575921/illegalstateexception-can-not-perform-this-action-after-onsaveinstancestate-wit
        activity.fragmentManager.beginTransaction().add(android.R.id.content, fragment, "PREFERENCE_FRAGMENT").addToBackStack(null).commitAllowingStateLoss()
    }

    private val preferencesListener = PreferencesListener()

    fun stopListeningPreferenceFragment(context: Context) {
        preferencesListener.stopListening(context)
    }

    /** Keeps track of changes to SharedPreferences.
     */
    class PreferencesListener : SharedPreferences.OnSharedPreferenceChangeListener {

        private var any: Boolean = false // whether any changes that require update have been made since startListening()

        fun startListening(context: Context) {
            if (MyDebug.LOG)
                Log.d(TAG, "startListening")
            any = false

            val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
            // n.b., registerOnSharedPreferenceChangeListener warns that we must keep a reference to the listener (which
            // is this class) as long as we want to listen for changes, otherwise the listener may be garbage collected!
            sharedPreferences.registerOnSharedPreferenceChangeListener(this)
        }

        fun stopListening(context: Context) {
            if (MyDebug.LOG)
                Log.d(TAG, "stopListening")
            val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
            sharedPreferences.unregisterOnSharedPreferenceChangeListener(this)
        }

        override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences, key: String) {
            if (MyDebug.LOG)
                Log.d(TAG, "onSharedPreferenceChanged: " + key)
            when (key) {
            // we whitelist preferences where we're sure that we don't need to call updateForSettings() if they've changed
                "preference_timer", "preference_touch_capture", "preference_pause_preview", "preference_shutter_sound", "preference_timer_beep", "preference_timer_speak", "preference_volume_keys", "preference_audio_noise_control_sensitivity", "preference_using_saf", "preference_save_photo_prefix", "preference_save_video_prefix", "preference_save_zulu_time", "preference_show_when_locked", "preference_startup_focus", "preference_show_zoom", "preference_show_angle", "preference_show_angle_line", "preference_show_pitch_lines", "preference_angle_highlight_color", "preference_show_geo_direction", "preference_show_geo_direction_lines", "preference_show_battery", "preference_show_time", "preference_free_memory", "preference_show_iso", "preference_grid", "preference_crop_guide", "preference_show_toasts", "preference_thumbnail_animation", "preference_take_photo_border", "preference_keep_display_on", "preference_max_brightness", "preference_hdr_save_expo", "preference_front_camera_mirror", "preference_stamp", "preference_stamp_dateformat", "preference_stamp_timeformat", "preference_stamp_gpsformat", "preference_textstamp", "preference_stamp_fontsize", "preference_stamp_font_color", "preference_stamp_style", "preference_background_photo_saving", "preference_record_audio", "preference_record_audio_src", "preference_record_audio_channels", "preference_lock_video", "preference_video_subtitle", "preference_require_location" -> if (MyDebug.LOG)
                    Log.d(TAG, "this change doesn't require update")
                else -> {
                    if (MyDebug.LOG)
                        Log.d(TAG, "this change does require update")
                    any = true
                }
            }
        }

        fun anyChanges(): Boolean {
            return any
        }
    }

    private val switch_video_toast = ToastBoxer()
    fun showPhotoVideoToast(always_show: Boolean, preview: Preview, context: Context, applicationInterface: MyApplicationInterface) {
        if (MyDebug.LOG) {
            Log.d(TAG, "showPhotoVideoToast")
            Log.d(TAG, "always_show? " + always_show)
        }
        val camera_controller = preview.cameraController
        if (camera_controller == null /*|| this.camera_in_background*/) {
            if (MyDebug.LOG)
                Log.d(TAG, "camera not open or in background")
            return
        }
        var toast_string: String
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
        var simple = true
        if (preview.isVideo) {
            val profile = preview.videoProfile
            val bitrate_string: String
            if (profile.videoBitRate >= 10000000)
                bitrate_string = (profile.videoBitRate / 1000000).toString() + "Mbps"
            else if (profile.videoBitRate >= 10000)
                bitrate_string = (profile.videoBitRate / 1000).toString() + "Kbps"
            else
                bitrate_string = profile.videoBitRate.toString() + "bps"

            val is_high_speed = preview.isVideoHighSpeed

            toast_string = context.resources.getString(R.string.video) + ": " + profile.videoFrameWidth + "x" + profile.videoFrameHeight + ", " + profile.videoFrameRate + context.resources.getString(R.string.fps) + (if (is_high_speed) " [" + context.resources.getString(R.string.high_speed) + "]" else "") + ", " + bitrate_string

            val fps_value = applicationInterface!!.videoFPSPref
            if (fps_value != "default" || is_high_speed) {
                simple = false
            }

            val record_audio = applicationInterface?.recordAudioPref
            if (!record_audio) {
                toast_string += "\n" + context.resources.getString(R.string.audio_disabled)
                simple = false
            }
            val max_duration_value = sharedPreferences.getString(PreferenceKeys.getVideoMaxDurationPreferenceKey(), "0")
            if (max_duration_value!!.length > 0 && max_duration_value != "0") {
                val entries_array = context.resources.getStringArray(R.array.preference_video_max_duration_entries)
                val values_array = context.resources.getStringArray(R.array.preference_video_max_duration_values)
                val index = Arrays.asList(*values_array).indexOf(max_duration_value)
                if (index != -1) { // just in case!
                    val entry = entries_array[index]
                    toast_string += "\n" + context.resources.getString(R.string.max_duration) + ": " + entry
                    simple = false
                }
            }
            val max_filesize = applicationInterface.getVideoMaxFileSizeUserPref()
            if (max_filesize != 0L) {
                val max_filesize_mb = max_filesize / (1024 * 1024)
                toast_string += "\n" + context.resources.getString(R.string.max_filesize) + ": " + max_filesize_mb + context.resources.getString(R.string.mb_abbreviation)
                simple = false
            }
            if (applicationInterface.videoFlashPref && preview.supportsFlash()) {
                toast_string += "\n" + context.resources.getString(R.string.preference_video_flash)
                simple = false
            }
        } else {
            toast_string = context.resources.getString(R.string.photo)
            val current_size = preview.currentPictureSize
            toast_string += " " + current_size.width + "x" + current_size.height
            if (preview.supportsFocus() && preview.supportedFocusValues.size > 1) {
                val focus_value = preview.currentFocusValue
                if (focus_value != null && focus_value != "focus_mode_auto" && focus_value != "focus_mode_continuous_picture") {
                    val focus_entry = preview.findFocusEntryForValue(focus_value)
                    if (focus_entry != null) {
                        toast_string += "\n" + focus_entry
                    }
                }
            }
//            if (applicationInterface.getAutoStabilisePref()) {
//                // important as users are sometimes confused at the behaviour if they don't realise the option is on
//                toast_string += "\n" + resources.getString(R.string.preference_auto_stabilise)
//                simple = false
//            }
            var photo_mode_string: String? = null
            val photo_mode = applicationInterface.getPhotoMode()
            if (photo_mode === MyApplicationInterface.PhotoMode.DRO) {
                photo_mode_string = context.resources.getString(R.string.photo_mode_dro)
            } else if (photo_mode === MyApplicationInterface.PhotoMode.HDR) {
                photo_mode_string = context.resources.getString(R.string.photo_mode_hdr)
            } else if (photo_mode === MyApplicationInterface.PhotoMode.ExpoBracketing) {
                photo_mode_string = context.resources.getString(R.string.photo_mode_expo_bracketing_full)
            }
            if (photo_mode_string != null) {
                toast_string += "\n" + context.resources.getString(R.string.photo_mode) + ": " + photo_mode_string
                simple = false
            }
        }
        if (applicationInterface.faceDetectionPref) {
            // important so that the user realises why touching for focus/metering areas won't work - easy to forget that face detection has been turned on!
            toast_string += "\n" + context.resources.getString(R.string.preference_face_detection)
            simple = false
        }
        val iso_value = applicationInterface.isoPref
        if (iso_value != CameraController.ISO_DEFAULT) {
            toast_string += "\nISO: " + iso_value
            if (preview.supportsExposureTime()) {
                val exposure_time_value = applicationInterface.exposureTimePref
                toast_string += " " + preview.getExposureTimeString(exposure_time_value)
            }
            simple = false
        }
        val current_exposure = camera_controller.exposureCompensation
        if (current_exposure != 0) {
            toast_string += "\n" + preview.getExposureCompensationString(current_exposure)
            simple = false
        }
        val scene_mode = camera_controller.sceneMode
        if (scene_mode != null && scene_mode != CameraController.SCENE_MODE_DEFAULT) {
            //toast_string += "\n" + resources.getString(R.string.scene_mode) + ": " + mainUI.getEntryForSceneMode(scene_mode)
            simple = false
        }
        val white_balance = camera_controller.whiteBalance
        if (white_balance != null && white_balance != CameraController.WHITE_BALANCE_DEFAULT) {
            //toast_string += "\n" + resources.getString(R.string.white_balance) + ": " + mainUI.getEntryForWhiteBalance(white_balance)
            if (white_balance == "manual" && preview.supportsWhiteBalanceTemperature()) {
                toast_string += " " + camera_controller.whiteBalanceTemperature
            }
            simple = false
        }
        val color_effect = camera_controller.colorEffect
        if (color_effect != null && color_effect != CameraController.COLOR_EFFECT_DEFAULT) {
            //toast_string += "\n" + resources.getString(R.string.color_effect) + ": " + mainUI.getEntryForColorEffect(color_effect)
            simple = false
        }
        val lock_orientation = applicationInterface.getLockOrientationPref()
        if (lock_orientation != "none") {
            val entries_array = context.resources.getStringArray(R.array.preference_lock_orientation_entries)
            val values_array = context.resources.getStringArray(R.array.preference_lock_orientation_values)
            val index = Arrays.asList(*values_array).indexOf(lock_orientation)
            if (index != -1) { // just in case!
                val entry = entries_array[index]
                toast_string += "\n" + entry
                simple = false
            }
        }
        val timer = sharedPreferences.getString(PreferenceKeys.getTimerPreferenceKey(), "0")
        if (timer != "0") {
            val entries_array = context.resources.getStringArray(R.array.preference_timer_entries)
            val values_array = context.resources.getStringArray(R.array.preference_timer_values)
            val index = Arrays.asList(*values_array).indexOf(timer)
            if (index != -1) { // just in case!
                val entry = entries_array[index]
                toast_string += "\n" + context.resources.getString(R.string.preference_timer) + ": " + entry
                simple = false
            }
        }
        val repeat = applicationInterface.getRepeatPref()
        if (repeat != "1") {
            val entries_array = context.resources.getStringArray(R.array.preference_burst_mode_entries)
            val values_array = context.resources.getStringArray(R.array.preference_burst_mode_values)
            val index = Arrays.asList(*values_array).indexOf(repeat)
            if (index != -1) { // just in case!
                val entry = entries_array[index]
                toast_string += "\n" + context.resources.getString(R.string.preference_burst_mode) + ": " + entry
                simple = false
            }
        }
        /*if( audio_listener != null ) {
			toast_string += "\n" + getResources().getString(R.string.preference_audio_noise_control);
		}*/

        if (MyDebug.LOG) {
            Log.d(TAG, "toast_string: " + toast_string)
            Log.d(TAG, "simple?: " + simple)
        }
        if (!simple || always_show)
            preview.showToast(switch_video_toast, toast_string)
    }

    fun setDeviceDefaults(context: Context) {
        if (MyDebug.LOG)
            Log.d(TAG, "setDeviceDefaults")
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
        val is_samsung = Build.MANUFACTURER.toLowerCase(Locale.US).contains("samsung")
        val is_oneplus = Build.MANUFACTURER.toLowerCase(Locale.US).contains("oneplus")
        //boolean is_nexus = Build.MODEL.toLowerCase(Locale.US).contains("nexus");
        //boolean is_nexus6 = Build.MODEL.toLowerCase(Locale.US).contains("nexus 6");
        //boolean is_pixel_phone = Build.DEVICE != null && Build.DEVICE.equals("sailfish");
        //boolean is_pixel_xl_phone = Build.DEVICE != null && Build.DEVICE.equals("marlin");
        if (MyDebug.LOG) {
            Log.d(TAG, "is_samsung? " + is_samsung)
            Log.d(TAG, "is_oneplus? " + is_oneplus)
            //Log.d(TAG, "is_nexus? " + is_nexus);
            //Log.d(TAG, "is_nexus6? " + is_nexus6);
            //Log.d(TAG, "is_pixel_phone? " + is_pixel_phone);
            //Log.d(TAG, "is_pixel_xl_phone? " + is_pixel_xl_phone);
        }
        if (is_samsung || is_oneplus) {
            // workaround needed for Samsung S7 at least (tested on Samsung RTL)
            // workaround needed for OnePlus 3 at least (see http://forum.xda-developers.com/oneplus-3/help/camera2-support-t3453103 )
            // update for v1.37: significant improvements have been made for standard flash and Camera2 API. But OnePlus 3T still has problem
            // that photos come out with a blue tinge if flash is on, and the scene is bright enough not to need it; Samsung devices also seem
            // to work okay, testing on S7 on RTL, but still keeping the fake flash mode in place for these devices, until we're sure of good
            // behaviour
            if (MyDebug.LOG)
                Log.d(TAG, "set fake flash for camera2")
            val editor = sharedPreferences.edit()
            editor.putBoolean(PreferenceKeys.Camera2FakeFlashPreferenceKey, true)
            editor.apply()
        }
        /*if( is_nexus6 ) {
			// Nexus 6 captureBurst() started having problems with Android 7 upgrade - images appeared in wrong order (and with wrong order of shutter speeds in exif info), as well as problems with the camera failing with serious errors
			// we set this even for Nexus 6 devices not on Android 7, as at some point they'll likely be upgraded to Android 7
			// Update: now fixed in v1.37, this was due to bug where we set RequestTag.CAPTURE for all captures in takePictureBurstExpoBracketing(), rather than just the last!
			if( MyDebug.LOG )
				Log.d(TAG, "disable fast burst for camera2");
			SharedPreferences.Editor editor = sharedPreferences.edit();
			editor.putBoolean(PreferenceKeys.getCamera2FastBurstPreferenceKey(), false);
			editor.apply();
		}*/
    }

    private var saf_dialog_from_preferences: Boolean = false
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    fun openFolderChooserDialogSAF(activity: Activity, from_preferences: Boolean) {
        if (MyDebug.LOG)
            Log.d(TAG, "openFolderChooserDialogSAF: " + from_preferences)
        this.saf_dialog_from_preferences = from_preferences
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT_TREE)
        //Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        //intent.addCategory(Intent.CATEGORY_OPENABLE);
        activity.startActivityForResult(intent, 42)
    }


    fun updateSaveFolder(new_save_location: String?, applicationInterface: MyApplicationInterface, preview: Preview, context: Context) {
        if (MyDebug.LOG)
            Log.d(TAG, "updateSaveFolder: " + new_save_location!!)
        if (new_save_location != null) {
            val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
            val orig_save_location = applicationInterface.getStorageUtils().saveLocation

            if (orig_save_location != new_save_location) {
                if (MyDebug.LOG)
                    Log.d(TAG, "changed save_folder to: " + applicationInterface.getStorageUtils().saveLocation)
                val editor = sharedPreferences.edit()
                editor.putString(PreferenceKeys.getSaveLocationPreferenceKey(), new_save_location)
                editor.apply()

                //  this.save_location_history.updateFolderHistory(this.getStorageUtils().saveLocation, true)
                preview.showToast(null, context.resources.getString(R.string.changed_save_location) + "\n" + applicationInterface.getStorageUtils().saveLocation)
            }
        }
    }
}
