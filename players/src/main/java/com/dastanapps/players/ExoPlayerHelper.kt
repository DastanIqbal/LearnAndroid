package com.dastanapps.players

import android.content.Context
import android.net.Uri
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
import java.io.File


/**
 * Created by dastaniqbal on 18/05/2018.
 * 18/05/2018 5:55
 */
class ExoPlayerHelper(val context: Context) {
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

    fun onResume() {
        if (Util.SDK_INT <= 23 && player == null) {
            initializePlayer()
        }
    }

    fun onPause() {
        if (Util.SDK_INT <= 23) {
            releasePlayer()
        }
    }

    fun onStop() {
        if (Util.SDK_INT > 23) {
            releasePlayer()
        }
    }

    private fun initializePlayer() {
        player = ExoPlayerFactory.newSimpleInstance(
                DefaultRenderersFactory(context),
                DefaultTrackSelector(), DefaultLoadControl())
        player?.seekTo(currentWindow, playbackPosition)
    }

    fun playUrl(url: String, listener: Player.EventListener? = null) {
        currentUrl = url
        releasePlayer()
        initializePlayer()
        player?.let {
            val uri = Uri.parse(url)
            val mediaSource = buildMediaSource(uri)
            playWhenReady(true)

            it.seekTo(currentPosition)
            it.addListener(listener)
            it.prepare(mediaSource, false, false)
        }
    }

    fun playLocal(filepath: String, listener: Player.EventListener? = null) {
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
            it.prepare(mediaSource, false, false)

            playWhenReady(true)
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
        val defaultHttpDataSourceFactory = DefaultHttpDataSourceFactory("exoplayer-kruso")
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
            player?.release()
            player = null
        }
    }

    fun seekTo(position: Long) {
        player?.run {
            seekTo(position)
        }
    }

    open class Listener : Player.EventListener {
        override fun onTimelineChanged(timeline: Timeline?, manifest: Any?) {
        }

        override fun onPlaybackParametersChanged(playbackParameters: PlaybackParameters?) {}

        override fun onSeekProcessed() {}

        override fun onTracksChanged(trackGroups: TrackGroupArray?, trackSelections: TrackSelectionArray?) {}

        override fun onPlayerError(error: ExoPlaybackException?) {}

        override fun onLoadingChanged(isLoading: Boolean) {}

        override fun onPositionDiscontinuity(reason: Int) {}

        override fun onRepeatModeChanged(repeatMode: Int) {}

        override fun onShuffleModeEnabledChanged(shuffleModeEnabled: Boolean) {}

        override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {}

    }
}