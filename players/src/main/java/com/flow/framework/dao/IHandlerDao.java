package com.flow.framework.dao;

import android.content.Context;
import com.flow.framework.bean.OfferStep;

public abstract class IHandlerDao extends IDao {
    public abstract void addAppUserInfo(ResponseHandler responseHandler, Context context);

    public abstract void checkExistPhoneAndSend(ResponseHandler responseHandler, Context context);

    public abstract void checkExistPhoneAndSendAndLock(ResponseHandler responseHandler, Context context);

    public abstract void getListOfferInfo(ResponseHandler responseHandler, Context context);

    public abstract void getOfferTrackUrl(Context context);

    public abstract void updateOfferInfo(ResponseHandler responseHandler, Context context, String str, int i, String str2, OfferStep offerStep);
}
