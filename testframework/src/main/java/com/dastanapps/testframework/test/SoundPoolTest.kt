package com.dastanapps.testframework.test

import android.media.AudioManager
import android.media.SoundPool
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.MotionEvent
import android.view.View
import android.widget.TextView
import java.io.IOException

/**
 * Created by dastaniqbal on 05/07/2017.
 * dastanIqbal@marvelmedia.com
 * 05/07/2017 11:08
 */
class SoundPoolTest : AppCompatActivity(), View.OnTouchListener {

    var soundPool: SoundPool? = null
    var explosionId = -1
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val textView = TextView(this)
        textView.setOnTouchListener(this)
        setContentView(textView)

        volumeControlStream = AudioManager.STREAM_MUSIC
        soundPool = SoundPool(20, AudioManager.STREAM_MUSIC, 0)
        try {
            val assestFD = assets.openFd("explosion-02.ogg")
            explosionId = soundPool?.load(assestFD, 1) as Int
        } catch (e: IOException) {
            textView.text = "Couldn't load sound effect from asset " + e.message
        }
    }

    override fun onTouch(v: View?, event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_UP -> {
                if (explosionId != -1) {
                    soundPool?.play(explosionId, 1f, 1f, 0, 0, 1f)
                }
            }
        }
        return true
    }

    override fun onDestroy() {
        super.onDestroy()
        soundPool?.unload(explosionId)
    }
}