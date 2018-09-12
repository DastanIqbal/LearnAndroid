package com.iaandroid.tutsopengles.fbo.sticker

import android.content.Context
import android.graphics.PointF
import com.dastanapps.mediasdk.opengles.gpu.fbo.*
import java.util.*

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
    private val mPositionHistories = ArrayList<PointF>() // 贴纸位置历史
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

        val textureAnchor = TextureAnchor()
        textureAnchor.leftAnchor = AnchorPoint(AnchorPoint.LEFT_BOTTOM, 0, 0)
        textureAnchor.rightAnchor = AnchorPoint(AnchorPoint.RIGHT_BOTTOM, 0, 0)
        textureAnchor.width = component.width
        textureAnchor.height = component.height
        component.textureAnchor = textureAnchor

        sticker1.components.add(component)
        Utils.convert(context, component)
        sticker = sticker1

        val screenAnchor = ScreenAnchor()
        screenAnchor.leftAnchor = AnchorPoint(AnchorPoint.LEFT_TOP, 0, 0)
        screenAnchor.rightAnchor = AnchorPoint(AnchorPoint.LEFT_TOP, 0, 0)
        setScreenAnchor(screenAnchor)
    }

    fun setPosition(x: Int, y: Int) {
        if (!mSticker.components.isEmpty()) {
            mScreenAnchor?.leftAnchor?.x = x - sticker.components[0].width.toFloat() / 2
            mScreenAnchor?.leftAnchor?.y = y.toFloat()

            mScreenAnchor?.rightAnchor?.x = x + sticker.components[0].width.toFloat() / 2 // 涂鸦贴纸只有一个元素
            mScreenAnchor?.rightAnchor?.y = y.toFloat()
        }
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

        if (!mIsPause) {
            val x = mScreenAnchor?.leftAnchor?.x!! + mSticker.components[0].width / 2
            val y = mScreenAnchor?.leftAnchor?.y!!
            mPositionHistories.add(PointF(x, y))
        } else {
            val currentTime = mTimeController.currentTime
            if (currentTime >= mStartTime && mEndTime > mStartTime && !mPositionHistories.isEmpty()) {
                val index = Math.round((mPositionHistories.size - 1) * (currentTime - mStartTime) / (mEndTime - mStartTime))
                val pointF = mPositionHistories.get(if (index < mPositionHistories.size) index else mPositionHistories.size - 1)
                setPosition(Math.round(pointF.x), Math.round(pointF.y))
            } else {
                setPosition(0, 0)
            }
        }
    }
}
