package com.nesp.android.cling.entity;

public enum LPPlayQueueType {
    LP_ALL_QUEUE("TotalQueue"),
    LP_CURRENT_QUEUE("CurrentQueue"),
    LP_USBDISK_QUEUE("USBDiskQueue");
//    LP_RECENTLY_QUEUE(IWiimuPlayQueueType.ExtRecentlyQueue),
//    LP_CUSTOMLIST_QUEUE(IWiimuPlayQueueType.ExtCustomListQueue);

    private String value;

    LPPlayQueueType(String str) {
        this.value = "";
        this.value = str;
    }

    public String getValue() {
        return this.value;
    }
}
