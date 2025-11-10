package com.dastanapps.javascriptengine.es6promises

/**
 * Represents the status of a test execution
 */
enum class TestStatus {
    SUCCESS,
    FAILED,
    RUNNING
}

/**
 * Represents the result of a single test execution
 */
data class TestResult(
    val testName: String,
    val description: String,
    val engine: String,
    val status: TestStatus,
    val output: String = "",
    val error: String = "",
    val executionTime: Long = 0L
)

/**
 * UI state for the ES6 Promise test screen
 */
data class ES6PromiseTestUiState(
    val isRunning: Boolean = false,
    val testResults: List<TestResult> = emptyList(),
    val testCategories: List<String> = listOf(
        "Comprehensive",
        "Arrow Functions",
        "Template Literals",
        "Destructuring",
        "Promises",
        "Async/Await",
        "Classes",
        "Modules",
        "Spread Operator",
        "Default Parameters",
        "For...of Loops",
        "Variable Declarations",
        "Collections"
    )
)

/**
 * Represents a JavaScript test case
 */
data class JSTestCase(
    val name: String,
    val description: String,
    val jsCode: String,
    val category: String,
    val expectedOutput: String? = null
)