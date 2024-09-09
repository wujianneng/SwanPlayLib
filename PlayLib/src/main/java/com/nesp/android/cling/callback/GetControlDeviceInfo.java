package com.nesp.android.cling.callback;

import com.linkplay.log.LinkplayLog;
import java.util.logging.Logger;
import org.teleal.cling.controlpoint.ActionCallback;
import org.teleal.cling.model.action.ActionInvocation;
import org.teleal.cling.model.message.UpnpResponse;
import org.teleal.cling.model.meta.Service;
import org.teleal.cling.model.types.UnsignedIntegerFourBytes;
import org.teleal.cling.protocol.UpnpUDNManager;

public abstract class GetControlDeviceInfo extends ActionCallback {
    private static Logger log = Logger.getLogger(org.teleal.cling.support.renderingcontrol.callback.GetControlDeviceInfo.class.getName());
    private String udn;

    private GetControlDeviceInfo(Service service) {
        this(new UnsignedIntegerFourBytes(0L), service);
    }

    public GetControlDeviceInfo(Service service, String udn) {
        this(new UnsignedIntegerFourBytes(0L), service);
        this.udn = udn;
    }

    public GetControlDeviceInfo(UnsignedIntegerFourBytes instanceId, Service service) {
        super(new ActionInvocation(service.getAction("GetControlDeviceInfo")));
        this.udn = "";
        this.getActionInvocation().setInput("InstanceID", instanceId);
    }

    protected void failure(ActionInvocation invocation, UpnpResponse operation) {
        super.failure(invocation, operation);
        String invocation1 = "UPNP-SEARCH";
        StringBuilder var3 = (new StringBuilder()).append("GetControlDeviceInfo failed: ").append(this.udn.toString());
        String operation1;
        if (operation == null) {
            operation1 = "";
        } else {
            operation1 = ", " + operation.getResponseDetails() + ", " + operation.getStatusMessage();
        }

        LinkplayLog.i(invocation1, var3.append(operation1).toString());
        UpnpUDNManager.getInstance().removeUDN(this.udn.toString());
    }
}
