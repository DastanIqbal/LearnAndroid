package com.dastanapps.flexlayout.core

import org.json.JSONArray
import org.json.JSONObject

object FlexSerializer {
    fun toJson(node: FlexNode): String {
        return nodeToJsonObject(node).toString(2)
    }

    private fun nodeToJsonObject(node: FlexNode): JSONObject {
        val json = JSONObject()
        json.put("type", node.type.name.lowercase())
        
        if (node.text != null) json.put("text", node.text)
        if (node.placeholder != null) json.put("placeholder", node.placeholder)
        if (node.inputType != null) json.put("inputType", node.inputType)
        if (node.onClick != null) json.put("onClick", node.onClick)

        val style = styleToJsonObject(node.style)
        if (style.length() > 0) {
            json.put("style", style)
        }

        if (node.children.isNotEmpty()) {
            val childrenArray = JSONArray()
            node.children.forEach { child ->
                childrenArray.put(nodeToJsonObject(child))
            }
            json.put("children", childrenArray)
        }

        return json
    }

    private fun styleToJsonObject(style: FlexStyle): JSONObject {
        val json = JSONObject()

        // Size
        when (val w = style.width) {
            is FlexSize.Fixed -> json.put("width", w.value)
            is FlexSize.Fill -> json.put("width", "fillMaxWidth")
            is FlexSize.Wrap -> {} // Default
        }
        when (val h = style.height) {
            is FlexSize.Fixed -> json.put("height", h.value)
            is FlexSize.Fill -> json.put("height", "fillMaxHeight")
            is FlexSize.Wrap -> {}
        }
        
        if (style.fillMaxSize) json.put("fillMaxSize", true)
        if (style.fillMaxWidth && style.width !is FlexSize.Fill) json.put("width", "fillMaxWidth")

        // Padding
        if (style.padding.all != 0) json.put("padding", style.padding.all)
        if (style.padding.top != style.padding.all) json.put("paddingTop", style.padding.top)
        if (style.padding.bottom != style.padding.all) json.put("paddingBottom", style.padding.bottom)
        if (style.padding.left != style.padding.all) json.put("paddingLeft", style.padding.left)
        if (style.padding.right != style.padding.all) json.put("paddingRight", style.padding.right)

        // Margin
        if (style.margin.top != 0) json.put("marginTop", style.margin.top)
        if (style.margin.bottom != 0) json.put("marginBottom", style.margin.bottom)
        if (style.margin.left != 0) json.put("marginLeft", style.margin.left)
        if (style.margin.right != 0) json.put("marginRight", style.margin.right)

        // Colors & Text
        if (style.backgroundColor != null) json.put("backgroundColor", style.backgroundColor)
        if (style.textColor != null) json.put("color", style.textColor)
        if (style.fontSize != 14) json.put("fontSize", style.fontSize)
        if (style.fontWeight != "normal") json.put("fontWeight", style.fontWeight)

        // Flex Properties
        if (style.justifyContent != FlexJustify.START) json.put("justifyContent", style.justifyContent.name.lowercase())
        if (style.alignItems != FlexAlign.START) json.put("alignItems", style.alignItems.name.lowercase())
        if (style.alignSelf != null) json.put("alignSelf", style.alignSelf.name.lowercase())
        if (style.flex != 0f) json.put("flex", style.flex)
        if (style.borderRadius != 0) json.put("borderRadius", style.borderRadius)
        if (style.gap != 0) json.put("gap", style.gap)
        if (style.flexWrap != FlexWrap.NOWRAP) json.put("flexWrap", style.flexWrap.name.lowercase())

        return json
    }
}
