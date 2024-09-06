package com.nesp.android.cling.service.callback;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.nesp.android.cling.Config;
import com.nesp.android.cling.Intents;
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

public class MediaServerSubscriptionCallback extends BaseSubscriptionCallback {

    private static final String TAG = MediaServerSubscriptionCallback.class.getSimpleName();

    public MediaServerSubscriptionCallback(org.teleal.cling.model.meta.Service service, Context context) {
        super(service, context);
    }

    @Override
    protected void eventReceived(GENASubscription subscription) { // 这里进行 事件接收处理
        Log.i(TAG, "SubscriptionMediaServer:" + subscription.toString());
        if (Utils.isNull(mContext))
            return;

        Map values = subscription.getCurrentValues();
        if (values != null && values.containsKey("LastChange")) {
            String lastChangeValue = values.get("LastChange").toString();
            Log.i(TAG, "LastChange:" + lastChangeValue);
        }
    }


}