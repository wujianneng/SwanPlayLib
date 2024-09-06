package com.nesp.android.cling.entity;

import org.teleal.cling.model.action.ActionInvocation;
import org.teleal.cling.model.message.UpnpResponse;
import org.teleal.cling.support.model.TransportInfo;

import java.util.Map;

/**
 * 说明：获取传输体信息
 */

public class ClingGetControlDeviceInfoResponse extends BaseClingResponse<Map> implements IResponse<Map> {


    public ClingGetControlDeviceInfoResponse(ActionInvocation actionInvocation) {
        super(actionInvocation);
    }

    public ClingGetControlDeviceInfoResponse(ActionInvocation actionInvocation, UpnpResponse operation, String defaultMsg) {
        super(actionInvocation, operation, defaultMsg);
    }

    public ClingGetControlDeviceInfoResponse(ActionInvocation actionInvocation, Map info) {
        super(actionInvocation, info);
    }
}
