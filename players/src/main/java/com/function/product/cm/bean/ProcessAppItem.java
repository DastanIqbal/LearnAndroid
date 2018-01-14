package com.function.product.cm.bean;

import android.graphics.drawable.Drawable;
import java.io.Serializable;

public class ProcessAppItem implements Serializable {
    private Drawable appIcon;
    private String appName;
    private long cacheSize;
    private boolean isCheck;
    private boolean isSystem;
    private String packageName;

    public String getAppName() {
        return this.appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public String getPackageName() {
        return this.packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public long getCacheSize() {
        return this.cacheSize;
    }

    public void setCacheSize(long cacheSize) {
        this.cacheSize = cacheSize;
    }

    public Drawable getAppIcon() {
        return this.appIcon;
    }

    public void setAppIcon(Drawable appIcon) {
        this.appIcon = appIcon;
    }

    public boolean isCheck() {
        return this.isCheck;
    }

    public void setCheck(boolean check) {
        this.isCheck = check;
    }

    public boolean isSystem() {
        return this.isSystem;
    }

    public void setSystem(boolean system) {
        this.isSystem = system;
    }
}
