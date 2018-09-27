package com.dastanapps.gameframework.impl

import android.content.res.AssetFileDescriptor
import android.media.MediaPlayer
import com.dastanapps.gameframework.Music

/**
 * Created by dastaniqbal on 09/10/2017.

 * 09/10/2017 12:07
 */
class AndroidMusic(val assetDiscriptor: AssetFileDescriptor) : Music, MediaPlayer.OnCompletionListener {
    val mediaPlayer = MediaPlayer()
    var isPrepared = false

    init {
        mediaPlayer.setDataSource(assetDiscriptor.fileDescriptor, assetDiscriptor.startOffset, assetDiscriptor.length)
        mediaPlayer.prepare()
        isPrepared = true
        mediaPlayer.setOnCompletionListener(this)
    }

    override fun play() {
        if (mediaPlayer.isPlaying)
            return
        synchronized(this) {
            if (!isPrepared) mediaPlayer.prepare()
            mediaPlayer.start()
        }
    }

    override fun pause() {
        if (mediaPlayer.isPlaying)
            mediaPlayer.pause()
    }

    override fun stop() {
        mediaPlayer.stop()
        synchronized(this) {
            isPrepared = false
        }
    }

    override fun setLooping(looping: Boolean) {
        mediaPlayer.isLooping = looping
    }

    override fun setVolume(volume: Float) {
        mediaPlayer.setVolume(volume, volume)
    }

    override fun isPlaying(): Boolean {
        return mediaPlayer.isPlaying
    }

    override fun isStopped(): Boolean {
        return !isPrepared
    }

    override fun isLooping(): Boolean {
        return mediaPlayer.isLooping
    }

    override fun dispose() {
        if (mediaPlayer.isPlaying) {
            mediaPlayer.stop()
        }
        mediaPlayer.release()
    }

    override fun onCompletion(mp: MediaPlayer?) {
        synchronized(this) {
            isPrepared = false
        }
    }
}