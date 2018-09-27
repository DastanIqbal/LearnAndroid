package com.dastanapps.gameframework.impl

import android.media.SoundPool
import com.dastanapps.gameframework.Sound

/**
 * Created by dastaniqbal on 09/10/2017.

 * 09/10/2017 12:06
 */
class AndroidSound(val soundPool: SoundPool, val soundId: Int) : Sound {
    override fun play(volume: Float) {
        soundPool.play(soundId, volume, volume, 0, 0, 1f)
    }

    override fun dispose() {
        soundPool.unload(soundId)
    }

}