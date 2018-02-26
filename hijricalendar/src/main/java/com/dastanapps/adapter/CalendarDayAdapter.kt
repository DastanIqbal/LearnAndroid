package com.dastanapps.adapter

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import com.dastanapps.hijricalendar.R
import com.dastanapps.utils.DateUtils
import java.util.*

/**
 * Created by dastaniqbal on 25/02/2018.
 * dastanIqbal@marvelmedia.com
 * 25/02/2018 6:49
 */
class CalendarDayAdapter(context: Context, resId: Int, private val dayItemList: ArrayList<DayItemB>, pageMonth: Int) : ArrayAdapter<DayItemB>(context, resId, dayItemList) {
    private var mPageMonth = 0
    private val todaysDay = DateUtils.getCalendar()

    init {
        mPageMonth = if (pageMonth < 0) 11 else pageMonth
    }

    override fun getCount(): Int {
        return dayItemList.size
    }

    override fun getItem(position: Int): DayItemB {
        return dayItemList[position]
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View? {
        var view = convertView
        if (view == null) {
            view = LayoutInflater.from(context).inflate(R.layout.cal_item, parent, false)
        }
        val dayItemB = getItem(position)
        val dayLabel: TextView? = view?.findViewById(R.id.dayLabel)
        val day = GregorianCalendar()
        day.time = dayItemB.date
        if (isCurrentMonthDay(day)) {
            dayLabel?.text = day.get(Calendar.DAY_OF_MONTH).toString()
        } else {
            dayLabel?.text = ""
        }

        if (DateUtils.isTodaysDate(todaysDay,day)) {
            dayLabel?.setTextColor(Color.RED)
        }
        return view
    }

    private fun isCurrentMonthDay(day: Calendar): Boolean {
        return day.get(Calendar.MONTH) == mPageMonth
    }
}