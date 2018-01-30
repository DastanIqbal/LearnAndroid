package com.dastanapps.camera

import android.app.Activity
import android.graphics.Matrix
import android.graphics.RectF
import android.hardware.Camera
import android.hardware.Camera.Size
import android.media.CamcorderProfile
import android.os.Build
import android.support.v4.util.Pair
import android.util.Log
import android.view.Surface
import com.dastanapps.CameraHelper
import com.dastanapps.view.AutoFitTextureView
import java.util.*

/**
 * Created by dastaniqbal on 19/01/2018.
 * dastanIqbal@marvelmedia.com
 * 19/01/2018 11:15
 */

class Camera1Helper : CameraHelper() {

    /**
     * Compares two `Size`s based on their areas.
     */
    internal class CompareSizesByArea : Comparator<Camera.Size> {

        override fun compare(lhs: Camera.Size, rhs: Camera.Size): Int {
            // We cast here to ensure the multiplications won't overflow
            return java.lang.Long.signum(lhs.width.toLong() * lhs.height - rhs.width.toLong() * rhs.height)
        }

    }

    companion object {

        fun getCameraInfo(cameraFacking: Int): Pair<Camera.CameraInfo, Int>? {
            val cameraInfo = Camera.CameraInfo()
            val numberOfCameras = Camera.getNumberOfCameras()

            for (i in 0 until numberOfCameras) {
                Camera.getCameraInfo(i, cameraInfo)
                if (cameraInfo.facing == cameraFacking) {
                    return Pair<Camera.CameraInfo, Int>(cameraInfo,
                            Integer.valueOf(i))
                }
            }
            return null
        }

        fun setDisplayOrientation(activity: Activity?, orientation: Int): Int {
            val rotation = activity?.windowManager?.defaultDisplay?.rotation
            var degrees = 0
            when (rotation) {
                Surface.ROTATION_0 -> degrees = 0
                Surface.ROTATION_90 -> degrees = 90
                Surface.ROTATION_180 -> degrees = 180
                Surface.ROTATION_270 -> degrees = 270
            }

            return (orientation - degrees + 360) % 360
        }

        /**
         * In this sample, we choose a video size with 3x4 aspect ratio. Also, we don't use sizes
         * larger than 1080p, since MediaRecorder cannot handle such a high-resolution video.
         *
         * @param choices The list of available sizes
         * @return The video size
         */
        fun chooseVideoSize(choices: List<Camera.Size>): Camera.Size {
            for (size in choices) {
                if (size.width == size.height * 16 / 9 && size.width <= 1080) {
                    return size
                }
            }
            Log.e(CameraHelper.TAG, "Couldn't find any suitable video size")
            return choices[choices.size - 1]
        }

        /**
         * Given `choices` of `Camera.Size`s supported by a camera, chooses the smallest one whose
         * width and height are at least as large as the respective requested values, and whose aspect
         * ratio matches with the specified value.
         *
         * @param choices     The list of sizes that the camera supports for the intended output class
         * @param width       The minimum desired width
         * @param height      The minimum desired height
         * @param aspectRatio The aspect ratio
         * @return The optimal `Camera.Size`, or an arbitrary one if none were big enough
         */
        fun chooseOptimalSize(choices: List<Camera.Size>, width: Int, height: Int, aspectRatio: Camera.Size): Camera.Size {
            // Collect the supported resolutions that are at least as big as the preview Surface
            val bigEnough = ArrayList<Camera.Size>()
            val w = aspectRatio.width
            val h = aspectRatio.height
            for (option in choices) {
                if (option.height == option.width * h / w &&
                        option.width >= width && option.height >= height) {
                    bigEnough.add(option)
                }
            }

            // Pick the smallest of those, assuming we found any
            if (bigEnough.size > 0) {
                return Collections.min(bigEnough, Camera1Helper.CompareSizesByArea())
            } else {
                Log.e(CameraHelper.TAG, "Couldn't find any suitable preview size")
                return choices[0]
            }
        }

        /**
         * Configures the necessary [Matrix] transformation to `mTextureView`.
         * This method should not to be called until the camera preview size is determined in
         * openCamera, or until the size of `mTextureView` is fixed.
         *
         * @param mPreviewSize
         * @param mTextureView
         * @param viewWidth    The width of `mTextureView`
         * @param viewHeight   The height of `mTextureView`
         */
        fun configureTransform(activity: Activity?, mPreviewSize: Size?, mTextureView: AutoFitTextureView?, viewWidth: Int, viewHeight: Int) {
            if (null == mTextureView || null == mPreviewSize || null == activity) {
                return
            }
            val rotation = activity.windowManager.defaultDisplay.rotation
            val matrix = Matrix()
            val viewRect = RectF(0f, 0f, viewWidth.toFloat(), viewHeight.toFloat())
            val bufferRect = RectF(0f, 0f, mPreviewSize.height.toFloat(), mPreviewSize.width.toFloat())
            val centerX = viewRect.centerX()
            val centerY = viewRect.centerY()
            if (Surface.ROTATION_90 == rotation || Surface.ROTATION_270 == rotation) {
                bufferRect.offset(centerX - bufferRect.centerX(), centerY - bufferRect.centerY())
                matrix.setRectToRect(viewRect, bufferRect, Matrix.ScaleToFit.FILL)
                val scale = Math.max(
                        viewHeight.toFloat() / mPreviewSize.height,
                        viewWidth.toFloat() / mPreviewSize.width)
                matrix.postScale(scale, scale, centerX, centerY)
                matrix.postRotate((90 * (rotation - 2)).toFloat(), centerX, centerY)
            }
            mTextureView.setTransform(matrix)
        }

        val baseRecordingProfile: CamcorderProfile
            get() {
                val returnProfile: CamcorderProfile
                if (CamcorderProfile.hasProfile(CamcorderProfile.QUALITY_720P)) {
                    returnProfile = CamcorderProfile.get(CamcorderProfile.QUALITY_720P)
                } else if (CamcorderProfile.hasProfile(CamcorderProfile.QUALITY_480P)) {
                    returnProfile = CamcorderProfile.get(CamcorderProfile.QUALITY_480P)
                } else {
                    returnProfile = defaultRecordingProfile
                }
                return returnProfile
            }

        val defaultRecordingProfile: CamcorderProfile
            get() {
                val highProfile = CamcorderProfile.get(CamcorderProfile.QUALITY_HIGH)
                if (highProfile != null) {
                    return highProfile
                }
                val lowProfile = CamcorderProfile.get(CamcorderProfile.QUALITY_LOW)
                if (lowProfile != null) {
                    return lowProfile
                }
                throw RuntimeException("No quality level found")
            }

        /**
         * Copyright (C) 2013 The Android Open Source Project
         *
         *
         * Licensed under the Apache License, Version 2.0 (the "License");
         * you may not use this file except in compliance with the License.
         * You may obtain a copy of the License at
         *
         *
         * http://www.apache.org/licenses/LICENSE-2.0
         *
         *
         * Unless required by applicable law or agreed to in writing, software
         * distributed under the License is distributed on an "AS IS" BASIS,
         * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
         * See the License for the specific language governing permissions and
         * limitations under the License.
         */
        fun getOptimalSize(sizes: List<Camera.Size>?, w: Int, h: Int): Size? {
            // Use a very small tolerance because we want an exact match.
            val ASPECT_TOLERANCE = 0.1
            val targetRatio = w.toDouble() / h
            if (sizes == null) return null

            var optimalSize: Camera.Size? = null

            // Start with max value and refine as we iterate over available preview sizes. This is the
            // minimum difference between view and camera height.
            var minDiff = java.lang.Double.MAX_VALUE

            // Target view height

            // Try to find a preview size that matches aspect ratio and the target view size.
            // Iterate over all available sizes and pick the largest size that can fit in the view and
            // still maintain the aspect ratio.
            for (size in sizes) {
                val ratio = size.width.toDouble() / size.height
                if (Math.abs(ratio - targetRatio) > ASPECT_TOLERANCE) {
                    continue
                }
                if (Math.abs(size.height - h) < minDiff) {
                    optimalSize = size
                    minDiff = Math.abs(size.height - h).toDouble()
                }
            }

            // Cannot find preview size that matches the aspect ratio, ignore the requirement
            if (optimalSize == null) {
                minDiff = java.lang.Double.MAX_VALUE
                for (size in sizes) {
                    if (Math.abs(size.height - h) < minDiff) {
                        optimalSize = size
                        minDiff = Math.abs(size.height - h).toDouble()
                    }
                }
            }
            return optimalSize
        }

        fun getSupportedRecordingSize(camera: Camera, width: Int, height: Int): Pair<Int, Int> {
            val recordingSize = getOptimalSize(getSupportedVideoSizes(camera, Build.VERSION.SDK_INT), width, height)
                    ?: return Pair(width, height)
            return Pair(recordingSize.width, recordingSize.height)
        }

        private fun getSupportedVideoSizes(camera: Camera, currentSdkInt: Int): List<Size> {
            val params = camera.parameters

            val supportedVideoSizes: List<Size>
            if (currentSdkInt < Build.VERSION_CODES.HONEYCOMB) {
                supportedVideoSizes = params.supportedPreviewSizes
            } else if (params.supportedVideoSizes == null) {
                supportedVideoSizes = params.supportedPreviewSizes
            } else {
                supportedVideoSizes = params.supportedVideoSizes
            }

            return supportedVideoSizes
        }
    }
}
