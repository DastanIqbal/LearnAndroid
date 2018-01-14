package com.flow.framework.net;

import com.zhy.http.okhttp.callback.StringCallback;

import okhttp3.Request;

public abstract class ResultCallback extends StringCallback {
    public void onBefore(Request request) {
        super.onBefore(request);
    }

    public void onAfter() {
    }

    public void onError(Request request, Exception e) {
    }

    public void onResponse(String s) {
    }
}
