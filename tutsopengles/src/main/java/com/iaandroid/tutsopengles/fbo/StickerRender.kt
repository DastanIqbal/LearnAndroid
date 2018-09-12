package com.iaandroid.tutsopengles.fbo

import android.content.Context
import android.opengl.GLES20
import com.dastanapps.mediasdk.opengles.gpu.fbo.IBitmapCache
import com.dastanapps.mediasdk.opengles.gpu.fbo.LruBitmapCache
import com.dastanapps.mediasdk.opengles.gpu.fbo.ScreenAnchor
import java.util.*

/**
 * 贴纸渲染器
 *
 * @author like
 * @date 2018-01-05
 */
open class StickerRender(protected var mContext: Context) : FilterRender() {

    // Sticker model
    protected lateinit var mSticker: Sticker

    // Component renderer list
    protected var mComponentRenders: MutableList<ComponentRender> = ArrayList()

    // Anchor group
    protected var mScreenAnchor: ScreenAnchor? = null

    /**
     * 设置贴纸模型
     *
     * @param sticker
     */
    var sticker: Sticker
        get() = mSticker
        set(sticker) {
            mSticker = sticker

            mComponentRenders.clear()
            for (component in mSticker.components) {
                val componentRender = ComponentRender(mContext, component)
                componentRender.setBitmapCache(bitmapCache)
                mComponentRenders.add(componentRender)
            }
        }

    /**
     * 设置显示锚点
     *
     * @param screenAnchor
     */
    fun setScreenAnchor(screenAnchor: ScreenAnchor) {
        mScreenAnchor = screenAnchor
    }

    override fun destroy() {
        super.destroy()

        for (componentRender in mComponentRenders) {
            componentRender.destroy()
        }
    }

    fun setBitmapCache(bitmapCache: IBitmapCache) {
        super.bitmapCache = bitmapCache as LruBitmapCache?

        for (componentRender in mComponentRenders) {
            componentRender.setBitmapCache(bitmapCache)
        }
    }

    override fun onRenderSizeChanged() {
        super.onRenderSizeChanged()
        if (mScreenAnchor != null) {
            mScreenAnchor?.width = mWidth
            mScreenAnchor?.height = mHeight
        }
    }

    override fun onDraw() {
        super.onDraw()

        // 开启混合
        GLES20.glEnable(GLES20.GL_BLEND)
        GLES20.glBlendFunc(GLES20.GL_ONE, GLES20.GL_ONE_MINUS_SRC_ALPHA)

        for (componentRender in mComponentRenders) {
            componentRender.onDraw(mTextureHandle, mPositionHandle, mTextureCoordHandle, mTextureVertices[2])
        }

        GLES20.glDisable(GLES20.GL_BLEND)
    }
}
