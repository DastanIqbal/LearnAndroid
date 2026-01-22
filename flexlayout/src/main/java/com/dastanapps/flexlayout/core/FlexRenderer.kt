package com.dastanapps.flexlayout.core

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowColumn
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun FlexRenderer(node: FlexNode, modifier: Modifier = Modifier) {
    val styleModifier = Modifier.applyStyle(node.style)
    val combinedModifier = modifier.then(styleModifier)

    when (node.type) {
        FlexType.COLUMN -> {
            if (node.style.flexWrap == FlexWrap.NOWRAP) {
                Column(
                    modifier = combinedModifier,
                    verticalArrangement = node.style.justifyContent.toVerticalArrangement(node.style.gap),
                    horizontalAlignment = node.style.alignItems.toHorizontalAlignment()
                ) {
                    node.children.forEach { child ->
                        var childModifier = Modifier.run {
                            if (child.style.flex > 0f) weight(child.style.flex) else this
                        }
                        
                        if (child.style.alignSelf != null) {
                             childModifier = childModifier.align(child.style.alignSelf.toHorizontalAlignment())
                        }

                        FlexRenderer(child, childModifier)
                    }
                }
            } else {
                FlowColumn(
                    modifier = combinedModifier,
                    verticalArrangement = node.style.justifyContent.toVerticalArrangement(node.style.gap),
                    horizontalArrangement = Arrangement.spacedBy(0.dp), // alignContent missing in model, default to 0 gap or handle alignItems?
                    // FlowColumn cross axis is horizontal.
                    // alignItems controls cross axis alignment of items.
                    // But FlowColumn doesn't have horizontalAlignment param (it has horizontalArrangement for lines).
                    // We must apply align to children.
                ) {
                    node.children.forEach { child ->
                        var childModifier = Modifier.run {
                            if (child.style.flex > 0f) weight(child.style.flex) else this
                        }
                        
                        // Apply alignItems from parent if alignSelf is not set
                        val align = child.style.alignSelf ?: node.style.alignItems
                        if (align != FlexAlign.START) { // Default is Start
                             childModifier = childModifier.align(align.toHorizontalAlignment())
                        }

                        FlexRenderer(child, childModifier)
                    }
                }
            }
        }
        FlexType.ROW -> {
            if (node.style.flexWrap == FlexWrap.NOWRAP) {
                Row(
                    modifier = combinedModifier,
                    horizontalArrangement = node.style.justifyContent.toHorizontalArrangement(node.style.gap),
                    verticalAlignment = node.style.alignItems.toVerticalAlignment()
                ) {
                    node.children.forEach { child ->
                         var childModifier = Modifier.run {
                            if (child.style.flex > 0f) weight(child.style.flex) else this
                        }
                        
                        if (child.style.alignSelf != null) {
                             childModifier = childModifier.align(child.style.alignSelf.toVerticalAlignment())
                        }

                        FlexRenderer(child, childModifier)
                    }
                }
            } else {
                FlowRow(
                    modifier = combinedModifier,
                    horizontalArrangement = node.style.justifyContent.toHorizontalArrangement(node.style.gap),
                    verticalArrangement = Arrangement.spacedBy(node.style.gap.dp) // alignContent missing
                ) {
                    node.children.forEach { child ->
                         var childModifier = Modifier.run {
                            if (child.style.flex > 0f) weight(child.style.flex) else this
                        }
                        
                        // Apply alignItems from parent if alignSelf is not set
                        val align = child.style.alignSelf ?: node.style.alignItems
                        if (align != FlexAlign.START) {
                             childModifier = childModifier.align(align.toVerticalAlignment())
                        }

                        FlexRenderer(child, childModifier)
                    }
                }
            }
        }
        FlexType.TEXT -> {
            Text(
                text = node.text ?: "",
                modifier = combinedModifier,
                color = parseColor(node.style.textColor),
                fontSize = node.style.fontSize.sp,
                fontWeight = parseFontWeight(node.style.fontWeight)
            )
        }
        FlexType.INPUT -> {
            val visualTransformation = if (node.inputType == "password") PasswordVisualTransformation() else VisualTransformation.None
            // Using BasicTextField or Material TextField? Material is easier for "hint"/placeholder label.
            // But user asked for flexbox properties, maybe they want custom styling.
            // Let's use TextField for simplicity with standard styling support.
            
            TextField(
                value = "", // TODO: State hoisting
                onValueChange = {},
                placeholder = { Text(node.placeholder ?: "") },
                modifier = combinedModifier,
                visualTransformation = visualTransformation,
                singleLine = true,
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    focusedIndicatorColor = Color.Gray,
                    unfocusedIndicatorColor = Color.LightGray
                )
            )
        }
        FlexType.BUTTON -> {
            val bgColor = parseColor(node.style.backgroundColor)
            val txtColor = parseColor(node.style.textColor)
            Button(
                onClick = { /* Handle action */ },
                modifier = combinedModifier,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (bgColor != Color.Unspecified) bgColor else Color.Blue
                ),
                shape = RoundedCornerShape(node.style.borderRadius.dp)
            ) {
                Text(
                    text = node.text ?: "",
                    color = if (txtColor != Color.Unspecified) txtColor else Color.White
                )
            }
        }
        FlexType.SPACER -> {
            Spacer(modifier = combinedModifier)
        }
        FlexType.BOX -> {
             Box(modifier = combinedModifier) {
                 node.children.forEach { child ->
                     // Box scope align
                     FlexRenderer(child)
                 }
             }
        }
    }
}

private fun Modifier.applyStyle(style: FlexStyle): Modifier {
    var m = this

    // Size
    if (style.fillMaxSize) m = m.fillMaxSize()
    if (style.width is FlexSize.Fill || style.fillMaxWidth) m = m.fillMaxWidth()
    if (style.height is FlexSize.Fill) m = m.fillMaxHeight()
    if (style.width is FlexSize.Fixed) m = m.width(style.width.value.dp)
    if (style.height is FlexSize.Fixed) m = m.height(style.height.value.dp)

    // Margin
    m = m.padding(
        start = style.margin.left.dp,
        top = style.margin.top.dp,
        end = style.margin.right.dp,
        bottom = style.margin.bottom.dp
    )

    // Background & Border Radius (if not Button which handles it internally, but good to have generic support)
    // Note: Button handles its own background, but if we apply it here it might overlay or double up.
    // We should conditionally apply background if it's not a component that handles it, OR let it wrap.
    // For Text/Column/Row/Box, we need this.
    if (style.backgroundColor != null) {
         val bgColor = parseColor(style.backgroundColor)
         if (bgColor != Color.Unspecified) {
             m = m.background(
                 color = bgColor,
                 shape = RoundedCornerShape(style.borderRadius.dp)
             )
             m = m.clip(RoundedCornerShape(style.borderRadius.dp))
         }
    }

    // Padding
    val pLeft = if (style.padding.left != 0) style.padding.left else style.padding.all
    val pTop = if (style.padding.top != 0) style.padding.top else style.padding.all
    val pRight = if (style.padding.right != 0) style.padding.right else style.padding.all
    val pBottom = if (style.padding.bottom != 0) style.padding.bottom else style.padding.all
    
    m = m.padding(
        start = pLeft.dp,
        top = pTop.dp,
        end = pRight.dp,
        bottom = pBottom.dp
    )

    return m
}

// Helpers

private fun parseColor(colorStr: String?): Color {
    if (colorStr == null) return Color.Unspecified
    return try {
        Color(android.graphics.Color.parseColor(colorStr))
    } catch (e: Exception) {
        Color.Unspecified
    }
}

private fun parseFontWeight(weight: String): FontWeight {
    return when (weight.lowercase()) {
        "bold" -> FontWeight.Bold
        "medium" -> FontWeight.Medium
        "light" -> FontWeight.Light
        else -> FontWeight.Normal
    }
}

// Mapping Enums to Compose

private fun FlexJustify.toVerticalArrangement(gap: Int): Arrangement.Vertical {
    val spacing = if (gap > 0) Arrangement.spacedBy(gap.dp) else null
    // If gap is present, we combine it? 
    // Arrangement.Center doesn't support spacing directly like spacedBy(x, Alignment.CenterVertically)
    // Actually spacedBy returns HorizontalOrVertical.
    
    if (gap > 0) {
         return when (this) {
             FlexJustify.CENTER -> Arrangement.spacedBy(gap.dp, Alignment.CenterVertically)
             FlexJustify.END -> Arrangement.spacedBy(gap.dp, Alignment.Bottom)
             else -> Arrangement.spacedBy(gap.dp, Alignment.Top)
         }
    }

    return when (this) {
        FlexJustify.CENTER -> Arrangement.Center
        FlexJustify.END -> Arrangement.Bottom
        FlexJustify.SPACE_BETWEEN -> Arrangement.SpaceBetween
        FlexJustify.SPACE_AROUND -> Arrangement.SpaceAround
        FlexJustify.SPACE_EVENLY -> Arrangement.SpaceEvenly
        else -> Arrangement.Top
    }
}

private fun FlexJustify.toHorizontalArrangement(gap: Int): Arrangement.Horizontal {
     if (gap > 0) {
         return when (this) {
             FlexJustify.CENTER -> Arrangement.spacedBy(gap.dp, Alignment.CenterHorizontally)
             FlexJustify.END -> Arrangement.spacedBy(gap.dp, Alignment.End)
             else -> Arrangement.spacedBy(gap.dp, Alignment.Start)
         }
    }
    
    return when (this) {
        FlexJustify.CENTER -> Arrangement.Center
        FlexJustify.END -> Arrangement.End
        FlexJustify.SPACE_BETWEEN -> Arrangement.SpaceBetween
        FlexJustify.SPACE_AROUND -> Arrangement.SpaceAround
        FlexJustify.SPACE_EVENLY -> Arrangement.SpaceEvenly
        else -> Arrangement.Start
    }
}

private fun FlexAlign.toHorizontalAlignment(): Alignment.Horizontal {
    return when (this) {
        FlexAlign.CENTER -> Alignment.CenterHorizontally
        FlexAlign.END -> Alignment.End
        else -> Alignment.Start
    }
}

private fun FlexAlign.toVerticalAlignment(): Alignment.Vertical {
    return when (this) {
        FlexAlign.CENTER -> Alignment.CenterVertically
        FlexAlign.END -> Alignment.Bottom
        else -> Alignment.Top
    }
}
