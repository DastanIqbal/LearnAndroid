package com.dastanapps.ui

import android.content.Context
import android.support.v4.view.ViewPager
import android.util.AttributeSet
import android.util.Log
import android.view.LayoutInflater
import android.widget.LinearLayout
import android.widget.TextView
import com.dastanapps.adapter.CalendarPagerAdapter
import com.dastanapps.hijricalendar.R
import com.dastanapps.utils.CalendarUtils
import com.dastanapps.utils.DateUtils
import java.util.*

/**
 * Created by dastaniqbal on 26/02/2018.

 * 26/02/2018 3:32
 */
class CalendarView : LinearLayout {
    val FIRST_VISIBLE_PAGE = CalendarPagerAdapter.MAX_VALUE / 2
    val calendarUtils = CalendarUtils()
    var viewPager: CalendarViewPager? = null
    var headerText: TextView? = null

    constructor(context: Context) : super(context) {
        initLayout(context)
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        initLayout(context)
    }

    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(context, attrs, defStyle) {
        initLayout(context)
    }

    private fun initLayout(context: Context) {
        val view = LayoutInflater.from(context).inflate(R.layout.calendar_view, this)
        val currentMonth = calendarUtils.currendDate.get(Calendar.MONTH)
        calendarUtils.currendDate.set(Calendar.MONTH, -FIRST_VISIBLE_PAGE)
        viewPager = view.findViewById(R.id.cal_vp)
        headerText = view.findViewById(R.id.currentDateLabel)

        viewPager?.addOnPageChangeListener(onPageChangeListener)
        viewPager?.adapter = CalendarPagerAdapter(context, calendarUtils)
        viewPager?.currentItem = FIRST_VISIBLE_PAGE + currentMonth

        isInEditMode
    }

    private var mCurrentPage: Int = 0
    private val onPageChangeListener = object : ViewPager.OnPageChangeListener {
        override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
            Log.d("DEBUG", "onPageScrolled")
        }

        override fun onPageSelected(position: Int) {
            Log.d("DEBUG", "onPageSelected")
            mCurrentPage = position
            val calendar = calendarUtils.currendDate.clone() as Calendar
            calendar.add(Calendar.MONTH, position)
            setHeaderName(calendar)
        }

        override fun onPageScrollStateChanged(state: Int) {
            Log.d("DEBUG", "onPageScrollStateChanged")
        }
    }

    private fun setHeaderName(calendar: Calendar) {
        headerText?.text = DateUtils.getMonthAndYearDate(context, calendar)
    }
}