package com.dastanapps.adapter

import android.content.Context
import android.support.v4.view.PagerAdapter
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.dastanapps.hijricalendar.R
import com.dastanapps.ui.CalendarGridView
import com.dastanapps.utils.CalendarUtils
import com.dastanapps.utils.DateUtils
import java.util.*
import kotlin.collections.ArrayList

/**
 * Created by dastaniqbal on 25/02/2018.

 * 25/02/2018 6:21
 */
class CalendarPagerAdapter(private val context: Context, private val calendarUtils: CalendarUtils) : PagerAdapter() {
    var mCalGridView: CalendarGridView? = null
    var eventsList = ArrayList<Calendar>()

    companion object {
        val MAX_VALUE = 2401
    }

    override fun instantiateItem(container: ViewGroup?, position: Int): CalendarGridView? {
        Log.d("DEBUG", "instantiateItem " + position)
        mCalGridView = LayoutInflater.from(context).inflate(R.layout.cal_gridview, null, false) as CalendarGridView?
        loadEvents(position);
        loadMonth(position)
        container?.addView(mCalGridView)
        return mCalGridView
    }

    private fun loadEvents(position: Int) {
        eventsList.clear()
        val cal1 = DateUtils.getCalendar()
        cal1.set(Calendar.DAY_OF_MONTH, 1)
        eventsList.add(cal1)

        val cal2 = DateUtils.getCalendar()
        cal2.set(Calendar.DAY_OF_MONTH, 7)
        cal2.set(Calendar.MONTH, 2)
        eventsList.add(cal2)

        val cal3 = DateUtils.getCalendar()
        cal3.set(Calendar.DAY_OF_MONTH, 10)
        cal3.set(Calendar.MONTH, 3)
        eventsList.add(cal3)

        val cal4 = DateUtils.getCalendar()
        cal4.set(Calendar.DAY_OF_MONTH, 20)
        cal4.set(Calendar.MONTH, 4)
        eventsList.add(cal4)

        val cal5 = DateUtils.getCalendar()
        cal5.set(Calendar.DAY_OF_MONTH, 28)
        cal5.set(Calendar.MONTH, 5)
        eventsList.add(cal5)
    }

    override fun destroyItem(container: ViewGroup?, position: Int, `object`: Any?) {
        container?.removeView(`object` as View?)
    }

    override fun getItemPosition(`object`: Any): Int {
        return PagerAdapter.POSITION_NONE
    }

    override fun isViewFromObject(view: View?, `object`: Any?): Boolean {
        return view == `object`
    }

    override fun getCount(): Int {
        return MAX_VALUE
    }

    private var mPageMonth: Int = 0

    /**
     * This method fill calendar GridView with days
     *
     * @param position Position of current page in ViewPager
     */
    private fun loadMonth(position: Int) {
        val days = ArrayList<DayItemB>()

        // Get Calendar object instance
        val calendar = calendarUtils.currendDate.clone() as Calendar
        val totDaysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
        val whichMonth = calendar.get(Calendar.MONTH)
        Log.d("DEBUG", "Total Days in Month $totDaysInMonth")
        Log.d("DEBUG", "The month is $whichMonth")

        // Add months to Calendar (a number of months depends on ViewPager position)
        calendar.add(Calendar.MONTH, position)

        // Set day of month as 1
        calendar.set(Calendar.DAY_OF_MONTH, 1)

        // Get a number of the first day of the week
        val dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)

        // Count when month is beginning
        val monthBeginningCell = dayOfWeek + if (dayOfWeek == 1) 5 else -2

        // Subtract a number of beginning days, it will let to load a part of a previous month
        calendar.add(Calendar.DAY_OF_MONTH, -monthBeginningCell)

        /*
        Get all days of one page (42 is a number of all possible cells in one page
        (a part of previous month, current month and a part of next month))
         */
        while (days.size < 42) {
            days.add(DayItemB(calendar.time))
            calendar.add(Calendar.DAY_OF_MONTH, 1)
        }

        mPageMonth = calendar.get(Calendar.MONTH) - 1
        val calendarDayAdapter = CalendarDayAdapter(context, R.layout.cal_item, days, mPageMonth, eventsList)
        mCalGridView?.adapter = calendarDayAdapter
    }
}