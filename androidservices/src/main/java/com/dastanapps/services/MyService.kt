package com.dastanapps.services

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Binder
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.dastanapps.androidservices.R

class MyService : Service() {

    inner class LocalBinder : Binder() {
        fun getLocalService(): MyService {
            return this@MyService
        }
    }

    val mBinder = LocalBinder()
    private val mNM by lazy {
        getSystemService(NOTIFICATION_SERVICE) as NotificationManager?
    }

    override fun onBind(intent: Intent): IBinder {
        return mBinder
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        buildNotification()
        return START_NOT_STICKY
    }

    private val channelId = "id_myservice_channel"
    private fun buildNotificationChannel(appContext: Context?) {
        if (appContext != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val chan1 = NotificationChannel(channelId,
                    "myservice",
                    NotificationManager.IMPORTANCE_DEFAULT)

            mNM?.createNotificationChannel(chan1)
        }
    }

    fun buildNotification() {
        buildNotificationChannel(applicationContext)
        mNM?.let {
            val mBuilder = NotificationCompat.Builder(applicationContext,
                    channelId)
            val notification = mBuilder
                    .setContentTitle("MyService")
                    .setContentText("Testing background service notification")
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .build()
            startForeground(20, notification)
        } ?: stopSelf()
    }

    override fun onDestroy() {
        super.onDestroy()
        mNM?.cancel(20)
    }

    fun stopService() {
        stopSelf()
    }
}
