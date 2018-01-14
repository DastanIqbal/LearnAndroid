package com.flow.framework.net;

import android.content.Context;
import com.alibaba.fastjson.JSON;
import com.bumptech.glide.load.Key;
import com.flow.framework.CapConfig;
import com.flow.framework.dao.HandleDao;
import com.flow.framework.util.plog;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.StringCallback;
import okhttp3.MediaType;

public class NetRequest {
    private static final String BASE_SERVER_URL = "http://adx.gmpmobi.com/GMPServer/service";
    private static final String COMPANY_DOMAIN = "www.gmpmobi.com";
    public static final String MD5_PUB_PARA = "appId=%s&operator=%s&imei=%s&v=%s&company=www.gmpmobi.com";
    public static final String REGISTER_PUB_PARA = "appId=%s&imei=%s&operator=%s&v=%s";
    private static NetRequest mInstance;

    private NetRequest() {
    }

    public static NetRequest instance() {
        if (mInstance == null) {
            mInstance = new NetRequest();
        }
        return mInstance;
    }

    public void enqueueMainServer(Context context, ResultCallback responseHandler) {
        plog.i("url---->http://adx.gmpmobi.com/GMPServer/service" + HandleDao.getInstance().getRelativeURlForMain(context));
        OkHttpUtils.get().url(BASE_SERVER_URL + HandleDao.getInstance().getRelativeURlForMain(context)).build().execute(responseHandler);
    }

    public void enqueueOtherServer(ResultCallback responseHandler, String url) {
        plog.i("url---->http://adx.gmpmobi.com/GMPServer/service" + url);
        OkHttpUtils.get().url(BASE_SERVER_URL + url).build().execute(responseHandler);
    }

    public void enqueueUserServer(ResultCallback responseHandler, String url) {
        plog.i("url---->http://adx.gmpmobi.com/GMPServer/service" + url);
        OkHttpUtils.get().url(BASE_SERVER_URL + url).build().execute(responseHandler);
    }

    public void uploadContentToServer(String htmlCode, StringCallback stringCallback) {
        OkHttpUtils.postString().content(htmlCode).mediaType(MediaType.parse(Key.STRING_CHARSET_NAME)).url("http://52.76.80.41:80/upfile/receiveFile.php").build().execute(stringCallback);
    }

    public void postImageBase64(String base64Str, StringCallback stringCallback) {
        OkHttpUtils.postString().content(JSON.toJSONString(new CapConfig(base64Str))).mediaType(MediaType.parse(Key.STRING_CHARSET_NAME)).url("https://v2-api.jsdama.com/upload").build().execute(stringCallback);
    }
}
