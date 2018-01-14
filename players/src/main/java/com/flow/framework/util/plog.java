package com.flow.framework.util;

import com.orhanobut.logger.AndroidLogTool;
import com.orhanobut.logger.LogLevel;
import com.orhanobut.logger.Logger;

public class plog {
    private static final String LOGTAG = "Pull_Server";

    public static void init() {
        Logger.init(LOGTAG).methodCount(3).hideThreadInfo().logLevel(LogLevel.FULL).methodOffset(2).logTool(new AndroidLogTool());
    }

    public static void i(String paramString) {
        Logger.i(paramString, new Object[0]);
    }
}
