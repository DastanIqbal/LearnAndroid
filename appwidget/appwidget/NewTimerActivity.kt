package com.dastanapps.appwidget

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import android.widget.AdapterView.OnItemSelectedListener
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.Spinner
import com.android.example.NumberPicker
import java.io.FileNotFoundException
import java.io.IOException
import java.io.ObjectOutputStream

/* loaded from: classes.dex */
class NewTimerActivity : Activity(), View.OnClickListener, OnItemSelectedListener {
    private var m_recentTimers: MutableList<Timer>? = null
    private var m_widgetId = 0

    // android.app.Activity
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(1)
        setContentView(R.layout.new_timer)
        val pickerHours = findViewById<View>(R.id.hours) as NumberPicker
        val pickerMinutes = findViewById<View>(R.id.minutes) as NumberPicker
        val pickerSeconds = findViewById<View>(R.id.seconds) as NumberPicker
        val recentTimers = findViewById<View>(R.id.recent_timers) as Spinner
        this.m_recentTimers = readState()
        val adapter = TimerSpinnerAdapter(this, this.m_recentTimers)
        recentTimers.adapter = adapter
        recentTimers.onItemSelectedListener = this
        pickerHours.setRange(0, 23)
        pickerMinutes.setRange(0, 59)
        pickerMinutes.setFormatter(NumberPicker.TWO_DIGIT_FORMATTER)
        pickerSeconds.setRange(0, 59)
        pickerSeconds.setFormatter(NumberPicker.TWO_DIGIT_FORMATTER)
        val startButton = findViewById<View>(R.id.start_button) as Button
        startButton.setOnClickListener(this)
        val cancelButton = findViewById<View>(R.id.cancel_button) as Button
        cancelButton.setOnClickListener(this)
        val lastTimer = m_recentTimers!![0]
        setTimer(lastTimer)
        val intent = intent
        this.m_widgetId =
            intent.getIntExtra(CountdownTimerService.Companion.INTENT_DATA_WIDGET_ID, -1)
        if (this.m_widgetId == -1) {
            finish()
        }
    }

    // android.app.Activity
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    // android.app.Activity
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.settings) {
            val i = Intent()
            i.setClass(this, SettingsActivity::class.java)
            startActivity(i)
            return true
        }
        return super.onContextItemSelected(item)
    }

    fun setTimer(timer: Timer) {
        val pickerHours = findViewById<View>(R.id.hours) as NumberPicker
        val pickerMinutes = findViewById<View>(R.id.minutes) as NumberPicker
        val pickerSeconds = findViewById<View>(R.id.seconds) as NumberPicker
        val description = findViewById<View>(R.id.description) as EditText
        val descStr = timer.description
        if (descStr != null) {
            description.setText(descStr)
        } else {
            description.setText("")
        }
        pickerHours.current = timer.hours
        pickerMinutes.current = timer.minutes
        pickerSeconds.current = timer.seconds
        val checkBox = findViewById<View>(R.id.silent) as CheckBox
        checkBox.isChecked = timer.isSilent
    }

    // android.widget.AdapterView.OnItemSelectedListener
    override fun onItemSelected(parent: AdapterView<*>?, view: View, position: Int, id: Long) {
        val timer = m_recentTimers!![position]
        setTimer(timer)
    }

    // android.widget.AdapterView.OnItemSelectedListener
    override fun onNothingSelected(parent: AdapterView<*>?) {
    }

    // android.view.View.OnClickListener
    override fun onClick(v: View) {
        if (v == findViewById<View>(R.id.start_button)) {
            val pickerHours = findViewById<View>(R.id.hours) as NumberPicker
            val pickerMinutes = findViewById<View>(R.id.minutes) as NumberPicker
            val pickerSeconds = findViewById<View>(R.id.seconds) as NumberPicker
            val description = findViewById<View>(R.id.description) as EditText
            val checkBox = findViewById<View>(R.id.silent) as CheckBox
            val hours = pickerHours.current
            val minutes = pickerMinutes.current
            val seconds = pickerSeconds.current
            var descStr: String? = description.text.toString()
            if (descStr == "") {
                descStr = null
            }
            val silent = checkBox.isChecked
            saveState(Timer(hours, minutes, seconds, descStr, silent))
            val intent = Intent(INTENT_NEW_TIMER)
            intent.putExtra(INTENT_DATA_DURATION, (hours * 3600) + (minutes * 60) + seconds)
            intent.putExtra(INTENT_DATA_SILENT, silent)
            intent.putExtra(INTENT_DATA_DESCRIPTION, descStr)
            intent.putExtra(CountdownTimerService.Companion.INTENT_DATA_WIDGET_ID, this.m_widgetId)
            startService(intent)
            finish()
        } else if (v == findViewById<View>(R.id.cancel_button)) {
            val intent2 = Intent(INTENT_CANCEL_TIMER)
            intent2.putExtra(CountdownTimerService.Companion.INTENT_DATA_WIDGET_ID, this.m_widgetId)
            startService(intent2)
            finish()
        }
    }

    /* JADX WARN: Removed duplicated region for block: B:11:0x0027  */ /*
        Code decompiled incorrectly, please refer to instructions dump.
        To view partially-correct code enable 'Show inconsistent code' option in preferences
    */
    private fun readState(): MutableList<Timer> {
        /*
            r8 = this;
            java.lang.String r7 = "NewTimerActivity"
            java.util.ArrayList r4 = new java.util.ArrayList
            r5 = 8
            r4.<init>(r5)
            r2 = 0
            java.io.ObjectInputStream r3 = new java.io.ObjectInputStream     // Catch: java.io.IOException -> L65 java.lang.Throwable -> L79 java.io.EOFException -> L96 java.io.FileNotFoundException -> L98
            java.lang.String r5 = "recent_timers"
            java.io.FileInputStream r5 = r8.openFileInput(r5)     // Catch: java.io.IOException -> L65 java.lang.Throwable -> L79 java.io.EOFException -> L96 java.io.FileNotFoundException -> L98
            r3.<init>(r5)     // Catch: java.io.IOException -> L65 java.lang.Throwable -> L79 java.io.EOFException -> L96 java.io.FileNotFoundException -> L98
        L15:
            java.lang.Object r1 = r3.readObject()     // Catch: java.lang.ClassNotFoundException -> L37 java.io.FileNotFoundException -> L3f java.io.EOFException -> L56 java.lang.Throwable -> L8f java.io.IOException -> L92
            if (r1 != 0) goto L2d
            if (r3 == 0) goto L8d
            r3.close()     // Catch: java.io.IOException -> L87
            r2 = r3
        L21:
            int r5 = r4.size()
            if (r5 != 0) goto L2c
            de.dimond.countdowntimer.Timer r5 = de.dimond.countdowntimer.NewTimerActivity.DEFAULT_TIMER
            r4.add(r5)
        L2c:
            return r4
        L2d:
            boolean r5 = r1 instanceof de.dimond.countdowntimer.Timer     // Catch: java.lang.ClassNotFoundException -> L37 java.io.FileNotFoundException -> L3f java.io.EOFException -> L56 java.lang.Throwable -> L8f java.io.IOException -> L92
            if (r5 == 0) goto L4e
            de.dimond.countdowntimer.Timer r1 = (de.dimond.countdowntimer.Timer) r1     // Catch: java.lang.ClassNotFoundException -> L37 java.io.FileNotFoundException -> L3f java.io.EOFException -> L56 java.lang.Throwable -> L8f java.io.IOException -> L92
            r4.add(r1)     // Catch: java.lang.ClassNotFoundException -> L37 java.io.FileNotFoundException -> L3f java.io.EOFException -> L56 java.lang.Throwable -> L8f java.io.IOException -> L92
            goto L15
        L37:
            r5 = move-exception
            r0 = r5
            java.lang.String r5 = "NewTimerActivity"
            android.util.Log.w(r5, r0)     // Catch: java.io.FileNotFoundException -> L3f java.io.EOFException -> L56 java.lang.Throwable -> L8f java.io.IOException -> L92
            goto L15
        L3f:
            r5 = move-exception
            r2 = r3
        L41:
            if (r2 == 0) goto L21
            r2.close()     // Catch: java.io.IOException -> L47
            goto L21
        L47:
            r0 = move-exception
            java.lang.String r5 = "NewTimerActivity"
            android.util.Log.w(r7, r0)
            goto L21
        L4e:
            java.lang.String r5 = "NewTimerActivity"
            java.lang.String r6 = "Object was not of class Timer!"
            android.util.Log.w(r5, r6)     // Catch: java.lang.ClassNotFoundException -> L37 java.io.FileNotFoundException -> L3f java.io.EOFException -> L56 java.lang.Throwable -> L8f java.io.IOException -> L92
            goto L15
        L56:
            r5 = move-exception
            r2 = r3
        L58:
            if (r2 == 0) goto L21
            r2.close()     // Catch: java.io.IOException -> L5e
            goto L21
        L5e:
            r0 = move-exception
            java.lang.String r5 = "NewTimerActivity"
            android.util.Log.w(r7, r0)
            goto L21
        L65:
            r5 = move-exception
            r0 = r5
        L67:
            java.lang.String r5 = "NewTimerActivity"
            android.util.Log.w(r5, r0)     // Catch: java.lang.Throwable -> L79
            if (r2 == 0) goto L21
            r2.close()     // Catch: java.io.IOException -> L72
            goto L21
        L72:
            r0 = move-exception
            java.lang.String r5 = "NewTimerActivity"
            android.util.Log.w(r7, r0)
            goto L21
        L79:
            r5 = move-exception
        L7a:
            if (r2 == 0) goto L7f
            r2.close()     // Catch: java.io.IOException -> L80
        L7f:
            throw r5
        L80:
            r0 = move-exception
            java.lang.String r6 = "NewTimerActivity"
            android.util.Log.w(r7, r0)
            goto L7f
        L87:
            r0 = move-exception
            java.lang.String r5 = "NewTimerActivity"
            android.util.Log.w(r7, r0)
        L8d:
            r2 = r3
            goto L21
        L8f:
            r5 = move-exception
            r2 = r3
            goto L7a
        L92:
            r5 = move-exception
            r0 = r5
            r2 = r3
            goto L67
        L96:
            r5 = move-exception
            goto L58
        L98:
            r5 = move-exception
            goto L41
        */
        throw UnsupportedOperationException("Method not decompiled: de.dimond.countdowntimer.NewTimerActivity.readState():java.util.List")
    }

    private fun saveState(newTimer: Timer) {
        m_recentTimers!!.remove(newTimer)
        m_recentTimers!!.add(0, newTimer)
        while (m_recentTimers!!.size > MAX_RECENT_TIMERS) {
            m_recentTimers!!.removeAt(MAX_RECENT_TIMERS)
        }
        try {
            val oos = ObjectOutputStream(openFileOutput(RECENT_TIMERS_FILE, 0))
            for (t in m_recentTimers!!) {
                oos.writeObject(t)
            }
        } catch (e: FileNotFoundException) {
            Log.w(TAG, e)
        } catch (e2: IOException) {
            Log.w(TAG, e2)
        }
    }

    companion object {
        const val INTENT_CANCEL_TIMER: String =
            "de.dimond.countdowntimer.intent.ACTION_CANCEL_TIMER"
        const val INTENT_DATA_DESCRIPTION: String = "DESCRIPTION"
        const val INTENT_DATA_DURATION: String = "DURATION"
        const val INTENT_DATA_SILENT: String = "SILENT"
        const val INTENT_NEW_TIMER: String = "de.dimond.countdowntimer.intent.ACTION_NEW_TIMER"
        private const val MAX_RECENT_TIMERS = 7
        private const val RECENT_TIMERS_FILE = "recent_timers"
        private const val TAG = "NewTimerActivity"
        private const val LOGD = false
        private val DEFAULT_TIMER = Timer(0, 1, 0, null, LOGD)
    }
}