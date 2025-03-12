package com.example.fontfamily

import android.annotation.SuppressLint
import android.content.res.Configuration
import android.os.Bundle
import android.widget.Button
import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.ParagraphStyle
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.core.text.buildSpannedString
import androidx.core.text.color
import com.example.fontfamily.ui.theme.LearnAndroidTheme
import java.util.Locale


class MainActivity : ComponentActivity() {
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        enableEdgeToEdge()
//        setContent {
//            LearnAndroidTheme {
//                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
//                    Greeting(
//                        name = "Android",
//                        modifier = Modifier.padding(innerPadding)
//                    )
//                }
//            }
//        }
//    }

    companion object {
        var currentLanguage = "en" // Default language is English
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val buttonChangeLanguage = findViewById<Button>(R.id.buttonChangeLanguage)
        buttonChangeLanguage.setOnClickListener {
            // Change language to French
            currentLanguage =
                if (currentLanguage == "en") "ur" else if (currentLanguage == "ur") "en" else "en"
            setAppLanguage(currentLanguage)
            recreate() // Restart the activity to apply the new language
        }

        findViewById<ComposeView>(R.id.composeView).setContent {
            MixedLanguageList1()
        }
    }

    private fun setAppLanguage(languageCode: String) {
        val locale: Locale = Locale(languageCode)
        Locale.setDefault(locale)

        val resources = resources
        val config: Configuration = resources.configuration
        config.setLocale(locale)
        config.setLayoutDirection(locale)
        resources.updateConfiguration(config, resources.displayMetrics)
    }
}

@Composable
fun MixedLanguageList() {
    val urduItems = listOf("اردو", "انگریزی", "پاکستان", "کامپوز")
    val mixedItems = listOf("اردو", "English", "پاکستان", "Compose")

    // Concatenate items in the desired format: mixedItems[] + (urduItems)
    val combinedItems = mixedItems.mapIndexed { index, mixedItem ->
        "$mixedItem (${urduItems.getOrNull(index) ?: ""})"
    }

    LazyColumn(
        modifier = Modifier.fillMaxWidth()
    ) {
        items(combinedItems) { item ->
            val currentLocale = Locale.getDefault()
            val isUrduLocale = currentLocale.language == "ur"
            val textAlign = if (isUrduLocale) TextAlign.End else TextAlign.Start
            val overallTextDirection = if (isUrduLocale) TextDirection.Rtl else TextDirection.Ltr
            val layoutDirection = if (isUrduLocale) LayoutDirection.Rtl else LayoutDirection.Ltr



            CompositionLocalProvider(LocalLayoutDirection provides layoutDirection) { // Keep overall LTR for container
                val annotatedString = buildAnnotatedString {
                    val parts = item.split(" (") // Split into English and Urdu parts
                    val englishPart = parts.getOrNull(0) ?: ""
                    val urduPartWithParenthesis = parts.getOrNull(1) ?: ""
                    val urduPart = urduPartWithParenthesis.removeSuffix(")")

                    // English part (implicitly LTR) - No explicit styling needed!
                    append(englishPart)


                    // Urdu part (RTL) - Explicit SpanStyle for RTL direction
                    if (urduPart.isNotEmpty()) {
                        withStyle(SpanStyle(color = Color.Black)) {
                            withStyle(ParagraphStyle(textDirection = TextDirection.Rtl)){
                                append(" (") // Add back the opening parenthesis
                                append(urduPart)
                                append(")") // Add back the closing parenthesis
                            }
                        }
                    }
                }
                Text(
                    text = annotatedString,
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth(),
                    style = TextStyle(
                        textAlign = textAlign,
                        textDirection = overallTextDirection
                    ),
                    maxLines = 1
                )
            }
        }
    }
}

@Composable
fun MixedLanguageList1() {
    val urduItems = listOf("اردو", "انگریزی", "پاکستان", "کامپوز")
    val mixedItems = listOf("اردو", "English", "پاکستان", "Compose")

    // Concatenate items in the desired format: mixedItems[] + (urduItems)
    val combinedItems = mixedItems.mapIndexed { index, mixedItem ->
        "$mixedItem (${urduItems.getOrNull(index) ?: ""})"
    }

    LazyColumn(
        modifier = Modifier.fillMaxWidth()
    ) {
        items(combinedItems) { item ->
            val currentLocale = Locale.getDefault()
            CompositionLocalProvider(
                LocalLayoutDirection provides if (currentLocale.language == "ur") {
                    LayoutDirection.Rtl
                } else {
                    LayoutDirection.Ltr
                }
            ) {
                Row(
                    modifier = Modifier
                        .wrapContentWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween // Use SpaceBetween for alignment
                ) {
                    val parts = item.split(" (")
                    val englishPart = parts.getOrNull(0) ?: ""
                    val urduPartWithParenthesis = parts.getOrNull(1) ?: ""
                    val urduPart = urduPartWithParenthesis.removeSuffix(")")

                    Text(
                        text = englishPart,
                        style = TextStyle(
                            textAlign = if(currentLocale.language == "ur"){
                                TextAlign.End
                            }else{
                                TextAlign.Start
                            },
                            textDirection = if(currentLocale.language == "ur"){
                                TextDirection.Rtl
                            }else{
                                TextDirection.Ltr
                            }
                        ),
                        modifier = Modifier.weight(
                            1f,
                            fill = false
                        ) // Give weight to English part to push Urdu to end
                    )

                    if (urduPart.isNotEmpty()) {
                        CompositionLocalProvider(LocalLayoutDirection provides if(currentLocale.language == "ur"){
                            LayoutDirection.Ltr
                        }else{
                            LayoutDirection.Rtl
                        }) {
                            Text(
                                text = "($urduPart)",
                                style = TextStyle(
                                    textAlign = if(currentLocale.language == "ur"){
                                        TextAlign.Start
                                    }else{
                                        TextAlign.End
                                    },
                                    textDirection = if(currentLocale.language == "ur"){
                                        TextDirection.Ltr
                                    }else{
                                        TextDirection.Rtl
                                    }
                                ),
                                modifier = Modifier.weight(
                                    1f,
                                    fill = false
                                ), // Also weight Urdu part, but less important
                                textAlign = if(currentLocale.language == "ur"){
                                    TextAlign.Start
                                }else{
                                    TextAlign.End
                                } // Ensure Urdu text is aligned to the end of its allocated space
                            )
                        }
                    }
                }
            }
        }
    }
}

fun isUrdu(text: String): Boolean {
    // Simple check to detect Urdu text (you can improve this logic)
    return text.any { it in '\u0600'..'\u06FF' }
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    LearnAndroidTheme {
//        Greeting("Android")
    }
}