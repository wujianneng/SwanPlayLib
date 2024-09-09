package com.nesp.android.cling.entity;

import org.teleal.cling.model.action.ActionInvocation;
import org.teleal.cling.model.message.UpnpResponse;
import org.teleal.cling.support.model.PositionInfo;

/**
 * 说明：获取播放进度回调结果

 * 日期：17/7/19 12:26
 */

public class ClingPositionResponse extends BaseClingResponse<PositionInfo> implements IResponse<PositionInfo> {


    public ClingPositionResponse(ActionInvocation actionInvocation) {
        super(actionInvocation);
    }

    public ClingPositionResponse(ActionInvocation actionInvocation, UpnpResponse operation, String defaultMsg) {
        super(actionInvocation, operation, defaultMsg);
    }

    public ClingPositionResponse(ActionInvocation actionInvocation, PositionInfo info) {
        super(actionInvocation, info);
    }
}
