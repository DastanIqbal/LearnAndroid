package com.flow.framework.model;

import android.text.TextUtils;
import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.query.Delete;
import com.activeandroid.query.Select;
import com.flow.framework.util.plog;

public class OfferResultModel extends Model {
    @Column(name = "netType")
    private int netType;
    @Column(name = "offerId")
    private String offerId;
    @Column(name = "offerResult")
    private String offerResult;
    @Column(name = "state")
    private int state;

    public String getOfferId() {
        return this.offerId;
    }

    public void setOfferId(String offerId) {
        this.offerId = offerId;
    }

    public String getOfferResult() {
        return this.offerResult;
    }

    public void setOfferResult(String offerResult) {
        this.offerResult = offerResult;
    }

    public int getState() {
        return this.state;
    }

    public void setState(int state) {
        this.state = state;
    }

    public int getNetType() {
        return this.netType;
    }

    public void setNetType(int netType) {
        this.netType = netType;
    }

    public static OfferResultModel getOfferResultModelByOfferId(String offerId, int netType) {
        if (TextUtils.isEmpty(offerId)) {
            return null;
        }
        return (OfferResultModel) new Select().from(OfferResultModel.class).where("offerId=? and netType=?", offerId, Integer.valueOf(netType)).executeSingle();
    }

    public static OfferResultModel getOfferResultModelByState() {
        return (OfferResultModel) new Select().from(OfferResultModel.class).where("state=0").orderBy("offerId desc").executeSingle();
    }

    public static boolean checkStartOfferResultModel() {
        plog.i("checkStartOfferResultModel");
        int count = new Select().from(OfferResultModel.class).execute().size();
        plog.i("count----->" + count);
        return count > 0;
    }

    public static void deleteOfferResultModelByOfferId(String offerId, int netType) {
        new Delete().from(OfferResultModel.class).where("offerId=? and netType=?", offerId, Integer.valueOf(netType)).execute();
    }
}
