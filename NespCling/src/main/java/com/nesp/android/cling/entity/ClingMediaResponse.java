package com.nesp.android.cling.entity;

import org.teleal.cling.model.action.ActionInvocation;
import org.teleal.cling.model.message.UpnpResponse;
import org.teleal.cling.support.model.MediaInfo;
import org.teleal.cling.support.model.PositionInfo;

/**
 * 说明：获取媒体信息
 */

public class ClingMediaResponse extends BaseClingResponse<MediaInfo> implements IResponse<MediaInfo> {


    public ClingMediaResponse(ActionInvocation actionInvocation) {
        super(actionInvocation);
    }

    public ClingMediaResponse(ActionInvocation actionInvocation, UpnpResponse operation, String defaultMsg) {
        super(actionInvocation, operation, defaultMsg);
    }

    public ClingMediaResponse(ActionInvocation actionInvocation, MediaInfo info) {
        super(actionInvocation, info);
    }
}
