package com.nesp.android.cling.listener;

import android.util.Log;

import com.nesp.android.cling.entity.SWDevice;

import com.nesp.android.cling.service.manager.SWDeviceManager;
import com.nesp.android.cling.util.LogUtils;
import com.nesp.android.cling.util.Utils;

import org.teleal.cling.model.meta.Device;
import org.teleal.cling.model.meta.LocalDevice;
import org.teleal.cling.model.meta.RemoteDevice;
import org.teleal.cling.registry.DefaultRegistryListener;
import org.teleal.cling.registry.Registry;


public class BrowseRegistryListener extends DefaultRegistryListener {


    private static final String TAG = BrowseRegistryListener.class.getSimpleName();

    private DeviceListChangedListener mOnDeviceListChangedListener;


    /* Discovery performance optimization for very slow Android devices! */
    @Override
    public void remoteDeviceDiscoveryStarted(Registry registry, RemoteDevice device) {
        super.remoteDeviceDiscoveryStarted(registry, device);
        // 在这里设备拥有服务 也木有 action。。
//        deviceAdded(device);
    }

    @Override
    public void remoteDeviceDiscoveryFailed(Registry registry, final RemoteDevice device, final Exception ex) {
        LogUtils.e(TAG, "remoteDeviceDiscoveryFailed device: " + device.getDisplayString());
        deviceRemoved(device);
    }
    /* End of optimization, you can remove the whole block if your Android handset is fast (>= 600 Mhz) */

    @Override
    public void remoteDeviceAdded(Registry registry, RemoteDevice device) {
        super.remoteDeviceAdded(registry, device);
        LogUtils.e(TAG, "deviceAdded:" + device.getDetails().getFriendlyName() + " ip：" + device.getIdentity().getDescriptorURL().getHost());
        deviceAdded(device);
    }

    @Override
    public void remoteDeviceRemoved(Registry registry, RemoteDevice device) {
        super.remoteDeviceRemoved(registry, device);
        deviceRemoved(device);
    }

    @Override
    public void localDeviceAdded(Registry registry, LocalDevice device) {
        super.localDeviceAdded(registry, device);
        LogUtils.e(TAG, "deviceAdded:2" + device.getDetails().getFriendlyName());
//                deviceAdded(device); // 本地设备 已加入
    }

    @Override
    public void localDeviceRemoved(Registry registry, LocalDevice device) {
        super.localDeviceRemoved(registry, device);
//                deviceRemoved(device); // 本地设备 已移除
    }


    private void deviceAdded(RemoteDevice device) {
        LogUtils.e(TAG, "deviceAdded:" + device.getDetails().getFriendlyName() + " 嵌入设备数量：" + device.findEmbeddedDevices().length);
        if (!device.getType().equals(SWDeviceManager.DMR_DEVICE_TYPE)) {
            LogUtils.e(TAG, "deviceAdded called, but not match");
            return;
        }


        SWDevice SWDevice = new SWDevice(device);
        SWDeviceManager.getInstance().addDevice(SWDevice);
        if (Utils.isNotNull(mOnDeviceListChangedListener)) {
            mOnDeviceListChangedListener.onDeviceAdded(SWDevice);
        }
    }

    public void deviceRemoved(Device device) {
        LogUtils.e(TAG, "deviceRemoved: " + device.getDetails().getFriendlyName());
        SWDevice SWDevice = SWDeviceManager.getInstance().getClingDevice(device);
        if (SWDevice != null) {
//                SWDeviceList.getInstance().removeDevice(SWDevice);
            if (Utils.isNotNull(mOnDeviceListChangedListener)) {
                mOnDeviceListChangedListener.onDeviceRemoved(SWDevice);
            }
            LogUtils.e(TAG, "deviceRemoved: " + device.getDetails().getFriendlyName() + (SWDevice == null));
        }

    }

    public void setOnDeviceListChangedListener(DeviceListChangedListener onDeviceListChangedListener) {
        mOnDeviceListChangedListener = onDeviceListChangedListener;
    }
}
