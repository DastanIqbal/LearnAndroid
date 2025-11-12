package com.dastanapps.javascriptengine.bridge

import android.app.Application
import android.webkit.WebView
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.encodeToJsonElement

data class BridgeEventLog(
    val timestamp: Long,
    val message: String
)

data class JSBridgeState(
    val isInitialized: Boolean = false,
    val eventLogs: List<BridgeEventLog> = emptyList(),
    val error: String? = null
)

class JSBridgeDemoViewModel(application: Application) : AndroidViewModel(application) {

    private val _state = MutableStateFlow(JSBridgeState())
    val state: StateFlow<JSBridgeState> = _state.asStateFlow()

    private var webViewBridge: WebViewBridge? = null
    private var webView: WebView? = null

    fun initializeWebView(webView: WebView) {
        this.webView = webView
        this.webViewBridge = WebViewBridge(getApplication(), webView)

        viewModelScope.launch {
            try {
                initializeBridge()
                _state.value = _state.value.copy(isInitialized = true, error = null)
                addEventLog("WebView bridge initialized successfully")
            } catch (e: Exception) {
                _state.value = _state.value.copy(error = e.message)
                addEventLog("Failed to initialize bridge: ${e.message}")
            }
        }
    }

    private fun initializeBridge() {
        webViewBridge?.let { bridge ->
            // Initialize the bridge
            bridge.initialize()

            // Register custom functions
            registerCustomFunctions(bridge)

            // Load demo content
            loadDemoContent(bridge)

            // Set up event listener
            setupEventListener(bridge)
        }
    }

    private fun setupEventListener(bridge: WebViewBridge) {
        val listener = object : BridgeEventListener {
            override fun onEvent(event: BridgeEvent) {
                addEventLog("Event: ${event.name} - ${event.data}")
            }
        }

        bridge.addEventListener("counterProgress", listener)
    }

    private fun registerCustomFunctions(bridge: WebViewBridge) {
        // JSONata transformation function
        bridge.registerFunction("jsonataTransform") { params ->
            val expression = params.getString("expression", "")
            val dataJson = params.getString("data", "{}")

            if (expression.isBlank()) {
                BridgeResult.error("Expression cannot be empty")
            } else {
                try {
                    // In a real implementation, you would use the JSONata library here
                    // For demo purposes, we'll simulate some common transformations
                    val result = when {
                        expression.contains("users.name") -> """["Alice Johnson", "Bob Smith", "Carol Williams"]"""
                        expression.contains("users[age >= 30]") -> """[{"name": "Alice Johnson", "age": 32}, {"name": "Carol Williams", "age": 28}]"""
                        expression.contains("\$count") -> "15"
                        expression.contains("price * quantity") -> "125.50"
                        else -> """{"message": "Simulated JSONata result", "expression": "$expression"}"""
                    }

                    val resultData =
                        """{"success": true, "result": $result, "expression": "$expression"}"""
                    BridgeResult.success(
                        kotlinx.serialization.json.Json.parseToJsonElement(
                            resultData
                        )
                    )
                } catch (e: Exception) {
                    BridgeResult.error("JSONata transformation failed: ${e.message}")
                }
            }
        }

        // Custom greeting function
        bridge.registerFunction("customGreet") { params ->
            val name = params.getString("name", "Anonymous")
            val message = "Hello $name from Android! 🤖"
            BridgeResult.success(Json.encodeToJsonElement(message))
        }

        // Math operations
        bridge.registerFunction("mathOperation") { params ->
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
        bridge.registerAsyncFunction("asyncCounter") { params, callback ->
            val start = params.getInt("start", 0)
            val end = params.getInt("end", 10)
            val delay = params.getLong("delay", 100L)

            viewModelScope.launch {
                val results = mutableListOf<Int>()
                for (i in start..end) {
                    results.add(i)
                    kotlinx.coroutines.delay(delay)

                    // Emit progress event with simple data
                    val progressData =
                        """{"current":$i,"total":$end,"progress":${((i - start).toDouble() / (end - start) * 100).toInt()}}"""
                    bridge.emitEvent(
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
        bridge.registerFunction("getSystemInfo") { params ->
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

    private fun loadDemoContent(bridge: WebViewBridge) {
        val demoHtml = """
            <div style="font-family: Arial, sans-serif; padding: 20px; max-width: 800px; margin: 0 auto;">
                <h1 style="color: #2196F3; text-align: center;">🌉 JavaScript & Android Bridge Demo</h1>
                
                <div style="background: #e8f4fd; padding: 15px; border-radius: 8px; margin: 20px 0; border-left: 4px solid #007acc;">
                    <h3>🧩 JSONata Transformation Demo</h3>
                    <p style="margin: 10px 0; color: #666; font-size: 14px;">
                        JSONata is a query and transformation language for JSON data. Try these examples:
                    </p>
                    
                    <div style="margin: 10px 0;">
                        <label style="display: block; margin: 5px 0; font-weight: bold;">Expression:</label>
                        <input type="text" id="jsonataExpression" placeholder="e.g., users.name" 
                               style="padding: 8px; margin: 2px; width: 300px; border: 1px solid #ddd; border-radius: 4px;">
                        <br>
                        <label style="display: block; margin: 5px 0; font-weight: bold;">Data (JSON):</label>
                        <input type="text" id="jsonataData" placeholder='{"users": [{"name": "Alice", "age": 30}]}' 
                               style="padding: 8px; margin: 2px; width: 300px; border: 1px solid #ddd; border-radius: 4px;">
                        <br>
                        <button onclick="testJsonata()" style="padding: 10px 20px; margin: 10px 2px 5px 0; background: #007acc; color: white; border: none; border-radius: 4px; cursor: pointer;">Transform Data</button>
                        <button onclick="loadSampleJsonata()" style="padding: 10px 20px; margin: 10px 2px 5px 0; background: #28a745; color: white; border: none; border-radius: 4px; cursor: pointer;">Load Sample</button>
                    </div>
                    
                    <div style="background: #f8f9fa; padding: 10px; border-radius: 4px; margin: 10px 0;">
                        <strong>Sample Expressions to try:</strong><br>
                        <code style="background: #e9ecef; padding: 2px 4px; margin: 2px; border-radius: 3px;">users.name</code> - Extract all user names<br>
                        <code style="background: #e9ecef; padding: 2px 4px; margin: 2px; border-radius: 3px;">users[age >= 30]</code> - Filter users 30 or older<br>
                        <code style="background: #e9ecef; padding: 2px 4px; margin: 2px; border-radius: 3px;">${'$'}count users</code> - Count number of users<br>
                        <code style="background: #e9ecef; padding: 2px 4px; margin: 2px; border-radius: 3px;">price * quantity</code> - Calculate total price
                    </div>
                    
                    <div id="jsonataResult" style="margin-top: 15px; padding: 10px; background: #fff; border: 1px solid #ddd; border-radius: 4px; min-height: 40px;"></div>
                </div>

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

                    function testJsonata() {
                        const expression = document.getElementById('jsonataExpression').value;
                        const data = document.getElementById('jsonataData').value;

                        try {
                            const result = AndroidBridge.call('jsonataTransform', {
                                expression: expression,
                                data: data
                            });

                            if (result.success) {
                                const transformResult = result.data;
                                document.getElementById('jsonataResult').innerHTML = 
                                    "<strong>✅ Transformation Result:</strong><br>" +
                                    "<pre style='background: #f8f9fa; padding: 10px; border-radius: 4px; margin: 5px 0; overflow-x: auto;'>" + 
                                    JSON.stringify(transformResult.result, null, 2) + "</pre>" +
                                    "<small style='color: #666;'>Expression: " + transformResult.expression + "</small>";
                                logEvent("JSONata transformation successful: " + expression);
                            } else {
                                document.getElementById('jsonataResult').innerHTML = 
                                    "<strong>❌ Error:</strong><br>" + 
                                    "<span style='color: #dc3545;'>" + result.error + "</span>";
                                logEvent("JSONata transformation failed: " + result.error);
                            }
                        } catch (e) {
                            document.getElementById('jsonataResult').innerHTML = 
                                "<strong>❌ Exception:</strong><br>" + 
                                "<span style='color: #dc3545;'>" + e.message + "</span>";
                            logEvent('JSONata transformation exception: ' + e.message);
                        }
                    }

                    function loadSampleJsonata() {
                        document.getElementById('jsonataExpression').value = 'users.name';
                        document.getElementById('jsonataData').value = '{"users": [{"name": "Alice", "age": 30}, {"name": "Bob", "age": 25}]}';
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

        bridge.loadHtmlWithBridge(demoHtml)
    }

    fun clearEventLog() {
        _state.value = _state.value.copy(eventLogs = emptyList())
    }

    private fun addEventLog(message: String) {
        val newLog = BridgeEventLog(
            timestamp = System.currentTimeMillis(),
            message = message
        )
        _state.value = _state.value.copy(
            eventLogs = _state.value.eventLogs + newLog
        )
    }

    override fun onCleared() {
        super.onCleared()
        webViewBridge?.cleanup()
    }
}