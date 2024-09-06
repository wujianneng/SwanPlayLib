package com.nesp.android.cling.callback;


import org.teleal.cling.controlpoint.ActionCallback;
import org.teleal.cling.controlpoint.ControlPoint;
import org.teleal.cling.model.action.ActionInvocation;
import org.teleal.cling.model.meta.Service;

public abstract class BrowseQueue extends ActionCallback {
    protected String QueueName;

    public BrowseQueue(ActionInvocation actionInvocation, ControlPoint controlPoint) {
        super(actionInvocation, controlPoint);
    }

    public BrowseQueue(ActionInvocation actionInvocation) {
        super(actionInvocation);
    }

    public BrowseQueue(Service service, String QueueName) {
        this(new ActionInvocation(service.getAction("BrowseQueue")));
        this.getActionInvocation().setInput("QueueName", QueueName);
        this.QueueName = QueueName;
    }

    public abstract void received(String var1, Object var2);

    public void success(ActionInvocation param1) {
        // $FF: Couldn't be decompiled
    }

    public interface BrowseQueueType {
        String TotalQueue = "TotalQueue";
        String CurrentQueue = "CurrentQueue";
        String USBDiskQueue = "USBDiskQueue";
        String PandoraQueue = "Pandora";
        String DoubanQueue = "Douban";
    }
}
