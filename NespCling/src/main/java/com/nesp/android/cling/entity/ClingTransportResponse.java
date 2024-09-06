package com.nesp.android.cling.entity;

import org.teleal.cling.model.action.ActionInvocation;
import org.teleal.cling.model.message.UpnpResponse;
import org.teleal.cling.support.model.MediaInfo;
import org.teleal.cling.support.model.TransportInfo;

/**
 * 说明：获取传输体信息
 */

public class ClingTransportResponse extends BaseClingResponse<TransportInfo> implements IResponse<TransportInfo> {


    public ClingTransportResponse(ActionInvocation actionInvocation) {
        super(actionInvocation);
    }

    public ClingTransportResponse(ActionInvocation actionInvocation, UpnpResponse operation, String defaultMsg) {
        super(actionInvocation, operation, defaultMsg);
    }

    public ClingTransportResponse(ActionInvocation actionInvocation, TransportInfo info) {
        super(actionInvocation, info);
    }
}
