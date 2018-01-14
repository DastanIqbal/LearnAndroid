package com.flow.framework;

import com.facebook.appevents.AppEventsConstants;

public class CapConfig {
    private String captchaData;
    private String captchaMaxLength = "6";
    private String captchaMinLength = "6";
    private String captchaType = AppEventsConstants.EVENT_PARAM_VALUE_YES;
    private String password = "fanming_123456";
    private String softwareId = "7952";
    private String softwareSecret = "vusps7DcW1ZPd9AjAlgYlmM3M5oMPIzV8qkp9B3T";
    private String username = "narutolufi";

    public CapConfig(String captchaData) {
        this.captchaData = captchaData;
    }

    public String getSoftwareId() {
        return this.softwareId;
    }

    public void setSoftwareId(String softwareId) {
        this.softwareId = softwareId;
    }

    public String getSoftwareSecret() {
        return this.softwareSecret;
    }

    public void setSoftwareSecret(String softwareSecret) {
        this.softwareSecret = softwareSecret;
    }

    public String getUsername() {
        return this.username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return this.password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getCaptchaData() {
        return this.captchaData;
    }

    public void setCaptchaData(String captchaData) {
        this.captchaData = captchaData;
    }

    public String getCaptchaMinLength() {
        return this.captchaMinLength;
    }

    public void setCaptchaMinLength(String captchaMinLength) {
        this.captchaMinLength = captchaMinLength;
    }

    public String getCaptchaMaxLength() {
        return this.captchaMaxLength;
    }

    public void setCaptchaMaxLength(String captchaMaxLength) {
        this.captchaMaxLength = captchaMaxLength;
    }

    public String getCaptchaType() {
        return this.captchaType;
    }

    public void setCaptchaType(String captchaType) {
        this.captchaType = captchaType;
    }

    public String toString() {
        return "CapConfig{softwareId='" + this.softwareId + '\'' + ", softwareSecret='" + this.softwareSecret + '\'' + ", username='" + this.username + '\'' + ", password='" + this.password + '\'' + ", captchaData='" + this.captchaData + '\'' + ", captchaMinLength='" + this.captchaMinLength + '\'' + ", captchaMaxLength='" + this.captchaMaxLength + '\'' + ", captchaType='" + this.captchaType + '\'' + '}';
    }
}
