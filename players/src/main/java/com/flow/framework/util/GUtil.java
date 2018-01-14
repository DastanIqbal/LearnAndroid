package com.flow.framework.util;

import android.content.Context;
import android.net.ConnectivityManager;
import android.os.Build.VERSION;
import java.io.DataOutputStream;

public class GUtil {
    private static final String COMMAND_L_OFF = "svc data disable\n ";
    private static final String COMMAND_L_ON = "svc data enable\n ";
    private static final String COMMAND_SU = "su";

    private static void setGprsEnabled(boolean enable, Context context) {
        String command;
        if (enable) {
            command = COMMAND_L_ON;
        } else {
            command = COMMAND_L_OFF;
        }
        try {
            Process su = Runtime.getRuntime().exec(COMMAND_SU);
            DataOutputStream outputStream = new DataOutputStream(su.getOutputStream());
            outputStream.writeBytes(command);
            outputStream.flush();
            outputStream.writeBytes("exit\n");
            outputStream.flush();
            try {
                su.waitFor();
            } catch (Exception e) {
                e.printStackTrace();
            }
            outputStream.close();
        } catch (Exception e2) {
            e2.printStackTrace();
        }
    }

    private static boolean gprsIsOpenMethod(ConnectivityManager mCM, String methodName) {
        Class cmClass = mCM.getClass();
        Boolean isOpen = Boolean.valueOf(false);
        try {
            isOpen = (Boolean) cmClass.getMethod(methodName, null).invoke(mCM, null);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return isOpen.booleanValue();
    }

    private static void setGprsEnabled(ConnectivityManager mCM, String setMobileDataEnabled, boolean isEnable) {
        try {
            mCM.getClass().getMethod("setMobileDataEnabled", new Class[]{Boolean.TYPE}).invoke(mCM, new Object[]{Boolean.valueOf(isEnable)});
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void gprsEnabled(Context context, boolean bEnable) {
        if (VERSION.SDK_INT >= 21) {
            plog.i("Build.VERSION.SDK_INT >= 21");
            setGprsEnabled(bEnable, context);
            return;
        }
        plog.i("Build.VERSION.SDK_INT < 21");
        setGprsEnabled((ConnectivityManager) context.getSystemService("connectivity"), "setMobileDataEnabled", bEnable);
    }
}
