package com.function.product.cm.impl.callback;

public interface ICacheSizeView {
    void onCleanCompleted(long j);

    void onCleanProgressUpdate(int i);

    void onCleanStarted();
}
