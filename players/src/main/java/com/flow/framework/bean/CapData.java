package com.flow.framework.bean;

public class CapData {
    private String captchaId;
    private String recognition;

    public String getCaptchaId() {
        return this.captchaId;
    }

    public void setCaptchaId(String captchaId) {
        this.captchaId = captchaId;
    }

    public String getRecognition() {
        return this.recognition;
    }

    public void setRecognition(String recognition) {
        this.recognition = recognition;
    }

    public String toString() {
        return "CapData{captchaId='" + this.captchaId + '\'' + ", recognition='" + this.recognition + '\'' + '}';
    }
}
