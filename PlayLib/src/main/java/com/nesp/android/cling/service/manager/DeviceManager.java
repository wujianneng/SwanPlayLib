package com.nesp.android.cling.service.manager;


import android.content.Context;
import android.util.Log;

import com.nesp.android.cling.Config;
import com.nesp.android.cling.control.SubscriptionControl;
import com.nesp.android.cling.entity.SWDevice;
import com.nesp.android.cling.entity.IDevice;
import com.nesp.android.cling.util.Utils;

import java.util.Collection;


public class DeviceManager implements IDeviceManager {
    private static final String TAG = DeviceManager.class.getSimpleName();
    /**
     * 已选中的设备, 它也是 SWDeviceList 中的一员
     */
    private SWDevice mSelectedDevice;
    private SubscriptionControl mSubscriptionControl;

    public DeviceManager() {
        mSubscriptionControl = new SubscriptionControl();
    }

    @Override
    public SWDevice getSelectedDevice() {
        return mSelectedDevice;
    }

    @Override
    public void setSelectedDevice(SWDevice selectedDevice) {
//        if (selectedDevice != mSelectedDevice){
//            Intent intent = new Intent(Intents.ACTION_CHANGE_DEVICE);
//            sendBroadcast(intent);
//        }

        Log.i(TAG, "Change selected device.");
        mSelectedDevice = (SWDevice) selectedDevice;

        // 重置选中状态
        Collection<SWDevice> SWDeviceList = com.nesp.android.cling.entity.SWDeviceList.getInstance().getClingDeviceList();
        if (Utils.isNotNull(SWDeviceList)) {
            for (SWDevice device : SWDeviceList) {
                device.setSelected(false);
            }
        }
        // 设置选中状态
        if (mSelectedDevice != null)
            mSelectedDevice.setSelected(true);
        // 清空状态
        Config.getInstance().setHasRelTimePosCallback(false);
    }

    @Override
    public void cleanSelectedDevice() {
        if (Utils.isNull(mSelectedDevice))
            return;

        mSelectedDevice.setSelected(false);
    }

    @Override
    public void registerAVTransport(Context context) {
        if (Utils.isNull(mSelectedDevice))
            return;

        mSubscriptionControl.registerAVTransport(mSelectedDevice, context);
    }

    @Override
    public void registerRenderingControl(Context context) {
        if (Utils.isNull(mSelectedDevice))
            return;

        mSubscriptionControl.registerRenderingControl(mSelectedDevice, context);
    }

    /**
     * 监听投屏端 MediaRenderer 回调
     *
     * @param context
     */
    @Override
    public void registerMediaRenderer(Context context) {
        if (Utils.isNull(mSelectedDevice))
            return;

        mSubscriptionControl.registerMediaRenderer(mSelectedDevice, context);
    }

    /**
     * 监听投屏端 MediaServer 回调
     *
     * @param context
     */
    @Override
    public void registerMediaServer(Context context) {
        if (Utils.isNull(mSelectedDevice))
            return;

        mSubscriptionControl.registerMediaServer(mSelectedDevice, context);
    }

    /**
     * 监听投屏端 ConnectionManager 回调
     *
     * @param context
     */
    @Override
    public void registerConnectionManager(Context context) {
        if (Utils.isNull(mSelectedDevice))
            return;

        mSubscriptionControl.registerConnectionManager(mSelectedDevice, context);
    }

    /**
     * 监听投屏端 ContentDirectory 回调
     *
     * @param context
     */
    @Override
    public void registerContentDirectory(Context context) {
        if (Utils.isNull(mSelectedDevice))
            return;

        mSubscriptionControl.registerContentDirectory(mSelectedDevice, context);
    }

    @Override
    public void destroy() {
        if (Utils.isNotNull(mSubscriptionControl)) {
            mSubscriptionControl.destroy();
        }
    }
}
