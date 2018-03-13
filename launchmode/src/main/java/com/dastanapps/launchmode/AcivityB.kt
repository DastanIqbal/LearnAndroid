package com.dastanapps.launchmode

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_acivity.*

class AcivityB : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_acivity)
        text1.text = "Activity B"
        text1.setOnClickListener {
            val intent = Intent(this, AcivityA::class.java)
            startActivityForResult(intent,4030)
        }
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        text1.text="From onNewIntent Activity B"
    }
}
