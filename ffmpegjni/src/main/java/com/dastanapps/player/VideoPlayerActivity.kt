package com.dastanapps.player

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.dastanapps.ffmpegjni.R

class VideoPlayerActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_video_player)
    }
}
