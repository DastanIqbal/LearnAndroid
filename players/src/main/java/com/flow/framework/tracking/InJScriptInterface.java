package com.flow.framework.tracking;

import android.webkit.JavascriptInterface;

public interface InJScriptInterface {
    @JavascriptInterface
    void getHtmlCode(String str);

    @JavascriptInterface
    void getImageBase64(String str);
}
