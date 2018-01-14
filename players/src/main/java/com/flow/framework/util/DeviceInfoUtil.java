package com.flow.framework.util;

import android.content.Context;
import android.os.Build;
import android.os.Build.VERSION;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import com.bumptech.glide.load.Key;
import com.facebook.appevents.AppEventsConstants;
import com.facebook.places.model.PlaceFields;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map.Entry;

public class DeviceInfoUtil {
    public static String RELEASE_VERSION = VERSION.RELEASE;

    public static String getDeviceId(Context context) {
        return ((TelephonyManager) context.getSystemService(PlaceFields.PHONE)).getDeviceId();
    }

    public static String getImsi(Context context) {
        String result = ((TelephonyManager) context.getSystemService(PlaceFields.PHONE)).getSubscriberId();
        if (TextUtils.isEmpty(result)) {
            return AppEventsConstants.EVENT_PARAM_VALUE_NO;
        }
        return result;
    }

    public static String getSimCountryIso(Context context) {
        return ((TelephonyManager) context.getSystemService(PlaceFields.PHONE)).getSimCountryIso();
    }

    public static String getPhoneName() {
        try {
            return URLEncoder.encode(Build.MODEL, Key.STRING_CHARSET_NAME);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return "";
        }
    }

    public static String getCpuName()  {
        Exception e;
        Throwable th;
        FileReader fr = null;
        BufferedReader br = null;
        String cpuName = null;
        try {
            FileReader fr2 = new FileReader("/proc/cpuinfo");
            try {
                BufferedReader br2 = new BufferedReader(fr2);
                try {
                    String[] array = br2.readLine().split(":\\s+", 2);
                    for (int i = 0; i < array.length; i++) {
                    }
                    cpuName = array[1];
                    if (fr2 != null) {
                        try {
                            fr2.close();
                        } catch (IOException e2) {
                            e2.printStackTrace();
                        }
                    }
                    if (br2 != null) {
                        try {
                            br2.close();
                            br = br2;
                            fr = fr2;
                        } catch (IOException e22) {
                            e22.printStackTrace();
                            br = br2;
                            fr = fr2;
                        }
                    } else {
                        fr = fr2;
                    }
                } catch (Exception e3) {
                    e = e3;
                    br = br2;
                    fr = fr2;
                    try {
                        e.printStackTrace();
                        if (fr != null) {
                            try {
                                fr.close();
                            } catch (IOException e222) {
                                e222.printStackTrace();
                            }
                        }
                        if (br != null) {
                            try {
                                br.close();
                            } catch (IOException e2222) {
                                e2222.printStackTrace();
                            }
                        }
                        return URLEncoder.encode(cpuName, Key.STRING_CHARSET_NAME);
                    } catch (Throwable th2) {
                        th = th2;
                        if (fr != null) {
                            try {
                                fr.close();
                            } catch (IOException e22222) {
                                e22222.printStackTrace();
                            }
                        }
                        if (br != null) {
                            try {
                                br.close();
                            } catch (IOException e222222) {
                                e222222.printStackTrace();
                            }
                        }
                        throw th;
                    }
                } catch (Throwable th3) {
                    th = th3;
                    br = br2;
                    fr = fr2;
                    if (fr != null) {
                        fr.close();
                    }
                    if (br != null) {
                        br.close();
                    }
                    throw th;
                }
            } catch (Exception e4) {
                e = e4;
                fr = fr2;
                e.printStackTrace();
                if (fr != null) {
                    fr.close();
                }
                if (br != null) {
                    br.close();
                }
                return URLEncoder.encode(cpuName, Key.STRING_CHARSET_NAME);
            } catch (Throwable th4) {
                th = th4;
                fr = fr2;
                if (fr != null) {
                    fr.close();
                }
                if (br != null) {
                    br.close();
                }
                //throw th;
            }
        } catch (Exception e5) {
            e = e5;
            e.printStackTrace();
            if (fr != null) {
                try {
                    fr.close();
                    if (br != null) {
                        br.close();
                    }
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
            try {
                return URLEncoder.encode(cpuName, Key.STRING_CHARSET_NAME);
            } catch (UnsupportedEncodingException e1) {
                e1.printStackTrace();
            }
        }
        try {
            return URLEncoder.encode(cpuName, Key.STRING_CHARSET_NAME);
        } catch (UnsupportedEncodingException e6) {
            e6.printStackTrace();
            return "";
        }
    }

    public static String getSimOperator(Context context) {
        return ((TelephonyManager) context.getSystemService(PlaceFields.PHONE)).getSimOperator();
    }

    public static int getNetworkType(Context context) {
        return ((TelephonyManager) context.getSystemService(PlaceFields.PHONE)).getNetworkType();
    }

    public static HashMap<String, String> getUserInfo(Context context) {
        HashMap<String, String> map = new HashMap();
        map.put("deviceId", getDeviceId(context));
        map.put("imsi", getImsi(context));
        map.put("areaCode", getSimCountryIso(context));
        map.put("phoneNetwork", String.valueOf(getNetworkType(context)));
        map.put("phoneOS", RELEASE_VERSION);
        map.put("phoneType", getPhoneName());
        map.put("telecomOperator", getSimOperator(context));
        map.put("phoneCPU", getCpuName());
        return map;
    }

    public static String makeUserInfoToString(Context context, String offerId, String htmlCode) {
        HashMap<String, String> map = new HashMap();
        map.put("html", htmlCode);
        map.put("deviceId", getDeviceId(context));
        map.put("imsi", getImsi(context));
        map.put("telecomOperator", getSimOperator(context));
        map.put("netType", NetworkUtil.getNetWorkStatus(context) + "");
        map.put("offerId", offerId);
        StringBuilder sb = new StringBuilder("");
        for (Entry<String, String> entry : map.entrySet()) {
            sb.append((String) entry.getKey());
            sb.append(":");
            sb.append((String) entry.getValue());
            sb.append(",");
        }
        return sb.toString();
    }
}
