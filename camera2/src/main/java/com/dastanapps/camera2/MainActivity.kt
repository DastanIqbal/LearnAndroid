package com.dastanapps.camera2

import android.annotation.TargetApi
import android.app.Activity
import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.os.StatFs
import android.preference.PreferenceManager
import android.util.Log
import android.view.ViewGroup
import android.view.WindowManager
import com.dastanapps.camera2.CameraController.CameraController
import com.dastanapps.camera2.CameraController.CameraUtils
import com.dastanapps.camera2.CameraController.CameraUtils.putBundleExtra
import com.dastanapps.camera2.Preview.Preview
import com.dastanapps.camera2.settings.MyPreferenceFragment
import com.dastanapps.camera2.settings.PreferenceKeys
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*

class MainActivity : Activity() {
    private val TAG: String = "DEBUG:MainActivity"
    lateinit var preview: Preview
    private lateinit var applicationInterface: MyApplicationInterface
    private var supports_force_video_4k: Boolean = false
    private var large_heap_memory: Int = 0
    private var supports_auto_stabilise: Boolean = false

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
        imv_setting.setOnClickListener { openSettings() }

        val activityManager = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        large_heap_memory = activityManager.largeMemoryClass
        if (large_heap_memory >= 128) {
            supports_auto_stabilise = true
        }
        // hack to rule out phones unlikely to have 4K video, so no point even offering the option!
        // both S5 and Note 3 have 128MB standard and 512MB large heap (tested via Samsung RTL), as does Galaxy K Zoom
        // also added the check for having 128MB standard heap, to support modded LG G2, which has 128MB standard, 256MB large - see https://sourceforge.net/p/opencamera/tickets/9/
        if (activityManager.getMemoryClass() >= 128 || activityManager.getLargeMemoryClass() >= 512) {
            supports_force_video_4k = true
        }
    }

    override fun onSaveInstanceState(state: Bundle) {
        if (MyDebug.LOG)
            Log.d(TAG, "onSaveInstanceState")
        super.onSaveInstanceState(state)
        if (this.preview != null) {
            preview.onSaveInstanceState(state)
        }
        if (this.applicationInterface != null) {
            applicationInterface.onSaveInstanceState(state)
        }
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
        if (this.supportsForceVideo4K() && preview.usingCamera2API()) {
            if (MyDebug.LOG)
                Log.d(TAG, "using Camera2 API, so can disable the force 4K option")
            this.disableForceVideo4K()
        }
        if (this.supportsForceVideo4K() && preview.videoQualityHander.supportedVideoSizes != null) {
            for (size in preview.videoQualityHander.supportedVideoSizes) {
                if (size.width >= 3840 && size.height >= 2160) {
                    if (MyDebug.LOG)
                        Log.d(TAG, "camera natively supports 4K, so can disable the force option")
                    this.disableForceVideo4K()
                }
            }
        }

        showPhotoVideoToast(false);
    }

    fun supportsAutoStabilise(): Boolean {
        return this.supports_auto_stabilise
    }

    fun supportsDRO(): Boolean {
        // require at least Android 5, for the Renderscript support in HDRProcessor
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP
    }

    fun supportsHDR(): Boolean {
        // we also require the device have sufficient memory to do the processing, simplest to use the same test as we do for auto-stabilise...
        // also require at least Android 5, for the Renderscript support in HDRProcessor
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && this.supportsAutoStabilise() && preview.supportsExpoBracketing()
    }

    fun supportsExpoBracketing(): Boolean {
        return preview.supportsExpoBracketing()
    }

    fun supportsNoiseReduction(): Boolean {
        //return( Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && preview.usingCamera2API() && large_heap_memory >= 512 && preview.supportsExpoBracketing() );
        return false // currently blocked for release
    }

    private fun maxExpoBracketingNImages(): Int {
        return preview.maxExpoBracketingNImages()
    }

    fun supportsForceVideo4K(): Boolean {
        return supports_force_video_4k;
    }

    private fun disableForceVideo4K() {
        this.supports_force_video_4k = false
    }

    fun freeMemory(): Long { // return free memory in MB
        if (MyDebug.LOG)
            Log.d(TAG, "freeMemory")
        try {
            val folder = applicationInterface?.getStorageUtils()?.imageFolder
                    ?: throw IllegalArgumentException() // so that we fall onto the backup
            val statFs = StatFs(folder.absolutePath)
            // cast to long to avoid overflow!
            val blocks = statFs.availableBlocks.toLong()
            val size = statFs.blockSize.toLong()
            return blocks * size / 1048576
        } catch (e: IllegalArgumentException) {
            // this can happen if folder doesn't exist, or don't have read access
            // if the save folder is a subfolder of DCIM, we can just use that instead
            try {
                if (!applicationInterface!!.getStorageUtils().isUsingSAF) {
                    // StorageUtils.getSaveLocation() only valid if !isUsingSAF()
                    val folder_name = applicationInterface?.getStorageUtils()?.saveLocation
                    if (!folder_name!!.startsWith("/")) {
                        val folder = StorageUtils.getBaseFolder()
                        val statFs = StatFs(folder.absolutePath)
                        // cast to long to avoid overflow!
                        val blocks = statFs.availableBlocks.toLong()
                        val size = statFs.blockSize.toLong()
                        return blocks * size / 1048576
                    }
                }
            } catch (e2: IllegalArgumentException) {
                // just in case
            }

        }
        return -1
    }

    fun getStorageUtils(): StorageUtils {
        return this.applicationInterface?.getStorageUtils()!!
    }

    private var saf_dialog_from_preferences: Boolean = false
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    fun openFolderChooserDialogSAF(from_preferences: Boolean) {
        if (MyDebug.LOG)
            Log.d(TAG, "openFolderChooserDialogSAF: " + from_preferences)
        this.saf_dialog_from_preferences = from_preferences
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT_TREE)
        //Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        //intent.addCategory(Intent.CATEGORY_OPENABLE);
        startActivityForResult(intent, 42)
    }


    fun updateSaveFolder(new_save_location: String?) {
        if (MyDebug.LOG)
            Log.d(TAG, "updateSaveFolder: " + new_save_location!!)
        if (new_save_location != null) {
            val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
            val orig_save_location = this.applicationInterface!!.getStorageUtils().saveLocation

            if (orig_save_location != new_save_location) {
                if (MyDebug.LOG)
                    Log.d(TAG, "changed save_folder to: " + this.applicationInterface!!.getStorageUtils().saveLocation)
                val editor = sharedPreferences.edit()
                editor.putString(PreferenceKeys.getSaveLocationPreferenceKey(), new_save_location)
                editor.apply()

                //  this.save_location_history.updateFolderHistory(this.getStorageUtils().saveLocation, true)
                this.preview.showToast(null, resources.getString(R.string.changed_save_location) + "\n" + this.applicationInterface!!.getStorageUtils().saveLocation)
            }
        }
    }


    fun setDeviceDefaults() {
        if (MyDebug.LOG)
            Log.d(TAG, "setDeviceDefaults")
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
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

    fun openSettings() {
        if (MyDebug.LOG)
            Log.d(TAG, "openSettings")
        // waitUntilImageQueueEmpty() // in theory not needed as we could continue running in the background, but best to be safe
        // closePopup()
        preview.cancelTimer() // best to cancel any timer, in case we take a photo while settings window is open, or when changing settings
        preview.cancelBurst() // similarly cancel the auto-repeat burst mode!
        preview.stopVideo(false) // important to stop video, as we'll be changing camera parameters when the settings window closes
        //  stopAudioListeners()

        val bundle = Bundle()
        bundle.putInt("cameraId", this.preview.cameraId)
        bundle.putInt("nCameras", preview.cameraControllerManager.numberOfCameras)
        bundle.putString("camera_api", this.preview.cameraAPI)
        bundle.putBoolean("using_android_l", this.preview.usingCamera2API())
        bundle.putBoolean("supports_auto_stabilise", this.supports_auto_stabilise)
        bundle.putBoolean("supports_force_video_4k", this.supports_force_video_4k)
        bundle.putBoolean("supports_camera2", CameraUtils.supports_camera2)
        bundle.putBoolean("supports_face_detection", this.preview.supportsFaceDetection())
        bundle.putBoolean("supports_raw", this.preview.supportsRaw())
        bundle.putBoolean("supports_hdr", this.supportsHDR())
        bundle.putBoolean("supports_nr", this.supportsNoiseReduction())
        bundle.putBoolean("supports_expo_bracketing", this.supportsExpoBracketing())
        bundle.putInt("max_expo_bracketing_n_images", this.maxExpoBracketingNImages())
        bundle.putBoolean("supports_exposure_compensation", this.preview.supportsExposures())
        bundle.putInt("exposure_compensation_min", this.preview.minimumExposure)
        bundle.putInt("exposure_compensation_max", this.preview.maximumExposure)
        bundle.putBoolean("supports_iso_range", this.preview.supportsISORange())
        bundle.putInt("iso_range_min", this.preview.minimumISO)
        bundle.putInt("iso_range_max", this.preview.maximumISO)
        bundle.putBoolean("supports_exposure_time", this.preview.supportsExposureTime())
        bundle.putLong("exposure_time_min", this.preview.minimumExposureTime)
        bundle.putLong("exposure_time_max", this.preview.maximumExposureTime)
        bundle.putBoolean("supports_white_balance_temperature", this.preview.supportsWhiteBalanceTemperature())
        bundle.putInt("white_balance_temperature_min", this.preview.minimumWhiteBalanceTemperature)
        bundle.putInt("white_balance_temperature_max", this.preview.maximumWhiteBalanceTemperature)
        bundle.putBoolean("supports_video_stabilization", this.preview.supportsVideoStabilization())
        bundle.putBoolean("can_disable_shutter_sound", this.preview.canDisableShutterSound())

        putBundleExtra(bundle, "color_effects", this.preview.supportedColorEffects)
        putBundleExtra(bundle, "scene_modes", this.preview.supportedSceneModes)
        putBundleExtra(bundle, "white_balances", this.preview.supportedWhiteBalances)
        putBundleExtra(bundle, "isos", this.preview.supportedISOs)
        bundle.putString("iso_key", this.preview.isoKey)
        if (this.preview.cameraController != null) {
            bundle.putString("parameters_string", preview.cameraController.parametersString)
        }

        val preview_sizes = this.preview.supportedPreviewSizes
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

        val sizes = this.preview.supportedPictureSizes
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

        val video_quality = this.preview.videoQualityHander.supportedVideoQuality
        if (video_quality != null && this.preview.cameraController != null) {
            val video_quality_arr = arrayOfNulls<String>(video_quality.size)
            val video_quality_string_arr = arrayOfNulls<String>(video_quality.size)
            var i = 0
            for (value in video_quality) {
                video_quality_arr[i] = value
                video_quality_string_arr[i] = this.preview.getCamcorderProfileDescription(value)
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

        val video_sizes = this.preview.videoQualityHander.supportedVideoSizes
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

        putBundleExtra(bundle, "flash_values", this.preview.supportedFlashValues)
        putBundleExtra(bundle, "focus_values", this.preview.supportedFocusValues)

        preferencesListener.startListening()

        //showPreview(false)
        CameraUtils.setWindowFlagsForSettings(this)
        val fragment = MyPreferenceFragment()
        fragment.setArguments(bundle)
        // use commitAllowingStateLoss() instead of commit(), does to "java.lang.IllegalStateException: Can not perform this action after onSaveInstanceState" crash seen on Google Play
        // see http://stackoverflow.com/questions/7575921/illegalstateexception-can-not-perform-this-action-after-onsaveinstancestate-wit
        fragmentManager.beginTransaction().add(android.R.id.content, fragment, "PREFERENCE_FRAGMENT").addToBackStack(null).commitAllowingStateLoss()
    }

    private val preferencesListener = PreferencesListener()

    /** Keeps track of changes to SharedPreferences.
     */
    internal inner class PreferencesListener : SharedPreferences.OnSharedPreferenceChangeListener {

        private var any: Boolean = false // whether any changes that require update have been made since startListening()

        fun startListening() {
            if (MyDebug.LOG)
                Log.d(TAG, "startListening")
            any = false

            val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this@MainActivity)
            // n.b., registerOnSharedPreferenceChangeListener warns that we must keep a reference to the listener (which
            // is this class) as long as we want to listen for changes, otherwise the listener may be garbage collected!
            sharedPreferences.registerOnSharedPreferenceChangeListener(this)
        }

        fun stopListening() {
            if (MyDebug.LOG)
                Log.d(TAG, "stopListening")
            val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this@MainActivity)
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
    private fun showPhotoVideoToast(always_show: Boolean) {
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
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
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

            toast_string = resources.getString(R.string.video) + ": " + profile.videoFrameWidth + "x" + profile.videoFrameHeight + ", " + profile.videoFrameRate + resources.getString(R.string.fps) + (if (is_high_speed) " [" + resources.getString(R.string.high_speed) + "]" else "") + ", " + bitrate_string

            val fps_value = applicationInterface!!.getVideoFPSPref()
            if (fps_value != "default" || is_high_speed) {
                simple = false
            }

            val record_audio = applicationInterface?.getRecordAudioPref()
            if (!record_audio) {
                toast_string += "\n" + resources.getString(R.string.audio_disabled)
                simple = false
            }
            val max_duration_value = sharedPreferences.getString(PreferenceKeys.getVideoMaxDurationPreferenceKey(), "0")
            if (max_duration_value!!.length > 0 && max_duration_value != "0") {
                val entries_array = resources.getStringArray(R.array.preference_video_max_duration_entries)
                val values_array = resources.getStringArray(R.array.preference_video_max_duration_values)
                val index = Arrays.asList(*values_array).indexOf(max_duration_value)
                if (index != -1) { // just in case!
                    val entry = entries_array[index]
                    toast_string += "\n" + resources.getString(R.string.max_duration) + ": " + entry
                    simple = false
                }
            }
            val max_filesize = applicationInterface.getVideoMaxFileSizeUserPref()
            if (max_filesize != 0L) {
                val max_filesize_mb = max_filesize / (1024 * 1024)
                toast_string += "\n" + resources.getString(R.string.max_filesize) + ": " + max_filesize_mb + resources.getString(R.string.mb_abbreviation)
                simple = false
            }
            if (applicationInterface.getVideoFlashPref() && preview.supportsFlash()) {
                toast_string += "\n" + resources.getString(R.string.preference_video_flash)
                simple = false
            }
        } else {
            toast_string = resources.getString(R.string.photo)
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
                photo_mode_string = resources.getString(R.string.photo_mode_dro)
            } else if (photo_mode === MyApplicationInterface.PhotoMode.HDR) {
                photo_mode_string = resources.getString(R.string.photo_mode_hdr)
            } else if (photo_mode === MyApplicationInterface.PhotoMode.ExpoBracketing) {
                photo_mode_string = resources.getString(R.string.photo_mode_expo_bracketing_full)
            }
            if (photo_mode_string != null) {
                toast_string += "\n" + resources.getString(R.string.photo_mode) + ": " + photo_mode_string
                simple = false
            }
        }
        if (applicationInterface.getFaceDetectionPref()) {
            // important so that the user realises why touching for focus/metering areas won't work - easy to forget that face detection has been turned on!
            toast_string += "\n" + resources.getString(R.string.preference_face_detection)
            simple = false
        }
        val iso_value = applicationInterface.getISOPref()
        if (iso_value != CameraController.ISO_DEFAULT) {
            toast_string += "\nISO: " + iso_value
            if (preview.supportsExposureTime()) {
                val exposure_time_value = applicationInterface.getExposureTimePref()
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
            val entries_array = resources.getStringArray(R.array.preference_lock_orientation_entries)
            val values_array = resources.getStringArray(R.array.preference_lock_orientation_values)
            val index = Arrays.asList(*values_array).indexOf(lock_orientation)
            if (index != -1) { // just in case!
                val entry = entries_array[index]
                toast_string += "\n" + entry
                simple = false
            }
        }
        val timer = sharedPreferences.getString(PreferenceKeys.getTimerPreferenceKey(), "0")
        if (timer != "0") {
            val entries_array = resources.getStringArray(R.array.preference_timer_entries)
            val values_array = resources.getStringArray(R.array.preference_timer_values)
            val index = Arrays.asList(*values_array).indexOf(timer)
            if (index != -1) { // just in case!
                val entry = entries_array[index]
                toast_string += "\n" + resources.getString(R.string.preference_timer) + ": " + entry
                simple = false
            }
        }
        val repeat = applicationInterface.getRepeatPref()
        if (repeat != "1") {
            val entries_array = resources.getStringArray(R.array.preference_burst_mode_entries)
            val values_array = resources.getStringArray(R.array.preference_burst_mode_values)
            val index = Arrays.asList(*values_array).indexOf(repeat)
            if (index != -1) { // just in case!
                val entry = entries_array[index]
                toast_string += "\n" + resources.getString(R.string.preference_burst_mode) + ": " + entry
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

}
