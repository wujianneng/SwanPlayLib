package com.nesp.android.cling.service.manager;


import com.nesp.android.cling.service.ClingUpnpService;
import org.teleal.cling.registry.Registry;

/**
 * 说明：

 * 日期：17/6/28 16:30
 */

public interface ISWManager extends IDLNAManager {

    void setUpnpService(ClingUpnpService upnpService);

    void setDeviceManager(IDeviceManager deviceManager);

    Registry getRegistry();
}
