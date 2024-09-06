package com.nesp.android.cling.callback;

//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//


import org.teleal.cling.controlpoint.ActionCallback;
import org.teleal.cling.controlpoint.ControlPoint;
import org.teleal.cling.model.action.ActionInvocation;
import org.teleal.cling.model.message.UpnpResponse;
import org.teleal.cling.model.meta.Service;
import org.teleal.cling.model.types.UnsignedIntegerFourBytes;

public class AppendTracksInQueueEx extends ActionCallback {
    public AppendTracksInQueueEx(ActionInvocation actionInvocation, ControlPoint controlPoint) {
        super(actionInvocation, controlPoint);
    }

    public AppendTracksInQueueEx(ActionInvocation actionInvocation) {
        super(actionInvocation);
    }

    public AppendTracksInQueueEx(Service service) {
        this(new UnsignedIntegerFourBytes(0L), service);
    }

    public AppendTracksInQueueEx(UnsignedIntegerFourBytes instanceId, Service service) {
        this(instanceId, service, (String)null, 0, 0);
    }

    public AppendTracksInQueueEx(Service service, String QueueContext, int startIndex, int direction) {
        this(new UnsignedIntegerFourBytes(0L), service, QueueContext, startIndex, direction);
    }

    public AppendTracksInQueueEx(UnsignedIntegerFourBytes instanceId, Service service, String QueueContext, int startIndex, int direction) {
        super(new ActionInvocation(service.getAction("AppendTracksInQueueEx")));
        this.getActionInvocation().setInput("QueueContext", QueueContext);
        ActionInvocation var10001 = this.getActionInvocation();
        UnsignedIntegerFourBytes var6;
        var6 = new UnsignedIntegerFourBytes((long)direction);
        var10001.setInput("Direction", var6);
        ActionInvocation var7 = getActionInvocation();
        var6 = new UnsignedIntegerFourBytes((long)startIndex);
        var7.setInput("StartIndex", var6);
    }

    public void failure(ActionInvocation arg0, UpnpResponse arg1, String arg2) {
    }

    public void success(ActionInvocation arg0) {
    }
}
