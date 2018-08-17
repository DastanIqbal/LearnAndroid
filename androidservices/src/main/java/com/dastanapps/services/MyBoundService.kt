package com.dastanapps.services

import android.app.*
import android.content.Intent
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.util.Log
import android.widget.Toast
import androidx.core.app.NotificationCompat
import com.dastanapps.androidservices.MainActivity
import com.dastanapps.androidservices.R


class MyBoundService : Service() {

    inner class LocalBinder : Binder() {
        fun getService(): MyBoundService {
            return this@MyBoundService
        }
    }

    private var mNM: NotificationManager? = null
    private val mBinder = LocalBinder()

    override fun onBind(intent: Intent): IBinder {
        return mBinder
    }

    override fun onCreate() {
        super.onCreate()
        mNM = getSystemService(NOTIFICATION_SERVICE) as NotificationManager?
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.i("LocalService", "Received start id $startId: $intent");
        return START_NOT_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        // Cancel the persistent notification.
        mNM?.cancel(1023)

        // Tell the user we stopped.
        Toast.makeText(this, "Service Stopped", Toast.LENGTH_SHORT).show();
    }

    private fun createNotificationChannel(): String {
        val channelId = "id_boundservice_channel"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val chan = NotificationChannel(channelId,
                    "Bound Service",
                    NotificationManager.IMPORTANCE_DEFAULT)
            chan.lightColor = getColor(R.color.colorPrimary)
            chan.lockscreenVisibility = Notification.VISIBILITY_PRIVATE
            mNM?.createNotificationChannel(chan)
        }
        return channelId
    }

    fun showNotification() {
        val contentIntent = PendingIntent.getActivity(this, 0,
                Intent(this, MainActivity::class.java), 0)

        // Set the info for the views that show in the notification panel.
        val notification =
                NotificationCompat.Builder(this, createNotificationChannel())
                        .setSmallIcon(R.mipmap.ic_launcher)  // the status icon
                        .setTicker("Bound Service Started")  // the status text
                        .setWhen(System.currentTimeMillis())  // the time stamp
                        .setContentTitle("Bound Service")  // the label of the entry
                        .setContentText("Bound Service Started")  // the contents of the entry
                        .setContentIntent(contentIntent)  // The intent to send when the entry is clicked
                        .build()

        // Send the notification.
        //mNM?.notify(1023, notification)
        startForeground(1023, notification)
    }
}
