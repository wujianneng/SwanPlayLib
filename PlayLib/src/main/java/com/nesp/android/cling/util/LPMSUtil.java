package com.nesp.android.cling.util;

//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//


import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

import java.util.regex.Pattern;

public class LPMSUtil {
    public LPMSUtil() {
    }

    public static void showToast(final Context var0, final String var1) {
        (new Handler(Looper.getMainLooper())).post(new Runnable() {
            public void run() {
                Toast.makeText(var0, var1, 0).show();
            }
        });
    }

    public static void showLongToast(final Context var0, final String var1) {
        (new Handler(Looper.getMainLooper())).post(new Runnable() {
            public void run() {
                Toast.makeText(var0, var1, 1).show();
            }
        });
    }


    public static String formatLoginData(String var0) {
        try {
            return var0.replace("\r", "").replace("\n", "");
        } catch (Exception var2) {
            var2.printStackTrace();
            return var0;
        }
    }

    public static String getRandom() {
        return Integer.toHexString((int)(Math.random() * 65535.0D + 1.0D));
    }

    public static boolean isNumber(String var0) {
        return Pattern.compile("[0-9]*").matcher(var0).matches();
    }
}
