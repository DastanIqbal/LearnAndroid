package com.flow.framework.bean;

import com.facebook.share.internal.ShareConstants;
import org.json.JSONException;
import org.json.JSONObject;

public class Message extends BaseEntity<Message> {
    private String code;
    private String message;
    private String result;

    public Message(){}
    public Message(String result_str) {
        parseJson(result_str);
    }

    public String getCode() {
        return this.code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getMessage() {
        return this.message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getResult() {
        return this.result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public Message parseJson(String result_str) {
        try {
            JSONObject jsonObject = new JSONObject(result_str);
            setCode(jsonObject.optString("code", "300"));
            setMessage(jsonObject.optString(ShareConstants.WEB_DIALOG_PARAM_MESSAGE, "bad request"));
            setResult(jsonObject.optString("result", "null"));
            return this;
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }
}
