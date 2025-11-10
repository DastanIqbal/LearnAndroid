package com.dastanapps.javascriptengine.integration

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel for managing the Integration Demo Activity state
 * Handles the initialization and execution of JS-JSONata integration scenarios
 */
class IntegrationDemoViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(IntegrationDemoUiState())
    val uiState: StateFlow<IntegrationDemoUiState> = _uiState.asStateFlow()

    private var integration: JSJSONataIntegration? = null
    private var applicationContext: Context? = null

    /**
     * Initialize the JS-JSONata integration
     */
    fun initializeIntegration(context: Context) {
        viewModelScope.launch {
            try {
                // Store application context for later use
                applicationContext = context.applicationContext

                _uiState.value = _uiState.value.copy(isInitializing = true)

                integration = JSJSONataIntegration.getInstance(context)

                val config = JSJSONataIntegration.IntegrationConfig(
                    enableCache = true,
                    maxCacheSize = 50,
                    timeoutMs = 15000,
                    enableLogging = true
                )

                val success = integration?.initialize(config) ?: false

                _uiState.value = _uiState.value.copy(
                    isInitialized = success,
                    isInitializing = false,
                    initializationError = if (!success) "Failed to initialize integration" else null
                )

            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isInitialized = false,
                    isInitializing = false,
                    initializationError = "Initialization error: ${e.message}"
                )
            }
        }
    }

    /**
     * Execute a demo scenario
     */
    suspend fun executeScenario(scenario: DemoScenario) {
        if (integration == null) {
            _uiState.value = _uiState.value.copy(
                lastResult = IntegrationResult(
                    success = false,
                    error = "Integration not initialized"
                )
            )
            return
        }

        try {
            _uiState.value = _uiState.value.copy(isExecuting = true)

            val result = when {
                scenario.isPipeline -> executePipelineScenario(scenario)
                scenario.inputData != null -> {
                    // JSONata first, then JavaScript
                    integration!!.executeJSONataWithJSProcessing(
                        jsonataExpression = scenario.jsonataExpression,
                        inputData = scenario.inputData,
                        jsCode = scenario.jsCode
                    )
                }

                else -> {
                    // JavaScript first, then JSONata
                    integration!!.executeJSWithJSONataTransform(
                        jsCode = scenario.jsCode,
                        jsonataExpression = scenario.jsonataExpression,
                        inputData = scenario.inputData
                    )
                }
            }

            _uiState.value = _uiState.value.copy(
                isExecuting = false,
                lastResult = result,
                executionHistory = _uiState.value.executionHistory +
                        ExecutionHistoryItem(
                            scenarioId = scenario.id,
                            timestamp = System.currentTimeMillis(),
                            result = result
                        )
            )

        } catch (e: Exception) {
            _uiState.value = _uiState.value.copy(
                isExecuting = false,
                lastResult = IntegrationResult(
                    success = false,
                    error = "Execution error: ${e.message}"
                )
            )
        }
    }

    /**
     * Execute a pipeline scenario with multiple operations
     */
    private suspend fun executePipelineScenario(scenario: DemoScenario): IntegrationResult {
        val operations = listOf(
            // Step 1: JSONata to extract and transform basic data
            PipelineOperation(
                type = PipelineOperationType.JSONATA,
                code = """
                    {
                        "sales_data": sales,
                        "total_sales": \$\sum(sales.amount),
                        "sales_count": \$\count(sales)
                    }
                """.trimIndent(),
                description = "Extract and aggregate sales data"
            ),

            // Step 2: JavaScript to add additional processing
            PipelineOperation(
                type = PipelineOperationType.JAVASCRIPT,
                code = """
                    var data = parsedInput;
                    var enhanced = {
                        ...data,
                        processing_info: {
                            processed_at: new Date().toISOString(),
                            record_count: data.sales_count,
                            average_monthly_sales: Math.round(data.total_sales / data.sales_count)
                        },
                        analysis: {
                            has_data: data.sales_count > 0,
                            total_revenue: data.total_sales
                        }
                    };
                    IntegrationUtils.stringifyJSON(enhanced);
                """.trimIndent(),
                description = "Add processing metadata and calculations"
            ),

            // Step 3: Final JSONata transformation for reporting
            PipelineOperation(
                type = PipelineOperationType.JSONATA,
                code = """
                    {
                        "report": {
                            "title": "Sales Analytics Report",
                            "summary": {
                                "total_sales": total_sales,
                                "average_sales": processing_info.average_monthly_sales,
                                "record_count": processing_info.record_count,
                                "has_revenue": analysis.has_data
                            },
                            "generated": processing_info.processed_at
                        }
                    }
                """.trimIndent(),
                description = "Create final report structure"
            )
        )

        return integration!!.executePipeline(operations)
    }

    /**
     * Get integration statistics
     */
    fun getIntegrationStats(): IntegrationStats? {
        return integration?.getStatistics()
    }

    /**
     * Clear execution history
     */
    fun clearHistory() {
        _uiState.value = _uiState.value.copy(
            executionHistory = emptyList(),
            lastResult = null
        )
    }

    /**
     * Run integration tests to verify functionality
     */
    suspend fun runIntegrationTests() {
        val context = applicationContext
        if (integration == null || context == null) {
            _uiState.value = _uiState.value.copy(
                lastResult = IntegrationResult(
                    success = false,
                    error = "Integration not initialized or context unavailable"
                )
            )
            return
        }

        try {
            _uiState.value = _uiState.value.copy(isExecuting = true)

            val testRunner = IntegrationTest(context)
            val testResults = testRunner.runAllTests()
            val testReport = testResults.getReport()

            val testResult = IntegrationResult(
                success = testResults.overallSuccess,
                result = testReport,
                error = if (!testResults.overallSuccess) "Some tests failed" else null
            )

            _uiState.value = _uiState.value.copy(
                isExecuting = false,
                lastResult = testResult,
                executionHistory = _uiState.value.executionHistory +
                        ExecutionHistoryItem(
                            scenarioId = "integration_tests",
                            timestamp = System.currentTimeMillis(),
                            result = testResult
                        )
            )

        } catch (e: Exception) {
            _uiState.value = _uiState.value.copy(
                isExecuting = false,
                lastResult = IntegrationResult(
                    success = false,
                    error = "Test execution error: ${e.message}"
                )
            )
        }
    }

    /**
     * Run JSONata-specific tests using the test assets
     */
    suspend fun runJSONataTests() {
        if (integration == null) {
            _uiState.value = _uiState.value.copy(
                lastResult = IntegrationResult(
                    success = false,
                    error = "Integration not initialized"
                )
            )
            return
        }

        try {
            _uiState.value = _uiState.value.copy(isExecuting = true)

            val testResult = integration!!.runJSONataTests()

            _uiState.value = _uiState.value.copy(
                isExecuting = false,
                lastResult = testResult,
                executionHistory = _uiState.value.executionHistory +
                        ExecutionHistoryItem(
                            scenarioId = "jsonata_tests",
                            timestamp = System.currentTimeMillis(),
                            result = testResult
                        )
            )

        } catch (e: Exception) {
            _uiState.value = _uiState.value.copy(
                isExecuting = false,
                lastResult = IntegrationResult(
                    success = false,
                    error = "JSONata test execution error: ${e.message}"
                )
            )
        }
    }

    /**
     * Run simple V8 tests to verify basic functionality
     */
    suspend fun runSimpleV8Tests() {
        val context = applicationContext
        if (context == null) {
            _uiState.value = _uiState.value.copy(
                lastResult = IntegrationResult(
                    success = false,
                    error = "Context unavailable"
                )
            )
            return
        }

        try {
            _uiState.value = _uiState.value.copy(isExecuting = true)

            val testReport = runSimpleV8Tests(context)
            val success = testReport.contains("Overall Success: true")

            val testResult = IntegrationResult(
                success = success,
                result = testReport,
                error = if (!success) "Some V8 tests failed" else null
            )

            _uiState.value = _uiState.value.copy(
                isExecuting = false,
                lastResult = testResult,
                executionHistory = _uiState.value.executionHistory +
                        ExecutionHistoryItem(
                            scenarioId = "simple_v8_tests",
                            timestamp = System.currentTimeMillis(),
                            result = testResult
                        )
            )

        } catch (e: Exception) {
            _uiState.value = _uiState.value.copy(
                isExecuting = false,
                lastResult = IntegrationResult(
                    success = false,
                    error = "V8 test execution error: ${e.message}"
                )
            )
        }
    }

    override fun onCleared() {
        super.onCleared()
        integration?.cleanup()
    }
}

/**
 * UI State for the Integration Demo
 */
data class IntegrationDemoUiState(
    val isInitialized: Boolean = false,
    val isInitializing: Boolean = false,
    val isExecuting: Boolean = false,
    val initializationError: String? = null,
    val lastResult: IntegrationResult? = null,
    val executionHistory: List<ExecutionHistoryItem> = emptyList()
)

/**
 * Execution history item for tracking past executions
 */
data class ExecutionHistoryItem(
    val scenarioId: String,
    val timestamp: Long,
    val result: IntegrationResult
)