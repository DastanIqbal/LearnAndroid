package com.dastanapps.javascriptengine.es6promises

import android.app.Application
import android.util.Log
import androidx.javascriptengine.JavaScriptIsolate
import androidx.javascriptengine.JavaScriptSandbox
import com.google.common.util.concurrent.FutureCallback
import com.google.common.util.concurrent.Futures
import com.google.common.util.concurrent.ListenableFuture
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

/**
 * Executes ES6 and Promise tests using AndroidX JavaScriptEngine
 */
class ES6TestExecutor(private val application: Application) {

    companion object {
        private const val TAG = "ES6TestExecutor"
        private const val JS_EXECUTION_TIMEOUT_SECONDS = 5L
    }

    private var javaScriptSandbox: JavaScriptSandbox? = null
    private var jsIsolate: JavaScriptIsolate? = null
    private val executor = Executors.newSingleThreadExecutor()

    /**
     * Load JavaScript file from assets
     */
    private fun loadAssetFile(filename: String): String {
        return try {
            application.assets.open(filename).bufferedReader().use { it.readText() }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to load asset file: $filename", e)
            "console.log('Failed to load asset file: $filename');"
        }
    }

    /**
     * Initialize the JavaScript engines
     */
    private suspend fun initializeJavaScriptEngine(): Boolean {
        return suspendCancellableCoroutine { continuation ->
            try {
                val sandboxFuture = JavaScriptSandbox.createConnectedInstanceAsync(application)

                Futures.addCallback(sandboxFuture, object : FutureCallback<JavaScriptSandbox> {
                    override fun onSuccess(sandbox: JavaScriptSandbox?) {
                        if (sandbox != null) {
                            javaScriptSandbox = sandbox
                            jsIsolate = sandbox.createIsolate()
                            continuation.resume(true)
                        } else {
                            continuation.resume(false)
                        }
                    }

                    override fun onFailure(t: Throwable) {
                        Log.e(TAG, "Failed to initialize JavaScript sandbox", t)
                        continuation.resumeWithException(t)
                    }
                }, executor)

            } catch (e: Exception) {
                Log.e(TAG, "Error initializing JavaScript engine", e)
                continuation.resumeWithException(e)
            }
        }
    }

    /**
     * Get all available test cases
     */
    fun getAllTestCases(): List<JSTestCase> {
        return listOf(
            // Comprehensive Test Suite from Assets
            JSTestCase(
                name = "Comprehensive ES6 Test Suite",
                description = "Run comprehensive ES6 and Promise tests from assets file",
                jsCode = loadAssetFile("es6-promise-tests.js"),
                category = "Comprehensive"
            ),

            // Arrow Functions Tests
            JSTestCase(
                name = "Basic Arrow Function",
                description = "Test basic arrow function syntax",
                jsCode = """
                    const add = (a, b) => a + b;
                    const result = add(5, 3);
                    result.toString();
                """.trimIndent(),
                category = "Arrow Functions"
            ),

            JSTestCase(
                name = "Arrow Function with Block Body",
                description = "Test arrow function with block body",
                jsCode = """
                    const multiply = (a, b) => {
                        const result = a * b;
                        return result;
                    };
                    multiply(4, 6).toString();
                """.trimIndent(),
                category = "Arrow Functions"
            ),

            // Template Literals Tests
            JSTestCase(
                name = "Basic Template Literal",
                description = "Test basic template literal syntax",
                jsCode = """
                    const name = "World";
                    const greeting = `Hello, ${'$'}{name}!`;
                    greeting;
                """.trimIndent(),
                category = "Template Literals"
            ),

            JSTestCase(
                name = "Multi-line Template Literal",
                description = "Test multi-line template literals",
                jsCode = """
                    const message = `This is a
                    multi-line
                    template literal`;
                    message.includes("multi-line").toString();
                """.trimIndent(),
                category = "Template Literals"
            ),

            // Destructuring Tests
            JSTestCase(
                name = "Array Destructuring",
                description = "Test array destructuring assignment",
                jsCode = """
                    const arr = [1, 2, 3, 4, 5];
                    const [first, second, ...rest] = arr;
                    (first + second + rest.length).toString();
                """.trimIndent(),
                category = "Destructuring"
            ),

            JSTestCase(
                name = "Object Destructuring",
                description = "Test object destructuring assignment",
                jsCode = """
                    const obj = { x: 10, y: 20, z: 30 };
                    const { x, y } = obj;
                    (x + y).toString();
                """.trimIndent(),
                category = "Destructuring"
            ),

            // Promise Tests
            JSTestCase(
                name = "Basic Promise",
                description = "Test basic Promise creation and resolution",
                jsCode = """
                    const promise1 = new Promise((resolve, reject) => {
                        resolve("Success!");
                    });
                    
                    // Return promise status for testing
                    "Promise created successfully";
                """.trimIndent(),
                category = "Promises"
            ),

            JSTestCase(
                name = "Promise.resolve",
                description = "Test Promise.resolve method",
                jsCode = """
                    const result1 = Promise.resolve("Resolved value");
                    "Promise.resolve created successfully";
                """.trimIndent(),
                category = "Promises"
            ),

            // Classes Tests
            JSTestCase(
                name = "Basic Class",
                description = "Test ES6 class syntax",
                jsCode = """
                    class Person {
                        constructor(name, age) {
                            this.name = name;
                            this.age = age;
                        }
                        
                        greet() {
                            return `Hello, I'm ${'$'}{this.name}`;
                        }
                    }
                    
                    const person = new Person("Alice", 30);
                    person.greet();
                """.trimIndent(),
                category = "Classes"
            ),

            JSTestCase(
                name = "Class Inheritance",
                description = "Test class inheritance with extends",
                jsCode = """
                    class Animal {
                        constructor(name) {
                            this.name = name;
                        }
                        
                        speak() {
                            return `${'$'}{this.name} makes a sound`;
                        }
                    }
                    
                    class Dog extends Animal {
                        speak() {
                            return `${'$'}{this.name} barks`;
                        }
                    }
                    
                    const dog = new Dog("Rex");
                    dog.speak();
                """.trimIndent(),
                category = "Classes"
            ),

            // Spread Operator Tests
            JSTestCase(
                name = "Array Spread",
                description = "Test spread operator with arrays",
                jsCode = """
                    const arr1 = [1, 2, 3];
                    const arr2 = [4, 5, 6];
                    const combined = [...arr1, ...arr2];
                    combined.length.toString();
                """.trimIndent(),
                category = "Spread Operator"
            ),

            JSTestCase(
                name = "Object Spread",
                description = "Test spread operator with objects",
                jsCode = """
                    const obj1 = { a: 1, b: 2 };
                    const obj2 = { c: 3, d: 4 };
                    const combined1 = { ...obj1, ...obj2 };
                    Object.keys(combined1).length.toString();
                """.trimIndent(),
                category = "Spread Operator"
            ),

            // Default Parameters Tests
            JSTestCase(
                name = "Default Parameters",
                description = "Test function default parameters",
                jsCode = """
                    function greet(name = "World", greeting = "Hello") {
                        return `${'$'}{greeting}, ${'$'}{name}!`;
                    }
                    
                    greet();
                """.trimIndent(),
                category = "Default Parameters"
            ),

            // For...of Loop Tests
            JSTestCase(
                name = "For...of with Array",
                description = "Test for...of loop with arrays",
                jsCode = """
                    const numbers = [1, 2, 3, 4, 5];
                    let sum = 0;
                    for (const num of numbers) {
                        sum += num;
                    }
                    sum.toString();
                """.trimIndent(),
                category = "For...of Loops"
            ),

            // Additional ES6 Features
            JSTestCase(
                name = "Let and Const",
                description = "Test let and const declarations",
                jsCode = """
                    let mutableVar = 10;
                    const immutableVar = 20;
                    mutableVar += immutableVar;
                    mutableVar.toString();
                """.trimIndent(),
                category = "Variable Declarations"
            ),

            JSTestCase(
                name = "Map and Set",
                description = "Test ES6 Map and Set collections",
                jsCode = """
                    const map = new Map();
                    map.set('key1', 'value1');
                    map.set('key2', 'value2');
                    
                    const set = new Set([1, 2, 3, 3, 4]);
                    
                    `Map size: ${'$'}{map.size}, Set size: ${'$'}{set.size}`;
                """.trimIndent(),
                category = "Collections"
            )
        )
    }

    /**
     * Get test cases filtered by category
     */
    fun getTestCasesByCategory(category: String): List<JSTestCase> {
        return getAllTestCases().filter { it.category == category }
    }

    /**
     * Execute a test case using AndroidX JavaScriptEngine
     */
    suspend fun executeTestWithAndroidXJSEngine(testCase: JSTestCase): TestResult {
        return suspendCancellableCoroutine { continuation ->
            try {
                // Initialize if needed
                if (jsIsolate == null) {
                    Futures.addCallback(
                        JavaScriptSandbox.createConnectedInstanceAsync(application),
                        object : FutureCallback<JavaScriptSandbox> {
                            override fun onSuccess(sandbox: JavaScriptSandbox?) {
                                if (sandbox != null) {
                                    javaScriptSandbox = sandbox
                                    jsIsolate = sandbox.createIsolate()
                                    executeJavaScript(testCase, continuation)
                                } else {
                                    continuation.resume(
                                        TestResult(
                                            testName = testCase.name,
                                            description = testCase.description,
                                            engine = "AndroidX JS Engine",
                                            status = TestStatus.FAILED,
                                            error = "Failed to create sandbox"
                                        )
                                    )
                                }
                            }

                            override fun onFailure(t: Throwable) {
                                continuation.resume(
                                    TestResult(
                                        testName = testCase.name,
                                        description = testCase.description,
                                        engine = "AndroidX JS Engine",
                                        status = TestStatus.FAILED,
                                        error = t.message ?: "Sandbox initialization failed"
                                    )
                                )
                            }
                        },
                        executor
                    )
                } else {
                    executeJavaScript(testCase, continuation)
                }

            } catch (e: Exception) {
                Log.e(TAG, "AndroidX JS Engine execution failed for ${testCase.name}", e)
                continuation.resume(
                    TestResult(
                        testName = testCase.name,
                        description = testCase.description,
                        engine = "AndroidX JS Engine",
                        status = TestStatus.FAILED,
                        error = e.message ?: "Unknown error"
                    )
                )
            }
        }
    }

    private fun executeJavaScript(
        testCase: JSTestCase,
        continuation: kotlin.coroutines.Continuation<TestResult>
    ) {
        try {
            val startTime = System.currentTimeMillis()
            val evaluationFuture = jsIsolate?.evaluateJavaScriptAsync(testCase.jsCode)

            if (evaluationFuture != null) {
                Futures.addCallback(evaluationFuture, object : FutureCallback<String> {
                    override fun onSuccess(result: String?) {
                        val executionTime = System.currentTimeMillis() - startTime
                        continuation.resume(
                            TestResult(
                                testName = testCase.name,
                                description = testCase.description,
                                engine = "AndroidX JS Engine",
                                status = TestStatus.SUCCESS,
                                output = result ?: "null",
                                executionTime = executionTime
                            )
                        )
                    }

                    override fun onFailure(t: Throwable) {
                        val executionTime = System.currentTimeMillis() - startTime
                        continuation.resume(
                            TestResult(
                                testName = testCase.name,
                                description = testCase.description,
                                engine = "AndroidX JS Engine",
                                status = TestStatus.FAILED,
                                error = t.message ?: "Execution failed",
                                executionTime = executionTime
                            )
                        )
                    }
                }, executor)
            } else {
                continuation.resume(
                    TestResult(
                        testName = testCase.name,
                        description = testCase.description,
                        engine = "AndroidX JS Engine",
                        status = TestStatus.FAILED,
                        error = "Failed to create evaluation future"
                    )
                )
            }
        } catch (e: Exception) {
            continuation.resume(
                TestResult(
                    testName = testCase.name,
                    description = testCase.description,
                    engine = "AndroidX JS Engine",
                    status = TestStatus.FAILED,
                    error = e.message ?: "Execution setup failed"
                )
            )
        }
    }

    /**
     * Clean up resources
     */
    fun cleanup() {
        try {
            jsIsolate?.close()
            jsIsolate = null
        } catch (e: Exception) {
            Log.w(TAG, "Error closing JS isolate", e)
        }

        try {
            javaScriptSandbox?.close()
            javaScriptSandbox = null
        } catch (e: Exception) {
            Log.w(TAG, "Error closing JS sandbox", e)
        }

        try {
            executor.shutdown()
        } catch (e: Exception) {
            Log.w(TAG, "Error shutting down executor", e)
        }
    }
}