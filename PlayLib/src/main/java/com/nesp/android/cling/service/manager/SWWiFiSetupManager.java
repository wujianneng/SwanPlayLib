package com.nesp.android.cling.service.manager;

import android.content.Context;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.text.TextUtils;
import android.util.Log;

import com.android.wiimu.f.a;
import com.google.gson.Gson;
import com.linkplay.core.app.LPDeviceManager;
import com.linkplay.core.utils.LPWiFiResultUtils;
import com.linkplay.log.LinkplayLog;
import com.linkplay.network.HttpRequestUtils;
import com.linkplay.network.IOkHttpRequestCallback;
import com.linkplay.network.OkHttpResponseItem;
import com.linkplay.request.RequestItem;
import com.linkplay.request.RequestItem.Builder;
import com.linkplay.wifisetup.LPApItem;
import com.linkplay.wifisetup.LPApListListener;
import com.nesp.android.cling.entity.SWDevice;
import com.nesp.android.cling.entity.SWDeviceList;
import com.nesp.android.cling.entity.SlaveBean;
import com.nesp.android.cling.util.OkHttp3Util;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.EOFException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class SWWiFiSetupManager {
    private SWDevice curDevice;
    private static LPApItem targetApItem;
    private SearchTimer searchTimer;

    public SWWiFiSetupManager() {
    }
    public SWWiFiSetupManager(SWDevice curDevice) {
        this.curDevice = curDevice;
    }

    public boolean isLinkplayHotspot(Context context) {
        WifiManager var3 = (WifiManager)context.getSystemService("wifi");
        WifiInfo var4 = var3.getConnectionInfo();
        String var5 = var4.getSSID();
        Log.e("SWWiFiSetupManager", "var50:" + var5);
        if (var5.startsWith("\"")) {
            var5 = var5.substring(1).trim();
        }

        if (var5.endsWith("\"")) {
            var5 = var5.substring(0, var5.length() - 1).trim();
        }
        Log.e("SWWiFiSetupManager", "var51:" + var5);
        List<SWDevice> var6 = SWDeviceList.masterDevices;
        if (var6 != null && var6.size() == 1 && var6.get(0) != null && var6.get(0).getSwDeviceInfo().getSWDeviceStatus() != null && var6.get(0).getSwDeviceInfo().getSWDeviceStatus().getSsid().equals(var5)) {
            this.curDevice = (SWDevice)var6.get(0);
            Log.e("SWWiFiSetupManager", "var52:" + var6.get(0).getSwDeviceInfo().getSWDeviceStatus().getSsid());
            return true;
        } else {
            this.curDevice = null;
            return false;
        }
    }

    public void getApList(final LPApListListener listener) {
        if (curDevice == null) {
            Log.e("SWWiFiSetupManager", "Please confirm whether to connect isLinkplayHotspot first.");
            if (listener != null) {
                listener.LPApList((List)null);
            }

        } else {
            OkHttp3Util.doGet("http://" + curDevice.getIp() + "/httpapi.asp?command=wlanGetApListEx", new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    Log.e("SWWiFiSetupManager", "getApList:" + e.getLocalizedMessage());
                    if (listener != null) {
                        listener.LPApList((List)null);
                    }
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    if (response == null) {
                        this.onFailure(call,new EOFException());
                    } else {
                        String var2 = new String(response.body().bytes());
                        Log.e("SWWiFiSetupManager", "getApList onSuccess:" + var2);
                        List var3 = parseApItems(var2);
                        if (listener != null) {
                            listener.LPApList(var3);
                        }
                    }

                }
            });
        }
    }

    public List<LPApItem> parseApItems(String content) {
        if (TextUtils.isEmpty(content)) {
            return null;
        } else {
            Gson var2 = new Gson();
            ArrayList var3 = null;

            try {
                JSONObject var4 = new JSONObject(content);
                JSONArray var5 = var4.getJSONArray("aplist");
                if (var5 != null && var5.length() != 0) {
                    if (var3 == null) {
                        var3 = new ArrayList();
                    }

                    int var6 = var5.length();

                    for(int var7 = 0; var7 < var6; ++var7) {
                        JSONObject var8 = var5.getJSONObject(var7);
                        LPApItem var9 = (LPApItem)var2.fromJson(var8.toString(), LPApItem.class);
                        var3.add(var9);
                    }

                    return var3;
                } else {
                    return var3;
                }
            } catch (JSONException var13) {
                var13.printStackTrace();
                return var3;
            } finally {
                ;
            }
        }
    }

    public void connectToWiFi(LPApItem apitem, String apitemPwd, final SWWiFiSetupListener listener) {
        if (this.curDevice != null) {
            targetApItem = apitem;
            String var5 = apitem.SSID;
            String var6 = "";
            String var7 = "";
            String var8 = "";
            String var9 = apitem.Channel + "";
            String var10 = "1";
            if (apitem.Encry.equals("NONE")) {
                var6 = "NONE";
                var7 = "NONE";
                var8 = "OPEN";
                var6 = "";
            } else {
                var6 = a.a(apitemPwd.getBytes());
                var7 = apitem.Encry;
                var8 = apitem.Auth;
            }

            String var11 = "wlanConnectApEx:";
            var11 = var11 + "ssid=" + var5 + ":ch=" + var9 + ":auth=" + var8 + ":encry=" + var7 + ":pwd=" + var6 + ":chext=" + var10;
            OkHttp3Util.doGet("http://" + curDevice.getIp() + "/httpapi.asp?command=" + var11, new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    LinkplayLog.i("SWWiFiSetupManager", "connectToWiFi onFailure:" + e.getLocalizedMessage());
                    if (listener != null) {
                        listener.SWWiFiSetupFailed("1002");
                    }
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    if (response == null) {
                        this.onFailure(call,new EOFException());
                    } else {
                        LinkplayLog.i("SWWiFiSetupManager", "connectToWiFi onSuccess");
                        if (searchTimer != null) {
                            searchTimer.cancel();
                            searchTimer = null;
                        }

                        searchTimer = new SearchTimer(curDevice, listener);
                        searchTimer.search();
                    }
                }
            });
        }
    }

    public interface SWWiFiSetupListener {
        void SWWiFiSetupSuccess(SWDevice var1);

        void SWWiFiSetupFailed(String var1);
    }

    public void retryCheckWithTime(int timeout, SWWiFiSetupListener listener) {
        LinkplayLog.i("SWWiFiSetupManager", "retryCheckWithTime");
        if (this.searchTimer != null) {
            this.searchTimer.cancel();
            this.searchTimer = null;
        }

        this.searchTimer = new SearchTimer(this.curDevice, listener);
        if (timeout <= 0) {
            timeout = 30000;
        }

        this.searchTimer.setTimeout(timeout);
        this.searchTimer.search();
    }

    static class SearchTimer extends Timer {
        private static long TIMEOUT = 50000L;
        private SWDevice device;
        private SWWiFiSetupListener listener;

        public SearchTimer(SWDevice device, SWWiFiSetupListener listener) {
            this.device = device;
            this.listener = listener;
        }

        public void setTimeout(int timeout) {
            TIMEOUT = (long)timeout;
        }

        public void search() {
            LPDeviceManager.getInstance().clear();
            final long var1 = System.currentTimeMillis();
            this.schedule(new TimerTask() {
                public void run() {
                    if (System.currentTimeMillis() - var1 > SearchTimer.TIMEOUT) {
                        this.cancel();
                        if (SearchTimer.this.listener == null) {
                            return;
                        }

                        WifiInfo var1x = LPWiFiResultUtils.getCurrentWifiInfo();
                        if (var1x == null) {
                            SearchTimer.this.listener.SWWiFiSetupFailed("1002");
                            return;
                        }

                        if (!a.a(SWWiFiSetupManager.targetApItem.SSID).equals(LPWiFiResultUtils.makeSSIDNoneQuoted(var1x.getSSID()))) {
                            SWWiFiSetupManager.SearchTimer.this.listener.SWWiFiSetupFailed("1001");
                        } else {
                            SearchTimer.this.listener.SWWiFiSetupFailed("1002");
                        }
                    }

                    SWDevice var4 = deviceForID(SearchTimer.this.device.getSwDeviceInfo().getSWDeviceStatus().getUuid());
                    if(var4 != null){
                        Log.e("test","netstat:" + var4.getSwDeviceInfo().getSWDeviceStatus().getNetstat());
                    }else {
                        Log.e("test","netstat:" + "var4 == null");
                    }
                    if (var4 != null && var4.getSwDeviceInfo().getSWDeviceStatus() != null) {
                        Log.e("test","netstat:" + var4.getSwDeviceInfo().getSWDeviceStatus().getNetstat());
                        if(var4.getSwDeviceInfo().getSWDeviceStatus().getNetstat().equals("1")) {
                            if (SearchTimer.this.listener != null) {
                                SearchTimer.this.listener.SWWiFiSetupSuccess(var4);
                            }
                        }else {
                            if (SearchTimer.this.listener != null) {
                                SearchTimer.this.listener.SWWiFiSetupFailed(var4.getSwDeviceInfo().getSWDeviceStatus().getNetstat());
                            }
                        }
                        this.cancel();
                    }

                }
            }, 1000L, 3000L);
        }
    }

    public static SWDevice deviceForID(String lpDeviceuuid) {
        for (SWDevice lpDevice : SWDeviceList.masterDevices) {
            if (lpDevice.getSwDeviceInfo().getSWDeviceStatus().getUuid().equals(lpDeviceuuid)) {
                return lpDevice;
            }
        }
        return null;
    }
}