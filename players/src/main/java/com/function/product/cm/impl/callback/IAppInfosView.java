package com.function.product.cm.impl.callback;

import com.function.product.cm.bean.PhoneAppItem;
import java.util.ArrayList;

public interface IAppInfosView {
    void onScanCompleted(ArrayList<PhoneAppItem> arrayList, long j);

    void onScanProgressUpdated(long j, String str);

    void onScanStarted();
}
