package com.dastanapps.hijricalendar

import android.os.Bundle
import android.support.v4.view.ViewPager
import android.support.v7.app.AppCompatActivity
import android.util.Log
import com.dastanapps.adapter.CalendarPagerAdapter
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        cal_vp.adapter = CalendarPagerAdapter(this)
        cal_vp.addOnPageChangeListener(onPageChangeListener)
    }

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
        }

        override fun onPageScrollStateChanged(state: Int) {
            Log.d("DEBUG", "onPageScrollStateChanged")
        }
    }
}
