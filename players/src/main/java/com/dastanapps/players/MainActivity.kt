package com.dastanapps.players

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.widget.AdapterView
import android.widget.ListView

class MainActivity : AppCompatActivity() {

    lateinit var exoPlayerHelper: ExoPlayerHelper
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        exoPlayerHelper = ExoPlayerHelper(this)
        val listView = findViewById<ListView>(R.id.listview)
        listView.onItemClickListener = AdapterView.OnItemClickListener { parent, view, position, id ->
            when (position) {
                0 -> startActivity(Intent(this, ExoPlayer::class.java))
                1 -> startActivity(Intent(this, ExoPlayer242::class.java))
                2 -> startActivity(Intent(this, MediaPlayerActivity::class.java))
            }
        }
    }

    override fun onStart() {
        super.onStart()
        exoPlayerHelper.onStart()
    }

    override fun onResume() {
        super.onResume()
        exoPlayerHelper.onResume()
        exoPlayerHelper.prepareUrl("http://www.panacherock.com/downloads/mp3/01_Sayso.mp3")
    }

    override fun onPause() {
        super.onPause()
        exoPlayerHelper.onPause()
    }

    override fun onStop() {
        super.onStop()
        exoPlayerHelper.onStop()
    }
}
