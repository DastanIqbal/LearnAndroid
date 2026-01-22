package com.dastanapps.flexlayout.core

enum class FlexType {
    COLUMN, ROW, TEXT, INPUT, BUTTON, SPACER, BOX
}

data class FlexNode(
    val type: FlexType,
    val style: FlexStyle = FlexStyle(),
    val text: String? = null,
    val placeholder: String? = null,
    val inputType: String? = null,
    val onClick: String? = null,
    val children: List<FlexNode> = emptyList()
)

data class FlexStyle(
    val width: FlexSize = FlexSize.Wrap,
    val height: FlexSize = FlexSize.Wrap,
    val padding: FlexPadding = FlexPadding(),
    val margin: FlexMargin = FlexMargin(),
    val backgroundColor: String? = null,
    val textColor: String? = null,
    val fontSize: Int = 14,
    val fontWeight: String = "normal",
    val justifyContent: FlexJustify = FlexJustify.START,
    val alignItems: FlexAlign = FlexAlign.START,
    val alignSelf: FlexAlign? = null,
    val flex: Float = 0f,
    val fillMaxSize: Boolean = false,
    val fillMaxWidth: Boolean = false,
    val borderRadius: Int = 0,
    val gap: Int = 0,
    val flexWrap: FlexWrap = FlexWrap.NOWRAP
)

sealed class FlexSize {
    object Wrap : FlexSize()
    object Fill : FlexSize()
    data class Fixed(val value: Int) : FlexSize()
}

data class FlexPadding(
    val top: Int = 0,
    val bottom: Int = 0,
    val left: Int = 0,
    val right: Int = 0,
    val all: Int = 0
)

data class FlexMargin(
    val top: Int = 0,
    val bottom: Int = 0,
    val left: Int = 0,
    val right: Int = 0
)

enum class FlexJustify {
    START, CENTER, END, SPACE_BETWEEN, SPACE_AROUND, SPACE_EVENLY
}

enum class FlexAlign {
    START, CENTER, END, STRETCH
}

enum class FlexWrap {
    NOWRAP, WRAP, WRAP_REVERSE
}
