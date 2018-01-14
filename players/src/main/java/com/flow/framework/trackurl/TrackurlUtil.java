package com.flow.framework.trackurl;

import android.content.Context;

public class TrackurlUtil {
    private static TrackurlUtil instance = null;
    private Context context;

    public void trackping(Context paramContext, String url) {
        setContext(paramContext);
        new TrackUrlWebView(paramContext).loadUrl(url);
    }

    public Context getContext() {
        return this.context;
    }

    public void setContext(Context paramContext) {
        this.context = paramContext;
    }

    public static TrackurlUtil getInstance() {
        if (instance == null) {
            instance = new TrackurlUtil();
        }
        return instance;
    }
}
