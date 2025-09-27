package com.dastanapps.javascriptengine

import android.content.Context
import androidx.lifecycle.lifecycleScope
import com.dastanapps.javascriptengine.jsengine.JSEngineManager
import com.dastanapps.javascriptengine.jsengine.JSExecutionResult
import com.quickjs.JavaCallback
import com.quickjs.JavaVoidCallback
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

/**
 * Example usage patterns for JSEngineManager
 * This file demonstrates various ways to integrate JavaScript execution in Android apps
 */
class JSEngineExample(private val context: Context, private val coroutineScope: CoroutineScope) {

    private val jsEngine = JSEngineManager(context)

    /**
     * Basic initialization and simple script execution
     */
    suspend fun basicExample() {
        // Initialize the engine
        val initSuccess = jsEngine.initialize()
        if (!initSuccess) {
            println("Failed to initialize JavaScript engine")
            return
        }

        // Execute simple math
        val mathResult = jsEngine.executeScript("2 + 3 * 4")
        println("Math result: ${mathResult.result}")

        // Execute string operations
        val stringResult = jsEngine.executeStringScript("\"Hello \" + \"World!\"")
        println("String result: ${stringResult.result}")

        // Execute boolean operations
        val boolResult = jsEngine.executeBooleanScript("5 > 3")
        println("Boolean result: ${boolResult.result}")
    }

    /**
     * Binding Kotlin functions to JavaScript
     */
    suspend fun bindingExample() {
        jsEngine.initialize()

        // Bind a function that returns a value
        jsEngine.bindFunction("calculateArea") { receiver, args ->
            val width = if (args.length() > 0) args.getDouble(0) else 0.0
            val height = if (args.length() > 1) args.getDouble(1) else 0.0
            width * height
        }

        // Bind a void function for side effects
        jsEngine.bindVoidFunction("logMessage") { receiver, args ->
            val message = if (args.length() > 0) args.getString(0) else "Empty message"
            println("JS Log: $message")
        }

        // Bind a Kotlin object
        val appConfig = mapOf(
            "appName" to "My Android App",
            "version" to "1.0",
            "debugMode" to true,
            "features" to listOf("JavaScript", "Native", "Hybrid")
        )
        jsEngine.bindObject("AppConfig", appConfig)

        // Use the bound functions and objects
        val script = """
            const area = calculateArea(10, 5);
            logMessage("Calculated area: " + area);
            
            const config = JSON.stringify(AppConfig, null, 2);
            logMessage("App config: " + config);
            
            area;
        """.trimIndent()

        val result = jsEngine.executeScript(script)
        println("Final result: ${result.result}")
    }

    /**
     * Loading and using external JavaScript libraries
     */
    suspend fun libraryExample() {
        jsEngine.initialize()

        // Load external library
        val libraryResult = jsEngine.loadLibraryFromAssets("sample-library.js")
        if (!libraryResult.success) {
            println("Failed to load library: ${libraryResult.error}")
            return
        }

        // Use functions from the loaded library
        val script = """
            const greeting = greetUser("Android Developer");
            const factorial = calculateFactorial(6);
            
            const numbers = [1, 2, 3, 4, 5, 6, 7, 8, 9, 10];
            const sum = processArray(numbers, 'sum');
            const average = processArray(numbers, 'average');
            
            const calc = new Calculator();
            calc.add(10, 20);
            calc.multiply(5, 6);
            const history = calc.getHistory();
            
            JSON.stringify({
                greeting: greeting,
                factorial: factorial,
                arraySum: sum,
                arrayAverage: average,
                calculatorHistory: history
            }, null, 2);
        """.trimIndent()

        val result = jsEngine.executeScript(script)
        println("Library example result: ${result.result}")
    }

    /**
     * Error handling and debugging
     */
    suspend fun errorHandlingExample() {
        jsEngine.initialize()

        // Try to execute invalid JavaScript
        val invalidResult = jsEngine.executeScript("invalid javascript syntax {")
        if (!invalidResult.success) {
            println("Expected error: ${invalidResult.error}")
        }

        // Try to call undefined function
        val undefinedResult = jsEngine.executeScript("undefinedFunction()")
        if (!undefinedResult.success) {
            println("Expected error: ${undefinedResult.error}")
        }

        // Valid script with error handling
        val safeScript = """
            try {
                const result = riskyOperation();
                result;
            } catch (error) {
                "Error caught: " + error.message;
            }
            
            function riskyOperation() {
                throw new Error("Something went wrong!");
            }
        """.trimIndent()

        val safeResult = jsEngine.executeScript(safeScript)
        println("Safe execution result: ${safeResult.result}")
    }

    /**
     * Working with complex data structures
     */
    suspend fun dataProcessingExample() {
        jsEngine.initialize()

        // Bind data processing functions
        jsEngine.bindFunction("processUserData") { receiver, args ->
            // Simulate processing user data from Kotlin/Android
            mapOf(
                "processedAt" to System.currentTimeMillis(),
                "processor" to "Android Native",
                "status" to "completed"
            )
        }

        val complexScript = """
            const users = [
                {name: "Alice", age: 25, city: "New York"},
                {name: "Bob", age: 30, city: "San Francisco"},  
                {name: "Charlie", age: 35, city: "Chicago"}
            ];
            
            // Process users with JavaScript
            const processed = users.map(user => ({
                ...user,
                ageGroup: user.age < 30 ? "young" : "mature",
                greeting: greetUser ? greetUser(user.name) : `Hello \$\{user.name\}`
            }));
            
            // Get processing metadata from Kotlin
            const metadata = processUserData();
            
            JSON.stringify({
                users: processed,
                count: processed.length,
                metadata: metadata
            }, null, 2);
        """.trimIndent()

        val result = jsEngine.executeScript(complexScript)
        println("Data processing result: ${result.result}")
    }

    /**
     * Async-like operations (simulated)
     */
    suspend fun asyncExample() {
        jsEngine.initialize()

        // Bind an async-like operation
        jsEngine.bindFunction("fetchData") { receiver, args ->
            val url = if (args.length() > 0) args.getString(0) else "default"
            // Simulate network call result
            mapOf(
                "url" to url,
                "data" to "Some fetched data",
                "timestamp" to System.currentTimeMillis(),
                "success" to true
            )
        }

        val asyncScript = """
            function simulateAsync(callback) {
                const data = fetchData("https://api.example.com/data");
                callback(data);
                return data;
            }
            
            let result = null;
            simulateAsync(function(data) {
                result = data;
                console.log("Async callback executed with:", JSON.stringify(data));
            });
            
            JSON.stringify(result, null, 2);
        """.trimIndent()

        val result = jsEngine.executeScript(asyncScript)
        println("Async example result: ${result.result}")
    }

    /**
     * Memory and performance monitoring
     */
    fun monitoringExample() {
        coroutineScope.launch {
            jsEngine.initialize()

            // Execute several operations
            jsEngine.executeScript("const data = Array(1000).fill(0).map((_, i) => i * i)")
            jsEngine.executeScript("const processed = data.filter(x => x % 2 === 0)")

            // Check memory usage
            val memoryInfo = jsEngine.getMemoryInfo()
            println("Memory info: $memoryInfo")

            // Clean up when done
            jsEngine.destroy()
        }
    }

    /**
     * Integration with Android lifecycle
     */
    fun lifecycleIntegration() {
        // Initialize in onCreate or similar
        coroutineScope.launch {
            val success = jsEngine.initialize()
            if (success) {
                println("JS Engine ready for use")
                // Set up bindings, load libraries, etc.
            }
        }

        // Clean up in onDestroy
        // jsEngine.destroy() - call this in Activity.onDestroy()
    }
}

/**
 * Usage in Activity or Fragment
 */
class ExampleUsage {

    fun useInActivity(context: Context, coroutineScope: CoroutineScope) {
        val example = JSEngineExample(context, coroutineScope)

        coroutineScope.launch {
            // Run different examples
            example.basicExample()
            example.bindingExample()
            example.libraryExample()
            example.errorHandlingExample()
            example.dataProcessingExample()
            example.asyncExample()
            example.monitoringExample()
        }
    }
}