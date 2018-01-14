package com.function.product.cm.impl.callback;

import com.function.product.cm.bean.ProcessAppItem;
import java.util.ArrayList;

public interface IProcessView {
    void onScanCompleted(ArrayList<ProcessAppItem> arrayList, long j);

    void onScanProgressUpdated(long j, String str);

    void onScanStarted();
}
