package com.nesp.android.cling.callback;


import org.teleal.cling.controlpoint.ActionCallback;
import org.teleal.cling.model.action.ActionInvocation;
import org.teleal.cling.model.meta.Service;
import org.teleal.cling.model.types.UnsignedIntegerFourBytes;

public abstract class GetQueueLoopMode extends ActionCallback {
    protected GetQueueLoopMode(ActionInvocation actionInvocation) {
        super(actionInvocation);
    }

    public GetQueueLoopMode(UnsignedIntegerFourBytes instanceId, Service service) {
        super(new ActionInvocation(service.getAction("GetQueueLoopMode")));
    }

    public GetQueueLoopMode(Service service) {
        this(new UnsignedIntegerFourBytes(0L), service);
    }
}