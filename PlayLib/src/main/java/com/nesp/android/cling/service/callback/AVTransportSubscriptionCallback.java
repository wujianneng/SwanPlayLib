package com.nesp.android.cling.service.callback;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import com.nesp.android.cling.Config;
import com.nesp.android.cling.Intents;
import com.nesp.android.cling.util.LogUtils;
import com.nesp.android.cling.util.Utils;
import org.teleal.cling.model.gena.GENASubscription;
import org.teleal.cling.support.avtransport.lastchange.AVTransportLastChangeParser;
import org.teleal.cling.support.avtransport.lastchange.AVTransportVariable;
import org.teleal.cling.support.lastchange.LastChange;
import org.teleal.cling.support.model.TransportState;

import java.util.Map;

/**
 * 说明：

 * 日期：15/7/17 AM11:33
 */

public class AVTransportSubscriptionCallback  extends BaseSubscriptionCallback {

    private static final String TAG = AVTransportSubscriptionCallback.class.getSimpleName();

    public AVTransportSubscriptionCallback(org.teleal.cling.model.meta.Service service, Context context) {
        super(service, context);
    }

    @Override
    protected void eventReceived(GENASubscription subscription) { // 这里进行 事件接收处理
        LogUtils.e(TAG, "SubscriptionAVTransport:1" + subscription.toString());
        if (Utils.isNull(mContext))
            return;

        Map values = subscription.getCurrentValues();
        LogUtils.e(TAG, "SubscriptionAVTransport:2" + values.toString());
        if (values != null && values.containsKey("LastChange")) {
            String lastChangeValue = values.get("LastChange").toString();
            LogUtils.e(TAG, "SubscriptionAVTransport:3" + lastChangeValue);
//            doAVTransportChange(lastChangeValue);
        }
    }

    private void doAVTransportChange(String lastChangeValue) {
        LogUtils.e(TAG, "SubscriptionAVTransport:4 " + lastChangeValue);
        try {
            LastChange lastChange = new LastChange(new AVTransportLastChangeParser(), lastChangeValue);

            //Parse TransportState value.
            AVTransportVariable.TransportState transportState = lastChange.getEventedValue(0, AVTransportVariable.TransportState.class);
            if (transportState != null) {
                TransportState ts = transportState.getValue();
                if (ts == TransportState.PLAYING) {
                    LogUtils.e(TAG, "PLAYING");
                    Intent intent = new Intent(Intents.ACTION_PLAYING);
                    mContext.sendBroadcast(intent);
                    return;
                } else if (ts == TransportState.PAUSED_PLAYBACK) {
                    LogUtils.e(TAG, "PAUSED_PLAYBACK");
                    Intent intent = new Intent(Intents.ACTION_PAUSED_PLAYBACK);
                    mContext.sendBroadcast(intent);
                    return;
                } else if (ts == TransportState.STOPPED) {
                    LogUtils.e(TAG, "STOPPED");
                    Intent intent = new Intent(Intents.ACTION_STOPPED);
                    mContext.sendBroadcast(intent);
                    return;
                } else if (ts == TransportState.TRANSITIONING) { // 转菊花状态
                    LogUtils.e(TAG, "BUFFER");
                    Intent intent = new Intent(Intents.ACTION_TRANSITIONING);
                    mContext.sendBroadcast(intent);
                    return;
                }
            }

//            //RelativeTimePosition
//            String position = "00:00:00";
//            AVTransportVariable.RelativeTimePosition eventedValue = lastChange.getEventedValue(0, AVTransportVariable.RelativeTimePosition.class);
//            if (Utils.isNotNull(eventedValue)) {
//                position = lastChange.getEventedValue(0, AVTransportVariable.RelativeTimePosition.class).getValue();
//                int intTime = Utils.getIntTime(position);
//                LogUtils.e(TAG, "position: " + position + ", intTime: " + intTime);
//
//                // 该设备支持进度回传
//                Config.getInstance().setHasRelTimePosCallback(true);
//
//                Intent intent = new Intent(Intents.ACTION_POSITION_CALLBACK);
//                intent.putExtra(Intents.EXTRA_POSITION, intTime);
//                mContext.sendBroadcast(intent);

                // TODO: 17/7/20 ACTION_PLAY_COMPLETE 播完了

//            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

}