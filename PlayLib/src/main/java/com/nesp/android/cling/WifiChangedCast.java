package com.nesp.android.cling;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

import com.linkplay.core.app.LPDeviceManager;
import com.nesp.android.cling.entity.SWDeviceList;
import com.nesp.android.cling.service.manager.SWDeviceManager;

import org.greenrobot.eventbus.EventBus;

public class WifiChangedCast extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals("android.net.conn.CONNECTIVITY_CHANGE")) {
            NetworkInfo.State arg01 = ((ConnectivityManager)context.getSystemService("connectivity")).getNetworkInfo(1).getState();
            Log.e("LINKPLAY_SDK", "wifi changed and State=" + arg01);
            if (arg01 == NetworkInfo.State.CONNECTED) {
                Log.e("LINKPLAY_SDK", "wifi changed and State=reInit()" + arg01);
                SWDeviceList.masterDevices.clear();
                EventBus.getDefault().post(SWDeviceList.REFRESH_LIST_UI_KEY);
                SWDeviceList.getInstance().mSWDeviceList.clear();
                SWDeviceManager.getInstance().refreshDevicesList();
            } else if (arg01 != NetworkInfo.State.DISCONNECTED && arg01 != NetworkInfo.State.UNKNOWN) {
                arg01 = NetworkInfo.State.CONNECTING;
            }else {
                SWDeviceList.masterDevices.clear();
                EventBus.getDefault().post(SWDeviceList.REFRESH_LIST_UI_KEY);
                SWDeviceList.getInstance().mSWDeviceList.clear();
                SWDeviceManager.getInstance().refreshDevicesList();
            }
        }
    }
}
