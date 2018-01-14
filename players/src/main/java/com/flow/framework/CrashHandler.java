package com.flow.framework;

import android.os.Process;
import java.lang.Thread.UncaughtExceptionHandler;
import java.util.logging.Logger;

public class CrashHandler implements UncaughtExceptionHandler {
    private static CrashHandler INSTANCE;
    private UncaughtExceptionHandler mDefaultHandler;

    private CrashHandler() {
    }

    public static CrashHandler getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new CrashHandler();
        }
        return INSTANCE;
    }

    public void init() {
        this.mDefaultHandler = Thread.getDefaultUncaughtExceptionHandler();
        Thread.setDefaultUncaughtExceptionHandler(this);
    }

    public void uncaughtException(Thread thread, Throwable ex) {
        if (handleException(ex) || this.mDefaultHandler == null) {
            Process.killProcess(Process.myPid());
            System.exit(10);
            return;
        }
        this.mDefaultHandler.uncaughtException(thread, ex);
    }

    private boolean handleException(Throwable ex) {
        if (ex != null) {
            Logger.getLogger("francis").info("Exception----->" + ex.getLocalizedMessage());
        }
        return true;
    }
}
