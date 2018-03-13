package com.dastanapps.launchmode

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        text2.text = "Main Activity"
        text2.setTextColor(Color.BLACK)
        text2.setOnClickListener {
            val intent = Intent(this@MainActivity, AcivityA::class.java)
            startActivity(intent)
        }
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        text2.text="From onNewIntent"
    }
}
