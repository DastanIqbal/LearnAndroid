package com.dastanapps.javascriptengine.bridge

import android.os.Bundle
import android.webkit.WebView
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dastanapps.javascriptengine.ui.theme.LearnAndroidTheme
import java.text.SimpleDateFormat
import java.util.*

/**
 * Demo Activity showcasing JavaScript & Android Bridge functionality
 * Demonstrates bidirectional communication between JavaScript and Android
 * Now uses ViewModel to handle WebView bridge functionality
 */
class JSBridgeDemoActivity : ComponentActivity() {

    // Use ViewModel to handle WebView bridge functionality
    private val viewModel: JSBridgeDemoViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            LearnAndroidTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    JSBridgeDemo(
                        viewModel = viewModel,
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JSBridgeDemo(
    viewModel: JSBridgeDemoViewModel,
    modifier: Modifier = Modifier
) {
    // Collect ViewModel state
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current

    // WebView reference
    var webView: WebView? by remember { mutableStateOf(null) }

    // Initialize WebView when first created
    LaunchedEffect(Unit) {
        if (webView == null) {
            webView = WebView(context).apply {
                viewModel.initializeWebView(this)
            }
        }
    }

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
                    text = "🌉 JavaScript & Android Bridge Demo",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Bidirectional communication between JavaScript and Android",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )

                // Bridge status indicator
                Row(
                    modifier = Modifier.padding(top = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val statusColor =
                        if (state.isInitialized) Color.Green else Color(0xFFFFA500) // Orange
                    val statusText =
                        if (state.isInitialized) "✅ Bridge Ready" else "⏳ Initializing..."

                    Text(
                        text = statusText,
                        style = MaterialTheme.typography.bodySmall,
                        color = statusColor
                    )

                    // Show error if any
                    state.error?.let { error ->
                        Text(
                            text = " - Error: $error",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Red,
                            modifier = Modifier.padding(start = 8.dp)
                        )
                    }
                }
            }
        }

        // WebView
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(vertical = 8.dp)
        ) {
            webView?.let { view ->
                AndroidView(
                    factory = { view },
                    modifier = Modifier.fillMaxSize()
                )
            } ?: Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
                Text(
                    text = "Loading WebView...",
                    modifier = Modifier.padding(top = 16.dp)
                )
            }
        }

        // Android-side event log from ViewModel
        if (state.eventLogs.isNotEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "📱 Android Event Log (${state.eventLogs.size})",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        TextButton(
                            onClick = { viewModel.clearEventLog() }
                        ) {
                            Text("Clear")
                        }
                    }

                    LazyColumn(
                        modifier = Modifier.height(120.dp),
                        reverseLayout = true
                    ) {
                        items(state.eventLogs.reversed()) { log ->
                            val timestamp = SimpleDateFormat("HH:mm:ss.SSS", Locale.getDefault())
                                .format(Date(log.timestamp))

                            Text(
                                text = "[$timestamp] ${log.message}",
                                style = MaterialTheme.typography.bodySmall,
                                fontFamily = FontFamily.Monospace,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }

        // Debug information
        if (state.isInitialized) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "🔧 Debug Info",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "WebView bridge is managed by ViewModel",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "All bridge functions and events are handled in the ViewModel scope",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}