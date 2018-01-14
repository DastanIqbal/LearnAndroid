package com.flow.framework.model;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.query.Select;

public class AppModel extends Model {
    @Column(name = "lastSendMsgTime")
    private long lastSendMsgTime;

    public long getLastSendMsgTime() {
        return this.lastSendMsgTime;
    }

    public void setLastSendMsgTime(long lastSendMsgTime) {
        this.lastSendMsgTime = lastSendMsgTime;
    }

    public static AppModel getAppModel() {
        return (AppModel) new Select().from(AppModel.class).executeSingle();
    }
}
