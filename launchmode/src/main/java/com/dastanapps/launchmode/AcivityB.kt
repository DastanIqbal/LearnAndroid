package com.dastanapps.launchmode

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_acivity.*

class AcivityB : AppCompatActivity() {

    private var str: String = "";
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_acivity)
        text1.text = "Activity B"
        text1.setOnClickListener {
            val intent = Intent(this, AcivityA::class.java)
            intent.putExtra("src", "B")
            startActivityForResult(intent, 4030)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 4030) {
            str += "onActivityResult"
            text1.text = str
        }
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        str += "From onNewIntent Activity B"
        text1.text = str
    }
}
