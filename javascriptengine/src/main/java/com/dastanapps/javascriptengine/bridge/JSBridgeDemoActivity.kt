package com.dastanapps.javascriptengine.bridge

import android.os.Bundle
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dastanapps.javascriptengine.ui.theme.LearnAndroidTheme
import java.text.SimpleDateFormat
import java.util.*

/**
 * Demo Activity showcasing JavaScript & Android Bridge functionality
 * Demonstrates bidirectional communication between JavaScript and Android
 * Now uses ViewModel to handle WebView bridge functionality with parallel JSONata execution
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
                    text = "🌉 Real End-to-End JSONata Demo",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "🏗️ ViewModel-Managed WebView with Real JSONata Library",
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
                        if (state.isInitialized) "✅ Real JSONata Ready" else "⏳ Loading JSONata Library..."

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

                // Show WebView status
                if (state.webView != null) {
                    Text(
                        text = "📱 WebView: Loaded from assets/real-jsonata-demo.html",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        }

        // Parallel Execution Status
        if (state.isInitialized) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "🚀 Parallel Execution Status",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Active: ${state.activeTaskCount} | Total Processed: ${state.totalTasksProcessed}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }

                    Row {
                        Button(
                            onClick = { viewModel.runJSONataStressTest() },
                            modifier = Modifier.padding(end = 8.dp)
                        ) {
                            Text("🔥 Stress Test")
                        }

                        Button(
                            onClick = { viewModel.runThreadedJSONataTest() }
                        ) {
                            Text("🧵 Threading")
                        }
                    }
                }
            }
        }

        // WebView from ViewModel State
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(vertical = 4.dp)
        ) {
            state.webView?.let { webView ->
                AndroidView(
                    factory = { webView },
                    modifier = Modifier.fillMaxSize()
                )
            } ?: Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "🏗️ Initializing ViewModel WebView...",
                        modifier = Modifier.padding(top = 16.dp),
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Text(
                        text = "Complete separation of concerns",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // JSONata Tasks Status
        if (state.jsonataTasks.isNotEmpty()) {
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
                            text = "📊 JSONata Tasks (${state.jsonataTasks.size})",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        TextButton(
                            onClick = { viewModel.clearCompletedTasks() }
                        ) {
                            Text("Clear Completed")
                        }
                    }

                    LazyColumn(
                        modifier = Modifier.height(150.dp)
                    ) {
                        items(state.jsonataTasks.reversed()) { task ->
                            JSONataTaskItem(task = task)
                        }
                    }
                }
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
                        modifier = Modifier.height(100.dp),
                        reverseLayout = true
                    ) {
                        items(state.eventLogs.reversed()) { log ->
                            val timestamp = SimpleDateFormat("HH:mm:ss.SSS", Locale.getDefault())
                                .format(Date(log.timestamp))

                            Text(
                                text = "[$timestamp] ${log.message}",
                                style = MaterialTheme.typography.bodySmall,
                                fontFamily = FontFamily.Monospace,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
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
                    .padding(top = 4.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "🔧 Architecture Info",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "✅ ViewModel-managed WebView bridge with parallel execution",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "⚡ Kotlin coroutines + thread pool for concurrent JSONata processing",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
fun JSONataTaskItem(task: JSONataTask) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = when (task.status) {
                TaskStatus.PENDING -> MaterialTheme.colorScheme.surfaceVariant
                TaskStatus.RUNNING -> Color(0xFFFFF3E0) // Light orange
                TaskStatus.COMPLETED -> Color(0xFFE8F5E8) // Light green
                TaskStatus.FAILED -> Color(0xFFFFEBEE) // Light red
            }
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = task.id,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = task.expression,
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (task.executionTime > 0) {
                    Text(
                        text = "⏱️ ${task.executionTime}ms",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Status indicator
            val (statusIcon, statusColor) = when (task.status) {
                TaskStatus.PENDING -> "⏳" to Color.Gray
                TaskStatus.RUNNING -> "🔄" to Color(0xFFFF9800) // Orange
                TaskStatus.COMPLETED -> "✅" to Color.Green
                TaskStatus.FAILED -> "❌" to Color.Red
            }

            Text(
                text = statusIcon,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}