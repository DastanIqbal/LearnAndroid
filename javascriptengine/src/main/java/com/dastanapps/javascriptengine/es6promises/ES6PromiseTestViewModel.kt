package com.dastanapps.javascriptengine.es6promises

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * ViewModel for managing ES6 Promise test execution and state
 */
class ES6PromiseTestViewModel(application: Application) : AndroidViewModel(application) {

    companion object {
        private const val TAG = "ES6PromiseTestVM"
    }

    private val _uiState = MutableStateFlow(ES6PromiseTestUiState())
    val uiState: StateFlow<ES6PromiseTestUiState> = _uiState

    private val testExecutor = ES6TestExecutor(application)

    /**
     * Run all available ES6 and Promise tests
     */
    fun runAllTests() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isRunning = true)

            try {
                val allTests = testExecutor.getAllTestCases()
                val results = mutableListOf<TestResult>()

                allTests.forEach { testCase ->
                    Log.d(TAG, "Running test: ${testCase.name}")

                    // Test with AndroidX JavaScript Engine
                    val androidXResult = testExecutor.executeTestWithAndroidXJSEngine(testCase)
                    results.add(androidXResult)

                    // Update UI with intermediate results
                    _uiState.value = _uiState.value.copy(testResults = results.toList())
                }

            } catch (e: Exception) {
                Log.e(TAG, "Error running all tests", e)
                val errorResult = TestResult(
                    testName = "Test Execution Error",
                    description = "Failed to execute all tests",
                    engine = "System",
                    status = TestStatus.FAILED,
                    error = e.message ?: "Unknown error"
                )
                _uiState.value = _uiState.value.copy(
                    testResults = _uiState.value.testResults + errorResult
                )
            } finally {
                _uiState.value = _uiState.value.copy(isRunning = false)
            }
        }
    }

    /**
     * Run tests for a specific category
     */
    fun runTestCategory(category: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isRunning = true)

            try {
                val categoryTests = testExecutor.getTestCasesByCategory(category)
                val results = mutableListOf<TestResult>()

                categoryTests.forEach { testCase ->
                    Log.d(TAG, "Running category test: ${testCase.name}")

                    // Test with AndroidX JavaScript Engine
                    val androidXResult = testExecutor.executeTestWithAndroidXJSEngine(testCase)
                    results.add(androidXResult)
                }

                _uiState.value = _uiState.value.copy(
                    testResults = _uiState.value.testResults + results
                )

            } catch (e: Exception) {
                Log.e(TAG, "Error running category tests: $category", e)
                val errorResult = TestResult(
                    testName = "$category Test Error",
                    description = "Failed to execute $category tests",
                    engine = "System",
                    status = TestStatus.FAILED,
                    error = e.message ?: "Unknown error"
                )
                _uiState.value = _uiState.value.copy(
                    testResults = _uiState.value.testResults + errorResult
                )
            } finally {
                _uiState.value = _uiState.value.copy(isRunning = false)
            }
        }
    }

    /**
     * Clear all test results
     */
    fun clearResults() {
        _uiState.value = _uiState.value.copy(testResults = emptyList())
    }

    override fun onCleared() {
        super.onCleared()
        testExecutor.cleanup()
    }
}