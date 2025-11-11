package com.dastanapps.javascriptengine.webview

/**
 * Data models for WebView JavaScript testing
 */

/**
 * Status of a WebView test execution
 */
enum class WebViewTestStatus {
    SUCCESS,
    FAILED,
    RUNNING
}

/**
 * Result of a WebView JavaScript test execution
 */
data class WebViewTestResult(
    val testName: String,
    val description: String,
    val status: WebViewTestStatus,
    val output: String = "",
    val error: String = "",
    val executionTime: Long = 0L
)

/**
 * WebView JavaScript test case definition
 */
data class WebViewJSTestCase(
    val name: String,
    val description: String,
    val jsCode: String,
    val category: String,
    val expectedResult: String? = null
)

/**
 * UI state for WebView JavaScript testing
 */
data class WebViewJSTestUiState(
    val testResults: List<WebViewTestResult> = emptyList(),
    val testCategories: List<String> = listOf(
        "ES6 Features",
        "Promises",
        "JSONata",
        "Comprehensive",
        "Performance"
    ),
    val isRunning: Boolean = false,
    val currentTest: String = ""
)