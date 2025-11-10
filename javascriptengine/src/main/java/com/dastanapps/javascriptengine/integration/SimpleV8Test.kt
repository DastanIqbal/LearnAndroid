package com.dastanapps.javascriptengine.integration

import android.content.Context
import android.util.Log
import com.eclipsesource.v8.V8
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject

/**
 * Simple test class to verify V8 JavaScript engine functionality
 * This tests basic V8 operations before testing JSONata integration
 */
class SimpleV8Test(private val context: Context) {

    companion object {
        private const val TAG = "SimpleV8Test"
    }

    /**
     * Run basic V8 tests
     */
    suspend fun runBasicTests(): TestResult {
        return withContext(Dispatchers.Main) {
            try {
                Log.i(TAG, "Starting basic V8 tests...")

                // Test 1: Create V8 engine
                val v8 = V8.createV8Runtime()
                Log.i(TAG, "✓ V8 engine created successfully")

                // Test 2: Execute simple JavaScript
                val result1 = v8.executeScript("2 + 3") as Int
                Log.i(TAG, "✓ Simple math: 2 + 3 = $result1")

                // Test 3: Execute JavaScript with variables
                v8.executeScript("var message = 'Hello from V8!';")
                val result2 = v8.executeScript("message") as String
                Log.i(TAG, "✓ Variable access: $result2")

                // Test 4: Execute JavaScript with objects
                val result3 =
                    v8.executeScript("JSON.stringify({name: 'Test', value: 42})") as String
                Log.i(TAG, "✓ JSON stringify: $result3")

                // Test 5: Execute JavaScript with functions
                v8.executeScript(
                    """
                    function multiply(a, b) {
                        return a * b;
                    }
                """
                )
                val result4 = v8.executeScript("multiply(6, 7)") as Int
                Log.i(TAG, "✓ Function call: 6 * 7 = $result4")

                // Test 6: Test JSON parsing
                v8.executeScript("var data = JSON.parse('{\"test\": true, \"number\": 123}');")
                val result5 = v8.executeScript("data.test") as Boolean
                val result6 = v8.executeScript("data.number") as Int
                Log.i(TAG, "✓ JSON parse: test=$result5, number=$result6")

                // Cleanup
                v8.close()
                Log.i(TAG, "✓ V8 engine closed successfully")

                TestResult(
                    success = true,
                    message = "All V8 basic tests passed",
                    details = mapOf(
                        "math" to result1.toString(),
                        "string" to result2,
                        "json" to result3,
                        "function" to result4.toString(),
                        "parse_boolean" to result5.toString(),
                        "parse_number" to result6.toString()
                    )
                )

            } catch (e: Exception) {
                Log.e(TAG, "V8 test failed", e)
                TestResult(
                    success = false,
                    message = "V8 test failed: ${e.message}",
                    details = emptyMap()
                )
            }
        }
    }

    /**
     * Test JSONata with actual test files (js-ex.json & js-ex-script.js)
     */
    suspend fun testJSONataWithTestFiles(): TestResult {
        return withContext(Dispatchers.Main) {
            try {
                Log.i(TAG, "Testing JSONata with actual test files...")

                // Create V8 engine
                val v8 = V8.createV8Runtime()

                // Load JSONata.js from assets
                val jsonataCode =
                    context.assets.open("js/jsonata-es5.js").bufferedReader().use { it.readText() }
                v8.executeScript(jsonataCode)
                Log.i(TAG, "✓ JSONata.js loaded successfully")

                // Load test data from js-ex.json
                val testDataJson =
                    context.assets.open("jsonata-ex.json").bufferedReader().use { it.readText() }
                v8.executeScript("var testData = $testDataJson;")
                Log.i(TAG, "✓ js-ex.json loaded successfully")

                // Load and execute test script from js-ex-script.js
                val testScript = context.assets.open("jsonata-ex-script.js").bufferedReader()
                    .use { it.readText() }
                v8.executeScript(testScript)
                Log.i(TAG, "✓ js-ex-script.js loaded successfully")

                // Test simple JSONata expressions on the test data
                val tests = mutableMapOf<String, String>()

                // Test 1: Access nested data
                val test1 = v8.executeScript("JSON.stringify(testData.data.users[0])") as String
                tests["first_user"] = test1
                Log.i(TAG, "✓ First user: $test1")

                // Test 2: Count users
                val test2 = v8.executeScript(
                    """
                    var js = new JSONata('data.users');
                    var users = js.evaluate(testData);
                    JSON.stringify({count: users.length, first_name: users[0].name});
                """
                ) as String
                tests["user_count"] = test2
                Log.i(TAG, "✓ User count test: $test2")

                // Test 3: Simple JSONata expression
                val test3 = v8.executeScript(
                    """
                    var expr = new JSONata('data.totalCount');
                    JSON.stringify({totalCount: expr.evaluate(testData)});
                """
                ) as String
                tests["total_count"] = test3
                Log.i(TAG, "✓ Total count: $test3")

                // Test 4: Product filtering
                val test4 = v8.executeScript(
                    """
                    var products = testData.data.products;
                    var filtered = products.filter(p => p.price > 100);
                    JSON.stringify({highValueProducts: filtered.length, total: products.length});
                """
                ) as String
                tests["product_filter"] = test4
                Log.i(TAG, "✓ Product filter: $test4")

                // Test 5: Run the test script function if it exists
                val test5 = try {
                    val scriptResult = v8.executeScript(
                        """
                        if (typeof runJSONataTests === 'function') {
                            JSON.stringify(runJSONataTests(testData));
                        } else {
                            JSON.stringify({error: 'runJSONataTests function not found'});
                        }
                    """
                    ) as String
                    tests["script_execution"] = scriptResult
                    Log.i(TAG, "✓ Test script execution: ${scriptResult.take(100)}...")
                    scriptResult
                } catch (e: Exception) {
                    val errorMsg = "Script execution failed: ${e.message}"
                    tests["script_execution"] = errorMsg
                    Log.w(TAG, "⚠ Test script execution failed: ${e.message}")
                    errorMsg
                }

                // Cleanup
                v8.close()

                TestResult(
                    success = true,
                    message = "JSONata test files evaluation completed",
                    details = tests
                )

            } catch (e: Exception) {
                Log.e(TAG, "JSONata test files evaluation failed", e)
                TestResult(
                    success = false,
                    message = "JSONata test files failed: ${e.message}",
                    details = emptyMap()
                )
            }
        }
    }

    /**
     * Test data class for results
     */
    data class TestResult(
        val success: Boolean,
        val message: String,
        val details: Map<String, String>
    )

    suspend fun runJsonataTest(): TestResult {
        val tests = mutableMapOf<String, String>()
        var runtime:V8?=null
        return try {
            runtime = V8.createV8Runtime()
            runtime.registerJavaMethod({ _, params -> Log.i("JS", params.get(0).toString()) }, "log")
            runtime.registerJavaMethod({ _, params -> Log.e("JS", params.get(0).toString()) }, "error")
            runtime.executeVoidScript("var console = { log: log, error: error };")

            val jsonataScript = context.assets.open("js/jsonata-es5.js").bufferedReader().use { it.readText() }
            runtime.executeVoidScript(jsonataScript)

            val isLoaded = runtime.executeBooleanScript("typeof jsonata !== 'undefined';")
            Log.i(TAG, "JSONata loaded: $isLoaded")
            tests["load_script"]=  if(isLoaded) "pass" else "fail"
            Log.i(TAG, "✓ JSONata.js loaded "+tests["load_script"])

            val jsonText = context.assets.open("test-data.json").bufferedReader().use { it.readText() }.trim()
            val exprText = context.assets.open("test-expr.js").bufferedReader().use { it.readText() }.trim()

            val escapedJson = JSONObject.quote(jsonText)
            val escapedExpr = exprText.replace("'", "\\'").replace("\n", "")

            val script = """
    (function() {
        try {
            var expr = jsonata('${'$'}sum(products.price)');
            var data = JSON.parse($escapedJson);
            var result = expr.evaluate(data);
            console.log('raw:', result);
            console.log('Result:', JSON.stringify(result));
            return JSON.stringify(result);
        } catch (e) {
            console.error('JS Error -> ' + (e && e.stack ? e.stack : e));
            return null;
        }
    })();
""".trimIndent()

            val testJs = """
try {
  var expr = jsonata('sum([1,2,3])');
  var result = expr.evaluate({});
  console.log('Result = ' + result);
  result;
} catch (e) {
  console.error('JS Error -> ' + (e && e.stack ? e.stack : e));
}
""".trimIndent()

            val result = runtime.executeStringScript(testJs)
            Log.i(TAG, "Result = ${result.toString()}")
            tests["jsonata_script"]= "$result"
            TestResult(
                success = true,
                message = "JSONata test files evaluation completed",
                details = tests
            )
        }catch (e: Exception){
            e.printStackTrace()
            TestResult(
                success = false,
                message = "JSONata test files evaluation completed",
                details = tests
            )
        }finally {
            runtime?.release()
        }
    }
}

/**
 * Utility function to run simple V8 tests with actual test files
 */
suspend fun runSimpleV8Tests(context: Context): String {
    val tester = SimpleV8Test(context)

//    val basicTest = tester.runBasicTests()
    val jsonataFilesTest = tester.runJsonataTest()

    return buildString {
        appendLine("=== Simple V8 Test Results ===")
        appendLine()

//        appendLine("Basic V8 Test:")
//        appendLine("Success: ${basicTest.success}")
//        appendLine("Message: ${basicTest.message}")
//        basicTest.details.forEach { (key, value) ->
//            appendLine("  $key: $value")
//        }
//        appendLine()

        appendLine("JSONata Files Test:")
        appendLine("Success: ${jsonataFilesTest.success}")
        appendLine("Message: ${jsonataFilesTest.message}")
        jsonataFilesTest.details.forEach { (key, value) ->
            val truncatedValue = if (value.length > 100) value.take(100) + "..." else value
            appendLine("  $key: $truncatedValue")
        }
        appendLine()

//        val overallSuccess = basicTest.success && jsonataFilesTest.success
//        appendLine("Overall Success: $overallSuccess")
    }
}