package com.dastanapps.camera2.settings

import android.app.Activity
import android.app.ActivityManager
import android.app.AlertDialog
import android.content.*
import android.content.DialogInterface.OnClickListener
import android.content.SharedPreferences.OnSharedPreferenceChangeListener
import android.content.pm.PackageManager.NameNotFoundException
import android.graphics.Color
import android.graphics.Point
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.preference.*
import android.preference.Preference.OnPreferenceChangeListener
import android.preference.Preference.OnPreferenceClickListener
import android.text.SpannableString
import android.text.Spanned
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.util.Log
import android.view.View
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import com.dastanapps.camera2.CameraController.CameraUtils
import com.dastanapps.camera2.MainActivity
import com.dastanapps.camera2.MyDebug
import com.dastanapps.camera2.Preview.Preview
import com.dastanapps.camera2.R
import java.util.*

/** Fragment to handle the Settings UI. Note that originally this was a
 * PreferenceActivity rather than a PreferenceFragment which required all
 * communication to be via the bundle (since this replaced the MainActivity,
 * meaning we couldn't access data from that class. This no longer applies due
 * to now using a PreferenceFragment, but I've still kept with transferring
 * information via the bundle (for the most part, at least).
 */
class MyPreferenceFragment : PreferenceFragment(), OnSharedPreferenceChangeListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        if (MyDebug.LOG)
            Log.d(TAG, "onCreate")
        super.onCreate(savedInstanceState)
        addPreferencesFromResource(R.xml.preferences)

        val bundle = arguments
        val cameraId = bundle.getInt("cameraId")
        if (MyDebug.LOG)
            Log.d(TAG, "cameraId: " + cameraId)
        val nCameras = bundle.getInt("nCameras")
        if (MyDebug.LOG)
            Log.d(TAG, "nCameras: " + nCameras)

        val camera_api = bundle.getString("camera_api")

        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this.activity)

        val supports_auto_stabilise = bundle.getBoolean("supports_auto_stabilise")
        if (MyDebug.LOG)
            Log.d(TAG, "supports_auto_stabilise: " + supports_auto_stabilise)

        /*if( !supports_auto_stabilise ) {
                   Preference pref = findPreference("preference_auto_stabilise");
                   PreferenceGroup pg = (PreferenceGroup)this.findPreference("preference_category_camera_effects");
                   pg.removePreference(pref);
               }*/

        //readFromBundle(bundle, "color_effects", Preview.getColorEffectPreferenceKey(), Camera.Parameters.EFFECT_NONE, "preference_category_camera_effects");
        //readFromBundle(bundle, "scene_modes", Preview.getSceneModePreferenceKey(), Camera.Parameters.SCENE_MODE_AUTO, "preference_category_camera_effects");
        //readFromBundle(bundle, "white_balances", Preview.getWhiteBalancePreferenceKey(), Camera.Parameters.WHITE_BALANCE_AUTO, "preference_category_camera_effects");
        //readFromBundle(bundle, "isos", Preview.getISOPreferenceKey(), "auto", "preference_category_camera_effects");
        //readFromBundle(bundle, "exposures", "preference_exposure", "0", "preference_category_camera_effects");

        val supports_face_detection = bundle.getBoolean("supports_face_detection")
        if (MyDebug.LOG)
            Log.d(TAG, "supports_face_detection: " + supports_face_detection)

        if (!supports_face_detection) {
            val pref = findPreference("preference_face_detection")
            val pg = this.findPreference("preference_category_camera_controls") as PreferenceGroup
            pg.removePreference(pref)
        }

        val preview_width = bundle.getInt("preview_width")
        val preview_height = bundle.getInt("preview_height")
        val preview_widths = bundle.getIntArray("preview_widths")
        val preview_heights = bundle.getIntArray("preview_heights")
        val video_widths = bundle.getIntArray("video_widths")
        val video_heights = bundle.getIntArray("video_heights")

        val resolution_width = bundle.getInt("resolution_width")
        val resolution_height = bundle.getInt("resolution_height")
        val widths = bundle.getIntArray("resolution_widths")
        val heights = bundle.getIntArray("resolution_heights")
        if (widths != null && heights != null) {
            val entries = arrayOfNulls<CharSequence>(widths.size)
            val values = arrayOfNulls<CharSequence>(widths.size)
            for (i in widths.indices) {
                entries[i] = "$widths[i] x $heights[i] ${Preview.getAspectRatioMPString(widths[i], heights[i])}"
                values[i] = "$widths[i] $heights[i]"
            }
            val lp = findPreference("preference_resolution") as ListPreference
            lp.entries = entries
            lp.entryValues = values
            val resolution_preference_key = PreferenceKeys.getResolutionPreferenceKey(cameraId)
            val resolution_value = sharedPreferences.getString(resolution_preference_key, "")
            if (MyDebug.LOG)
                Log.d(TAG, "resolution_value: " + resolution_value!!)
            lp.value = resolution_value
            // now set the key, so we save for the correct cameraId
            lp.key = resolution_preference_key
        } else {
            val pref = findPreference("preference_resolution")
            val pg = this.findPreference("preference_screen_photo_settings") as PreferenceGroup
            pg.removePreference(pref)
        }

        run({
            val n_quality = 100
            val entries = arrayOfNulls<CharSequence>(n_quality)
            val values = arrayOfNulls<CharSequence>(n_quality)
            for (i in 0 until n_quality) {
                entries[i] = "" + (i + 1) + "%"
                values[i] = "" + (i + 1)
            }
            val lp = findPreference("preference_quality") as ListPreference
            lp.entries = entries
            lp.entryValues = values
        })

        val supports_raw = bundle.getBoolean("supports_raw")
        if (MyDebug.LOG)
            Log.d(TAG, "supports_raw: " + supports_raw)

        if (!supports_raw) {
            val pref = findPreference("preference_raw")
            val pg = this.findPreference("preference_screen_photo_settings") as PreferenceGroup
            pg.removePreference(pref)
        } else {
            val pref = findPreference("preference_raw")
            pref.onPreferenceChangeListener = object : OnPreferenceChangeListener {
                override fun onPreferenceChange(preference: Preference, newValue: Any): Boolean {
                    if (MyDebug.LOG)
                        Log.d(TAG, "clicked raw: " + newValue)
                    if (newValue == "preference_raw_yes") {
                        // we check done_raw_info every time, so that this works if the user selects RAW again without leaving and returning to Settings
                        val done_raw_info = sharedPreferences.contains(PreferenceKeys.RawInfoPreferenceKey)
                        if (!done_raw_info) {
                            val alertDialog = AlertDialog.Builder(this@MyPreferenceFragment.activity)
                            alertDialog.setTitle(R.string.preference_raw)
                            alertDialog.setMessage(R.string.raw_info)
                            alertDialog.setPositiveButton(android.R.string.ok, null)
                            alertDialog.setNegativeButton(R.string.dont_show_again, object : OnClickListener {
                                override fun onClick(dialog: DialogInterface, which: Int) {
                                    if (MyDebug.LOG)
                                        Log.d(TAG, "user clicked dont_show_again for raw info dialog")
                                    val editor = sharedPreferences.edit()
                                    editor.putBoolean(PreferenceKeys.RawInfoPreferenceKey, true)
                                    editor.apply()
                                }
                            })
                            alertDialog.show()
                        }
                    }
                    return true
                }
            }
        }

        val supports_hdr = bundle.getBoolean("supports_hdr")
        if (MyDebug.LOG)
            Log.d(TAG, "supports_hdr: " + supports_hdr)

        if (!supports_hdr) {
            val pref = findPreference("preference_hdr_save_expo")
            val pg = this.findPreference("preference_screen_photo_settings") as PreferenceGroup
            pg.removePreference(pref)
        }

        val supports_expo_bracketing = bundle.getBoolean("supports_expo_bracketing")
        if (MyDebug.LOG)
            Log.d(TAG, "supports_expo_bracketing: " + supports_expo_bracketing)

        val max_expo_bracketing_n_images = bundle.getInt("max_expo_bracketing_n_images")
        if (MyDebug.LOG)
            Log.d(TAG, "max_expo_bracketing_n_images: " + max_expo_bracketing_n_images)

        val supports_nr = bundle.getBoolean("supports_nr")
        if (MyDebug.LOG)
            Log.d(TAG, "supports_nr: " + supports_nr)

        if (!supports_nr) {
            val pref = findPreference("preference_nr_save")
            val pg = this.findPreference("preference_screen_photo_settings") as PreferenceGroup
            pg.removePreference(pref)
        }

        val supports_exposure_compensation = bundle.getBoolean("supports_exposure_compensation")
        val exposure_compensation_min = bundle.getInt("exposure_compensation_min")
        val exposure_compensation_max = bundle.getInt("exposure_compensation_max")
        if (MyDebug.LOG) {
            Log.d(TAG, "supports_exposure_compensation: " + supports_exposure_compensation)
            Log.d(TAG, "exposure_compensation_min: " + exposure_compensation_min)
            Log.d(TAG, "exposure_compensation_max: " + exposure_compensation_max)
        }

        val supports_iso_range = bundle.getBoolean("supports_iso_range")
        val iso_range_min = bundle.getInt("iso_range_min")
        val iso_range_max = bundle.getInt("iso_range_max")
        if (MyDebug.LOG) {
            Log.d(TAG, "supports_iso_range: " + supports_iso_range)
            Log.d(TAG, "iso_range_min: " + iso_range_min)
            Log.d(TAG, "iso_range_max: " + iso_range_max)
        }

        val supports_exposure_time = bundle.getBoolean("supports_exposure_time")
        val exposure_time_min = bundle.getLong("exposure_time_min")
        val exposure_time_max = bundle.getLong("exposure_time_max")
        if (MyDebug.LOG) {
            Log.d(TAG, "supports_exposure_time: " + supports_exposure_time)
            Log.d(TAG, "exposure_time_min: " + exposure_time_min)
            Log.d(TAG, "exposure_time_max: " + exposure_time_max)
        }

        val supports_white_balance_temperature = bundle.getBoolean("supports_white_balance_temperature")
        val white_balance_temperature_min = bundle.getInt("white_balance_temperature_min")
        val white_balance_temperature_max = bundle.getInt("white_balance_temperature_max")
        if (MyDebug.LOG) {
            Log.d(TAG, "supports_white_balance_temperature: " + supports_white_balance_temperature)
            Log.d(TAG, "white_balance_temperature_min: " + white_balance_temperature_min)
            Log.d(TAG, "white_balance_temperature_max: " + white_balance_temperature_max)
        }

        if (!supports_expo_bracketing || max_expo_bracketing_n_images <= 3) {
            val pref = findPreference("preference_expo_bracketing_n_images")
            val pg = this.findPreference("preference_screen_photo_settings") as PreferenceGroup
            pg.removePreference(pref)
        }
        if (!supports_expo_bracketing) {
            val pref = findPreference("preference_expo_bracketing_stops")
            val pg = this.findPreference("preference_screen_photo_settings") as PreferenceGroup
            pg.removePreference(pref)
        }

        val video_quality = bundle.getStringArray("video_quality")
        val video_quality_string = bundle.getStringArray("video_quality_string")
        if (video_quality != null && video_quality_string != null) {
            val entries = arrayOfNulls<CharSequence>(video_quality.size)
            val values = arrayOfNulls<CharSequence>(video_quality.size)
            for (i in video_quality.indices) {
                entries[i] = video_quality_string[i]
                values[i] = video_quality[i]
            }
            val lp = findPreference("preference_video_quality") as ListPreference
            lp.entries = entries
            lp.entryValues = values
            val video_quality_preference_key = PreferenceKeys.getVideoQualityPreferenceKey(cameraId)
            val video_quality_value = sharedPreferences.getString(video_quality_preference_key, "")
            if (MyDebug.LOG)
                Log.d(TAG, "video_quality_value: " + video_quality_value!!)
            lp.value = video_quality_value
            // now set the key, so we save for the correct cameraId
            lp.key = video_quality_preference_key
        } else {
            val pref = findPreference("preference_video_quality")
            val pg = this.findPreference("preference_screen_video_settings") as PreferenceGroup
            pg.removePreference(pref)
        }
        val current_video_quality = bundle.getString("current_video_quality")
        val video_frame_width = bundle.getInt("video_frame_width")
        val video_frame_height = bundle.getInt("video_frame_height")
        val video_bit_rate = bundle.getInt("video_bit_rate")
        val video_frame_rate = bundle.getInt("video_frame_rate")

        val supports_force_video_4k = bundle.getBoolean("supports_force_video_4k")
        if (MyDebug.LOG)
            Log.d(TAG, "supports_force_video_4k: " + supports_force_video_4k)
        if (!supports_force_video_4k || video_quality == null || video_quality_string == null) {
            val pref = findPreference("preference_force_video_4k")
            val pg = this.findPreference("preference_category_video_debugging") as PreferenceGroup
            pg.removePreference(pref)
        }

        val supports_video_stabilization = bundle.getBoolean("supports_video_stabilization")
        if (MyDebug.LOG)
            Log.d(TAG, "supports_video_stabilization: " + supports_video_stabilization)
        if (!supports_video_stabilization) {
            val pref = findPreference("preference_video_stabilization")
            val pg = this.findPreference("preference_screen_video_settings") as PreferenceGroup
            pg.removePreference(pref)
        }

        val can_disable_shutter_sound = bundle.getBoolean("can_disable_shutter_sound")
        if (MyDebug.LOG)
            Log.d(TAG, "can_disable_shutter_sound: " + can_disable_shutter_sound)
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR1 || !can_disable_shutter_sound) {
            // Camera.enableShutterSound requires JELLY_BEAN_MR1 or greater
            val pref = findPreference("preference_shutter_sound")
            val pg = this.findPreference("preference_screen_camera_controls_more") as PreferenceGroup
            pg.removePreference(pref)
        }

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
            // Some immersive modes require KITKAT - simpler to require Kitkat for any of the menu options
            val pref = findPreference("preference_immersive_mode")
            val pg = this.findPreference("preference_screen_gui") as PreferenceGroup
            pg.removePreference(pref)
        }

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
            // the required ExifInterface tags requires Android N or greater
            val pref = findPreference("preference_category_exif_tags")
            val pg = this.findPreference("preference_screen_photo_settings") as PreferenceGroup
            pg.removePreference(pref)
        } else {
            setSummary("preference_exif_artist")
            setSummary("preference_exif_copyright")
        }


        val using_android_l = bundle.getBoolean("using_android_l")
        if (!using_android_l) {
            val pref = findPreference("preference_show_iso")
            val pg = this.findPreference("preference_screen_gui") as PreferenceGroup
            pg.removePreference(pref)
        }
        if (!using_android_l) {
            var pref = findPreference("preference_camera2_fake_flash")
            var pg = this.findPreference("preference_category_photo_debugging") as PreferenceGroup
            pg.removePreference(pref)

            pref = findPreference("preference_camera2_fast_burst")
            pg = this.findPreference("preference_category_photo_debugging") as PreferenceGroup
            pg.removePreference(pref)
        }

        val supports_camera2 = bundle.getBoolean("supports_camera2")
        if (MyDebug.LOG)
            Log.d(TAG, "supports_camera2: " + supports_camera2)
        if (supports_camera2) {
            val pref = findPreference("preference_use_camera2")
            pref.onPreferenceClickListener = object : OnPreferenceClickListener {
                override fun onPreferenceClick(arg0: Preference): Boolean {
                    if (pref.key == "preference_use_camera2") {
                        if (MyDebug.LOG)
                            Log.d(TAG, "user clicked camera2 API - need to restart")
                        // see http://stackoverflow.com/questions/2470870/force-application-to-restart-on-first-activity
                        val i = activity.baseContext.packageManager.getLaunchIntentForPackage(activity.baseContext.packageName)
                        i!!.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                        startActivity(i)
                        return false
                    }
                    return false
                }
            }
        } else {
            val pref = findPreference("preference_use_camera2")
            val pg = this.findPreference("preference_category_online") as PreferenceGroup
            pg.removePreference(pref)
        }

        run({
            val pref = findPreference("preference_online_help")
            pref.onPreferenceClickListener = object : OnPreferenceClickListener {
                override fun onPreferenceClick(arg0: Preference): Boolean {
                    if (pref.key == "preference_online_help") {
                        if (MyDebug.LOG)
                            Log.d(TAG, "user clicked online help")
                        val main_activity = this@MyPreferenceFragment.activity as MainActivity
                        //main_activity.launchOnlineHelp();
                        return false
                    }
                    return false
                }
            }
        })

        /*{
                   EditTextPreference edit = (EditTextPreference)findPreference("preference_save_location");
                   InputFilter filter = new InputFilter() {
                       // whilst Android seems to allow any characters on internal memory, SD cards are typically formatted with FAT32
                       String disallowed = "|\\?*<\":>";
                       public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
                           for(int i=start;i<end;i++) {
                               if( disallowed.indexOf( source.charAt(i) ) != -1 ) {
                                   return "";
                               }
                           }
                           return null;
                       }
                   };
                   edit.getEditText().setFilters(new InputFilter[]{filter});
               }*/
        run({
            val pref = findPreference("preference_save_location")
            pref.onPreferenceClickListener = object : OnPreferenceClickListener {
                override fun onPreferenceClick(arg0: Preference): Boolean {
                    if (MyDebug.LOG)
                        Log.d(TAG, "clicked save location")
                    val main_activity = this@MyPreferenceFragment.activity as MainActivity
                    if (main_activity.getStorageUtils().isUsingSAF) {
                        CameraUtils.openFolderChooserDialogSAF(activity,true)
                        return true
                    } else {
                        val fragment = SaveFolderChooserDialog()
                        fragment.show(fragmentManager, "FOLDER_FRAGMENT")
                        return true
                    }
                }
            }
        })

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            val pref = findPreference("preference_using_saf")
            val pg = this.findPreference("preference_screen_camera_controls_more") as PreferenceGroup
            pg.removePreference(pref)
        } else {
            val pref = findPreference("preference_using_saf")
            pref.onPreferenceClickListener = object : OnPreferenceClickListener {
                override fun onPreferenceClick(arg0: Preference): Boolean {
                    if (pref.key == "preference_using_saf") {
                        if (MyDebug.LOG)
                            Log.d(TAG, "user clicked saf")
                        if (sharedPreferences.getBoolean(PreferenceKeys.getUsingSAFPreferenceKey(), false)) {
                            if (MyDebug.LOG)
                                Log.d(TAG, "saf is now enabled")
                            // seems better to alway re-show the dialog when the user selects, to make it clear where files will be saved (as the SAF location in general will be different to the non-SAF one)
                            //String uri = sharedPreferences.getString(PreferenceKeys.getSaveLocationSAFPreferenceKey(), "");
                            //if( uri.length() == 0 )
                            run({
                                val main_activity = this@MyPreferenceFragment.activity as MainActivity
                                Toast.makeText(main_activity, R.string.saf_select_save_location, Toast.LENGTH_SHORT).show()
                                CameraUtils.openFolderChooserDialogSAF(activity,true)
                            })
                        } else {
                            if (MyDebug.LOG)
                                Log.d(TAG, "saf is now disabled")
                        }
                    }
                    return false
                }
            }
        }

        run({
            val pref = findPreference("preference_calibrate_level")
            pref.onPreferenceClickListener = object : OnPreferenceClickListener {
                override fun onPreferenceClick(arg0: Preference): Boolean {
                    if (pref.key == "preference_calibrate_level") {
                        if (MyDebug.LOG)
                            Log.d(TAG, "user clicked calibrate level option")
                        val alertDialog = AlertDialog.Builder(this@MyPreferenceFragment.activity)
                        alertDialog.setTitle(activity.resources.getString(R.string.preference_calibrate_level))
                        alertDialog.setMessage(R.string.preference_calibrate_level_dialog)
                        alertDialog.setPositiveButton(R.string.preference_calibrate_level_calibrate, object : OnClickListener {
                            override fun onClick(dialog: DialogInterface, id: Int) {
                                if (MyDebug.LOG)
                                    Log.d(TAG, "user clicked calibrate level")
                                val main_activity = this@MyPreferenceFragment.activity as MainActivity
                                if (main_activity.preview.hasLevelAngle()) {
                                    val current_level_angle = main_activity.preview.levelAngleUncalibrated
                                    val editor = sharedPreferences.edit()
                                    editor.putFloat(PreferenceKeys.CalibratedLevelAnglePreferenceKey, current_level_angle.toFloat())
                                    editor.apply()
                                    main_activity.preview.updateLevelAngles()
                                    Toast.makeText(main_activity, R.string.preference_calibrate_level_calibrated, Toast.LENGTH_SHORT).show()
                                }
                            }
                        })
                        alertDialog.setNegativeButton(R.string.preference_calibrate_level_reset, object : OnClickListener {
                            override fun onClick(dialog: DialogInterface, id: Int) {
                                if (MyDebug.LOG)
                                    Log.d(TAG, "user clicked reset calibration level")
                                val main_activity = this@MyPreferenceFragment.activity as MainActivity
                                val editor = sharedPreferences.edit()
                                editor.putFloat(PreferenceKeys.CalibratedLevelAnglePreferenceKey, 0.0f)
                                editor.apply()
                                main_activity.preview.updateLevelAngles()
                                Toast.makeText(main_activity, R.string.preference_calibrate_level_calibration_reset, Toast.LENGTH_SHORT).show()
                            }
                        })
                        alertDialog.show()
                        return false
                    }
                    return false
                }
            }
        })

        run({
            val pref = findPreference("preference_donate")
            pref.onPreferenceClickListener = object : OnPreferenceClickListener {
                override fun onPreferenceClick(arg0: Preference): Boolean {
                    if (pref.key == "preference_donate") {
                        if (MyDebug.LOG)
                            Log.d(TAG, "user clicked to donate")
                        /*Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(MainActivity.getDonateMarketLink()));
                                                   try {
                                                       startActivity(browserIntent);
                                                   }
                                                   catch(ActivityNotFoundException e) {
                                                       // needed in case market:// not supported
                                                       if( MyDebug.LOG )
                                                           Log.d(TAG, "can't launch market:// intent");
                                                       browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(MainActivity.getDonateLink()));
                                                       startActivity(browserIntent);
                                                   }*/
                        //            	        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(MainActivity.getDonateLink()));
                        //        	        	startActivity(browserIntent);
                        return false
                    }
                    return false
                }
            }
        })

        run({
            val pref = findPreference("preference_about")
            pref.onPreferenceClickListener = object : OnPreferenceClickListener {
                override fun onPreferenceClick(arg0: Preference): Boolean {
                    if (pref.key == "preference_about") {
                        if (MyDebug.LOG)
                            Log.d(TAG, "user clicked about")
                        val alertDialog = AlertDialog.Builder(this@MyPreferenceFragment.activity)
                        alertDialog.setTitle(R.string.preference_about)
                        val about_string = StringBuilder()
                        val gpl_link = "GPL v3 or later"
                        val online_help_link = "online help"
                        var version = "UNKNOWN_VERSION"
                        var version_code = -1
                        try {
                            val pInfo = this@MyPreferenceFragment.activity.packageManager.getPackageInfo(this@MyPreferenceFragment.activity.packageName, 0)
                            version = pInfo.versionName
                            version_code = pInfo.versionCode
                        } catch (e: NameNotFoundException) {
                            if (MyDebug.LOG)
                                Log.d(TAG, "NameNotFoundException exception trying to get version number")
                            e.printStackTrace()
                        }

                        about_string.append("Open Camera v")
                        about_string.append(version)
                        about_string.append("\nCode: ")
                        about_string.append(version_code)
                        about_string.append("\n(c) 2013-2017 Mark Harman")
                        about_string.append("\nReleased under the ")
                        about_string.append(gpl_link)
                        about_string.append(" (Open Camera also uses additional third party files, see " + online_help_link + " for full licences and attributions.)")
                        about_string.append("\nPackage: ")
                        about_string.append(this@MyPreferenceFragment.activity.packageName)
                        about_string.append("\nAndroid API version: ")
                        about_string.append(Build.VERSION.SDK_INT)
                        about_string.append("\nDevice manufacturer: ")
                        about_string.append(Build.MANUFACTURER)
                        about_string.append("\nDevice model: ")
                        about_string.append(Build.MODEL)
                        about_string.append("\nDevice code-name: ")
                        about_string.append(Build.HARDWARE)
                        about_string.append("\nDevice variant: ")
                        about_string.append(Build.DEVICE)
                        about_string.append("\nLanguage: ")
                        about_string.append(Locale.getDefault().language)
                        run({
                            val activityManager = activity.getSystemService(Activity.ACTIVITY_SERVICE) as ActivityManager?
                            about_string.append("\nStandard max heap?: ")
                            about_string.append(activityManager!!.memoryClass)
                            about_string.append("\nLarge max heap?: ")
                            about_string.append(activityManager.largeMemoryClass)
                        })
                        run({
                            val display_size = Point()
                            val display = this@MyPreferenceFragment.activity.windowManager.defaultDisplay
                            display.getSize(display_size)
                            about_string.append("\nDisplay size: ")
                            about_string.append(display_size.x)
                            about_string.append("x")
                            about_string.append(display_size.y)
                        })
                        about_string.append("\nCurrent camera ID: ")
                        about_string.append(cameraId)
                        about_string.append("\nNo. of cameras: ")
                        about_string.append(nCameras)
                        about_string.append("\nCamera API: ")
                        about_string.append(camera_api)
                        run({
                            val last_video_error = sharedPreferences.getString("last_video_error", "")
                            if (last_video_error!!.length > 0) {
                                about_string.append("\nLast video error: ")
                                about_string.append(last_video_error)
                            }
                        })
                        if (preview_widths != null && preview_heights != null) {
                            about_string.append("\nPreview resolutions: ")
                            for (i in preview_widths.indices) {
                                if (i > 0) {
                                    about_string.append(", ")
                                }
                                about_string.append(preview_widths[i])
                                about_string.append("x")
                                about_string.append(preview_heights[i])
                            }
                        }
                        about_string.append("\nPreview resolution: ")
                        about_string.append(preview_width)
                        about_string.append("x")
                        about_string.append(preview_height)
                        if (widths != null && heights != null) {
                            about_string.append("\nPhoto resolutions: ")
                            for (i in widths.indices) {
                                if (i > 0) {
                                    about_string.append(", ")
                                }
                                about_string.append(widths[i])
                                about_string.append("x")
                                about_string.append(heights[i])
                            }
                        }
                        about_string.append("\nPhoto resolution: ")
                        about_string.append(resolution_width)
                        about_string.append("x")
                        about_string.append(resolution_height)
                        if (video_quality != null) {
                            about_string.append("\nVideo qualities: ")
                            for (i in video_quality.indices) {
                                if (i > 0) {
                                    about_string.append(", ")
                                }
                                about_string.append(video_quality[i])
                            }
                        }
                        if (video_widths != null && video_heights != null) {
                            about_string.append("\nVideo resolutions: ")
                            for (i in video_widths.indices) {
                                if (i > 0) {
                                    about_string.append(", ")
                                }
                                about_string.append(video_widths[i])
                                about_string.append("x")
                                about_string.append(video_heights[i])
                            }
                        }
                        about_string.append("\nVideo quality: ")
                        about_string.append(current_video_quality)
                        about_string.append("\nVideo frame width: ")
                        about_string.append(video_frame_width)
                        about_string.append("\nVideo frame height: ")
                        about_string.append(video_frame_height)
                        about_string.append("\nVideo bit rate: ")
                        about_string.append(video_bit_rate)
                        about_string.append("\nVideo frame rate: ")
                        about_string.append(video_frame_rate)
                        about_string.append("\nAuto-stabilise?: ")
                        about_string.append(getString(if (supports_auto_stabilise) R.string.about_available else R.string.about_not_available))
                        about_string.append("\nAuto-stabilise enabled?: ")
                        about_string.append(sharedPreferences.getBoolean(PreferenceKeys.AutoStabilisePreferenceKey, false))
                        about_string.append("\nFace detection?: ")
                        about_string.append(getString(if (supports_face_detection) R.string.about_available else R.string.about_not_available))
                        about_string.append("\nRAW?: ")
                        about_string.append(getString(if (supports_raw) R.string.about_available else R.string.about_not_available))
                        about_string.append("\nHDR?: ")
                        about_string.append(getString(if (supports_hdr) R.string.about_available else R.string.about_not_available))
                        about_string.append("\nExpo?: ")
                        about_string.append(getString(if (supports_expo_bracketing) R.string.about_available else R.string.about_not_available))
                        about_string.append("\nExpo compensation?: ")
                        about_string.append(getString(if (supports_exposure_compensation) R.string.about_available else R.string.about_not_available))
                        if (supports_exposure_compensation) {
                            about_string.append("\nExposure compensation range: ")
                            about_string.append(exposure_compensation_min)
                            about_string.append(" to ")
                            about_string.append(exposure_compensation_max)
                        }
                        about_string.append("\nManual ISO?: ")
                        about_string.append(getString(if (supports_iso_range) R.string.about_available else R.string.about_not_available))
                        if (supports_iso_range) {
                            about_string.append("\nISO range: ")
                            about_string.append(iso_range_min)
                            about_string.append(" to ")
                            about_string.append(iso_range_max)
                        }
                        about_string.append("\nManual exposure?: ")
                        about_string.append(getString(if (supports_exposure_time) R.string.about_available else R.string.about_not_available))
                        if (supports_exposure_time) {
                            about_string.append("\nExposure range: ")
                            about_string.append(exposure_time_min)
                            about_string.append(" to ")
                            about_string.append(exposure_time_max)
                        }
                        about_string.append("\nManual WB?: ")
                        about_string.append(getString(if (supports_white_balance_temperature) R.string.about_available else R.string.about_not_available))
                        if (supports_white_balance_temperature) {
                            about_string.append("\nWB temperature: ")
                            about_string.append(white_balance_temperature_min)
                            about_string.append(" to ")
                            about_string.append(white_balance_temperature_max)
                        }
                        about_string.append("\nVideo stabilization?: ")
                        about_string.append(getString(if (supports_video_stabilization) R.string.about_available else R.string.about_not_available))
                        about_string.append("\nCan disable shutter sound?: ")
                        about_string.append(getString(if (can_disable_shutter_sound) R.string.about_available else R.string.about_not_available))
                        about_string.append("\nFlash modes: ")
                        val flash_values = bundle.getStringArray("flash_values")
                        if (flash_values != null && flash_values.size > 0) {
                            for (i in flash_values.indices) {
                                if (i > 0) {
                                    about_string.append(", ")
                                }
                                about_string.append(flash_values[i])
                            }
                        } else {
                            about_string.append("None")
                        }
                        about_string.append("\nFocus modes: ")
                        val focus_values = bundle.getStringArray("focus_values")
                        if (focus_values != null && focus_values.size > 0) {
                            for (i in focus_values.indices) {
                                if (i > 0) {
                                    about_string.append(", ")
                                }
                                about_string.append(focus_values[i])
                            }
                        } else {
                            about_string.append("None")
                        }
                        about_string.append("\nColor effects: ")
                        val color_effects_values = bundle.getStringArray("color_effects")
                        if (color_effects_values != null && color_effects_values.size > 0) {
                            for (i in color_effects_values.indices) {
                                if (i > 0) {
                                    about_string.append(", ")
                                }
                                about_string.append(color_effects_values[i])
                            }
                        } else {
                            about_string.append("None")
                        }
                        about_string.append("\nScene modes: ")
                        val scene_modes_values = bundle.getStringArray("scene_modes")
                        if (scene_modes_values != null && scene_modes_values.size > 0) {
                            for (i in scene_modes_values.indices) {
                                if (i > 0) {
                                    about_string.append(", ")
                                }
                                about_string.append(scene_modes_values[i])
                            }
                        } else {
                            about_string.append("None")
                        }
                        about_string.append("\nWhite balances: ")
                        val white_balances_values = bundle.getStringArray("white_balances")
                        if (white_balances_values != null && white_balances_values.size > 0) {
                            for (i in white_balances_values.indices) {
                                if (i > 0) {
                                    about_string.append(", ")
                                }
                                about_string.append(white_balances_values[i])
                            }
                        } else {
                            about_string.append("None")
                        }
                        if (!using_android_l) {
                            about_string.append("\nISOs: ")
                            val isos = bundle.getStringArray("isos")
                            if (isos != null && isos.size > 0) {
                                for (i in isos.indices) {
                                    if (i > 0) {
                                        about_string.append(", ")
                                    }
                                    about_string.append(isos[i])
                                }
                            } else {
                                about_string.append("None")
                            }
                            val iso_key = bundle.getString("iso_key")
                            if (iso_key != null) {
                                about_string.append("\nISO key: ")
                                about_string.append(iso_key)
                            }
                        }

                        about_string.append("\nUsing SAF?: ")
                        about_string.append(sharedPreferences.getBoolean(PreferenceKeys.getUsingSAFPreferenceKey(), false))
                        val save_location = sharedPreferences.getString(PreferenceKeys.getSaveLocationPreferenceKey(), "Kruso")
                        about_string.append("\nSave Location: ")
                        about_string.append(save_location)
                        val save_location_saf = sharedPreferences.getString(PreferenceKeys.getSaveLocationSAFPreferenceKey(), "")
                        about_string.append("\nSave Location SAF: ")
                        about_string.append(save_location_saf)

                        about_string.append("\nParameters: ")
                        val parameters_string = bundle.getString("parameters_string")
                        if (parameters_string != null) {
                            about_string.append(parameters_string)
                        } else {
                            about_string.append("None")
                        }

                        val span = SpannableString(about_string)
                        span.setSpan(object : ClickableSpan() {
                            override fun onClick(v: View) {
                                if (MyDebug.LOG)
                                    Log.d(TAG, "gpl link clicked")
                                val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse("http://www.gnu.org/copyleft/gpl.html"))
                                startActivity(browserIntent)
                            }
                        }, about_string.indexOf(gpl_link), about_string.indexOf(gpl_link) + gpl_link.length, Spanned.SPAN_INCLUSIVE_INCLUSIVE)
                        span.setSpan(object : ClickableSpan() {
                            override fun onClick(v: View) {
                                if (MyDebug.LOG)
                                    Log.d(TAG, "online help link clicked")
                                val main_activity = this@MyPreferenceFragment.activity as MainActivity
                                //	main_activity.launchOnlineHelp();
                            }
                        }, about_string.indexOf(online_help_link), about_string.indexOf(online_help_link) + online_help_link.length, Spanned.SPAN_INCLUSIVE_INCLUSIVE)

                        // clickable text is only supported if we call setMovementMethod on the TextView - which means we need to create
                        // our own for the AlertDialog!
                        val scale = activity.resources.displayMetrics.density
                        val textView = TextView(activity)
                        textView.text = span
                        textView.movementMethod = LinkMovementMethod.getInstance()
                        textView.setTextAppearance(activity, android.R.style.TextAppearance_Medium)
                        val scrollView = ScrollView(activity)
                        scrollView.addView(textView)
                        // padding values from /sdk/platforms/android-18/data/res/layout/alert_dialog.xml
                        textView.setPadding((5 * scale + 0.5f).toInt(), (5 * scale + 0.5f).toInt(), (5 * scale + 0.5f).toInt(), (5 * scale + 0.5f).toInt())
                        scrollView.setPadding((14 * scale + 0.5f).toInt(), (2 * scale + 0.5f).toInt(), (10 * scale + 0.5f).toInt(), (12 * scale + 0.5f).toInt())
                        alertDialog.setView(scrollView)
                        //alertDialog.setMessage(about_string);

                        alertDialog.setPositiveButton(android.R.string.ok, null)
                        alertDialog.setNegativeButton(R.string.about_copy_to_clipboard, object : OnClickListener {
                            override fun onClick(dialog: DialogInterface, id: Int) {
                                if (MyDebug.LOG)
                                    Log.d(TAG, "user clicked copy to clipboard")
                                val clipboard = activity.getSystemService(Activity.CLIPBOARD_SERVICE) as ClipboardManager?
                                val clip = ClipData.newPlainText("OpenCamera About", about_string)
                                clipboard!!.primaryClip = clip
                            }
                        })
                        alertDialog.show()
                        return false
                    }
                    return false
                }
            }
        })

        run({
            val pref = findPreference("preference_reset")
            pref.onPreferenceClickListener = OnPreferenceClickListener {
                if (pref.key == "preference_reset") {
                    if (MyDebug.LOG)
                        Log.d(TAG, "user clicked reset")
                    AlertDialog.Builder(this@MyPreferenceFragment.activity)
                            .setIcon(android.R.drawable.ic_dialog_alert)
                            .setTitle(R.string.preference_reset)
                            .setMessage(R.string.preference_reset_question)
                            .setPositiveButton(android.R.string.yes, object : OnClickListener {
                                override fun onClick(dialog: DialogInterface, which: Int) {
                                    if (MyDebug.LOG)
                                        Log.d(TAG, "user confirmed reset")
                                    val editor = sharedPreferences.edit()
                                    editor.clear()
                                    editor.putBoolean(PreferenceKeys.FirstTimePreferenceKey, true)
                                    try {
                                        val pInfo = this@MyPreferenceFragment.activity.packageManager.getPackageInfo(this@MyPreferenceFragment.activity.packageName, 0)
                                        val version_code = pInfo.versionCode
                                        editor.putInt(PreferenceKeys.LatestVersionPreferenceKey, version_code)
                                    } catch (e: NameNotFoundException) {
                                        if (MyDebug.LOG)
                                            Log.d(TAG, "NameNotFoundException exception trying to get version number")
                                        e.printStackTrace()
                                    }

                                    editor.apply()
                                    CameraUtils.setDeviceDefaults(activity)
                                    if (MyDebug.LOG)
                                        Log.d(TAG, "user clicked reset - need to restart")
                                    // see http://stackoverflow.com/questions/2470870/force-application-to-restart-on-first-activity
                                    val i = activity.baseContext.packageManager.getLaunchIntentForPackage(activity.baseContext.packageName)
                                    i!!.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                                    startActivity(i)
                                }
                            })
                            .setNegativeButton(android.R.string.no, null)
                            .show()
                }
                false
            }
        })
    }

    class SaveFolderChooserDialog : FolderChooserDialog() {
        override fun onDismiss(dialog: DialogInterface) {
            if (MyDebug.LOG)
                Log.d(TAG, "FolderChooserDialog dismissed")
            // n.b., fragments have to be static (as they might be inserted into a new Activity - see http://stackoverflow.com/questions/15571010/fragment-inner-class-should-be-static),
            // so we access the MainActivity via the fragment's getActivity().
            val main_activity = this.activity as MainActivity
            val new_save_location = this.chosenFolder
            CameraUtils.updateSaveFolder(new_save_location,main_activity.applicationInterface,main_activity.preview,main_activity)
            super.onDismiss(dialog)
        }
    }

    /*private void readFromBundle(Bundle bundle, String intent_key, String preference_key, String default_value, String preference_category_key) {
           if( MyDebug.LOG ) {
               Log.d(TAG, "readFromBundle: " + intent_key);
           }
           String [] values = bundle.getStringArray(intent_key);
           if( values != null && values.length > 0 ) {
               if( MyDebug.LOG ) {
                   Log.d(TAG, intent_key + " values:");
                   for(int i=0;i<values.length;i++) {
                       Log.d(TAG, values[i]);
                   }
               }
               ListPreference lp = (ListPreference)findPreference(preference_key);
               lp.setEntries(values);
               lp.setEntryValues(values);
               SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this.getActivity());
               String value = sharedPreferences.getString(preference_key, default_value);
               if( MyDebug.LOG )
                   Log.d(TAG, "    value: " + values);
               lp.setValue(value);
           }
           else {
               if( MyDebug.LOG )
                   Log.d(TAG, "remove preference " + preference_key + " from category " + preference_category_key);
               Preference pref = findPreference(preference_key);
               PreferenceGroup pg = (PreferenceGroup)this.findPreference(preference_category_key);
               pg.removePreference(pref);
           }
       }*/

    override fun onResume() {
        super.onResume()
        // prevent fragment being transparent
        // note, setting color here only seems to affect the "main" preference fragment screen, and not sub-screens
        // note, on Galaxy Nexus Android 4.3 this sets to black rather than the dark grey that the background theme should be (and what the sub-screens use); works okay on Nexus 7 Android 5
        // we used to use a light theme for the PreferenceFragment, but mixing themes in same activity seems to cause problems (e.g., for EditTextPreference colors)
        val array = activity.theme.obtainStyledAttributes(intArrayOf(android.R.attr.colorBackground))
        val backgroundColor = array.getColor(0, Color.BLACK)
        /*if( MyDebug.LOG ) {
                   int r = (backgroundColor >> 16) & 0xFF;
                   int g = (backgroundColor >> 8) & 0xFF;
                   int b = (backgroundColor >> 0) & 0xFF;
                   Log.d(TAG, "backgroundColor: " + r + " , " + g + " , " + b);
               }*/
        view!!.setBackgroundColor(backgroundColor)
        array.recycle()

        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this.activity)
        sharedPreferences.registerOnSharedPreferenceChangeListener(this)
    }

    override fun onPause() {
        super.onPause()
    }

    /* So that manual changes to the checkbox/switch preferences, while the preferences are showing, show up;
        * in particular, needed for preference_using_saf, when the user cancels the SAF dialog (see
        * MainActivity.onActivityResult).
        * Also programmatically sets summary (see setSummary).
        */
    override fun onSharedPreferenceChanged(prefs: SharedPreferences, key: String) {
        if (MyDebug.LOG)
            Log.d(TAG, "onSharedPreferenceChanged")
        val pref = findPreference(key)
        if (pref is TwoStatePreference) {
            val twoStatePref = pref
            twoStatePref.isChecked = prefs.getBoolean(key, true)
        }
        setSummary(key)
    }

    /** Programmatically sets summaries as required.
     * Remember to call setSummary() from the constructor for any keys we set, to initialise the
     * summary.
     */
    private fun setSummary(key: String) {
        val pref = findPreference(key)
        if (pref is EditTextPreference) {
            // %s only supported for ListPreference
            // we also display the usual summary if no preference value is set
            if (pref.getKey() == "preference_exif_artist" || pref.getKey() == "preference_exif_copyright") {
                val editTextPref = pref
                if (editTextPref.text.length > 0) {
                    pref.setSummary(editTextPref.text)
                } else if (pref.getKey() == "preference_exif_artist") {
                    pref.setSummary(activity.resources.getString(R.string.preference_exif_artist_summary))
                } else if (pref.getKey() == "preference_exif_copyright") {
                    pref.setSummary(activity.resources.getString(R.string.preference_exif_copyright_summary))
                }
            }
        }
    }

    companion object {
        private val TAG = "MyPreferenceFragment"
    }
}
