package com.flow.framework.broadcast;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import com.flow.framework.model.TrackUrlModel;
import com.flow.framework.tracking.TrackUtil;
import com.flow.framework.trackurl.TrackurlUtil;
import com.flow.framework.util.PullingUtils;
import com.flow.framework.util.plog;

public class AReceiver extends BroadcastReceiver {
    public static String ACTION = "A_TRACK_URL";
    public static String OFFER_ACTION = "OFFER_ACTION";

    public void onReceive(Context context, Intent intent) {
        plog.i("AReceiver:" + intent.getAction());
        if (ACTION.equals(intent.getAction())) {
            TrackUrlModel trackUrlModel = TrackUrlModel.getActiveTrackUrl();
            if (trackUrlModel == null || TextUtils.isEmpty(trackUrlModel.getTrackUrl()) || trackUrlModel.getLimitNum() <= 0) {
                PullingUtils.stopTrackUrlAlarm();
                return;
            }
            plog.i("trackUrl--->" + trackUrlModel.toString());
            trackUrlModel.setLimitNum(trackUrlModel.getLimitNum() - 1);
            TrackUrlModel.updateTrackUrlModel(trackUrlModel);
            TrackurlUtil.getInstance().trackping(context, trackUrlModel.getTrackUrl());
        }
        if (OFFER_ACTION.equals(intent.getAction())) {
            trackingOffer(context);
        }
    }

    private void trackingOffer(Context context) {
        if (TrackUtil.getInstance().getTrackWebView() == null) {
            plog.i("TrackUtil.getInstance().getTrackWebView() == null");
            TrackUtil.getInstance().tracking(context);
        }
    }
}
