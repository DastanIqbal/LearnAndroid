package com.dastanapps.javascriptengine.webview

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel for WebView JavaScript testing
 * Manages test execution state and coordinates with WebView executor
 */
class WebViewJSTestViewModel(application: Application) : AndroidViewModel(application) {

    companion object {
        private const val TAG = "WebViewJSTestViewModel"
    }

    private val _uiState = MutableStateFlow(WebViewJSTestUiState())
    val uiState: StateFlow<WebViewJSTestUiState> = _uiState.asStateFlow()

    private val testExecutor = WebViewJSTestExecutor(application)

    /**
     * Run all available tests
     */
    fun runAllTests() {
        viewModelScope.launch {
            Log.i(TAG, "Starting all WebView JavaScript tests")

            _uiState.value = _uiState.value.copy(
                isRunning = true,
                testResults = emptyList()
            )

            val allTests = testExecutor.getAllTestCases()
            val results = mutableListOf<WebViewTestResult>()

            Log.i(TAG, "Total tests to execute: ${allTests.size}")

            for (testCase in allTests) {
                Log.d(TAG, "Executing test: ${testCase.name}")

                _uiState.value = _uiState.value.copy(
                    currentTest = testCase.name
                )

                val result = testExecutor.executeTest(testCase)
                results.add(result)

                // Log test results to Logcat
                logTestResult(result)

                // Update UI with current results
                _uiState.value = _uiState.value.copy(
                    testResults = results.toList()
                )
            }

            val successCount = results.count { it.status == WebViewTestStatus.SUCCESS }
            val failCount = results.count { it.status == WebViewTestStatus.FAILED }

            Log.i(TAG, "All tests completed - Success: $successCount, Failed: $failCount")

            _uiState.value = _uiState.value.copy(
                isRunning = false,
                currentTest = ""
            )
        }
    }

    /**
     * Run tests for a specific category
     */
    fun runTestCategory(category: String) {
        viewModelScope.launch {
            Log.i(TAG, "Starting WebView JavaScript tests for category: $category")

            _uiState.value = _uiState.value.copy(
                isRunning = true,
                testResults = emptyList()
            )

            val categoryTests = testExecutor.getTestCasesByCategory(category)
            val results = mutableListOf<WebViewTestResult>()

            Log.i(TAG, "Tests in category '$category': ${categoryTests.size}")

            for (testCase in categoryTests) {
                Log.d(TAG, "Executing test: ${testCase.name} (Category: $category)")

                _uiState.value = _uiState.value.copy(
                    currentTest = testCase.name
                )

                val result = testExecutor.executeTest(testCase)
                results.add(result)

                // Log test results to Logcat
                logTestResult(result)

                // Update UI with current results
                _uiState.value = _uiState.value.copy(
                    testResults = results.toList()
                )
            }

            val successCount = results.count { it.status == WebViewTestStatus.SUCCESS }
            val failCount = results.count { it.status == WebViewTestStatus.FAILED }

            Log.i(
                TAG,
                "Category '$category' tests completed - Success: $successCount, Failed: $failCount"
            )

            _uiState.value = _uiState.value.copy(
                isRunning = false,
                currentTest = ""
            )
        }
    }

    /**
     * Clear all test results
     */
    fun clearResults() {
        Log.d(TAG, "Clearing all test results")
        _uiState.value = _uiState.value.copy(
            testResults = emptyList()
        )
    }

    /**
     * Log test results to Logcat with detailed information
     */
    private fun logTestResult(result: WebViewTestResult) {
        val logMessage = buildString {
            appendLine("=".repeat(60))
            appendLine("TEST RESULT: ${result.testName}")
            appendLine("Description: ${result.description}")
            appendLine("Status: ${result.status}")
            appendLine("Execution Time: ${result.executionTime}ms")

            if (result.output.isNotEmpty()) {
                appendLine("Output:")

                // Special handling for JSONata results - format JSON for better readability
                if (result.testName.contains("JSONata", ignoreCase = true) ||
                    result.output.startsWith("{") || result.output.startsWith("[")
                ) {

                    try {
                        // Try to format JSON for better readability
                        val formattedOutput = formatJsonOutput(result.output)
                        appendLine(formattedOutput)
                    } catch (e: Exception) {
                        // If JSON formatting fails, log as-is
                        appendLine(result.output)
                    }
                } else {
                    appendLine(result.output)
                }
            }

            if (result.error.isNotEmpty()) {
                appendLine("Error: ${result.error}")
            }

            appendLine("=".repeat(60))
        }

        when (result.status) {
            WebViewTestStatus.SUCCESS -> Log.i(TAG, logMessage)
            WebViewTestStatus.FAILED -> Log.e(TAG, logMessage)
            WebViewTestStatus.RUNNING -> Log.d(TAG, logMessage)
        }

        // Additional JSONata-specific logging
        if (result.testName.contains("JSONata", ignoreCase = true)) {
            logJsonataSpecificResults(result)
        }
    }

    /**
     * Format JSON output for better readability in logs
     */
    private fun formatJsonOutput(output: String): String {
        return try {
            // Simple JSON formatting - add line breaks after commas and braces
            output
                .replace(",", ",\n    ")
                .replace("{", "{\n    ")
                .replace("}", "\n}")
                .replace("[", "[\n    ")
                .replace("]", "\n]")
        } catch (e: Exception) {
            output
        }
    }

    /**
     * Log JSONata-specific results with additional analysis
     */
    private fun logJsonataSpecificResults(result: WebViewTestResult) {
        Log.d(TAG, "JSONata Analysis for: ${result.testName}")

        try {
            when {
                result.output.contains("transformations") -> {
                    Log.d(TAG, "JSONata Transformations detected - multiple expressions executed")
                }

                result.output.contains("success\": true") -> {
                    Log.d(TAG, "JSONata operation successful")
                }

                result.output.contains("success\": false") -> {
                    Log.w(TAG, "JSONata operation failed - check error details")
                }

                result.output.startsWith("[") -> {
                    Log.d(TAG, "JSONata result is an array")
                }

                result.output.startsWith("{") -> {
                    Log.d(TAG, "JSONata result is an object")
                }

                else -> {
                    Log.d(TAG, "JSONata result is a primitive value: ${result.output}")
                }
            }

            // Log execution performance for JSONata
            if (result.executionTime > 0) {
                when {
                    result.executionTime < 50 -> Log.d(
                        TAG,
                        "JSONata Performance: Fast (${result.executionTime}ms)"
                    )

                    result.executionTime < 200 -> Log.d(
                        TAG,
                        "JSONata Performance: Normal (${result.executionTime}ms)"
                    )

                    else -> Log.w(TAG, "JSONata Performance: Slow (${result.executionTime}ms)")
                }
            }

        } catch (e: Exception) {
            Log.w(TAG, "Error analyzing JSONata result: ${e.message}")
        }
    }

    override fun onCleared() {
        super.onCleared()
        Log.d(TAG, "ViewModel cleared, cleaning up WebView test executor")
        testExecutor.cleanup()
    }
}