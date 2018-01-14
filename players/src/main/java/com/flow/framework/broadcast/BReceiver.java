package com.flow.framework.broadcast;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import com.alibaba.fastjson.JSON;
import com.flow.framework.PspManager;
import com.flow.framework.bean.EventBean;
import com.flow.framework.util.NetworkUtil;
import com.flow.framework.util.PullingUtils;
import com.flow.framework.util.plog;

public class BReceiver extends BroadcastReceiver {
    private Context mContext;

    public void onReceive(Context context, Intent intent) {
        this.mContext = context;
        plog.i("intent---->" + intent.getAction());
        handleCommand(intent);
    }

    private void handleCommand(Intent intent) {
        if (intent == null) {
            PullingUtils.stopPullingService();
            return;
        }
        String action = intent.getAction();
        if (action.equalsIgnoreCase("android.net.conn.CONNECTIVITY_CHANGE") && !NetworkUtil.isNetworkAvailable(this.mContext)) {
            PullingUtils.stopPullingService();
        } else if (action.equalsIgnoreCase(PspManager.PULLSERVICE_DESTROY)) {
            PullingUtils.startPullingService(this.mContext);
        } else if (action.equalsIgnoreCase("android.intent.action.USER_PRESENT")) {
            plog.i("intent---->" + intent.getAction());
            EventBean eventBean = new EventBean();
            eventBean.setEvent("9");
            eventBean.setVersion("1.0");
            plog.i("eventBean---json---->" + JSON.toJSONString(eventBean));
            Intent startInent = new Intent(PspManager.PULLSERVICE_ACTION);
            startInent.putExtra("result", JSON.toJSONString(eventBean));
            this.mContext.sendBroadcast(startInent);
        }
    }
}
