package com.dastanapps.javascriptengine.bridge

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.annotation.MainThread
import kotlinx.coroutines.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.encodeToJsonElement
import kotlinx.serialization.Contextual
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.doubleOrNull
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.longOrNull
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicLong

/**
 * Comprehensive Android JavaScript Bridge
 * Provides bi-directional communication between Android and JavaScript engines
 *
 * Features:
 * - Function registration and calling
 * - Event system
 * - Data serialization/deserialization
 * - Promise-based async operations
 * - Context-aware Android APIs access
 */
class AndroidJSBridge(private val context: Context) {

    companion object {
        private const val TAG = "AndroidJSBridge"
        private const val BRIDGE_NAMESPACE = "AndroidBridge"
    }

    // JSON serializer for data exchange
    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    // Registered functions from Android side
    private val registeredFunctions = ConcurrentHashMap<String, BridgeFunction>()

    // Registered event listeners
    private val eventListeners = ConcurrentHashMap<String, MutableList<BridgeEventListener>>()

    // Callback management for async operations
    private val callbackCounter = AtomicLong(0)
    private val pendingCallbacks = ConcurrentHashMap<String, BridgeCallback>()

    // Coroutine scope for async operations
    private val bridgeScope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    // Main thread handler for UI operations
    private val mainHandler = Handler(Looper.getMainLooper())

    /**
     * Initialize the bridge with core Android functions
     */
    fun initialize() {
        Log.i(TAG, "Initializing Android JavaScript Bridge")

        // Register core Android functions
        registerCoreAndroidFunctions()

        Log.i(TAG, "Bridge initialized with ${registeredFunctions.size} core functions")
    }

    /**
     * Register core Android functions that JavaScript can call
     */
    private fun registerCoreAndroidFunctions() {
        // Toast notifications
        registerFunction("showToast") { params ->
            val message = params.getString("message", "Hello from JS!")
            val duration =
                if (params.getBoolean("long", false)) Toast.LENGTH_LONG else Toast.LENGTH_SHORT

            mainHandler.post {
                Toast.makeText(context, message, duration).show()
            }

            BridgeResult.success(Json.encodeToJsonElement("Toast shown: $message"))
        }

        // Log functions
        registerFunction("log") { params ->
            val message = params.getString("message", "")
            val level = params.getString("level", "info")

            when (level.lowercase()) {
                "debug" -> Log.d(TAG, "JS: $message")
                "info" -> Log.i(TAG, "JS: $message")
                "warn" -> Log.w(TAG, "JS: $message")
                "error" -> Log.e(TAG, "JS: $message")
                else -> Log.i(TAG, "JS: $message")
            }

            BridgeResult.success(Json.encodeToJsonElement("Logged: $message"))
        }

        // Device information
        registerFunction("getDeviceInfo") { params ->
            val deviceInfo = buildString {
                append("{")
                append("\"model\":\"${android.os.Build.MODEL}\",")
                append("\"manufacturer\":\"${android.os.Build.MANUFACTURER}\",")
                append("\"version\":\"${android.os.Build.VERSION.RELEASE}\",")
                append("\"sdk\":${android.os.Build.VERSION.SDK_INT},")
                append("\"packageName\":\"${context.packageName}\"")
                append("}")
            }

            BridgeResult.success(kotlinx.serialization.json.Json.parseToJsonElement(deviceInfo))
        }

        // Storage operations
        registerFunction("setPreference") { params ->
            val key = params.getString("key", "")
            val value = params.getString("value", "")

            if (key.isNotBlank()) {
                val prefs = context.getSharedPreferences("JSBridge", Context.MODE_PRIVATE)
                prefs.edit().putString(key, value).apply()
                BridgeResult.success(Json.encodeToJsonElement("Preference saved: $key"))
            } else {
                BridgeResult.error("Key cannot be empty")
            }
        }

        registerFunction("getPreference") { params ->
            val key = params.getString("key", "")
            val defaultValue = params.getString("default", "")

            if (key.isNotBlank()) {
                val prefs = context.getSharedPreferences("JSBridge", Context.MODE_PRIVATE)
                val value = prefs.getString(key, defaultValue) ?: defaultValue
                BridgeResult.success(Json.encodeToJsonElement(value))
            } else {
                BridgeResult.error("Key cannot be empty")
            }
        }

        // Async operation example
        registerAsyncFunction("delay") { params, callback ->
            val milliseconds = params.getLong("ms", 1000L)

            bridgeScope.launch {
                delay(milliseconds)
                callback.onSuccess(Json.encodeToJsonElement("Delayed for ${milliseconds}ms"))
            }
        }

        // Network status (simplified)
        registerFunction("isNetworkAvailable") { params ->
            // In a real implementation, you would check actual network connectivity
            BridgeResult.success(Json.encodeToJsonElement(true))
        }
    }

    /**
     * Register a synchronous function that JavaScript can call
     */
    fun registerFunction(name: String, function: (BridgeParams) -> BridgeResult) {
        registeredFunctions[name] = BridgeFunction.Sync(function)
        Log.d(TAG, "Registered sync function: $name")
    }

    /**
     * Register an asynchronous function that JavaScript can call
     */
    fun registerAsyncFunction(name: String, function: (BridgeParams, BridgeCallback) -> Unit) {
        registeredFunctions[name] = BridgeFunction.Async(function)
        Log.d(TAG, "Registered async function: $name")
    }

    /**
     * Call a function from JavaScript side
     */
    suspend fun callJSFunction(
        functionName: String,
        params: Map<String, Any> = emptyMap()
    ): BridgeResult {
        return withContext(Dispatchers.Main) {
            try {
                // In a real implementation, this would call the JavaScript function
                // For now, we'll simulate the call
                Log.d(TAG, "Calling JS function: $functionName with params: $params")

                // Simulate different responses based on function name
                when (functionName) {
                    "greet" -> BridgeResult.success(Json.encodeToJsonElement("Hello from JavaScript!"))
                    "calculate" -> {
                        val a = (params["a"] as? Number)?.toDouble() ?: 0.0
                        val b = (params["b"] as? Number)?.toDouble() ?: 0.0
                        val operation = params["operation"] as? String ?: "add"

                        val result = when (operation) {
                            "add" -> a + b
                            "subtract" -> a - b
                            "multiply" -> a * b
                            "divide" -> if (b != 0.0) a / b else Double.NaN
                            else -> 0.0
                        }

                        BridgeResult.success(Json.encodeToJsonElement(result))
                    }

                    else -> BridgeResult.error("Function not found: $functionName")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error calling JS function: $functionName", e)
                BridgeResult.error("Error: ${e.message}")
            }
        }
    }

    /**
     * Handle function call from JavaScript
     */
    fun handleFunctionCall(
        functionName: String,
        params: String,
        callbackId: String? = null
    ): String {
        return try {
            val bridgeParams = BridgeParams.fromJson(params)
            val function = registeredFunctions[functionName]

            if (function == null) {
                val error = BridgeResult.error("Function not found: $functionName")
                json.encodeToString(error)
            } else {
                when (function) {
                    is BridgeFunction.Sync -> {
                        val result = function.execute(bridgeParams)
                        json.encodeToString(result)
                    }

                    is BridgeFunction.Async -> {
                        if (callbackId != null) {
                            val callback = object : BridgeCallback {
                                override fun onSuccess(result: JsonElement?) {
                                    val bridgeResult = BridgeResult.success(result)
                                    // In a real implementation, this would call back to JavaScript
                                    Log.d(TAG, "Async callback success for $callbackId: $result")
                                    pendingCallbacks.remove(callbackId)
                                }

                                override fun onError(error: String) {
                                    val bridgeResult = BridgeResult.error(error)
                                    // In a real implementation, this would call back to JavaScript
                                    Log.e(TAG, "Async callback error for $callbackId: $error")
                                    pendingCallbacks.remove(callbackId)
                                }
                            }

                            pendingCallbacks[callbackId] = callback
                            function.execute(bridgeParams, callback)

                            // Return pending status
                            json.encodeToString(BridgeResult.pending(callbackId))
                        } else {
                            val error =
                                BridgeResult.error("Callback ID required for async function")
                            json.encodeToString(error)
                        }
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error handling function call: $functionName", e)
            val error = BridgeResult.error("Error: ${e.message}")
            json.encodeToString(error)
        }
    }

    /**
     * Emit an event from Android to JavaScript
     */
    fun emitEvent(eventName: String, data: JsonElement? = null) {
        val event = BridgeEvent(eventName, data)
        val eventJson = json.encodeToString(event)

        // Notify local listeners
        eventListeners[eventName]?.forEach { listener ->
            try {
                listener.onEvent(event)
            } catch (e: Exception) {
                Log.e(TAG, "Error in event listener for $eventName", e)
            }
        }

        // In a real implementation, this would also notify JavaScript listeners
        Log.d(TAG, "Event emitted: $eventName with data: $data")
    }

    /**
     * Register an event listener from Android side
     */
    fun addEventListener(eventName: String, listener: BridgeEventListener) {
        eventListeners.getOrPut(eventName) { mutableListOf() }.add(listener)
        Log.d(TAG, "Event listener added for: $eventName")
    }

    /**
     * Remove an event listener
     */
    fun removeEventListener(eventName: String, listener: BridgeEventListener) {
        eventListeners[eventName]?.remove(listener)
        Log.d(TAG, "Event listener removed for: $eventName")
    }

    /**
     * Get JavaScript code to inject into JS engines
     */
    fun getJavaScriptBridgeCode(): String {
        return """
            // Android JavaScript Bridge
            (function() {
                window.$BRIDGE_NAMESPACE = {
                    // Call Android function synchronously
                    call: function(functionName, params) {
                        try {
                            if (typeof AndroidInterface !== 'undefined' && AndroidInterface.callBridgeFunction) {
                                const paramsJson = JSON.stringify(params || {});
                                const resultJson = AndroidInterface.callBridgeFunction(functionName, paramsJson);
                                return JSON.parse(resultJson);
                            } else {
                                console.error('AndroidInterface not available');
                                return { success: false, error: 'AndroidInterface not available' };
                            }
                        } catch (e) {
                            console.error('Error calling bridge function:', e);
                            return { success: false, error: e.message };
                        }
                    },
                    
                    // Call Android function asynchronously
                    callAsync: function(functionName, params) {
                        return new Promise((resolve, reject) => {
                            try {
                                if (typeof AndroidInterface !== 'undefined' && AndroidInterface.callBridgeFunction) {
                                    const callbackId = 'callback_' + Date.now() + '_' + Math.random();
                                    const paramsJson = JSON.stringify(params || {});
                                    
                                    // Store callback
                                    window.$BRIDGE_NAMESPACE._callbacks = window.$BRIDGE_NAMESPACE._callbacks || {};
                                    window.$BRIDGE_NAMESPACE._callbacks[callbackId] = { resolve, reject };
                                    
                                    const resultJson = AndroidInterface.callBridgeFunction(functionName, paramsJson, callbackId);
                                    const result = JSON.parse(resultJson);
                                    
                                    if (result.status === 'pending') {
                                        // Callback will be called later
                                    } else if (result.success) {
                                        resolve(result.data);
                                    } else {
                                        reject(new Error(result.error || 'Unknown error'));
                                    }
                                } else {
                                    reject(new Error('AndroidInterface not available'));
                                }
                            } catch (e) {
                                reject(e);
                            }
                        });
                    },
                    
                    // Handle callback from Android
                    _handleCallback: function(callbackId, success, data) {
                        const callbacks = window.$BRIDGE_NAMESPACE._callbacks || {};
                        const callback = callbacks[callbackId];
                        
                        if (callback) {
                            if (success) {
                                callback.resolve(data);
                            } else {
                                callback.reject(new Error(data || 'Unknown error'));
                            }
                            delete callbacks[callbackId];
                        }
                    },
                    
                    // Event system
                    _eventListeners: {},
                    
                    addEventListener: function(eventName, listener) {
                        if (!this._eventListeners[eventName]) {
                            this._eventListeners[eventName] = [];
                        }
                        this._eventListeners[eventName].push(listener);
                    },
                    
                    removeEventListener: function(eventName, listener) {
                        const listeners = this._eventListeners[eventName];
                        if (listeners) {
                            const index = listeners.indexOf(listener);
                            if (index !== -1) {
                                listeners.splice(index, 1);
                            }
                        }
                    },
                    
                    _handleEvent: function(eventName, data) {
                        const listeners = this._eventListeners[eventName] || [];
                        listeners.forEach(listener => {
                            try {
                                listener(data);
                            } catch (e) {
                                console.error('Error in event listener:', e);
                            }
                        });
                    },
                    
                    // Convenience functions
                    showToast: function(message, isLong) {
                        return this.call('showToast', { message: message, long: isLong || false });
                    },
                    
                    log: function(message, level) {
                        return this.call('log', { message: message, level: level || 'info' });
                    },
                    
                    getDeviceInfo: function() {
                        return this.call('getDeviceInfo');
                    },
                    
                    setPreference: function(key, value) {
                        return this.call('setPreference', { key: key, value: value });
                    },
                    
                    getPreference: function(key, defaultValue) {
                        return this.call('getPreference', { key: key, default: defaultValue || '' });
                    },
                    
                    delay: function(milliseconds) {
                        return this.callAsync('delay', { ms: milliseconds });
                    },
                    
                    isNetworkAvailable: function() {
                        return this.call('isNetworkAvailable');
                    }
                };
                
                console.log('Android JavaScript Bridge initialized');
            })();
        """.trimIndent()
    }

    /**
     * Clean up resources
     */
    fun cleanup() {
        try {
            bridgeScope.cancel()
            registeredFunctions.clear()
            eventListeners.clear()
            pendingCallbacks.clear()
            Log.i(TAG, "Bridge cleaned up")
        } catch (e: Exception) {
            Log.e(TAG, "Error during cleanup", e)
        }
    }
}

/**
 * Represents different types of bridge functions
 */
sealed class BridgeFunction {
    data class Sync(val function: (BridgeParams) -> BridgeResult) : BridgeFunction() {
        fun execute(params: BridgeParams): BridgeResult = function(params)
    }

    data class Async(val function: (BridgeParams, BridgeCallback) -> Unit) : BridgeFunction() {
        fun execute(params: BridgeParams, callback: BridgeCallback) = function(params, callback)
    }
}

/**
 * Parameters passed from JavaScript to Android
 */
class BridgeParams(private val data: Map<String, JsonElement?>) {

    fun getString(key: String, default: String = ""): String {
        val value = data[key]
        return value?.jsonPrimitive?.content ?: default
    }

    fun getInt(key: String, default: Int = 0): Int {
        val value = data[key]
        return value?.jsonPrimitive?.intOrNull ?: default
    }

    fun getLong(key: String, default: Long = 0L): Long {
        val value = data[key]
        return value?.jsonPrimitive?.longOrNull ?: default
    }

    fun getDouble(key: String, default: Double = 0.0): Double {
        val value = data[key]
        return value?.jsonPrimitive?.doubleOrNull ?: default
    }

    fun getBoolean(key: String, default: Boolean = false): Boolean {
        val value = data[key]
        return value?.jsonPrimitive?.booleanOrNull ?: default
    }

    fun get(key: String): JsonElement? = data[key]

    fun getAll(): Map<String, JsonElement?> = data.toMap()

    companion object {
        fun fromJson(json: String): BridgeParams {
            return try {
                val map = Json.decodeFromString<Map<String, JsonElement>>(json)
                BridgeParams(map)
            } catch (e: Exception) {
                BridgeParams(emptyMap())
            }
        }
    }
}

/**
 * Result returned from Android to JavaScript
 */
@Serializable
data class BridgeResult(
    val success: Boolean,
    @Contextual
    val data: JsonElement? = null,
    val error: String? = null,
    val status: String = "completed"
) {
    companion object {
        fun success(data: JsonElement? = null) = BridgeResult(true, data = data)
        fun error(message: String) = BridgeResult(false, error = message)
        fun pending(callbackId: String) =
            BridgeResult(true, data = Json.encodeToJsonElement(callbackId), status = "pending")
    }
}

/**
 * Event emitted from Android to JavaScript
 */
@Serializable
data class BridgeEvent(
    val name: String,
    @Contextual
    val data: JsonElement? = null,
    val timestamp: Long = System.currentTimeMillis()
)

/**
 * Callback interface for async operations
 */
interface BridgeCallback {
    fun onSuccess(result: JsonElement?)
    fun onError(error: String)
}

/**
 * Event listener interface for Android side
 */
interface BridgeEventListener {
    fun onEvent(event: BridgeEvent)
}