package com.nesp.android.cling.entity;

import org.teleal.cling.model.action.ActionInvocation;
import org.teleal.cling.model.message.UpnpResponse;

import java.util.Map;

public class ClingPlaylistResponse extends BaseClingResponse<PlayList> {


    public ClingPlaylistResponse(ActionInvocation actionInvocation) {
        super(actionInvocation);
    }

    public ClingPlaylistResponse(ActionInvocation actionInvocation, UpnpResponse operation, String defaultMsg) {
        super(actionInvocation, operation, defaultMsg);
    }

    public ClingPlaylistResponse(ActionInvocation actionInvocation, PlayList info) {
        super(actionInvocation, info);
    }
}