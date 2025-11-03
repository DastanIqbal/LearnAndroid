package com.dastanapps.javascriptengine.jsonata

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.api.jsonata4java.Expression
import com.dastanapps.javascriptengine.ExecutionLog
import com.dastanapps.javascriptengine.ExecutionResultCard
import com.dastanapps.javascriptengine.initializeEngine
import com.dastanapps.javascriptengine.jsengine.JSEngineManager
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.json.JSONObject


/**
 * Created by Iqbal Ahmed on 27/09/2025.
 */

// MVI State
data class JsonataState(
    val jsonInput: String = """{"name": "John", "age": 30, "city": "New York", "hobbies": ["reading", "swimming"]}""",
    val jsonataScript: String = "name",
    val output: String = "",
    val isExecuting: Boolean = false,
    val errorMessage: String = "",
    val jsonata4JavaError: String = "",
    val duktapeError: String = "",
    val currentEngine: String = "JSONata4Java" // Track which engine is being used
)

// MVI Intents
sealed class JsonataIntent {
    data class UpdateJsonInput(val input: String) : JsonataIntent()
    data class UpdateJsonataScript(val script: String) : JsonataIntent()
    object ResetInputs : JsonataIntent()
    data class ExecuteJsonata(val jsonInput: String, val jsonataScript: String) : JsonataIntent()
}

// ViewModel
class JsonataViewModel : ViewModel() {
    private val _state = MutableStateFlow(JsonataState())
    val state: StateFlow<JsonataState> = _state.asStateFlow()
    private lateinit var jsEngine: JSEngineManager

    fun setJSEngine(jsEngine: JSEngineManager) {
        this.jsEngine = jsEngine
    }

    fun handleIntent(intent: JsonataIntent) {
        when (intent) {
            is JsonataIntent.UpdateJsonInput -> {
                _state.value = _state.value.copy(jsonInput = intent.input)
                // Trigger execution if both inputs are valid
                if (intent.input.isNotBlank() && _state.value.jsonataScript.isNotBlank()) {
                    handleIntent(
                        JsonataIntent.ExecuteJsonata(
                            intent.input,
                            _state.value.jsonataScript
                        )
                    )
                } else {
                    _state.value = _state.value.copy(
                        output = "",
                        errorMessage = "",
                        jsonata4JavaError = "",
                        duktapeError = ""
                    )
                }
            }

            is JsonataIntent.UpdateJsonataScript -> {
                _state.value = _state.value.copy(jsonataScript = intent.script)
                // Trigger execution if both inputs are valid
                if (intent.script.isNotBlank() && _state.value.jsonInput.isNotBlank()) {
                    handleIntent(
                        JsonataIntent.ExecuteJsonata(
                            _state.value.jsonInput,
                            intent.script
                        )
                    )
                } else {
                    _state.value = _state.value.copy(
                        output = "",
                        errorMessage = "",
                        jsonata4JavaError = "",
                        duktapeError = ""
                    )
                }
            }

            is JsonataIntent.ResetInputs -> {
                _state.value = JsonataState(
                    errorMessage = "",
                    jsonata4JavaError = "",
                    duktapeError = ""
                )
                // Auto-execute with default values
                handleIntent(
                    JsonataIntent.ExecuteJsonata(
                        _state.value.jsonInput,
                        _state.value.jsonataScript
                    )
                )
            }

            is JsonataIntent.ExecuteJsonata -> {
                executeJsonata(intent.jsonInput, intent.jsonataScript)
            }
        }
    }

    private fun executeJsonata(jsonInput: String, jsonataScript: String) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isExecuting = true)

            try {
                // Validate JSON input
                val objectMapper = ObjectMapper()
                val jsonNode: JsonNode = try {
                    objectMapper.readTree(jsonInput)
                } catch (e: Exception) {
                    _state.value = _state.value.copy(
                        errorMessage = "Invalid JSON format: ${e.message}",
                        output = "",
                        isExecuting = false,
                        jsonata4JavaError = "",
                        duktapeError = ""
                    )
                    return@launch
                }

                // Execute JSONata expression using JSONata4Java
                try {
                    val expression = Expression.jsonata(jsonataScript)
                    val result = expression.evaluate(jsonNode)
                    val formattedResult = when (result) {
                        null -> "null"
                        is JsonNode -> {
                            if (result.isTextual) {
                                result.asText()
                            } else {
                                objectMapper.writerWithDefaultPrettyPrinter()
                                    .writeValueAsString(result)
                            }
                        }

                        else -> result.toString()
                    }
                    _state.value = _state.value.copy(
                        output = formattedResult,
                        errorMessage = "",
                        isExecuting = false,
                        jsonata4JavaError = "",
                        duktapeError = "",
                        currentEngine = "JSONata4Java"
                    )
                } catch (e: Exception) {
                    // Fallback to Duktape (JavaScript engine)
                    _state.value = _state.value.copy(
                        jsonata4JavaError = "Error executing JSONata expression using JSONata4Java: ${e.message}",
                        isExecuting = false
                    )
                    executeJsonataUsingDuktape(jsonInput, jsonataScript)
                }
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    errorMessage = "Unexpected error: ${e.message}",
                    output = "",
                    isExecuting = false,
                    jsonata4JavaError = "",
                    duktapeError = ""
                )
            }
        }
    }

    private fun executeJsonataUsingDuktape(jsonInput: String, jsonataScript: String) {
        viewModelScope.launch {
            try {
                // Initialize Duktape engine if not already initialized
                val initialized = jsEngine.initialize()
                if (!initialized) {
                    _state.value = _state.value.copy(
                        duktapeError = "Failed to initialize JavaScript engine",
                        output = "",
                        isExecuting = false
                    )
                    return@launch
                }

                // Load JSONata library first
                val libraryResult = jsEngine.loadLibraryFromAssets("jsonata.min.js")
                if (!libraryResult.success) {
                    _state.value = _state.value.copy(
                        duktapeError = "Failed to load JSONata library: ${libraryResult.error}",
                        output = "",
                        isExecuting = false
                    )
                    return@launch
                }

                // Escape the JSON input and script for JavaScript
                val escapedJsonInput = escapeJsString(jsonInput)
                val escapedScript = escapeJsString(jsonataScript)

                // Create JavaScript code to execute JSONata
                val jsCode = """
                    (function() {
                        try {
                            var json = JSON.parse('$escapedJsonInput');
                            var expression = jsonata('$escapedScript');
                            var result = expression.evaluate(json);
                            return JSON.stringify(result, null, 2);
                        } catch (e) {
                            return JSON.stringify({ "error": e.message });
                        }
                    })();
                """.trimIndent()

                // Execute the JavaScript code using the JS engine
                val result = jsEngine.executeScript(jsCode, "jsonata_fallback.js")

                if (result.success) {
                    _state.value = _state.value.copy(
                        output = result.result ?: "null",
                        errorMessage = "",
                        isExecuting = false,
                        duktapeError = "",
                        currentEngine = "Duktape (JavaScript)"
                    )
                } else {
                    _state.value = _state.value.copy(
                        duktapeError = "Error executing JSONata expression using Duktape: ${result.error}",
                        output = "",
                        isExecuting = false
                    )
                    // If both engines failed, set a final error message
                    if (_state.value.jsonata4JavaError.isNotEmpty()) {
                        _state.value = _state.value.copy(
                            errorMessage = "Both JSONata4Java and Duktape engines failed to execute the expression"
                        )
                    }
                }
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    duktapeError = "Error executing JSONata expression using Duktape: ${e.message}",
                    output = "",
                    isExecuting = false
                )
                // If both engines failed, set a final error message
                if (_state.value.jsonata4JavaError.isNotEmpty()) {
                    _state.value = _state.value.copy(
                        errorMessage = "Both JSONata4Java and Duktape engines failed to execute the expression"
                    )
                }
            }
        }
    }
}

@Composable
fun JSonata4JavaDemo(
    jsEngine: JSEngineManager,
    modifier: Modifier = Modifier,
    viewModel: JsonataViewModel = remember { JsonataViewModel() }
) {
    val state by viewModel.state.collectAsState()
    val scrollState = rememberScrollState()

    val jsonataExp = jsEngine.loadAssets("jsonata-ex-script.js")
    val jsonInput = jsEngine.loadAssets("jsonata-ex.json")

    // Auto-execute on first composition
    LaunchedEffect(Unit) {
        viewModel.handleIntent(JsonataIntent.UpdateJsonInput(jsonInput))
        viewModel.handleIntent(JsonataIntent.UpdateJsonataScript(jsonataExp))
    }

    // Pass jsEngine to viewModel
    LaunchedEffect(jsEngine) {
        viewModel.setJSEngine(jsEngine)
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(scrollState),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Title
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "JSONata4Java Demo",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                if (state.isExecuting) {
                    Text(
                        text = "⚡ Live",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        // Reset Button (only)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            TextButton(
                onClick = {
                    viewModel.handleIntent(JsonataIntent.ResetInputs)
                }
            ) {
                Text("Reset")
            }
        }

        // JSON Input
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "JSON Data",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = state.jsonInput,
                    onValueChange = { input ->
                        viewModel.handleIntent(JsonataIntent.UpdateJsonInput(input))
                    },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Enter your JSON data here...") },
                    minLines = 4,
                    maxLines = 8,
                    isError = state.errorMessage.isNotEmpty() && state.errorMessage.contains("JSON")
                )
            }
        }

        // JSONata Script Input
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "JSONata Expression",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = state.jsonataScript,
                    onValueChange = { script ->
                        viewModel.handleIntent(JsonataIntent.UpdateJsonataScript(script))
                    },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Enter your JSONata expression here...") },
                    minLines = 4,
                    maxLines = 8,
                    isError = state.errorMessage.isNotEmpty() && state.errorMessage.contains("expression")
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Examples: name, age, hobbies[0], {\"fullName\": name, \"yearsOld\": age}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Error Message
        if (state.errorMessage.isNotEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                )
            ) {
                Text(
                    text = "Error: ${state.errorMessage}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onErrorContainer,
                    modifier = Modifier.padding(16.dp)
                )
            }
        }

        // Output
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
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
                        text = "Output",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    if (state.isExecuting) {
                        Text(
                            text = "Processing...",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        Text(
                            text = "Engine: ${state.currentEngine}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                // Display error messages at the top of output section
                if (state.jsonata4JavaError.isNotEmpty() || state.duktapeError.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.7f)
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp)
                        ) {
                            if (state.jsonata4JavaError.isNotEmpty()) {
                                Text(
                                    text = "❌ JSONata4Java failed: ${state.jsonata4JavaError}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onErrorContainer,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                            if (state.duktapeError.isNotEmpty()) {
                                if (state.jsonata4JavaError.isNotEmpty()) {
                                    Spacer(modifier = Modifier.height(4.dp))
                                }
                                Text(
                                    text = "❌ Duktape failed: ${state.duktapeError}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onErrorContainer,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = state.output,
                    onValueChange = { }, // Read-only
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Output will appear here automatically...") },
                    minLines = 4,
                    maxLines = 10,
                    readOnly = true
                )
            }
        }
    }
}

private fun escapeJsString(str: String): String {
    return str
        .replace("\\", "\\\\")
        .replace("'", "\\'")
        .replace("\n", "\\n")
        .replace("\r", "\\r")
}