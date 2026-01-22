package com.dastanapps.flexlayout.core

import org.json.JSONObject

object FlexParser {
    fun parse(json: String): FlexNode {
        val jsonObject = JSONObject(json)
        return parseNode(jsonObject)
    }

    private fun parseNode(json: JSONObject): FlexNode {
        val typeStr = json.optString("type", "column").uppercase()
        val type = try {
            FlexType.valueOf(typeStr)
        } catch (e: IllegalArgumentException) {
            FlexType.COLUMN
        }

        val styleJson = json.optJSONObject("style")
        val style = parseStyle(styleJson)

        val children = mutableListOf<FlexNode>()
        val childrenJson = json.optJSONArray("children")
        if (childrenJson != null) {
            for (i in 0 until childrenJson.length()) {
                children.add(parseNode(childrenJson.getJSONObject(i)))
            }
        }

        return FlexNode(
            type = type,
            style = style,
            text = if (json.has("text")) json.getString("text") else null,
            placeholder = if (json.has("placeholder")) json.getString("placeholder") else null,
            inputType = if (json.has("inputType")) json.getString("inputType") else null,
            onClick = if (json.has("onClick")) json.getString("onClick") else null,
            children = children
        )
    }

    private fun parseStyle(json: JSONObject?): FlexStyle {
        if (json == null) return FlexStyle()

        val width = parseSize(json.opt("width"))
        val height = parseSize(json.opt("height"))

        val paddingVal = json.optInt("padding", 0)
        val padding = FlexPadding(
            all = paddingVal,
            top = json.optInt("paddingTop", paddingVal),
            bottom = json.optInt("paddingBottom", paddingVal),
            left = json.optInt("paddingLeft", paddingVal),
            right = json.optInt("paddingRight", paddingVal)
        )

        val margin = FlexMargin(
            top = json.optInt("marginTop", 0),
            bottom = json.optInt("marginBottom", 0),
            left = json.optInt("marginLeft", 0),
            right = json.optInt("marginRight", 0)
        )

        return FlexStyle(
            width = width,
            height = height,
            padding = padding,
            margin = margin,
            backgroundColor = if (json.has("backgroundColor")) json.getString("backgroundColor") else null,
            textColor = if (json.has("color")) json.getString("color") else if (json.has("textColor")) json.getString("textColor") else null,
            fontSize = json.optInt("fontSize", 14),
            fontWeight = json.optString("fontWeight", "normal"),
            justifyContent = parseJustify(json.optString("justifyContent")),
            alignItems = parseAlign(json.optString("alignItems")),
            alignSelf = if (json.has("alignSelf")) parseAlign(json.getString("alignSelf")) else null,
            flex = json.optDouble("flex", 0.0).toFloat(),
            fillMaxSize = json.optBoolean("fillMaxSize", false),
            fillMaxWidth = width is FlexSize.Fill, // Convenience if user used width: fillMaxWidth
            borderRadius = json.optInt("borderRadius", 0),
            gap = json.optInt("gap", 0),
            flexWrap = parseWrap(json.optString("flexWrap", json.optString("flex-wrap")))
        )
    }

    private fun parseSize(value: Any?): FlexSize {
        return when (value) {
            is Int -> FlexSize.Fixed(value)
            is String -> {
                if (value.equals("fillMaxWidth", ignoreCase = true) || 
                    value.equals("fillMaxHeight", ignoreCase = true) || 
                    value.equals("match_parent", ignoreCase = true)) {
                    FlexSize.Fill
                } else {
                    FlexSize.Wrap
                }
            }
            else -> FlexSize.Wrap
        }
    }

    private fun parseJustify(value: String): FlexJustify {
        return when (value.lowercase()) {
            "center" -> FlexJustify.CENTER
            "end", "flex-end" -> FlexJustify.END
            "space-between", "space_between" -> FlexJustify.SPACE_BETWEEN
            "space-around", "space_around" -> FlexJustify.SPACE_AROUND
            "space-evenly", "space_evenly" -> FlexJustify.SPACE_EVENLY
            else -> FlexJustify.START
        }
    }

    private fun parseAlign(value: String): FlexAlign {
        return when (value.lowercase()) {
            "center" -> FlexAlign.CENTER
            "end", "flex-end" -> FlexAlign.END
            "stretch" -> FlexAlign.STRETCH
            else -> FlexAlign.START
        }
    }

    private fun parseWrap(value: String): FlexWrap {
        return when (value.lowercase()) {
            "wrap" -> FlexWrap.WRAP
            "wrap-reverse", "wrap_reverse" -> FlexWrap.WRAP_REVERSE
            else -> FlexWrap.NOWRAP
        }
    }
}
