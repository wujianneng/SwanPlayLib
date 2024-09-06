package com.nesp.android.cling.service.manager;

import org.teleal.cling.model.message.header.InvalidHeaderException;
import org.teleal.cling.model.message.header.UpnpHeader;

public class STCustomHeader extends UpnpHeader<String> {
    private String notifyType = "";

    public STCustomHeader(String notifyType) {
        this.notifyType = notifyType;
        this.setValue(notifyType);
    }

    public void setString(String s) throws InvalidHeaderException {
        if (!s.equals(this.notifyType)) {
            throw new InvalidHeaderException("Invalid ST header value (not " + this.notifyType + "): " + s);
        }
    }

    public String getString() {
        return this.notifyType;
    }
}
