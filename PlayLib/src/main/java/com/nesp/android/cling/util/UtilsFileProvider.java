package com.nesp.android.cling.util;

import android.app.Application;

import androidx.core.content.FileProvider;


public class UtilsFileProvider extends FileProvider {
    public static Application application;

    @Override
    public boolean onCreate() {
        //noinspection ConstantConditions
        application = (Application) getContext().getApplicationContext();
        return true;
    }
}
