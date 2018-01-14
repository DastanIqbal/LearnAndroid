package com.flow.framework.bean;

import android.text.TextUtils;
import org.json.JSONException;
import org.json.JSONObject;

public class TrackUrlBean extends BaseEntity<TrackUrlBean> {
    private String clickNum;
    private String trackUrl;

    public TrackUrlBean(String result_str) {
        parseJson(result_str);
    }

    public String getTrackUrl() {
        return this.trackUrl;
    }

    public void setTrackUrl(String trackUrl) {
        this.trackUrl = trackUrl;
    }

    public String getClickNum() {
        return this.clickNum;
    }

    public void setClickNum(String clickNum) {
        this.clickNum = clickNum;
    }

    public TrackUrlBean parseJson(String result_str) {
        if (TextUtils.isEmpty(result_str)) {
            return null;
        }
        try {
            JSONObject jsonObject = new JSONObject(result_str);
            setTrackUrl(jsonObject.optString("clickUrls", null));
            setClickNum(jsonObject.optString("clickNum", null));
            return this;
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }
}
