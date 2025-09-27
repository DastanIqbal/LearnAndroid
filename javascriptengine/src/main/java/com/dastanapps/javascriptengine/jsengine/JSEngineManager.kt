package com.dastanapps.javascriptengine.jsengine

import android.content.Context
import android.util.Log
import androidx.annotation.WorkerThread
import com.quickjs.JSArray
import com.quickjs.JSContext
import com.quickjs.JSFunction
import com.quickjs.JSObject
import com.quickjs.JSValue
import com.quickjs.JavaCallback
import com.quickjs.JavaVoidCallback
import com.quickjs.QuickJS
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.Executors

/**
 * JavaScript Engine Manager using QuickJS
 * Provides JavaScript execution without WebView dependency
 *
 * Features:
 * - Execute JavaScript code
 * - Call JavaScript functions
 * - Bind Java/Kotlin objects to JavaScript
 * - Handle JSON data transformation
 * - Memory management for JS objects
 * - Thread-safe operations using single-threaded executor
 */
class JSEngineManager(private val context: Context) {

    private var quickJS: QuickJS? = null
    private var jsContext: JSContext? = null
    private val jsObjects = ConcurrentHashMap<String, JSObject>()
    private val jsFunctions = ConcurrentHashMap<String, JSFunction>()

    // Single-threaded executor to ensure all JS operations run on the same thread
    private val jsExecutor = Executors.newSingleThreadExecutor { r ->
        Thread(r, "JSEngine-Thread").apply {
            isDaemon = true
        }
    }
    private val jsDispatcher = jsExecutor.asCoroutineDispatcher()

    // JSON serializer for data exchange
    private val jsonSerializer = Json {
        prettyPrint = true
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    /**
     * Initialize the JavaScript engine
     */
    suspend fun initialize(): Boolean = withContext(jsDispatcher) {
        try {
            // Create QuickJS runtime
            quickJS = QuickJS.createRuntime()
            jsContext = quickJS?.createContext()

            // Set up global objects and utilities
            setupGlobalObjects()

            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    /**
     * Execute JavaScript code and return result
     */
    @WorkerThread
    suspend fun executeScript(script: String, filename: String = "script.js"): JSExecutionResult =
        withContext(jsDispatcher) {
            try {
                val context = jsContext
                    ?: return@withContext JSExecutionResult.error("Engine not initialized")

                val result = context.executeScript(script, filename)

                JSExecutionResult.success(convertResultToString(result))
            } catch (e: Exception) {
                JSExecutionResult.error("Script execution failed: ${e.message}")
            }
        }

    /**
     * Execute JavaScript code without return value
     */
    @WorkerThread
    suspend fun executeVoidScript(
        script: String,
        filename: String = "script.js"
    ): JSExecutionResult = withContext(jsDispatcher) {
        try {
            val context =
                jsContext ?: return@withContext JSExecutionResult.error("Engine not initialized")

            context.executeVoidScript(script, filename)

            JSExecutionResult.success("Script executed successfully")
        } catch (e: Exception) {
            JSExecutionResult.error("Script execution failed: ${e.message}")
        }
    }

    /**
     * Execute JavaScript code and return integer result
     */
    @WorkerThread
    suspend fun executeIntegerScript(
        script: String,
        filename: String = "script.js"
    ): JSExecutionResult = withContext(jsDispatcher) {
        try {
            val context =
                jsContext ?: return@withContext JSExecutionResult.error("Engine not initialized")

            val result = context.executeIntegerScript(script, filename)

            JSExecutionResult.success(result.toString())
        } catch (e: Exception) {
            JSExecutionResult.error("Script execution failed: ${e.message}")
        }
    }

    /**
     * Execute JavaScript code and return string result
     */
    @WorkerThread
    suspend fun executeStringScript(
        script: String,
        filename: String = "script.js"
    ): JSExecutionResult = withContext(jsDispatcher) {
        try {
            val context =
                jsContext ?: return@withContext JSExecutionResult.error("Engine not initialized")

            val result = context.executeStringScript(script, filename)

            JSExecutionResult.success(result ?: "null")
        } catch (e: Exception) {
            JSExecutionResult.error("Script execution failed: ${e.message}")
        }
    }

    /**
     * Execute JavaScript code and return boolean result
     */
    @WorkerThread
    suspend fun executeBooleanScript(
        script: String,
        filename: String = "script.js"
    ): JSExecutionResult = withContext(jsDispatcher) {
        try {
            val context =
                jsContext ?: return@withContext JSExecutionResult.error("Engine not initialized")

            val result = context.executeBooleanScript(script, filename)

            JSExecutionResult.success(result.toString())
        } catch (e: Exception) {
            JSExecutionResult.error("Script execution failed: ${e.message}")
        }
    }

    /**
     * Execute JavaScript code and return double result
     */
    @WorkerThread
    suspend fun executeDoubleScript(
        script: String,
        filename: String = "script.js"
    ): JSExecutionResult = withContext(jsDispatcher) {
        try {
            val context =
                jsContext ?: return@withContext JSExecutionResult.error("Engine not initialized")

            val result = context.executeDoubleScript(script, filename)

            JSExecutionResult.success(result.toString())
        } catch (e: Exception) {
            JSExecutionResult.error("Script execution failed: ${e.message}")
        }
    }

    /**
     * Call a JavaScript function by name with parameters
     */
    @WorkerThread
    suspend fun callFunction(functionName: String, vararg parameters: Any): JSExecutionResult =
        withContext(jsDispatcher) {
            try {
                val context = jsContext
                    ?: return@withContext JSExecutionResult.error("Engine not initialized")

                // Build script to call the function
                val paramString = parameters.joinToString(",") { convertToJSValue(it) }
                val callScript = "$functionName($paramString)"

                val result = context.executeScript(callScript, "function_call.js")

                JSExecutionResult.success(convertResultToString(result))
            } catch (e: Exception) {
                JSExecutionResult.error("Function call failed: ${e.message}")
            }
        }

    /**
     * Bind a Kotlin object to JavaScript global scope
     */
    suspend fun bindObject(name: String, obj: Any): Boolean = withContext(jsDispatcher) {
        try {
            val context = jsContext ?: return@withContext false

            // Convert Kotlin object to JS-compatible format
            val jsCompatibleObj = convertToJSCompatible(obj, context)
            context.set(name, jsCompatibleObj as JSValue)

            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    /**
     * Bind a Kotlin function to JavaScript
     */
    suspend fun bindFunction(name: String, callback: JavaCallback): Boolean =
        withContext(jsDispatcher) {
            try {
                val context = jsContext ?: return@withContext false

            val jsFunction = JSFunction(context, callback)
            context.set(name, jsFunction as JSValue)
            jsFunctions[name] = jsFunction

            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    /**
     * Bind a Kotlin void function to JavaScript
     */
    suspend fun bindVoidFunction(name: String, callback: JavaVoidCallback): Boolean =
        withContext(jsDispatcher) {
            try {
                val context = jsContext ?: return@withContext false

            val jsFunction = JSFunction(context, callback)
            context.set(name, jsFunction as JSValue)
            jsFunctions[name] = jsFunction

            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    /**
     * Load JavaScript library from assets
     */
    suspend fun loadLibraryFromAssets(filename: String): JSExecutionResult =
        withContext(jsDispatcher) {
            try {
                val jsCode = context.assets.open(filename).bufferedReader().use { it.readText() }
                executeScript(jsCode, filename)
            } catch (e: Exception) {
                JSExecutionResult.error("Failed to load library $filename: ${e.message}")
            }
        }

    /**
     * Get memory usage information
     */
    fun getMemoryInfo(): JSMemoryInfo {
        return JSMemoryInfo(
            totalObjects = jsObjects.size + jsFunctions.size,
            jsObjectsCount = jsObjects.size,
            functionsCount = jsFunctions.size
        )
    }

    /**
     * Setup global JavaScript objects and utilities
     */
    private fun setupGlobalObjects() {
        val context = jsContext ?: return

        // Add console object for logging
        val console = JSObject(context)
        console.set("log", JSFunction(context, JavaVoidCallback { receiver, args ->
            val message = (0 until args.length()).joinToString(" ") { index ->
                when (val arg = args.get(JSValue.TYPE.UNKNOWN, index)) {
                    is JSArray -> arg.toString()
                    is JSObject -> arg.toString()
                    else -> arg.toString()
                }
            }
            println("JS Console: $message")
        }) as JSValue)
        console.set("error", JSFunction(context, JavaVoidCallback { receiver, args ->
            val message = (0 until args.length()).joinToString(" ") { index ->
                when (val arg = args.get(JSValue.TYPE.UNKNOWN, index)) {
                    is JSArray -> arg.toString()
                    is JSObject -> arg.toString()
                    else -> arg.toString()
                }
            }
            System.err.println("JS Error: $message")
        }) as JSValue)
        context.set("console", console as JSValue)

        // Add Android-specific utilities
        val android = JSObject(context)
        android.set("log", JSFunction(context, JavaVoidCallback { receiver, args ->
            val tag =
                if (args.length() > 0) args.get(JSValue.TYPE.STRING, 0).toString() else "QuickJS"
            val message = if (args.length() > 1) args.get(JSValue.TYPE.STRING, 1).toString() else ""
            Log.d(tag, message)
        }) as JSValue)
        context.set("Android", android as JSValue)
    }

    /**
     * Convert JavaScript result to string representation
     */
    private fun convertResultToString(result: Any?): String {
        return when (result) {
            null -> "null"
            is String -> result
            is Number -> result.toString()
            is Boolean -> result.toString()
            is JSObject -> {
                try {
                    result.toString()
                } catch (e: Exception) {
                    "[object Object]"
                }
            }
            is JSArray -> {
                try {
                    result.toString()
                } catch (e: Exception) {
                    "[object Array]"
                }
            }
            else -> result.toString()
        }
    }

    /**
     * Convert Kotlin value to JavaScript value string for injection
     */
    private fun convertToJSValue(value: Any?): String {
        return when (value) {
            null -> "null"
            is String -> "\"${value.replace("\"", "\\\"")}\""
            is Number, is Boolean -> value.toString()
            is List<*> -> "[${value.joinToString(",") { convertToJSValue(it) }}]"
            is Map<*, *> -> {
                val entries = value.entries.joinToString(",") { (k, v) ->
                    "\"$k\":${convertToJSValue(v)}"
                }
                "{$entries}"
            }

            else -> "\"$value\""
        }
    }

    /**
     * Convert Kotlin object to JavaScript-compatible format
     */
    private fun convertToJSCompatible(obj: Any, context: JSContext): Any {
        return when (obj) {
            is String, is Number, is Boolean -> obj
            is List<*> -> {
                val jsArray = JSArray(context)
                obj.forEachIndexed { index, item ->
                    when (item) {
                        null -> jsArray.set(index.toString(), "null")
                        is String -> jsArray.set(index.toString(), item)
                        is Number -> {
                            if (item is Int) {
                                jsArray.set(index.toString(), item)
                            } else {
                                jsArray.set(index.toString(), item.toDouble())
                            }
                        }

                        is Boolean -> jsArray.set(index.toString(), item)
                        else -> jsArray.set(
                            index.toString(),
                            convertToJSCompatible(item, context) as JSValue
                        )
                    }
                }
                jsArray
            }
            is Map<*, *> -> {
                val jsObject = JSObject(context)
                obj.forEach { (key, value) ->
                    when (value) {
                        null -> jsObject.set(key.toString(), "null")
                        is String, is Number, is Boolean -> jsObject.set(
                            key.toString(),
                            value as JSValue
                        )

                        else -> jsObject.set(
                            key.toString(),
                            convertToJSCompatible(value, context) as JSValue
                        )
                    }
                }
                jsObject
            }
            else -> {
                // Try to serialize to JSON and create JS object
                try {
                    val jsonString =
                        jsonSerializer.encodeToString(kotlinx.serialization.serializer(), obj)
                    // Parse JSON in JavaScript context
                    context.executeObjectScript("JSON.parse('$jsonString')", "object_conversion.js")
                } catch (e: Exception) {
                    obj.toString()
                }
            }
        }
    }

    /**
     * Clean up resources
     */
    fun destroy() {
        jsObjects.values.forEach { /* QuickJS handles cleanup automatically */ }
        jsFunctions.values.forEach { /* QuickJS handles cleanup automatically */ }
        jsObjects.clear()
        jsFunctions.clear()

        jsContext?.close()
        quickJS?.close()
        jsContext = null
        quickJS = null

        jsExecutor.shutdown()
        jsDispatcher.close()
    }
}

/**
 * JavaScript execution result
 */
@Serializable
data class JSExecutionResult(
    val success: Boolean,
    val result: String? = null,
    val error: String? = null
) {
    companion object {
        fun success(result: String) = JSExecutionResult(true, result, null)
        fun error(error: String) = JSExecutionResult(false, null, error)
    }
}

/**
 * JavaScript engine memory information
 */
@Serializable
data class JSMemoryInfo(
    val totalObjects: Int,
    val jsObjectsCount: Int,
    val functionsCount: Int
)