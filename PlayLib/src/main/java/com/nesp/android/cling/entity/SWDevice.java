package com.nesp.android.cling.entity;

import org.teleal.cling.model.meta.Device;
import org.teleal.cling.model.meta.RemoteDevice;

import java.util.ArrayList;
import java.util.List;


public class SWDevice implements IDevice<Device> {

    private String ip;

    private String uuid;

    private Device mDevice;
    /** 是否已选中 */
    private boolean isSelected;

    /** 播放状态类 */
    PlayStatusBean playStatusBean;
    /** 媒体信息类 */
    MusicDataBean.DataBean mediaInfo;
    /** 设备信息类 */
    SWDeviceInfo swDeviceInfo = new SWDeviceInfo();


    public SWDevice(Device device) {
        this.mDevice = device;
        this.ip =  ((RemoteDevice)device).getIdentity().getDescriptorURL().getHost();
        this.uuid = device.getIdentity().getUdn().getIdentifierString();
    }

    public PlayStatusBean getPlayStatusBean() {
        return playStatusBean;
    }

    public void setPlayStatusBean(PlayStatusBean playStatusBean) {
        this.playStatusBean = playStatusBean;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public Device getmDevice() {
        return mDevice;
    }

    public void setmDevice(Device mDevice) {
        this.mDevice = mDevice;
    }

    public MusicDataBean.DataBean getMediaInfo() {
        return mediaInfo;
    }

    public void setMediaInfo(MusicDataBean.DataBean lpMediaInfo) {
        this.mediaInfo = lpMediaInfo;
    }

    @Override
    public Device getDevice() {
        return mDevice;
    }

    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean selected) {
        isSelected = selected;
    }

    public SWDeviceInfo getSwDeviceInfo() {
        return swDeviceInfo;
    }

    public void setSwDeviceInfo(SWDeviceInfo swDeviceInfo) {
        this.swDeviceInfo = swDeviceInfo;
    }
}