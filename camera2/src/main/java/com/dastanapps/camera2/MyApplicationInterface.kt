package com.dastanapps.camera2

import android.content.ContentValues.TAG
import android.content.Context
import android.content.SharedPreferences
import android.graphics.Canvas
import android.hardware.camera2.DngCreator
import android.location.Location
import android.media.Image
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.os.StatFs
import android.preference.PreferenceManager
import android.provider.MediaStore
import android.util.Log
import android.util.Pair
import android.view.MotionEvent
import com.dastanapps.camera2.CameraController.CameraController
import com.dastanapps.camera2.CameraController.CameraUtils
import com.dastanapps.camera2.Preview.ApplicationInterface
import com.dastanapps.camera2.Preview.ApplicationInterface.*
import com.dastanapps.camera2.Preview.VideoProfile
import com.dastanapps.camera2.settings.PreferenceKeys
import java.io.File
import java.util.*

/**
 * Created by dastaniqbal on 10/02/2018.
 * dastanIqbal@marvelmedia.com
 * 10/02/2018 5:15
 */
class MyApplicationInterface(private val mainActivity: MainActivity, private val savedInstanceState: Bundle?) : ApplicationInterface {
    private val storageUtils: StorageUtils
    private val sharedPreferences: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(mainActivity);
    private var cameraId: Int = 0;
    private var zoom_factor = 0
    private var focus_distance = 0.0f

    init {
        CameraUtils.setWindowFlagsForCamera(mainActivity)
        this.storageUtils = StorageUtils(mainActivity)
        if (savedInstanceState != null) {
            // load the things we saved in onSaveInstanceState().
            if (MyDebug.LOG)
                Log.d(TAG, "read from savedInstanceState")
            cameraId = savedInstanceState.getInt("cameraId", 0)
            if (MyDebug.LOG)
                Log.d(TAG, "found cameraId: " + cameraId)
            zoom_factor = savedInstanceState.getInt("zoom_factor", 0)
            if (MyDebug.LOG)
                Log.d(TAG, "found zoom_factor: " + zoom_factor)
            focus_distance = savedInstanceState.getFloat("focus_distance", 0.0f)
            if (MyDebug.LOG)
                Log.d(TAG, "found focus_distance: " + focus_distance)
        }
    }

    override fun getContext(): Context {
        return mainActivity
    }

    override fun useCamera2(): Boolean {
        return if (CameraUtils.initCamera2Support(mainActivity)) {
            sharedPreferences.getBoolean(PreferenceKeys.UseCamera2PreferenceKey, false)
            return true
        } else false
    }

    override fun cameraSetup() {
        mainActivity.cameraSetup();
    }

    override fun touchEvent(event: MotionEvent?) {
    }

    override fun startingVideo() {
    }

    override fun startedVideo() {
    }

    override fun stoppingVideo() {

    }

    override fun stoppedVideo(video_method: Int, uri: Uri?, filename: String?) {

    }

    override fun onFailedStartPreview() {

    }

    override fun onCameraError() {

    }

    override fun onPhotoError() {

    }

    override fun onVideoInfo(what: Int, extra: Int) {

    }

    override fun onVideoError(what: Int, extra: Int) {

    }

    override fun onVideoRecordStartError(profile: VideoProfile?) {

    }

    override fun onVideoRecordStopError(profile: VideoProfile?) {

    }

    override fun onFailedReconnectError() {

    }

    override fun onFailedCreateVideoFileError() {

    }

    override fun hasPausedPreview(paused: Boolean) {

    }

    override fun cameraInOperation(in_operation: Boolean, is_video: Boolean) {

    }

    override fun turnFrontScreenFlashOn() {

    }

    override fun cameraClosed() {

    }

    override fun timerBeep(remaining_time: Long) {

    }

    override fun layoutUI() {

    }

    override fun multitouchZoom(new_zoom: Int) {

    }

    override fun onDrawPreview(canvas: Canvas?) {

    }

    override fun onPictureTaken(data: ByteArray?, current_date: Date?): Boolean {
        return false
    }

    override fun onBurstPictureTaken(images: MutableList<ByteArray>?, current_date: Date?): Boolean {
        return false
    }

    override fun onRawPictureTaken(dngCreator: DngCreator?, image: Image?, current_date: Date?): Boolean {
        return false
    }

    override fun onCaptureStarted() {

    }

    override fun onPictureCompleted() {

    }

    override fun onContinuousFocusMove(start: Boolean) {

    }

    override fun getLocation(): Location? {
        return null
    }

    /** Here we save states which aren't saved in preferences (we don't want them to be saved if the
     * application is restarted from scratch), but we do want to preserve if Android has to recreate
     * the application (e.g., configuration change, or it's destroyed while in background).
     */
    internal fun onSaveInstanceState(state: Bundle) {
        if (MyDebug.LOG)
            Log.d(TAG, "onSaveInstanceState")
        if (MyDebug.LOG)
            Log.d(TAG, "save cameraId: " + cameraId)
        state.putInt("cameraId", cameraId)
        if (MyDebug.LOG)
            Log.d(TAG, "save zoom_factor: " + zoom_factor)
        state.putInt("zoom_factor", zoom_factor)
        if (MyDebug.LOG)
            Log.d(TAG, "save focus_distance: " + focus_distance)
        state.putFloat("focus_distance", focus_distance)
    }

    override fun createOutputVideoMethod(): Int {
        val action = mainActivity.getIntent().getAction()
        if (MediaStore.ACTION_VIDEO_CAPTURE == action) {
            if (MyDebug.LOG)
                Log.d(TAG, "from video capture intent")
            val myExtras = mainActivity.getIntent().getExtras()
            if (myExtras != null) {
                val intent_uri = myExtras!!.getParcelable<Uri>(MediaStore.EXTRA_OUTPUT)
                if (intent_uri != null) {
                    if (MyDebug.LOG)
                        Log.d(TAG, "save to: " + intent_uri!!)
                    return VIDEOMETHOD_URI
                }
            }
            // if no EXTRA_OUTPUT, we should save to standard location, and will pass back the Uri of that location
            if (MyDebug.LOG)
                Log.d(TAG, "intent uri not specified")
            // note that SAF URIs don't seem to work for calling applications (tested with Grabilla and "Photo Grabber Image From Video" (FreezeFrame)), so we use standard folder with non-SAF method
            return VIDEOMETHOD_FILE
        }
        val using_saf = storageUtils.isUsingSAF
        return if (using_saf) VIDEOMETHOD_SAF else VIDEOMETHOD_FILE
    }

    override fun createOutputVideoFile(): File {
        return File("/sdcard")
    }

    private var last_video_file_saf: Uri? = null

    override fun createOutputVideoSAF(): Uri? {
        last_video_file_saf = storageUtils.createOutputMediaFileSAF(StorageUtils.MEDIA_TYPE_VIDEO, "", "mp4", Date())
        return last_video_file_saf
    }

    override fun createOutputVideoUri(): Uri? {
        val action = mainActivity.getIntent().getAction()
        if (MediaStore.ACTION_VIDEO_CAPTURE == action) {
            if (MyDebug.LOG)
                Log.d(TAG, "from video capture intent")
            val myExtras = mainActivity.getIntent().getExtras()
            if (myExtras != null) {
                val intent_uri = myExtras.getParcelable<Uri>(MediaStore.EXTRA_OUTPUT)
                if (intent_uri != null) {
                    if (MyDebug.LOG)
                        Log.d(TAG, "save to: " + intent_uri!!)
                    return intent_uri
                }
            }
        }
        throw RuntimeException() // programming error if we arrived here
    }

    override fun getCameraIdPref(): Int {
        return cameraId;
    }

    override fun getVideoFlashPref(): Boolean {
        return sharedPreferences.getBoolean(PreferenceKeys.getVideoFlashPreferenceKey(), false)
    }

    override fun getVideoLowPowerCheckPref(): Boolean {
        return sharedPreferences.getBoolean(PreferenceKeys.getVideoLowPowerCheckPreferenceKey(), true)
    }

    override fun getPreviewSizePref(): String {
        return sharedPreferences.getString(PreferenceKeys.PreviewSizePreferenceKey, "preference_preview_size_wysiwyg")
    }

    override fun getPreviewRotationPref(): String {
        return sharedPreferences.getString(PreferenceKeys.getRotatePreviewPreferenceKey(), "0")
    }

    override fun getLockOrientationPref(): String {
        return sharedPreferences.getString(PreferenceKeys.getLockOrientationPreferenceKey(), "none")
    }

    override fun getTouchCapturePref(): Boolean {
        val value = sharedPreferences.getString(PreferenceKeys.TouchCapturePreferenceKey, "none")
        return value == "single"
    }

    override fun getDoubleTapCapturePref(): Boolean {
        val value = sharedPreferences.getString(PreferenceKeys.TouchCapturePreferenceKey, "none")
        return value == "double"
    }

    override fun getPausePreviewPref(): Boolean {
        return if (mainActivity.preview.isVideoRecording()) {
            // don't pause preview when taking photos while recording video!
            false
        } else sharedPreferences.getBoolean(PreferenceKeys.PausePreviewPreferenceKey, false)
    }

    override fun getShowToastsPref(): Boolean {
        return sharedPreferences.getBoolean(PreferenceKeys.ShowToastsPreferenceKey, true)
    }

    fun getThumbnailAnimationPref(): Boolean {
        return sharedPreferences.getBoolean(PreferenceKeys.ThumbnailAnimationPreferenceKey, true)
    }

    override fun getShutterSoundPref(): Boolean {
        return sharedPreferences.getBoolean(PreferenceKeys.getShutterSoundPreferenceKey(), true)
    }

    override fun getStartupFocusPref(): Boolean {
        return sharedPreferences.getBoolean(PreferenceKeys.getStartupFocusPreferenceKey(), true)
    }

    override fun getTimerPref(): Long {
        val timer_value = sharedPreferences.getString(PreferenceKeys.getTimerPreferenceKey(), "0")
        var timer_delay: Long
        try {
            timer_delay = Integer.parseInt(timer_value).toLong() * 1000
        } catch (e: NumberFormatException) {
            if (MyDebug.LOG)
                Log.e(TAG, "failed to parse preference_timer value: " + timer_value!!)
            e.printStackTrace()
            timer_delay = 0
        }

        return timer_delay
    }

    override fun getRepeatPref(): String {
        return sharedPreferences.getString(PreferenceKeys.getBurstModePreferenceKey(), "1")
    }

    override fun getRepeatIntervalPref(): Long {
        val timer_value = sharedPreferences.getString(PreferenceKeys.getBurstIntervalPreferenceKey(), "0")
        var timer_delay: Long
        try {
            timer_delay = Integer.parseInt(timer_value).toLong() * 1000
        } catch (e: NumberFormatException) {
            if (MyDebug.LOG)
                Log.e(TAG, "failed to parse preference_burst_interval value: " + timer_value!!)
            e.printStackTrace()
            timer_delay = 0
        }

        return timer_delay
    }

    override fun getGeotaggingPref(): Boolean {
        return sharedPreferences.getBoolean(PreferenceKeys.LocationPreferenceKey, false)
    }

    override fun getRequireLocationPref(): Boolean {
        return sharedPreferences.getBoolean(PreferenceKeys.RequireLocationPreferenceKey, false)
    }

    fun getGeodirectionPref(): Boolean {
        return sharedPreferences.getBoolean(PreferenceKeys.GPSDirectionPreferenceKey, false)
    }

    override fun getRecordAudioPref(): Boolean {
        return sharedPreferences.getBoolean(PreferenceKeys.getRecordAudioPreferenceKey(), true)
    }

    override fun getRecordAudioChannelsPref(): String {
        return sharedPreferences.getString(PreferenceKeys.getRecordAudioChannelsPreferenceKey(), "audio_default")
    }

    override fun getRecordAudioSourcePref(): String {
        return sharedPreferences.getString(PreferenceKeys.getRecordAudioSourcePreferenceKey(), "audio_src_camcorder")
    }


    override fun getZoomPref(): Int {
        if (MyDebug.LOG)
            Log.d(TAG, "getZoomPref: " + zoom_factor)
        return zoom_factor
    }

    override fun getCalibratedLevelAngle(): Double {
        return sharedPreferences.getFloat(PreferenceKeys.CalibratedLevelAnglePreferenceKey, 0.0f).toDouble()
    }

    override fun getExposureTimePref(): Long {
        return sharedPreferences.getLong(PreferenceKeys.ExposureTimePreferenceKey, CameraController.EXPOSURE_TIME_DEFAULT)
    }

    override fun getFocusDistancePref(): Float {
        return focus_distance
    }

    override fun isExpoBracketingPref(): Boolean {
        val photo_mode = getPhotoMode()
        return if (photo_mode == PhotoMode.HDR || photo_mode == PhotoMode.ExpoBracketing) true else false
    }

    override fun isCameraBurstPref(): Boolean {
        val photo_mode = getPhotoMode()
        return if (photo_mode == PhotoMode.NoiseReduction) true else false
    }

    override fun getExpoBracketingNImagesPref(): Int {
        if (MyDebug.LOG)
            Log.d(TAG, "getExpoBracketingNImagesPref")
        var n_images: Int = 0
        val photo_mode = getPhotoMode()
        if (photo_mode == PhotoMode.HDR) {
            // always set 3 images for HDR
            n_images = 3
        } else {
            val n_images_s = sharedPreferences.getString(PreferenceKeys.ExpoBracketingNImagesPreferenceKey, "3")
            try {
                n_images = Integer.parseInt(n_images_s)
            } catch (exception: NumberFormatException) {
                if (MyDebug.LOG)
                    Log.e(TAG, "n_images_s invalid format: " + n_images_s!!)
                n_images = 3
            }

        }
        if (MyDebug.LOG)
            Log.d(TAG, "n_images = " + n_images)
        return n_images
    }

    override fun getExpoBracketingStopsPref(): Double {
        if (MyDebug.LOG)
            Log.d(TAG, "getExpoBracketingStopsPref")
        var n_stops: Double = 0.0
        val photo_mode = getPhotoMode()
        if (photo_mode == PhotoMode.HDR) {
            // always set 2 stops for HDR
            n_stops = 2.0
        } else {
            val n_stops_s = sharedPreferences.getString(PreferenceKeys.ExpoBracketingStopsPreferenceKey, "2")
            try {
                n_stops = java.lang.Double.parseDouble(n_stops_s)
            } catch (exception: NumberFormatException) {
                if (MyDebug.LOG)
                    Log.e(TAG, "n_stops_s invalid format: " + n_stops_s!!)
                n_stops = 2.0
            }

        }
        if (MyDebug.LOG)
            Log.d(TAG, "n_stops = " + n_stops)
        return n_stops
    }

    fun getPhotoMode(): PhotoMode {
        // Note, this always should return the true photo mode - if we're in video mode and taking a photo snapshot while
        // video recording, the caller should override. We don't override here, as this preference may be used to affect how
        // the CameraController is set up, and we don't always re-setup the camera when switching between photo and video modes.
        val photo_mode_pref = sharedPreferences.getString(PreferenceKeys.PhotoModePreferenceKey, "preference_photo_mode_std")
        val dro = photo_mode_pref == "preference_photo_mode_dro"
        if (dro && CameraUtils.supportsDRO())
            return PhotoMode.DRO
        val hdr = photo_mode_pref == "preference_photo_mode_hdr"
        if (hdr && CameraUtils.supportsHDR(mainActivity.preview))
            return PhotoMode.HDR
        val expo_bracketing = photo_mode_pref == "preference_photo_mode_expo_bracketing"
        if (expo_bracketing && CameraUtils.supportsExpoBracketing(mainActivity.preview))
            return PhotoMode.ExpoBracketing
        val noise_reduction = photo_mode_pref == "preference_photo_mode_noise_reduction"
        return if (noise_reduction && CameraUtils.supportsNoiseReduction()) PhotoMode.NoiseReduction else PhotoMode.Standard
    }

    override fun getOptimiseAEForDROPref(): Boolean {
        val photo_mode = getPhotoMode()
        return photo_mode == PhotoMode.DRO
    }

    override fun isRawPref(): Boolean {
        return if (isImageCaptureIntent()) false else sharedPreferences.getString(PreferenceKeys.RawPreferenceKey, "preference_raw_no") == "preference_raw_yes"
    }

    override fun useCamera2FakeFlash(): Boolean {
        return sharedPreferences.getBoolean(PreferenceKeys.Camera2FakeFlashPreferenceKey, false)
    }

    override fun useCamera2FastBurst(): Boolean {
        return sharedPreferences.getBoolean(PreferenceKeys.Camera2FastBurstPreferenceKey, true)
    }

    override fun isTestAlwaysFocus(): Boolean {
        return false
    }


    override fun setCameraIdPref(cameraId: Int) {
        this.cameraId = cameraId
    }

    override fun setFlashPref(flash_value: String) {
        val editor = sharedPreferences.edit()
        editor.putString(PreferenceKeys.getFlashPreferenceKey(cameraId), flash_value)
        editor.apply()
    }

    override fun setFocusPref(focus_value: String, is_video: Boolean) {
        val editor = sharedPreferences.edit()
        editor.putString(PreferenceKeys.getFocusPreferenceKey(cameraId, is_video), focus_value)
        editor.apply()
        // focus may be updated by preview (e.g., when switching to/from video mode)
//        val visibility = if (mainActivity.preview.currentFocusValue != null && mainActivity.preview.currentFocusValue.equals("focus_mode_manual2")) View.VISIBLE else View.INVISIBLE
//        val focusSeekBar = mainActivity.findViewById(R.id.focus_seekbar)
//        focusSeekBar.setVisibility(visibility)
    }

    override fun setVideoPref(is_video: Boolean) {
        val editor = sharedPreferences.edit()
        editor.putBoolean(PreferenceKeys.IsVideoPreferenceKey, is_video)
        editor.apply()
    }

    override fun setSceneModePref(scene_mode: String) {
        val editor = sharedPreferences.edit()
        editor.putString(PreferenceKeys.SceneModePreferenceKey, scene_mode)
        editor.apply()
    }

    override fun clearSceneModePref() {
        val editor = sharedPreferences.edit()
        editor.remove(PreferenceKeys.SceneModePreferenceKey)
        editor.apply()
    }

    override fun setColorEffectPref(color_effect: String) {
        val editor = sharedPreferences.edit()
        editor.putString(PreferenceKeys.ColorEffectPreferenceKey, color_effect)
        editor.apply()
    }

    override fun clearColorEffectPref() {
        val editor = sharedPreferences.edit()
        editor.remove(PreferenceKeys.ColorEffectPreferenceKey)
        editor.apply()
    }

    override fun setWhiteBalancePref(white_balance: String) {
        val editor = sharedPreferences.edit()
        editor.putString(PreferenceKeys.WhiteBalancePreferenceKey, white_balance)
        editor.apply()
    }

    override fun clearWhiteBalancePref() {
        val editor = sharedPreferences.edit()
        editor.remove(PreferenceKeys.WhiteBalancePreferenceKey)
        editor.apply()
    }

    override fun setWhiteBalanceTemperaturePref(white_balance_temperature: Int) {
        val editor = sharedPreferences.edit()
        editor.putInt(PreferenceKeys.WhiteBalanceTemperaturePreferenceKey, white_balance_temperature)
        editor.apply()
    }

    override fun setISOPref(iso: String) {
        val editor = sharedPreferences.edit()
        editor.putString(PreferenceKeys.ISOPreferenceKey, iso)
        editor.apply()
    }

    override fun clearISOPref() {
        val editor = sharedPreferences.edit()
        editor.remove(PreferenceKeys.ISOPreferenceKey)
        editor.apply()
    }

    override fun setExposureCompensationPref(exposure: Int) {
        val editor = sharedPreferences.edit()
        editor.putString(PreferenceKeys.ExposurePreferenceKey, "" + exposure)
        editor.apply()
    }

    override fun clearExposureCompensationPref() {
        val editor = sharedPreferences.edit()
        editor.remove(PreferenceKeys.ExposurePreferenceKey)
        editor.apply()
    }

    override fun setCameraResolutionPref(width: Int, height: Int) {
        val resolution_value = width.toString() + " " + height
        if (MyDebug.LOG) {
            Log.d(TAG, "save new resolution_value: " + resolution_value)
        }
        val editor = sharedPreferences.edit()
        editor.putString(PreferenceKeys.getResolutionPreferenceKey(cameraId), resolution_value)
        editor.apply()
    }

    override fun setVideoQualityPref(video_quality: String) {
        val editor = sharedPreferences.edit()
        editor.putString(PreferenceKeys.getVideoQualityPreferenceKey(cameraId), video_quality)
        editor.apply()
    }

    override fun setZoomPref(zoom: Int) {
        zoom_factor = zoom;
    }

    override fun requestCameraPermission() {

    }

    override fun requestStoragePermission() {

    }

    override fun requestRecordAudioPermission() {

    }

    override fun setExposureTimePref(exposure_time: Long) {
        val editor = sharedPreferences.edit()
        editor.putLong(PreferenceKeys.ExposureTimePreferenceKey, exposure_time)
        editor.apply()
    }

    override fun clearExposureTimePref() {
        val editor = sharedPreferences.edit()
        editor.remove(PreferenceKeys.ExposureTimePreferenceKey)
        editor.apply()
    }

    override fun setFocusDistancePref(focus_distance: Float) {
        this.focus_distance = focus_distance;
    }


    override fun getFlashPref(): String {
        return sharedPreferences.getString(PreferenceKeys.getFlashPreferenceKey(cameraId), "")
    }

    override fun getFocusPref(is_video: Boolean): String {
        return sharedPreferences.getString(PreferenceKeys.getFocusPreferenceKey(cameraId, is_video), "")
    }

    override fun isVideoPref(): Boolean {
        return sharedPreferences.getBoolean(PreferenceKeys.IsVideoPreferenceKey, true)
    }

    override fun getSceneModePref(): String? {
        return sharedPreferences.getString(PreferenceKeys.SceneModePreferenceKey, CameraController.SCENE_MODE_DEFAULT)
    }

    override fun getColorEffectPref(): String? {
        return sharedPreferences.getString(PreferenceKeys.ColorEffectPreferenceKey, CameraController.COLOR_EFFECT_DEFAULT)
    }

    override fun getWhiteBalancePref(): String? {
        return sharedPreferences.getString(PreferenceKeys.WhiteBalancePreferenceKey, CameraController.WHITE_BALANCE_DEFAULT)
    }

    override fun getWhiteBalanceTemperaturePref(): Int {
        return sharedPreferences.getInt(PreferenceKeys.WhiteBalanceTemperaturePreferenceKey, 5000)
    }

    override fun getISOPref(): String? {
        return sharedPreferences.getString(PreferenceKeys.ISOPreferenceKey, CameraController.ISO_DEFAULT)
    }

    override fun getExposureCompensationPref(): Int {
        val value = sharedPreferences.getString(PreferenceKeys.ExposurePreferenceKey, "0")
        if (MyDebug.LOG)
            Log.d(TAG, "saved exposure value: " + value!!)
        var exposure = 0
        try {
            exposure = Integer.parseInt(value)
            if (MyDebug.LOG)
                Log.d(TAG, "exposure: " + exposure)
        } catch (exception: NumberFormatException) {
            if (MyDebug.LOG)
                Log.d(TAG, "exposure invalid format, can't parse to int")
        }

        return exposure
    }

    override fun getCameraResolutionPref(): Pair<Int, Int>? {
        val resolution_value = sharedPreferences.getString(PreferenceKeys.getResolutionPreferenceKey(cameraId), "")
        if (MyDebug.LOG)
            Log.d(TAG, "resolution_value: " + resolution_value!!)
        if (resolution_value!!.length > 0) {
            // parse the saved size, and make sure it is still valid
            val index = resolution_value.indexOf(' ')
            if (index == -1) {
                if (MyDebug.LOG)
                    Log.d(TAG, "resolution_value invalid format, can't find space")
            } else {
                val resolution_w_s = resolution_value.substring(0, index)
                val resolution_h_s = resolution_value.substring(index + 1)
                if (MyDebug.LOG) {
                    Log.d(TAG, "resolution_w_s: " + resolution_w_s)
                    Log.d(TAG, "resolution_h_s: " + resolution_h_s)
                }
                try {
                    val resolution_w = Integer.parseInt(resolution_w_s)
                    if (MyDebug.LOG)
                        Log.d(TAG, "resolution_w: " + resolution_w)
                    val resolution_h = Integer.parseInt(resolution_h_s)
                    if (MyDebug.LOG)
                        Log.d(TAG, "resolution_h: " + resolution_h)
                    return Pair(resolution_w, resolution_h)
                } catch (exception: NumberFormatException) {
                    if (MyDebug.LOG)
                        Log.d(TAG, "resolution_value invalid format, can't parse w or h to int")
                }

            }
        }
        return null
    }

    /** getImageQualityPref() returns the image quality used for the Camera Controller for taking a
     * photo - in some cases, we may set that to a higher value, then perform processing on the
     * resultant JPEG before resaving. This method returns the image quality setting to be used for
     * saving the final image (as specified by the user).
     */
    private fun getSaveImageQualityPref(): Int {
        if (MyDebug.LOG)
            Log.d(TAG, "getSaveImageQualityPref")
        val image_quality_s = sharedPreferences.getString(PreferenceKeys.QualityPreferenceKey, "90")
        var image_quality: Int
        try {
            image_quality = Integer.parseInt(image_quality_s)
        } catch (exception: NumberFormatException) {
            if (MyDebug.LOG)
                Log.e(TAG, "image_quality_s invalid format: " + image_quality_s!!)
            image_quality = 90
        }

        return image_quality
    }

    override fun getImageQualityPref(): Int {
        if (MyDebug.LOG)
            Log.d(TAG, "getImageQualityPref")
        // see documentation for getSaveImageQualityPref(): in DRO mode we want to take the photo
        // at 100% quality for post-processing, the final image will then be saved at the user requested
        // setting
        val photo_mode = getPhotoMode()
        if (photo_mode == PhotoMode.DRO)
            return 100
        else if (photo_mode == PhotoMode.NoiseReduction)
            return 100
        return getSaveImageQualityPref()
    }

    override fun getFaceDetectionPref(): Boolean {
        return sharedPreferences.getBoolean(PreferenceKeys.FaceDetectionPreferenceKey, false)
    }

    override fun getVideoQualityPref(): String {
        return sharedPreferences.getString(PreferenceKeys.getVideoQualityPreferenceKey(cameraId), "")
    }

    override fun getVideoStabilizationPref(): Boolean {
        return sharedPreferences.getBoolean(PreferenceKeys.getVideoStabilizationPreferenceKey(), false)
    }

    override fun getForce4KPref(): Boolean {
        return cameraId == 0 && sharedPreferences.getBoolean(PreferenceKeys.getForceVideo4KPreferenceKey(), false) && CameraUtils.supportsForceVideo4K()
    }

    override fun getVideoBitratePref(): String {
        return sharedPreferences.getString(PreferenceKeys.getVideoBitratePreferenceKey(), "default")
    }

    override fun getVideoFPSPref(): String {
        return sharedPreferences.getString(PreferenceKeys.getVideoFPSPreferenceKey(), "default")
    }

    override fun getVideoMaxDurationPref(): Long {
        val video_max_duration_value = sharedPreferences.getString(PreferenceKeys.getVideoMaxDurationPreferenceKey(), "0")
        var video_max_duration: Long
        try {
            video_max_duration = Integer.parseInt(video_max_duration_value).toLong() * 1000
        } catch (e: NumberFormatException) {
            if (MyDebug.LOG)
                Log.e(TAG, "failed to parse preference_video_max_duration value: " + video_max_duration_value!!)
            e.printStackTrace()
            video_max_duration = 0
        }

        return video_max_duration
    }

    override fun getVideoRestartTimesPref(): Int {
        val restart_value = sharedPreferences.getString(PreferenceKeys.getVideoRestartPreferenceKey(), "0")
        var remaining_restart_video: Int
        try {
            remaining_restart_video = Integer.parseInt(restart_value)
        } catch (e: NumberFormatException) {
            if (MyDebug.LOG)
                Log.e(TAG, "failed to parse preference_video_restart value: " + restart_value!!)
            e.printStackTrace()
            remaining_restart_video = 0
        }

        return remaining_restart_video
    }

    private fun getVideoRestartMaxFileSizeUserPref(): Boolean {
        return sharedPreferences.getBoolean(PreferenceKeys.getVideoRestartMaxFileSizePreferenceKey(), true)
    }

    var test_set_available_memory = false
    var test_available_memory: Long = 0
    override fun getVideoMaxFileSizePref(): ApplicationInterface.VideoMaxFileSize {
        if (MyDebug.LOG)
            Log.d(TAG, "getVideoMaxFileSizePref")
        val video_max_filesize = ApplicationInterface.VideoMaxFileSize()
        video_max_filesize.max_filesize = getVideoMaxFileSizeUserPref()
        video_max_filesize.auto_restart = getVideoRestartMaxFileSizeUserPref()

        /* Also if using internal memory without storage access framework, try to set the max filesize so we don't run out of space.
		   This is the only way to avoid the problem where videos become corrupt when run out of space - MediaRecorder doesn't stop on
		   its own, and no error is given!
		   If using SD card, it's not reliable to get the free storage (see https://sourceforge.net/p/opencamera/tickets/153/ ).
		   If using storage access framework, in theory we could check if this was on internal storage, but risk of getting it wrong...
		   so seems safest to leave (the main reason for using SAF is for SD cards, anyway).
		   */
        if (!storageUtils.isUsingSAF()) {
            val folder_name = storageUtils.getSaveLocation()
            if (MyDebug.LOG)
                Log.d(TAG, "saving to: " + folder_name)
            var is_internal = false
            if (!folder_name.startsWith("/")) {
                is_internal = true
            } else {
                // if save folder path is a full path, see if it matches the "external" storage (which actually means "primary", which typically isn't an SD card these days)
                val storage = Environment.getExternalStorageDirectory()
                if (MyDebug.LOG)
                    Log.d(TAG, "compare to: " + storage.absolutePath)
                if (folder_name.startsWith(storage.absolutePath))
                    is_internal = true
            }
            if (is_internal) {
                if (MyDebug.LOG)
                    Log.d(TAG, "using internal storage")
                val free_memory = freeMemory() * 1024 * 1024
                val min_free_memory: Long = 50000000 // how much free space to leave after video
                // min_free_filesize is the minimum value to set for max file size:
                //   - no point trying to create a really short video
                //   - too short videos can end up being corrupted
                //   - also with auto-restart, if this is too small we'll end up repeatedly restarting and creating shorter and shorter videos
                val min_free_filesize: Long = 20000000
                var available_memory = free_memory - min_free_memory
                if (test_set_available_memory) {
                    available_memory = test_available_memory
                }
                if (MyDebug.LOG) {
                    Log.d(TAG, "free_memory: " + free_memory)
                    Log.d(TAG, "available_memory: " + available_memory)
                }
                if (available_memory > min_free_filesize) {
                    if (video_max_filesize.max_filesize == 0L || video_max_filesize.max_filesize > available_memory) {
                        video_max_filesize.max_filesize = available_memory
                        // still leave auto_restart set to true - because even if we set a max filesize for running out of storage, the video may still hit a maximum limit before hand, if there's a device max limit set (typically ~2GB)
                        if (MyDebug.LOG)
                            Log.d(TAG, "set video_max_filesize to avoid running out of space: " + video_max_filesize)
                    }
                } else {
                    if (MyDebug.LOG)
                        Log.e(TAG, "not enough free storage to record video")
                    throw ApplicationInterface.NoFreeStorageException()
                }
            }
        }

        return video_max_filesize
    }

    internal fun getVideoMaxFileSizeUserPref(): Long {
        if (MyDebug.LOG)
            Log.d(TAG, "getVideoMaxFileSizeUserPref")
        val video_max_filesize_value = sharedPreferences.getString(PreferenceKeys.getVideoMaxFileSizePreferenceKey(), "0")
        var video_max_filesize: Long
        try {
            video_max_filesize = Integer.parseInt(video_max_filesize_value).toLong()
        } catch (e: NumberFormatException) {
            if (MyDebug.LOG)
                Log.e(TAG, "failed to parse preference_video_max_filesize value: " + video_max_filesize_value!!)
            e.printStackTrace()
            video_max_filesize = 0
        }

        if (MyDebug.LOG)
            Log.d(TAG, "video_max_filesize: " + video_max_filesize)
        return video_max_filesize
    }

    private fun isImageCaptureIntent(): Boolean {
        var image_capture_intent = false
        val action = mainActivity.getIntent().getAction()
        if (MediaStore.ACTION_IMAGE_CAPTURE == action || MediaStore.ACTION_IMAGE_CAPTURE_SECURE == action) {
            if (MyDebug.LOG)
                Log.d(TAG, "from image capture intent")
            image_capture_intent = true
        }
        return image_capture_intent
    }

    internal fun getStorageUtils(): StorageUtils {
        return storageUtils
    }

    fun freeMemory(): Long { // return free memory in MB
        if (MyDebug.LOG)
            Log.d(TAG, "freeMemory")
        try {
            val folder = getStorageUtils()?.imageFolder
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
                if (!getStorageUtils().isUsingSAF) {
                    // StorageUtils.getSaveLocation() only valid if !isUsingSAF()
                    val folder_name = getStorageUtils()?.saveLocation
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
    // note, okay to change the order of enums in future versions, as getPhotoMode() does not rely on the order for the saved photo mode
    enum class PhotoMode {
        Standard,
        DRO, // single image "fake" HDR
        HDR, // HDR created from multiple (expo bracketing) images
        ExpoBracketing, // take multiple expo bracketed images, without combining to a single image
        NoiseReduction
    }
}