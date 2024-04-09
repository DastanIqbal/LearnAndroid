package com.dastanapps.appwidget

import android.app.AlarmManager
import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.appwidget.AppWidgetManager
import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.SharedPreferences
import android.content.SharedPreferences.OnSharedPreferenceChangeListener
import android.net.Uri
import android.os.IBinder
import android.os.SystemClock
import android.preference.PreferenceManager
import android.provider.Settings
import android.util.Log
import android.widget.RemoteViews
import java.io.EOFException
import java.io.FileNotFoundException
import java.io.IOException
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.util.Collections
import java.util.LinkedList

/* loaded from: classes.dex */
class CountdownTimerService : Service(), OnSharedPreferenceChangeListener {
    private var m_alarms: MutableMap<Int?, Alarm?>? = null
    private var m_countdownTasks: MutableMap<Int?, CountdownTask?>? = null
    private var m_preferences: SharedPreferences? = null
    private var m_receiver: BroadcastReceiver? = null

    // android.app.Service
    override fun onCreate() {
        this.m_preferences = PreferenceManager.getDefaultSharedPreferences(this)
        m_preferences.registerOnSharedPreferenceChangeListener(this)
        loadAlarms()
        scheduleAlarm()
        startAllCountdownTasks()
        val filter = IntentFilter("android.intent.action.SCREEN_ON")
        filter.addAction("android.intent.action.SCREEN_OFF")
        this.m_receiver = ScreenBroadcastReceiver(this, null)
        registerReceiver(this.m_receiver, filter)
    }

    // android.app.Service
    override fun onDestroy() {
        m_preferences!!.unregisterOnSharedPreferenceChangeListener(this)
        unregisterReceiver(this.m_receiver)
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

    // android.app.Service
    override fun onStart(intent: Intent, startId: Int) {
        onStartCommand(intent, 0, startId)
    }

    // android.app.Service
    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        var widgetId: Int
        var streamType: Int
        val sound: Uri
        if (intent?.action == null) {
            return START_STICKY
        }
        if (intent.action == INTENT_ADD_WIDGET) {
            if (getIntentWidgetId(intent) == -1) {
                return START_STICKY
            }
        } else if (intent.action == INTENT_REMOVE_WIDGET) {
            val widgetId2 = getIntentWidgetId(intent)
            if (widgetId2 == -1) {
                return START_STICKY
            }
            cancelAlarmAndTask(widgetId2)
        } else if (intent.action == INTENT_RESET_ALARMS) {
            deleteAllAlarms()
        } else if (intent.action == NewTimerActivity.Companion.INTENT_NEW_TIMER) {
            val extras = intent.extras
            if (extras != null) {
                val widgetId3 = extras.getInt(INTENT_DATA_WIDGET_ID, -1)
                val duration = extras.getInt(NewTimerActivity.Companion.INTENT_DATA_DURATION, -1)
                val description =
                    extras.getString(NewTimerActivity.Companion.INTENT_DATA_DESCRIPTION)
                val silent = extras.getBoolean(NewTimerActivity.Companion.INTENT_DATA_SILENT, LOGD)
                if (widgetId3 == -1 || duration == -1) {
                    Log.w(TAG, "Received invalid intent!")
                    return START_STICKY
                }
                if (m_countdownTasks!!.containsKey(widgetId3)) {
                    m_countdownTasks!![widgetId3]!!.stop()
                    m_countdownTasks!!.remove(widgetId3)
                }
                val views: RemoteViews = CountdownTimerAppWidgetProvider.Companion.buildRemoteView(
                    this, widgetId3, description
                )
                val `when` = SystemClock.elapsedRealtime() + (duration * 1000)
                val countdownTask = CountdownTask(this, views, widgetId3, `when`)
                m_countdownTasks!![widgetId3] = countdownTask
                val interval = m_preferences!!.getString(REFRESH_INTERVAL_KEY, "1")!!
                    .toInt()
                countdownTask.start(interval)
                addAlarm(widgetId3, `when`, description, silent)
            }
        } else if (intent.action == NewTimerActivity.Companion.INTENT_CANCEL_TIMER) {
            val extras2 = intent.extras
            if (extras2 != null) {
                val widgetId4 = extras2.getInt(INTENT_DATA_WIDGET_ID, -1)
                if (widgetId4 == -1) {
                    Log.w(TAG, "Received invalid intent!")
                    return START_STICKY
                }
                cancelAlarmAndTask(widgetId4)
                resetWidget(widgetId4)
            }
        } else if (intent.action == INTENT_ALARM_ALERT) {
            val extras3 = intent.extras
            if (extras3 == null) {
                Log.w(TAG, "Received invalid intent, Extras was null!")
                return START_STICKY
            }
            val widgetId5 = extras3.getInt(INTENT_DATA_WIDGET_ID, -1)
            if (widgetId5 == -1) {
                Log.w(TAG, "Received invalid intent!")
                return START_STICKY
            }
            val isSilent = extras3.getBoolean(INTENT_DATA_IS_SILENT, LOGD)
            val description2 = extras3.getString(NewTimerActivity.Companion.INTENT_DATA_DESCRIPTION)
            val vibrate =
                m_preferences!!.getBoolean(VIBRATE_KEY, true)
            val insistent = m_preferences!!.getBoolean(INSISTENT_KEY, LOGD)
            val streamTypeStr =
                m_preferences!!.getString(VOLUME_SOURCE_KEY, 4.toString())
            try {
                streamType = streamTypeStr!!.toInt()
            } catch (e: NumberFormatException) {
                streamType = 5
                Log.w(TAG, e)
            }
            sound = if (isSilent) {
                Uri.EMPTY
            } else {
                Uri.parse(
                    m_preferences!!.getString(
                        RINGTONE_KEY,
                        Settings.System.DEFAULT_NOTIFICATION_URI.toString()
                    )
                )
            }
            showNotification(widgetId5, description2, streamType, sound, vibrate, insistent)
            removeAlarm(widgetId5)
            if (m_countdownTasks!!.containsKey(widgetId5)) {
                m_countdownTasks!![widgetId5]!!.refresh()
                m_countdownTasks!!.remove(widgetId5)
            }
            if (m_alarms!!.size == 0) {
                stopSelf()
            }
        } else if (intent.action != INTENT_RESET_WIDGET || (getIntentWidgetId(intent).also {
                widgetId = it
            }) == -1) {
            return START_STICKY
        } else {
            resetWidget(widgetId)
        }
        return START_STICKY
    }

    fun showNotification(
        id: Int,
        description: String?,
        streamType: Int,
        sound: Uri,
        vibrate: Boolean,
        insistent: Boolean
    ) {
        val title =
            (if (description == null) "" else "$description: ").toString() + getString(R.string.timer_expired)
        val n = Notification(R.drawable.stat_notify_alarm, title, System.currentTimeMillis())
        n.defaults = 4
        n.audioStreamType = streamType
        if (sound != Uri.EMPTY) {
            n.sound = sound
        }
        if (vibrate) {
            n.vibrate = longArrayOf(0, 500, 200, 500, 200, 750)
        }
        n.flags = 16
        if (insistent) {
            n.flags = n.flags or 4
        }
        val intent = Intent(this, CountdownTimerService::class.java)
        intent.setAction(INTENT_RESET_WIDGET)
        intent.putExtra(INTENT_DATA_WIDGET_ID, id)
        intent.setData(Uri.parse("widget://$id"))
        val pendingIntent = PendingIntent.getService(this, 0, intent, PendingIntent.FLAG_IMMUTABLE)
        n.deleteIntent = pendingIntent
        //        n.setLatestEventInfo(this, title, getString(R.string.click_to_remove), pendingIntent);
        val manager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        manager.cancel(id)
        manager.notify(id, n)
    }

    private fun resetWidget(widgetId: Int) {
        AppWidgetManager.getInstance(this).updateAppWidget(
            widgetId, CountdownTimerAppWidgetProvider.Companion.buildRemoteView(
                this, widgetId, null
            )
        )
    }

    /* JADX INFO: Access modifiers changed from: private */
    fun startAllCountdownTasks() {
        val interval =
            m_preferences!!.getString(REFRESH_INTERVAL_KEY, "1")!!
                .toInt()
        for (task in m_countdownTasks!!.values) {
            task!!.start(interval)
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    fun stopAllCountdownTasks() {
        for (task in m_countdownTasks!!.values) {
            task!!.stop()
        }
    }

    private fun cancelAlarmAndTask(widgetId: Int) {
        if (m_countdownTasks!!.containsKey(widgetId)) {
            m_countdownTasks!![widgetId]!!.reset()
            m_countdownTasks!!.remove(widgetId)
        }
        removeAlarm(widgetId)
    }

    private fun loadAlarms() {
        var e: IOException?
        var ois: ObjectInputStream? = null
        var widgetId = 0
        var `object`: Any? = null
        var ois2: ObjectInputStream? = null
        this.m_alarms = HashMap<Any?, Any?>()
        this.m_countdownTasks = HashMap<Any?, Any?>()
        try {
            try {
                ois = ObjectInputStream(openFileInput(ALARMS_FILE))
                while (true) {
                    try {
                        try {
                            widgetId = ois.readInt()
                            `object` = ois.readObject()
                        } catch (e2: EOFException) {
                            ois2 = ois
                            if (ois2 != null) {
                                try {
                                    ois2.close()
                                    return
                                } catch (e3: IOException) {
                                    Log.w(TAG, e3)
                                    return
                                }
                            }
                            return
                        } catch (e4: FileNotFoundException) {
                            ois2 = ois
                            if (ois2 != null) {
                                try {
                                    ois2.close()
                                    return
                                } catch (e5: IOException) {
                                    Log.w(TAG, e5)
                                    return
                                }
                            }
                            return
                        } catch (e6: IOException) {
                            e = e6
                            ois2 = ois
                            Log.w(TAG, e)
                            if (ois2 != null) {
                                try {
                                    ois2.close()
                                } catch (e7: IOException) {
                                    Log.w(TAG, e7)
                                }
                            }
                        } catch (th: Throwable) {
                            th = th
                            ois2 = ois
                            if (ois2 != null) {
                                try {
                                    ois2.close()
                                } catch (e8: IOException) {
                                    Log.w(TAG, e8)
                                }
                            }
                            throw th
                        }
                    } catch (e9: ClassNotFoundException) {
                        Log.w(TAG, e9)
                    }
                    if (`object` == null) {
                        break
                    } else if (`object` is Alarm) {
                        val alarm = `object`
                        m_alarms[widgetId] = alarm
                        val views: RemoteViews =
                            CountdownTimerAppWidgetProvider.Companion.buildRemoteView(
                                this, widgetId, alarm.m_description
                            )
                        val task = CountdownTask(this, views, widgetId, alarm.m_when)
                        m_countdownTasks[widgetId] = task
                    } else {
                        Log.w(TAG, "Object was not of class Alarm!")
                    }
                }
            } catch (e10: EOFException) {
            } catch (e11: FileNotFoundException) {
            } catch (e12: IOException) {
                e = e12
            }
            if (ois != null) {
                try {
                    ois.close()
                    ois2 = ois
                } catch (e13: IOException) {
                    Log.w(TAG, e13)
                }
            }
            ois2 = ois
        } catch (th2: Throwable) {
            th = th2
        }
    }

    private fun saveAlarms() {
        try {
            val oos = ObjectOutputStream(openFileOutput(ALARMS_FILE, 0))
            for ((key, value) in m_alarms!!) {
                oos.writeInt(key!!)
                oos.writeObject(value)
            }
        } catch (e: FileNotFoundException) {
            Log.w(TAG, e)
        } catch (e2: IOException) {
            Log.w(TAG, e2)
        }
    }

    private fun removeAlarm(widgetId: Int) {
        if (m_alarms!!.remove(widgetId) != null) {
            saveAlarms()
            scheduleAlarm()
        }
    }

    private fun addAlarm(widgetId: Int, `when`: Long, description: String?, isSilent: Boolean) {
        m_alarms!![widgetId] = Alarm(`when`, description, isSilent)
        saveAlarms()
        scheduleAlarm()
    }

    private fun deleteAllAlarms() {
        this.m_alarms = HashMap<Any?, Any?>()
        saveAlarms()
        scheduleAlarm()
    }

    private fun scheduleAlarm() {
        val now = SystemClock.elapsedRealtime()
        var nextAlarm = smallestValue(this.m_alarms)
        while (nextAlarm != null && nextAlarm.value!!.m_when < now - 2000) {
            m_alarms!!.remove(nextAlarm.key)
            Log.w(TAG, "Removing too old alarm!")
            nextAlarm = smallestValue(this.m_alarms)
        }
        val manager = getSystemService(ALARM_SERVICE) as AlarmManager
        if (nextAlarm == null) {
            val intent = Intent(INTENT_ALARM_ALERT)
            intent.setComponent(ComponentName(this, CountdownTimerService::class.java))
            val pendingIntent = PendingIntent.getService(
                this,
                0,
                intent,
                PendingIntent.FLAG_CANCEL_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            manager.cancel(pendingIntent)
            return
        }
        val `when` = nextAlarm.value!!.m_when
        val intent2 = Intent(INTENT_ALARM_ALERT)
        intent2.setComponent(ComponentName(this, CountdownTimerService::class.java))
        intent2.putExtra(INTENT_DATA_WIDGET_ID, nextAlarm.key)
        intent2.putExtra(
            NewTimerActivity.Companion.INTENT_DATA_DESCRIPTION,
            nextAlarm.value!!.m_description
        )
        intent2.putExtra(INTENT_DATA_IS_SILENT, nextAlarm.value!!.m_isSilent)
        val pendingIntent2 = PendingIntent.getService(
            this,
            0,
            intent2,
            PendingIntent.FLAG_CANCEL_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        Log.i(TAG, "Setting alarm in " + ((`when` - now) / 1000) + " seconds!")
        manager[2, `when`] = pendingIntent2
    }

    // android.app.Service
    override fun onBind(intent: Intent): IBinder? {
        throw IllegalStateException("This service cannot be bound!")
    }

    /* loaded from: classes.dex */
    private inner class ScreenBroadcastReceiver private constructor() : BroadcastReceiver() {
        /* synthetic */
        constructor(
            countdownTimerService: CountdownTimerService?,
            screenBroadcastReceiver: ScreenBroadcastReceiver?
        ) : this()

        // android.content.BroadcastReceiver
        override fun onReceive(context: Context, intent: Intent) {
            if (intent.action == "android.intent.action.SCREEN_ON") {
                this@CountdownTimerService.startAllCountdownTasks()
            } else if (intent.action == "android.intent.action.SCREEN_OFF") {
                this@CountdownTimerService.stopAllCountdownTasks()
            }
        }
    }

    // android.content.SharedPreferences.OnSharedPreferenceChangeListener
    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences, key: String?) {
        if (key == REFRESH_INTERVAL_KEY) {
            stopAllCountdownTasks()
            startAllCountdownTasks()
        }
    }

    companion object {
        private const val ALARMS_FILE = "alarms"
        private const val INSISTENT_KEY = "CTW_INSISTENT"
        const val INTENT_ADD_WIDGET: String =
            "de.dimond.countdowntimer.intent.ACTION_SERVICE_ADD_WIDGET"
        const val INTENT_ALARM_ALERT: String = "de.dimond.countdowntimer.intent.ACTION_ALARM_ALERT"
        const val INTENT_DATA_IS_SILENT: String = "IS_SILENT"
        const val INTENT_DATA_WIDGET_ID: String = "WIDGET_ID"
        const val INTENT_REMOVE_WIDGET: String =
            "de.dimond.countdowntimer.intent.ACTION_SERVICE_REMOVE_WIDGET"
        const val INTENT_RESET_ALARMS: String =
            "de.dimond.countdowntimer.intent.ACTION_SERVICE_RESET_ALARMS"
        const val INTENT_RESET_WIDGET: String =
            "de.dimond.countdowntimer.intent.ACTION_RESET_WIDGET"
        private const val LOGD = false
        private const val REFRESH_INTERVAL_KEY = "CTW_REFRESH_INTERVAL"
        private const val RINGTONE_KEY = "CTW_RINGTONE"
        private const val TAG = "CountdownTimerService"
        private const val VIBRATE_KEY = "CTW_VIBRATE"
        private const val VOLUME_SOURCE_KEY = "CTW_VOLUME_SOURCE"
        private fun <K, V : Any> smallestValue(map: Map<K, V>?): Map.Entry<K, V>? {
            if (map!!.size == 0) {
                return null
            }
            val list: List<Map.Entry<K, V>> = LinkedList(
                map.entries
            )
            Collections.sort(list, object : Comparator<Map.Entry<K, V>> {
                // java.util.Comparator
                /* bridge */ /* synthetic */ fun compare(obj: Any?, obj2: Any?): Int {
                    return compare(obj as Map.Entry<*, *>?, obj2 as Map.Entry<*, *>?)
                }

                override fun compare(o1: Map.Entry<K, V>, o2: Map.Entry<K, V>): Int {
                    return (o1.value as Comparable<*>).compareTo(o2.value)
                }
            })
            return list[0]
        }
    }
}