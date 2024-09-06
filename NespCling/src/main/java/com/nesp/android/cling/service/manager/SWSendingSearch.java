package com.nesp.android.cling.service.manager;

import org.teleal.cling.UpnpService;
import org.teleal.cling.controlpoint.ControlPoint;
import org.teleal.cling.model.message.header.MXHeader;
import org.teleal.cling.model.message.header.STAllHeader;
import org.teleal.cling.model.message.header.UpnpHeader;
import org.teleal.cling.protocol.SendingAsync;
import org.teleal.cling.protocol.async.SendingSearch;

import java.util.logging.Logger;

public class SWSendingSearch extends SendingAsync {
    private static final Logger log = Logger.getLogger(SendingSearch.class.getName());
    private final UpnpHeader searchTarget;
    private final int mxSeconds;

    public static void searchDevices(ControlPoint controlPoint, SWSendingSearch swSendingSearch){
        controlPoint.getConfiguration().getAsyncProtocolExecutor().execute(swSendingSearch);
    }

    public SWSendingSearch(UpnpService upnpService) {
        this(upnpService, new STAllHeader());
    }

    public SWSendingSearch(UpnpService upnpService, UpnpHeader searchTarget) {
        this(upnpService, searchTarget, MXHeader.DEFAULT_VALUE);
    }

    public SWSendingSearch(UpnpService upnpService, UpnpHeader searchTarget, int mxSeconds) {
        super(upnpService);
        this.searchTarget = searchTarget;
        this.mxSeconds = mxSeconds;
    }

    public UpnpHeader getSearchTarget() {
        return this.searchTarget;
    }

    public int getMxSeconds() {
        return this.mxSeconds;
    }

    protected void execute() {
        log.fine("Executing search for target: " + this.searchTarget.getString() + " with MX seconds: " + this.getMxSeconds());
        OutgoingSearchRequest1 var1 = new OutgoingSearchRequest1(this.searchTarget, this.getMxSeconds());
        OutgoingSearchRequest2 var7 = new OutgoingSearchRequest2(this.searchTarget, this.getMxSeconds());
        OutgoingSearchRequestMDNS var8 = new OutgoingSearchRequestMDNS(this.searchTarget, this.getMxSeconds());
        for(int var9 = 0; var9 < this.getBulkRepeat(); ++var9) {
            try {
                this.getUpnpService().getRouter().send(var1);
                log.finer("Sleeping " + this.getBulkIntervalMilliseconds() + " milliseconds");
                this.getUpnpService().getRouter().send(var7);
                this.getUpnpService().getRouter().send(var8);
            } catch (Exception var6) {
                log.warning("Search sending thread was interrupted: " + var6);
            }
        }

    }

    public int getBulkRepeat() {
        return 5;
    }

    public int getBulkIntervalMilliseconds() {
        return 500;
    }
}
