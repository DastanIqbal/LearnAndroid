package com.dastanapps.players

import android.annotation.SuppressLint
import android.net.Uri
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.Surface
import android.view.View
import android.widget.SeekBar
import com.google.android.exoplayer2.*
import com.google.android.exoplayer2.audio.AudioRendererEventListener
import com.google.android.exoplayer2.decoder.DecoderCounters
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory
import com.google.android.exoplayer2.source.ExtractorMediaSource
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.ui.SimpleExoPlayerView
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.util.Util
import com.google.android.exoplayer2.video.VideoRendererEventListener

/**
 * Created by dastaniqbal on 10/01/2018.
 * dastanIqbal@marvelmedia.com
 * 10/01/2018 6:11
 */
class ExoPlayer: AppCompatActivity() {
    // bandwidth meter to measure and estimate bandwidth
    private val BANDWIDTH_METER = DefaultBandwidthMeter()
    private val TAG = "PlayerActivity"

    private var player: SimpleExoPlayer? = null
    private var playerView: SimpleExoPlayerView? = null
    private var componentListener: ComponentListener? = null
    private var seekBar: SeekBar? = null

    private var playbackPosition: Long = 0
    private var currentWindow: Int = 0
    private var playWhenReady = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_player)

        componentListener = ComponentListener()
        playerView = findViewById(R.id.video_view)
        seekBar = findViewById(R.id.seekbar)
        seekBar!!.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, i: Int, b: Boolean) {
                player!!.seekTo(i.toLong())
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {

            }

            override fun onStopTrackingTouch(seekBar: SeekBar) {

            }
        })
    }

    public override fun onStart() {
        super.onStart()
        if (Util.SDK_INT > 23) {
            initializePlayer()
        }
    }

    public override fun onResume() {
        super.onResume()
        hideSystemUi()
        if (Util.SDK_INT <= 23 || player == null) {
            initializePlayer()
        }
    }

    public override fun onPause() {
        super.onPause()
        if (Util.SDK_INT <= 23) {
            releasePlayer()
        }
    }

    public override fun onStop() {
        super.onStop()
        if (Util.SDK_INT > 23) {
            releasePlayer()
        }
    }

    private fun initializePlayer() {
        if (player == null) {
            // a factory to create an AdaptiveVideoTrackSelection
            val adaptiveTrackSelectionFactory = AdaptiveTrackSelection.Factory(BANDWIDTH_METER)
            // using a DefaultTrackSelector with an adaptive video selection factory
            player = ExoPlayerFactory.newSimpleInstance(DefaultRenderersFactory(this),
                    DefaultTrackSelector(adaptiveTrackSelectionFactory), DefaultLoadControl())
            player!!.addListener(componentListener)
            player!!.addVideoDebugListener(componentListener)
            player!!.addAudioDebugListener(componentListener)
            playerView!!.player = player
            player!!.playWhenReady = playWhenReady
            player!!.seekTo(currentWindow, playbackPosition)
        }
        val mediaSource = buildMediaSource(Uri.parse(getString(R.string.media_url_dash)))
        player!!.prepare(mediaSource, true, false)
    }

    private fun releasePlayer() {
        if (player != null) {
            playbackPosition = player!!.currentPosition
            currentWindow = player!!.currentWindowIndex
            playWhenReady = player!!.playWhenReady
            player!!.removeListener(componentListener)
            player!!.removeVideoDebugListener(componentListener)
            player!!.removeAudioDebugListener(componentListener)
            player!!.release()
            player = null
        }
    }

    private fun buildMediaSource(uri: Uri): MediaSource {
        val dataSourceFactory = DefaultDataSourceFactory(this, Util.getUserAgent(this, "exoplayer2example"), BANDWIDTH_METER)
        return ExtractorMediaSource(uri, dataSourceFactory, DefaultExtractorsFactory(), null, null)
        //DataSource.Factory manifestDataSourceFactory = new DefaultHttpDataSourceFactory("ua");
        //        DashChunkSource.Factory dashChunkSourceFactory = new DefaultDashChunkSource.Factory(
        //                new DefaultHttpDataSourceFactory("ua", BANDWIDTH_METER));
        //        return new DashMediaSource.Factory(dashChunkSourceFactory, manifestDataSourceFactory)
        //                .createMediaSource(uri);
    }

    @SuppressLint("InlinedApi")
    private fun hideSystemUi() {
        playerView!!.systemUiVisibility = (View.SYSTEM_UI_FLAG_LOW_PROFILE
                or View.SYSTEM_UI_FLAG_FULLSCREEN
                or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION)
    }

    private inner class ComponentListener : Player.DefaultEventListener(), VideoRendererEventListener, AudioRendererEventListener {

        override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
            val stateString: String
            when (playbackState) {
                Player.STATE_IDLE -> stateString = "ExoPlayer.STATE_IDLE      -"
                Player.STATE_BUFFERING -> stateString = "ExoPlayer.STATE_BUFFERING -"
                Player.STATE_READY -> {
                    seekBar!!.max = player!!.duration.toInt()
                    stateString = "ExoPlayer.STATE_READY     -"
                }
                Player.STATE_ENDED -> stateString = "ExoPlayer.STATE_ENDED     -"
                else -> stateString = "UNKNOWN_STATE             -"
            }
            Log.d(TAG, "changed state to $stateString playWhenReady: $playWhenReady")
        }

        // Implementing VideoRendererEventListener.

        override fun onVideoEnabled(counters: DecoderCounters) {
            // Do nothing.
        }

        override fun onVideoDecoderInitialized(decoderName: String, initializedTimestampMs: Long, initializationDurationMs: Long) {
            // Do nothing.
        }

        override fun onVideoInputFormatChanged(format: Format) {
            // Do nothing.
        }

        override fun onDroppedFrames(count: Int, elapsedMs: Long) {
            // Do nothing.
        }

        override fun onVideoSizeChanged(width: Int, height: Int, unappliedRotationDegrees: Int, pixelWidthHeightRatio: Float) {
            // Do nothing.
        }

        override fun onRenderedFirstFrame(surface: Surface) {
            // Do nothing.
        }

        override fun onVideoDisabled(counters: DecoderCounters) {
            // Do nothing.
        }

        // Implementing AudioRendererEventListener.

        override fun onAudioEnabled(counters: DecoderCounters) {
            // Do nothing.
        }

        override fun onAudioSessionId(audioSessionId: Int) {
            // Do nothing.
        }

        override fun onAudioDecoderInitialized(decoderName: String, initializedTimestampMs: Long, initializationDurationMs: Long) {
            // Do nothing.
        }

        override fun onAudioInputFormatChanged(format: Format) {
            // Do nothing.
        }

        override fun onAudioSinkUnderrun(bufferSize: Int, bufferSizeMs: Long, elapsedSinceLastFeedMs: Long) {
            // Do nothing.
        }

        override fun onAudioDisabled(counters: DecoderCounters) {
            // Do nothing.
        }

    }
}