package com.dastanapps.javascriptengine.jsonata.activity

import android.annotation.SuppressLint
import android.content.Context
import android.webkit.JavascriptInterface
import android.webkit.WebView
import android.webkit.WebViewClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import org.json.JSONObject
import kotlin.coroutines.resume

/**
 * Headless WebView engine for executing JSONata expressions
 */
class WebViewEngine(private val context: Context) {

    private var webView: WebView? = null
    private var isPageLoaded = false
    private val pendingCallbacks = mutableMapOf<String, (String) -> Unit>()


    @SuppressLint("SetJavaScriptEnabled")
    fun initialize() {
        webView = WebView(context).apply {
            settings.javaScriptEnabled = true
            settings.domStorageEnabled = true
            addJavascriptInterface(WebAppInterface(), "Android")
            webViewClient = object : WebViewClient() {
                override fun onPageFinished(view: WebView?, url: String?) {
                    super.onPageFinished(view, url)
                    isPageLoaded = true
                }
            }
            loadUrl("file:///android_asset/jsonata.html")
            evaluateJavascript("", null)
        }
    }

    inner class WebAppInterface {
        @JavascriptInterface
        fun onResultWithCallback(callId: String, result: String) {
            // Find and invoke the pending callback
            pendingCallbacks.remove(callId)?.invoke(result)
        }
    }

    /**
     * Evaluate JSONata expression and return the result as String
     * This is a suspend function that waits for JS execution to complete
     */
    suspend fun evaluate(json: String, expression: String): String {
        if (!isPageLoaded) {
            throw IllegalStateException("WebView not ready yet")
        }

        return suspendCancellableCoroutine { continuation ->
            val callId = System.currentTimeMillis().toString()

            // Store the callback
            pendingCallbacks[callId] = { result ->
                continuation.resume(result)
            }

            // Clean up on cancellation
            continuation.invokeOnCancellation {
                pendingCallbacks.remove(callId)
            }
            val js = """
                    window.evaluateJsonataWithCallback(
                        ${JSONObject.quote(json)}, 
                        ${JSONObject.quote(expression)},
                        ${JSONObject.quote(callId)}
                    );
                """.trimIndent()
            webView?.evaluateJavascript(js, null)
        }
    }

    /**
     * Clean up resources
     */
    fun destroy() {
        webView?.destroy()
        webView = null
        pendingCallbacks.clear()
    }
}
