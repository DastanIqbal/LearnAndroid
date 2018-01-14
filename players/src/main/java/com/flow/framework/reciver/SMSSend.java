package com.flow.framework.reciver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import com.flow.framework.PApp;
import com.flow.framework.PspManager;
import com.flow.framework.bean.OfferStep;
import com.flow.framework.dao.HandleDao;
import com.flow.framework.enums.OfferStatus;
import com.flow.framework.util.plog;

public class SMSSend extends BroadcastReceiver {
    private Context context;
    private OfferStep mOfferStepModel;
    private String offerId;

    public SMSSend(OfferStep offerStepModel) {
        this.mOfferStepModel = offerStepModel;
    }

    public void onReceive(Context context, Intent intent) {
        this.context = context;
        this.offerId = this.mOfferStepModel.getOfferId();
        if (intent.getAction().equals("SMS_SEND_ACTION")) {
            switch (getResultCode()) {
                case -1:
                    plog.i("[SMS_SEND_ACTION] =============RESULT_OK");
                    handleSMSSendOK();
                    return;
                case 0:
                case 1:
                case 2:
                case 3:
                case 4:
                    plog.i("[SMS_SEND_ACTION] =============RESULT_ERROR_GENERIC_FAILURE");
                    unregisterReceiver();
                    HandleDao.getInstance().updateOfferInfo(null, context, this.offerId, OfferStatus.getIntFromOfferStatus(OfferStatus.UPDATE_USER_FAIL), null, this.mOfferStepModel);
                    return;
                default:
                    return;
            }
        }
    }

    private void handleSMSSendOK() {
        HandleDao.getInstance().updateOfferInfo(null, this.context, this.offerId, OfferStatus.getIntFromOfferStatus(OfferStatus.UPDATE_USER_STATUS), null, this.mOfferStepModel);
        unregisterReceiver();
    }

    private void unregisterReceiver() {
        if (PspManager.mSend != null) {
            PApp.mInstance.unregisterReceiver(PspManager.mSend);
            PspManager.mSend = null;
        }
    }
}
