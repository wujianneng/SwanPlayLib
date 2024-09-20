package com.nesp.android.cling.util;

import android.util.Log;

public class LogUtils {
    private static boolean canLog = true;

    public static void setCanLog(boolean iscan){
        canLog = iscan;
    }

    public static void e(String key, String value) {
        if (canLog) {
            Log.e(key, value);
        }
    }

    public static void e(String value) {
        if (canLog) {
            Log.e("test", value);
        }
    }
}
