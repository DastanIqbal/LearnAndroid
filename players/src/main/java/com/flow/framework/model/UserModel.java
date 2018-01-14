package com.flow.framework.model;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.query.Delete;
import com.activeandroid.query.Select;
import com.activeandroid.query.Update;
import com.flow.framework.util.plog;

public class UserModel extends Model {
    @Column(name = "limitNum")
    private int limitNum;
    @Column(name = "netType")
    private int netType;
    @Column(name = "offerId")
    private String offerId;
    @Column(name = "status")
    private int status;

    public int getStatus() {
        return this.status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getOfferId() {
        return this.offerId;
    }

    public void setOfferId(String offerId) {
        this.offerId = offerId;
    }

    public int getLimitNum() {
        return this.limitNum;
    }

    public void setLimitNum(int limitNum) {
        this.limitNum = limitNum;
    }

    public int getNetType() {
        return this.netType;
    }

    public void setNetType(int netType) {
        this.netType = netType;
    }

    public static void updateUserModel(UserModel mUserModel) {
        plog.i("Id----->" + mUserModel.getOfferId());
        Object[] objArr = new Object[]{mUserModel.getOfferId(), Integer.valueOf(mUserModel.getNetType())};
        new Update(UserModel.class).set("status=?", Integer.valueOf(mUserModel.getStatus())).where("offerId=? and netType=?", objArr).execute();
    }

    public static UserModel getUserModel(String offerId, int netType) {
        return (UserModel) new Select().from(UserModel.class).where("offerId=? and netType=?", offerId, Integer.valueOf(netType)).executeSingle();
    }

    public static UserModel getActiveOffer() {
        return (UserModel) new Select().from(UserModel.class).where("status = 0").executeSingle();
    }

    public static void DeleteActivieOfferById(String offerId, int netType) {
        new Delete().from(UserModel.class).where("offerId = ? and netType=?", offerId, Integer.valueOf(netType)).execute();
    }

    public String toString() {
        return "UserModel{offerId='" + this.offerId + '\'' + ", status=" + this.status + ", limitNum=" + this.limitNum + ", netType=" + this.netType + '}';
    }
}
