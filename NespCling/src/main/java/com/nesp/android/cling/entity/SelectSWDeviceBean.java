package com.nesp.android.cling.entity;

public class SelectSWDeviceBean {
    boolean isSelected = false;
    boolean isSlaveDevice = false;
    String lpDeviceName;
    String ssid;
    String lpDeviceIp;
    String hardware;

    public SelectSWDeviceBean(String ssid,String lpDeviceName, String lpDeviceIp,String hardware,boolean isSelected,boolean isSlaveDevice) {
        this.ssid = ssid;
        this.lpDeviceName = lpDeviceName;
        this.lpDeviceIp = lpDeviceIp;
        this.isSlaveDevice = isSlaveDevice;
        this.isSelected = isSelected;
        this.hardware = hardware;
    }

    public String getSsid() {
        return ssid;
    }

    public void setSsid(String ssid) {
        this.ssid = ssid;
    }

    public String getHardware() {
        return hardware;
    }

    public void setHardware(String firmware) {
        this.hardware = firmware;
    }

    public boolean isSlaveDevice() {
        return isSlaveDevice;
    }

    public void setSlaveDevice(boolean slaveDevice) {
        isSlaveDevice = slaveDevice;
    }

    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean selected) {
        isSelected = selected;
    }

    public String getLpDeviceName() {
        return lpDeviceName;
    }

    public void setLpDeviceName(String lpDeviceName) {
        this.lpDeviceName = lpDeviceName;
    }

    public String getLpDeviceIp() {
        return lpDeviceIp;
    }

    public void setLpDeviceIp(String lpDeviceIp) {
        this.lpDeviceIp = lpDeviceIp;
    }
}

