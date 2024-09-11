package com.nesp.android.cling.service.manager;

import org.teleal.cling.UpnpService;
import org.teleal.cling.controlpoint.ControlPoint;
import org.teleal.cling.model.message.header.UpnpHeader;
import org.teleal.cling.protocol.SendingAsync;
import org.teleal.cling.protocol.async.SendingSearch;

import java.util.logging.Logger;

public class CustomSendingSearch extends SendingAsync {

    final private static Logger log = Logger.getLogger(SendingSearch.class.getName());

    private final String customAddress;
    private final int customPort;
    UpnpHeader searchTarget,manHearder,mxHearder,hostHearder;

    public static void searchDevices(ControlPoint controlPoint, CustomSendingSearch customSendingSearch){
        controlPoint.getConfiguration().getAsyncProtocolExecutor().execute(customSendingSearch);
    }


    public CustomSendingSearch(UpnpService upnpService,UpnpHeader searchTarget, UpnpHeader manHearder, UpnpHeader mxHearder,
                               UpnpHeader hostHearder, String host, int port) {
        super(upnpService);
        this.customAddress = host;
        this.customPort = port;
        this.searchTarget = searchTarget;
        this.manHearder = manHearder;
        this.mxHearder = mxHearder;
        this.hostHearder = hostHearder;
    }



    protected void execute(){


        CustomOutgoingSearchRequest msg = new CustomOutgoingSearchRequest(searchTarget,manHearder,mxHearder,hostHearder,customAddress,customPort);
        for (int i = 0; i < getBulkRepeat(); i++) {
            try {
                getUpnpService().getRouter().send(msg);
                // UDA 1.0 is silent about this but UDA 1.1 recommends "a few hundred milliseconds"
                log.finer("Sleeping " + getBulkIntervalMilliseconds() + " milliseconds");
                Thread.sleep(getBulkIntervalMilliseconds());
            } catch (Exception ex) {
                // Interruption means we stop sending search messages, e.g. on shutdown of thread pool
                break;
            }
        }
    }

    public int getBulkRepeat() {
        return 3; // UDA 1.0 says "repeat more than once"
    }

    public int getBulkIntervalMilliseconds() {
        return 300; // That should be plenty on an ethernet LAN
    }


}

