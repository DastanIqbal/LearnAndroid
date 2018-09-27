package com.dastanapps.gameframework.impl

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager

/**
 * Created by dastaniqbal on 09/10/2017.

 * 09/10/2017 11:12
 */
class CompassHandler(context: Context) : SensorEventListener {
    var yaw = 0.0f
    var pitch = 0.0f
    var roll = 0.0f
    var mAccelerometer: Sensor
    var mMagnetometer: Sensor
    val mLastAcceleroMeter = floatArrayOf(3f)
    val mLastMagnetoMeter = floatArrayOf(3f)
    var mLastAcceleroMeterSet = false
    var mLastMagnetoMeterSet = false
    val mR = floatArrayOf(9f)
    val mOrientation = floatArrayOf(3f)

    init {
        val manager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        mAccelerometer = manager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        mMagnetometer = manager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    override fun onSensorChanged(event: SensorEvent) {
        if (event.sensor == mAccelerometer) {
            System.arraycopy(event.values, 0, mLastAcceleroMeter, 0, event.values.size)
            mLastAcceleroMeterSet = true
        } else if (event.sensor == mMagnetometer) {
            System.arraycopy(event.values, 0, mLastMagnetoMeter, 0, event.values.size)
            mLastMagnetoMeterSet = true
        }

        if (mLastAcceleroMeterSet && mLastMagnetoMeterSet) {
            SensorManager.getRotationMatrix(mR, null, mLastAcceleroMeter, mLastMagnetoMeter)
            SensorManager.getOrientation(mR, mOrientation)
            yaw = mOrientation[0]
            pitch = mOrientation[1]
            roll = mOrientation[2]
        }
    }
}