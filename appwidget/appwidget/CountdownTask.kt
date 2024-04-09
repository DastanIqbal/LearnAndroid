package com.dastanapps.appwidget

import android.appwidget.AppWidgetManager
import android.content.Context
import android.os.SystemClock
import android.widget.RemoteViews
import java.util.Timer
import java.util.TimerTask

/* loaded from: classes.dex */
class CountdownTask(
    private val m_context: Context,
    private val m_views: RemoteViews,
    private val m_widgetId: Int,
    private val m_when: Long
) {
    private var m_lastRemainingTime = -1
    private var m_timer: Timer? = null

    /* loaded from: classes.dex */
    private inner class CountdownTimerTask private constructor() : TimerTask() {
        /* synthetic */
        constructor(countdownTask: CountdownTask?, countdownTimerTask: CountdownTimerTask?) : this()

        // java.util.TimerTask, java.lang.Runnable
        override fun run() {
            this@CountdownTask.refresh()
        }
    }

    fun start(interval: Int) {
        stopTimer()
        this.m_timer = Timer()
        val interval2 = if (interval == 1) {
            200
        } else {
            interval * 1000
        }
        m_timer!!.scheduleAtFixedRate(CountdownTimerTask(this, null), 0L, interval2.toLong())
    }

    fun refresh() {
        var remainingTime = ((this.m_when - SystemClock.elapsedRealtime()) / 1000).toInt()
        if (remainingTime <= 0) {
            remainingTime = 0
            stopTimer()
        }
        if (this.m_lastRemainingTime != remainingTime) {
            this.m_lastRemainingTime = remainingTime
            val seconds = remainingTime % 60
            val minutes = (remainingTime / 60) % 60
            val hours = remainingTime / 3600
            val time = String.format("%02d:%02d:%02d", hours, minutes, seconds)
            m_views.setTextViewText(R.id.timer_text, time)
            AppWidgetManager.getInstance(this.m_context)
                .updateAppWidget(this.m_widgetId, this.m_views)
        }
    }

    fun reset() {
        stopTimer()
        m_views.setTextViewText(R.id.timer_text, m_context.getText(R.string.timer_uninitialised))
        AppWidgetManager.getInstance(this.m_context).updateAppWidget(this.m_widgetId, this.m_views)
    }

    fun stop() {
        stopTimer()
    }

    private fun stopTimer() {
        if (this.m_timer != null) {
            m_timer!!.cancel()
            this.m_timer = null
        }
    }

    companion object {
        private const val LOGD = false
        private const val TAG = "CountdownTask"
    }
}