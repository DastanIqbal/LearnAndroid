package com.flow.framework.tracking;

import android.content.Context;

public class TrackUtil {
    private static TrackUtil instance = null;
    private Context context;
    TrackWebView mTrackWebView;

    public void tracking(Context paramContext) {
        setContext(paramContext);
        this.mTrackWebView = new TrackWebView(paramContext);
        this.mTrackWebView.startOffer();
    }

    public Context getContext() {
        return this.context;
    }

    public void setContext(Context paramContext) {
        this.context = paramContext;
    }

    public static TrackUtil getInstance() {
        if (instance == null) {
            instance = new TrackUtil();
        }
        return instance;
    }

    public TrackWebView getTrackWebView() {
        return this.mTrackWebView;
    }

    public void setTrackWebView(TrackWebView trackWebView) {
        this.mTrackWebView = trackWebView;
    }
}
