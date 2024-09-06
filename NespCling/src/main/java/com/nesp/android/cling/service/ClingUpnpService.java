package com.nesp.android.cling.service;

import android.content.Intent;
import android.os.IBinder;

import org.teleal.cling.UpnpService;
import org.teleal.cling.UpnpServiceConfiguration;
import org.teleal.cling.android.AndroidUpnpService;
import org.teleal.cling.android.AndroidUpnpServiceImpl;
import org.teleal.cling.controlpoint.ControlPoint;
import org.teleal.cling.model.meta.LocalDevice;
import org.teleal.cling.registry.Registry;

/**
 * 说明：

 * 日期：17/6/28 16:11
 */

public class ClingUpnpService extends AndroidUpnpServiceImpl {
    private LocalDevice mLocalDevice = null;

    @Override
    public void onCreate() {
        super.onCreate();

        //LocalBinder instead of binder
        binder = new LocalBinder();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    public LocalDevice getLocalDevice() {
        return mLocalDevice;
    }

    public UpnpServiceConfiguration getConfiguration() {
        return upnpService.getConfiguration();
    }

    public Registry getRegistry() {
        return upnpService.getRegistry();
    }

    public ControlPoint getControlPoint() {
        return upnpService.getControlPoint();
    }

    public UpnpService getUpnpService(){
        return upnpService;
    }

    public class LocalBinder extends Binder implements AndroidUpnpService {
        public ClingUpnpService getService() {
            return ClingUpnpService.this;
        }

        public UpnpService getUpnpService() {
            return upnpService;
        }

        public UpnpServiceConfiguration getConfiguration() {
            return upnpService.getConfiguration();
        }

        public Registry getRegistry() {
            return upnpService.getRegistry();
        }

        public ControlPoint getControlPoint() {
            return upnpService.getControlPoint();
        }
    }
}
