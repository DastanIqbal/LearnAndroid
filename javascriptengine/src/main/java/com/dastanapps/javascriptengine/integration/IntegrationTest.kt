package com.dastanapps.javascriptengine.integration

import android.content.Context
import android.util.Log
import kotlinx.coroutines.runBlocking

/**
 * Simple test class to verify JS-JSONata integration functionality
 * This can be used to test the integration programmatically
 */
class IntegrationTest(private val context: Context) {

    companion object {
        private const val TAG = "IntegrationTest"
    }

    /**
     * Run all basic integration tests
     */
    suspend fun runAllTests(): TestResults {
        val results = mutableListOf<TestResult>()

        Log.i(TAG, "Starting integration tests...")

        // Initialize integration
        val integration = JSJSONataIntegration.getInstance(context)
        val initSuccess = integration.initialize()

        if (!initSuccess) {
            return TestResults(
                tests = listOf(
                    TestResult(
                        "initialization",
                        false,
                        "Failed to initialize integration"
                    )
                ),
                overallSuccess = false
            )
        }

        // Test 1: JavaScript to JSONata
        results.add(testJavaScriptToJSONata(integration))

        // Test 2: JSONata to JavaScript  
        results.add(testJSONataToJavaScript(integration))

        // Test 3: Pipeline processing
        results.add(testPipelineProcessing(integration))

        // Test 4: Error handling
        results.add(testErrorHandling(integration))

        // Test 5: Performance and caching
        results.add(testPerformanceAndCaching(integration))

        // Cleanup
        integration.cleanup()

        val overallSuccess = results.all { it.passed }
        Log.i(TAG, "Integration tests completed. Overall success: $overallSuccess")

        return TestResults(results, overallSuccess)
    }

    /**
     * Test JavaScript execution followed by JSONata transformation
     */
    private suspend fun testJavaScriptToJSONata(integration: JSJSONataIntegration): TestResult {
        return try {
            val jsCode = """
                var data = {
                    numbers: [1, 2, 3, 4, 5],
                    timestamp: new Date().toISOString()
                };
                IntegrationUtils.stringifyJSON(data);
            """

            val jsonataExpression = """
                {
                    "sum": ${'$'}sum(numbers),
                    "count": ${'$'}count(numbers),
                    "average": ${'$'}round(${'$'}sum(numbers) / ${'$'}count(numbers), 2),
                    "processed_at": timestamp
                }
            """

            val result = integration.executeJSWithJSONataTransform(jsCode, jsonataExpression)

            if (result.success && result.result?.contains("sum") == true) {
                TestResult("js_to_jsonata", true, "Successfully executed JS → JSONata")
            } else {
                TestResult("js_to_jsonata", false, "Failed: ${result.error}")
            }
        } catch (e: Exception) {
            TestResult("js_to_jsonata", false, "Exception: ${e.message}")
        }
    }

    /**
     * Test JSONata transformation followed by JavaScript processing
     */
    private suspend fun testJSONataToJavaScript(integration: JSJSONataIntegration): TestResult {
        return try {
            val inputData = """
                {
                    "products": [
                        {"name": "Laptop", "price": 1000},
                        {"name": "Mouse", "price": 50},
                        {"name": "Keyboard", "price": 100}
                    ]
                }
            """

            val jsonataExpression = """
                {
                    "filtered_products": products[price > 75].{
                        "item": name,
                        "cost": price
                    }
                }
            """

            val jsCode = """
                var data = parsedInput;
                var summary = {
                    items: data.filtered_products,
                    count: data.filtered_products.length,
                    total_cost: data.filtered_products.reduce((sum, item) => sum + item.cost, 0)
                };
                IntegrationUtils.stringifyJSON(summary);
            """

            val result =
                integration.executeJSONataWithJSProcessing(jsonataExpression, inputData, jsCode)

            if (result.success && result.result?.contains("total_cost") == true) {
                TestResult("jsonata_to_js", true, "Successfully executed JSONata → JS")
            } else {
                TestResult("jsonata_to_js", false, "Failed: ${result.error}")
            }
        } catch (e: Exception) {
            TestResult("jsonata_to_js", false, "Exception: ${e.message}")
        }
    }

    /**
     * Test pipeline processing with multiple operations
     */
    private suspend fun testPipelineProcessing(integration: JSJSONataIntegration): TestResult {
        return try {
            val operations = listOf(
                PipelineOperation(
                    type = PipelineOperationType.JSONATA,
                    code = """
                        {
                            "processed_numbers": [1, 2, 3, 4, 5],
                            "operation": "initial"
                        }
                    """,
                    description = "Initialize data"
                ),
                PipelineOperation(
                    type = PipelineOperationType.JAVASCRIPT,
                    code = """
                        var data = parsedInput;
                        var result = {
                            ...data,
                            doubled: data.processed_numbers.map(n => n * 2),
                            operation: "doubled"
                        };
                        IntegrationUtils.stringifyJSON(result);
                    """,
                    description = "Double the numbers"
                ),
                PipelineOperation(
                    type = PipelineOperationType.JSONATA,
                    code = """
                        {
                            "final_sum": doubled[0] + doubled[1] + doubled[2] + doubled[3] + doubled[4],
                            "original_sum": processed_numbers[0] + processed_numbers[1] + processed_numbers[2] + processed_numbers[3] + processed_numbers[4],
                            "operations": ["initial", "doubled", "summed"]
                        }
                    """,
                    description = "Calculate sums"
                )
            )

            val result = integration.executePipeline(operations)

            if (result.success && result.result?.contains("final_sum") == true) {
                TestResult("pipeline", true, "Successfully executed pipeline")
            } else {
                TestResult("pipeline", false, "Failed: ${result.error}")
            }
        } catch (e: Exception) {
            TestResult("pipeline", false, "Exception: ${e.message}")
        }
    }

    /**
     * Test error handling with invalid code
     */
    private suspend fun testErrorHandling(integration: JSJSONataIntegration): TestResult {
        return try {
            val invalidJsCode = "var x = { invalid: syntax error"
            val validJsonataExpression = "{ \"test\": \"value\" }"

            val result =
                integration.executeJSWithJSONataTransform(invalidJsCode, validJsonataExpression)

            if (!result.success && result.error != null) {
                TestResult("error_handling", true, "Properly handled JS syntax error")
            } else {
                TestResult("error_handling", false, "Should have failed with syntax error")
            }
        } catch (e: Exception) {
            TestResult("error_handling", true, "Exception properly caught: ${e.message}")
        }
    }

    /**
     * Test performance and caching functionality
     */
    private suspend fun testPerformanceAndCaching(integration: JSJSONataIntegration): TestResult {
        return try {
            val jsonataExpression = """{ "test": "cached_expression", "value": 42 }"""
            val jsCode = """IntegrationUtils.stringifyJSON({ "from_js": true });"""

            // Execute the same operation multiple times to test caching
            val startTime = System.currentTimeMillis()

            repeat(3) {
                integration.executeJSWithJSONataTransform(jsCode, jsonataExpression)
            }

            val endTime = System.currentTimeMillis()
            val totalTime = endTime - startTime

            val stats = integration.getStatistics()

            if (stats.isInitialized && totalTime < 5000) { // Should complete within 5 seconds
                TestResult("performance", true, "Performance test passed in ${totalTime}ms")
            } else {
                TestResult("performance", false, "Performance test failed or took too long")
            }
        } catch (e: Exception) {
            TestResult("performance", false, "Exception: ${e.message}")
        }
    }
}

/**
 * Individual test result
 */
data class TestResult(
    val testName: String,
    val passed: Boolean,
    val message: String
)

/**
 * Overall test results
 */
data class TestResults(
    val tests: List<TestResult>,
    val overallSuccess: Boolean
) {
    fun getReport(): String {
        val report = StringBuilder()
        report.appendLine("Integration Test Report")
//        report.appendLine("=" * 40)
        report.appendLine("Overall Success: $overallSuccess")
        report.appendLine("Tests Passed: ${tests.count { it.passed }}/${tests.size}")
        report.appendLine()

        tests.forEach { test ->
            val status = if (test.passed) "✓ PASS" else "✗ FAIL"
            report.appendLine("$status - ${test.testName}: ${test.message}")
        }

        return report.toString()
    }
}

/**
 * Utility function to run tests and get report
 */
suspend fun runIntegrationTests(context: Context): String {
    val tester = IntegrationTest(context)
    val results = tester.runAllTests()
    return results.getReport()
}