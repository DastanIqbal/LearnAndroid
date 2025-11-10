package com.dastanapps.javascriptengine.integration

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.launch

/**
 * Demo activity showcasing the JSJSONataIntegration
 * This is completely separate from existing code and demonstrates
 * the integration between JavaScript engine and JSONata
 */
class IntegrationDemoActivity : ComponentActivity() {
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        setContent {
            MaterialTheme {
                IntegrationDemoScreen()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IntegrationDemoScreen() {
    val context = LocalContext.current
    val viewModel: IntegrationDemoViewModel = viewModel()
    val uiState by viewModel.uiState.collectAsState()
    val scope = rememberCoroutineScope()
    
    // Initialize integration on first composition
    LaunchedEffect(Unit) {
        viewModel.initializeIntegration(context)
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Header
        Text(
            text = "JS-JSONata Integration Demo",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        
        // Status indicator
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (uiState.isInitialized) 
                    MaterialTheme.colorScheme.primaryContainer 
                else MaterialTheme.colorScheme.errorContainer
            )
        ) {
            Text(
                text = if (uiState.isInitialized) "✓ Integration Ready" else "⚠ Initializing...",
                modifier = Modifier.padding(12.dp),
                fontWeight = FontWeight.Medium
            )
        }
        
        // Demo buttons
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.weight(1f)
        ) {
            // Add test button at the top
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp)
                    ) {
                        Text(
                            text = "Run Integration Tests",
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(bottom = 4.dp)
                        )

                        Text(
                            text = "Verify that all integration functionality works correctly",
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = {
                                    scope.launch {
                                        viewModel.runSimpleV8Tests()
                                    }
                                },
                                enabled = !uiState.isExecuting,
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("V8 Tests")
                            }

                            Button(
                                onClick = {
                                    scope.launch {
                                        viewModel.runIntegrationTests()
                                    }
                                },
                                enabled = uiState.isInitialized && !uiState.isExecuting,
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Integration Tests")
                            }

                            Button(
                                onClick = {
                                    scope.launch {
                                        viewModel.runJSONataTests()
                                    }
                                },
                                enabled = uiState.isInitialized && !uiState.isExecuting,
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("JSONata Tests")
                            }
                        }
                    }
                }
            }

            items(getDemoScenarios()) { scenario ->
                DemoScenarioCard(
                    scenario = scenario,
                    isEnabled = uiState.isInitialized && !uiState.isExecuting,
                    onExecute = { 
                        scope.launch {
                            viewModel.executeScenario(scenario)
                        }
                    }
                )
            }
        }
        
        // Results section
        if (uiState.lastResult != null) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(12.dp)
                ) {
                    Text(
                        text = "Last Execution Result:",
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    
                    SelectionContainer {
                        Text(
                            text = formatResult(uiState.lastResult),
                            fontFamily = FontFamily.Monospace,
                            fontSize = 12.sp,
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = 200.dp)
                                .verticalScroll(rememberScrollState())
                        )
                    }
                }
            }
        }
        
        // Loading indicator
        if (uiState.isExecuting) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }
    }
}

@Composable
fun DemoScenarioCard(
    scenario: DemoScenario,
    isEnabled: Boolean,
    onExecute: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            Text(
                text = scenario.title,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 4.dp)
            )
            
            Text(
                text = scenario.description,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            
            Button(
                onClick = onExecute,
                enabled = isEnabled,
                modifier = Modifier.align(Alignment.End)
            ) {
                Text("Execute")
            }
        }
    }
}

/**
 * Demo scenarios to showcase different integration capabilities
 */
fun getDemoScenarios(): List<DemoScenario> {
    return listOf(
        DemoScenario(
            id = "js_then_jsonata",
            title = "JavaScript → JSONata",
            description = "Execute JS to generate data, then transform with JSONata",
            jsCode = """
                var data = {
                    users: [
                        { name: "Alice", age: 30, city: "New York" },
                        { name: "Bob", age: 25, city: "San Francisco" },
                        { name: "Charlie", age: 35, city: "New York" }
                    ],
                    timestamp: IntegrationUtils.getTimestamp()
                };
                IntegrationUtils.stringifyJSON(data);
            """.trimIndent(),
            jsonataExpression = """
                {
                    "new_york_users": users[city = "New York"].name,
                    "user_count": ${'$'}count(users),
                    "total_age": ${'$'}sum(users.age),
                    "processed_at": timestamp
                }
            """.trimIndent(),
            inputData = null
        ),
        
        DemoScenario(
            id = "jsonata_then_js",
            title = "JSONata → JavaScript",
            description = "Transform data with JSONata, then process with JavaScript",
            jsCode = """
                var processedData = {
                    summary: "Processed " + parsedInput.length + " items",
                    items: parsedInput,
                    processing_time: new Date().toISOString()
                };
                IntegrationUtils.stringifyJSON(processedData);
            """.trimIndent(),
            jsonataExpression = """
                products[price > 100].{
                    "item": name,
                    "cost": price,
                    "category": category
                }
            """.trimIndent(),
            inputData = """
                {
                    "products": [
                        {"name": "Laptop", "price": 1200, "category": "Electronics"},
                        {"name": "Mouse", "price": 25, "category": "Electronics"},
                        {"name": "Desk", "price": 300, "category": "Furniture"},
                        {"name": "Chair", "price": 150, "category": "Furniture"}
                    ]
                }
            """.trimIndent()
        ),
        
        DemoScenario(
            id = "pipeline_demo",
            title = "Pipeline Processing",
            description = "Chain multiple JS and JSONata operations",
            jsCode = "", // Will be used for pipeline
            jsonataExpression = "", // Will be used for pipeline
            inputData = """
                {
                    "sales": [
                        {"month": "Jan", "amount": 1000, "region": "North"},
                        {"month": "Feb", "amount": 1500, "region": "South"},
                        {"month": "Mar", "amount": 1200, "region": "North"}
                    ]
                }
            """.trimIndent(),
            isPipeline = true
        ),
        
        DemoScenario(
            id = "simple_stats",
            title = "Simple Statistics",
            description = "Basic JSONata functions with JavaScript post-processing",
            jsCode = """
                var result = {
                    report_title: "Simple Analytics Report",
                    generated_at: new Date().toISOString(),
                    data: parsedInput,
                    has_revenue_data: parsedInput.total_revenue > 0
                };
                IntegrationUtils.stringifyJSON(result);
            """.trimIndent(),
            jsonataExpression = """
                {
                    "total_revenue": ${'$'}sum(orders.total),
                    "order_count": ${'$'}count(orders),
                    "min_order": ${'$'}min(orders.total),
                    "max_order": ${'$'}max(orders.total)
                }
            """.trimIndent(),
            inputData = """
                {
                    "orders": [
                        {"id": 1, "customer": "Alice", "total": 150.00},
                        {"id": 2, "customer": "Bob", "total": 75.50},
                        {"id": 3, "customer": "Alice", "total": 200.00},
                        {"id": 4, "customer": "Charlie", "total": 300.00},
                        {"id": 5, "customer": "Bob", "total": 125.75}
                    ]
                }
            """.trimIndent()
        ),

        DemoScenario(
            id = "filter_demo",
            title = "Data Filtering",
            description = "Filter data with JSONata and process with JavaScript",
            jsCode = """
                var result = {
                    filtered_users: parsedInput,
                    count: parsedInput.length,
                    analysis: "Found " + parsedInput.length + " active users"
                };
                IntegrationUtils.stringifyJSON(result);
            """.trimIndent(),
            jsonataExpression = """
                users[active = true].{
                    "name": name,
                    "department": department,
                    "salary": salary
                }
            """.trimIndent(),
            inputData = """
                {
                    "users": [
                        {"name": "Alice", "department": "Engineering", "salary": 85000, "active": true},
                        {"name": "Bob", "department": "Marketing", "salary": 75000, "active": true},
                        {"name": "Carol", "department": "Engineering", "salary": 90000, "active": false},
                        {"name": "David", "department": "Sales", "salary": 70000, "active": true}
                    ]
                }
            """.trimIndent()
        )
    )
}

data class DemoScenario(
    val id: String,
    val title: String,
    val description: String,
    val jsCode: String,
    val jsonataExpression: String,
    val inputData: String?,
    val isPipeline: Boolean = false
)

fun formatResult(result: IntegrationResult?): String {
    if (result == null) return "No result"
    
    return buildString {
        appendLine("Success: ${result.success}")
        if (result.error != null) {
            appendLine("Error: ${result.error}")
        }
        if (result.result != null) {
            appendLine("Result:")
            appendLine(result.result)
        }
        if (result.jsResult != null) {
            appendLine("\nJS Execution:")
            appendLine("  Success: ${result.jsResult.success}")
            appendLine("  Time: ${result.jsResult.executionTimeMs}ms")
            if (result.jsResult.error != null) {
                appendLine("  Error: ${result.jsResult.error}")
            }
        }
        if (result.jsonataResult != null) {
            appendLine("\nJSONata Execution:")
            appendLine("  Success: ${result.jsonataResult.success}")
            appendLine("  Time: ${result.jsonataResult.executionTimeMs}ms")
            if (result.jsonataResult.error != null) {
                appendLine("  Error: ${result.jsonataResult.error}")
            }
        }
    }
}