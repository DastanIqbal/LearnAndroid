package com.flow.framework.bean;

import org.json.JSONException;
import org.json.JSONObject;

public class OfferResult extends BaseEntity<OfferResult> {
    private String offerId;
    private String offerResult;

    public OfferResult(String result_str) {
        parseJson(result_str);
    }

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

    public OfferResult parseJson(String result_str) {
        try {
            JSONObject jsonObject = new JSONObject(result_str);
            setOfferId(jsonObject.optString("offerId", null));
            setOfferResult(jsonObject.optString("list", null));
            return this;
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }
}
