package com.function.product.cm.util;

import android.app.ActivityManager;
import android.app.ActivityManager.MemoryInfo;
import android.content.Context;
import android.os.Environment;
import android.os.StatFs;
import android.support.v4.media.session.PlaybackStateCompat;
import android.text.format.Formatter;
import java.io.BufferedReader;
import java.io.FileReader;

public class MemoryUtil {
    private static long getAvailableRAM(Context context) {
        ActivityManager activityManager = (ActivityManager) context.getSystemService("activity");
        MemoryInfo mi = new MemoryInfo();
        activityManager.getMemoryInfo(mi);
        return mi.availMem;
    }

    private static long getTotalRAM() {
        String firstLine = null;
        try {
            BufferedReader br = new BufferedReader(new FileReader("/proc/meminfo"), 8192);
            firstLine = br.readLine().split("\\s+")[1];
            br.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Long.valueOf(firstLine).longValue() * PlaybackStateCompat.ACTION_PLAY_FROM_MEDIA_ID;
    }

    public static String getUsedAndTotalRAM(Context context) {
        long availableRam = getAvailableRAM(context);
        long totalRam = getTotalRAM();
        return Formatter.formatFileSize(context, totalRam - availableRam) + " / " + Formatter.formatFileSize(context, totalRam);
    }

    public static String getRAMPercent(Context context) {
        long availableRam = getAvailableRAM(context);
        long totalRam = getTotalRAM();
        return ((int) ((((float) (totalRam - availableRam)) / ((float) totalRam)) * 100.0f)) + "%";
    }

    public static String getUsedAndTotalStorage(Context context) {
        StatFs statFs = new StatFs(Environment.getDataDirectory().getAbsolutePath());
        long blockCount = statFs.getBlockCountLong();
        long availableCount = statFs.getAvailableBlocksLong();
        long size = statFs.getBlockSizeLong();
        long totalROMSize = blockCount * size;
        return Formatter.formatFileSize(context, totalROMSize - (availableCount * size)) + " / " + Formatter.formatFileSize(context, totalROMSize);
    }

    public static String getStoragePercent() {
        StatFs statFs = new StatFs(Environment.getDataDirectory().getPath());
        long blockCount = statFs.getBlockCountLong();
        long auailableCount = statFs.getAvailableBlocksLong();
        long size = statFs.getBlockSizeLong();
        long totalROMSize = blockCount * size;
        return ((int) ((((float) (totalROMSize - (auailableCount * size))) / ((float) totalROMSize)) * 100.0f)) + "%";
    }

    public static int getAvailableSpace() {
        StatFs statFs = new StatFs(Environment.getDataDirectory().getPath());
        long blockCount = statFs.getBlockCountLong();
        long auailableCount = statFs.getAvailableBlocksLong();
        long size = statFs.getBlockSizeLong();
        return (int) ((((float) (auailableCount * size)) / ((float) (blockCount * size))) * 100.0f);
    }
}
