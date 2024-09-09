package com.nesp.android.cling.listener;

import java.util.Map;

public interface IWiimuActionCallback {
    void success(Map var1);

    void failure(Exception var1);
}