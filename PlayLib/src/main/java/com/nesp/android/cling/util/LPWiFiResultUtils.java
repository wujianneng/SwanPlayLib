package com.nesp.android.cling.util;


import android.content.Context;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.text.TextUtils;

import java.util.regex.Pattern;


public class LPWiFiResultUtils {
    public LPWiFiResultUtils() {
    }

    public static boolean isWiFiConnectAP(Context var0) {
        return b(var0) ^ true;
    }

    public static WifiInfo getCurrentWifiInfo(Context var0) {
        return getWifiInfo(var0);
    }

    public static String makeSSIDNoneQuoted(String ssid) {
        return ssid != null ? ssid.replace("\"", "").trim() : null;
    }

    public static WifiInfo getWifiInfo(Context context) {
        return ((WifiManager)context.getSystemService("wifi")).getConnectionInfo();
    }

    public static boolean b(Context context) {
        Context var10000 = context;
        boolean context1 = false;
        WifiInfo var1;
        if ((var1 = getWifiInfo(var10000)) == null) {
            return false;
        } else {
            String var3;
            if (TextUtils.isEmpty(var3 = var1.getBSSID())) {
                return false;
            } else {
                if (var3.startsWith("00:22:6c") || var3.startsWith("00:25:92") || var3.startsWith("0:22:6c") || var3.startsWith("0:25:92")) {
                    context1 = true;
                }

                return context1;
            }
        }
    }

    public static String a(String hexString) {
        if (hexString.replaceAll("[a-f0-9A-F]", "").length() == 0) {
            int var1;
            byte[] var2 = new byte[var1 = hexString.length() / 2];

            Exception var13;
            boolean var14;
            for(int var3 = 0; var3 < var1; ++var3) {
                byte[] var10000 = var2;
                int var10001 = var3;
                String var10002 = hexString;
                short var4 = 255;
                int var10003 = var3 * 2;
                int var10004 = var3 * 2 + 2;

                label93: {
                    int var17;
                    try {
                        var17 = Integer.parseInt(var10002.substring(var10003, var10004), 16);
                    } catch (Exception var6) {
                        var13 = var6;
                        var14 = false;
                        break label93;
                    }

                    byte var19 = (byte)(var17 & var4);

                    try {
                        var10000[var10001] = var19;
                        continue;
                    } catch (Exception var5) {
                        var13 = var5;
                        var14 = false;
                    }
                }

                var13.printStackTrace();
            }

            label94: {
                String var15;
                try {
                    var15 = new String(var2, "utf-8");
                } catch (Exception var11) {
                    var13 = var11;
                    var14 = false;
                    break label94;
                }

                String var12;
                String var16 = var12 = var15;

                int var18;
                try {
                    var18 = var15.length();
                } catch (Exception var10) {
                    var13 = var10;
                    var14 = false;
                    break label94;
                }

                if (var18 == 0) {
                    return hexString;
                }

                boolean var20;
                try {
                    var20 = d(var12);
                } catch (Exception var9) {
                    var13 = var9;
                    var14 = false;
                    break label94;
                }

                if (!var20) {
                    return var12;
                }

                try {
                    var15 = new String(var2, "GBK");
                } catch (Exception var8) {
                    var13 = var8;
                    var14 = false;
                    break label94;
                }

                var16 = var12 = var15;

                try {
                    var18 = var15.length();
                } catch (Exception var7) {
                    var13 = var7;
                    var14 = false;
                    break label94;
                }

                if (var18 == 0) {
                    return hexString;
                }

                return var12;
            }

            var13.printStackTrace();
        }

        return hexString;
    }

    public static boolean d(String strName) {
        char[] strName1;
        float var1 = (float)(strName1 = Pattern.compile("\\s*|\t*|\r*|\n*").matcher(strName).replaceAll("").replaceAll("\\p{P}", "").trim().toCharArray()).length;
        float var2 = 0.0F;

        for(int var3 = 0; var3 < strName1.length; ++var3) {
            char var4;
            if (!Character.isLetterOrDigit(var4 = strName1[var3]) && !bb(var4)) {
                ++var2;
                System.out.print(var4);
            }
        }

        if ((double)(var2 / var1) > 0.4D) {
            return true;
        } else {
            return false;
        }
    }

    public static boolean bb(char c) {
        Character.UnicodeBlock c1;
        return (c1 = Character.UnicodeBlock.of(c)) == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS || c1 == Character.UnicodeBlock.CJK_COMPATIBILITY_IDEOGRAPHS || c1 == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_A || c1 == Character.UnicodeBlock.GENERAL_PUNCTUATION || c1 == Character.UnicodeBlock.CJK_SYMBOLS_AND_PUNCTUATION || c1 == Character.UnicodeBlock.HALFWIDTH_AND_FULLWIDTH_FORMS;
    }
}
