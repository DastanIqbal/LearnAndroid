package com.flow.framework.util;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.SystemClock;
import com.flow.framework.PspManager;
import com.flow.framework.broadcast.AReceiver;
import com.flow.framework.service.FlowService;
import java.util.concurrent.TimeUnit;

public class PullingUtils {
    private static long PERIOD = TimeUnit.MINUTES.toMillis(5);
    private static AlarmManager alarmManager = null;
    private static PendingIntent mainPendSender = null;
    private static PendingIntent offerSender = null;
    private static PendingIntent sender = null;

    private static AlarmManager getAlarmManager(Context context) {
        if (alarmManager == null) {
            alarmManager = (AlarmManager) context.getSystemService("alarm");
        }
        return alarmManager;
    }

    public static void startPullingService(Context context) {
        plog.i("----------startPullingService----------------");
        getAlarmManager(context);
        if (mainPendSender == null) {
            plog.i("mainPendSender");
            Intent intent = new Intent(context, FlowService.class);
            intent.setAction(PspManager.PULLSERVICE_ALARM);
            mainPendSender = PendingIntent.getService(context, 0, intent, 134217728);
            alarmManager.setRepeating(2, SystemClock.elapsedRealtime(), (long) PspManager.PULL_SECONDS, mainPendSender);
        }
    }

    public static void stopPullingService() {
        plog.i("----------stopPullingService----------------");
        if (alarmManager != null && mainPendSender != null) {
            alarmManager.cancel(mainPendSender);
            mainPendSender = null;
        }
    }

    public static void startOffer(Context context) {
        plog.i("----------startOffer----------------");
        getAlarmManager(context);
        if (offerSender == null) {
            Intent intent = new Intent();
            intent.setAction(AReceiver.OFFER_ACTION);
            offerSender = PendingIntent.getBroadcast(context, 0, intent, 0);
            long firstime = SystemClock.elapsedRealtime() + 1000;
            plog.i("startofferSender");
            alarmManager.setRepeating(2, firstime, PERIOD, offerSender);
        }
    }

    public static void stopOffer() {
        plog.i("----------stopOffer----------------");
        if (alarmManager != null && offerSender != null) {
            alarmManager.cancel(offerSender);
            offerSender = null;
        }
    }

    public static void startTrackUrlAlarm(Context context) {
        getAlarmManager(context);
        if (sender == null) {
            Intent intent = new Intent();
            intent.setAction(AReceiver.ACTION);
            sender = PendingIntent.getBroadcast(context, 0, intent, 0);
            long firstime = SystemClock.elapsedRealtime() + PERIOD;
            plog.i("startTrackUrlAlarm");
            alarmManager.setRepeating(2, firstime, PERIOD, sender);
        }
    }

    public static void stopTrackUrlAlarm() {
        plog.i("stopTrackUrlAlarm");
        if (alarmManager != null && sender != null) {
            alarmManager.cancel(sender);
            sender = null;
        }
    }

    public static void setHeaderHitPeriod(int second) {
        PspManager.PULL_SECONDS = second * 1000;
    }
}
