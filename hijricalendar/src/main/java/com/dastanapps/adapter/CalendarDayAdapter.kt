package com.dastanapps.adapter

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView
import com.dastanapps.hijricalendar.R
import com.dastanapps.utils.DateUtils
import com.dastanapps.utils.HijriCalendarDate
import java.util.*

/**
 * Created by dastaniqbal on 25/02/2018.
 * dastanIqbal@marvelmedia.com
 * 25/02/2018 6:49
 */
class CalendarDayAdapter(context: Context, resId: Int, private val dayItemList: ArrayList<DayItemB>, pageMonth: Int, private val eventsList: ArrayList<Calendar>) : ArrayAdapter<DayItemB>(context, resId, dayItemList) {
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
        val dayHijriLabel: TextView? = view?.findViewById(R.id.hijriLabel)
        val dayIcon: ImageView? = view?.findViewById(R.id.dayIcon)
        val day = GregorianCalendar()
        day.time = dayItemB.date


        if (isCurrentMonthDay(day)) {
            setEvent(day, dayIcon);
            if (DateUtils.isTodaysDate(todaysDay, day)) {
                dayLabel?.setTextColor(Color.RED)
            }
            dayLabel?.text = day.get(Calendar.DAY_OF_MONTH).toString()
            dayHijriLabel?.text = HijriCalendarDate.getSimpleDateDay(day, 0)
           // dayLabel?.visibility=View.VISIBLE
        } else {
            dayLabel?.text = ""
            dayIcon?.setImageBitmap(null)
            //dayIcon?.visibility = View.GONE
        }
        return view
    }

    private fun setEvent(day: Calendar, dayIcon: ImageView?) {
        eventsList.filter { day == it }.forEach {
            dayIcon?.setImageResource(android.R.drawable.ic_delete)
           // dayIcon?.visibility = View.VISIBLE
        }
    }

    private fun isCurrentMonthDay(day: Calendar): Boolean {
        return day.get(Calendar.MONTH) == mPageMonth
    }
}