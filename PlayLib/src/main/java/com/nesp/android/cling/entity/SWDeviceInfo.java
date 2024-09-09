package com.nesp.android.cling.entity;

import java.util.ArrayList;
import java.util.List;

public class SWDeviceInfo {
    String MultiType;
    String Router;
    String Ssid;
    String SlaveMask;
    String CurrentVolume;
    String CurrentMute;
    String CurrentChannel;
    /** 设备状态类 */
    SWDeviceStatus SWDeviceStatus = new SWDeviceStatus();
    /** 设备子设备列表 */
    private List<SlaveBean.SlaveListDTO> slaveList = new ArrayList<>();

    public String getMultiType() {
        return MultiType;
    }

    public void setMultiType(String multiType) {
        MultiType = multiType;
    }

    public String getRouter() {
        return Router;
    }

    public void setRouter(String router) {
        Router = router;
    }

    public String getSsid() {
        return Ssid;
    }

    public void setSsid(String ssid) {
        Ssid = ssid;
    }

    public String getSlaveMask() {
        return SlaveMask;
    }

    public void setSlaveMask(String slaveMask) {
        SlaveMask = slaveMask;
    }

    public String getCurrentVolume() {
        return CurrentVolume;
    }

    public void setCurrentVolume(String currentVolume) {
        CurrentVolume = currentVolume;
    }

    public String getCurrentMute() {
        return CurrentMute;
    }

    public void setCurrentMute(String currentMute) {
        CurrentMute = currentMute;
    }

    public String getCurrentChannel() {
        return CurrentChannel;
    }

    public void setCurrentChannel(String currentChannel) {
        CurrentChannel = currentChannel;
    }


    public SWDeviceStatus getSWDeviceStatus() {
        return SWDeviceStatus;
    }

    public void setSWDeviceStatus(SWDeviceStatus SWDeviceStatus) {
        this.SWDeviceStatus = SWDeviceStatus;
    }


    public List<SlaveBean.SlaveListDTO> getSlaveList() {
        return slaveList;
    }

    public void setSlaveList(List<SlaveBean.SlaveListDTO> slaveList) {
        this.slaveList.clear();
        this.slaveList.addAll(slaveList);
    }
}
