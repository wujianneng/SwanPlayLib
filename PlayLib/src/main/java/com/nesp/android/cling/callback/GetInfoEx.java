package com.nesp.android.cling.callback;

import android.util.Log;

import org.teleal.cling.controlpoint.ActionCallback;
import org.teleal.cling.controlpoint.ControlPoint;
import org.teleal.cling.model.action.ActionInvocation;
import org.teleal.cling.model.meta.Action;
import org.teleal.cling.model.meta.ActionArgument;
import org.teleal.cling.model.meta.Service;
import org.teleal.cling.model.types.UnsignedIntegerFourBytes;

import java.util.ArrayList;
import java.util.List;

public abstract class GetInfoEx extends ActionCallback {

    protected GetInfoEx(ActionInvocation actionInvocation, ControlPoint controlPoint) {
        super(actionInvocation, controlPoint);
    }

    protected GetInfoEx(ActionInvocation actionInvocation) {
        super(actionInvocation);
    }

    public GetInfoEx(Service service) {
        this(new UnsignedIntegerFourBytes(0L), service);
    }

    public GetInfoEx(UnsignedIntegerFourBytes instanceId, Service service) {

        super(new ActionInvocation(service.getAction("GetInfoEx")));
        this.getActionInvocation().setInput("InstanceID", instanceId);
    }


    public void success(ActionInvocation invocation) {
    }
}

