package com.dastanapps

import android.os.Build
import android.util.SparseIntArray
import android.view.Surface
import com.dastanapps.camera2.Camera2Helper

/**
 * Created by dastaniqbal on 19/01/2018.

 * 19/01/2018 1:06
 */

open class CameraHelper {
    companion object {

        var TAG = CameraHelper::class.java.simpleName

        val DEFAULT_ORIENTATIONS = SparseIntArray()
        val INVERSE_ORIENTATIONS = SparseIntArray()

        init {
            DEFAULT_ORIENTATIONS.append(Surface.ROTATION_0, 90)
            DEFAULT_ORIENTATIONS.append(Surface.ROTATION_90, 0)
            DEFAULT_ORIENTATIONS.append(Surface.ROTATION_180, 270)
            DEFAULT_ORIENTATIONS.append(Surface.ROTATION_270, 180)
        }

        init {
            INVERSE_ORIENTATIONS.append(Surface.ROTATION_0, 270)
            INVERSE_ORIENTATIONS.append(Surface.ROTATION_90, 180)
            INVERSE_ORIENTATIONS.append(Surface.ROTATION_180, 90)
            INVERSE_ORIENTATIONS.append(Surface.ROTATION_270, 0)
        }

        fun isCamera2Supported(): Boolean {
            return Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && Camera2Helper.allowCamera2Support()
        }
    }
}
