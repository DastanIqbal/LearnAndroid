package com.dastanapps.javascriptengine.bridge

import android.os.Bundle
import android.webkit.WebView
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.lifecycleScope
import com.dastanapps.javascriptengine.ui.theme.LearnAndroidTheme
import kotlinx.coroutines.launch

/**
 * Simple debug activity to test bridge functionality
 */
class BridgeDebugActivity : ComponentActivity() {

    private lateinit var webViewBridge: WebViewBridge
    private lateinit var webView: WebView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Initialize WebView
        webView = WebView(this)

        // Initialize bridge
        webViewBridge = WebViewBridge(this, webView)

        setContent {
            LearnAndroidTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    BridgeDebugScreen(
                        webViewBridge = webViewBridge,
                        webView = webView,
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }

        // Initialize bridge after content is set
        lifecycleScope.launch {
            initializeBridge()
        }
    }

    private fun initializeBridge() {
        // Initialize the bridge
        webViewBridge.initialize()

        // Load debug HTML from assets
        webView.loadUrl("file:///android_asset/debug-bridge-test.html")
    }

    override fun onDestroy() {
        super.onDestroy()
        webViewBridge.cleanup()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BridgeDebugScreen(
    webViewBridge: WebViewBridge,
    webView: WebView,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxSize()) {
        // Header
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "🔧 Bridge Debug Test",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Testing JavaScript & Android Bridge functionality",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }

        // WebView
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(vertical = 8.dp)
        ) {
            AndroidView(
                factory = { webView },
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}