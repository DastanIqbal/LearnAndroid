package com.iaandroid.tutsopengles.fbo

import android.content.Context
import android.graphics.PointF

import java.util.ArrayList

import com.dastanapps.mediasdk.opengles.gpu.fbo.Component
import com.dastanapps.mediasdk.opengles.gpu.fbo.Utils

/**
 * GraffitiStickerRender
 *
 * @author like
 * @date 2018-05-25
 */
class GraffitiStickerRender(context: Context, private val mTimeController: IStickerTimeController) : StickerRender(context) {

    private var mStartTime: Float = 0.toFloat()
    private var mEndTime: Float = 0.toFloat()

    private var mIsPause = true

    interface IStickerTimeController {

        val currentTime: Float
    }

    init {

        var sticker1 = Sticker()
        sticker1.components = ArrayList()
        val component = Component()
        component.duration = 2000
        component.src = "lear"
        component.width = 245
        component.height = 245

        sticker1.components.add(component)
        Utils.convert(context, component)
        sticker = sticker1
    }


    fun start() {
        mIsPause = false
        mStartTime = mTimeController.currentTime
    }

    fun pause() {
        mIsPause = true
        mEndTime = mTimeController.currentTime
    }

    override fun onDraw() {
        super.onDraw()
    }
}
