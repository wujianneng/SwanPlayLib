package com.nesp.android.cling.callback;


import org.teleal.cling.controlpoint.ActionCallback;
import org.teleal.cling.model.action.ActionInvocation;
import org.teleal.cling.model.meta.Service;
import org.teleal.cling.model.types.UnsignedIntegerFourBytes;

public abstract class SetQueueLoopMode extends ActionCallback {
    protected SetQueueLoopMode(ActionInvocation actionInvocation) {
        super(actionInvocation);
    }

    public SetQueueLoopMode(Service service, UnsignedIntegerFourBytes ui4_LoopMode) {
        super(new ActionInvocation(service.getAction("SetQueueLoopMode")));
        this.getActionInvocation().setInput("LoopMode", ui4_LoopMode);
    }

    public SetQueueLoopMode(Service service, int LoopMode) {
        this(service, new UnsignedIntegerFourBytes((long)LoopMode));
    }
}
