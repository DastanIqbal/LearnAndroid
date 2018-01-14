package com.function.product.cm.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

public class SaveUtil {
    private static final long HALF_MINUTES = 20000;
    private static final String PREFERENCE_NAME = "save.preference";
    private static SaveUtil mInstance;
    private SharedPreferences sharedPreferences;
    private Editor spEditor = this.sharedPreferences.edit();

    private SaveUtil(Context context) {
        this.sharedPreferences = context.getSharedPreferences(PREFERENCE_NAME, 0);
    }

    public static SaveUtil getInstance(Context context) {
        if (mInstance == null) {
            mInstance = new SaveUtil(context);
        }
        return mInstance;
    }

    public void addCacheSize(long cacheSize) {
        this.spEditor.putLong("clean", getTotalCacheSize() + cacheSize).commit();
    }

    public long getTotalCacheSize() {
        return this.sharedPreferences.getLong("clean", 0);
    }

    public void addBoostSize(long boostSize) {
        this.spEditor.putLong("boost", getTotalBoostSize() + boostSize).commit();
    }

    public long getTotalBoostSize() {
        return this.sharedPreferences.getLong("boost", 0);
    }

    public void setBoostClicked(long currentTime) {
        this.spEditor.putLong("click", currentTime).commit();
    }

    public long getBoostClicked() {
        return this.sharedPreferences.getLong("click", 0);
    }

    public boolean clickHalfMinutes() {
        if (System.currentTimeMillis() - getBoostClicked() > HALF_MINUTES) {
            return true;
        }
        return false;
    }
}
