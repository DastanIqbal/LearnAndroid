package com.dastanapps.appwidget.countdown

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.dastanapps.appwidget.R

class CountdownActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_countdown)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }


        val instance = AppWidgetManager.getInstance(this)
        val component = ComponentName(this, CountdownTimerAppWidgetProvider::class.java)
        val appWidgetIds = instance.getAppWidgetIds(component)
        CountdownTimerAppWidgetProvider().onUpdate(this, instance, appWidgetIds)

    }
}