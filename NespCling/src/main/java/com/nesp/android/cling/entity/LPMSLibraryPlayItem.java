package com.nesp.android.cling.entity;

//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//


public class LPMSLibraryPlayItem extends LPPlayItem {
    private String local_id = "";
    private String track = "0";
    private String genreName = "unknown";
    private String Size = "";
    private int fileId = 0;
    private String songlist = "";
    private String filePath = "";
    private String[] currDirArr;
    private int songCount = 0;
    private String folderName = "";

    public LPMSLibraryPlayItem() {
    }

    public String[] getCurrDirArr() {
        return this.currDirArr;
    }

    public void setCurrDirArr(String[] currDirArr) {
        this.currDirArr = currDirArr;
    }

    public String getLocal_id() {
        return this.local_id;
    }

    public void setLocal_id(String local_id) {
        this.local_id = local_id;
    }

    public String getTrack() {
        return this.track;
    }

    public void setTrack(String track) {
        this.track = track;
    }

    public String getGenreName() {
        return this.genreName;
    }

    public void setGenreName(String genreName) {
        this.genreName = genreName;
    }

    public String getSize() {
        return this.Size;
    }

    public void setSize(String size) {
        this.Size = size;
    }

    public int getFileId() {
        return this.fileId;
    }

    public void setFileId(int fileId) {
        this.fileId = fileId;
    }

    public String getSonglist() {
        return this.songlist;
    }

    public void setSonglist(String songlist) {
        this.songlist = songlist;
    }

    public String getFilePath() {
        return this.filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public int getSongCount() {
        return this.songCount;
    }

    public void setSongCount(int songCount) {
        this.songCount = songCount;
    }

    public String getFolderName() {
        return this.folderName;
    }

    public void setFolderName(String folderName) {
        this.folderName = folderName;
    }
}

