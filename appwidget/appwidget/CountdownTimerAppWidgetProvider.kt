package com.dastanapps.appwidget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.RemoteViews

/* loaded from: classes.dex */
class CountdownTimerAppWidgetProvider : AppWidgetProvider() {
    // android.appwidget.AppWidgetProvider
    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        for (appWidgetId in appWidgetIds) {
            val intent = Intent(context, CountdownTimerService::class.java)
            intent.setAction(CountdownTimerService.Companion.INTENT_ADD_WIDGET)
            intent.putExtra(CountdownTimerService.Companion.INTENT_DATA_WIDGET_ID, appWidgetId)
            context.startService(intent)
            val views = buildRemoteView(context, appWidgetId, null)
            appWidgetManager.updateAppWidget(appWidgetId, views)
        }
    }

    // android.appwidget.AppWidgetProvider
    override fun onEnabled(context: Context) {
        val intent = Intent(context, CountdownTimerService::class.java)
        intent.setAction(CountdownTimerService.Companion.INTENT_RESET_ALARMS)
        context.startService(intent)
    }

    // android.appwidget.AppWidgetProvider
    override fun onDeleted(context: Context, appWidgetIds: IntArray) {
        for (appWidgetId in appWidgetIds) {
            val intent = Intent(context, CountdownTimerService::class.java)
            intent.setAction(CountdownTimerService.Companion.INTENT_REMOVE_WIDGET)
            intent.putExtra(CountdownTimerService.Companion.INTENT_DATA_WIDGET_ID, appWidgetId)
            context.startService(intent)
        }
    }

    companion object {
        fun buildRemoteView(context: Context, widgetId: Int, description: String?): RemoteViews {
            val views: RemoteViews
            val intent = Intent(context, NewTimerActivity::class.java)
            intent.setData(Uri.parse("widget://$widgetId"))
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            intent.putExtra(CountdownTimerService.Companion.INTENT_DATA_WIDGET_ID, widgetId)
            val pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
            if (description == null) {
                views = RemoteViews(context.packageName, R.layout.countdown_timer_widget)
            } else {
                views = RemoteViews(context.packageName, R.layout.countdown_timer_widget_desc)
                views.setTextViewText(R.id.description_text, description)
            }
            views.setOnClickPendingIntent(R.id.timer_text, pendingIntent)
            return views
        }
    }
}