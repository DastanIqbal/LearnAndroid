package com.dastanapps.camera2.Preview.CameraSurface

import com.dastanapps.mediasdk.opengles.CameraSurfaceRenderer
import com.dastanapps.mediasdk.opengles.encoder.MediaVideoEncoder

/**
 * Created by dastaniqbal on 04/09/2018.
 * dastanIqbal@marvelmedia.com
 * 04/09/2018 8:35
 */
class MyCameraSurfaceRenderer: CameraSurfaceRenderer() {
    fun setVideoEnocder(mTextureView: MySurfaceView, videoEnocder: MediaVideoEncoder?) {
        mTextureView.queueEvent {
            if (videoEnocder != null) {
                videoEnocder.setEglContext(mCameraSurfaceGlTexture)
                mVideoEncoder = videoEnocder
            }
        }
    }

    fun setVideoEnocder(mTextureView: MyTextureView, videoEnocder: MediaVideoEncoder?) {
        mTextureView.queueEvent {
            if (videoEnocder != null) {
                videoEnocder.setEglContext(mCameraSurfaceGlTexture)
                mVideoEncoder = videoEnocder
            }
        }
    }
}