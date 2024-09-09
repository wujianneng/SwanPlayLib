package com.nesp.android.cling.service.manager;

import org.teleal.cling.model.ModelUtil;
import org.teleal.cling.model.message.OutgoingDatagramMessage;
import org.teleal.cling.model.message.UpnpRequest;
import org.teleal.cling.model.message.header.InvalidHeaderException;
import org.teleal.cling.model.message.header.UpnpHeader;

public class CustomOutgoingSearchRequest extends OutgoingDatagramMessage<UpnpRequest> {

    public CustomOutgoingSearchRequest(UpnpHeader searchTarget, UpnpHeader manHearder, UpnpHeader mxHearder, UpnpHeader hostHearder, String host, int port) {
        super(
                new UpnpRequest(UpnpRequest.Method.MSEARCH),
                ModelUtil.getInetAddressByName(host),
                port
        );

        getHeaders().add(UpnpHeader.Type.MAN, manHearder);
        getHeaders().add(UpnpHeader.Type.MX, mxHearder);
        getHeaders().add(UpnpHeader.Type.ST, searchTarget);
        getHeaders().add(UpnpHeader.Type.HOST, hostHearder);
        getHeaders().add(UpnpHeader.Type.MAX_AGE, new UpnpHeader<String>() {
            @Override
            public void setString(String s) throws InvalidHeaderException {

            }

            @Override
            public String getString() {
                return "120";
            }
        });
        getHeaders().add(UpnpHeader.Type.USER_AGENT, new UpnpHeader<String>() {
            @Override
            public void setString(String s) throws InvalidHeaderException {

            }

            @Override
            public String getString() {
                return "mac/1.0 UPnP/1.1 upnpx/1.0/1";
            }
        });
    }
}
