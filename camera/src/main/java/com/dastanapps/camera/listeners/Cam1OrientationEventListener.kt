package com.dastanapps.camera.listeners

import android.content.Context
import android.hardware.SensorManager
import android.util.Log
import android.view.Surface

/**
 * Created by dastaniqbal on 19/01/2018.
 * dastanIqbal@marvelmedia.com
 * 19/01/2018 11:08
 */

class Cam1OrientationEventListener(context: Context) : android.view.OrientationEventListener(context, SensorManager.SENSOR_DELAY_NORMAL) {
    private var mOrientation: Int = 0

    override fun onOrientationChanged(orientation: Int) {
        val lastOrientation = mOrientation

        if (orientation >= 315 || orientation < 45) {
            if (mOrientation != Surface.ROTATION_0) {
                mOrientation = Surface.ROTATION_0
            }
        } else if (orientation >= 45 && orientation < 135) {
            if (mOrientation != Surface.ROTATION_90) {
                mOrientation = Surface.ROTATION_90
            }
        } else if (orientation >= 135 && orientation < 225) {
            if (mOrientation != Surface.ROTATION_180) {
                mOrientation = Surface.ROTATION_180
            }
        } else if (mOrientation != Surface.ROTATION_270) {
            mOrientation = Surface.ROTATION_270
        }

        if (lastOrientation != mOrientation) {
            Log.d("!!!!", "rotation!!! lastOrientation:"
                    + lastOrientation + " mOrientation:"
                    + mOrientation + " orientaion:"
                    + orientation)
        }
    }
}
