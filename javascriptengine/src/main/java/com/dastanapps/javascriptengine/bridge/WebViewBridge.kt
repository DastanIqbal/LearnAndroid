package com.dastanapps.javascriptengine.bridge

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import android.webkit.JavascriptInterface
import android.webkit.WebView
import android.webkit.WebViewClient
import kotlinx.coroutines.*
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.encodeToJsonElement

/**
 * WebView-specific implementation of JavaScript Bridge
 * Integrates AndroidJSBridge with WebView for bidirectional communication
 */
class WebViewBridge(
    private val context: Context,
    private val webView: WebView
) {

    companion object {
        private const val TAG = "WebViewBridge"
        private const val JS_INTERFACE_NAME = "AndroidInterface"
    }

    private val androidBridge = AndroidJSBridge(context)
    private val bridgeScope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private var isInitialized = false

    /**
     * Initialize the WebView bridge
     */
    @SuppressLint("SetJavaScriptEnabled")
    fun initialize() {
        Log.i(TAG, "Initializing WebView Bridge")

        // Initialize Android bridge first
        androidBridge.initialize()

        // Configure WebView settings
        webView.settings.apply {
            javaScriptEnabled = true
            domStorageEnabled = true
            allowFileAccess = true
            allowContentAccess = true
            allowUniversalAccessFromFileURLs = true
            allowFileAccessFromFileURLs = true
        }

        // Add JavaScript interface BEFORE setting WebViewClient
        webView.addJavascriptInterface(WebViewJSInterface(), JS_INTERFACE_NAME)
        Log.d(TAG, "JavaScript interface added: $JS_INTERFACE_NAME")

        // Set WebView client
        webView.webViewClient = object : WebViewClient() {
            override fun onPageStarted(
                view: WebView?,
                url: String?,
                favicon: android.graphics.Bitmap?
            ) {
                super.onPageStarted(view, url, favicon)
                Log.d(TAG, "Page started loading: $url")
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                Log.d(TAG, "Page finished loading: $url")

                // Inject bridge code after page loads
                injectBridgeCode()

                if (!isInitialized) {
                    isInitialized = true
                    Log.i(TAG, "WebView Bridge initialized successfully")
                }
            }

            override fun onReceivedError(
                view: WebView?,
                errorCode: Int,
                description: String?,
                failingUrl: String?
            ) {
                super.onReceivedError(view, errorCode, description, failingUrl)
                Log.e(TAG, "WebView error: $errorCode - $description for URL: $failingUrl")
            }
        }

        // Register additional WebView-specific functions
        registerWebViewFunctions()
    }

    /**
     * Register WebView-specific functions
     */
    private fun registerWebViewFunctions() {
        // Navigate function
        androidBridge.registerFunction("navigate") { params ->
            val url = params.getString("url", "")
            if (url.isNotBlank()) {
                webView.post {
                    webView.loadUrl(url)
                }
                BridgeResult.success(Json.encodeToJsonElement("Navigating to: $url"))
            } else {
                BridgeResult.error("URL cannot be empty")
            }
        }

        // Evaluate JavaScript from Android
        androidBridge.registerAsyncFunction("evaluateJS") { params, callback ->
            val jsCode = params.getString("code", "")
            if (jsCode.isNotBlank()) {
                webView.post {
                    webView.evaluateJavascript(jsCode) { result ->
                        callback.onSuccess(Json.encodeToJsonElement(result ?: "null"))
                    }
                }
            } else {
                callback.onError("JavaScript code cannot be empty")
            }
        }

        // Get page title
        androidBridge.registerFunction("getPageTitle") { params ->
            val title = webView.title ?: "No Title"
            BridgeResult.success(Json.encodeToJsonElement(title))
        }

        // Get current URL
        androidBridge.registerFunction("getCurrentUrl") { params ->
            val url = webView.url ?: "about:blank"
            BridgeResult.success(Json.encodeToJsonElement(url))
        }

        // Reload page
        androidBridge.registerFunction("reload") { params ->
            webView.post {
                webView.reload()
            }
            BridgeResult.success(Json.encodeToJsonElement("Page reloaded"))
        }

        // Go back
        androidBridge.registerFunction("goBack") { params ->
            if (webView.canGoBack()) {
                webView.post {
                    webView.goBack()
                }
                BridgeResult.success(Json.encodeToJsonElement("Navigated back"))
            } else {
                BridgeResult.error("Cannot go back")
            }
        }

        // Go forward
        androidBridge.registerFunction("goForward") { params ->
            if (webView.canGoForward()) {
                webView.post {
                    webView.goForward()
                }
                BridgeResult.success(Json.encodeToJsonElement("Navigated forward"))
            } else {
                BridgeResult.error("Cannot go forward")
            }
        }
    }

    /**
     * Inject bridge JavaScript code into WebView
     */
    private fun injectBridgeCode() {
        val bridgeCode = androidBridge.getJavaScriptBridgeCode()
        val enhancedCode = """
            $bridgeCode
            
            // WebView-specific enhancements
            AndroidBridge.webview = {
                navigate: function(url) {
                    return AndroidBridge.call('navigate', { url: url });
                },
                
                evaluateJS: function(code) {
                    return AndroidBridge.callAsync('evaluateJS', { code: code });
                },
                
                getPageTitle: function() {
                    return AndroidBridge.call('getPageTitle');
                },
                
                getCurrentUrl: function() {
                    return AndroidBridge.call('getCurrentUrl');
                },
                
                reload: function() {
                    return AndroidBridge.call('reload');
                },
                
                goBack: function() {
                    return AndroidBridge.call('goBack');
                },
                
                goForward: function() {
                    return AndroidBridge.call('goForward');
                }
            };
            
            console.log('WebView Bridge enhanced features loaded');
        """.trimIndent()

        webView.evaluateJavascript(enhancedCode, null)
        Log.d(TAG, "Bridge code injected into WebView")
    }

    /**
     * Load HTML content with bridge support
     */
    fun loadHtmlWithBridge(html: String, baseUrl: String? = null) {
        val enhancedHtml = """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <title>WebView Bridge Demo</title>
            </head>
            <body>
                $html
                <script>
                    console.log('HTML loaded with bridge support');
                </script>
            </body>
            </html>
        """.trimIndent()

        webView.loadDataWithBaseURL(
            baseUrl ?: "file:///android_asset/",
            enhancedHtml,
            "text/html",
            "UTF-8",
            null
        )
    }

    /**
     * Call JavaScript function from Android
     */
    suspend fun callJSFunction(
        functionName: String,
        params: Map<String, Any> = emptyMap()
    ): BridgeResult {
        return androidBridge.callJSFunction(functionName, params)
    }

    /**
     * Register Android function for JavaScript to call
     */
    fun registerFunction(name: String, function: (BridgeParams) -> BridgeResult) {
        androidBridge.registerFunction(name, function)
    }

    /**
     * Register async Android function for JavaScript to call
     */
    fun registerAsyncFunction(name: String, function: (BridgeParams, BridgeCallback) -> Unit) {
        androidBridge.registerAsyncFunction(name, function)
    }

    /**
     * Emit event to JavaScript
     */
    fun emitEvent(eventName: String, data: JsonElement? = null) {
        androidBridge.emitEvent(eventName, data)

        // Also emit to WebView
        val eventJson = if (data != null) {
            data.toString()
        } else {
            "null"
        }

        val jsCode = """
            if (window.AndroidBridge && window.AndroidBridge._handleEvent) {
                window.AndroidBridge._handleEvent('$eventName', $eventJson);
            }
        """.trimIndent()

        webView.post {
            webView.evaluateJavascript(jsCode, null)
        }
    }

    /**
     * Add event listener from Android side
     */
    fun addEventListener(eventName: String, listener: BridgeEventListener) {
        androidBridge.addEventListener(eventName, listener)
    }

    /**
     * Remove event listener
     */
    fun removeEventListener(eventName: String, listener: BridgeEventListener) {
        androidBridge.removeEventListener(eventName, listener)
    }

    /**
     * Clean up resources
     */
    fun cleanup() {
        try {
            bridgeScope.cancel()
            androidBridge.cleanup()
            webView.removeJavascriptInterface(JS_INTERFACE_NAME)
            Log.i(TAG, "WebView Bridge cleaned up")
        } catch (e: Exception) {
            Log.e(TAG, "Error during cleanup", e)
        }
    }

    /**
     * JavaScript interface for WebView
     */
    private inner class WebViewJSInterface {

        @JavascriptInterface
        fun callBridgeFunction(functionName: String, params: String): String {
            Log.d(TAG, "Bridge function called: $functionName with params: $params")
            return androidBridge.handleFunctionCall(functionName, params, null)
        }

        @JavascriptInterface
        fun callBridgeFunction(functionName: String, params: String, callbackId: String): String {
            Log.d(
                TAG,
                "Bridge async function called: $functionName with params: $params, callbackId: $callbackId"
            )
            return androidBridge.handleFunctionCall(functionName, params, callbackId)
        }

        @JavascriptInterface
        fun handleCallback(callbackId: String, success: Boolean, data: String) {
            Log.d(TAG, "Callback received: $callbackId, success: $success")

            val jsCode = """
                if (window.AndroidBridge && window.AndroidBridge._handleCallback) {
                    window.AndroidBridge._handleCallback('$callbackId', $success, $data);
                }
            """.trimIndent()

            webView.post {
                webView.evaluateJavascript(jsCode, null)
            }
        }

        @JavascriptInterface
        fun emitEventToAndroid(eventName: String, data: String) {
            Log.d(TAG, "Event emitted from JS: $eventName")

            try {
                val eventData = if (data != "null" && data.isNotBlank()) {
                    kotlinx.serialization.json.Json.parseToJsonElement(data)
                } else {
                    null
                }
                androidBridge.emitEvent(eventName, eventData)
            } catch (e: Exception) {
                Log.e(TAG, "Error parsing event data from JS", e)
            }
        }

        @JavascriptInterface
        fun onConsoleLog(message: String) {
            Log.d(TAG, "WebView Console Log: $message")
        }

        @JavascriptInterface
        fun onConsoleError(message: String) {
            Log.e(TAG, "WebView Console Error: $message")
        }
    }
}