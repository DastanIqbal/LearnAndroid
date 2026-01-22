package com.dastanapps.flexlayout

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import com.dastanapps.flexlayout.core.FlexParser
import com.dastanapps.flexlayout.ui.DevToolScreen
import com.dastanapps.flexlayout.ui.theme.LearnAndroidTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        val json = try {
            assets.open("login_screen.json").bufferedReader().use { it.readText() }
        } catch (e: Exception) {
            e.printStackTrace()
            "{}"
        }
        
        setContent {
            LearnAndroidTheme {
                DevToolScreen(initialJson = json)
            }
        }
    }
}