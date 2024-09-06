package com.nesp.android.cling.entity;


import com.google.gson.annotations.SerializedName;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/* loaded from: classes2.dex */
public class LPPlayInfoItem implements Serializable {

    @SerializedName("queueName")
    private String playlistName = "";

    @SerializedName("playIndex")
    private int playIndex = -1;

    @SerializedName("appendCount")
    private int appendCount = 0;

    @SerializedName("playData")
    List<String> playDataList = new ArrayList();

    @SerializedName("mediaSource")
    private String mediaSource = "";

    public int getAppendCount() {
        return this.appendCount;
    }

    public String getMediaSource() {
        return this.mediaSource;
    }

    public List<String> getPlayDataList() {
        return this.playDataList;
    }

    public int getPlayIndex() {
        return this.playIndex;
    }

    public String getPlaylistName() {
        return this.playlistName;
    }

    public void setAppendCount(int i) {
        this.appendCount = i;
    }

    public void setMediaSource(String str) {
        this.mediaSource = str;
    }

    public void setPlayDataList(List<String> list) {
        this.playDataList = list;
    }

    public void setPlayIndex(int i) {
        this.playIndex = i;
    }

    public void setPlaylistName(String str) {
        this.playlistName = str;
    }
}

