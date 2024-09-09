package com.nesp.android.cling.entity;

//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//


public class LPPlayItem {
    private String trackName;
    private String trackId;
    private String trackUrl;
    private String trackImage;
    private String trackArtist;
    private String artistId;
    private int trackSize;
    private long trackDuration;
    private int playCount;
    private int itemType;
    private int stepType;
    private String albumName;
    private String albumId;
    private String createTime;

    public LPPlayItem() {
    }

    public void setTrackName(String var1) {
        this.trackName = var1;
    }

    public void setTrackId(String var1) {
        this.trackId = var1;
    }

    public void setTrackUrl(String var1) {
        this.trackUrl = var1;
    }

    public void setTrackImage(String var1) {
        this.trackImage = var1;
    }

    public void setTrackArtist(String var1) {
        this.trackArtist = var1;
    }

    public void setArtistId(String var1) {
        this.artistId = var1;
    }

    public void setTrackSize(int var1) {
        this.trackSize = var1;
    }

    public void setTrackDuration(long var1) {
        this.trackDuration = var1;
    }

    public void setPlayCount(int var1) {
        this.playCount = var1;
    }

    public void setAlbumName(String var1) {
        this.albumName = var1;
    }

    public void setAlbumId(String var1) {
        this.albumId = var1;
    }

    public void setCreateTime(String var1) {
        this.createTime = var1;
    }

    public String getTrackName() {
        return this.trackName;
    }

    public String getTrackId() {
        return this.trackId;
    }

    public String getTrackUrl() {
        return this.trackUrl;
    }

    public String getTrackImage() {
        return this.trackImage;
    }

    public String getTrackArtist() {
        return this.trackArtist;
    }

    public String getArtistId() {
        return this.artistId;
    }

    public int getTrackSize() {
        return this.trackSize;
    }

    public long getTrackDuration() {
        return this.trackDuration;
    }

    public int getPlayCount() {
        return this.playCount;
    }

    public String getAlbumName() {
        return this.albumName;
    }

    public String getAlbumId() {
        return this.albumId;
    }

    public String getCreateTime() {
        return this.createTime;
    }

    public void setItemType(int var1) {
        this.itemType = var1;
    }

    public int getItemType() {
        return this.itemType;
    }

    public void setStepType(int var1) {
        this.stepType = var1;
    }

    public int getStepType() {
        return this.stepType;
    }

    public static class ItemStepType {
        public static final int ITEM_STEP_TYPE_NULL = 0;
        public static final int ITEM_STEP_TYPE_MORE = 1;
        public static final int ITEM_STEP_TYPE_NEXT = 2;

        public ItemStepType() {
        }
    }
}
