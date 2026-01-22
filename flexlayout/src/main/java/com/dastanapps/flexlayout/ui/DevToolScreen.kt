package com.dastanapps.flexlayout.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.dastanapps.flexlayout.core.FlexAlign
import com.dastanapps.flexlayout.core.FlexJustify
import com.dastanapps.flexlayout.core.FlexNode
import com.dastanapps.flexlayout.core.FlexParser
import com.dastanapps.flexlayout.core.FlexRenderer
import com.dastanapps.flexlayout.core.FlexSerializer
import com.dastanapps.flexlayout.core.FlexType
import com.dastanapps.flexlayout.core.FlexWrap

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DevToolScreen(initialJson: String) {
    var jsonText by remember { mutableStateOf(initialJson) }
    var flexNode by remember { mutableStateOf<FlexNode?>(null) }
    var error by remember { mutableStateOf<String?>(null) }
    var showDialog by remember { mutableStateOf(false) }

    LaunchedEffect(jsonText) {
        try {
            flexNode = FlexParser.parse(jsonText)
            error = null
        } catch (e: Exception) {
            error = e.message
        }
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = { showDialog = true }) {
                Icon(Icons.Default.Edit, contentDescription = "Edit")
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Color.White)
        ) {
            if (flexNode != null) {
                FlexRenderer(flexNode!!, Modifier.fillMaxSize())
            } else if (error != null) {
                Text(
                    text = "Error: $error",
                    color = Color.Red,
                    modifier = Modifier.align(Alignment.Center)
                )
            }
        }

        if (showDialog) {
            EditorDialog(
                initialJson = jsonText,
                onDismiss = { showDialog = false },
                onSave = { newJson ->
                    jsonText = newJson
                    showDialog = false
                }
            )
        }
    }
}

@Composable
fun EditorDialog(
    initialJson: String,
    onDismiss: () -> Unit,
    onSave: (String) -> Unit
) {
    var editJson by remember { mutableStateOf(initialJson) }
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("Visual", "JSON")
    
    // For Visual Tab syncing
    var visualNode by remember { mutableStateOf<FlexNode?>(null) }
    var visualError by remember { mutableStateOf<String?>(null) }

    // Parse whenever editJson changes to keep visual tab ready (or show error)
    LaunchedEffect(editJson) {
        try {
            visualNode = FlexParser.parse(editJson)
            visualError = null
        } catch (e: Exception) {
            visualError = e.message
            visualNode = null
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .background(Color.White, RoundedCornerShape(16.dp))
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                TabRow(selectedTabIndex = selectedTab) {
                    tabs.forEachIndexed { index, title ->
                        Tab(
                            selected = selectedTab == index,
                            onClick = { selectedTab = index },
                            text = { Text(title) }
                        )
                    }
                }

                Box(modifier = Modifier.weight(1f).padding(16.dp)) {
                    if (selectedTab == 0) {
                        // Visual Editor
                        if (visualNode != null) {
                             VisualEditor(
                                 node = visualNode!!, 
                                 onUpdate = { updatedNode ->
                                     editJson = FlexSerializer.toJson(updatedNode)
                                 }
                             )
                        } else {
                            Text("Invalid JSON. Switch to JSON tab to fix.", color = Color.Red)
                        }
                    } else {
                        // JSON Editor
                        BasicTextField(
                            value = editJson,
                            onValueChange = { editJson = it },
                            modifier = Modifier
                                .fillMaxSize()
                                .border(1.dp, Color.Gray, RoundedCornerShape(4.dp))
                                .padding(8.dp)
                                .verticalScroll(rememberScrollState()),
                            textStyle = TextStyle(
                                fontFamily = FontFamily.Monospace,
                                fontSize = 12.sp,
                                color = Color.Black
                            )
                        )
                    }
                }

                // Action Buttons
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(onClick = { onSave(editJson) }) {
                        Text("Save")
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VisualEditor(node: FlexNode, onUpdate: (FlexNode) -> Unit) {
    // We only support editing the ROOT properties for now, 
    // as selecting deep nodes is complex without a tree viewer.
    
    Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
        Text("Root Properties", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(16.dp))

        // Type (Direction)
        Text("Layout Direction (Type)", style = MaterialTheme.typography.bodySmall)
        DropdownSelector(
            options = listOf("COLUMN", "ROW"),
            selected = if (node.type == FlexType.COLUMN || node.type == FlexType.ROW) node.type.name else "COLUMN",
            onSelected = { 
                onUpdate(node.copy(type = FlexType.valueOf(it)))
            }
        )
        
        Spacer(modifier = Modifier.height(16.dp))

        // Justify Content
        Text("Justify Content", style = MaterialTheme.typography.bodySmall)
        DropdownSelector(
            options = FlexJustify.values().map { it.name },
            selected = node.style.justifyContent.name,
            onSelected = { 
                val newStyle = node.style.copy(justifyContent = FlexJustify.valueOf(it))
                onUpdate(node.copy(style = newStyle))
            }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Align Items
        Text("Align Items", style = MaterialTheme.typography.bodySmall)
        DropdownSelector(
            options = FlexAlign.values().map { it.name },
            selected = node.style.alignItems.name,
            onSelected = { 
                val newStyle = node.style.copy(alignItems = FlexAlign.valueOf(it))
                onUpdate(node.copy(style = newStyle))
            }
        )

        Spacer(modifier = Modifier.height(16.dp))
        
        // Wrap
        Text("Flex Wrap", style = MaterialTheme.typography.bodySmall)
        DropdownSelector(
            options = FlexWrap.values().map { it.name },
            selected = node.style.flexWrap.name,
            onSelected = { 
                val newStyle = node.style.copy(flexWrap = FlexWrap.valueOf(it))
                onUpdate(node.copy(style = newStyle))
            }
        )

        Spacer(modifier = Modifier.height(16.dp))
        
        // Gap
        Text("Gap", style = MaterialTheme.typography.bodySmall)
        OutlinedTextField(
            value = node.style.gap.toString(),
            onValueChange = { 
                val gap = it.toIntOrNull() ?: 0
                val newStyle = node.style.copy(gap = gap)
                onUpdate(node.copy(style = newStyle))
            },
            modifier = Modifier.fillMaxWidth()
        )
        
        Spacer(modifier = Modifier.height(16.dp))

        // Padding
        Text("Padding", style = MaterialTheme.typography.bodySmall)
        OutlinedTextField(
            value = node.style.padding.all.toString(),
            onValueChange = { 
                val pad = it.toIntOrNull() ?: 0
                // For simplicity, we update 'all' padding.
                val newPadding = node.style.padding.copy(all = pad, top = pad, bottom = pad, left = pad, right = pad)
                val newStyle = node.style.copy(padding = newPadding)
                onUpdate(node.copy(style = newStyle))
            },
             modifier = Modifier.fillMaxWidth()
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        Text("Note: Only root container properties are editable here. Use JSON tab for children.", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
    }
}

@Composable
fun DropdownSelector(
    options: List<String>,
    selected: String,
    onSelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    
    Box(modifier = Modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = selected,
            onValueChange = {},
            readOnly = true,
            trailingIcon = { 
                // Arrow icon could go here
            },
            modifier = Modifier
                .fillMaxWidth()
                .clickable { expanded = true },
            enabled = false, // We use Box click to open
            colors = androidx.compose.material3.TextFieldDefaults.colors(
                disabledContainerColor = Color.Transparent,
                disabledTextColor = Color.Black,
                disabledIndicatorColor = Color.Gray
            )
        )
        
        // Overlay transparent box to capture click since TextField is disabled or readOnly sometimes blocks clicks
        Box(
            modifier = Modifier
                .matchParentSize()
                .clickable { expanded = true }
        )

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option) },
                    onClick = {
                        onSelected(option)
                        expanded = false
                    }
                )
            }
        }
    }
}
