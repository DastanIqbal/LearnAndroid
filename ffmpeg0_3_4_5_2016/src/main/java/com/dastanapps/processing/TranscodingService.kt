package com.dastanapps.processing

import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.graphics.Color
import android.os.IBinder
import android.support.v4.app.NotificationCompat
import android.util.Log
import com.dastanapps.ffmpegjni.MainActivity
import com.dastanapps.ffmpegjni.R
import com.dastanapps.ffmpegjni.VideoKit

/**
 * Created by dastaniqbal on 25/07/2018.
 * dastanIqbal@marvelmedia.com
 * 25/07/2018 12:49
 */
class TranscodingService : Service() {
    override fun onBind(p0: Intent?): IBinder? {
        return null
    }


    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val hanldeIntent = Intent(this, MainActivity::class.java)
        val resultPendingIntent = PendingIntent.getActivity(
                this, 101, hanldeIntent, PendingIntent.FLAG_UPDATE_CURRENT)

        val mBuilder = NotificationCompat.Builder(this)
                .setContentTitle("Transcoding")
                .setContentText("Transcoding")
                .setSmallIcon(R.mipmap.ic_launcher)
                .setAutoCancel(true)
                .setColor(Color.BLACK)
                .setContentIntent(resultPendingIntent)
        //   startForeground(101, mBuilder.build())


        val cmds = CmdlineBuilder()
                .addInputPath("/sdcard/KrusoTestVideo/ezgif-3-704253d805.mp4")
             //   .customCommand("-filter_complex drawtext=fontfile=/system/fonts/Roboto-Bold.ttf:text='iqbal':fontcolor=white:fontsize=96")
                .outputPath("/sdcard/KrusoTestVideo/FFmpegDrawText.mp4")
                .build()


        val resultCode = FFmpegExecutor.execute(cmds, object : VideoKit.IVideoKit {
            override fun benchmark(bench: String) {
                Log.d("JNI::DEBUG", bench)
            }

            override fun progress(progress: String) {
                Log.d("JNI::DEBUG", progress)
            }

        });
        Log.d("JNI::DEBUG", "ResultCode:$resultCode")
        return super.onStartCommand(intent, flags, startId);
    }
}