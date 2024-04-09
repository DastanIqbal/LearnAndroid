package com.dastanapps.appwidget.countdown

import android.appwidget.AppWidgetManager
import android.content.Context
import android.os.SystemClock
import android.widget.RemoteViews
import com.dastanapps.appwidget.R
import java.util.Timer
import java.util.TimerTask


class CountdownTask(
    private val context: Context,
    private val views: RemoteViews,
    private val widgetId: Int,
    private val mwhen: Long
) {
    private var lastRemainingTime = -1
    private var timer: Timer? = null

    
    private inner class CountdownTimerTask : TimerTask() {
        override fun run() {
            this@CountdownTask.refresh()
        }
    }

    fun start(interval: Int) {
        stopTimer()
        this.timer = Timer()
        val interval2 = if (interval == 1) { 200 } else { interval * 1000 }
        timer?.schedule(CountdownTimerTask(),0L, interval2.toLong())
    }

    fun refresh() {
        var remainingTime = ((this.mwhen - SystemClock.elapsedRealtime()) / 1000).toInt()
        if (remainingTime <= 0) {
            remainingTime = 0
            stopTimer()
        }
        if (this.lastRemainingTime != remainingTime) {
            this.lastRemainingTime = remainingTime
            val seconds = remainingTime % 60
            val minutes = (remainingTime / 60) % 60
            val hours = remainingTime / 3600
            val time = String.format("%02d:%02d:%02d", hours, minutes, seconds)

            views.setTextViewText(R.id.timer_text, time)

            AppWidgetManager.getInstance(this.context).updateAppWidget(this.widgetId, this.views)
        }
    }

    fun reset() {
        stopTimer()
        views.setTextViewText(R.id.timer_text, context.getText(R.string.timer_uninitialised))
        AppWidgetManager.getInstance(this.context).updateAppWidget(this.widgetId, this.views)
    }

    fun stop() {
        stopTimer()
    }

    private fun stopTimer() {
        if (this.timer != null) {
            timer!!.cancel()
            this.timer = null
        }
    }

    companion object {
        private const val LOGD = false
        private const val TAG = "CountdownTask"
    }
}