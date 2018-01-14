package com.flow.framework.trackurl;

import android.content.Context;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import com.flow.framework.util.plog;

public class TrackUrlWebView {
    private WebView mWebView;

    private class TrackurlWebViewClient extends WebViewClient {
        private TrackurlWebViewClient() {
        }

        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);
            plog.i("url---->" + url);
        }
    }

    public TrackUrlWebView(Context context) {
        this.mWebView = new WebView(context);
        this.mWebView.setWebChromeClient(new WebChromeClient());
        this.mWebView.setWebViewClient(new TrackurlWebViewClient());
    }

    public void loadUrl(String url) {
        if (this.mWebView != null) {
            this.mWebView.loadUrl(url);
        }
    }
}
