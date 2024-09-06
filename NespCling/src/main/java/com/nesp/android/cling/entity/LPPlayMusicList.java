package com.nesp.android.cling.entity;

//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//


import android.util.Log;
import java.util.List;

public class LPPlayMusicList {
    private static final String TAG = "LPPlayMusicList";
    public LPPlayHeader header;
    public List<LPPlayItem> list;
    public int index;

    public LPPlayMusicList() {
    }

    public LPPlayMusicList(int var1) {
        this.index = var1;
    }


    public void setHeader(LPPlayHeader var1) {
        this.header = var1;
    }

    public void setList(List<LPPlayItem> var1) {
        try {
            this.list = var1;
        } catch (Exception var2) {
            var2.printStackTrace();
            Log.e("LPPlayMusicList", "setList error = " + var2.getMessage());
        }

    }

    public void setIndex(int var1) {
        this.index = var1;
    }

    public LPPlayHeader getHeader() {
        return this.header;
    }

    public List<LPPlayItem> getList() {
        return this.list;
    }

    public int getIndex() {
        return this.index;
    }
}
