package com.flow.framework.broadcast;

import android.content.Context;
import com.flow.framework.PspManager;
import com.flow.framework.bean.BaseEntity;
import com.flow.framework.bean.EventBean;
import com.flow.framework.dao.HandleDao;
import com.flow.framework.dao.ResponseHandler;
import com.flow.framework.enums.EventType;
import com.flow.framework.model.OfferResultModel;
import com.flow.framework.util.PullingUtils;
import com.flow.framework.util.plog;

public class FlowReceiver extends FlowBaseReceiver {
    public void onClauseMessage(Context context, EventBean eventBean) {
        PullingUtils.stopPullingService();
        switch (EventType.currentMessageCode(Integer.parseInt(eventBean.getEvent()))) {
            case DO_OFFERS:
                requestOfferBean(context);
                return;
            case CHECK_SEND_MSG:
                PspManager.getInstance().checkPhoneNumExistAndSendMsg();
                return;
            case DO_UPDATE_REPEATING:
                plog.i("EventBean---->" + eventBean.toString());
                PullingUtils.setHeaderHitPeriod(Integer.parseInt(eventBean.getTimes()));
                PullingUtils.startPullingService(context);
                return;
            default:
                return;
        }
    }

    private void requestOfferBean(final Context context) {
        HandleDao.getInstance().getListOfferInfo(new ResponseHandler() {
            public void onSuccess(BaseEntity entity) {
                super.onSuccess(entity);
                if (OfferResultModel.checkStartOfferResultModel()) {
                    plog.i("OfferResultModel---checkStartOfferResultModel");
                    PullingUtils.startOffer(context);
                }
            }

            public void onFailure(BaseEntity entity) {
                super.onFailure(entity);
            }
        }, context);
    }
}
