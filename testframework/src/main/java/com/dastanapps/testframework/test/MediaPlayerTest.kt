package com.dastanapps.testframework.test

import android.media.AudioManager
import android.media.MediaPlayer
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.widget.TextView
import java.io.IOException


/**
 * Created by dastaniqbal on 05/07/2017.
 * dastanIqbal@marvelmedia.com
 * 05/07/2017 11:33
 */
class MediaPlayerTest : AppCompatActivity() {
    var mediaPlayer: MediaPlayer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val textView = TextView(this)
        setContentView(textView)

        volumeControlStream = AudioManager.STREAM_MUSIC
        mediaPlayer = MediaPlayer()
        try {
            val assetFD = assets.openFd("music.ogg")
            mediaPlayer?.setDataSource(assetFD.fileDescriptor, assetFD.startOffset, assetFD.length)
            mediaPlayer?.prepare()
            mediaPlayer?.isLooping = true
        } catch (e: IOException) {
            textView.text = "Couldn't load music file " + e.message
        }
    }

    override fun onResume() {
        super.onResume()
        if (mediaPlayer != null)
            mediaPlayer?.start()
    }

    override fun onStop() {
        super.onStop()
        if (mediaPlayer != null) {
            mediaPlayer?.pause()
            if (isFinishing) {
                mediaPlayer?.stop()
                mediaPlayer?.release()
            }
        }
    }
}