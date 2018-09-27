package com.dastanapps.players

import android.content.Context
import android.net.Uri
import com.google.android.exoplayer2.DefaultLoadControl
import com.google.android.exoplayer2.DefaultRenderersFactory
import com.google.android.exoplayer2.ExoPlayerFactory
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory
import com.google.android.exoplayer2.source.ExtractorMediaSource
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.upstream.DefaultHttpDataSourceFactory
import com.google.android.exoplayer2.util.Util


/**
 * Created by dastaniqbal on 18/05/2018.

 * 18/05/2018 5:55
 */
class ExoPlayerHelper(val context: Context) {
    var player: SimpleExoPlayer? = null
    var playbackPosition: Long = 0
    var currentWindow: Int = 0
    var playWhenReady = true

    fun onStart() {
        if (Util.SDK_INT > 23) {
            initializePlayer()
        }
    }

    fun onResume() {
        if (Util.SDK_INT <= 23 || player == null) {
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
        player?.playWhenReady = playWhenReady
        player?.seekTo(currentWindow, playbackPosition)
    }

    fun prepareUrl(url: String) {
        player?.run {
            val uri = Uri.parse(url)
            val mediaSource = buildMediaSource(uri)
            prepare(mediaSource, true, false)
        }
    }

    private fun buildMediaSource(uri: Uri): MediaSource {
        return ExtractorMediaSource(uri,
                DefaultHttpDataSourceFactory("exoplayer-kruso"),
                DefaultExtractorsFactory(), null, null)
    }

    fun releasePlayer() {
        player.run {
            playbackPosition = player?.run {
                currentPosition
            } ?: 0
            currentWindow = player?.run {
                currentWindowIndex
            } ?: 0
            playWhenReady = player?.run {
                playWhenReady
            } ?: true
            player?.release()
            player = null
        }
    }
}