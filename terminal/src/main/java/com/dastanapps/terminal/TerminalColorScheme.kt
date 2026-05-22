package com.dastanapps.terminal

import androidx.compose.ui.graphics.Color

data class TerminalColorScheme(
    val name: String,
    val foreground: String,
    val background: String,
    val ansiColors: IntArray
) {
    fun toArgbPalette(): IntArray {
        return ansiColors.copyOf()
    }

    companion object {
        val Dracula = TerminalColorScheme(
            name = "Dracula",
            foreground = "#E1E1E6",
            background = "#191622",
            ansiColors = intArrayOf(
                color(0xFF201B2D), color(0xFFE96379), color(0xFF67E480), color(0xFFE7DE79),
                color(0xFF78D1E1), color(0xFF988BC7), color(0xFFA1EFE4), color(0xFFE1E1E6),
                color(0xFF4D4D4D), color(0xFFED4556), color(0xFF00F769), color(0xFFE7DE79),
                color(0xFF78D1E1), color(0xFF988BC7), color(0xFFA4FFFF), color(0xFFF7F7FB)
            )
        )

        val SolarizedDark = TerminalColorScheme(
            name = "Solarized Dark",
            foreground = "#839496",
            background = "#002B36",
            ansiColors = intArrayOf(
                color(0xFF073642), color(0xFFDC322F), color(0xFF859900), color(0xFFB58900),
                color(0xFF268BD2), color(0xFFD33682), color(0xFF2AA198), color(0xFFEEE8D5),
                color(0xFF002B36), color(0xFFCB4B16), color(0xFF586E75), color(0xFF657B83),
                color(0xFF839496), color(0xFF6C71C4), color(0xFF93A1A1), color(0xFFFDF6E3)
            )
        )

        val SolarizedLight = TerminalColorScheme(
            name = "Solarized Light",
            foreground = "#657B83",
            background = "#FDF6E3",
            ansiColors = intArrayOf(
                color(0xFF073642), color(0xFFDC322F), color(0xFF859900), color(0xFFB58900),
                color(0xFF268BD2), color(0xFFD33682), color(0xFF2AA198), color(0xFFEEE8D5),
                color(0xFF002B36), color(0xFFCB4B16), color(0xFF586E75), color(0xFF657B83),
                color(0xFF839496), color(0xFF6C71C4), color(0xFF93A1A1), color(0xFFFDF6E3)
            )
        )

        val Monokai = TerminalColorScheme(
            name = "Monokai",
            foreground = "#F8F8F2",
            background = "#272822",
            ansiColors = intArrayOf(
                color(0xFF272822), color(0xFFF92672), color(0xFFA6E22E), color(0xFFF4BF75),
                color(0xFF66D9EF), color(0xFFAE81FF), color(0xFFA1EFE4), color(0xFFF8F8F2),
                color(0xFF75715E), color(0xFFF92672), color(0xFFA6E22E), color(0xFFF4BF75),
                color(0xFF66D9EF), color(0xFFAE81FF), color(0xFFA1EFE4), color(0xFFF9F8F5)
            )
        )

        val GruvboxDark = TerminalColorScheme(
            name = "Gruvbox Dark",
            foreground = "#EBDBB2",
            background = "#282828",
            ansiColors = intArrayOf(
                color(0xFF282828), color(0xFFCC241D), color(0xFF98971A), color(0xFFD79921),
                color(0xFF458588), color(0xFFB16286), color(0xFF689D6A), color(0xFFA89984),
                color(0xFF928374), color(0xFFFB4934), color(0xFFB8BB26), color(0xFFFABD2F),
                color(0xFF83A598), color(0xFFD3869B), color(0xFF8EC07C), color(0xFFEBDBB2)
            )
        )

        val Nord = TerminalColorScheme(
            name = "Nord",
            foreground = "#D8DEE9",
            background = "#2E3440",
            ansiColors = intArrayOf(
                color(0xFF3B4252), color(0xFFBF616A), color(0xFFA3BE8C), color(0xFFEBCB8B),
                color(0xFF81A1C1), color(0xFFB48EAD), color(0xFF88C0D0), color(0xFFE5E9F0),
                color(0xFF4C566A), color(0xFFBF616A), color(0xFFA3BE8C), color(0xFFEBCB8B),
                color(0xFF81A1C1), color(0xFFB48EAD), color(0xFF8FBCBB), color(0xFFECEFF4)
            )
        )

        val all = listOf(Dracula, SolarizedDark, SolarizedLight, Monokai, GruvboxDark, Nord)

        private fun color(argb: Long): Int = argb.toInt()
    }
}