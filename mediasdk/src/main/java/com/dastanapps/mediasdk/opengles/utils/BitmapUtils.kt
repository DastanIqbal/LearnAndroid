package com.dastanapps.mediasdk.opengles.utils

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Matrix

/**
 * Created by dastaniqbal on 13/12/2018.
 * 13/12/2018 11:39
 */
object BitmapUtils {
    private val TAG = this::class.java.simpleName

    fun overlay(bitmap1: Bitmap?, bitmap2: Bitmap?): Bitmap? {
        val bmOverlay = Bitmap.createBitmap(bitmap1?.width!!, bitmap1.height, bitmap1.config)
        val canvas = Canvas(bmOverlay)
        canvas.drawBitmap(bitmap1, Matrix(), null)

        val centreX = (canvas.width - bitmap2?.width!!) / 2f
        val centreY = (canvas.height - bitmap2.height) / 2f

        canvas.drawBitmap(bitmap2, centreX, centreY, null)
        return bmOverlay
    }

}