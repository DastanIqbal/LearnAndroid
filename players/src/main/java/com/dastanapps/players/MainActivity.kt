package com.dastanapps.players

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.widget.AdapterView
import android.widget.ListView

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val listView = findViewById<ListView>(R.id.listview)
        listView.onItemClickListener = AdapterView.OnItemClickListener { parent, view, position, id ->
            if (position == 0) {
                startActivity(Intent(this, ExoPlayer::class.java))
            } else if (position == 1) {
                startActivity(Intent(this, MediaPlayerActivity::class.java))
            }
        }
    }
}
