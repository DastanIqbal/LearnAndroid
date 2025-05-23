package com.dastanapps.players

import android.content.Context
import android.net.Uri
import android.util.Log
import com.google.android.exoplayer2.*
import com.google.android.exoplayer2.source.ExtractorMediaSource
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.source.TrackGroupArray
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.trackselection.TrackSelectionArray
import com.google.android.exoplayer2.upstream.DataSource
import com.google.android.exoplayer2.upstream.DataSpec
import com.google.android.exoplayer2.upstream.DefaultHttpDataSourceFactory
import com.google.android.exoplayer2.upstream.FileDataSource
import com.google.android.exoplayer2.util.Util
import com.google.android.exoplayer2.video.VideoListener
import java.io.File


/**
 * Created by dastaniqbal on 18/05/2018.
 * 18/05/2018 5:55
 */
class ExoPlayerHelper(val context: Context) {
    private val TAG = this::class.java.simpleName
    var player: SimpleExoPlayer? = null
    var playbackPosition = 0L
    var currentWindow = 0
    var playWhenReady = false
    private var currentPosition = 0L
    private var currentUrl: String? = null

    fun onStart() {
        if (Util.SDK_INT > 23 && player == null) {
            initializePlayer()
        }
    }

    fun onStop() {
        if (Util.SDK_INT > 23) {
            releasePlayer()
        }
    }

    fun initializePlayer() {
        player = ExoPlayerFactory.newSimpleInstance(context,
                DefaultRenderersFactory(context),
                DefaultTrackSelector(), DefaultLoadControl())
        player?.seekTo(currentWindow, playbackPosition)
    }

    fun playUrl(url: String, listener: Listener? = null) {
        currentUrl = url
//        releasePlayer()
//        initializePlayer()
        player?.let {
            val uri = Uri.parse(url)
            val mediaSource = buildMediaSource(uri)
            playWhenReady(true)

            it.seekTo(currentPosition)
            it.addListener(listener)
            it.addVideoListener(listener)
//            it.addVideoDebugListener(listener
//            it.addAudioDebugListener(listener)
            it.prepare(mediaSource, false, false)
        }
    }

    fun playLocal(filepath: String, listener: Listener? = null) {
        currentUrl = filepath
        if (player == null) {
            releasePlayer()
            initializePlayer()
        }
        player?.let {
            val uri = Uri.fromFile(File(filepath))
            val mediaSource = buildLocalMediaSource(uri)

            it.seekTo(currentPosition)
            it.addListener(listener)
            it.addVideoListener(listener)


            playWhenReady(true)
            it.prepare(mediaSource, false, false)
        }
    }

    fun pause() {
        player?.let {
            playWhenReady(false)
            currentPosition = it.currentPosition
        }
    }

    fun isPlaying(url: String): Boolean {
        return if (currentUrl == url) {
            playWhenReady
        } else {
            false
        }
    }


    fun playWhenReady(playWhenReady: Boolean) {
        this.playWhenReady = playWhenReady
        player?.playWhenReady = playWhenReady
    }

    private fun buildMediaSource(uri: Uri): MediaSource {
        val defaultHttpDataSourceFactory = DefaultHttpDataSourceFactory("exoplayer-")
        defaultHttpDataSourceFactory.setDefaultRequestProperty("Image", "getme")
        return ExtractorMediaSource.Factory(defaultHttpDataSourceFactory).createMediaSource(uri)
    }

    private fun buildLocalMediaSource(uri: Uri): MediaSource {
        val dataSpec = DataSpec(uri)
        val fileDataSource = FileDataSource()
        try {
            fileDataSource.open(dataSpec)
        } catch (e: FileDataSource.FileDataSourceException) {
            e.printStackTrace()
        }

        val factory = DataSource.Factory { fileDataSource }
        return ExtractorMediaSource.Factory(factory).createMediaSource(fileDataSource.uri)
    }


    fun releasePlayer() {
        player?.run {
            playWhenReady(false)
            playbackPosition = player?.run {
                currentPosition
            } ?: playbackPosition
            currentWindow = player?.run {
                currentWindowIndex
            } ?: currentWindow
            player?.stop()
            player?.release()
            player = null
        }
    }

    fun seekTo(position: Long) {
        player?.run {
            seekTo(position)
        }
    }

    open class Listener : Player.EventListener, VideoListener/*,VideoRendererEventListener, AudioRendererEventListener*/ {
        private val TAG = this::class.java.simpleName
       /* override fun onAudioSinkUnderrun(bufferSize: Int, bufferSizeMs: Long, elapsedSinceLastFeedMs: Long) {
            Log.d(TAG,"onAudioSinkUnderrun $bufferSize $bufferSizeMs $elapsedSinceLastFeedMs")
        }

        override fun onAudioEnabled(counters: DecoderCounters?) {
            Log.d(TAG,"onAudioEnabled")
        }

        override fun onAudioInputFormatChanged(format: Format?) {
            Log.d(TAG,"onAudioInputFormatChanged")
        }

        override fun onAudioSessionId(audioSessionId: Int) {
            Log.d(TAG,"onAudioSessionId")
        }

        override fun onAudioDecoderInitialized(decoderName: String?, initializedTimestampMs: Long, initializationDurationMs: Long) {
            Log.d(TAG,"onAudioDecoderInitialized")
        }

        override fun onAudioDisabled(counters: DecoderCounters?) {
            Log.d(TAG,"onAudioDisabled")
        }

        override fun onDroppedFrames(count: Int, elapsedMs: Long) {
            Log.d(TAG,"onDroppedFrames")
        }

        override fun onVideoEnabled(counters: DecoderCounters?) {
            Log.d(TAG,"onVideoEnabled")
        }

        override fun onVideoSizeChanged(width: Int, height: Int, unappliedRotationDegrees: Int, pixelWidthHeightRatio: Float) {
            Log.d(TAG,"onVideoSizeChanged")
        }

        override fun onVideoDisabled(counters: DecoderCounters?) {
            Log.d(TAG,"onVideoDisabled")
        }

        override fun onVideoDecoderInitialized(decoderName: String?, initializedTimestampMs: Long, initializationDurationMs: Long) {
            Log.d(TAG,"onVideoDecoderInitialized")
        }

        override fun onVideoInputFormatChanged(format: Format?) {
            Log.d(TAG,"onVideoInputFormatChanged")
        }

        override fun onRenderedFirstFrame(surface: Surface?) {
            Log.d(TAG,"onRenderedFirstFrame")
        }*/

        override fun onTimelineChanged(timeline: Timeline?, manifest: Any?, reason: Int) {
            Log.d(TAG,"onTimelineChanged")
        }

        override fun onPlaybackParametersChanged(playbackParameters: PlaybackParameters?) {
            Log.d(TAG,"onPlaybackParametersChanged")
        }

        override fun onSeekProcessed() {
            Log.d(TAG,"onSeekProcessed")
        }

        override fun onTracksChanged(trackGroups: TrackGroupArray?, trackSelections: TrackSelectionArray?) {
            Log.d(TAG,"onTracksChanged")
        }

        override fun onPlayerError(error: ExoPlaybackException?) {
            Log.d(TAG,"onPlayerError")
        }

        override fun onLoadingChanged(isLoading: Boolean) {
            Log.d(TAG,"onLoadingChanged")
        }

        override fun onPositionDiscontinuity(reason: Int) {
            Log.d(TAG,"onPositionDiscontinuity")
        }

        override fun onRepeatModeChanged(repeatMode: Int) {
            Log.d(TAG,"onRepeatModeChanged")
        }

        override fun onShuffleModeEnabledChanged(shuffleModeEnabled: Boolean) {
            Log.d(TAG,"onShuffleModeEnabledChanged")
        }

        override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
            Log.d(TAG,"onPlayerStateChanged")
        }

        override fun onVideoSizeChanged(width: Int, height: Int, unappliedRotationDegrees: Int, pixelWidthHeightRatio: Float) {
            super.onVideoSizeChanged(width, height, unappliedRotationDegrees, pixelWidthHeightRatio)
            Log.d(TAG,"onVideoSizeChanged")
        }

    }
}