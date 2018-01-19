package com.briskersystems.webviewfeatures

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.webkit.WebView

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val webView = findViewById<WebView>(R.id.webView);
        val allHeaders = HashMap<String, String>();
        allHeaders.put("Custom1", "Header1")
        allHeaders.put("Custom2", "Header2")
        allHeaders.put("Custom3", "Header3")
        webView.loadUrl("http://192.168.1.213/", allHeaders)
    }
}
