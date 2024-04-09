package com.dastanapps.appwidget

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView

/* loaded from: classes.dex */
class TimerSpinnerAdapter(context: Context, private val m_timers: List<Timer>?) : BaseAdapter() {
    private val m_inflater = context.getSystemService("layout_inflater") as LayoutInflater

    // android.widget.Adapter
    override fun getCount(): Int {
        return m_timers!!.size
    }

    // android.widget.Adapter
    override fun getItem(position: Int): Any {
        return m_timers!![position]
    }

    // android.widget.Adapter
    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    // android.widget.Adapter
    override fun getView(position: Int, convertView: View, parent: ViewGroup): View {
        val text = if (convertView == null) {
            m_inflater.inflate(17367048, null as ViewGroup?) as TextView
        } else {
            convertView as TextView
        }
        text.setText(R.string.recently_used)
        return text
    }

    // android.widget.BaseAdapter, android.widget.SpinnerAdapter
    override fun getDropDownView(position: Int, convertView: View, parent: ViewGroup): View {
        return generateFromResource(
            R.layout.spinner_dropdown_view, true,
            m_timers!![position], convertView, parent
        )
    }

    private fun generateFromResource(
        resourceId: Int,
        icon: Boolean,
        timer: Timer,
        convertView: View?,
        parent: ViewGroup
    ): View {
        val layout = convertView ?: m_inflater.inflate(resourceId, null as ViewGroup?)
        val timeText = layout.findViewById<View>(R.id.spinner_time) as TextView
        if (icon) {
            val which = if (timer.isSilent) R.drawable.speaker_mute else R.drawable.speaker
            timeText.setCompoundDrawablesWithIntrinsicBounds(which, 0, 0, 0)
        }
        val descriptionText = layout.findViewById<View>(R.id.spinner_description) as TextView
        val timeString = String.format("%02d:%02d:%02d", timer.hours, timer.minutes, timer.seconds)
        timeText.text = timeString
        descriptionText.text = timer.description
        return layout
    }
}