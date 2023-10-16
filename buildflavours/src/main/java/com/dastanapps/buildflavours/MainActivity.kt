package com.dastanapps.buildflavours

import android.graphics.Color
import android.os.Bundle
import android.webkit.WebView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.dastanapps.buildflavours.ads.AdsConfig

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val text = findViewById<TextView>(R.id.text)
        val webview = findViewById<WebView>(R.id.webview)

        text.text = AdsConfig().callMe(this)
        webview.setBackgroundColor(Color.RED)
        webview.loadUrl("https://tamm.abudhabi")
    }
}