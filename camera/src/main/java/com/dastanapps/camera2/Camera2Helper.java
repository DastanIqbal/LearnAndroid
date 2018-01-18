package com.dastanapps.camera2;

import android.app.Activity;
import android.graphics.Matrix;
import android.graphics.RectF;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureRequest;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.util.Log;
import android.util.Size;
import android.view.Surface;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by dastaniqbal on 17/01/2018.
 * dastanIqbal@marvelmedia.com
 * 17/01/2018 10:21
 */
@RequiresApi(Build.VERSION_CODES.LOLLIPOP)
public class Camera2Helper {
    private static String TAG = Camera2Helper.class.getSimpleName();

    /**
     * Compares two {@code Size}s based on their areas.
     */
    static class CompareSizesByArea implements Comparator<Size> {

        @Override
        public int compare(Size lhs, Size rhs) {
            // We cast here to ensure the multiplications won't overflow
            return Long.signum((long) lhs.getWidth() * lhs.getHeight() -
                    (long) rhs.getWidth() * rhs.getHeight());
        }

    }

    /**
     * Configures the necessary {@link Matrix} transformation to `mTextureView`.
     * This method should not to be called until the camera preview size is determined in
     * openCamera, or until the size of `mTextureView` is fixed.
     *
     * @param mPreviewSize
     * @param mTextureView
     * @param viewWidth    The width of `mTextureView`
     * @param viewHeight   The height of `mTextureView`
     */
    public static void configureTransform(Activity activity, Size mPreviewSize, AutoFitTextureView mTextureView, int viewWidth, int viewHeight) {
        if (null == mTextureView || null == mPreviewSize || null == activity) {
            return;
        }
        int rotation = activity.getWindowManager().getDefaultDisplay().getRotation();
        Matrix matrix = new Matrix();
        RectF viewRect = new RectF(0, 0, viewWidth, viewHeight);
        RectF bufferRect = new RectF(0, 0, mPreviewSize.getHeight(), mPreviewSize.getWidth());
        float centerX = viewRect.centerX();
        float centerY = viewRect.centerY();
        if (Surface.ROTATION_90 == rotation || Surface.ROTATION_270 == rotation) {
            bufferRect.offset(centerX - bufferRect.centerX(), centerY - bufferRect.centerY());
            matrix.setRectToRect(viewRect, bufferRect, Matrix.ScaleToFit.FILL);
            float scale = Math.max(
                    (float) viewHeight / mPreviewSize.getHeight(),
                    (float) viewWidth / mPreviewSize.getWidth());
            matrix.postScale(scale, scale, centerX, centerY);
            matrix.postRotate(90 * (rotation - 2), centerX, centerY);
        }
        mTextureView.setTransform(matrix);
    }

    /**
     * Return true if the given array contains the given integer.
     *
     * @param modes array to check.
     * @param mode  integer to get for.
     * @return true if the array contains the given integer, otherwise false.
     */
    public static boolean contains(int[] modes, int mode) {
        if (modes == null) {
            return false;
        }
        for (int i : modes) {
            if (i == mode) {
                return true;
            }
        }
        return false;
    }

    /**
     * In this sample, we choose a video size with 3x4 aspect ratio. Also, we don't use sizes
     * larger than 1080p, since MediaRecorder cannot handle such a high-resolution video.
     *
     * @param choices The list of available sizes
     * @return The video size
     */
    public static Size chooseVideoSize(Size[] choices) {
        for (Size size : choices) {
            if (size.getWidth() == size.getHeight() * 4 / 3 && size.getWidth() <= 1080) {
                return size;
            }
        }
        Log.e(TAG, "Couldn't find any suitable video size");
        return choices[choices.length - 1];
    }

    /**
     * Given {@code choices} of {@code Size}s supported by a camera, chooses the smallest one whose
     * width and height are at least as large as the respective requested values, and whose aspect
     * ratio matches with the specified value.
     *
     * @param choices     The list of sizes that the camera supports for the intended output class
     * @param width       The minimum desired width
     * @param height      The minimum desired height
     * @param aspectRatio The aspect ratio
     * @return The optimal {@code Size}, or an arbitrary one if none were big enough
     */
    public static Size chooseOptimalSize(Size[] choices, int width, int height, Size aspectRatio) {
        // Collect the supported resolutions that are at least as big as the preview Surface
        List<Size> bigEnough = new ArrayList<>();
        int w = aspectRatio.getWidth();
        int h = aspectRatio.getHeight();
        for (Size option : choices) {
            if (option.getHeight() == option.getWidth() * h / w &&
                    option.getWidth() >= width && option.getHeight() >= height) {
                bigEnough.add(option);
            }
        }

        // Pick the smallest of those, assuming we found any
        if (bigEnough.size() > 0) {
            return Collections.min(bigEnough, new Camera2Helper.CompareSizesByArea());
        } else {
            Log.e(TAG, "Couldn't find any suitable preview size");
            return choices[0];
        }
    }

    public static String getEffectName(int effect) {
        switch (effect) {
            /**
             * <p>A "monocolor" effect where the image is mapped into
             * a single color.</p>
             * <p>This will typically be grayscale.</p>
             * @see CaptureRequest#CONTROL_EFFECT_MODE
             */
            case CameraMetadata.CONTROL_EFFECT_MODE_MONO:
                return "MONO";

            /**
             * <p>A "photo-negative" effect where the image's colors
             * are inverted.</p>
             * @see CaptureRequest#CONTROL_EFFECT_MODE
             */
            case CameraMetadata.CONTROL_EFFECT_MODE_NEGATIVE:
                return "NEGATIVE";

            /**
             * <p>A "solarisation" effect (Sabattier effect) where the
             * image is wholly or partially reversed in
             * tone.</p>
             * @see CaptureRequest#CONTROL_EFFECT_MODE
             */
            case CameraMetadata.CONTROL_EFFECT_MODE_SOLARIZE:
                return "SOLARIZE";

            /**
             * <p>A "sepia" effect where the image is mapped into warm
             * gray, red, and brown tones.</p>
             * @see CaptureRequest#CONTROL_EFFECT_MODE
             */
            case CameraMetadata.CONTROL_EFFECT_MODE_SEPIA:
                return "SEPIA";

            /**
             * <p>A "posterization" effect where the image uses
             * discrete regions of tone rather than a continuous
             * gradient of tones.</p>
             * @see CaptureRequest#CONTROL_EFFECT_MODE
             */
            case CameraMetadata.CONTROL_EFFECT_MODE_POSTERIZE:
                return "POSTERIZE";

            /**
             * <p>A "whiteboard" effect where the image is typically displayed
             * as regions of white, with black or grey details.</p>
             * @see CaptureRequest#CONTROL_EFFECT_MODE
             */
            case CameraMetadata.CONTROL_EFFECT_MODE_WHITEBOARD:
                return "WHITEBOARD";

            /**
             * <p>A "blackboard" effect where the image is typically displayed
             * as regions of black, with white or grey details.</p>
             * @see CaptureRequest#CONTROL_EFFECT_MODE
             */
            case CameraMetadata.CONTROL_EFFECT_MODE_BLACKBOARD:
                return "BLACKBOARD";

            /**
             * <p>An "aqua" effect where a blue hue is added to the image.</p>
             * @see CaptureRequest#CONTROL_EFFECT_MODE
             */
            case CameraMetadata.CONTROL_EFFECT_MODE_AQUA:
                return "AQUA";
            /**
             * <p>No color effect will be applied.</p>
             * @see CaptureRequest#CONTROL_EFFECT_MODE
             */
            default:
                return "OFF";

        }
    }

    public static String getSceneNames(int scenes) {
        switch (scenes) {
            /**
             * <p>Indicates that no scene modes are set for a given capture request.</p>
             * @see CaptureRequest#CONTROL_SCENE_MODE
             */
            case CameraMetadata.CONTROL_SCENE_MODE_DISABLED:
                return "DISABLED";

            /**
             * <p>If face detection support exists, use face
             * detection data for auto-focus, auto-white balance, and
             * auto-exposure routines.</p>
             * <p>If face detection statistics are disabled
             * (i.e. {@link CaptureRequest#STATISTICS_FACE_DETECT_MODE android.statistics.faceDetectMode} is set to OFF),
             * this should still operate correctly (but will not return
             * face detection statistics to the framework).</p>
             * <p>Unlike the other scene modes, {@link CaptureRequest#CONTROL_AE_MODE android.control.aeMode},
             * {@link CaptureRequest#CONTROL_AWB_MODE android.control.awbMode}, and {@link CaptureRequest#CONTROL_AF_MODE android.control.afMode}
             * remain active when FACE_PRIORITY is set.</p>
             *
             * @see CaptureRequest#CONTROL_AE_MODE
             * @see CaptureRequest#CONTROL_AF_MODE
             * @see CaptureRequest#CONTROL_AWB_MODE
             * @see CaptureRequest#STATISTICS_FACE_DETECT_MODE
             * @see CaptureRequest#CONTROL_SCENE_MODE
             */
            case CameraMetadata.CONTROL_SCENE_MODE_FACE_PRIORITY:
                return "FACE PRIORITY";

            /**
             * <p>Optimized for photos of quickly moving objects.</p>
             * <p>Similar to SPORTS.</p>
             * @see CaptureRequest#CONTROL_SCENE_MODE
             */
            case CameraMetadata.CONTROL_SCENE_MODE_ACTION:
                return "ACTION";

            /**
             * <p>Optimized for still photos of people.</p>
             * @see CaptureRequest#CONTROL_SCENE_MODE
             */
            case CameraMetadata.CONTROL_SCENE_MODE_PORTRAIT:
                return "PORTRAIT";

            /**
             * <p>Optimized for photos of distant macroscopic objects.</p>
             * @see CaptureRequest#CONTROL_SCENE_MODE
             */
            case CameraMetadata.CONTROL_SCENE_MODE_LANDSCAPE:
                return "LANDSCAPE";

            /**
             * <p>Optimized for low-light settings.</p>
             * @see CaptureRequest#CONTROL_SCENE_MODE
             */
            case CameraMetadata.CONTROL_SCENE_MODE_NIGHT:
                return "NIGHT";

            /**
             * <p>Optimized for still photos of people in low-light
             * settings.</p>
             * @see CaptureRequest#CONTROL_SCENE_MODE
             */
            case CameraMetadata.CONTROL_SCENE_MODE_NIGHT_PORTRAIT:
                return "NIGHT PORTRAIT";

            /**
             * <p>Optimized for dim, indoor settings where flash must
             * remain off.</p>
             * @see CaptureRequest#CONTROL_SCENE_MODE
             */
            case CameraMetadata.CONTROL_SCENE_MODE_THEATRE:
                return "THEATRE";

            /**
             * <p>Optimized for bright, outdoor beach settings.</p>
             * @see CaptureRequest#CONTROL_SCENE_MODE
             */
            case CameraMetadata.CONTROL_SCENE_MODE_BEACH:
                return "BEACH";

            /**
             * <p>Optimized for bright, outdoor settings containing snow.</p>
             * @see CaptureRequest#CONTROL_SCENE_MODE
             */
            case CameraMetadata.CONTROL_SCENE_MODE_SNOW:
                return "SNOW";

            /**
             * <p>Optimized for scenes of the setting sun.</p>
             * @see CaptureRequest#CONTROL_SCENE_MODE
             */
            case CameraMetadata.CONTROL_SCENE_MODE_SUNSET:
                return "SUNSET";

            /**
             * <p>Optimized to avoid blurry photos due to small amounts of
             * device motion (for example: due to hand shake).</p>
             * @see CaptureRequest#CONTROL_SCENE_MODE
             */
            case CameraMetadata.CONTROL_SCENE_MODE_STEADYPHOTO:
                return "STEADY PHOTO";

            /**
             * <p>Optimized for nighttime photos of fireworks.</p>
             * @see CaptureRequest#CONTROL_SCENE_MODE
             */
            case CameraMetadata.CONTROL_SCENE_MODE_FIREWORKS:
                return "FIREWORKS";

            /**
             * <p>Optimized for photos of quickly moving people.</p>
             * <p>Similar to ACTION.</p>
             * @see CaptureRequest#CONTROL_SCENE_MODE
             */
            case CameraMetadata.CONTROL_SCENE_MODE_SPORTS:
                return "SPORTS";

            /**
             * <p>Optimized for dim, indoor settings with multiple moving
             * people.</p>
             * @see CaptureRequest#CONTROL_SCENE_MODE
             */
            case CameraMetadata.CONTROL_SCENE_MODE_PARTY:
                return "PARTY";

            /**
             * <p>Optimized for dim settings where the main light source
             * is a flame.</p>
             * @see CaptureRequest#CONTROL_SCENE_MODE
             */
            case CameraMetadata.CONTROL_SCENE_MODE_CANDLELIGHT:
                return "CANDLELIGHT";

            /**
             * <p>Optimized for accurately capturing a photo of barcode
             * for use by camera applications that wish to read the
             * barcode value.</p>
             * @see CaptureRequest#CONTROL_SCENE_MODE
             */
            case CameraMetadata.CONTROL_SCENE_MODE_BARCODE:
                return "BARCODE";

            /**
             * <p>Optimized for high speed video recording (frame rate &gt;=60fps) use case.</p>
             * <p>The supported high speed video sizes and fps ranges are specified in
             * android.control.availableHighSpeedVideoConfigurations. To get desired
             * output frame rates, the application is only allowed to select video size
             * and fps range combinations listed in this static metadata. The fps range
             * can be control via {@link CaptureRequest#CONTROL_AE_TARGET_FPS_RANGE android.control.aeTargetFpsRange}.</p>
             * <p>In this mode, the camera device will override aeMode, awbMode, and afMode to
             * ON, ON, and CONTINUOUS_VIDEO, respectively. All post-processing block mode
             * controls will be overridden to be FAST. Therefore, no manual control of capture
             * and post-processing parameters is possible. All other controls operate the
             * same as when {@link CaptureRequest#CONTROL_MODE android.control.mode} == AUTO. This means that all other
             * android.control.* fields continue to work, such as</p>
             * <ul>
             * <li>{@link CaptureRequest#CONTROL_AE_TARGET_FPS_RANGE android.control.aeTargetFpsRange}</li>
             * <li>{@link CaptureRequest#CONTROL_AE_EXPOSURE_COMPENSATION android.control.aeExposureCompensation}</li>
             * <li>{@link CaptureRequest#CONTROL_AE_LOCK android.control.aeLock}</li>
             * <li>{@link CaptureRequest#CONTROL_AWB_LOCK android.control.awbLock}</li>
             * <li>{@link CaptureRequest#CONTROL_EFFECT_MODE android.control.effectMode}</li>
             * <li>{@link CaptureRequest#CONTROL_AE_REGIONS android.control.aeRegions}</li>
             * <li>{@link CaptureRequest#CONTROL_AF_REGIONS android.control.afRegions}</li>
             * <li>{@link CaptureRequest#CONTROL_AWB_REGIONS android.control.awbRegions}</li>
             * <li>{@link CaptureRequest#CONTROL_AF_TRIGGER android.control.afTrigger}</li>
             * <li>{@link CaptureRequest#CONTROL_AE_PRECAPTURE_TRIGGER android.control.aePrecaptureTrigger}</li>
             * </ul>
             * <p>Outside of android.control.*, the following controls will work:</p>
             * <ul>
             * <li>{@link CaptureRequest#FLASH_MODE android.flash.mode} (automatic flash for still capture will not work since aeMode is ON)</li>
             * <li>{@link CaptureRequest#LENS_OPTICAL_STABILIZATION_MODE android.lens.opticalStabilizationMode} (if it is supported)</li>
             * <li>{@link CaptureRequest#SCALER_CROP_REGION android.scaler.cropRegion}</li>
             * <li>{@link CaptureRequest#STATISTICS_FACE_DETECT_MODE android.statistics.faceDetectMode}</li>
             * </ul>
             * <p>For high speed recording use case, the actual maximum supported frame rate may
             * be lower than what camera can output, depending on the destination Surfaces for
             * the image data. For example, if the destination surface is from video encoder,
             * the application need check if the video encoder is capable of supporting the
             * high frame rate for a given video size, or it will end up with lower recording
             * frame rate. If the destination surface is from preview window, the preview frame
             * rate will be bounded by the screen refresh rate.</p>
             * <p>The camera device will only support up to 2 output high speed streams
             * (processed non-stalling format defined in android.request.maxNumOutputStreams)
             * in this mode. This control will be effective only if all of below conditions are true:</p>
             * <ul>
             * <li>The application created no more than maxNumHighSpeedStreams processed non-stalling
             * format output streams, where maxNumHighSpeedStreams is calculated as
             * min(2, android.request.maxNumOutputStreams[Processed (but not-stalling)]).</li>
             * <li>The stream sizes are selected from the sizes reported by
             * android.control.availableHighSpeedVideoConfigurations.</li>
             * <li>No processed non-stalling or raw streams are configured.</li>
             * </ul>
             * <p>When above conditions are NOT satistied, the controls of this mode and
             * {@link CaptureRequest#CONTROL_AE_TARGET_FPS_RANGE android.control.aeTargetFpsRange} will be ignored by the camera device,
             * the camera device will fall back to {@link CaptureRequest#CONTROL_MODE android.control.mode} <code>==</code> AUTO,
             * and the returned capture result metadata will give the fps range choosen
             * by the camera device.</p>
             * <p>Switching into or out of this mode may trigger some camera ISP/sensor
             * reconfigurations, which may introduce extra latency. It is recommended that
             * the application avoids unnecessary scene mode switch as much as possible.</p>
             *
             * @see CaptureRequest#CONTROL_AE_EXPOSURE_COMPENSATION
             * @see CaptureRequest#CONTROL_AE_LOCK
             * @see CaptureRequest#CONTROL_AE_PRECAPTURE_TRIGGER
             * @see CaptureRequest#CONTROL_AE_REGIONS
             * @see CaptureRequest#CONTROL_AE_TARGET_FPS_RANGE
             * @see CaptureRequest#CONTROL_AF_REGIONS
             * @see CaptureRequest#CONTROL_AF_TRIGGER
             * @see CaptureRequest#CONTROL_AWB_LOCK
             * @see CaptureRequest#CONTROL_AWB_REGIONS
             * @see CaptureRequest#CONTROL_EFFECT_MODE
             * @see CaptureRequest#CONTROL_MODE
             * @see CaptureRequest#FLASH_MODE
             * @see CaptureRequest#LENS_OPTICAL_STABILIZATION_MODE
             * @see CaptureRequest#SCALER_CROP_REGION
             * @see CaptureRequest#STATISTICS_FACE_DETECT_MODE
             * @see CaptureRequest#CONTROL_SCENE_MODE
             */
            case CameraMetadata.CONTROL_SCENE_MODE_HIGH_SPEED_VIDEO:
                return "HIGHT SPEED VIDEO";

            /**
             * <p>Turn on custom high dynamic range (HDR) mode.</p>
             * <p>This is intended for LEGACY mode devices only;
             * HAL3+ camera devices should not implement this mode.</p>
             * @see CaptureRequest#CONTROL_SCENE_MODE
             * @hide
             */
            case CameraMetadata.CONTROL_SCENE_MODE_HDR:
                return "HDR";
            default:
                return "UNKNOWN";
        }
    }
}
