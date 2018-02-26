package com.dastanapps.hijricalendar

import android.os.Bundle
import android.support.v4.view.ViewPager
import android.support.v7.app.AppCompatActivity
import android.util.Log
import com.dastanapps.adapter.CalendarPagerAdapter
import com.dastanapps.utils.CalendarUtils
import com.dastanapps.utils.DateUtils
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*

class MainActivity : AppCompatActivity() {

    val FIRST_VISIBLE_PAGE = CalendarPagerAdapter.MAX_VALUE / 2
    val calendarUtils = CalendarUtils()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val currentMonth = calendarUtils.currendDate.get(Calendar.MONTH)
        calendarUtils.currendDate.set(Calendar.MONTH, -FIRST_VISIBLE_PAGE)

        cal_vp.addOnPageChangeListener(onPageChangeListener)
        cal_vp.adapter = CalendarPagerAdapter(this, calendarUtils)
        cal_vp.currentItem = FIRST_VISIBLE_PAGE + currentMonth
    }

    private var mCurrentPage: Int = 0
    private val onPageChangeListener = object : ViewPager.OnPageChangeListener {
        override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
            Log.d("DEBUG", "onPageScrolled")
        }

        /**
         * This method set calendar header label
         *
         * @param position Current ViewPager position
         * @see ViewPager.OnPageChangeListener
         */
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
        currentDateLabel.text = DateUtils.getMonthAndYearDate(this, calendar)
    }
}
