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

public abstract class PlayQueueWithIndex extends ActionCallback {
    public PlayQueueWithIndex(ActionInvocation actionInvocation, ControlPoint controlPoint) {
        super(actionInvocation, controlPoint);
    }

    public PlayQueueWithIndex(ActionInvocation actionInvocation) {
        super(actionInvocation);
    }

    public PlayQueueWithIndex(Service service) {
        this(new UnsignedIntegerFourBytes(0L), service, (String)null, (UnsignedIntegerFourBytes)null);
    }

    public PlayQueueWithIndex(Service service, String QueueName, UnsignedIntegerFourBytes ui4_Index) {
        this(new UnsignedIntegerFourBytes(0L), service, QueueName, ui4_Index);
    }

    public PlayQueueWithIndex(UnsignedIntegerFourBytes instanceId, Service service, String QueueName, UnsignedIntegerFourBytes ui4_Index) {
        super(new ActionInvocation(service.getAction("PlayQueueWithIndex")));
        this.getActionInvocation().setInput("QueueName", QueueName);
        this.getActionInvocation().setInput("Index", ui4_Index);
    }

    public void failure(ActionInvocation arg0, UpnpResponse arg1, String arg2) {
    }

    public void success(ActionInvocation arg0) {
    }
}
