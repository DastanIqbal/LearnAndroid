package com.dastanapps.gameframework.impl

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager

/**
 * Created by dastaniqbal on 09/10/2017.
 * dastanIqbal@marvelmedia.com
 * 09/10/2017 11:12
 */
class AccelerometerHandler(context: Context) : SensorEventListener {
    var accX = 0.0f
    var accY = 0.0f
    var accZ = 0.0f

    init {
        val manager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        if (manager.getSensorList(Sensor.TYPE_ACCELEROMETER).size != 0) {
            val acceleroMeter = manager.getSensorList(Sensor.TYPE_ACCELEROMETER)[0]
            manager.registerListener(this, acceleroMeter, SensorManager.SENSOR_DELAY_GAME)
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    override fun onSensorChanged(event: SensorEvent) {
        accX = event.values[0]
        accY = event.values[1]
        accZ = event.values[2]
    }
}