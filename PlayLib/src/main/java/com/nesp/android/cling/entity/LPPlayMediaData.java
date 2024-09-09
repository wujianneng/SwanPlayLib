package com.nesp.android.cling.entity;

//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//


import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class LPPlayMediaData implements Serializable {
    private int playIndex;
    private String queueName;
    private String mediaSource;
    private int appendCount;
    private List<String> playData;

    public LPPlayMediaData() {
        LPPlayMediaData var10000 = this;
        this.queueName = "";
        ArrayList var1;
        var1 = new ArrayList();
        var10000.playData = var1;
    }

    public String getQueueName() {
        return this.queueName;
    }

    public void setQueueName(String var1) {
        this.queueName = var1;
    }

    public String getMediaSource() {
        return this.mediaSource;
    }

    public void setMediaSource(String var1) {
        this.mediaSource = var1;
    }

    public int getPlayIndex() {
        return this.playIndex;
    }

    public void setPlayIndex(int var1) {
        this.playIndex = var1;
    }

    public int getAppendCount() {
        return this.appendCount;
    }

    public void setAppendCount(int var1) {
        this.appendCount = var1;
    }

    public List<String> getPlayData() {
        return this.playData;
    }

    public void setPlayData(List<String> var1) {
        this.playData = var1;
    }
}
