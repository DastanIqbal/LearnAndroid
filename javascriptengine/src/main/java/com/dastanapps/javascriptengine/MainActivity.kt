package com.dastanapps.javascriptengine

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.lifecycleScope
import com.dastanapps.javascriptengine.jsengine.JSEngineManager
import com.dastanapps.javascriptengine.jsengine.JSExecutionResult
import com.dastanapps.javascriptengine.jsonata.JSonata4JavaDemo
import com.dastanapps.javascriptengine.ui.theme.LearnAndroidTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private lateinit var jsEngine: JSEngineManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize JavaScript Engine
        jsEngine = JSEngineManager(this)
        lifecycleScope.launch(Dispatchers.IO) {
//            useRawJSONataTransformation(this@MainActivity)
        }

        enableEdgeToEdge()
        setContent {
            LearnAndroidTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
//                    JSEngineDemo(
//                        jsEngine = jsEngine,
//                        modifier = Modifier.padding(innerPadding)
//                    )
//                    JSonataDemo(
//                        jsEngine = jsEngine,
//                        modifier = Modifier.padding(innerPadding)
//                    )
                    JSonata4JavaDemo(
                        jsEngine = jsEngine,
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        // Clean up JavaScript Engine resources
        lifecycleScope.launch(Dispatchers.IO) {
            jsEngine.destroy()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JSEngineDemo(
    jsEngine: JSEngineManager,
    modifier: Modifier = Modifier
) {
    var isEngineInitialized by remember { mutableStateOf(false) }
    var currentScript by remember { mutableStateOf("") }
    var executionResults by remember { mutableStateOf(listOf<ExecutionLog>()) }
    var isLoading by remember { mutableStateOf(false) }
    var libraryLoaded by remember { mutableStateOf(false) }

    val coroutineScope = rememberCoroutineScope()

    // Sample JavaScript scripts
    val sampleScripts = remember {
        listOf(
            "Basic Math" to "2 + 3 * 4",
            "String Operations" to "\"Hello \" + \"World!\"",
            "Array Operations" to "[1, 2, 3, 4, 5].map(x => x * 2)",
            "Object Creation" to "({name: \"John\", age: 30, city: \"New York\"})",
            "Function Definition" to """
                function fibonacci(n) {
                    if (n <= 1) return n;
                    return fibonacci(n - 1) + fibonacci(n - 2);
                }
                fibonacci(10);
            """.trimIndent(),
            "JSON Operations" to """
                const data = {users: [{name: "Alice", age: 25}, {name: "Bob", age: 30}]};
                JSON.stringify(data, null, 2);
            """.trimIndent(),
            "Console Logging" to """
                console.log("Hello from JavaScript!");
                console.error("This is an error message");
                "Check Android Logcat for console outputs";
            """.trimIndent(),
            "Kotlin Bindings Test" to """
                // Test the bound Kotlin functions
                const result1 = multiply(6, 7);
                const result2 = JSON.stringify(KotlinData, null, 2);
                showToast("Testing Kotlin bindings!");
                `Multiply: \$\{result1\}, KotlinData: \$\{result2\}`;
            """.trimIndent(),
            "Load External Library" to "// This will be handled by the Load Library button",
            "Use Library Functions" to """
                // Make sure to load the library first!
                const greeting = greetUser("Android Developer");
                const factorial = calculateFactorial(5);
                const arrayResult = processArray([1, 2, 3, 4, 5], 'sum');
                `\$\{greeting\}, Factorial of 5: \$\{factorial\}, Array sum: \$\{arrayResult\}`;
            """.trimIndent(),
            "Calculator Class" to """
                // Use the Calculator class from the library
                const myCalc = new Calculator();
                myCalc.add(10, 5);
                myCalc.multiply(3, 4);
                myCalc.divide(20, 4);
                const history = myCalc.getHistory();
                JSON.stringify(history, null, 2);
            """.trimIndent(),
            "Test All Bindings" to "testKotlinBindings()",
            "JSONata Transform" to """
                const inputData = {
                    config: {
                        lang: "en",
                        localization: {
                            "label.date.from": "From",
                            "label.date.to": "to",
                            "label.targetAudiance": "Target Audience:"
                        }
                    },
                    data: {
                        result: {
                            eventinfo: {
                                 name: "This is name",
                                    category: "This is category",
                                    eventLogo: "https://example.com/workshop-logo.png",
                                    description: "This is description",
                                    eventFromDate: "2024-09-25T14:00:00Z",
                                    eventToDate: "2024-09-25T18:00:00Z", 
                                    eventLocation: "This is location",
                                    targetAudience: "This is audience",
                            }
                        },
                        filters: { category: "tech", year: 2024 }
                    },
                    localizationStrings: {}
                };
                
                const transformed = transformEventData(inputData);
                JSON.stringify(transformed, null, 2);
            """.trimIndent(),
            "Real API Data Transform" to """
                const apiResponse = {
                    "status": "success",
                    "data": {
                        "error": false,
                        "status": true,
                        "responseAt": "2025-09-26T13:13:58.540Z",
                        "result": {
                            "eventinfo": [
                                {
                                    "id": 102,
                                     name: "This is name",
                                    category: "This is category",
                                    eventLogo: "https://example.com/workshop-logo.png",
                                    description: "This is description",
                                    eventFromDate: "2024-09-25T14:00:00Z",
                                    eventToDate: "2024-09-25T18:00:00Z", 
                                    eventLocation: "This is location",
                                    targetAudience: "This is audience",
                                    "category": "This is category",
                                    "Classification": "This is Classification",
                                    "registrationEnable": "N",
                                    "email": "Food@Global.ae",
                                    "mobile": "0541111111",
                                    "availableSeats": "Y",
                                    "isRegistered": "N"
                                }
                            ]
                        }
                    }
                };
                
                const eventData = apiResponse.data.result.eventinfo[0];
                const transformInput = {
                    config: {
                        lang: "en",
                        localization: {
                            "label.date.from": "From",
                            "label.date.to": "to",
                            "label.targetAudiance": "Target Audience:",
                            "label.contact": "Contact:",
                            "label.registration": "Registration:",
                            "label.classification": "Type:",
                            "label.seats": "Available Seats:"
                        }
                    },
                    data: {
                        result: {
                            eventinfo: {
                                name: eventData.name,
                                category: eventData.category,
                                eventLogo: "https://example.com/food-week-logo.png", // Default logo
                                description: eventData.description,
                                eventFromDate: eventData.eventFromDate,
                                eventToDate: eventData.eventToDate,
                                eventLocation: eventData.eventLocation,
                                targetAudience: eventData.targetAudience,
                                eventTimeFrom: eventData.eventTimeFrom,
                                eventTimeTo: eventData.eventTimeTo,
                                email: eventData.email,
                                mobile: eventData.mobile,
                                classification: eventData.Classification,
                                registrationEnabled: eventData.registrationEnable === "Y",
                                availableSeats: eventData.availableSeats === "Y",
                                isRegistered: eventData.isRegistered === "Y"
                            }
                        },
                        filters: {
                            category: eventData.category.toLowerCase().replace(" ", "_"),
                            hasRegistration: eventData.registrationEnable === "Y",
                            hasSeats: eventData.availableSeats === "Y"
                        }
                    },
                    localizationStrings: {}
                };
                
               
                const transformed = transformEventData(transformInput);
                
               
                transformed.data.list[0].props.captions.push(
                    {
                        "text": transformInput.config.localization["label.contact"] + " " + eventData.email + " | " + eventData.mobile,
                        
                        "type": "contact"
                    },
                    {
                        "text": transformInput.config.localization["label.classification"] + " " + eventData.Classification,
                        
                        "type": "classification"
                    },
                    {
                        "text": transformInput.config.localization["label.registration"] + " " + (eventData.registrationEnable === "Y" ? "Available" : "Not Available"),
                        
                        "type": eventData.registrationEnable === "Y" ? "success" : "warning"
                    }
                );
                
                JSON.stringify(transformed, null, 2);
            """.trimIndent()
        )
    }

    // Initialize engine on first composition
    LaunchedEffect(Unit) {
        initializeEngine(jsEngine) { success ->
            isEngineInitialized = success
            if (success) {
                executionResults = executionResults + ExecutionLog(
                    "System",
                    "JavaScript Engine initialized successfully!",
                    true
                )
                // Setup Kotlin bindings after initialization
                coroutineScope.launch {
                    setupKotlinBindings(jsEngine)
                }
            } else {
                executionResults = executionResults + ExecutionLog(
                    "System",
                    "Failed to initialize JavaScript Engine",
                    false
                )
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Header
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "JavaScript Engine Demo",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Engine Status: ${if (isEngineInitialized) "✅ Ready" else "⏳ Initializing..."}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (isEngineInitialized) Color.Green else MaterialTheme.colorScheme.tertiary
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Script Input Section
        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "JavaScript Code",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = currentScript,
                    onValueChange = { currentScript = it },
                    label = { Text("Enter JavaScript code") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3,
                    maxLines = 6
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            if (currentScript.isNotBlank() && isEngineInitialized) {
                                coroutineScope.launch {
                                    executeScript(
                                        jsEngine = jsEngine,
                                        script = currentScript,
                                        isLoading = isLoading,
                                        onLoadingChange = { isLoading = it },
                                        onResult = { result ->
                                            executionResults = executionResults + ExecutionLog(
                                                "Custom Script",
                                                if (result.success) result.result
                                                    ?: "Success" else result.error
                                                    ?: "Unknown error",
                                                result.success
                                            )
                                        }
                                    )
                                }
                            }
                        },
                        enabled = isEngineInitialized && currentScript.isNotBlank() && !isLoading,
                        modifier = Modifier.weight(1f)
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text("Execute")
                        }
                    }

                    Button(
                        onClick = {
                            if (isEngineInitialized) {
                                coroutineScope.launch {
                                    loadLibrary(
                                        jsEngine = jsEngine,
                                        isLoading = isLoading,
                                        onLoadingChange = { isLoading = it },
                                        onResult = { result ->
                                            libraryLoaded = result.success
                                            executionResults = executionResults + ExecutionLog(
                                                "Library Loading",
                                                if (result.success) "Library loaded successfully!" else result.error
                                                    ?: "Unknown error",
                                                result.success
                                            )
                                        }
                                    )
                                }
                            }
                        },
                        enabled = isEngineInitialized && !isLoading,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(if (libraryLoaded) "✅ Library" else "Load Library")
                    }

                    OutlinedButton(
                        onClick = { currentScript = "" },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Clear")
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Sample Scripts
        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "Sample Scripts",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(8.dp))

                LazyColumn(
                    modifier = Modifier.height(200.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(sampleScripts) { (name, script) ->
                        OutlinedButton(
                            onClick = { currentScript = script },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(name)
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Execution Results
        Card(
            modifier = Modifier.fillMaxSize()
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Execution Results",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    if (executionResults.isNotEmpty()) {
                        TextButton(
                            onClick = { executionResults = emptyList() }
                        ) {
                            Text("Clear")
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                if (executionResults.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No executions yet. Try running some JavaScript code!",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        reverseLayout = true // Show latest results first
                    ) {
                        items(executionResults.reversed()) { log ->
                            ExecutionResultCard(log)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ExecutionResultCard(log: ExecutionLog) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (log.success)
                MaterialTheme.colorScheme.surfaceVariant
            else
                MaterialTheme.colorScheme.errorContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = log.source,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (log.success)
                        MaterialTheme.colorScheme.primary
                    else
                        MaterialTheme.colorScheme.error
                )

                Text(
                    text = if (log.success) "✅" else "❌",
                    fontSize = 12.sp
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = log.result,
                style = MaterialTheme.typography.bodySmall,
                fontFamily = FontFamily.Monospace,
                color = if (log.success)
                    MaterialTheme.colorScheme.onSurface
                else
                    MaterialTheme.colorScheme.onErrorContainer
            )
        }
    }
}

// Helper functions
suspend fun initializeEngine(
    jsEngine: JSEngineManager,
    onResult: (Boolean) -> Unit
) {
    val success = jsEngine.initialize()
    onResult(success)
}

private suspend fun setupKotlinBindings(jsEngine: JSEngineManager) {
    // Bind Kotlin functions to JavaScript
    jsEngine.bindVoidFunction("showToast") { receiver, args ->
        // This would show a toast in a real implementation
        println("Toast: ${if (args.length() > 0) args.getString(0) else "Hello from Kotlin!"}")
    }

    jsEngine.bindFunction("multiply") { receiver, args ->
        val a = if (args.length() > 0) args.getDouble(0) else 0.0
        val b = if (args.length() > 1) args.getDouble(1) else 0.0
        a * b
    }

    // Bind a Kotlin object
    val kotlinData = mapOf(
        "appName" to "JavaScript Engine Demo",
        "version" to "1.0.0",
        "features" to listOf("QuickJS", "Kotlin Binding", "Async Execution")
    )
    jsEngine.bindObject("KotlinData", kotlinData)
}

private suspend fun executeScript(
    jsEngine: JSEngineManager,
    script: String,
    isLoading: Boolean,
    onLoadingChange: (Boolean) -> Unit,
    onResult: (JSExecutionResult) -> Unit
) {
    onLoadingChange(true)
    try {
        val result = jsEngine.executeScript(script)
        onResult(result)
    } catch (e: Exception) {
        onResult(JSExecutionResult.error("Execution failed: ${e.message}"))
    } finally {
        onLoadingChange(false)
    }
}

private suspend fun loadLibrary(
    jsEngine: JSEngineManager,
    isLoading: Boolean,
    onLoadingChange: (Boolean) -> Unit,
    onResult: (JSExecutionResult) -> Unit
) {
    onLoadingChange(true)
    try {
        val sampleResult = jsEngine.loadLibraryFromAssets("sample-library.js")
        if (!sampleResult.success) {
            onResult(JSExecutionResult.error("Failed to load sample library: ${sampleResult.error}"))
            return
        }

        val jsonataResult = jsEngine.loadLibraryFromAssets("jsonata.min.js")
        if (!jsonataResult.success) {
            onResult(JSExecutionResult.error("Failed to load JSONata library: ${jsonataResult.error}"))
            return
        }

        onResult(JSExecutionResult.success("Both libraries loaded successfully!"))
    } catch (e: Exception) {
        onResult(JSExecutionResult.error("Library loading failed: ${e.message}"))
    } finally {
        onLoadingChange(false)
    }
}

// Data class for execution logs
data class ExecutionLog(
    val source: String,
    val result: String,
    val success: Boolean
)

@Preview(showBackground = true)
@Composable
fun JSEngineDemoPreview() {
    LearnAndroidTheme {
        // Preview with mock data - cannot show actual JSEngine in preview
        Text("JavaScript Engine Demo Preview")
    }
}