package com.dastanapps.players

import android.content.Intent
import android.os.Bundle
import android.widget.AdapterView
import android.widget.ListView
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    lateinit var exoPlayerHelper: ExoPlayerHelper
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        exoPlayerHelper = ExoPlayerHelper(this)
        val listView = findViewById<ListView>(R.id.listview)
        listView.onItemClickListener = AdapterView.OnItemClickListener { parent, view, position, id ->
            when (position) {
                0 -> startActivity(Intent(this, ExoPlayerActivity::class.java))
                1 -> startActivity(Intent(this, ExoPlayer242::class.java))
                2 -> startActivity(Intent(this, MediaPlayerActivity::class.java))
                3 -> startActivity(Intent(this, TextureViewActivity::class.java))
            }
        }
    }

    override fun onStart() {
        super.onStart()
        exoPlayerHelper.onStart()
    }

    override fun onResume() {
        super.onResume()
        exoPlayerHelper.onStart()
        exoPlayerHelper.playUrl("http://www.panacherock.com/downloads/mp3/01_Sayso.mp3",object : ExoPlayerHelper.Listener() {

        })
    }

    override fun onPause() {
        super.onPause()
        exoPlayerHelper.onStop()
    }

    override fun onStop() {
        super.onStop()
        exoPlayerHelper.onStop()
    }
}
