package com.dastanapps

import android.util.Size

/**
 * Created by dastaniqbal on 19/01/2018.
 * dastanIqbal@marvelmedia.com
 * 19/01/2018 12:39
 */

interface ICamera {
    fun cameraOperned(mPreviewSize: Size)

    fun cameraError(error: Int)

    fun cameraRecordingStarted()

    fun cameraRecordingStopped()

    fun updateFlashMode(flashMode: Int)

    fun orientationChanged(rotation: Int)

}
