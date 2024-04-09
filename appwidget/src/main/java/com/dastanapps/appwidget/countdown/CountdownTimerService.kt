package com.dastanapps.appwidget.countdown

import android.app.Service
import android.appwidget.AppWidgetManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.SharedPreferences
import android.content.SharedPreferences.OnSharedPreferenceChangeListener
import android.net.Uri
import android.os.IBinder
import android.os.SystemClock
import android.util.Log
import android.widget.RemoteViews
import android.widget.Toast


class CountdownTimerService : Service() {
    private var countdownTasks: MutableMap<Int?, CountdownTask?>? = null
    private var receiver: BroadcastReceiver? = null
    val interval = 10 * 1000

    override fun onCreate() {
        startAllCountdownTasks()

        val filter = IntentFilter("android.intent.action.SCREEN_ON")
        filter.addAction("android.intent.action.SCREEN_OFF")
        this.receiver = ScreenBroadcastReceiver()
        registerReceiver(this.receiver, filter)
    }


    override fun onDestroy() {
        unregisterReceiver(this.receiver)
    }

    private fun getIntentWidgetId(intent: Intent): Int {
        val extras = intent.extras
        if (extras == null) {
            Log.w(TAG, "Received invalid Intent!")
            return -1
        } else if (extras.containsKey(INTENT_DATA_WIDGET_ID)) {
            val widgetId = extras.getInt(INTENT_DATA_WIDGET_ID)
            return widgetId
        } else {
            Log.w(TAG, "Received invalid Intent!")
            return -1
        }
    }

    override fun onStart(intent: Intent, startId: Int) {
        onStartCommand(intent, 0, startId)
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        var widgetId: Int = getIntentWidgetId(intent)
        if (intent?.action == null || widgetId == -1) {
            return START_STICKY
        }

        else if (intent.action == INTENT_REMOVE_WIDGET) {
            cancelAlarmAndTask(widgetId)
            return START_STICKY
        }

        val extras = intent.extras
        if (extras != null) {
            val duration = extras.getInt(INTENT_DATA_DURATION, interval)
            val description = extras.getString(INTENT_DATA_DESCRIPTION)

            if (widgetId == -1 || duration == -1) {
                Log.w(TAG, "Received invalid intent!")
                return START_STICKY
            }
            countdownTasks?.run {
                if (countdownTasks!!.containsKey(widgetId)) {
                    countdownTasks!![widgetId]!!.stop()
                    countdownTasks!!.remove(widgetId)
                }
            }

            val views: RemoteViews = CountdownTimerAppWidgetProvider.buildRemoteView(this, widgetId, description)
            val `when` = SystemClock.elapsedRealtime() + (duration * 1000)
            val countdownTask = CountdownTask(this, views, widgetId, `when`)
            countdownTasks!![widgetId] = countdownTask
            countdownTask.start(interval)
        }

        return START_STICKY
    }

    private fun resetWidget(widgetId: Int) {
        AppWidgetManager.getInstance(this).updateAppWidget(
            widgetId, CountdownTimerAppWidgetProvider.buildRemoteView(
                this, widgetId, null
            )
        )
    }


    fun startAllCountdownTasks() {
        countdownTasks?.run {
            for (task in this.values) {
                task!!.start(interval)
            }
        }
    }

    private fun cancelAlarmAndTask(widgetId: Int) {
        countdownTasks?.run {
            if (countdownTasks!!.containsKey(widgetId)) {
                countdownTasks!![widgetId]!!.reset()
                countdownTasks!!.remove(widgetId)
            }
        }
    }


    fun stopAllCountdownTasks() {
        for (task in countdownTasks!!.values) {
            task!!.stop()
        }
    }

    override fun onBind(intent: Intent): IBinder? {
        throw IllegalStateException("This service cannot be bound!")
    }


    private inner class ScreenBroadcastReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (intent.action == "android.intent.action.SCREEN_ON") {
                this@CountdownTimerService.startAllCountdownTasks()
                Toast.makeText(context, "Screen On", Toast.LENGTH_LONG).show()
            } else if (intent.action == "android.intent.action.SCREEN_OFF") {
                this@CountdownTimerService.stopAllCountdownTasks()
                Toast.makeText(context, "Screen Off", Toast.LENGTH_LONG).show()
            }
        }
    }

    companion object {

        const val INTENT_DATA_DESCRIPTION: String = "DESCRIPTION"
        const val INTENT_DATA_DURATION: String = "DURATION"

        const val INTENT_ADD_WIDGET: String =
            " com.dastanapps.appwidget.intent.ACTION_SERVICE_ADD_WIDGET"
        const val INTENT_DATA_WIDGET_ID: String = "WIDGET_ID"
        const val INTENT_REMOVE_WIDGET: String =
            " com.dastanapps.appwidget.intent.ACTION_SERVICE_REMOVE_WIDGET"
        const val INTENT_RESET_ALARMS: String =
            " com.dastanapps.appwidget.intent.ACTION_SERVICE_RESET_ALARMS"
        private const val LOGD = false
        private const val REFRESH_INTERVAL_KEY = "CTW_REFRESH_INTERVAL"

        private const val TAG = "CountdownTimerService"

    }
}