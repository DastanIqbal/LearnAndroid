# JavaScript Engine for Android

A powerful JavaScript execution engine for Android using QuickJS, providing native JavaScript
execution without WebView dependency.

## Features

- ✅ **Lightweight**: Uses QuickJS - fast and memory-efficient JavaScript engine
- ✅ **No WebView Dependency**: Pure native JavaScript execution
- ✅ **Kotlin/Java Binding**: Bind Android functions and objects to JavaScript
- ✅ **Async Support**: Coroutine-based execution with proper threading
- ✅ **External Libraries**: Load JavaScript libraries from assets
- ✅ **Memory Management**: Built-in memory monitoring and cleanup
- ✅ **Error Handling**: Comprehensive error handling and debugging support
- ✅ **Modern UI**: Jetpack Compose demo interface

## Quick Start

### 1. Initialize the Engine

```kotlin
class MainActivity : ComponentActivity() {
    private lateinit var jsEngine: JSEngineManager
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Initialize JavaScript Engine
        jsEngine = JSEngineManager(this)
        
        lifecycleScope.launch {
            val success = jsEngine.initialize()
            if (success) {
                // Engine is ready to use
                executeJavaScript()
            }
        }
    }
    
    override fun onDestroy() {
        super.onDestroy()
        jsEngine.destroy() // Important: Clean up resources
    }
}
```

### 2. Execute JavaScript Code

```kotlin
// Basic script execution
suspend fun executeJavaScript() {
    // Simple math
    val mathResult = jsEngine.executeScript("2 + 3 * 4")
    println("Result: ${mathResult.result}") // Output: 14
    
    // String operations
    val stringResult = jsEngine.executeStringScript("\"Hello \" + \"World!\"")
    println("String: ${stringResult.result}") // Output: Hello World!
    
    // Complex operations
    val complexScript = """
        const users = [{name: "Alice", age: 25}, {name: "Bob", age: 30}];
        users.filter(user => user.age > 26).map(user => user.name);
    """.trimIndent()
    
    val result = jsEngine.executeScript(complexScript)
    println("Filtered users: ${result.result}") // Output: ["Bob"]
}
```

### 3. Bind Kotlin Functions

```kotlin
suspend fun setupBindings() {
    jsEngine.initialize()
    
    // Bind a function that returns a value
    jsEngine.bindFunction("multiply") { receiver, args ->
        val a = if (args.length() > 0) args.getDouble(0) else 0.0
        val b = if (args.length() > 1) args.getDouble(1) else 0.0
        a * b
    }
    
    // Bind a void function for side effects
    jsEngine.bindVoidFunction("showToast") { receiver, args ->
        val message = if (args.length() > 0) args.getString(0) else "Hello!"
        // Show Android toast or log message
        println("Toast: $message")
    }
    
    // Bind Kotlin objects
    val appData = mapOf(
        "appName" to "My App",
        "version" to "1.0.0",
        "features" to listOf("JavaScript", "Native", "Hybrid")
    )
    jsEngine.bindObject("AppData", appData)
    
    // Use in JavaScript
    val script = """
        const result = multiply(5, 7);
        showToast("Result is: " + result);
        
        const appInfo = JSON.stringify(AppData, null, 2);
        console.log("App Info:", appInfo);
        
        result;
    """.trimIndent()
    
    val executionResult = jsEngine.executeScript(script)
    println("Execution result: ${executionResult.result}") // Output: 35
}
```

### 4. Load External JavaScript Libraries

Create a JavaScript file in `src/main/assets/`:

```javascript
// assets/my-library.js
function calculateFactorial(n) {
    if (n <= 1) return 1;
    return n * calculateFactorial(n - 1);
}

class DataProcessor {
    static processArray(arr, operation) {
        switch(operation) {
            case 'sum': return arr.reduce((a, b) => a + b, 0);
            case 'average': return arr.reduce((a, b) => a + b, 0) / arr.length;
            case 'max': return Math.max(...arr);
            default: return arr;
        }
    }
}

"Library loaded successfully!";
```

Load and use the library:

```kotlin
suspend fun useExternalLibrary() {
    jsEngine.initialize()
    
    // Load the library
    val libraryResult = jsEngine.loadLibraryFromAssets("my-library.js")
    if (!libraryResult.success) {
        println("Failed to load library: ${libraryResult.error}")
        return
    }
    
    // Use library functions
    val script = """
        const factorial = calculateFactorial(5);
        const numbers = [1, 2, 3, 4, 5];
        const sum = DataProcessor.processArray(numbers, 'sum');
        
        JSON.stringify({
            factorial: factorial,
            sum: sum
        });
    """.trimIndent()
    
    val result = jsEngine.executeScript(script)
    println("Library result: ${result.result}")
}
```

## Advanced Usage

### Error Handling

```kotlin
suspend fun handleErrors() {
    // Execute script with error handling
    val result = jsEngine.executeScript("invalid.syntax(")
    
    if (result.success) {
        println("Success: ${result.result}")
    } else {
        println("Error: ${result.error}")
    }
    
    // JavaScript-side error handling
    val safeScript = """
        try {
            riskyOperation();
        } catch (error) {
            "Error caught: " + error.message;
        }
        
        function riskyOperation() {
            throw new Error("Something went wrong!");
        }
    """.trimIndent()
    
    val safeResult = jsEngine.executeScript(safeScript)
    println("Safe execution: ${safeResult.result}")
}
```

### Memory Management

```kotlin
fun monitorMemory() {
    // Get memory information
    val memoryInfo = jsEngine.getMemoryInfo()
    println("Total objects: ${memoryInfo.totalObjects}")
    println("JS objects: ${memoryInfo.jsObjectsCount}")
    println("Functions: ${memoryInfo.functionsCount}")
    
    // Clean up when done (automatically called in onDestroy)
    jsEngine.destroy()
}
```

### Complex Data Processing

```kotlin
suspend fun processComplexData() {
    jsEngine.initialize()
    
    // Bind data processing function
    jsEngine.bindFunction("processUserData") { receiver, args ->
        // Simulate Android data processing
        mapOf(
            "processedAt" to System.currentTimeMillis(),
            "processor" to "Android Native",
            "success" to true
        )
    }
    
    val dataScript = """
        const users = [
            {name: "Alice", age: 25, city: "New York"},
            {name: "Bob", age: 30, city: "San Francisco"},
            {name: "Charlie", age: 35, city: "Chicago"}
        ];
        
        // JavaScript processing
        const processed = users
            .filter(user => user.age >= 30)
            .map(user => ({
                ...user,
                ageGroup: user.age < 35 ? "adult" : "senior",
                displayName: user.name.toUpperCase()
            }));
            
        // Get metadata from Android
        const metadata = processUserData();
        
        JSON.stringify({
            filteredUsers: processed,
            count: processed.length,
            metadata: metadata
        }, null, 2);
    """.trimIndent()
    
    val result = jsEngine.executeScript(dataScript)
    println("Complex processing result: ${result.result}")
}
```

## Demo Application

The included `MainActivity` provides a comprehensive demo with:

- **Interactive JavaScript Editor**: Write and execute JavaScript code
- **Sample Scripts**: Pre-built examples for common use cases
- **External Library Loading**: Demonstrate loading JavaScript from assets
- **Real-time Results**: See execution results immediately
- **Error Handling**: Proper error display and handling
- **Modern UI**: Built with Jetpack Compose

### Sample Scripts Available

1. **Basic Math**: `2 + 3 * 4`
2. **String Operations**: `"Hello " + "World!"`
3. **Array Operations**: `[1,2,3,4,5].map(x => x * 2)`
4. **Object Creation**: `({name: "John", age: 30})`
5. **Function Definition**: Fibonacci sequence calculator
6. **JSON Operations**: Complex object manipulation
7. **Console Logging**: Debug output examples
8. **Kotlin Bindings Test**: Test bound functions and objects
9. **Library Functions**: Use external library functions
10. **Calculator Class**: Object-oriented JavaScript examples

## Dependencies

```gradle
dependencies {
    // JavaScript Engine - QuickJS Android
    implementation "io.github.taoweiji.quickjs:quickjs-android:1.4.0"
    
    // JSON serialization
    implementation "org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.0"
    
    // Coroutines for async operations
    implementation "org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3"
}
```

## Architecture

```
JSEngineManager
├── QuickJS Runtime
├── JavaScript Context
├── Object Bindings (Kotlin ↔ JavaScript)
├── Function Bindings (Callbacks)
├── Memory Management
├── Error Handling
└── Asset Loading
```

## Performance Considerations

- **Initialization**: Initialize once and reuse the engine instance
- **Threading**: All operations use background threads (IO dispatcher)
- **Memory**: Clean up resources in `onDestroy()`
- **Large Scripts**: Consider breaking large scripts into smaller chunks
- **Bindings**: Minimize complex object bindings for better performance

## Troubleshooting

### Common Issues

1. **Engine not initialized**: Always call `initialize()` before executing scripts
2. **Memory leaks**: Ensure `destroy()` is called in `onDestroy()`
3. **Asset loading fails**: Check file path and ensure assets exist
4. **Binding errors**: Verify parameter types and handling in callbacks

### Debug Tips

- Use `console.log()` in JavaScript (output goes to Android Logcat)
- Check `JSExecutionResult.success` and `error` fields
- Monitor memory usage with `getMemoryInfo()`
- Test scripts incrementally

## License

This implementation uses QuickJS, which is under MIT license. The wrapper and demo code are
available under Apache 2.0 license.

## Contributing

Feel free to contribute improvements, bug fixes, or additional examples!