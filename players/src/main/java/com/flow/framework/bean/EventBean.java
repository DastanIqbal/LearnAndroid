package com.flow.framework.bean;

import com.facebook.appevents.AppEventsConstants;
import com.facebook.internal.ServerProtocol;
import com.flow.framework.util.plog;
import org.json.JSONException;
import org.json.JSONObject;

public class EventBean extends BaseEntity<EventBean> {
    private String event;
    private String times;
    private String version;

    public EventBean(){}
    public EventBean(String result_str) {
        parseJson(result_str);
    }

    public String getEvent() {
        return this.event;
    }

    public void setEvent(String event) {
        this.event = event;
    }

    public String getVersion() {
        return this.version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getTimes() {
        return this.times;
    }

    public void setTimes(String times) {
        this.times = times;
    }

    public EventBean parseJson(String result_str) {
        plog.i("result_str--->" + result_str);
        try {
            JSONObject jsonObject = new JSONObject(result_str);
            setEvent(jsonObject.optString("event", AppEventsConstants.EVENT_PARAM_VALUE_NO));
            setVersion(jsonObject.optString(ServerProtocol.FALLBACK_DIALOG_PARAM_VERSION, AppEventsConstants.EVENT_PARAM_VALUE_NO));
            setTimes(jsonObject.optString("times", AppEventsConstants.EVENT_PARAM_VALUE_NO));
            return this;
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }

    public String toString() {
        return "EventBean{event='" + this.event + '\'' + ", version='" + this.version + '\'' + ", times='" + this.times + '\'' + '}';
    }
}
