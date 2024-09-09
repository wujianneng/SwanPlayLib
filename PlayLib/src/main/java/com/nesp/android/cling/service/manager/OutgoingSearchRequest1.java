package com.nesp.android.cling.service.manager;

import org.teleal.cling.model.ModelUtil;
import org.teleal.cling.model.message.OutgoingDatagramMessage;
import org.teleal.cling.model.message.UpnpRequest;
import org.teleal.cling.model.message.header.HostHeader;
import org.teleal.cling.model.message.header.MANHeader;
import org.teleal.cling.model.message.header.MXHeader;
import org.teleal.cling.model.message.header.UpnpHeader;
import org.teleal.cling.model.types.NotificationSubtype;

public class OutgoingSearchRequest1 extends OutgoingDatagramMessage<UpnpRequest> {
    private UpnpHeader searchTarget;

    public OutgoingSearchRequest1(UpnpHeader searchTarget, int mxSeconds) {
        super(new UpnpRequest(UpnpRequest.Method.MSEARCH), ModelUtil.getInetAddressByName("239.255.255.250"), 1900);
        this.searchTarget = searchTarget;
        this.getHeaders().add(UpnpHeader.Type.MAN, new MANHeader(NotificationSubtype.DISCOVER.getHeaderString()));
        this.getHeaders().add(UpnpHeader.Type.MX, new MXHeader(mxSeconds));
        this.getHeaders().add(UpnpHeader.Type.ST, searchTarget);
        this.getHeaders().add(UpnpHeader.Type.HOST, new HostHeader());
    }

    public UpnpHeader getSearchTarget() {
        return this.searchTarget;
    }
}