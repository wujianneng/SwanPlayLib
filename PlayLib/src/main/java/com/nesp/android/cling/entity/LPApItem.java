package com.nesp.android.cling.entity;

import com.google.gson.annotations.SerializedName;

public class LPApItem {
    @SerializedName("ssid")
    public String SSID;
    @SerializedName("bssid")
    public String BSSID;
    @SerializedName("rssi")
    public int RSSI;
    @SerializedName("channel")
    public int Channel;
    @SerializedName("auth")
    public String Auth;
    @SerializedName("encry")
    public String Encry;
    @SerializedName("extch")
    public int Extch;

    public LPApItem() {
    }

    public String toString() {
        return "LPApItem{SSID='" + this.SSID + '\'' + ", BSSID='" + this.BSSID + '\'' + ", RSSI=" + this.RSSI + ", Channel=" + this.Channel + ", Auth='" + this.Auth + '\'' + ", Encry='" + this.Encry + '\'' + ", Extch=" + this.Extch + '}';
    }
}
