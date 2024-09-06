package com.nesp.android.cling.service.callback;

import android.content.Context;
import android.util.Log;
import org.teleal.cling.controlpoint.SubscriptionCallback;
import org.teleal.cling.model.gena.CancelReason;
import org.teleal.cling.model.gena.GENASubscription;
import org.teleal.cling.model.message.UpnpResponse;
import org.teleal.cling.model.meta.Service;

/**
 * 说明：

 * 日期：17/7/20 15:48
 */

public abstract class BaseSubscriptionCallback extends SubscriptionCallback {

    private static final int SUBSCRIPTION_DURATION_SECONDS = 3600 * 3;
    private static final String TAG = BaseSubscriptionCallback.class.getSimpleName();
    protected Context mContext;

    protected BaseSubscriptionCallback(Service service, Context context) {
        super(service, SUBSCRIPTION_DURATION_SECONDS);
        mContext = context;
    }

    @Override
    protected void failed(GENASubscription subscription, UpnpResponse responseStatus, Exception exception, String defaultMsg) {
        Log.e(TAG, "AVTransportSubscriptionCallback failed.");
    }

    @Override
    protected void established(GENASubscription subscription) {
    }

    @Override
    protected void eventsMissed(GENASubscription subscription, int numberOfMissedEvents) {
    }

    @Override
    protected void ended(GENASubscription subscription, CancelReason reason, UpnpResponse responseStatus) {
        mContext = null;
        Log.e(TAG, "ended");
    }
}
