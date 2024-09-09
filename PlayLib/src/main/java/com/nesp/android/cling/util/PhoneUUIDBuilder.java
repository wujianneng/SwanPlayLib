package com.nesp.android.cling.util;

import android.os.Build;

public class PhoneUUIDBuilder {
    public static String a;

    public PhoneUUIDBuilder() {
    }

    public static String uuidBuild() {
        return a;
    }

    static {
        String var0;
        a = (a = (var0 = Build.DISPLAY) + Build.BOARD.length() % 10 + Build.BRAND.length() % 10 + Build.CPU_ABI.length() % 10 + Build.DEVICE.length() % 10 + var0.length() % 10 + Build.HOST.length() % 10 + Build.ID.length() % 10 + Build.MANUFACTURER.length() % 10 + Build.MODEL.length() % 10 + Build.PRODUCT.length() % 10 + Build.TAGS.length() % 10 + Build.TYPE.length() % 10 + Build.USER.length() % 10).replace(" ", "");
    }
}