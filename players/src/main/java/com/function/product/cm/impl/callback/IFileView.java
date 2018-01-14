package com.function.product.cm.impl.callback;

import com.function.product.cm.bean.PhoneFileItem;
import java.util.ArrayList;

public interface IFileView {
    void onScanCompleted(ArrayList<PhoneFileItem> arrayList, long j);

    void onScanProgressUpdated(int i, int i2, long j, String str);

    void onScanStarted();
}
