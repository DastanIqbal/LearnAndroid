package com.dastanapps.androidp.subscription

import android.content.Context
import android.net.Uri
import android.util.Log
import com.google.android.exoplayer2.*
import com.google.android.exoplayer2.source.ExtractorMediaSource
import com.google.android.exoplayer2.source.LoopingMediaSource
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.source.TrackGroupArray
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.trackselection.TrackSelectionArray
import com.google.android.exoplayer2.upstream.*
import com.google.android.exoplayer2.util.Util
import java.io.File


/**
 * Created by dastaniqbal on 18/05/2018.
 * dastanIqbal@marvelmedia.com
 * 18/05/2018 5:55
 */
class ExoPlayerHelper(val context: Context) : KViewActionHandler {
    private val TAG = this::class.java.simpleName

    init {
        PlayerLifecycleObserver.INSTANCE.registerViewActionHandler(this)
    }

    var player: SimpleExoPlayer? = null
    var currentWindow = 0
    var playWhenReady = false
    var playbackPosition = 0L
        get() {
            return player?.run {
                currentPosition
            } ?: field
        }
    private var currentUrl: String? = null
    private var listener: Listener? = null
    var seekMode: SeekParameters = SeekParameters.DEFAULT
        set(value) {
            field = value
            player?.seekParameters = field
        }

    override fun onStart() {
        if (Util.SDK_INT > 23 && player == null) {
            initializePlayer()
        }
    }


    override fun onStop() {
        if (Util.SDK_INT > 23) {
            releasePlayer()
        }
    }

    private fun initializePlayer() {
        player = null
        player = ExoPlayerFactory.newSimpleInstance(context,
                DefaultRenderersFactory(context),
                DefaultTrackSelector(), DefaultLoadControl())
        player?.seekParameters = seekMode
        player?.seekTo(currentWindow, playbackPosition)
    }


    fun setRepeatMode(mode: Int) {
        player?.repeatMode = mode
    }

    fun playUrl(url: String, listener: Listener? = null) {
        currentUrl = url
        releasePlayer()
        initializePlayer()
        this@ExoPlayerHelper.listener = listener
        player?.let {
            val uri = Uri.parse(url)
            val mediaSource = buildMediaSource(uri)
            playWhenReady(true)

            it.seekTo(playbackPosition)
            it.addListener(this@ExoPlayerHelper.listener)
            it.addVideoListener(this@ExoPlayerHelper.listener)
            it.prepare(mediaSource, false, false)
        }
    }

    fun playLocal(filepath: String, listener: Listener? = null) {
        Log.d(TAG, "FilePath:$filepath")
        currentUrl = filepath
        if (player == null) {
            releasePlayer()
            initializePlayer()
        }
        player?.let {
            val uri = Uri.fromFile(File(filepath))
            val mediaSource = buildLocalMediaSource(uri)

            it.seekTo(playbackPosition)
            this@ExoPlayerHelper.listener = listener
            it.addVideoListener(this@ExoPlayerHelper.listener)
            it.addListener(this@ExoPlayerHelper.listener)
            it.prepare(mediaSource)//;, false, false)
        }
    }

    fun playfromRaw(rawid: Int, loop: Boolean = false, listener: Listener? = null) {
        if (player == null) {
            releasePlayer()
            initializePlayer()
        }
        player?.let {
            var mediaSource = buildRawMediaSource(rawid)
            if (loop) {
                mediaSource = LoopingMediaSource(mediaSource)
            }
            playWhenReady(playWhenReady)

            it.seekTo(playbackPosition)
           // this@ExoPlayerHelper.listener = listener
           // it.addVideoListener(this@ExoPlayerHelper.listener)
           // it.addListener(this@ExoPlayerHelper.listener)
            it.prepare(mediaSource)//;, false, false)
        }
    }

    fun pause() {
        player?.let {
            playWhenReady(false)
            Log.d(TAG, "Current Position: " + it.currentPosition)
            playbackPosition = it.currentPosition
        }
    }

    fun isPlaying(url: String): Boolean {
        return if (currentUrl == url) {
            playWhenReady
        } else {
            false
        }
    }


    fun isPlaying() = playWhenReady


    fun playWhenReady(playWhenReady: Boolean) {
        Log.d(TAG, "Playing Position ${player?.currentPosition?.toString()}")
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

    private fun buildRawMediaSource(rawId: Int): MediaSource {
        val dataSpec = DataSpec(RawResourceDataSource.buildRawResourceUri(rawId))
        val rawDataSource = RawResourceDataSource(context)
        try {
            rawDataSource.open(dataSpec)
        } catch (e: RawResourceDataSource.RawResourceDataSourceException) {
            e.printStackTrace()
        }
        val factory = DataSource.Factory { rawDataSource }
        return ExtractorMediaSource.Factory(factory).createMediaSource(rawDataSource.uri)
    }


    fun releasePlayer() {
        player?.run {

            this@ExoPlayerHelper.playbackPosition = player?.run {
                currentPosition
            } ?: this@ExoPlayerHelper.playbackPosition

            currentWindow = player?.run {
                currentWindowIndex
            } ?: currentWindow

            unregisterListener()

            stop(true)
            release()
            clearVideoSurface()
            setVideoTextureView(null)
            setVideoSurfaceView(null)
            player = null
        }
    }

    fun unregisterListener() {
        player?.removeListener(listener)
        player?.removeVideoListener(listener)
    }

    fun resetPlayer() {
        releasePlayer()
        playbackPosition = 0
        currentWindow = 0
    }

    fun seekTo(position: Long) {
        player?.run {
            Log.d(TAG, "SeekTo $position")
            seekTo(position)
            this@ExoPlayerHelper.playbackPosition = currentPosition
        }
    }

    open class Listener : Player.EventListener, SimpleExoPlayer.VideoListener {
        private val TAG = "ExoPlayerHelper" + this::class.java.simpleName

        override fun onPlaybackParametersChanged(playbackParameters: PlaybackParameters?) {
            Log.d(TAG, "onPlaybackParametersChanged")
        }

        override fun onSeekProcessed() {
            Log.d(TAG, "onSeekProcessed")
        }

        override fun onTracksChanged(trackGroups: TrackGroupArray?, trackSelections: TrackSelectionArray?) {
            Log.d(TAG, "onTracksChanged")
        }

        override fun onPlayerError(error: ExoPlaybackException?) {
            Log.d(TAG, "onPlayerError:${error?.message}")
            error?.printStackTrace()
        }

        override fun onLoadingChanged(isLoading: Boolean) {
            Log.d(TAG, "onLoadingChanged:$isLoading")
        }

        override fun onPositionDiscontinuity(reason: Int) {
            Log.d(TAG, "onPositionDiscontinuity:$reason")
        }

        override fun onRepeatModeChanged(repeatMode: Int) {
            Log.d(TAG, "onRepeatModeChanged:$repeatMode")
        }

        override fun onShuffleModeEnabledChanged(shuffleModeEnabled: Boolean) {
            Log.d(TAG, "onShuffleModeEnabledChanged:$shuffleModeEnabled")
        }

        override fun onTimelineChanged(timeline: Timeline?, manifest: Any?, reason: Int) {
            Log.d(TAG, "onTimelineChanged:$reason")
        }


        override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
            Log.d(TAG, "onPlayerStateChanged: $playWhenReady,$playbackState")
        }

        override fun onVideoSizeChanged(width: Int, height: Int, unappliedRotationDegrees: Int, pixelWidthHeightRatio: Float) {
            super.onVideoSizeChanged(width, height, unappliedRotationDegrees, pixelWidthHeightRatio)
            Log.d(TAG, "onVideoSizeChanged: $width,$height;$unappliedRotationDegrees;$pixelWidthHeightRatio")
        }

        override fun onRenderedFirstFrame() {
            super.onRenderedFirstFrame()
            Log.d(TAG, "onRenderedFirstFrame")
        }

        override fun onSurfaceSizeChanged(width: Int, height: Int) {
            super.onSurfaceSizeChanged(width, height)
            Log.d(TAG, "onSurfaceSizeChanged: $width,$height")
        }

    }

    interface OnCompletionListener {
        fun onCompletion(mp: ExoPlayerHelper)
    }

    interface OnErrorListener {
        fun onError(error: String, type: Int, renderIndex: Int): Boolean
    }

}