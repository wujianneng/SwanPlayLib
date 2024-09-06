package com.nesp.android.cling.callback;

import org.teleal.cling.controlpoint.ActionCallback;
import org.teleal.cling.controlpoint.ControlPoint;
import org.teleal.cling.model.action.ActionInvocation;
import org.teleal.cling.model.message.UpnpResponse;
import org.teleal.cling.model.meta.Service;
import org.teleal.cling.model.types.UnsignedIntegerFourBytes;

public class RemoveTracksInQueue extends ActionCallback {
    public RemoveTracksInQueue(ActionInvocation actionInvocation, ControlPoint controlPoint) {
        super(actionInvocation, controlPoint);
    }

    public RemoveTracksInQueue(ActionInvocation actionInvocation) {
        super(actionInvocation);
    }

    public RemoveTracksInQueue(Service service) {
        this(new UnsignedIntegerFourBytes(0L), service);
    }

    public RemoveTracksInQueue(UnsignedIntegerFourBytes instanceId, Service service) {
        this(instanceId, service, (String)null, 0, 0);
    }

    public RemoveTracksInQueue(Service service, String QueueContext, int startIndex, int endIndex) {
        this(new UnsignedIntegerFourBytes(0L), service, QueueContext, startIndex, endIndex);
    }

    public RemoveTracksInQueue(UnsignedIntegerFourBytes instanceId, Service service, String QueueName, int startIndex, int endIndex) {
        super(new ActionInvocation(service.getAction("RemoveTracksInQueue")));
        this.getActionInvocation().setInput("QueueName", QueueName);
        ActionInvocation var10001 = this.getActionInvocation();
        UnsignedIntegerFourBytes var6;
        var6 = new UnsignedIntegerFourBytes((long)startIndex);
        var10001.setInput("RangStart", var6);
        ActionInvocation var7 = getActionInvocation();
        var6 = new UnsignedIntegerFourBytes((long)endIndex);
        var7.setInput("RangEnd", var6);
    }

    public void failure(ActionInvocation arg0, UpnpResponse arg1, String arg2) {
    }

    public void success(ActionInvocation arg0) {
    }
}
