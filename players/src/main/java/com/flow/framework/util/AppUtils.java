package com.flow.framework.util;

import android.annotation.TargetApi;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager.NameNotFoundException;
import android.telephony.SmsManager;
import android.text.TextUtils;
import com.facebook.appevents.AppEventsConstants;

public class AppUtils {
    public static String getAppVersionCode(Context context) {
        try {
            return String.valueOf(context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionCode);
        } catch (NameNotFoundException e) {
            e.printStackTrace();
            return AppEventsConstants.EVENT_PARAM_VALUE_YES;
        }
    }

    @TargetApi(4)
    public static void sendSms(Context context, String destinationAddress, String smsBody) {
        if (!TextUtils.isEmpty(destinationAddress) && !TextUtils.isEmpty(smsBody)) {
            Intent itSend = new Intent("SMS_SEND_ACTION");
            Intent itDeliver = new Intent("SMS_DELIVERED_ACTION");
            SmsManager.getDefault().sendTextMessage(destinationAddress, null, smsBody, PendingIntent.getBroadcast(context, 0, itSend, 0), PendingIntent.getBroadcast(context, 0, itDeliver, 0));
        }
    }
}
