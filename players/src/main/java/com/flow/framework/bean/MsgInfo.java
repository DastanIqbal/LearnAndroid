package com.flow.framework.bean;

public class MsgInfo extends BaseEntity<MsgInfo> {
    private String phoneNo;
    private String status;

    public String getStatus() {
        return this.status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getPhoneNo() {
        return this.phoneNo;
    }

    public void setPhoneNo(String phoneNo) {
        this.phoneNo = phoneNo;
    }

    public MsgInfo parseJson(String result_str) {
        return null;
    }
}
