package com.dastanapps.gameframework

/**
 * Created by dastaniqbal on 21/06/2017.

 * 21/06/2017 12:27
 */
interface Game {
    fun getInput(): Input
    fun getFileIO(): FileIO
    fun getGraphics(): Graphics
    fun getAudio(): Audio
    fun setScreen(screen: Screen)
    fun getCurrentScreen(): Screen
    fun getStartScreen(): Screen
}