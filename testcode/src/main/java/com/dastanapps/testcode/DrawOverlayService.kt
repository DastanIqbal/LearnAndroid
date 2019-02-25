package com.dastanapps.testcode

import android.app.Service
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.view.WindowManager.LayoutParams.*
import android.widget.FrameLayout
import androidx.core.content.ContextCompat
import com.dastanapps.dastanlib.NotificationB
import com.dastanapps.dastanlib.utils.CommonUtils

class DrawOverlayService : Service() {
    private lateinit var windowManager: WindowManager
    private lateinit var surfaceView: View
    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        val notificationB = NotificationB()
        notificationB.smallIcon = R.drawable.ic_stat_nightfilter
        notificationB.title = "NightFilter"
        notificationB.pendingIntent = Intent(this, MainActivity::class.java)
        notificationB.channelId = "id_night_filter"
        notificationB.channelName = "Night Filter"
        notificationB.cancelable = false
        CommonUtils.openNotification2(this, notificationB)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        addViewToWindow()
        return Service.START_REDELIVER_INTENT
    }

    private fun addViewToWindow() {
        val params = WindowManager.LayoutParams()
        params.width = ViewGroup.LayoutParams.MATCH_PARENT
        params.height = ViewGroup.LayoutParams.MATCH_PARENT
        params.format = PixelFormat.TRANSLUCENT
        params.gravity = Gravity.START or Gravity.TOP

        params.type = if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O)
            WindowManager.LayoutParams.TYPE_SYSTEM_OVERLAY
        else
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY

        params.flags = FLAG_NOT_TOUCHABLE or
                FLAG_FULLSCREEN or
                FLAG_LAYOUT_IN_SCREEN or
                FLAG_LAYOUT_INSET_DECOR or
                FLAG_NOT_TOUCH_MODAL or
                FLAG_LAYOUT_NO_LIMITS or
                FLAG_NOT_FOCUSABLE

        surfaceView = FrameLayout(this)
        surfaceView.setBackgroundColor(ContextCompat.getColor(this, R.color.color_overlay))

        windowManager.addView(surfaceView, params)
    }

    override fun onDestroy() {
        super.onDestroy()
        windowManager.removeView(surfaceView)
    }
}
