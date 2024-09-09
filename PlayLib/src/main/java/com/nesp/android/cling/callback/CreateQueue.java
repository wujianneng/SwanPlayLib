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

public abstract class CreateQueue extends ActionCallback {
    public CreateQueue(ActionInvocation actionInvocation, ControlPoint controlPoint) {
        super(actionInvocation, controlPoint);
    }

    public CreateQueue(ActionInvocation actionInvocation) {
        super(actionInvocation);
    }

    public CreateQueue(Service service) {
        this(new UnsignedIntegerFourBytes(0L), service);
    }

    public CreateQueue(UnsignedIntegerFourBytes instanceId, Service service) {
        this(instanceId, service, (String) null);
    }

    public CreateQueue(Service service, String QueueContext) {
        this(new UnsignedIntegerFourBytes(0L), service, QueueContext);
    }

    public CreateQueue(UnsignedIntegerFourBytes instanceId, Service service, String QueueContext) {
        super(new ActionInvocation(service.getAction("CreateQueue")));
        this.getActionInvocation().setInput("QueueContext", QueueContext);
    }

    public void failure(ActionInvocation arg0, UpnpResponse arg1, String arg2) {
    }

    public void success(ActionInvocation arg0) {
    }
}
