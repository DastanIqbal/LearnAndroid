package com.dastanapps.gameframework

/**
 * Created by dastaniqbal on 21/06/2017.
 * dastanIqbal@marvelmedia.com
 * 21/06/2017 12:14
 */
interface Audio {
    fun newMusic(fileName: String): Music
    fun newSound(fileName: String): Sound
}