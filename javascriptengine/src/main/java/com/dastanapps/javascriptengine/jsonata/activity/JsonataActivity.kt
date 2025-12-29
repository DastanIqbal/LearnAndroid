package com.dastanapps.javascriptengine.jsonata.activity

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class JsonataActivity : ComponentActivity() {

    private val webViewEngine by lazy { WebViewEngine(this) }

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Get JSON and expression from Intent extras; fall back to defaults
        val json = intent.getStringExtra("json") ?: """
            {
              "FirstName": "Fred",
              "Surname": "Smith",
              "Age": 28
            }
        """.trimIndent()
        val expr = intent.getStringExtra("expr") ?: "FirstName"

        setContent {
            var result by remember { mutableStateOf("Waiting for JSONata result...") }
            var error by remember { mutableStateOf<String?>(null) }
            val scope = rememberCoroutineScope()

            // Execute JSONata when ready
            LaunchedEffect(Unit) {
                try {
                    webViewEngine.initialize()
                    // Wait a bit for WebView to initialize
                    delay(500)
                    val evalResult = webViewEngine.evaluate(json, expr)
                    result = evalResult
                } catch (e: Exception) {
                    error = e.message
                }
            }

            MaterialTheme {
                Scaffold {
                    JsonataScreen(
                        result = result,
                        error = error,
                        modifier = Modifier.padding(it)
                    )
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        webViewEngine.destroy()
    }
}

@Composable
fun JsonataScreen(
    result: String,
    error: String?,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxSize()) {
        // Display result or error only - WebView is completely headless
        Text(
            text = (error ?: result).plus(" in text"),
            modifier = Modifier
                .background(color = if (error != null) Color.Red else Color.Green)
                .padding(16.dp)
        )
    }
}
