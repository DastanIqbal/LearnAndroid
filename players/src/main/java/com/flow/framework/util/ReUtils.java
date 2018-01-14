package com.flow.framework.util;

import android.text.TextUtils;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ReUtils {
    public static boolean checkPINAddress(String regexStr, String strAddress) {
        if (TextUtils.isEmpty(regexStr) || TextUtils.isEmpty(strAddress)) {
            return false;
        }
        return Pattern.matches(regexStr.trim(), strAddress.trim());
    }

    public static boolean checkPINBody(String regexStr, String strBody) {
        if (TextUtils.isEmpty(regexStr) || TextUtils.isEmpty(strBody)) {
            return false;
        }
        return Pattern.matches(regexStr.trim(), strBody.trim());
    }

    public static String getVcodeByRegex(String regexStr, String strBody) {
        if (TextUtils.isEmpty(regexStr) || TextUtils.isEmpty(strBody)) {
            return null;
        }
        regexStr = regexStr.trim();
        Matcher matcher = Pattern.compile(regexStr).matcher(strBody.trim());
        if (matcher.find()) {
            return matcher.group(0);
        }
        return null;
    }

    public static String[] getPhonenumAndBody(String tempStr) {
        if (TextUtils.isEmpty(tempStr)) {
            return null;
        }
        String[] tempArray = tempStr.split("\\?");
        if (tempArray == null || tempArray.length <= 0) {
            return null;
        }
        String shortCode = tempArray[0].split(":")[1];
        String keyword = tempArray[1].split("=")[1];
        if (TextUtils.isEmpty(shortCode) || TextUtils.isEmpty(keyword)) {
            return null;
        }
        return new String[]{shortCode, keyword};
    }
}
