package com.dastanapps.javascriptengine.bridge

import android.R.attr.data
import android.app.Application
import android.webkit.WebView
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.encodeToJsonElement
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicInteger
import kotlin.coroutines.resume
import kotlin.random.Random

data class BridgeEventLog(
    val timestamp: Long,
    val message: String
)

data class JSONataTask(
    val id: String,
    val expression: String,
    val data: String,
    val status: TaskStatus = TaskStatus.PENDING,
    val result: String? = null,
    val error: String? = null,
    val executionTime: Long = 0,
    val startTime: Long = System.currentTimeMillis()
)

enum class TaskStatus {
    PENDING, RUNNING, COMPLETED, FAILED
}

data class JSBridgeState(
    val isInitialized: Boolean = false,
    val eventLogs: List<BridgeEventLog> = emptyList(),
    val error: String? = null,
    val jsonataTasks: List<JSONataTask> = emptyList(),
    val activeTaskCount: Int = 0,
    val totalTasksProcessed: Int = 0,
    val webView: WebView? = null
)

class JSBridgeDemoViewModel(application: Application) : AndroidViewModel(application) {

    private val _state = MutableStateFlow(JSBridgeState())
    val state: StateFlow<JSBridgeState> = _state.asStateFlow()

    private var webViewBridge: WebViewBridge? = null
    private var webView: WebView? = null

    // Thread pool for parallel JSONata execution
    private val jsonataExecutor = Executors.newFixedThreadPool(4)
    private val taskIdCounter = AtomicInteger(0)

    init {
        // Initialize WebView and bridge automatically
        initializeWebViewAndBridge()
    }

    private fun initializeWebViewAndBridge() {
        viewModelScope.launch {
            try {
                // Create WebView on main thread
                withContext(Dispatchers.Main) {
                    webView = WebView(getApplication<Application>()).apply {
                        // Configure WebView settings
                        settings.apply {
                            javaScriptEnabled = true
                            domStorageEnabled = true
                            allowFileAccess = true
                            allowContentAccess = true
                        }
                    }

                    // Update state with WebView
                    _state.value = _state.value.copy(webView = webView)
                }

                // Initialize bridge
                webView?.let { view ->
                    webViewBridge = WebViewBridge(getApplication(), view)
                    initializeBridge()
                }

                _state.value = _state.value.copy(isInitialized = true, error = null)
                addEventLog("WebView and bridge initialized successfully in ViewModel")
            } catch (e: Exception) {
                _state.value = _state.value.copy(error = e.message)
                addEventLog("Failed to initialize WebView and bridge: ${e.message}")
            }
        }
    }

    /**
     * Initialize bridge with custom functions and load demo content
     */
    private fun initializeBridge() {
        webViewBridge?.let { bridge ->
            // Initialize the bridge
            bridge.initialize()

            // Register custom functions
            registerCustomFunctions(bridge)

            // Load demo content from separate HTML file
            loadDemoContentFromAssets(bridge)

            // Set up event listener
            setupEventListener(bridge)
        }
    }

    /**
     * Load demo content from separate HTML file in assets
     */
    private fun loadDemoContentFromAssets(bridge: WebViewBridge) {
        try {
            // Load HTML from assets using WebView's loadUrl method
            webView?.loadUrl("file:///android_asset/real-jsonata-demo.html")
            addEventLog("Demo HTML content loaded from assets")
        } catch (e: Exception) {
            addEventLog("Failed to load demo content: ${e.message}")
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
        // Real JSONata transformation function using WebView execution
        bridge.registerFunction("jsonataTransform") { params ->
            val expression = params.getString("expression", "")
            val dataJson = params.getString("data", "{}")
            val parallel = params.getBoolean("parallel", false)

            if (expression.isBlank()) {
                BridgeResult.error("Expression cannot be empty")
            } else {
                try {
                    if (parallel) {
                        // Queue for parallel execution
                        viewModelScope.launch {
                            val tasks = listOf(expression to dataJson)
                            executeParallelJSONata(tasks)
                        }

                        val resultData =
                            """{"success": true, "message": "JSONata task queued for parallel execution", "expression": "$expression"}"""
                        BridgeResult.success(Json.parseToJsonElement(resultData))
                    } else {
                        // Execute directly in WebView - this will be handled by the bridge
                        // The actual execution happens in JavaScript, so we return a placeholder
                        // The real result will be processed by the WebView
                        val resultData =
                            """{"success": true, "message": "JSONata execution delegated to WebView", "expression": "$expression", "data": $dataJson}"""
                        BridgeResult.success(Json.parseToJsonElement(resultData))
                    }
                } catch (e: Exception) {
                    BridgeResult.error("JSONata transformation failed: ${e.message}")
                }
            }
        }

        // Real parallel JSONata batch execution
        bridge.registerFunction("jsonataParallelBatch") { params ->
            try {
                val expressionsJson = params.getString("expressions", "[]")
                val dataJson = params.getString("data", "[]")

                val expressions = parseJsonStringArray(expressionsJson)
                val dataArray = parseJsonStringArray(dataJson)

                if (expressions.size != dataArray.size) {
                    BridgeResult.error("Expressions and data arrays must have the same length")
                } else {
                    val tasks = mutableListOf<Pair<String, String>>()
                    for (i in expressions.indices) {
                        tasks.add(Pair(expressions[i], dataArray[i]))
                    }

                    executeParallelJSONata(tasks)

                    val resultData =
                        """{"success": true, "message": "${tasks.size} JSONata tasks queued for parallel execution"}"""
                    BridgeResult.success(Json.parseToJsonElement(resultData))
                }
            } catch (e: Exception) {
                BridgeResult.error("Parallel batch execution failed: ${e.message}")
            }
        }

        // Execute JSONata directly in WebView
        bridge.registerAsyncFunction("executeJSONataInWebView") { params, callback ->
            val expression = params.getString("expression", "")
            val dataJson = params.getString("data", "{}")
            val taskId = params.getString("taskId", "task_${System.currentTimeMillis()}")

            viewModelScope.launch {
                try {
                    val startTime = System.currentTimeMillis()
                    val escapedExpression = expression.replace("\"", "\\\"").replace("'", "\\'")

                    // Execute JSONata directly in WebView using JavaScript
                    val jsCode = """
                        (function() {
                            try {
                                const data = $dataJson;
                                const expression = jsonata('$escapedExpression');
                                const result = expression.evaluate(data);
                                return JSON.stringify({
                                    success: true,
                                    result: result,
                                    expression: '$escapedExpression',
                                    taskId: '$taskId',
                                    executionTime: ${System.currentTimeMillis() - startTime}
                                });
                            } catch (error) {
                                return JSON.stringify({
                                    success: false,
                                    error: error.message,
                                    expression: '$escapedExpression',
                                    taskId: '$taskId'
                                });
                            }
                        })();
                    """.trimIndent()

                    // Evaluate in WebView
                    withContext(Dispatchers.Main) {
                        webView?.evaluateJavascript(jsCode) { result ->
                            try {
                                val cleanResult =
                                    result?.removeSurrounding("\"")?.replace("\\\"", "\"") ?: "{}"
                                val jsonResult = Json.parseToJsonElement(cleanResult)
                                callback.onSuccess(jsonResult)

                                // Log the execution
                                addEventLog("JSONata executed in WebView: $expression")
                            } catch (e: Exception) {
                                callback.onError("Failed to parse WebView result: ${e.message}")
                                addEventLog("JSONata WebView execution failed: ${e.message}")
                            }
                        }
                    }
                } catch (e: Exception) {
                    callback.onError("JSONata WebView execution error: ${e.message}")
                    addEventLog("JSONata WebView execution error: ${e.message}")
                }
            }
        }

        // Batch execute multiple JSONata expressions in WebView
        bridge.registerAsyncFunction("batchExecuteJSONataInWebView") { params, callback ->
            val expressionsJson = params.getString("expressions", "[]")
            val dataJson = params.getString("data", "[]")

            viewModelScope.launch {
                try {
                    val expressions = parseJsonStringArray(expressionsJson)
                    val dataArray = parseJsonStringArray(dataJson)

                    if (expressions.size != dataArray.size) {
                        callback.onError("Expressions and data arrays must have the same length")
                        return@launch
                    }

                    val startTime = System.currentTimeMillis()

                    // Build escaped expressions array
                    val escapedExpressions =
                        expressions.map { it.replace("\"", "\\\"").replace("'", "\\'") }
                    val expressionsArrayJs =
                        escapedExpressions.joinToString(prefix = "[", postfix = "]") { "\"$it\"" }
                    val dataArrayJs = dataArray.joinToString(prefix = "[", postfix = "]") { it }

                    // Execute all expressions in WebView
                    val jsCode = """
                        (function() {
                            const expressions = $expressionsArrayJs;
                            const dataArray = $dataArrayJs;
                            const results = [];
                            
                            for (let i = 0; i < expressions.length; i++) {
                                try {
                                    const data = dataArray[i];
                                    const expression = jsonata(expressions[i]);
                                    const result = expression.evaluate(data);
                                    results.push({
                                        success: true,
                                        result: result,
                                        expression: expressions[i],
                                        index: i
                                    });
                                } catch (error) {
                                    results.push({
                                        success: false,
                                        error: error.message,
                                        expression: expressions[i],
                                        index: i
                                    });
                                }
                            }
                            
                            return JSON.stringify({
                                success: true,
                                results: results,
                                totalExecutionTime: ${System.currentTimeMillis() - startTime},
                                totalTasks: expressions.length
                            });
                        })();
                    """.trimIndent()

                    withContext(Dispatchers.Main) {
                        webView?.evaluateJavascript(jsCode) { result ->
                            try {
                                val cleanResult =
                                    result?.removeSurrounding("\"")?.replace("\\\"", "\"") ?: "{}"
                                val jsonResult = Json.parseToJsonElement(cleanResult)
                                callback.onSuccess(jsonResult)

                                addEventLog("Batch JSONata executed in WebView: ${expressions.size} expressions")
                            } catch (e: Exception) {
                                callback.onError("Failed to parse batch WebView result: ${e.message}")
                                addEventLog("Batch JSONata WebView execution failed: ${e.message}")
                            }
                        }
                    }
                } catch (e: Exception) {
                    callback.onError("Batch JSONata WebView execution error: ${e.message}")
                    addEventLog("Batch JSONata WebView execution error: ${e.message}")
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

                    val progressData =
                        """{"current":$i,"total":$end,"progress":${((i - start).toDouble() / (end - start) * 100).toInt()}}"""
                    bridge.emitEvent(
                        "counterProgress",
                        kotlinx.serialization.json.Json.parseToJsonElement(progressData)
                    )
                }

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

            BridgeResult.success(Json.parseToJsonElement(systemInfo))
        }

        // Bridge functions for parallel execution demos
        bridge.registerFunction("runJSONataStressTest") { params ->
            runJSONataStressTest()
            val resultData =
                """{"success": true, "message": "JSONata stress test started with 8 parallel tasks"}"""
            BridgeResult.success(Json.parseToJsonElement(resultData))
        }

        bridge.registerFunction("runThreadedJSONataTest") { params ->
            runThreadedJSONataTest()
            val resultData =
                """{"success": true, "message": "Threaded JSONata test started with 4 tasks"}"""
            BridgeResult.success(Json.parseToJsonElement(resultData))
        }

        bridge.registerFunction("clearCompletedTasks") { params ->
            clearCompletedTasks()
            val resultData = """{"success": true, "message": "Completed tasks cleared"}"""
            BridgeResult.success(Json.parseToJsonElement(resultData))
        }

        // Get current parallel execution status
        bridge.registerFunction("getParallelStatus") { params ->
            val statusData = buildString {
                append("{")
                append("\"activeTaskCount\":${_state.value.activeTaskCount},")
                append("\"totalTasksProcessed\":${_state.value.totalTasksProcessed},")
                append("\"pendingTasks\":${_state.value.jsonataTasks.count { it.status == TaskStatus.PENDING }},")
                append("\"runningTasks\":${_state.value.jsonataTasks.count { it.status == TaskStatus.RUNNING }},")
                append("\"completedTasks\":${_state.value.jsonataTasks.count { it.status == TaskStatus.COMPLETED }},")
                append("\"failedTasks\":${_state.value.jsonataTasks.count { it.status == TaskStatus.FAILED }}")
                append("}")
            }
            BridgeResult.success(Json.parseToJsonElement(statusData))
        }

        // Storage operations
        bridge.registerFunction("setPreference") { params ->
            val key = params.getString("key", "")
            val value = params.getString("value", "")

            val resultData = """{"success": true, "message": "Preference saved: $key = $value"}"""
            BridgeResult.success(Json.parseToJsonElement(resultData))
        }

        bridge.registerFunction("getPreference") { params ->
            val key = params.getString("key", "")
            val defaultValue = params.getString("defaultValue", "")

            val resultData = """{"success": true, "data": "$defaultValue"}"""
            BridgeResult.success(Json.parseToJsonElement(resultData))
        }

        // Device info function
        bridge.registerFunction("getDeviceInfo") { params ->
            val deviceInfo = buildString {
                append("{")
                append("\"manufacturer\":\"${android.os.Build.MANUFACTURER}\",")
                append("\"model\":\"${android.os.Build.MODEL}\",")
                append("\"version\":\"${android.os.Build.VERSION.RELEASE}\",")
                append("\"sdk\":${android.os.Build.VERSION.SDK_INT},")
                append("\"packageName\":\"${getApplication<Application>().packageName}\"")
                append("}")
            }
            BridgeResult.success(Json.parseToJsonElement(deviceInfo))
        }

        // Toast function
        bridge.registerFunction("showToast") { params ->
            val message = params.getString("message", "Hello from Bridge!")
            val isLong = params.getBoolean("isLong", false)

            addEventLog("Toast: $message (${if (isLong) "long" else "short"})")

            val resultData = """{"success": true, "message": "Toast shown: $message"}"""
            BridgeResult.success(Json.parseToJsonElement(resultData))
        }
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

    // Execute multiple JSONata scripts in parallel
    fun executeParallelJSONata(tasks: List<Pair<String, String>>) {
        viewModelScope.launch {
            addEventLog("Starting parallel execution of ${tasks.size} JSONata tasks")

            val jsonataTasks = tasks.mapIndexed { index, (expression, data) ->
                JSONataTask(
                    id = "task_${taskIdCounter.incrementAndGet()}",
                    expression = expression,
                    data = data
                )
            }

            // Update state with pending tasks
            _state.value = _state.value.copy(
                jsonataTasks = _state.value.jsonataTasks + jsonataTasks,
                activeTaskCount = jsonataTasks.size
            )

            val completedTasks = jsonataTasks.map { task ->
                async(Dispatchers.IO) {
                    executeJSONataTaskWithWebView(task)
                }
            }.awaitAll()

            _state.value = _state.value.copy(
                jsonataTasks = _state.value.jsonataTasks.map { existingTask ->
                    completedTasks.find { it.id == existingTask.id } ?: existingTask
                },
                activeTaskCount = 0,
                totalTasksProcessed = _state.value.totalTasksProcessed + completedTasks.size
            )

            val successCount = completedTasks.count { it.status == TaskStatus.COMPLETED }
            val failureCount = completedTasks.count { it.status == TaskStatus.FAILED }

            addEventLog("Parallel execution completed: $successCount successful, $failureCount failed")
        }
    }

    // Execute JSONata with threading (using thread pool)
    fun executeJSONataWithThreading(tasks: List<Pair<String, String>>) {
        viewModelScope.launch {
            addEventLog("Starting threaded execution of ${tasks.size} JSONata tasks")

            val jsonataTasks = tasks.mapIndexed { index, (expression, data) ->
                JSONataTask(
                    id = "thread_task_${taskIdCounter.incrementAndGet()}",
                    expression = expression,
                    data = data
                )
            }

            // Update state with pending tasks
            _state.value = _state.value.copy(
                jsonataTasks = _state.value.jsonataTasks + jsonataTasks,
                activeTaskCount = jsonataTasks.size
            )

            val completedTasks = jsonataTasks.map { task ->
                async(Dispatchers.IO) {
                    executeJSONataTaskWithWebView(task)
                }
            }.awaitAll()

            _state.value = _state.value.copy(
                jsonataTasks = _state.value.jsonataTasks.map { existingTask ->
                    completedTasks.find { it.id == existingTask.id } ?: existingTask
                },
                activeTaskCount = 0,
                totalTasksProcessed = _state.value.totalTasksProcessed + completedTasks.size
            )

            val successCount = completedTasks.count { it.status == TaskStatus.COMPLETED }
            val failureCount = completedTasks.count { it.status == TaskStatus.FAILED }

            addEventLog("Threaded execution completed: $successCount successful, $failureCount failed")
        }
    }

    private suspend fun executeJSONataTask(task: JSONataTask): JSONataTask {
        // Deprecated in favor of executeJSONataTaskWithWebView()
        return executeJSONataTaskWithWebView(task)
    }

    private suspend fun executeJSONataTaskOnThread(task: JSONataTask): JSONataTask {
        // Deprecated in favor of executeJSONataTaskWithWebView()
        return executeJSONataTaskWithWebView(task)
    }

    private fun updateTaskStatus(taskId: String, status: TaskStatus) {
        _state.value = _state.value.copy(
            jsonataTasks = _state.value.jsonataTasks.map { task ->
                if (task.id == taskId) {
                    task.copy(status = status)
                } else {
                    task
                }
            }
        )
    }

    private fun simulateJSONataExecution(expression: String, data: String): String {
        // Deprecated, use executeJSONataTaskWithWebView for real execution
        return """ { "message": "JSONata simulated result for: $expression", "processed": false, "timestamp": ${ System.currentTimeMillis() } }"""
    }

    private suspend fun executeJSONataTaskWithWebView(task: JSONataTask): JSONataTask {
        val startTime = System.currentTimeMillis()
        updateTaskStatus(task.id, TaskStatus.RUNNING)
        return suspendCancellableCoroutine { continuation ->
            val expression = task.expression.replace("\"", "\\\"").replace("'", "\\'")
            val dataJson = task.data
            val taskId = task.id

            val jsCode = """
                (function() {
                    try {
                        const data = $dataJson;
                        const expression = jsonata('$expression');
                        const result = expression.evaluate(data);
                        return JSON.stringify({
                            success: true,
                            result: result,
                            expression: '$expression',
                            taskId: '$taskId',
                            executionTime: ${System.currentTimeMillis() - startTime}
                        });
                    } catch (error) {
                        return JSON.stringify({
                            success: false,
                            error: error.message,
                            expression: '$expression',
                            taskId: '$taskId'
                        });
                    }
                })();
            """.trimIndent()

            webView?.post {
                webView?.evaluateJavascript(jsCode) { result ->
                    try {
                        val cleanResult =
                            result?.removeSurrounding("\"")?.replace("\\\"", "\"") ?: "{}"
                        val json = Json.parseToJsonElement(cleanResult).jsonObject
                        val executionTime = System.currentTimeMillis() - startTime

                        if (json["success"]?.jsonPrimitive?.booleanOrNull == true) {
                            val resultText = json["result"]?.toString() ?: "null"
                            continuation.resume(
                                task.copy(
                                    status = TaskStatus.COMPLETED,
                                    result = resultText,
                                    executionTime = executionTime
                                )
                            )
                        } else {
                            val errorMsg =
                                json["error"]?.jsonPrimitive?.contentOrNull ?: "Unknown error"
                            continuation.resume(
                                task.copy(
                                    status = TaskStatus.FAILED,
                                    error = errorMsg,
                                    executionTime = executionTime
                                )
                            )
                        }
                    } catch (e: Exception) {
                        continuation.resume(
                            task.copy(
                                status = TaskStatus.FAILED,
                                error = "Failed to parse result: ${e.message}",
                                executionTime = System.currentTimeMillis() - startTime
                            )
                        )
                    }
                }
            }
        }
    }

    // Clear completed tasks
    fun clearCompletedTasks() {
        _state.value = _state.value.copy(
            jsonataTasks = _state.value.jsonataTasks.filter { it.status == TaskStatus.RUNNING || it.status == TaskStatus.PENDING }
        )
        addEventLog("Cleared completed tasks")
    }

    // Demo functions to test parallel execution
    fun runJSONataStressTest() {
        val testTasks = listOf(
            "users.name" to """{"users": [{"name": "Alice", "age": 30}, {"name": "Bob", "age": 25}]}""",
            "users[age >= 25]" to """{"users": [{"name": "Alice", "age": 30}, {"name": "Bob", "age": 25}, {"name": "Carol", "age": 35}]}""",
            "\$count(users)" to """{"users": [{"name": "Alice"}, {"name": "Bob"}, {"name": "Carol"}]}""",
            "\$sum(employees.salary)" to """{"employees": [{"salary": 50000}, {"salary": 60000}, {"salary": 70000}]}""",
            "\$average(employees.age)" to """{"employees": [{"age": 25}, {"age": 30}, {"age": 35}]}""",
            "employees.department" to """{"employees": [{"department": "Engineering"}, {"department": "Design"}]}""",
            "projects.name" to """{"projects": [{"name": "WebApp"}, {"name": "MobileAPI"}]}""",
            "employees.skills[]" to """{"employees": [{"skills": ["JS", "React"]}, {"skills": ["Java", "Spring"]}]}"""
        )

        executeParallelJSONata(testTasks)
    }

    fun runThreadedJSONataTest() {
        val testTasks = listOf(
            "users.location.city" to """{"users": [{"location": {"city": "NYC"}}, {"location": {"city": "SF"}}]}""",
            "products[price > 100]" to """{"products": [{"price": 150}, {"price": 50}, {"price": 200}]}""",
            "\$distinct(orders.status)" to """{"orders": [{"status": "pending"}, {"status": "completed"}, {"status": "pending"}]}""",
            "customers.{name: name, total: \$sum(orders.amount)}" to """{"customers": [{"name": "John", "orders": [{"amount": 100}]}]}"""
        )

        executeJSONataWithThreading(testTasks)
    }

    override fun onCleared() {
        super.onCleared()
        webViewBridge?.cleanup()
        jsonataExecutor.shutdown()

        // Clean up WebView
        webView?.destroy()
        webView = null

        addEventLog("ViewModel cleared - WebView and resources cleaned up")
    }
}

fun parseJsonStringArray(jsonString: String): List<String> {
    return try {
        Json.decodeFromString<List<String>>(jsonString)
    } catch (e: Exception) {
        listOf()
    }
}