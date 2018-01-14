package com.flow.framework.bean;

public class OfferStep {
    private String actionUrl;
    private String endUrl;
    private String keyWord;
    private int netType;
    private int number;
    private String offerId;
    private String params;
    private String parseKeys;
    private String shortCode;
    private int step;
    private String trankUrl;

    public int getNumber() {
        return this.number;
    }

    public void setNumber(int number) {
        this.number = number;
    }

    public String getOfferId() {
        return this.offerId;
    }

    public void setOfferId(String offerId) {
        this.offerId = offerId;
    }

    public int getStep() {
        return this.step;
    }

    public void setStep(int step) {
        this.step = step;
    }

    public String getTrankUrl() {
        return this.trankUrl;
    }

    public void setTrankUrl(String trankUrl) {
        this.trankUrl = trankUrl;
    }

    public String getEndUrl() {
        return this.endUrl;
    }

    public void setEndUrl(String endUrl) {
        this.endUrl = endUrl;
    }

    public String getParseKeys() {
        return this.parseKeys;
    }

    public void setParseKeys(String parseKeys) {
        this.parseKeys = parseKeys;
    }

    public String getParams() {
        return this.params;
    }

    public void setParams(String params) {
        this.params = params;
    }

    public String getActionUrl() {
        return this.actionUrl;
    }

    public void setActionUrl(String actionUrl) {
        this.actionUrl = actionUrl;
    }

    public String getShortCode() {
        return this.shortCode;
    }

    public void setShortCode(String shortCode) {
        this.shortCode = shortCode;
    }

    public String getKeyWord() {
        return this.keyWord;
    }

    public void setKeyWord(String keyWord) {
        this.keyWord = keyWord;
    }

    public int getNetType() {
        return this.netType;
    }

    public void setNetType(int netType) {
        this.netType = netType;
    }

    public String toString() {
        return "OfferStep{number=" + this.number + ", offerId='" + this.offerId + '\'' + ", step=" + this.step + ", netType=" + this.netType + ", trankUrl='" + this.trankUrl + '\'' + ", endUrl='" + this.endUrl + '\'' + ", parseKeys='" + this.parseKeys + '\'' + ", params='" + this.params + '\'' + ", actionUrl='" + this.actionUrl + '\'' + ", shortCode='" + this.shortCode + '\'' + ", keyWord='" + this.keyWord + '\'' + '}';
    }
}
