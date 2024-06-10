package com.dastanapps.widgets

import android.os.Bundle
import android.text.SpannableString
import android.text.SpannableStringBuilder
import android.text.TextUtils
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.dastanapps.widgets.databinding.ActivityTextViewBinding

class TextViewActivity : AppCompatActivity() {
    private lateinit var binding: ActivityTextViewBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityTextViewBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }


        binding.mediaArtist.post {
            binding.mediaArtist.setText(
                binding.mediaArtist.truncateLines(
                    text = "Please note that you must update the annual declaration data to continue payment of retirement pension salary",
                )
            )
        }
    }

    fun TextView.truncateLines(
        text: String
    ): String {

        val ellipsis = Typography.ellipsis.toString()

        val ellipsisSpannable = SpannableString(ellipsis)
        val spannableStringBuilder = SpannableStringBuilder()

        var result = text
        val availableScreenWidth = width.toFloat()
        var availableTextWidth = availableScreenWidth * maxLines
        val originalText = text.toString()
//    var ellipsizedText = customEllipsize(originalText, availableTextWidth)
        var ellipsizedText = TextUtils.ellipsize(text, paint, availableTextWidth, ellipsize)

        if (ellipsizedText.toString() != originalText) {
            // If the ellipsizedText is different than the original text, this means that it didn't fit and got indeed ellipsized.
            // Calculate the new availableTextWidth by taking into consideration the size of the custom ellipsis, too.
            availableTextWidth = (availableScreenWidth - paint.measureText(ellipsis)) * maxLines
            ellipsizedText = customEllipsize(originalText, availableTextWidth)
            val defaultEllipsisStart = ellipsizedText.indexOf(ellipsis)
            val defaultEllipsisEnd = defaultEllipsisStart + 1

            spannableStringBuilder.clear()

            // Update the text with the ellipsized version and replace the default ellipsis with the custom one.
            result = spannableStringBuilder.append(ellipsizedText).replace(defaultEllipsisStart, defaultEllipsisEnd, ellipsisSpannable).toString()
        }

        return result

    }

    private fun getDefaultEllipsis(): Char {
        return Typography.ellipsis
    }

    private fun TextView.customEllipsize(text: String, availableWidth: Float): String {
        val ellipsis = getDefaultEllipsis().toString()
        val ellipsisWidth = paint.measureText(ellipsis)
        val words = text.split(" ")
        val startBuilder = StringBuilder()
        val endBuilder = StringBuilder()

        var startWidth = 0f
        var endWidth = 0f
        var startIndex = 0
        var endIndex = words.size - 1

        // Accumulate words from the start until half of the available width
        while (startIndex <= endIndex) {
            val wordWidth = paint.measureText(words[startIndex])
            if (startWidth + wordWidth + ellipsisWidth <= availableWidth / 2) {
                startBuilder.append(words[startIndex]).append(" ")
                startWidth += wordWidth + paint.measureText(" ")
                startIndex++
            } else {
                break
            }
        }

        // Accumulate words from the end until half of the available width
        while (startIndex <= endIndex) {
            val wordWidth = paint.measureText(words[endIndex])
            if (endWidth + wordWidth + ellipsisWidth <= availableWidth / 2) {
                endBuilder.insert(0, " ").insert(0, words[endIndex])
                endWidth += wordWidth + paint.measureText(" ")
                endIndex--
            } else {
                break
            }
        }

        // Combine the start and end parts with the ellipsis in the middle
        val combinedText = startBuilder.toString().trim() + ellipsis + endBuilder.toString().trim()

        // If the combined text exceeds the available width, further trim the text
        if (paint.measureText(combinedText) > availableWidth) {
            return customEllipsize(startBuilder.toString().trim() + " " + endBuilder.toString().trim(), availableWidth)
        }

        return combinedText
    }

}