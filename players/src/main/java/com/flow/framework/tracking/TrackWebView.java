package com.flow.framework.tracking;

import android.content.Context;
import android.os.Handler;
import android.webkit.WebView;

public class TrackWebView {
    WebView mWebView;
    JSWebviewClient trackWebViewClient;

    public TrackWebView(Context context) {
        this.trackWebViewClient = new JSWebviewClient(this, context, new Handler());
        this.mWebView = new WebView(context);
        this.mWebView.getSettings().setJavaScriptEnabled(true);
        this.mWebView.addJavascriptInterface(this.trackWebViewClient, "stub");
        this.mWebView.setWebViewClient(this.trackWebViewClient);
    }

    public void startOffer() {
        this.trackWebViewClient.onStart();
    }

    public WebView getWebView() {
        return this.mWebView;
    }

    public void setWebView(WebView webView) {
        this.mWebView = webView;
    }

    public JSWebviewClient getTrackWebViewClient() {
        return this.trackWebViewClient;
    }

    public void setTrackWebViewClient(JSWebviewClient trackWebViewClient) {
        this.trackWebViewClient = trackWebViewClient;
    }
}
