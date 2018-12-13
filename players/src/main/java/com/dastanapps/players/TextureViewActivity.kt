package com.dastanapps.players

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.SurfaceTexture
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v7.app.AppCompatActivity
import android.view.Surface
import android.view.TextureView
import com.dastanapps.dastanlib.log.Logger
import com.dastanapps.dastanlib.utils.VideoUtils
import com.google.android.exoplayer2.ExoPlaybackException
import kotlinx.android.synthetic.main.activity_text_view.*

class TextureViewActivity : AppCompatActivity(), TextureView.SurfaceTextureListener {
    companion object {
        const val PICK_IMAGE_REQUEST_CODE = 1000
        const val READ_EXTERNAL_STORAGE_REQUEST_CODE = 1001
    }

    override fun onSurfaceTextureSizeChanged(surface: SurfaceTexture?, width: Int, height: Int) {

    }

    override fun onSurfaceTextureUpdated(surface: SurfaceTexture?) {
    }

    override fun onSurfaceTextureDestroyed(surface: SurfaceTexture?): Boolean {
        return true
    }

    override fun onSurfaceTextureAvailable(surface: SurfaceTexture?, width: Int, height: Int) {
        VideoUtils.adjustAspectRatio(texture, width, height)
        val exoPlayer = ExoPlayerHelper(this)
        val texture = findViewById<TextureView>(R.id.texture)
        //exoPlayer.player?.setVideoTextureView(texture)
        exoPlayer?.player?.setVideoSurface(Surface(surface))
        exoPlayer.playLocal("/sdcard/KrusoTestVideo/big_buck_bunny_720p_stereo.mp4", object : ExoPlayerHelper.Listener() {
            override fun onPlayerError(error: ExoPlaybackException?) {
                super.onPlayerError(error)
                Logger.onlyDebug("Error: ${error?.message}")
            }

            override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
                super.onPlayerStateChanged(playWhenReady, playbackState)
                Logger.onlyDebug("PlayerStateChange: $playbackState")
            }
        })
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_text_view)
        texture.surfaceTextureListener = this
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                    READ_EXTERNAL_STORAGE_REQUEST_CODE
            )
        }
    }
}
