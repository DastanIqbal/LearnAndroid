/*
 * Copyright 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.dastanapps.camera.view

import android.content.Context
import android.graphics.Rect
import android.hardware.Camera
import android.os.Handler
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.TextureView
import android.view.View
import com.dastanapps.camera.Camera1
import com.dastanapps.view.AutoFitTextureView
import java.util.*

/**
 * A [TextureView] that can be adjusted to a specified aspect ratio.
 */
class Cam1AutoFitTextureView : AutoFitTextureView {
    private var mCamera: Camera? = null
    internal var mDist = 0f
    private var mainHandler: Handler? = null

    constructor(context: Context) : super(context) {}

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {}

    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(context, attrs, defStyle) {}

    fun setCameraSettings(camera: Camera) {
        this.mCamera = camera
    }

    override fun onTouch(v: View, event: MotionEvent): Boolean {
        // Get the pointer ID
        val params = mCamera!!.parameters
        val action = event.action

        if (event.pointerCount > 1) {
            // handle multi-touch events
            if (action == MotionEvent.ACTION_POINTER_DOWN) {
                mDist = getFingerSpacing(event)
            } else if (action == MotionEvent.ACTION_MOVE && params.isZoomSupported) {
                mCamera!!.cancelAutoFocus()
                pinchToZoom(event)
            }
        } else {
            // handle single touch events
            if (action == MotionEvent.ACTION_DOWN) {
                try {
                    touchToFocus(event)
                } catch (e: Exception) {
                    e.printStackTrace()
                }

            }
        }
        return true
    }

    override fun pinchToZoom(event: MotionEvent) {
        val params = mCamera!!.parameters
        val maxZoom = params.maxZoom
        var zoom = params.zoom
        val newDist = getFingerSpacing(event)
        if (newDist > mDist) {
            // zoom in
            if (zoom < maxZoom)
                zoom++
        } else if (newDist < mDist) {
            // zoom out
            if (zoom > 0)
                zoom--
        }
        mDist = newDist
        params.zoom = zoom
        mCamera!!.parameters = params
    }

    override fun touchToFocus(event: MotionEvent) {
        val params = mCamera!!.parameters
        val supportedFocusModes = params.supportedFocusModes
        if (supportedFocusModes != null && supportedFocusModes.contains(Camera.Parameters.FOCUS_MODE_AUTO)) {
            val x = event.x
            val y = event.y

            val touchRect = Rect((x - 100).toInt(), (y - 100).toInt(), (x + 100).toInt(), (y + 100).toInt())
            val targetFocusRect = Rect(
                    touchRect.left * 2000 / this.width - 1000,
                    touchRect.top * 2000 / this.height - 1000,
                    touchRect.right * 2000 / this.width - 1000,
                    touchRect.bottom * 2000 / this.height - 1000)

            if (params.maxNumFocusAreas > 0) {
                val focusList = ArrayList<Camera.Area>()
                val focusArea = Camera.Area(targetFocusRect, 1000)
                focusList.add(focusArea)

                params.focusAreas = focusList
                params.meteringAreas = focusList
                mCamera!!.parameters = params
            }

//Focusing
            val message = mainHandler!!.obtainMessage()
            message.what = Camera1.FOCUSING_FOCUS_VIEW
            message.obj = event
            mainHandler!!.sendMessage(message)
            mCamera!!.cancelAutoFocus()

            mCamera!!.autoFocus { success, camera ->
                if (success) {
                    val message = mainHandler!!.obtainMessage()
                    message.what = Camera1.SUCCESS_FOCUS_VIEW
                    message.obj = event
                    mainHandler!!.sendMessage(message)
                    mCamera!!.cancelAutoFocus()
                } else {
                    val message = mainHandler!!.obtainMessage()
                    message.what = Camera1.FAILED_FOCUS_VIEW
                    message.obj = event
                    mainHandler!!.sendMessage(message)
                }
            }
        }
    }

    fun setMainHandler(mainHandler: Handler) {
        this.mainHandler = mainHandler
    }
}
