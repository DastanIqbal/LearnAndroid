package com.dastanapps.testframework.test

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.WindowManager
import android.widget.TextView

/**
 * Created by dastaniqbal on 03/07/2017.

 * 03/07/2017 9:47
 */
class AccelerometerTest : AppCompatActivity(), SensorEventListener {
    var textView: TextView? = null
    var stringBuilder = StringBuilder()

    //Landscape Screen
    var screenRotation = 0
    var ACCELEROMETER_AXIS_SWAP = arrayOf(
            intArrayOf(1, -1, 0, 1), //Rotation 0
            intArrayOf(-1, -1, 1, 0), //Rotation 90
            intArrayOf(-1, 1, 0, 1), //Rotation 180
            intArrayOf(1, 1, 1, 0 //Rotation 270
            ))

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        textView = TextView(this)
        setContentView(textView)

        var sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        if (sensorManager.getSensorList(Sensor.TYPE_ACCELEROMETER).size == 0) {
            textView?.text = "No accelerometer found"
        } else {
            var accelerometerSensor = sensorManager.getSensorList(Sensor.TYPE_ACCELEROMETER).get(0)
            if (!sensorManager.registerListener(this, accelerometerSensor, SensorManager.SENSOR_DELAY_GAME)) {
                textView?.text = "Could not register sensor listener"
            }
        }
    }

    override fun onResume() {
        super.onResume()
        var windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        screenRotation = windowManager.defaultDisplay.rotation
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {

    }

    override fun onSensorChanged(event: SensorEvent) {
        //Landscape
        var accsensor = ACCELEROMETER_AXIS_SWAP[screenRotation]
        var screenX = accsensor[0] * event.values[accsensor[2]]
        var screenY = accsensor[1] * event.values[accsensor[3]]
        var screenZ = event.values[2]
        stringBuilder.setLength(0)
        stringBuilder.append("x:").append(screenX)
        stringBuilder.append(", y:").append(screenY)
        stringBuilder.append(", z:").append(screenZ)

        //Potrait
//        stringBuilder.setLength(0)
//        stringBuilder.append("x:").append(event.values[0])
//        stringBuilder.append(", y:").append(event.values[1])
//        stringBuilder.append(", z:").append(event.values[2])
        textView?.text = stringBuilder.toString()
    }
}