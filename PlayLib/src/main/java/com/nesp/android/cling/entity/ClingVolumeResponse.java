package com.nesp.android.cling.entity;

import org.teleal.cling.model.action.ActionInvocation;
import org.teleal.cling.model.message.UpnpResponse;

/**
 * 说明：

 * 日期：17/7/19 16:22
 */

public class ClingVolumeResponse extends BaseClingResponse<Integer> {


    public ClingVolumeResponse(ActionInvocation actionInvocation, UpnpResponse operation, String defaultMsg) {
        super(actionInvocation, operation, defaultMsg);
    }

    public ClingVolumeResponse(ActionInvocation actionInvocation, Integer info) {
        super(actionInvocation, info);
    }
}
