package com.iaandroid.tutsopengles.fbo

import android.opengl.GLES20

import java.util.ArrayList


/**
 * 支持帧缓冲的渲染器
 *
 *
 * 会将图像渲染到一个输出纹理中。
 * FBORender需要与EndPointRender配合使用才能将帧缓冲中的内容显示出来。
 *
 * @author like
 * @date 2017-09-15
 */
open class FBORender : GLRender() {

    protected var mFrameBuffer: IntArray? = null
    protected var mTextureOut: IntArray? = null
    protected var mDepthRenderBuffer: IntArray? = null

    override open fun onRenderSizeChanged() {
        initFBO()
    }

    /**
     * 为了方便子类覆盖，这里独立为一个方法
     */
    open fun onDraw() {
        super.drawFrame()
    }

    private fun initFBO() {
        // Initialize the output texture
        if (mTextureOut != null) {
            GLES20.glDeleteTextures(1, mTextureOut, 0)
            mTextureOut = null
        }
        mTextureOut = IntArray(1)
        GLES20.glGenTextures(1, mTextureOut, 0)
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0)
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mTextureOut!![0])

        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D,
                GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR)
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D,
                GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR)
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S,
                GLES20.GL_CLAMP_TO_EDGE)
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T,
                GLES20.GL_CLAMP_TO_EDGE)

        GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_RGBA,
                mWidth, mHeight, 0, GLES20.GL_RGBA,
                GLES20.GL_UNSIGNED_BYTE, null)

        // Initialize framebuffer and depth buffer
        if (mFrameBuffer != null) {
            GLES20.glDeleteFramebuffers(1, mFrameBuffer, 0)
            mFrameBuffer = null
        }
        if (mDepthRenderBuffer != null) {
            GLES20.glDeleteRenderbuffers(1, mDepthRenderBuffer, 0)
            mDepthRenderBuffer = null
        }
        mFrameBuffer = IntArray(1)
        mDepthRenderBuffer = IntArray(1)
        GLES20.glGenFramebuffers(1, mFrameBuffer, 0)
        GLES20.glGenRenderbuffers(1, mDepthRenderBuffer, 0)
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, mFrameBuffer!![0])
        GLES20.glFramebufferTexture2D(GLES20.GL_FRAMEBUFFER,
                GLES20.GL_COLOR_ATTACHMENT0, GLES20.GL_TEXTURE_2D,
                mTextureOut!![0], 0)
        GLES20.glBindRenderbuffer(GLES20.GL_RENDERBUFFER, mDepthRenderBuffer!![0])
        GLES20.glRenderbufferStorage(GLES20.GL_RENDERBUFFER,
                GLES20.GL_DEPTH_COMPONENT16, mWidth, mHeight)
        GLES20.glFramebufferRenderbuffer(GLES20.GL_FRAMEBUFFER,
                GLES20.GL_DEPTH_ATTACHMENT, GLES20.GL_RENDERBUFFER,
                mDepthRenderBuffer!![0])
    }


    override fun drawFrame() {
        if (mTextureOut == null) {
            if (mWidth != 0 && mHeight != 0) {
                initFBO()
            } else {
                return
            }
        }

        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, mFrameBuffer?.get(0)!!)

        onDraw()

        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0)
    }

    override fun destroy() {
        super.destroy();
        if (mFrameBuffer != null) {
            GLES20.glDeleteFramebuffers(1, mFrameBuffer, 0);
            mFrameBuffer = null;
        }
        if (mDepthRenderBuffer != null) {
            GLES20.glDeleteRenderbuffers(1, mDepthRenderBuffer, 0);
            mDepthRenderBuffer = null;
        }
        if (mTextureOut != null) {
            GLES20.glDeleteTextures(1, mTextureOut, 0);
            mTextureOut = null;
        }
    }
}
