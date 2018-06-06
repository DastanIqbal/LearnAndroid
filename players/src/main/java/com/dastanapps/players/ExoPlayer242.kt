package com.dastanapps.players

import android.annotation.SuppressLint
import android.net.Uri
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.widget.SeekBar
import com.google.android.exoplayer2.*
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory
import com.google.android.exoplayer2.source.ExtractorMediaSource
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.source.TrackGroupArray
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.trackselection.TrackSelectionArray
import com.google.android.exoplayer2.ui.SimpleExoPlayerView
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.upstream.DefaultHttpDataSource
import com.google.android.exoplayer2.upstream.DefaultHttpDataSourceFactory
import com.google.android.exoplayer2.util.Util


/**
 * Created by dastaniqbal on 10/01/2018.
 * dastanIqbal@marvelmedia.com
 * 10/01/2018 6:11
 */
class ExoPlayer242 : AppCompatActivity() {
    // bandwidth meter to measure and estimate bandwidth
    private val BANDWIDTH_METER = DefaultBandwidthMeter()
    private val TAG = "PlayerActivity"

    private var player: SimpleExoPlayer? = null
    private var playerView: SimpleExoPlayerView? = null
    private var seekBar: SeekBar? = null

    private var playbackPosition: Long = 0
    private var currentWindow: Int = 0
    private var playWhenReady = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_player)

        playerView = findViewById(R.id.video_view)
        playerView?.setControllerVisibilityListener {};
        playerView?.requestFocus();
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
        seekBar?.visibility = View.GONE
    }

    public override fun onStart() {
        super.onStart()
        if (Util.SDK_INT > 23) {
            initializePlayer()
        }
    }

    public override fun onResume() {
        super.onResume()
      //  hideSystemUi()
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
            val adaptiveTrackSelectionFactory = AdaptiveTrackSelection.Factory(BANDWIDTH_METER)
            val eventLogger = EventLogger(DefaultTrackSelector(adaptiveTrackSelectionFactory))
            player = ExoPlayerFactory.newSimpleInstance(DefaultRenderersFactory(this),
                    DefaultTrackSelector(adaptiveTrackSelectionFactory), DefaultLoadControl())
            player?.addListener(object : ExoPlayer.EventListener {
                override fun onPlaybackParametersChanged(playbackParameters: PlaybackParameters?) {
                    Log.d("DEBUG", playbackParameters.toString())
                }

                override fun onTracksChanged(trackGroups: TrackGroupArray?, trackSelections: TrackSelectionArray?) {
                    Log.d("DEBUG", trackGroups.toString())
                }

                override fun onPlayerError(error: ExoPlaybackException?) {
                    Log.d("DEBUG", error?.let { error.message } ?: error?.localizedMessage)
                }

                override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
                    Log.d("DEBUG", "onPlayerStateChanged$playWhenReady $playbackState")
                    when (playbackState) {
                        ExoPlayer.STATE_IDLE -> Log.d("DEBUG", "Idle")
                        ExoPlayer.STATE_BUFFERING -> Log.d("DEBUG", "Buffering")
                        ExoPlayer.STATE_READY -> Log.d("DEBUG", "Ready")
                        ExoPlayer.STATE_ENDED -> Log.d("DEBUG", "Ended")
                    }

                }

                override fun onLoadingChanged(isLoading: Boolean) {
                    Log.d("DEBUG", "onLoadingChanged:$isLoading")
                }

                override fun onPositionDiscontinuity() {
                    Log.d("DEBUG", "onPositionDiscontinuity")
                }

                override fun onTimelineChanged(timeline: Timeline?, manifest: Any?) {
                    Log.d("DEBUG", manifest.toString())
                }

            });
            player?.addListener(eventLogger);
            player?.setAudioDebugListener(eventLogger);
            player?.setVideoDebugListener(eventLogger);
            player?.setMetadataOutput(eventLogger);

            playerView!!.player = player
            player!!.playWhenReady = playWhenReady
            player!!.seekTo(currentWindow, playbackPosition)
        }
        val mediaSource = buildMediaSource(Uri.parse(getString(R.string.media_url_stream)))
        player!!.prepare(mediaSource, true, false)
    }

    private fun releasePlayer() {
        if (player != null) {
            playbackPosition = player!!.currentPosition
            currentWindow = player!!.currentWindowIndex
            playWhenReady = player!!.playWhenReady
            player!!.release()
            player = null
        }
    }

    override fun dispatchKeyEvent(event: KeyEvent): Boolean {
        // Show the controls on any key event.
        playerView!!.showController()
        // If the event was not handled then see if the player view can handle it as a media key event.
        return super.dispatchKeyEvent(event) || playerView!!.dispatchMediaKeyEvent(event)
    }

    private fun buildMediaSource(uri: Uri): MediaSource {
        val userAgent = Util.getUserAgent(this, "exoplayer2example")
        // Default parameters, except allowCrossProtocolRedirects is true
        val httpDataSourceFactory = DefaultHttpDataSourceFactory(
                userAgent,
                BANDWIDTH_METER /* listener */,
                DefaultHttpDataSource.DEFAULT_CONNECT_TIMEOUT_MILLIS,
                DefaultHttpDataSource.DEFAULT_READ_TIMEOUT_MILLIS,
                true /* allowCrossProtocolRedirects */
        )

        val dataSourceFactory = DefaultDataSourceFactory(
                this, null,
                httpDataSourceFactory
        )
        return ExtractorMediaSource(uri, dataSourceFactory, DefaultExtractorsFactory(), null, null)
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
}