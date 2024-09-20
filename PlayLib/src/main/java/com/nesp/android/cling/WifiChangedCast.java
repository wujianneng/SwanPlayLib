package com.nesp.android.cling;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

import com.linkplay.core.app.LPDeviceManager;

import com.nesp.android.cling.service.manager.SWDeviceManager;
import com.nesp.android.cling.util.LogUtils;

import org.greenrobot.eventbus.EventBus;

public class WifiChangedCast extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals("android.net.conn.CONNECTIVITY_CHANGE")) {
            NetworkInfo.State arg01 = ((ConnectivityManager)context.getSystemService("connectivity")).getNetworkInfo(1).getState();
            LogUtils.e("LINKPLAY_SDK", "wifi changed and State=" + arg01);
            if (arg01 == NetworkInfo.State.CONNECTED) {
                LogUtils.e("LINKPLAY_SDK", "wifi changed and State=reInit()" + arg01);
                SWDeviceManager.getInstance().getMasterDeviceList().clear();
                EventBus.getDefault().post(SWDeviceManager.REFRESH_LIST_UI_KEY);
                SWDeviceManager.getInstance().refreshDevicesList();
            } else if (arg01 != NetworkInfo.State.DISCONNECTED && arg01 != NetworkInfo.State.UNKNOWN) {
                arg01 = NetworkInfo.State.CONNECTING;
            }else {
                SWDeviceManager.getInstance().getMasterDeviceList().clear();
                EventBus.getDefault().post(SWDeviceManager.REFRESH_LIST_UI_KEY);
                SWDeviceManager.getInstance().refreshDevicesList();
            }
        }
    }
}
