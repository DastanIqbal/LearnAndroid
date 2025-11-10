package com.dastanapps.javascriptengine.integration

import android.content.Context
import android.util.Log
import com.eclipsesource.v8.V8
import com.eclipsesource.v8.V8Object
import com.eclipsesource.v8.V8Array
import kotlinx.coroutines.*
import kotlinx.serialization.json.*
import java.util.concurrent.ConcurrentHashMap
import java.io.IOException

/**
 * Clean integration layer between JavaScript Engine and JSONata
 * This class uses JSONata.js directly in V8 engine instead of Java libraries
 * keeping everything separate from existing implementations
 */
class JSJSONataIntegration private constructor(private val context: Context) {
    
    companion object {
        private const val TAG = "JSJSONataIntegration"
        private var instance: JSJSONataIntegration? = null
        
        /**
         * Get singleton instance of the integration
         */
        fun getInstance(context: Context): JSJSONataIntegration {
            return instance ?: synchronized(this) {
                instance ?: JSJSONataIntegration(context.applicationContext).also { instance = it }
            }
        }
    }

    // JavaScript engine instance - using V8 for separate execution context
    private var jsEngine: V8? = null
    
    // Cache for compiled JSONata expressions
    private val expressionCache = ConcurrentHashMap<String, String>()

    // Execution scope for coroutines - use Main dispatcher for V8 thread safety
    private val integrationScope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    
    // Configuration for the integration
    data class IntegrationConfig(
        val enableCache: Boolean = true,
        val maxCacheSize: Int = 100,
        val timeoutMs: Long = 30000,
        val enableLogging: Boolean = true
    )
    
    private var config = IntegrationConfig()
    private var testData: String? = null
    private var testScript: String? = null
    
    /**
     * Initialize the integration with custom configuration
     */
    suspend fun initialize(config: IntegrationConfig = IntegrationConfig()): Boolean {
        return withContext(Dispatchers.Main) {
            try {
                this@JSJSONataIntegration.config = config

                // Initialize JavaScript engine on Main thread
                initializeJSEngine()

                // Load JSONata.js library
                loadJSONataLibrary()

                // Load test assets
                loadTestAssets()

                logInfo("JSJSONataIntegration initialized successfully with V8 JSONata")
                true
            } catch (e: Exception) {
                logError("Failed to initialize JSJSONataIntegration", e)
                false
            }
        }
    }
    
    /**
     * Execute JavaScript code and then apply JSONata transformation
     */
    suspend fun executeJSWithJSONataTransform(
        jsCode: String,
        jsonataExpression: String,
        inputData: String? = null
    ): IntegrationResult {
        return withContext(Dispatchers.Main) {
            try {
                logInfo("Starting JS execution with JSONata transform")
                
                // Step 1: Execute JavaScript code
                val jsResult = executeJavaScript(jsCode, inputData)
                if (!jsResult.success) {
                    return@withContext IntegrationResult(
                        success = false,
                        error = "JavaScript execution failed: ${jsResult.error}",
                        jsResult = jsResult,
                        jsonataResult = null
                    )
                }
                
                // Step 2: Apply JSONata transformation to JS result
                val jsonataResult = applyJSONataTransformation(
                    jsonataExpression, 
                    jsResult.result ?: "{}"
                )
                
                IntegrationResult(
                    success = jsonataResult.success,
                    result = jsonataResult.result,
                    error = jsonataResult.error,
                    jsResult = jsResult,
                    jsonataResult = jsonataResult
                )
                
            } catch (e: Exception) {
                logError("Error in executeJSWithJSONataTransform", e)
                IntegrationResult(
                    success = false,
                    error = "Integration error: ${e.message}",
                    jsResult = null,
                    jsonataResult = null
                )
            }
        }
    }
    
    /**
     * Apply JSONata transformation to data and then execute JavaScript on result
     */
    suspend fun executeJSONataWithJSProcessing(
        jsonataExpression: String,
        inputData: String,
        jsCode: String
    ): IntegrationResult {
        return withContext(Dispatchers.Main) {
            try {
                logInfo("Starting JSONata transformation with JS processing")
                
                // Step 1: Apply JSONata transformation
                val jsonataResult = applyJSONataTransformation(jsonataExpression, inputData)
                if (!jsonataResult.success) {
                    return@withContext IntegrationResult(
                        success = false,
                        error = "JSONata transformation failed: ${jsonataResult.error}",
                        jsResult = null,
                        jsonataResult = jsonataResult
                    )
                }
                
                // Step 2: Execute JavaScript on JSONata result
                val jsResult = executeJavaScript(jsCode, jsonataResult.result)
                
                IntegrationResult(
                    success = jsResult.success,
                    result = jsResult.result,
                    error = jsResult.error,
                    jsResult = jsResult,
                    jsonataResult = jsonataResult
                )
                
            } catch (e: Exception) {
                logError("Error in executeJSONataWithJSProcessing", e)
                IntegrationResult(
                    success = false,
                    error = "Integration error: ${e.message}",
                    jsResult = null,
                    jsonataResult = null
                )
            }
        }
    }
    
    /**
     * Execute a pipeline of alternating JS and JSONata operations
     */
    suspend fun executePipeline(operations: List<PipelineOperation>): IntegrationResult {
        return withContext(Dispatchers.Main) {
            try {
                logInfo("Starting pipeline execution with ${operations.size} operations")
                
                var currentData = "{}"
                val results = mutableListOf<Any>()
                
                for ((index, operation) in operations.withIndex()) {
                    logInfo("Executing pipeline operation $index: ${operation.type}")
                    
                    val result = when (operation.type) {
                        PipelineOperationType.JAVASCRIPT -> {
                            executeJavaScript(operation.code, currentData)
                        }
                        PipelineOperationType.JSONATA -> {
                            applyJSONataTransformation(operation.code, currentData)
                        }
                    }
                    
                    if (!result.success) {
                        return@withContext IntegrationResult(
                            success = false,
                            error = "Pipeline failed at operation $index: ${result.error}",
                            result = null,
                            jsResult = null,
                            jsonataResult = null,
                            pipelineResults = results
                        )
                    }
                    
                    currentData = result.result ?: "{}"
                    results.add(result)
                }
                
                IntegrationResult(
                    success = true,
                    result = currentData,
                    error = null,
                    jsResult = null,
                    jsonataResult = null,
                    pipelineResults = results
                )
                
            } catch (e: Exception) {
                logError("Error in executePipeline", e)
                IntegrationResult(
                    success = false,
                    error = "Pipeline error: ${e.message}",
                    jsResult = null,
                    jsonataResult = null
                )
            }
        }
    }
    
    /**
     * Initialize JavaScript engine in separate context
     */
    private fun initializeJSEngine() {
        try {
            // Create new V8 instance for isolated execution using proper factory method
            jsEngine = V8.createV8Runtime()
            
            // Add utility functions for JSONata integration
            addJSUtilityFunctions()
            
            logInfo("JavaScript engine initialized")
        } catch (e: Exception) {
            logError("Failed to initialize JavaScript engine", e)
            throw e
        }
    }
    
    /**
     * Load JSONata.js library into V8 engine
     */
    private fun loadJSONataLibrary() {
        try {
            val jsonataCode =
                context.assets.open("js/jsonata.js").bufferedReader().use { it.readText() }
            jsEngine?.executeScript(jsonataCode)
            logInfo("JSONata library loaded into V8 engine")
        } catch (e: IOException) {
            logError("Failed to load JSONata library", e)
            throw e
        }
    }
    
    /**
     * Load test assets (data and scripts)
     */
    private fun loadTestAssets() {
        try {
            // Load test data from js-ex.json
            testData = context.assets.open("jsonata-ex.json").bufferedReader().use { it.readText() }

            // Load test script from js-ex-script.js
            testScript =
                context.assets.open("jsonata-ex-script.js").bufferedReader().use { it.readText() }

            logInfo("Test assets loaded successfully from js-ex files")
        } catch (e: IOException) {
            logError("Failed to load test assets from js-ex files", e)
            // Non-critical, don't throw
        }
    }

    /**
     * Add utility functions to JavaScript engine for integration
     */
    private fun addJSUtilityFunctions() {
        jsEngine?.let { engine ->
            // Add JSON parsing utilities
            engine.executeScript(
                """
                var IntegrationUtils = {
                    parseJSON: function(jsonString) {
                        try {
                            return JSON.parse(jsonString);
                        } catch (e) {
                            return { error: 'Invalid JSON: ' + e.message };
                        }
                    },
                    
                    stringifyJSON: function(obj) {
                        try {
                            return JSON.stringify(obj, null, 2);
                        } catch (e) {
                            return '{"error": "Failed to stringify: ' + e.message + '"}';
                        }
                    },
                    
                    mergeObjects: function(obj1, obj2) {
                        return Object.assign({}, obj1, obj2);
                    },
                    
                    getTimestamp: function() {
                        return new Date().toISOString();
                    }
                };
                """.trimIndent()
            )
        }
    }
    
    /**
     * Execute JavaScript code with optional input data
     */
    private suspend fun executeJavaScript(jsCode: String, inputData: String?): ExecutionResult {
        return withTimeout(config.timeoutMs) {
            try {
                ensureMainThread()
                jsEngine?.let { engine ->
                    // Inject input data if provided
                    inputData?.let { data ->
                        engine.executeScript("var inputData = '$data';")
                        engine.executeScript("var parsedInput = IntegrationUtils.parseJSON(inputData);")
                    }
                    
                    // Execute the JavaScript code
                    val result = engine.executeScript(jsCode)
                    
                    // Convert result to string
                    val resultString = when (result) {
                        is String -> result
                        is Number -> result.toString()
                        is Boolean -> result.toString()
                        is V8Object -> {
                            try {
                                result.toString()
                            } finally {
                                result.close()
                            }
                        }

                        is V8Array -> {
                            try {
                                result.toString()
                            } finally {
                                result.close()
                            }
                        }
                        else -> result?.toString() ?: "null"
                    }
                    
                    ExecutionResult(
                        success = true,
                        result = resultString,
                        error = null,
                        executionTimeMs = System.currentTimeMillis()
                    )
                } ?: ExecutionResult(
                    success = false,
                    result = null,
                    error = "JavaScript engine not initialized",
                    executionTimeMs = 0
                )
                
            } catch (e: Exception) {
                logError("JavaScript execution failed", e)
                ExecutionResult(
                    success = false,
                    result = null,
                    error = e.message,
                    executionTimeMs = 0
                )
            }
        }
    }
    
    /**
     * Apply JSONata transformation using V8 engine directly - must run on Main thread
     */
    private suspend fun applyJSONataTransformation(
        expression: String, 
        inputData: String
    ): ExecutionResult {
        return withTimeout(config.timeoutMs) {
            try {
                ensureMainThread()
                jsEngine?.let { engine ->
                    // Set input data in V8 context
                    engine.executeScript("var inputData = $inputData;")

                    // Properly escape the JSONata expression for JavaScript
                    val escapedExpression = expression
                        .replace("\\", "\\\\")  // Escape backslashes first
                        .replace("'", "\\'")    // Escape single quotes
                        .replace("\"", "\\\"")  // Escape double quotes
                        .replace("\n", "\\n")   // Escape newlines
                        .replace("\r", "\\r")   // Escape carriage returns
                        .replace("\t", "\\t")   // Escape tabs

                    // Create JSONata instance and evaluate
                    val jsCode = """
                        try {
                            var js = new JSONata('$escapedExpression');
                            var result = js.evaluate(inputData);
                            JSON.stringify(result);
                        } catch (error) {
                            JSON.stringify({ error: error.message });
                        }
                    """.trimIndent()

                    val result = engine.executeScript(jsCode) as String

                    // Check if result contains error
                    if (result.contains("\"error\"")) {
                        val errorObj = Json.parseToJsonElement(result).jsonObject
                        ExecutionResult(
                            success = false,
                            result = null,
                            error = errorObj["error"]?.jsonPrimitive?.content
                                ?: "Unknown JSONata error",
                            executionTimeMs = System.currentTimeMillis()
                        )
                    } else {
                        ExecutionResult(
                            success = true,
                            result = result,
                            error = null,
                            executionTimeMs = System.currentTimeMillis()
                        )
                    }
                } ?: ExecutionResult(
                    success = false,
                    result = null,
                    error = "JavaScript engine not initialized",
                    executionTimeMs = 0
                )
                
            } catch (e: Exception) {
                logError("JSONata transformation failed", e)
                ExecutionResult(
                    success = false,
                    result = null,
                    error = "Transformation error: ${e.message}",
                    executionTimeMs = 0
                )
            }
        }
    }
    
    /**
     * Run JSONata tests using test assets - must run on Main thread
     */
    suspend fun runJSONataTests(): IntegrationResult {
        return withContext(Dispatchers.Main) {
            try {
                if (testData == null || testScript == null) {
                    return@withContext IntegrationResult(
                        success = false,
                        error = "Test assets not loaded"
                    )
                }

                ensureMainThread()
                jsEngine?.let { engine ->
                    // Load test script into engine
                    engine.executeScript(testScript!!)

                    // Set test data
                    engine.executeScript("var testData = $testData;")

                    // Run tests
                    val result =
                        engine.executeScript("JSON.stringify(runJSONataTests(testData));") as String

                    IntegrationResult(
                        success = true,
                        result = result,
                        error = null
                    )
                } ?: IntegrationResult(
                    success = false,
                    error = "JavaScript engine not initialized"
                )

            } catch (e: Exception) {
                logError("JSONata tests failed", e)
                IntegrationResult(
                    success = false,
                    error = "Test execution error: ${e.message}"
                )
            }
        }
    }

    /**
     * Ensure we're running on the Main thread for V8 operations
     */
    private fun ensureMainThread() {
        if (!isMainThread()) {
            throw IllegalStateException("V8 operations must run on Main thread")
        }
    }

    /**
     * Check if current thread is Main thread
     */
    private fun isMainThread(): Boolean {
        return android.os.Looper.myLooper() == android.os.Looper.getMainLooper()
    }

    /**
     * Clean up resources
     */
    fun cleanup() {
        try {
            jsEngine?.close()
            jsEngine = null
            
            expressionCache.clear()
            integrationScope.cancel()
            
            logInfo("JSJSONataIntegration cleaned up")
        } catch (e: Exception) {
            logError("Error during cleanup", e)
        }
    }
    
    /**
     * Get integration statistics
     */
    fun getStatistics(): IntegrationStats {
        return IntegrationStats(
            cacheSize = expressionCache.size,
            maxCacheSize = config.maxCacheSize,
            cacheEnabled = config.enableCache,
            isInitialized = (jsEngine != null)
        )
    }
    
    // Logging utilities
    private fun logInfo(message: String) {
        if (config.enableLogging) {
            Log.i(TAG, message)
        }
    }
    
    private fun logError(message: String, throwable: Throwable? = null) {
        if (config.enableLogging) {
            Log.e(TAG, message, throwable)
        }
    }
}

// Data classes for the integration
data class IntegrationResult(
    val success: Boolean,
    val result: String? = null,
    val error: String? = null,
    val jsResult: ExecutionResult? = null,
    val jsonataResult: ExecutionResult? = null,
    val pipelineResults: List<Any>? = null
)

data class ExecutionResult(
    val success: Boolean,
    val result: String? = null,
    val error: String? = null,
    val executionTimeMs: Long
)

data class PipelineOperation(
    val type: PipelineOperationType,
    val code: String,
    val description: String = ""
)

enum class PipelineOperationType {
    JAVASCRIPT,
    JSONATA
}

data class IntegrationStats(
    val cacheSize: Int,
    val maxCacheSize: Int,
    val cacheEnabled: Boolean,
    val isInitialized: Boolean
)