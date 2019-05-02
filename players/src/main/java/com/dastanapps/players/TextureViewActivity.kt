package com.dastanapps.players

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Matrix
import android.graphics.SurfaceTexture
import android.os.Bundle
import android.util.Log
import android.view.TextureView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.dastanapps.ExoPlayerHelper2
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
        exoPlayer.player?.setVideoTextureView(texture)
        exoPlayer.playLocal("/storage/emulated/0/Kruso/Video_kruso_20190415173019271.mp4", listener = object : ExoPlayerHelper2.Listener() {
            override fun onPlayerError(error: ExoPlaybackException?) {
                super.onPlayerError(error)
                Log.d("TextureViewActivity", "Error: ${error?.message}")
            }

            override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
                super.onPlayerStateChanged(playWhenReady, playbackState)
                Log.d("TextureViewActivity", "PlayerStateChange: $playbackState")
            }

            override fun onVideoSizeChanged(width: Int, height: Int, unappliedRotationDegrees: Int, pixelWidthHeightRatio: Float) {
                super.onVideoSizeChanged(width, height, unappliedRotationDegrees, pixelWidthHeightRatio)
                adjustAspectRatio(texture, width, height)
            }
        })
    }

    val exoPlayer = ExoPlayerHelper2(this)

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

    override fun onStart() {
        super.onStart()
        exoPlayer.onStart()
    }

    override fun onResume() {
        super.onResume()
        exoPlayer.onStart()
    }

    override fun onPause() {
        super.onPause()
        exoPlayer.releasePlayer()
    }

    override fun onStop() {
        super.onStop()
        exoPlayer.releasePlayer()
    }


    fun adjustAspectRatio(vidTxv: TextureView, videoWidth: Int, videoHeight: Int) {
        val viewWidth = vidTxv.width
        val viewHeight = vidTxv.height
        val aspectRatio = videoHeight.toDouble() / videoWidth

        val newWidth: Int
        val newHeight: Int
        if (viewHeight > (viewWidth * aspectRatio).toInt()) {
            // limited by narrow width; restrict height
            newWidth = viewWidth
            newHeight = (viewWidth * aspectRatio).toInt()
        } else {
            // limited by short height; restrict width
            newWidth = (viewHeight / aspectRatio).toInt()
            newHeight = viewHeight
        }
        val xoff = (viewWidth - newWidth) / 2
        val yoff = (viewHeight - newHeight) / 2
//        Logger.onlyDebug("video=" + videoWidth + "x" + videoHeight +
//                " view=" + viewWidth + "x" + viewHeight +
//                " newView=" + newWidth + "x" + newHeight +
//                " off=" + xoff + "," + yoff)

        val txform = Matrix()
        vidTxv.getTransform(txform)
        txform.setScale(newWidth.toFloat() / viewWidth, newHeight.toFloat() / viewHeight)
        txform.postTranslate(xoff.toFloat(), yoff.toFloat())
        vidTxv.setTransform(txform)
    }
}
