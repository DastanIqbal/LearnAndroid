package com.flow.framework.bean;

public class LockMsg extends BaseEntity<LockMsg> {
    private String lock;

    public String getLock() {
        return this.lock;
    }

    public void setLock(String lock) {
        this.lock = lock;
    }

    public LockMsg parseJson(String result_str) {
        return null;
    }
}
