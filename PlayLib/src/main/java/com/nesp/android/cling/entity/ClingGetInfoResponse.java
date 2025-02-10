package com.nesp.android.cling.entity;

import org.teleal.cling.model.action.ActionInvocation;
import org.teleal.cling.model.message.UpnpResponse;
import org.teleal.cling.support.model.PositionInfo;

import java.util.Map;

/**
 * 说明：获取播放进度回调结果

 * 日期：17/7/19 12:26
 */

public class ClingGetInfoResponse extends BaseClingResponse<PlayStatusBean> implements IResponse<PlayStatusBean> {


    public ClingGetInfoResponse(ActionInvocation actionInvocation) {
        super(actionInvocation);
    }

    public ClingGetInfoResponse(ActionInvocation actionInvocation, UpnpResponse operation, String defaultMsg) {
        super(actionInvocation, operation, defaultMsg);
    }

    public ClingGetInfoResponse(ActionInvocation actionInvocation, PlayStatusBean info) {
        super(actionInvocation, info);
    }
}
