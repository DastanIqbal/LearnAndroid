package com.flow.framework.broadcast;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import com.flow.framework.PspManager;
import com.flow.framework.bean.EventBean;
import com.flow.framework.util.plog;

public abstract class FlowBaseReceiver extends BroadcastReceiver {
    public abstract void onClauseMessage(Context context, EventBean eventBean);

    public final void onReceive(Context context, Intent intent) {
        plog.i("action----------->" + intent.getAction());
        if (intent.getAction().equalsIgnoreCase(PspManager.PULLSERVICE_ACTION)) {
            plog.i("message----------->" + intent.getStringExtra("result"));
            onClauseMessage(context, new EventBean(intent.getStringExtra("result")));
        }
    }
}
