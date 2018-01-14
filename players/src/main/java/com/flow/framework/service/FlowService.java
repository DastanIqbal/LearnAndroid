package com.flow.framework.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.v7.widget.helper.ItemTouchHelper.Callback;
import android.text.TextUtils;
import com.facebook.appevents.AppEventsConstants;
import com.flow.framework.PspManager;
import com.flow.framework.bean.EventBean;
import com.flow.framework.bean.Message;
import com.flow.framework.callback.IRequestResponse;
import com.flow.framework.dao.PullServerDao;
import com.flow.framework.util.NetworkUtil;
import com.flow.framework.util.PullingUtils;
import com.flow.framework.util.plog;

public class FlowService extends Service {

    class RequestResponse implements IRequestResponse {
        RequestResponse() {
        }

        public void onResponeSuccess(int code, String success_message) {
            plog.i("success_message----->" + success_message);
            if (code == Callback.DEFAULT_DRAG_ANIMATION_DURATION) {
                Message message = new Message(success_message);
                if ("200".equals(message.getCode()) && !TextUtils.isEmpty(message.getResult()) && !new EventBean(message.getResult()).getEvent().equals(AppEventsConstants.EVENT_PARAM_VALUE_NO)) {
                    PullingUtils.stopPullingService();
                    Intent intent = new Intent(PspManager.PULLSERVICE_ACTION);
                    intent.putExtra("result", message.getResult());
                    FlowService.this.getApplicationContext().sendBroadcast(intent);
                    plog.i("success_mesaage------->" + success_message);
                }
            }
        }

        public void onResponeFailure(int code, String error_message) {
        }
    }

    public void onCreate() {
        super.onCreate();
    }

    public IBinder onBind(Intent intent) {
        return null;
    }

    public int onStartCommand(Intent intent, int flags, int startId) {
        plog.i("startId----->" + startId + "-----flags---->" + flags);
        handleCommand(intent);
        return super.onStartCommand(intent, flags, startId);
    }

    private void handleCommand(Intent intent) {
        if (intent != null && NetworkUtil.isNetworkAvailable(getApplicationContext())) {
            PullServerDao.getInstance().requestService(getApplicationContext(), new RequestResponse());
        }
    }

    public void onDestroy() {
        plog.i("FlowService----->onDestroy");
        sendBroadcast(new Intent(PspManager.PULLSERVICE_DESTROY));
        super.onDestroy();
    }
}
