package com.dastanapps.camera.listeners

import android.app.Activity
import android.content.Context
import android.hardware.SensorManager
import android.os.Handler
import android.util.Log
import android.view.OrientationEventListener
import com.dastanapps.CommonUtils
import com.dastanapps.view.Util

/**
 * Created by dastaniqbal on 19/01/2018.
 * dastanIqbal@marvelmedia.com
 * 19/01/2018 11:08
 */

class Cam1OrientationEventListener(val context: Context, val handler: Handler) : android.view.OrientationEventListener(context, SensorManager.SENSOR_DELAY_NORMAL) {
    private var mOrientation: Int = 0
    private var mOrientationCompensation: Int = 0

    override fun onOrientationChanged(orientation: Int) {
        // We keep the last known orientation. So if the user first orient
        // the camera then point the camera to floor or sky, we still have
        // the correct orientation.
        if (orientation == OrientationEventListener.ORIENTATION_UNKNOWN) return
        mOrientation = Util.roundOrientation(orientation, mOrientation)
        // When the screen is unlocked, display rotation may change. Always
        // calculate the up-to-date orientationCompensation.
        val orientationCompensation = mOrientation + Util.getDisplayRotation(context as Activity?)
        if (mOrientationCompensation != orientationCompensation) {
            mOrientationCompensation = orientationCompensation
        }

        Log.d("!!!!", "rotation!!! lastOrientation: $orientation mOrientationCompensation: $mOrientationCompensation")
        CommonUtils.sendMessageToHandler(handler, 3, mOrientationCompensation)
    }
}
