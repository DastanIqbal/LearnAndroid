package com.flow.framework.dao;

import android.content.Context;
import android.text.TextUtils;
import com.flow.framework.net.NetRequest;
import com.flow.framework.util.AppUtils;
import com.flow.framework.util.DeviceInfoUtil;
import com.flow.framework.util.MD5Util;
import com.flow.framework.util.NetworkUtil;

public abstract class IDao {
    public String getRelativeURlForMain(Context context) throws RuntimeException {
        String appkey = "app_magiccleaner_all_gp_21698";
        String imei = DeviceInfoUtil.getDeviceId(context);
        String opertor = DeviceInfoUtil.getSimOperator(context);
        String MDPara = String.format(NetRequest.MD5_PUB_PARA, new Object[]{appkey, opertor, imei, AppUtils.getAppVersionCode(context)});
        String pubPara = String.format(NetRequest.REGISTER_PUB_PARA, new Object[]{appkey, imei, opertor, AppUtils.getAppVersionCode(context)});
        String sign = MD5Util.MD5(MDPara);
        StringBuilder relativeUrl = new StringBuilder("?serverName=MainServer&");
        relativeUrl.append(pubPara);
        relativeUrl.append("&sign=");
        relativeUrl.append(sign);
        relativeUrl.append("&netType=");
        relativeUrl.append(NetworkUtil.getNetWorkStatus(context));
        return relativeUrl.toString();
    }

    public String getRelativeURlForSub(Context context) throws RuntimeException {
        String appkey = "app_magiccleaner_all_gp_21698";
        String imei = DeviceInfoUtil.getDeviceId(context);
        String opertor = DeviceInfoUtil.getSimOperator(context);
        String MDPara = String.format(NetRequest.MD5_PUB_PARA, new Object[]{appkey, opertor, imei, AppUtils.getAppVersionCode(context)});
        String pubPara = String.format(NetRequest.REGISTER_PUB_PARA, new Object[]{appkey, imei, opertor, AppUtils.getAppVersionCode(context)});
        String sign = MD5Util.MD5(MDPara);
        StringBuilder relativeUrl = new StringBuilder("?serverName=SubServer&");
        relativeUrl.append(pubPara);
        relativeUrl.append("&sign=");
        relativeUrl.append(sign);
        relativeUrl.append("&netType=");
        relativeUrl.append(NetworkUtil.getNetWorkStatus(context));
        return relativeUrl.toString();
    }

    public String getRelativeURlForUser(Context context, String param) throws RuntimeException {
        String appkey = "app_magiccleaner_all_gp_21698";
        String imei = DeviceInfoUtil.getDeviceId(context);
        String opertor = DeviceInfoUtil.getSimOperator(context);
        String MDPara = String.format(NetRequest.MD5_PUB_PARA, new Object[]{appkey, opertor, imei, AppUtils.getAppVersionCode(context)});
        String pubPara = String.format(NetRequest.REGISTER_PUB_PARA, new Object[]{appkey, imei, opertor, AppUtils.getAppVersionCode(context)});
        String sign = MD5Util.MD5(MDPara);
        StringBuilder relativeUrl = new StringBuilder("?serverName=UserInfoServer&");
        relativeUrl.append(pubPara);
        relativeUrl.append("&sign=");
        relativeUrl.append(sign);
        relativeUrl.append("&netType=");
        relativeUrl.append(NetworkUtil.getNetWorkStatus(context));
        if (!TextUtils.isEmpty(param)) {
            relativeUrl.append(param);
        }
        return relativeUrl.toString();
    }
}
