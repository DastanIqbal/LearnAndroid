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
import android.preference.PreferenceManager
import android.provider.MediaStore
import android.util.Log
import android.util.Pair
import android.view.MotionEvent
import com.dastanapps.camera2.CameraController.CameraController
import com.dastanapps.camera2.CameraController.CameraUtils
import com.dastanapps.camera2.Preview.ApplicationInterface
import com.dastanapps.camera2.Preview.VideoProfile
import com.dastanapps.camera2.settings.PreferenceKeys
import java.io.File
import java.util.*

/**
 * Created by dastaniqbal on 10/02/2018.
 * dastanIqbal@marvelmedia.com
 * 10/02/2018 5:15
 */
class MyApplicationInterface(val mainActivity: MainActivity, val savedInstanceState: Bundle?) : ApplicationInterface {

    private val sharedPreferences: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(mainActivity);
    private val cameraId: Int = 0;
    override fun getContext(): Context {
        return mainActivity
    }

    override fun useCamera2(): Boolean {
        return CameraUtils.initCamera2Support(mainActivity)
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

    override fun createOutputVideoMethod(): Int {
        return 0;
    }

    override fun createOutputVideoFile(): File {
        return File("/sdcard")
    }

    override fun createOutputVideoSAF(): Uri? {
        return null;
    }

    override fun createOutputVideoUri(): Uri? {
        return null;
    }

    override fun getCameraIdPref(): Int {
        return 0;
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
//        return if (mainActivity.getPreview().isVideoRecording()) {
//            // don't pause preview when taking photos while recording video!
//            false
//        } else sharedPreferences.getBoolean(PreferenceKeys.PausePreviewPreferenceKey, false)
        return false;
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
        //if (MyDebug.LOG)
        //  Log.d(TAG, "getZoomPref: " + zoom_factor)
        return 0;//zoom_factor
    }

    override fun getCalibratedLevelAngle(): Double {
        return sharedPreferences.getFloat(PreferenceKeys.CalibratedLevelAnglePreferenceKey, 0.0f).toDouble()
    }

    override fun getExposureTimePref(): Long {
        return sharedPreferences.getLong(PreferenceKeys.ExposureTimePreferenceKey, CameraController.EXPOSURE_TIME_DEFAULT)
    }

    override fun getFocusDistancePref(): Float {
        return 0f;//focus_distance
    }

    override fun isExpoBracketingPref(): Boolean {
//        val photo_mode = getPhotoMode()
//        return if (photo_mode == PhotoMode.HDR || photo_mode == PhotoMode.ExpoBracketing) true else false
        return false
    }

    override fun isCameraBurstPref(): Boolean {
//        val photo_mode = getPhotoMode()
//        return if (photo_mode == PhotoMode.NoiseReduction) true else false
        return false
    }

    override fun getExpoBracketingNImagesPref(): Int {
        if (MyDebug.LOG)
            Log.d(TAG, "getExpoBracketingNImagesPref")
        var n_images: Int = 0
        /*val photo_mode = getPhotoMode()
        if (photo_mode == PhotoMode.HDR) {
            // always set 3 images for HDR
            n_images = 3
        } else*/run {
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
        //val photo_mode = getPhotoMode()
        /*if (photo_mode == PhotoMode.HDR) {
            // always set 2 stops for HDR
            n_stops = 2.0
        } else*/
        run {
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

//    fun getPhotoMode(): PhotoMode {
//        // Note, this always should return the true photo mode - if we're in video mode and taking a photo snapshot while
//        // video recording, the caller should override. We don't override here, as this preference may be used to affect how
//        // the CameraController is set up, and we don't always re-setup the camera when switching between photo and video modes.
//        val photo_mode_pref = sharedPreferences.getString(PreferenceKeys.PhotoModePreferenceKey, "preference_photo_mode_std")
//        val dro = photo_mode_pref == "preference_photo_mode_dro"
//        if (dro && main_activity.supportsDRO())
//            return PhotoMode.DRO
//        val hdr = photo_mode_pref == "preference_photo_mode_hdr"
//        if (hdr && main_activity.supportsHDR())
//            return PhotoMode.HDR
//        val expo_bracketing = photo_mode_pref == "preference_photo_mode_expo_bracketing"
//        if (expo_bracketing && main_activity.supportsExpoBracketing())
//            return PhotoMode.ExpoBracketing
//        val noise_reduction = photo_mode_pref == "preference_photo_mode_noise_reduction"
//        return if (noise_reduction && main_activity.supportsNoiseReduction()) PhotoMode.NoiseReduction else PhotoMode.Standard
//    }

    override fun getOptimiseAEForDROPref(): Boolean {
        // val photo_mode = getPhotoMode()
        //return photo_mode == PhotoMode.DRO
        return false;
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

    }

    override fun setFlashPref(flash_value: String?) {

    }

    override fun setFocusPref(focus_value: String?, is_video: Boolean) {

    }

    override fun setVideoPref(is_video: Boolean) {

    }

    override fun setSceneModePref(scene_mode: String?) {

    }

    override fun clearSceneModePref() {

    }

    override fun setColorEffectPref(color_effect: String?) {

    }

    override fun clearColorEffectPref() {

    }

    override fun setWhiteBalancePref(white_balance: String?) {

    }

    override fun clearWhiteBalancePref() {

    }

    override fun setWhiteBalanceTemperaturePref(white_balance_temperature: Int) {

    }

    override fun setISOPref(iso: String?) {

    }

    override fun clearISOPref() {

    }

    override fun setExposureCompensationPref(exposure: Int) {

    }

    override fun clearExposureCompensationPref() {

    }

    override fun setCameraResolutionPref(width: Int, height: Int) {

    }

    override fun setVideoQualityPref(video_quality: String?) {

    }

    override fun setZoomPref(zoom: Int) {

    }

    override fun requestCameraPermission() {

    }

    override fun requestStoragePermission() {

    }

    override fun requestRecordAudioPermission() {

    }

    override fun setExposureTimePref(exposure_time: Long) {

    }

    override fun clearExposureTimePref() {

    }

    override fun setFocusDistancePref(focus_distance: Float) {

    }


    override fun getFlashPref(): String {
        return sharedPreferences.getString(PreferenceKeys.getFlashPreferenceKey(cameraId), "")
    }

    override fun getFocusPref(is_video: Boolean): String {
        return sharedPreferences.getString(PreferenceKeys.getFocusPreferenceKey(cameraId, is_video), "")
    }

    override fun isVideoPref(): Boolean {
        return sharedPreferences.getBoolean(PreferenceKeys.IsVideoPreferenceKey, false)
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
//        if (MyDebug.LOG)
//            Log.d(TAG, "getImageQualityPref")
//        // see documentation for getSaveImageQualityPref(): in DRO mode we want to take the photo
//        // at 100% quality for post-processing, the final image will then be saved at the user requested
//        // setting
//        val photo_mode = getPhotoMode()
//        if (photo_mode == PhotoMode.DRO)
//            return 100
//        else if (photo_mode == PhotoMode.NoiseReduction)
//            return 100
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
//        return if (cameraId == 0 && sharedPreferences.getBoolean(PreferenceKeys.getForceVideo4KPreferenceKey(), false) && main_activity.supportsForceVideo4K()) {
//            true
//        } else false
        return false;
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
        /*if (!storageUtils.isUsingSAF()) {
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
                val free_memory = main_activity.freeMemory() * 1024 * 1024
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
                    if (video_max_filesize.max_filesize === 0 || video_max_filesize.max_filesize > available_memory) {
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
        }*/

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

    // note, okay to change the order of enums in future versions, as getPhotoMode() does not rely on the order for the saved photo mode
    enum class PhotoMode {
        Standard,
        DRO, // single image "fake" HDR
        HDR, // HDR created from multiple (expo bracketing) images
        ExpoBracketing, // take multiple expo bracketed images, without combining to a single image
        NoiseReduction
    }
}