package com.dastanapps.javascriptengine.bridge

import android.os.Bundle
import android.webkit.WebView
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
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
import androidx.lifecycle.lifecycleScope
import com.dastanapps.javascriptengine.ui.theme.LearnAndroidTheme
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.encodeToJsonElement

/**
 * Demo Activity showcasing JavaScript & Android Bridge functionality
 * Demonstrates bidirectional communication between JavaScript and Android
 */
class JSBridgeDemoActivity : ComponentActivity() {

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
                    JSBridgeDemo(
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

        // Register custom functions
        registerCustomFunctions()

        // Load demo HTML
        loadDemoContent()
    }

    private fun registerCustomFunctions() {
        // Custom greeting function
        webViewBridge.registerFunction("customGreet") { params ->
            val name = params.getString("name", "Anonymous")
            val message = "Hello $name from Android! 🤖"
            BridgeResult.success(Json.encodeToJsonElement(message))
        }

        // Math operations
        webViewBridge.registerFunction("mathOperation") { params ->
            val a = params.getDouble("a", 0.0)
            val b = params.getDouble("b", 0.0)
            val operation = params.getString("operation", "add")

            val result = when (operation) {
                "add" -> a + b
                "subtract" -> a - b
                "multiply" -> a * b
                "divide" -> if (b != 0.0) a / b else Double.NaN
                "power" -> Math.pow(a, b)
                else -> 0.0
            }

            // Return simple data structure that can be serialized
            val resultData = """{"operation":"$operation","operands":[$a,$b],"result":$result}"""
            BridgeResult.success(kotlinx.serialization.json.Json.parseToJsonElement(resultData))
        }

        // Async counter function
        webViewBridge.registerAsyncFunction("asyncCounter") { params, callback ->
            val start = params.getInt("start", 0)
            val end = params.getInt("end", 10)
            val delay = params.getLong("delay", 100L)

            lifecycleScope.launch {
                val results = mutableListOf<Int>()
                for (i in start..end) {
                    results.add(i)
                    kotlinx.coroutines.delay(delay)

                    // Emit progress event with simple data
                    val progressData =
                        """{"current":$i,"total":$end,"progress":${((i - start).toDouble() / (end - start) * 100).toInt()}}"""
                    webViewBridge.emitEvent(
                        "counterProgress",
                        kotlinx.serialization.json.Json.parseToJsonElement(progressData)
                    )
                }

                // Return simple result
                val resultData = """{"results":[${results.joinToString(",")}],"completed":true}"""
                callback.onSuccess(kotlinx.serialization.json.Json.parseToJsonElement(resultData))
            }
        }

        // System information function
        webViewBridge.registerFunction("getSystemInfo") { params ->
            val systemInfo = buildString {
                append("{")
                append("\"timestamp\":${System.currentTimeMillis()},")
                append("\"availableMemory\":${Runtime.getRuntime().maxMemory()},")
                append(
                    "\"usedMemory\":${
                        Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()
                    },"
                )
                append("\"processors\":${Runtime.getRuntime().availableProcessors()},")
                append("\"androidVersion\":\"${android.os.Build.VERSION.RELEASE}\",")
                append("\"deviceModel\":\"${android.os.Build.MODEL}\",")
                append("\"manufacturer\":\"${android.os.Build.MANUFACTURER}\"")
                append("}")
            }

            BridgeResult.success(kotlinx.serialization.json.Json.parseToJsonElement(systemInfo))
        }
    }

    private fun loadDemoContent() {
        val demoHtml = """
            <div style="font-family: Arial, sans-serif; padding: 20px; max-width: 800px; margin: 0 auto;">
                <h1 style="color: #2196F3; text-align: center;">🌉 JavaScript & Android Bridge Demo</h1>
                
                <div style="background: #f5f5f5; padding: 15px; border-radius: 8px; margin: 20px 0;">
                    <h3>🔗 Bridge Status</h3>
                    <p id="bridgeStatus">Initializing...</p>
                </div>

                <div style="background: #e8f5e8; padding: 15px; border-radius: 8px; margin: 20px 0;">
                    <h3>📱 Device Info</h3>
                    <button onclick="testDeviceInfo()" style="padding: 10px 20px; margin: 5px; background: #4CAF50; color: white; border: none; border-radius: 4px;">Get Device Info</button>
                    <div id="deviceInfo"></div>
                </div>

                <div style="background: #fff3e0; padding: 15px; border-radius: 8px; margin: 20px 0;">
                    <h3>💬 Toast Messages</h3>
                    <button onclick="showSimpleToast()" style="padding: 10px 20px; margin: 5px; background: #FF9800; color: white; border: none; border-radius: 4px;">Simple Toast</button>
                    <button onclick="showLongToast()" style="padding: 10px 20px; margin: 5px; background: #FF9800; color: white; border: none; border-radius: 4px;">Long Toast</button>
                </div>

                <div style="background: #f3e5f5; padding: 15px; border-radius: 8px; margin: 20px 0;">
                    <h3>🧮 Math Operations</h3>
                    <input type="number" id="mathA" value="10" style="width: 80px; padding: 5px; margin: 2px;">
                    <select id="mathOp" style="padding: 5px; margin: 2px;">
                        <option value="add">+</option>
                        <option value="subtract">-</option>
                        <option value="multiply">×</option>
                        <option value="divide">÷</option>
                        <option value="power">^</option>
                    </select>
                    <input type="number" id="mathB" value="5" style="width: 80px; padding: 5px; margin: 2px;">
                    <button onclick="performMath()" style="padding: 5px 15px; margin: 2px; background: #9C27B0; color: white; border: none; border-radius: 4px;">Calculate</button>
                    <div id="mathResult"></div>
                </div>

                <div style="background: #e1f5fe; padding: 15px; border-radius: 8px; margin: 20px 0;">
                    <h3>⏱️ Async Counter</h3>
                    <label>Start: <input type="number" id="counterStart" value="1" style="width: 60px; padding: 5px;"></label>
                    <label>End: <input type="number" id="counterEnd" value="10" style="width: 60px; padding: 5px;"></label>
                    <label>Delay (ms): <input type="number" id="counterDelay" value="200" style="width: 80px; padding: 5px;"></label>
                    <button onclick="startAsyncCounter()" style="padding: 10px 20px; margin: 5px; background: #2196F3; color: white; border: none; border-radius: 4px;">Start Counter</button>
                    <div id="counterProgress"></div>
                    <div id="counterResult"></div>
                </div>

                <div style="background: #ffebee; padding: 15px; border-radius: 8px; margin: 20px 0;">
                    <h3>💾 Storage Operations</h3>
                    <input type="text" id="storageKey" placeholder="Key" style="padding: 8px; margin: 2px; width: 120px;">
                    <input type="text" id="storageValue" placeholder="Value" style="padding: 8px; margin: 2px; width: 120px;">
                    <button onclick="savePreference()" style="padding: 8px 15px; margin: 2px; background: #F44336; color: white; border: none; border-radius: 4px;">Save</button>
                    <button onclick="loadPreference()" style="padding: 8px 15px; margin: 2px; background: #F44336; color: white; border: none; border-radius: 4px;">Load</button>
                    <div id="storageResult"></div>
                </div>

                <div style="background: #f1f8e9; padding: 15px; border-radius: 8px; margin: 20px 0;">
                    <h3>📊 System Information</h3>
                    <button onclick="getSystemInfo()" style="padding: 10px 20px; margin: 5px; background: #8BC34A; color: white; border: none; border-radius: 4px;">Get System Info</button>
                    <div id="systemInfo"></div>
                </div>

                <div style="background: #fafafa; padding: 15px; border-radius: 8px; margin: 20px 0;">
                    <h3>📝 Event Log</h3>
                    <button onclick="clearLog()" style="padding: 8px 15px; margin: 5px; background: #666; color: white; border: none; border-radius: 4px;">Clear Log</button>
                    <div id="eventLog" style="background: #fff; padding: 10px; border: 1px solid #ddd; border-radius: 4px; height: 200px; overflow-y: auto; font-family: monospace; font-size: 12px;"></div>
                </div>

                <script>
                    // Bridge readiness check
                    function checkBridgeStatus() {
                        if (window.AndroidBridge) {
                            document.getElementById('bridgeStatus').innerHTML = '✅ Bridge is ready and operational!';
                            logEvent('Bridge initialized successfully');
                        } else {
                            document.getElementById('bridgeStatus').innerHTML = '❌ Bridge not available';
                            setTimeout(checkBridgeStatus, 100);
                        }
                    }

                    // Event logging
                    function logEvent(message) {
                        const log = document.getElementById('eventLog');
                        const timestamp = new Date().toLocaleTimeString();
                        log.innerHTML += "[" + timestamp + "] " + message + "<br>";
                        log.scrollTop = log.scrollHeight;
                    }

                    function clearLog() {
                        document.getElementById('eventLog').innerHTML = '';
                    }

                    // Device info test
                    function testDeviceInfo() {
                        try {
                            const result = AndroidBridge.getDeviceInfo();
                            if (result.success) {
                                const info = result.data;
                                document.getElementById('deviceInfo').innerHTML =
                                    "<strong>Model:</strong> " + info.manufacturer + " " + info.model + "<br>" +
                                    "<strong>Android:</strong> " + info.version + " (SDK " + info.sdk + ")<br>" +
                                    "<strong>Package:</strong> " + info.packageName;
                                logEvent('Device info retrieved successfully');
                            } else {
                                logEvent('Error getting device info: ' + result.error);
                            }
                        } catch (e) {
                            logEvent('Exception getting device info: ' + e.message);
                        }
                    }

                    // Toast functions
                    function showSimpleToast() {
                        const result = AndroidBridge.showToast('Hello from JavaScript! 👋');
                        logEvent('Simple toast shown: ' + JSON.stringify(result));
                    }

                    function showLongToast() {
                        const result = AndroidBridge.showToast('This is a long toast message from JavaScript! 🚀', true);
                        logEvent('Long toast shown: ' + JSON.stringify(result));
                    }

                    // Math operations
                    function performMath() {
                        const a = parseFloat(document.getElementById('mathA').value);
                        const b = parseFloat(document.getElementById('mathB').value);
                        const operation = document.getElementById('mathOp').value;

                        try {
                            const result = AndroidBridge.call('mathOperation', {
                                a: a,
                                b: b,
                                operation: operation
                            });

                            if (result.success) {
                                const data = result.data;
                                document.getElementById('mathResult').innerHTML =
                                    "<strong>Result:</strong> " +
                                    data.operands[0] + " " + getOperatorSymbol(data.operation) +
                                    " " + data.operands[1] + " = " + data.result;
                                logEvent("Math operation: " + data.operation + "(" +
                                    data.operands.join(", ") + ") = " + data.result);
                            } else {
                                logEvent('Math operation error: ' + result.error);
                            }
                        } catch (e) {
                            logEvent('Math operation exception: ' + e.message);
                        }
                    }

                    function getOperatorSymbol(op) {
                        const symbols = { add: '+', subtract: '-', multiply: '×', divide: '÷', power: '^' };
                        return symbols[op] || op;
                    }

                    // Async counter
                    function startAsyncCounter() {
                        const start = parseInt(document.getElementById('counterStart').value);
                        const end = parseInt(document.getElementById('counterEnd').value);
                        const delay = parseInt(document.getElementById('counterDelay').value);

                        document.getElementById('counterProgress').innerHTML = 'Starting counter...';
                        document.getElementById('counterResult').innerHTML = '';

                        // Listen for progress events
                        AndroidBridge.addEventListener('counterProgress', function(data) {
                            document.getElementById('counterProgress').innerHTML =
                                "Progress: " + data.current + "/" + data.total + " (" + data.progress + "%)";
                        });

                        AndroidBridge.callAsync('asyncCounter', {
                            start: start,
                            end: end,
                            delay: delay
                        }).then(result => {
                            document.getElementById('counterResult').innerHTML =
                                "<strong>Completed!</strong><br>" +
                                "Results: [" + result.results.join(", ") + "]";
                            logEvent("Async counter completed: " + result.results.length + " numbers");
                        }).catch(error => {
                            logEvent('Async counter error: ' + error.message);
                        });

                        logEvent("Started async counter: " + start + " to " + end + " with " + delay + "ms delay");
                    }

                    // Storage operations
                    function savePreference() {
                        const key = document.getElementById('storageKey').value;
                        const value = document.getElementById('storageValue').value;

                        if (key) {
                            const result = AndroidBridge.setPreference(key, value);
                            document.getElementById('storageResult').innerHTML =
                                result.success ? "✅ Saved: " + key + " = " + value : "❌ Error: " + result.error;
                            logEvent("Preference saved: " + key + " = " + value);
                        } else {
                            alert('Please enter a key');
                        }
                    }

                    function loadPreference() {
                        const key = document.getElementById('storageKey').value;

                        if (key) {
                            const result = AndroidBridge.getPreference(key, 'Not found');
                            document.getElementById('storageResult').innerHTML = "📖 " + key + " = " + result.data;
                            logEvent("Preference loaded: " + key + " = " + result.data);
                        } else {
                            alert('Please enter a key');
                        }
                    }

                    // System information
                    function getSystemInfo() {
                        try {
                            const result = AndroidBridge.call('getSystemInfo');
                            if (result.success) {
                                const info = result.data;
                                document.getElementById('systemInfo').innerHTML =
                                    "<strong>Timestamp:</strong> " + (new Date(info.timestamp)).toLocaleString() + "<br>" +
                                    "<strong>Available Memory:</strong> " + (info.availableMemory / 1024 / 1024).toFixed(1) + " MB<br>" +
                                    "<strong>Used Memory:</strong> " + (info.usedMemory / 1024 / 1024).toFixed(1) + " MB<br>" +
                                    "<strong>CPU Cores:</strong> " + info.processors + "<br>" +
                                    "<strong>Device:</strong> " + info.manufacturer + " " + info.deviceModel + "<br>" +
                                    "<strong>Android:</strong> " + info.androidVersion;
                                logEvent('System info retrieved');
                            } else {
                                logEvent('System info error: ' + result.error);
                            }
                        } catch (e) {
                            logEvent('System info exception: ' + e.message);
                        }
                    }

                    // Initialize when page loads
                    window.addEventListener('load', function() {
                        logEvent('Page loaded, checking bridge status...');
                        checkBridgeStatus();
                    });

                    // Log bridge initialization
                    if (window.AndroidBridge) {
                        logEvent('AndroidBridge found at page load');
                    }
                </script>
            </div>
        """.trimIndent()

        webViewBridge.loadHtmlWithBridge(demoHtml)
    }

    override fun onDestroy() {
        super.onDestroy()
        webViewBridge.cleanup()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JSBridgeDemo(
    webViewBridge: WebViewBridge,
    webView: WebView,
    modifier: Modifier = Modifier
) {
    var eventLog by remember { mutableStateOf(listOf<String>()) }
    val context = LocalContext.current

    // Add event listener for bridge events
    LaunchedEffect(Unit) {
        val listener = object : BridgeEventListener {
            override fun onEvent(event: BridgeEvent) {
                val timestamp =
                    java.text.SimpleDateFormat("HH:mm:ss", java.util.Locale.getDefault())
                        .format(java.util.Date(event.timestamp))
                eventLog = eventLog + "[$timestamp] Event: ${event.name} - ${event.data}"
            }
        }

        webViewBridge.addEventListener("counterProgress", listener)
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

        // Android-side event log
        if (eventLog.isNotEmpty()) {
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
                            text = "📱 Android Event Log",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        TextButton(
                            onClick = { eventLog = emptyList() }
                        ) {
                            Text("Clear")
                        }
                    }

                    LazyColumn(
                        modifier = Modifier.height(120.dp),
                        reverseLayout = true
                    ) {
                        items(eventLog.reversed()) { log ->
                            Text(
                                text = log,
                                style = MaterialTheme.typography.bodySmall,
                                fontFamily = FontFamily.Monospace,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}