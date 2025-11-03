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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.api.jsonata4java.Expression
import com.dastanapps.javascriptengine.ExecutionLog
import com.dastanapps.javascriptengine.ExecutionResultCard
import com.dastanapps.javascriptengine.initializeEngine
import com.dastanapps.javascriptengine.jsengine.JSEngineManager
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import kotlinx.coroutines.launch
import org.json.JSONObject


/**
 * Created by Iqbal Ahmed on 27/09/2025.
 */

private fun escapeJsString(str: String): String {
    return str
        .replace("\\", "\\\\")
        .replace("'", "\\'")
        .replace("\n", "\\n")
        .replace("\r", "\\r")
}


@Composable
fun JSonataDemo(
    jsEngine: JSEngineManager,
    modifier: Modifier = Modifier
) {
    var isEngineInitialized by remember { mutableStateOf(false) }
    var currentScript by remember { mutableStateOf("") }
    var executionResults by remember { mutableStateOf(listOf<ExecutionLog>()) }
    var isLoading by remember { mutableStateOf(false) }
    var libraryLoaded by remember { mutableStateOf(false) }

    val coroutineScope = rememberCoroutineScope()
    LaunchedEffect(Unit) {
        val jsonataExp = jsEngine.loadAssets("jsonata-ex-script.js")
        val jsonInput = jsEngine.loadAssets("jsonata-ex.json")

        val jsonExpression = JSONObject.quote(jsonataExp.toString())
        val jsonInputData = JSONObject.quote(jsonInput.toString())

        val escapedJsonataExpr: String? = jsonExpression


        val expr = """
            (
              ${'$'}language := "en";
              ${'$'}localization := ${'$'}localization ? ${'$'}localization : {
                "label.date.from": "From",
                "label.date.to": "To",
                "label.targetAudiance": "Target Audience"
              };
              ${'$'}transformedData := data
                .{
                  "props": {
                    "title": result.eventinfo.name,
                    "subtitle": result.eventinfo.category,
                    "image": result.eventinfo.eventLogo,
                    "description": result.eventinfo.description,
                    "uiType": "media",
                    "captions": [
                      {
                        "text": ${'$'}lookup(${'$'}localization, "label.date.from") & " " &
                        ${'$'}fromMillis(${'$'}toMillis(result.eventinfo.eventFromDate), "[MNn] [D1o], [Y0001]") &
                        " " & ${'$'}lookup(${'$'}localization, "label.date.to") & " " &
                        ${'$'}fromMillis(${'$'}toMillis(result.eventinfo.eventToDate), "[MNn] [D1o], [Y0001]"),
                       
                        "type": "default"
                      },
                      {
                        "text": result.eventinfo.eventLocation,
                       
                        "type": "default"
                      },
                      {
                        "text": ${'$'}fromMillis(${'$'}toMillis(result.eventinfo.eventFromDate), "[h]:[m01] [P]") & " " &
                        ${'$'}lookup(${'$'}localization, "label.date.to") & " " &
                        ${'$'}fromMillis(${'$'}toMillis(result.eventinfo.eventToDate), "[h]:[m01] [P]"),
                        
                        "type": "default"
                      },
                      {
                        "text": ${'$'}lookup(${'$'}localization, "label.targetAudiance") & " " & result.eventinfo.targetAudience,
                        
                        "type": "default"
                      }
                    ]
                  },
                  "metadata": {}
                };
              {
                "status": "success",
                "data": {
                  "list": ${'$'}type(${'$'}transformedData) = "array" ? ${'$'}transformedData : [${'$'}transformedData],
                  "filters": data.filters
                }
              }
            )
            
            """.trimIndent()


        // Escape the string for JavaScript
        val escapedExpr: String? = escapeJsString(expr)


        val jsonString =
            "{ \"status\": \"success\", \"data\": { \"result\": { \"eventinfo\": { \"name\": \"Global Food Week\", \"category\": \"Hospital Clinic\", \"description\": \"This is Description...\", \"eventFromDate\": \"2025-10-06T00:00:00Z\", \"eventToDate\": \"2025-10-10T00:00:00Z\", \"eventLocation\": \"Halls 4-11\", \"targetAudience\": \"Testing Audience\" } } } }"

        // Build the JavaScript script
        val script =
            "const data = " + jsonString + ";\n" +
                    "const expr = jsonata('" + escapedExpr + "');\n" +
                    "const result = expr.evaluate(data);\n" +
                    "JSON.stringify(result);"

        val script1 = """
                (function() {
                    try {
                        var jsonataInstance = jsonata($jsonExpression);
                        var result = jsonataInstance.evaluate(JSON.parse($jsonInputData));
                        return JSON.stringify(result);
                    } catch (e) {
                        return JSON.stringify({ "error": e.message });
                    }
                })();
            """.trimIndent()

        initializeEngine(jsEngine) { success ->
            isEngineInitialized = success
            executionResults = if (success) {
                coroutineScope.launch {
                    val results = jsEngine.loadLibraryFromAssets("jsonata.min.js")
                    if (!results.success) {
                        executionResults += ExecutionLog(
                            "Library Loading",
                            if (results.success) "Library loaded successfully!" else results.error
                                ?: "Unknown error",
                            false
                        )
                    }

                    val result = jsEngine.executeScript(script, "evaluate.js")
                    executionResults  += ExecutionLog(
                        "Script Running",
                        if (results.success) result.result?: "Success But Error" else results.error
                            ?: "Unknown error",
                        false
                    )
                }
                executionResults + ExecutionLog(
                    "System",
                    "JavaScript Engine initialized successfully!",
                    true
                )
            } else {
                executionResults + ExecutionLog(
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