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

public abstract class AppendTracksInQueue extends ActionCallback {
    public AppendTracksInQueue(ActionInvocation actionInvocation, ControlPoint controlPoint) {
        super(actionInvocation, controlPoint);
    }

    public AppendTracksInQueue(ActionInvocation actionInvocation) {
        super(actionInvocation);
    }

    public AppendTracksInQueue(Service service) {
        this(new UnsignedIntegerFourBytes(0L), service);
    }

    public AppendTracksInQueue(UnsignedIntegerFourBytes instanceId, Service service) {
        this(instanceId, service, (String) null);
    }

    public AppendTracksInQueue(Service service, String QueueContext) {
        this(new UnsignedIntegerFourBytes(0L), service, QueueContext);
    }

    public AppendTracksInQueue(UnsignedIntegerFourBytes instanceId, Service service, String QueueContext) {
        super(new ActionInvocation(service.getAction("AppendTracksInQueue")));
        this.getActionInvocation().setInput("QueueContext", QueueContext);
    }

    public void failure(ActionInvocation arg0, UpnpResponse arg1, String arg2) {
    }

    public void success(ActionInvocation arg0) {
    }
}
