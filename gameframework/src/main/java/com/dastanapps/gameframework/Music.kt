package com.dastanapps.gameframework

/**
 * Created by dastaniqbal on 21/06/2017.

 * 21/06/2017 12:10
 */
interface Music {
    fun play()
    fun pause()
    fun stop()
    fun setLooping(looping: Boolean)
    fun setVolume(volume: Float)
    fun isPlaying(): Boolean
    fun isStopped(): Boolean
    fun isLooping(): Boolean
    fun dispose()
}