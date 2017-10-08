package com.dastanapps.gameframework.impl

import android.app.Activity
import android.media.AudioManager
import android.media.SoundPool
import com.dastanapps.gameframework.Audio
import com.dastanapps.gameframework.Music
import com.dastanapps.gameframework.Sound

/**
 * Created by dastaniqbal on 08/10/2017.
 * dastanIqbal@marvelmedia.com
 * 08/10/2017 11:59
 */
class AndroidAudio(val activity: Activity) : Audio {
    val soundPool = SoundPool(20, AudioManager.STREAM_MUSIC, 0)
    val assets = activity.assets

    override fun newMusic(fileName: String): Music {
        return AndroidMusic(assets.openFd(fileName))
    }

    override fun newSound(fileName: String): Sound {
        return AndroidSound(soundPool, soundPool.load(assets.openFd(fileName), 0))
    }
}