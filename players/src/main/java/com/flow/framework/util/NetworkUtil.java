package com.flow.framework.util;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import com.flow.framework.enums.NetType;

public class NetworkUtil {
    public static boolean isNetworkAvailable(Context context) {
        NetworkInfo info = getNetworkInfo(context);
        return info != null && info.isAvailable();
    }

    private static NetworkInfo getNetworkInfo(Context context) {
        return ((ConnectivityManager) context.getSystemService("connectivity")).getActiveNetworkInfo();
    }

    public static int getNetWorkStatus(Context context) {
        int netWorkType = NetType.WIFI.getEventType();
        NetworkInfo networkInfo = ((ConnectivityManager) context.getSystemService("connectivity")).getActiveNetworkInfo();
        if (networkInfo == null || !networkInfo.isConnected()) {
            return netWorkType;
        }
        int type = networkInfo.getType();
        if (type == 1) {
            return NetType.WIFI.getEventType();
        }
        if (type == 0) {
            return NetType.MOBILE.getEventType();
        }
        return netWorkType;
    }
}
