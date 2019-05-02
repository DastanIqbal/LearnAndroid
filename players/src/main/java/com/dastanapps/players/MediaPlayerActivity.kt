package com.dastanapps.players

import android.graphics.Matrix
import android.graphics.SurfaceTexture
import android.media.AudioManager
import android.media.MediaPlayer
import android.os.Bundle
import android.util.Log
import android.view.Surface
import android.view.SurfaceHolder
import android.view.TextureView
import android.widget.SeekBar
import androidx.appcompat.app.AppCompatActivity


/**
 * Created by dastaniqbal on 10/01/2018.

 * 10/01/2018 6:21
 */
class MediaPlayerActivity : AppCompatActivity(), MediaPlayer.OnBufferingUpdateListener, MediaPlayer.OnCompletionListener, MediaPlayer.OnPreparedListener,
        MediaPlayer.OnVideoSizeChangedListener {

    private val TAG = "MediaPlayerDemo"
    private var mVideoWidth: Int = 0
    private var mVideoHeight: Int = 0
    private var mMediaPlayer: MediaPlayer? = null
    private var mPreview: TextureView? = null
    private var holder: SurfaceHolder? = null
    private var path: String? = null
    private var mIsVideoSizeKnown = false
    private var mIsVideoReadyToBePlayed = false
    private var seekBar: SeekBar? = null

    /**
     *
     * Called when the activity is first created.
     */
    public override fun onCreate(icicle: Bundle?) {
        super.onCreate(icicle)
        setContentView(R.layout.textureview)
        mPreview = findViewById<TextureView>(R.id.texture)
        mPreview?.surfaceTextureListener = object : TextureView.SurfaceTextureListener {
            override fun onSurfaceTextureUpdated(p0: SurfaceTexture?) {

            }

            override fun onSurfaceTextureSizeChanged(p0: SurfaceTexture?, p1: Int, p2: Int) {
                Log.d(TAG, "surfaceChanged called")
            }

            override fun onSurfaceTextureDestroyed(p0: SurfaceTexture?): Boolean {
                Log.d(TAG, "surfaceDestroyed called")
                return true
            }

            override fun onSurfaceTextureAvailable(p0: SurfaceTexture?, p1: Int, p2: Int) {
                Log.d(TAG, "surfaceCreated called")
                playVideo(Surface(p0))
            }
        }

        seekBar = findViewById(R.id.seekbar)
        seekBar!!.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, i: Int, b: Boolean) {
                if (mMediaPlayer != null)
                    mMediaPlayer!!.seekTo(i)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {

            }

            override fun onStopTrackingTouch(seekBar: SeekBar) {

            }
        })
    }

    private fun playVideo(surface: Surface) {
        doCleanUp()
        try {

            // Create a new media player and set the listeners
            val afd = assets.openFd("video.mp4")
            mMediaPlayer = MediaPlayer()
            mMediaPlayer!!.setDataSource(afd.fileDescriptor, afd.startOffset, afd.length)
            mMediaPlayer!!.setSurface(surface)
            mMediaPlayer!!.prepareAsync()
            mMediaPlayer!!.setOnBufferingUpdateListener(this)
            mMediaPlayer!!.setOnCompletionListener(this)
            mMediaPlayer!!.setOnPreparedListener(this)
            mMediaPlayer!!.setOnVideoSizeChangedListener(this)
            mMediaPlayer!!.setAudioStreamType(AudioManager.STREAM_MUSIC)
            mMediaPlayer!!.setOnPreparedListener {
                seekBar!!.max = mMediaPlayer!!.duration
                adjustAspectRatio(mMediaPlayer!!.videoWidth, mMediaPlayer!!.videoHeight)
                mMediaPlayer!!.start()
            }


        } catch (e: Exception) {
            Log.e(TAG, "error: " + e.message, e)
        }

    }

    override fun onBufferingUpdate(arg0: MediaPlayer, percent: Int) {
        Log.d(TAG, "onBufferingUpdate percent:" + percent)

    }

    override fun onCompletion(arg0: MediaPlayer) {
        Log.d(TAG, "onCompletion called")
    }

    override fun onVideoSizeChanged(mp: MediaPlayer, width: Int, height: Int) {
        Log.v(TAG, "onVideoSizeChanged called")
        if (width == 0 || height == 0) {
            Log.e(TAG, ("invalid video width(" + width + ") or height(" + height
                    + ")"))
            return
        }
        mIsVideoSizeKnown = true
        mVideoWidth = width
        mVideoHeight = height
        if (mIsVideoReadyToBePlayed && mIsVideoSizeKnown) {
            startVideoPlayback()
        }
    }

    override fun onPrepared(mediaplayer: MediaPlayer) {
        Log.d(TAG, "onPrepared called")
        mIsVideoReadyToBePlayed = true
        if (mIsVideoReadyToBePlayed && mIsVideoSizeKnown) {
            startVideoPlayback()
        }
    }

    override fun onPause() {
        super.onPause()
        releaseMediaPlayer()
        doCleanUp()
    }

    override fun onDestroy() {
        super.onDestroy()
        releaseMediaPlayer()
        doCleanUp()
    }

    private fun releaseMediaPlayer() {
        if (mMediaPlayer != null) {
            mMediaPlayer!!.release()
            mMediaPlayer = null
        }
    }

    private fun doCleanUp() {
        mVideoWidth = 0
        mVideoHeight = 0
        mIsVideoReadyToBePlayed = false
        mIsVideoSizeKnown = false
    }

    private fun startVideoPlayback() {
        Log.v(TAG, "startVideoPlayback")
        holder!!.setFixedSize(mVideoWidth, mVideoHeight)
        mMediaPlayer!!.start()
    }

    private fun adjustAspectRatio(videoWidth: Int, videoHeight: Int) {
        val viewWidth = mPreview!!.width
        val viewHeight = mPreview!!.height
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
        val xoff = (viewWidth - newWidth).toFloat() / 2f
        val yoff = (viewHeight - newHeight).toFloat() / 2f
        Log.v(TAG, "video=" + videoWidth + "x" + videoHeight +
                " view=" + viewWidth + "x" + viewHeight +
                " newView=" + newWidth + "x" + newHeight +
                " off=" + xoff + "," + yoff)

        val txform = Matrix()
        mPreview?.getTransform(txform)
        txform.setScale(newWidth.toFloat() / viewWidth, newHeight.toFloat() / viewHeight)
        //txform.postRotate(10);          // just for fun
        txform.postTranslate(xoff, yoff)
        mPreview?.setTransform(txform)
    }
}