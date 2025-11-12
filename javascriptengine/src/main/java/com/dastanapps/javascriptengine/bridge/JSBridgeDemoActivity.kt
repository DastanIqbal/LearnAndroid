package com.dastanapps.javascriptengine.bridge

import android.os.Bundle
import android.util.Log
import android.view.ViewGroup
import android.webkit.WebView
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dastanapps.javascriptengine.ui.theme.LearnAndroidTheme
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import com.dastanapps.javascriptengine.bridge.TaskStatus

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
    val state by viewModel.state.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Top section with status and controls - scrollable
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(0.6f), // Take 60% of screen
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header Card
            item {
                ElevatedCard(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "🌉 JS Bridge Demo",
                                style = MaterialTheme.typography.headlineSmall,
                                modifier = Modifier.weight(1f)
                            )

                            Text(
                                text = if (state.isInitialized) "✅ Ready" else "⏳ Loading",
                                style = MaterialTheme.typography.bodyMedium,
                                color = if (state.isInitialized) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                            )
                        }

                        if (state.error != null) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Surface(
                                color = MaterialTheme.colorScheme.errorContainer,
                                shape = MaterialTheme.shapes.small
                            ) {
                                Text(
                                    text = "❌ ${state.error}",
                                    modifier = Modifier.padding(8.dp),
                                    color = MaterialTheme.colorScheme.onErrorContainer,
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                    }
                }
            }

            // Parallel Execution Controls
            item {
                ElevatedCard(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "⚡ Parallel JSONata Execution",
                                style = MaterialTheme.typography.titleMedium,
                                modifier = Modifier.weight(1f)
                            )

                            if (state.jsonataTasks.isNotEmpty()) {
                                Text(
                                    text = "${state.jsonataTasks.count { it.status == TaskStatus.COMPLETED }}/${state.jsonataTasks.size}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            FilledTonalButton(
                                onClick = { viewModel.runJSONataStressTest() },
                                modifier = Modifier.weight(1f),
                                enabled = state.isInitialized
                            ) {
                                Text("🔥 Stress Test")
                            }

                            FilledTonalButton(
                                onClick = { viewModel.runThreadedJSONataTest() },
                                modifier = Modifier.weight(1f),
                                enabled = state.isInitialized
                            ) {
                                Text("🧵 Threaded Test")
                            }
                        }
                    }
                }
            }

            // JSONata Tasks Status
            if (state.jsonataTasks.isNotEmpty()) {
                item {
                    ElevatedCard(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Text(
                                text = "📊 JSONata Tasks Status",
                                style = MaterialTheme.typography.titleMedium
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            state.jsonataTasks.forEach { task ->
                                JSONataTaskItem(task)
                                Spacer(modifier = Modifier.height(4.dp))
                            }
                        }
                    }
                }
            }

            // Event Log
            if (state.eventLogs.isNotEmpty()) {
                item {
                    ElevatedCard(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Text(
                                text = "📝 Event Log",
                                style = MaterialTheme.typography.titleMedium
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                color = MaterialTheme.colorScheme.surfaceVariant,
                                shape = MaterialTheme.shapes.small
                            ) {
                                LazyColumn(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(120.dp)
                                        .padding(8.dp),
                                    reverseLayout = true
                                ) {
                                    items(state.eventLogs.takeLast(10)) { log ->
                                        val timestamp = SimpleDateFormat(
                                            "HH:mm:ss.SSS",
                                            Locale.getDefault()
                                        ).format(Date(log.timestamp))
                                        Text(
                                            text = "[$timestamp] ${log.message}",
                                            style = MaterialTheme.typography.bodySmall,
                                            modifier = Modifier.padding(vertical = 1.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Architecture Info
            if (state.isInitialized) {
                item {
                    ElevatedCard(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Text(
                                text = "🏗️ Architecture Features",
                                style = MaterialTheme.typography.titleMedium
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            val features = listOf(
                                "✅ MVVM Architecture with ViewModel-managed WebView",
                                "✅ Reactive state management with StateFlow",
                                "✅ Structured concurrency with coroutines",
                                "✅ Real JSONata library execution (not simulated)",
                                "✅ Parallel processing with configurable threading",
                                "✅ Comprehensive error handling and logging",
                                "✅ Memory leak prevention with proper cleanup"
                            )

                            features.forEach { feature ->
                                Text(
                                    text = feature,
                                    style = MaterialTheme.typography.bodySmall,
                                    modifier = Modifier.padding(vertical = 1.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        // Separator
        HorizontalDivider(
            modifier = Modifier.padding(vertical = 8.dp),
            color = MaterialTheme.colorScheme.outline
        )

        // Bottom section with WebView - independent scrolling
        ElevatedCard(
            modifier = Modifier
                .fillMaxWidth()
                .weight(0.4f) // Take 40% of screen
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                Text(
                    text = "🌐 WebView JSONata Demo",
                    style = MaterialTheme.typography.titleMedium
                )

                Text(
                    text = "Interactive JSONata testing interface",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.outline
                )

                Spacer(modifier = Modifier.height(8.dp))

                if (state.webView != null) {
                    AndroidView(
                        modifier = Modifier.fillMaxSize(),
                        factory = { context ->
                            state.webView!!.apply {
                                // Remove from any existing parent
                                (parent as? ViewGroup)?.removeView(this)
                            }
                        },
                        update = { webView ->
                            // Ensure scrolling remains enabled
                            webView.isNestedScrollingEnabled = false
                        }
                    )
                } else {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            CircularProgressIndicator()
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Loading WebView...",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun JSONataTasksCard(
    tasks: List<JSONataTask>,
    onClearCompleted: () -> Unit
) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "\uD83D\uDCCA",
                        style = MaterialTheme.typography.titleLarge
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "JSONata Tasks (${tasks.size})",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
                TextButton(
                    onClick = onClearCompleted
                ) {
                    Text("Clear Completed")
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            LazyColumn(
                modifier = Modifier.height(180.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(tasks.reversed()) { task ->
                    JSONataTaskItem(task = task)
                }
            }
        }
    }
}

@Composable
fun EventLogCard(
    eventLogs: List<BridgeEventLog>,
    onClear: () -> Unit
) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "\uD83D\uDCF1",
                        style = MaterialTheme.typography.titleLarge
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Android Event Log (${eventLogs.size})",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
                TextButton(
                    onClick = onClear
                ) {
                    Text("Clear")
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp),
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = RoundedCornerShape(8.dp)
            ) {
                LazyColumn(
                    modifier = Modifier.padding(12.dp),
                    reverseLayout = true,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    items(eventLogs.reversed()) { log ->
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
}

@Composable
fun ArchitectureInfoCard() {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "\uD83D\uDD27",
                    style = MaterialTheme.typography.titleLarge
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "Architecture Info",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Column(
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                ArchitectureInfoItem(
                    icon = "✅",
                    text = "ViewModel-managed WebView bridge with parallel execution"
                )
                ArchitectureInfoItem(
                    icon = "⚡",
                    text = "Kotlin coroutines + thread pool for concurrent JSONata processing"
                )
                ArchitectureInfoItem(
                    icon = "\uD83C\uDF10",
                    text = "Real JSONata library execution with no simulation"
                )
            }
        }
    }
}

@Composable
fun ArchitectureInfoItem(
    icon: String,
    text: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = icon,
            style = MaterialTheme.typography.bodyMedium
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun JSONataTaskItem(task: JSONataTask) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.elevatedCardColors(
            containerColor = when (task.status) {
                TaskStatus.PENDING -> MaterialTheme.colorScheme.surfaceVariant
                TaskStatus.RUNNING -> MaterialTheme.colorScheme.tertiaryContainer
                TaskStatus.COMPLETED -> Color(0xFFE8F5E8) // Light green
                TaskStatus.FAILED -> MaterialTheme.colorScheme.errorContainer
            }
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                // Task ID with status badge
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = task.id,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = when (task.status) {
                            TaskStatus.PENDING -> MaterialTheme.colorScheme.onSurfaceVariant
                            TaskStatus.RUNNING -> MaterialTheme.colorScheme.onTertiaryContainer
                            TaskStatus.COMPLETED -> Color(0xFF2E7D32) // Dark green
                            TaskStatus.FAILED -> MaterialTheme.colorScheme.onErrorContainer
                        }
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    // Status badge
                    Surface(
                        color = when (task.status) {
                            TaskStatus.PENDING -> MaterialTheme.colorScheme.outline
                            TaskStatus.RUNNING -> MaterialTheme.colorScheme.primary
                            TaskStatus.COMPLETED -> Color(0xFF4CAF50) // Green
                            TaskStatus.FAILED -> MaterialTheme.colorScheme.error
                        },
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = task.status.name,
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Expression
                Text(
                    text = task.expression,
                    style = MaterialTheme.typography.bodyMedium,
                    color = when (task.status) {
                        TaskStatus.PENDING -> MaterialTheme.colorScheme.onSurfaceVariant
                        TaskStatus.RUNNING -> MaterialTheme.colorScheme.onTertiaryContainer
                        TaskStatus.COMPLETED -> Color(0xFF1B5E20) // Dark green
                        TaskStatus.FAILED -> MaterialTheme.colorScheme.onErrorContainer
                    },
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    fontFamily = FontFamily.Monospace
                )

                // Execution time and error (if any)
                if (task.executionTime > 0) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "⏱️",
                            style = MaterialTheme.typography.labelSmall
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${task.executionTime}ms",
                            style = MaterialTheme.typography.labelSmall,
                            color = when (task.status) {
                                TaskStatus.PENDING -> MaterialTheme.colorScheme.onSurfaceVariant
                                TaskStatus.RUNNING -> MaterialTheme.colorScheme.onTertiaryContainer
                                TaskStatus.COMPLETED -> Color(0xFF2E7D32) // Dark green
                                TaskStatus.FAILED -> MaterialTheme.colorScheme.onErrorContainer
                            },
                            fontWeight = FontWeight.Medium
                        )

                        // Show result preview for completed tasks
                        if (task.status == TaskStatus.COMPLETED && !task.result.isNullOrBlank()) {
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = "📊",
                                style = MaterialTheme.typography.labelSmall
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Result available",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color(0xFF2E7D32), // Dark green
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }

                // Error message for failed tasks
                if (task.status == TaskStatus.FAILED && !task.error.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "❌",
                            style = MaterialTheme.typography.labelSmall
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = task.error.take(50) + if (task.error.length > 50) "..." else "",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            // Status icon
            val (statusIcon, statusColor) = when (task.status) {
                TaskStatus.PENDING -> "⏳" to MaterialTheme.colorScheme.onSurfaceVariant
                TaskStatus.RUNNING -> "🔄" to MaterialTheme.colorScheme.primary
                TaskStatus.COMPLETED -> "✅" to Color(0xFF4CAF50) // Green
                TaskStatus.FAILED -> "❌" to MaterialTheme.colorScheme.error
            }

            Text(
                text = statusIcon,
                style = MaterialTheme.typography.headlineSmall
            )
        }
    }
}