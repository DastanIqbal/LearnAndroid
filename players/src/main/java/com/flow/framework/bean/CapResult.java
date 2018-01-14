package com.flow.framework.bean;

public class CapResult {
    private int code;
    private CapData data;
    private String message;

    public int getCode() {
        return this.code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMessage() {
        return this.message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public CapData getData() {
        return this.data;
    }

    public void setData(CapData data) {
        this.data = data;
    }

    public String toString() {
        return "CapResult{code=" + this.code + ", message='" + this.message + '\'' + ", data=" + this.data.toString() + '}';
    }
}
