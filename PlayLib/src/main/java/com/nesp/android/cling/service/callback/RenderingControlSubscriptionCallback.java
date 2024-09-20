package com.nesp.android.cling.service.callback;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import com.nesp.android.cling.Intents;
import com.nesp.android.cling.util.LogUtils;
import com.nesp.android.cling.util.Utils;
import org.teleal.cling.model.gena.GENASubscription;
import org.teleal.cling.model.meta.Service;
import org.teleal.cling.model.state.StateVariableValue;
import org.teleal.cling.support.lastchange.LastChange;
import org.teleal.cling.support.renderingcontrol.lastchange.RenderingControlLastChangeParser;
import org.teleal.cling.support.renderingcontrol.lastchange.RenderingControlVariable;

import java.util.Map;

/**
 * 说明：RenderingControl 事件回传

 * 日期：17/7/18 18:54
 */

public class RenderingControlSubscriptionCallback extends BaseSubscriptionCallback {

    private static final String TAG = RenderingControlSubscriptionCallback.class.getSimpleName();

    public RenderingControlSubscriptionCallback(Service service, Context context) {
        super(service, context);
    }

    @Override
    protected void eventReceived(GENASubscription subscription) {
        LogUtils.e(TAG, "SubscriptioneventReceived:" + subscription.toString());
        Map<String, StateVariableValue> values = subscription.getCurrentValues();
        if (Utils.isNull(values)) {
            return;
        }
        if (Utils.isNull(mContext)) {
            return;
        }
        if (!values.containsKey("LastChange")) {
            return;
        }

        String lastChangeValue = values.get("LastChange").toString();
        LogUtils.e(TAG, "LastChange:" + lastChangeValue);
        LastChange lastChange;
        try {
            lastChange = new LastChange(new RenderingControlLastChangeParser(), lastChangeValue);
            //获取音量 volume
            int volume = 0;
            if (lastChange.getEventedValue(0, RenderingControlVariable.Volume.class) != null) {

                volume = lastChange.getEventedValue(0, RenderingControlVariable.Volume.class).getValue().getVolume();

                LogUtils.e(TAG, "onVolumeChange volume: " + volume);
                Intent intent = new Intent(Intents.ACTION_VOLUME_CALLBACK);
                intent.putExtra(Intents.EXTRA_VOLUME, volume);
                mContext.sendBroadcast(intent);

            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

}

