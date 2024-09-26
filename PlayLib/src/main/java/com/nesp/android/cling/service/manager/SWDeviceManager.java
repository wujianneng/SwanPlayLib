package com.nesp.android.cling.service.manager;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;

import com.google.gson.Gson;
import com.linkplay.bonjour.model.LinkplayConstants;
import com.linkplay.core.app.LPDeviceManager;
import com.linkplay.core.app.LPDeviceManagerParam;
import com.linkplay.core.clingx.LPSearchControlPoint;
import com.linkplay.lpmdpkit.callback.LPPrintLogCallback;
import com.linkplay.lpmdpkit.utils.LPLogUtil;
import com.nesp.android.cling.WifiChangedCast;
import com.nesp.android.cling.control.SWPlayControl;
import com.nesp.android.cling.control.callback.ControlCallback;
import com.nesp.android.cling.control.callback.ControlReceiveCallback;
import com.nesp.android.cling.entity.ClingControlPoint;
import com.nesp.android.cling.entity.ClingGetControlDeviceInfoResponse;
import com.nesp.android.cling.entity.ClingMediaResponse;
import com.nesp.android.cling.entity.ClingPositionResponse;
import com.nesp.android.cling.entity.DeviceInfoBean;
import com.nesp.android.cling.entity.IControlPoint;
import com.nesp.android.cling.entity.IResponse;
import com.nesp.android.cling.entity.LPMediaInfo;
import com.nesp.android.cling.entity.MusicDataBean;
import com.nesp.android.cling.entity.PlayStatusBean;
import com.nesp.android.cling.entity.SWDevice;
import com.nesp.android.cling.entity.SWDeviceInfo;
import com.nesp.android.cling.entity.SWDeviceStatus;
import com.nesp.android.cling.entity.SlaveBean;
import com.nesp.android.cling.listener.BrowseRegistryListener;
import com.nesp.android.cling.listener.DeviceListChangedListener;
import com.nesp.android.cling.service.ClingUpnpService;
import com.nesp.android.cling.util.LogUtils;
import com.nesp.android.cling.util.SWDeviceUtils;
import com.nesp.android.cling.util.Utils;

import org.greenrobot.eventbus.EventBus;
import org.teleal.cling.model.action.ActionArgumentValue;
import org.teleal.cling.model.message.header.InvalidHeaderException;
import org.teleal.cling.model.message.header.UpnpHeader;
import org.teleal.cling.model.meta.Device;
import org.teleal.cling.model.meta.RemoteDevice;
import org.teleal.cling.model.types.DeviceType;
import org.teleal.cling.model.types.ServiceType;
import org.teleal.cling.model.types.UDADeviceType;
import org.teleal.cling.model.types.UDAServiceType;
import org.teleal.cling.model.types.UDN;
import org.teleal.cling.registry.Registry;
import org.teleal.cling.support.model.MediaInfo;
import org.teleal.cling.support.model.PositionInfo;

import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.ConcurrentModificationException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

/**
 * 说明：所有对服务的操作都通过该类代理执行
 * <p>
 */

public class SWDeviceManager implements ISWManager {

    //    public static final ServiceType CONTENT_DIRECTORY_SERVICE = new UDAServiceType("ContentDirectory");
    public static final ServiceType AV_TRANSPORT_SERVICE = new UDAServiceType("AVTransport");
    /**
     * 控制服务
     */
    public static final ServiceType RENDERING_CONTROL_SERVICE = new UDAServiceType("RenderingControl");
    public static final ServiceType MEDIA_RENDERER_SERVICE = new UDAServiceType("MediaRenderer");
    public static final ServiceType MEDIA_SERVER_SERVICE = new UDAServiceType("MediaServer");
    public static final ServiceType CONNECTION_MANAGER_SERVICE = new UDAServiceType("ConnectionManager");
    public static final ServiceType CONTENT_DIRECTORY_SERVICE = new UDAServiceType("ContentDirectory");

    public static final DeviceType DMR_DEVICE_TYPE = new UDADeviceType("MediaRenderer");
    /**
     * eventbus控制ui更新主设备列表ui的key
     */
    public final static String REFRESH_LIST_UI_KEY = "refresh_list_ui_key";
    public final static String HIDE_ASK_CONNECT_DIALOG = "hideAskConnectDialog";
    private static SWDeviceManager INSTANCE = null;

    private ClingUpnpService mUpnpService;
    private IDeviceManager mDeviceManager;

    //    private SystemService mSystemService;
    private Timer positionTimer, infoTimer;
    private List<SWDevice> masterDevices = new ArrayList<>();//主设备集合
    public List<SWDevice> offLineDeviceList = new ArrayList<>();
    private boolean isPauseAllTask = false;

    public void pauseAllTask(boolean isPauseAllTask) {
        this.isPauseAllTask = isPauseAllTask;
        if (!isPauseAllTask) {
            runOhterInfoExTask();
        }
    }

    TimerTask taskGetPositionInfoEx, taskOhterInfoEx;


    private SWPlayControl mSWPlayControl = new SWPlayControl();

    /**
     * 获取选中设备的媒体信息
     */
    public void getMediaInfo() {
        LogUtils.e("test", "getPositionInfodostart");
        SWDevice swDevice = getSelectedDevice();
        mSWPlayControl.getMediaInfo(new ControlReceiveCallback() {
            @Override
            public void receive(IResponse response) {
                ClingMediaResponse mediaResponse = (ClingMediaResponse) response;
                MediaInfo mediaInfo = mediaResponse.getResponse();
                LogUtils.e("test", "getPositionInfodoreceive:" + mediaInfo.getNumberOfTracks());
                if (mediaInfoTask != null && swDevice != null)
                    mediaInfoTask.work(mediaInfo, swDevice.getUuid());
            }

            @Override
            public void success(IResponse response) {
                LogUtils.e("test", "getPositionInfodosuccess");
            }

            @Override
            public void fail(IResponse response) {
                LogUtils.e("test", "getPositionInfodofail");
                if (mediaInfoTask != null)
                    mediaInfoTask.work(null, swDevice.getUuid());
            }
        });
    }

    public interface WorkPlayStatusTask {
        void work(SWDevice selectedSWDevice);
    }

    public interface WorkMediaInfoTask {
        void work(MediaInfo positionInfo, String fromUuid);
    }

    public interface WorkDeviceInfoTask {
        void work();
    }

    WorkPlayStatusTask playStatusTask;
    WorkMediaInfoTask mediaInfoTask;
    WorkDeviceInfoTask deviceInfoTask;

    public void setGetInfoTask(WorkPlayStatusTask playStatusTask, WorkMediaInfoTask mediaInfoTask, WorkDeviceInfoTask deviceInfoTask) {
        this.playStatusTask = playStatusTask;
        this.mediaInfoTask = mediaInfoTask;
        this.deviceInfoTask = deviceInfoTask;
    }

    /**
     * 删除重复添加的主设备
     */
    public void removeDuplicationDevices() {
        Map<String, Integer> mutrimap = new HashMap<>();
        for (SWDevice swDevice : SWDeviceManager.getInstance().getMasterDeviceList()) {
            if (swDevice == null) continue;
            if (mutrimap.containsKey(swDevice.getUuid())) {
                mutrimap.put(swDevice.getUuid(), 1 + mutrimap.get(swDevice.getUuid()));
            } else {
                mutrimap.put(swDevice.getUuid(), 1);
            }
        }
        List<SWDevice> mutriDevices = new ArrayList<>();
        for (SWDevice swDevice : SWDeviceManager.getInstance().getMasterDeviceList()) {
            if (swDevice != null && mutrimap.containsKey(swDevice.getUuid())) {
                int count = mutrimap.get(swDevice.getUuid());
                if (count > 1) {
                    mutriDevices.add(swDevice);
                    mutrimap.put(swDevice.getUuid(), count - 1);
                }
            }
        }
        removeSomeMasterDevices(mutriDevices);
    }

    public void removeSomeMasterDevices(List<SWDevice> swDeviceList) {
//        if(false){
        if(swDeviceList.size() == 0) return;
        LogUtils.e("removeSomeMasterDevices");
        if (mActivity != null && onRemoveMasterDeviceListener != null) {
            mActivity.runOnUiThread(() -> {
                LogUtils.e("removeSomeMasterDevices2");
                masterDevices.removeAll(swDeviceList);
                onRemoveMasterDeviceListener.onRemoveMasterDevice(swDeviceList);
                offLineDeviceList.clear();
            });
        } else {
            masterDevices.removeAll(swDeviceList);
            EventBus.getDefault().post(REFRESH_LIST_UI_KEY);
            offLineDeviceList.clear();
        }
    }

    public void addOneMasterDevice(SWDevice swDevice) {
//        if(false){
        if(swDevice == null) return;
        if (mActivity != null && onAddOneMasterDeviceListener != null) {
            mActivity.runOnUiThread(() -> {
                masterDevices.add(swDevice);
                onAddOneMasterDeviceListener.onAddOneMasterDevice(swDevice);
            });
        } else {
            masterDevices.add(swDevice);
            EventBus.getDefault().post(REFRESH_LIST_UI_KEY);
        }
    }

    OnRemoveMasterDeviceListener onRemoveMasterDeviceListener;

    public void setOnRemoveMasterDeviceListener(OnRemoveMasterDeviceListener listener) {
        onRemoveMasterDeviceListener = listener;
    }

    public interface OnRemoveMasterDeviceListener {
        void onRemoveMasterDevice(List<SWDevice> swDeviceList);
    }

    OnAddMasterDeviceListener onAddMasterDeviceListener;

    public void setOnAddMasterDeviceListener(OnAddMasterDeviceListener listener) {
        onAddMasterDeviceListener = listener;
    }

    public interface OnAddMasterDeviceListener {
        void onAddMasterDevice(List<SWDevice> swDeviceList);
    }

    OnAddOneMasterDeviceListener onAddOneMasterDeviceListener;

    public void setOnAddOneMasterDeviceListener(OnAddOneMasterDeviceListener listener) {
        onAddOneMasterDeviceListener = listener;
    }

    public interface OnAddOneMasterDeviceListener {
        void onAddOneMasterDevice(SWDevice swDevice);
    }


    public void stopTask() {
        Timer var1;
        if ((var1 = this.positionTimer) != null) {
            var1.cancel();
            this.positionTimer = null;
        }
        Timer var2;
        if ((var2 = this.infoTimer) != null) {
            var2.cancel();
            this.infoTimer = null;
        }
        if (taskGetPositionInfoEx != null)
            taskGetPositionInfoEx.cancel();
        if (taskOhterInfoEx != null)
            taskOhterInfoEx.cancel();
        taskGetPositionInfoEx = new TimerTask() {
            @Override
            public void run() {
                if (isPauseAllTask) return;
                SWDevice swDevice = getSelectedDevice();
                if (swDevice != null && swDevice.getIp() != null) {
                    PlayStatusBean playStatusBean = swDevice.getPlayStatusBean();
                    if (playStatusBean != null && playStatusBean.getStatus().equals("play")) {
                        int lastpos = playStatusBean.getCurpos() == null ? 0 : Integer.parseInt(playStatusBean.getCurpos());
                        int tolpos = playStatusBean.getTotlen() == null ? 0 : Integer.parseInt(playStatusBean.getTotlen());
                        if (lastpos < tolpos)
                            playStatusBean.setCurpos(String.valueOf(lastpos + 1000));
                        swDevice.setPlayStatusBean(playStatusBean);
                        if (playStatusTask != null) {
                            playStatusTask.work(swDevice);
                        }
                    }
                    SWDeviceUtils.getDevicePlayerStatus(swDevice.getIp(), new SWDeviceUtils.GetDevicePlayerStatusCallback() {
                        @Override
                        public void onResponse(PlayStatusBean playStatusBean) {
                            SWDevice swDevice = getSelectedDevice();
                            PlayStatusBean lastbean = swDevice.getPlayStatusBean();
                            if (lastbean != null && playStatusBean != null) {
                                if (!swDevice.getSwDeviceInfo().getSWDeviceStatus().getHardware().contains("SWAN")) {
                                    int lastpos = lastbean.getCurpos() == null ? 0 : Integer.parseInt(lastbean.getCurpos());
                                    int thispos = playStatusBean.getCurpos() == null ? 0 : Integer.parseInt(playStatusBean.getCurpos());
                                    if (Math.abs(thispos - lastpos) < 5000) {
                                        playStatusBean.setCurpos(lastbean.getCurpos());
                                    }
                                } else {
                                    int lastposex = lastbean.getCurpos() == null ? 0 : Integer.parseInt(lastbean.getCurpos());
                                    int lasttolposex = playStatusBean.getTotlen() == null ? 0 : Integer.parseInt(playStatusBean.getTotlen());
                                    playStatusBean.setCurpos(lastposex * 1000 + "");
                                    playStatusBean.setTotlen(lasttolposex * 1000 + "");
                                    int lastpos = lastbean.getCurpos() == null ? 0 : Integer.parseInt(lastbean.getCurpos());
                                    int thispos = playStatusBean.getCurpos() == null ? 0 : Integer.parseInt(playStatusBean.getCurpos());
                                    if (Math.abs(thispos - lastpos) < 5000) {
                                        playStatusBean.setCurpos(lastbean.getCurpos());
                                    }
                                }
                            }
                            swDevice.setPlayStatusBean(playStatusBean);
                            if (playStatusTask != null)
                                playStatusTask.work(swDevice);
                        }

                        @Override
                        public void onFailure(String msg) {
                        }
                    });
                }
                getMediaInfo();
            }
        };
        taskOhterInfoEx = new TimerTask() {
            @Override
            public void run() {
                runOhterInfoExTask();
            }
        };
    }

    public List<SWDevice> getMasterDeviceList() {
        return masterDevices;
    }

    public void removeSomeMasterDeviceList(List<SWDevice> list) {
        masterDevices.removeAll(list);
    }

    public void addSomeMasterDeviceList(List<SWDevice> list) {
        masterDevices.addAll(list);
    }

    public void addOneMasterDeviceList(SWDevice swDevice) {
        masterDevices.add(swDevice);
    }

    Gson gson = new Gson();

    public void runOhterInfoExTask() {
        if (isPauseAllTask) return;
        removeSomeMasterDevices(offLineDeviceList);
        removeDuplicationDevices();
        if (SWDeviceManager.getInstance().getMasterDeviceList().size() != 0)
            try {
                for (SWDevice SWDevice : SWDeviceManager.getInstance().getMasterDeviceList()) {
                    if (SWDevice == null) continue;
                    RemoteDevice device1 = (RemoteDevice) SWDevice.getDevice();
                    SWDeviceUtils.getDeviceInfo(device1.getIdentity().getDescriptorURL().getHost(), new SWDeviceUtils.GetDeviceInfoCallback() {
                        @Override
                        public void onResponse(DeviceInfoBean deviceInfoBean) {
                            LogUtils.e("test", "testOnlines:" + SWDevice.getSwDeviceInfo().getSWDeviceStatus().getDeviceName());
                            SWDevice.setOnLineTestFailTimes(0);
                        }

                        @Override
                        public void onFailure(String msg) {
//                            int OnLineTestFailTimes = SWDevice.getOnLineTestFailTimes() + 1;
//                            SWDevice.setOnLineTestFailTimes(OnLineTestFailTimes);
//                            if (OnLineTestFailTimes >= 2)
                            offLineDeviceList.add(SWDevice);
                            LogUtils.e("test", "testOnlinef:" + SWDevice.getSwDeviceInfo().getSWDeviceStatus().getDeviceName()
                                    + " e:" + msg);
                            removeSomeMasterDevices(offLineDeviceList);
                        }
                    });
                }
            } catch (ConcurrentModificationException e) {
            }

        LogUtils.e("test", "mSWDeviceList:" + SWDeviceManager.getInstance().getMasterDeviceList().size() +
                " offLineDeviceList:" + offLineDeviceList.size() + " getSelectedDevice:" + (getSelectedDevice() == null));

        refreshDevicesList();
        if (deviceInfoTask != null) {
            deviceInfoTask.work();
        }
        if (SWDeviceManager.getInstance().getMasterDeviceList() != null && SWDeviceManager.getInstance().getMasterDeviceList().size() != 0) {
            for (SWDevice swDevice : SWDeviceManager.getInstance().getMasterDeviceList()) {
                if (swDevice == null) continue;
                SWDeviceUtils.getControlDeviceInfo(swDevice.getDevice(), new ControlCallback() {
                    @Override
                    public void success(IResponse response) {
                        ClingGetControlDeviceInfoResponse getControlDeviceInfoResponse = (ClingGetControlDeviceInfoResponse) response;
                        Map<String, ActionArgumentValue> map = getControlDeviceInfoResponse.info;
                        LogUtils.e("test", "getSWDeviceStatus:" + map.toString());

                        SWDeviceInfo swDeviceInfo = new SWDeviceInfo();
                        swDeviceInfo.setMultiType(map.get("MultiType").toString());
                        swDeviceInfo.setRouter(map.get("Router").toString());
                        swDeviceInfo.setSsid(map.get("Ssid").toString());
                        swDeviceInfo.setSlaveMask(map.get("SlaveMask").toString());
                        swDeviceInfo.setCurrentVolume(map.get("CurrentVolume").toString());
                        swDeviceInfo.setCurrentMute(map.get("CurrentMute").toString());
                        swDeviceInfo.setCurrentChannel(map.get("CurrentChannel").toString());
                        String slaveListStr = map.get("SlaveList").toString();
                        String statusStr = map.get("Status").toString();
                        SlaveBean slaveBean = gson.fromJson(slaveListStr, SlaveBean.class);
                        SWDeviceStatus swDeviceStatus = gson.fromJson(statusStr, SWDeviceStatus.class);
                        swDeviceInfo.setSlaveList(slaveBean.getSlave_list());
                        for (SWDevice swDevice : SWDeviceManager.getInstance().getMasterDeviceList()) {
                            for (SlaveBean.SlaveListDTO slaveListDTO : slaveBean.getSlave_list()) {
                                if (swDevice.getSwDeviceInfo().getSWDeviceStatus().getSsid().equals(slaveListDTO.getSsid())) {
                                    SWDeviceManager.getInstance().offLineDeviceList.add(swDevice);
                                    removeSomeMasterDevices(offLineDeviceList);
                                }
                            }
                        }
                        if (SWDeviceManager.getInstance().offLineDeviceList.size() != 0) {
                            removeSomeMasterDevices(offLineDeviceList);
                        }
                        swDeviceInfo.setSWDeviceStatus(swDeviceStatus);
                        swDevice.setSwDeviceInfo(swDeviceInfo);
                        SWDeviceUtils.getDevicePlayerStatus(swDevice.getIp(), new SWDeviceUtils.GetDevicePlayerStatusCallback() {
                            @Override
                            public void onResponse(PlayStatusBean playStatusBean) {
                                if (swDevice.getSwDeviceInfo().getSWDeviceStatus().getHardware().contains("SWAN")) {
                                    int lastposex = playStatusBean.getCurpos() == null ? 0 : Integer.parseInt(playStatusBean.getCurpos());
                                    int lasttolposex = playStatusBean.getTotlen() == null ? 0 : Integer.parseInt(playStatusBean.getTotlen());
                                    playStatusBean.setCurpos(lastposex * 1000 + "");
                                    playStatusBean.setTotlen(lasttolposex * 1000 + "");
                                }
                                swDevice.setPlayStatusBean(playStatusBean);
                                SWDeviceUtils.getDeviceMediaInfo(swDevice.getDevice(), new ControlReceiveCallback() {
                                    @Override
                                    public void receive(IResponse response) {
                                        ClingMediaResponse mediaResponse = (ClingMediaResponse) response;
                                        MediaInfo mediaInfo = mediaResponse.getResponse();
                                        LPMediaInfo lpMediaInfo = new LPMediaInfo();
                                        lpMediaInfo.parseMetaData(mediaInfo.getCurrentURIMetaData());
                                        try {
                                            MusicDataBean.DataBean musicDataBean = gson.fromJson(URLDecoder.decode(lpMediaInfo.getAlbumArtURI(),
                                                    "UTF-8"), MusicDataBean.DataBean.class);
                                            LogUtils.e("parse", "Current URI metadata: " + gson.toJson(musicDataBean));
                                            if (musicDataBean != null) {
                                                musicDataBean.setAlbum(lpMediaInfo.getAlbum());
                                                musicDataBean.setCreator(lpMediaInfo.getCreator());
                                                musicDataBean.setMediaType(lpMediaInfo.getMediaType());
                                                if (swDevice != null && (swDevice.getMediaInfo() == null || !swDevice.getMediaInfo().getPlayUrl().equals(musicDataBean.getPlayUrl())))
                                                    swDevice.setMediaInfo(musicDataBean);
                                                if (deviceInfoTask != null && (swDevice == null || getSelectedDevice() == null
                                                        || swDevice.getUuid().equals(getSelectedDevice().getUuid()))) {
                                                    deviceInfoTask.work();
                                                    EventBus.getDefault().post(REFRESH_LIST_UI_KEY);
                                                }
                                            } else {
                                                if (swDevice != null)
                                                    swDevice.setMediaInfo(null);
                                                if (deviceInfoTask != null && (swDevice == null || getSelectedDevice() == null
                                                        || swDevice.getUuid().equals(getSelectedDevice().getUuid()))) {
                                                    deviceInfoTask.work();
                                                    EventBus.getDefault().post(REFRESH_LIST_UI_KEY);
                                                }
                                            }
                                        } catch (Exception e) {
                                            if (swDevice != null)
                                                swDevice.setMediaInfo(null);
                                            if (deviceInfoTask != null && (swDevice == null || getSelectedDevice() == null
                                                    || swDevice.getUuid().equals(getSelectedDevice().getUuid()))) {
                                                deviceInfoTask.work();
                                                EventBus.getDefault().post(REFRESH_LIST_UI_KEY);
                                            }
                                        }
                                    }

                                    @Override
                                    public void success(IResponse response) {

                                    }

                                    @Override
                                    public void fail(IResponse response) {
                                        if (swDevice != null)
                                            swDevice.setMediaInfo(null);
                                        if (deviceInfoTask != null && (swDevice == null || getSelectedDevice() == null
                                                || swDevice.getUuid().equals(getSelectedDevice().getUuid()))) {
                                            deviceInfoTask.work();
                                        }
                                    }
                                });
                            }

                            @Override
                            public void onFailure(String msg) {

                            }
                        });
                    }

                    @Override
                    public void fail(IResponse response) {

                    }
                });
            }
        }
    }

    public void initSelectedDevice() {
        if (SWDeviceManager.getInstance().getSelectedDevice() == null && SWDeviceManager.getInstance().getMasterDeviceList().size() != 0) {
            SWDevice item = (SWDevice) SWDeviceManager.getInstance().getMasterDeviceList().get(0);
            if (Utils.isNull(item)) {
                return;
            }
            SWDeviceManager.getInstance().setSelectedDevice(item);
        }
    }

    public static boolean isContainsUuidDevice(String uuid, List<SWDevice> datalist) {
        for (SWDevice swDevice : datalist) {
            if (uuid.equals(swDevice.getUuid())) {
                return true;
            }
        }
        return false;
    }

    public void executeTask() {
        this.stopTask();
        if (taskGetPositionInfoEx == null || taskOhterInfoEx == null) return;
        Timer var1;
        if (this.positionTimer == null) {
            var1 = new Timer();
            this.positionTimer = var1;
        }
        this.positionTimer.scheduleAtFixedRate(this.taskGetPositionInfoEx, 1100L, 1000L);

        Timer var2;
        if (this.infoTimer == null) {
            var2 = new Timer();
            this.infoTimer = var2;
        }
        this.infoTimer.scheduleAtFixedRate(this.taskOhterInfoEx, 1100L, 5000L);
    }

    private SWDeviceManager() {
    }

    public static SWDeviceManager getInstance() {
        if (Utils.isNull(INSTANCE)) {
            INSTANCE = new SWDeviceManager();
        }
        return INSTANCE;
    }


    String customQuery = "M-SEARCH * HTTP/1.1" + "\r\n" +
            "St: ssdp:wiimudevice" + "\r\n" + // Use this for all UPnP Devices (DEFAULT)
            "Host: 239.255.255.250:1900" + "\r\n" +
            "Mx: 3" + "\r\n" +
//            "USER-AGENT: 3"+ "\r\n" +
            //"ST: urn:schemas-upnp-org:service:AVTransport:1" + "\r\n" + // Use for Sonos
            //"ST: urn:schemas-upnp-org:device:InternetGatewayDevice:1" + "\r\n" + // Use for Routers
            "Man: \"ssdp:discover\"" + "\r\n" +
            "\r\n";
    int customPort = 1900;
    String customAddress = "239.255.255.250";
    String customQuery2 = "M-SEARCH * HTTP/1.1" + "\r\n" +
            "St: ssdp:wiimudevice" + "\r\n" + // Use this for all UPnP Devices (DEFAULT)
            "Host: 239.255.255.250:1900" + "\r\n" +
            "Mx: 3" + "\r\n" +
            "Man: \"ssdp:discover\"" + "\r\n" +
            "\r\n";
    int customPort2 = 1900;
    String customAddress2 = "229.255.255.250";
    String customQuery3 = "M-SEARCH * HTTP/1.1" + "\r\n" +
            "St: ssdp:wiimudevice" + "\r\n" + // Use this for all UPnP Devices (DEFAULT)
            "Host: 239.255.255.250:1900" + "\r\n" +
            "Mx: 3" + "\r\n" +
            "Man: \"ssdp:discover\"" + "\r\n" +
            "\r\n";
    int customPort3 = 5353;
    String customAddress3 = "224.0.0.251";

    @Override
    public void searchDevices() {//"ssdp:yamaha", "ssdp:wiimudevice"
        if (!Utils.isNull(mUpnpService)) {
//             SWSendingSearch.searchDevices(mUpnpService.getControlPoint(),new SWSendingSearch(
//                     mUpnpService.getUpnpService(),new STCustomHeader("ssdp:all"),60));
//
//            CustomSendingSearch.searchDevices(mUpnpService.getControlPoint(), new CustomSendingSearch(mUpnpService.getUpnpService(),
//                    getStringUpnpHearder("ssdp:yamaha"), getStringUpnpHearder("\"ssdp:discover\""), getStringUpnpHearder("3"),
//                    getStringUpnpHearder("239.255.255.250:1900"), customAddress, customPort));
//            CustomSendingSearch.searchDevices(mUpnpService.getControlPoint(), new CustomSendingSearch(mUpnpService.getUpnpService(),
//                    getStringUpnpHearder("ssdp:yamaha"), getStringUpnpHearder("\"ssdp:discover\""), getStringUpnpHearder("3"),
//                    getStringUpnpHearder("239.255.255.250:1900"), customAddress2, customPort2));
//            CustomSendingSearch.searchDevices(mUpnpService.getControlPoint(), new CustomSendingSearch(mUpnpService.getUpnpService(),
//                    getStringUpnpHearder("ssdp:yamaha"), getStringUpnpHearder("\"ssdp:discover\""), getStringUpnpHearder("3"),
//                    getStringUpnpHearder("239.255.255.250:1900"), customAddress3, customPort3));
            LogUtils.e("dosearchDevices");

////
            CustomSendingSearch.searchDevices(mUpnpService.getControlPoint(), new CustomSendingSearch(mUpnpService.getUpnpService(),
                    getStringUpnpHearder("ssdp:all"), getStringUpnpHearder("\"ssdp:discover\""), getStringUpnpHearder("3"),
                    getStringUpnpHearder("239.255.255.250:1900"), customAddress, customPort));
            CustomSendingSearch.searchDevices(mUpnpService.getControlPoint(), new CustomSendingSearch(mUpnpService.getUpnpService(),
                    getStringUpnpHearder("ssdp:all"), getStringUpnpHearder("\"ssdp:discover\""), getStringUpnpHearder("3"),
                    getStringUpnpHearder("239.255.255.250:1900"), customAddress2, customPort2));
            CustomSendingSearch.searchDevices(mUpnpService.getControlPoint(), new CustomSendingSearch(mUpnpService.getUpnpService(),
                    getStringUpnpHearder("ssdp:all"), getStringUpnpHearder("\"ssdp:discover\""), getStringUpnpHearder("3"),
                    getStringUpnpHearder("239.255.255.250:1900"), customAddress3, customPort3));

            CustomSendingSearch.searchDevices(mUpnpService.getControlPoint(), new CustomSendingSearch(mUpnpService.getUpnpService(),
                    getStringUpnpHearder("ssdp:wiimudevice"), getStringUpnpHearder("\"ssdp:discover\""), getStringUpnpHearder("3"),
                    getStringUpnpHearder("239.255.255.250:1900"), customAddress, customPort));
            CustomSendingSearch.searchDevices(mUpnpService.getControlPoint(), new CustomSendingSearch(mUpnpService.getUpnpService(),
                    getStringUpnpHearder("ssdp:wiimudevice"), getStringUpnpHearder("\"ssdp:discover\""), getStringUpnpHearder("3"),
                    getStringUpnpHearder("239.255.255.250:1900"), customAddress2, customPort2));
            CustomSendingSearch.searchDevices(mUpnpService.getControlPoint(), new CustomSendingSearch(mUpnpService.getUpnpService(),
                    getStringUpnpHearder("ssdp:wiimudevice"), getStringUpnpHearder("\"ssdp:discover\""), getStringUpnpHearder("3"),
                    getStringUpnpHearder("239.255.255.250:1900"), customAddress3, customPort3));
        }
    }

    public UpnpHeader getStringUpnpHearder(String param) {
        return new UpnpHeader<String>() {
            @Override
            public void setString(String s) throws InvalidHeaderException {

            }

            @Override
            public String getString() {
                return param;
            }
        };
    }


    //创建wifi连断状态的监听广播
    WifiChangedCast wifiChangedCast;

    public void createBroadcast(Context context) {
        IntentFilter var1 = new IntentFilter();
        var1.addAction("android.net.conn.CONNECTIVITY_CHANGE");
        wifiChangedCast = new WifiChangedCast();
        context.registerReceiver(wifiChangedCast, var1);
    }


    @Override
    @Nullable
    public Collection<SWDevice> getDmrDevices() {
        if (Utils.isNull(mUpnpService)) {
            return null;
        }
        return masterDevices;
    }

    @Override
    @Nullable
    public IControlPoint getControlPoint() {
        if (Utils.isNull(mUpnpService)) {
            return null;
        }
        ClingControlPoint.getInstance().setControlPoint(mUpnpService.getControlPoint());

        return ClingControlPoint.getInstance();
    }

    @Override
    public Registry getRegistry() {
        return mUpnpService.getRegistry();
    }

    @Override
    public SWDevice getSelectedDevice() {
        if (Utils.isNull(mDeviceManager)) {
            return null;
        }
        return mDeviceManager.getSelectedDevice();
    }

    @Override
    public void cleanSelectedDevice() {
        if (Utils.isNull(mDeviceManager)) {
            return;
        }
        mDeviceManager.cleanSelectedDevice();
    }

    @Override
    public void setSelectedDevice(SWDevice device) {
        mDeviceManager.setSelectedDevice(device);

    }


    @Override
    public void registerAVTransport(Context context) {
        if (Utils.isNull(mDeviceManager))
            return;
        mDeviceManager.registerAVTransport(context);
    }

    @Override
    public void registerRenderingControl(Context context) {
        if (Utils.isNull(mDeviceManager))
            return;

        mDeviceManager.registerRenderingControl(context);
    }

    /**
     * 监听投屏端 MediaRenderer 回调
     *
     * @param context
     */
    @Override
    public void registerMediaRenderer(Context context) {
        if (Utils.isNull(mDeviceManager))
            return;

        mDeviceManager.registerMediaRenderer(context);
    }

    /**
     * 监听投屏端 MediaServer 回调
     *
     * @param context
     */
    @Override
    public void registerMediaServer(Context context) {
        if (Utils.isNull(mDeviceManager))
            return;

        mDeviceManager.registerMediaServer(context);
    }

    /**
     * 监听投屏端 ConnectionManager 回调
     *
     * @param context
     */
    @Override
    public void registerConnectionManager(Context context) {
        if (Utils.isNull(mDeviceManager))
            return;

        mDeviceManager.registerConnectionManager(context);
    }

    /**
     * 监听投屏端 ContentDirectory 回调
     *
     * @param context
     */
    @Override
    public void registerContentDirectory(Context context) {
        if (Utils.isNull(mDeviceManager))
            return;

        mDeviceManager.registerContentDirectory(context);
    }

    @Override
    public void setUpnpService(ClingUpnpService upnpService) {
        mUpnpService = upnpService;
    }

    @Override
    public void setDeviceManager(IDeviceManager deviceManager) {
        mDeviceManager = deviceManager;
    }


    /**
     * 用于监听发现设备
     */
    private BrowseRegistryListener mBrowseRegistryListener = new BrowseRegistryListener();
    private ServiceConnection mUpnpServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            LogUtils.e("test", "mUpnpServiceConnection onServiceConnected");

            ClingUpnpService.LocalBinder binder = (ClingUpnpService.LocalBinder) service;
            ClingUpnpService beyondUpnpService = binder.getService();

            SWDeviceManager swDeviceManager = SWDeviceManager.getInstance();
            swDeviceManager.setUpnpService(beyondUpnpService);
            swDeviceManager.setDeviceManager(new DeviceManager());
            swDeviceManager.getRegistry().addListener(mBrowseRegistryListener);
            LPSearchControlPoint.getInstance();
            LPSearchControlPoint.upnpservice = binder;
            //Search on service created.
            swDeviceManager.searchDevices();

        }

        @Override
        public void onServiceDisconnected(ComponentName className) {
            LogUtils.e("test", "mUpnpServiceConnection onServiceDisconnected");
            SWDeviceManager.getInstance().setUpnpService(null);
        }
    };

    public void setOnDeviceListChangedListener(DeviceListChangedListener deviceListChangedListener) {
        this.mBrowseRegistryListener.setOnDeviceListChangedListener(deviceListChangedListener);
    }

    Activity mActivity;
    LPDeviceManager lpDeviceManager;

    public void init(Activity activity) {
        this.mActivity = activity;
        LPDeviceManagerParam param = new LPDeviceManagerParam();
        param.context = activity.getApplication();
        param.appid = "";
        List<String> mdnsServiceTypes = new ArrayList<>();
        mdnsServiceTypes.add(LinkplayConstants.regType);
        param.mdnsServiceTypes = mdnsServiceTypes;
        param.registerMaintainMaxAgeSeconds = 30;
        lpDeviceManager = LPDeviceManager.getInstance();
        lpDeviceManager.init(param);

        LPLogUtil.init(new LPPrintLogCallback() {
            @Override
            public void i(String s, String s1) {
                LogUtils.e("test", "LPLogUtilI:" + s1);
            }

            @Override
            public void d(String s, String s1) {
                LogUtils.e("test", "LPLogUtilD:" + s1);
            }

            @Override
            public void e(String s, String s1) {
                LogUtils.e("test", "LPLogUtilE:" + s1);
            }

            @Override
            public void v(String s, String s1) {
                LogUtils.e("test", "LPLogUtilV:" + s1);
            }

            @Override
            public void w(String s, String s1) {
                LogUtils.e("test", "LPLogUtilW:" + s1);
            }
        });

        bindServices(activity);
        createBroadcast(activity);
        SWDeviceManager.getInstance().executeTask();
    }

    private void bindServices(Activity activity) {
        // Bind UPnP service
        Intent upnpServiceIntent = new Intent(activity, ClingUpnpService.class);
        activity.bindService(upnpServiceIntent, mUpnpServiceConnection, Context.BIND_AUTO_CREATE);
    }

    public void refreshDevicesList() {
        LogUtils.e("test", "refreshDevicesList");
        if (mUpnpService != null) {
            LogUtils.e("test", "refreshDevicesList2");
            mUpnpService.getRegistry().removeAllRemoteDevices();  // 清除当前列表
            mUpnpService.getRegistry().removeAllLocalDevices();
            searchDevices();  // 重新搜索设备
            if (onRefreshSearchDevicesListener != null) {
                LogUtils.e("test", "refreshDevicesList3");
                onRefreshSearchDevicesListener.onRefresh();
            }
        }
    }


    public void setOnRefreshSearchDevicesListener(OnRefreshSearchDevicesListener listener) {
        this.onRefreshSearchDevicesListener = listener;
    }

    OnRefreshSearchDevicesListener onRefreshSearchDevicesListener;

    public interface OnRefreshSearchDevicesListener {
        void onRefresh();
    }

    public void addDevice(SWDevice SWDevice) {
        if (!contain(SWDevice.getDevice(), masterDevices)) {
            SWDeviceUtils.getControlDeviceInfo(SWDevice.getDevice(), new ControlCallback() {
                @Override
                public void success(IResponse response) {
                    ClingGetControlDeviceInfoResponse getControlDeviceInfoResponse = (ClingGetControlDeviceInfoResponse) response;
                    Map<String, ActionArgumentValue> map = getControlDeviceInfoResponse.info;
                    LogUtils.e("test", "getSWDeviceStatus:" + map.toString());

                    SWDeviceInfo swDeviceInfo = new SWDeviceInfo();
                    swDeviceInfo.setMultiType(map.get("MultiType").toString());
                    swDeviceInfo.setRouter(map.get("Router").toString());
                    swDeviceInfo.setSsid(map.get("Ssid").toString());
                    swDeviceInfo.setSlaveMask(map.get("SlaveMask").toString());
                    swDeviceInfo.setCurrentVolume(map.get("CurrentVolume").toString());
                    swDeviceInfo.setCurrentMute(map.get("CurrentMute").toString());
                    swDeviceInfo.setCurrentChannel(map.get("CurrentChannel").toString());
                    String slaveListStr = map.get("SlaveList").toString();
                    String statusStr = map.get("Status").toString();
                    SlaveBean slaveBean = gson.fromJson(slaveListStr, SlaveBean.class);
                    SWDeviceStatus swDeviceStatus = gson.fromJson(statusStr, SWDeviceStatus.class);
                    swDeviceInfo.setSlaveList(slaveBean.getSlave_list());
                    swDeviceInfo.setSWDeviceStatus(swDeviceStatus);
                    SWDevice.setSwDeviceInfo(swDeviceInfo);

                    mSWPlayControl.getPositionInfoByDevice(SWDevice.getDevice(), new ControlReceiveCallback() {
                        @Override
                        public void receive(IResponse response) {
                            ClingPositionResponse positionResponse = (ClingPositionResponse) response;
                            PositionInfo positionInfo = positionResponse.getResponse();
                            if (positionInfo != null) {
                                LPMediaInfo lpMediaInfo = new LPMediaInfo();
                                lpMediaInfo.parseMetaData(positionInfo.getTrackMetaData());
                                try {
                                    MusicDataBean.DataBean musicDataBean = gson.fromJson(URLDecoder.decode(lpMediaInfo.getAlbumArtURI(),
                                            "UTF-8"), MusicDataBean.DataBean.class);
                                    LogUtils.e("parse", SWDevice.getDevice().getDetails().getFriendlyName() + "Current URI metadata2: " + gson.toJson(musicDataBean));
                                    if (musicDataBean != null) {
                                        musicDataBean.setAlbum(lpMediaInfo.getAlbum());
                                        musicDataBean.setCreator(lpMediaInfo.getCreator());
                                        musicDataBean.setMediaType(lpMediaInfo.getMediaType());
                                        if (SWDevice.getMediaInfo() == null || !SWDevice.getMediaInfo().getPlayUrl().equals(musicDataBean.getPlayUrl()))
                                            SWDevice.setMediaInfo(musicDataBean);
                                    } else {
                                        SWDevice.setMediaInfo(null);
                                    }
                                } catch (Exception e) {
                                    SWDevice.setMediaInfo(null);
                                }
                            } else {
                                SWDevice.setMediaInfo(null);
                            }
                            if (!contain(SWDevice.getDevice(), masterDevices)) {
                                addOneMasterDevice(SWDevice);
                                SWDeviceManager.getInstance().removeDuplicationDevices();
                            }
                        }

                        @Override
                        public void success(IResponse response) {

                        }

                        @Override
                        public void fail(IResponse response) {

                        }
                    });
                }

                @Override
                public void fail(IResponse response) {

                }
            });
        }

//        if (!contain(SWDevice.getDevice(),mSWDeviceList)) {
//            mSWPlayControl.getPositionInfoByDevice(SWDevice.getDevice(), new ControlReceiveCallback() {
//                @Override
//                public void receive(IResponse response) {
//                    ClingPositionResponse positionResponse = (ClingPositionResponse) response;
//                    PositionInfo positionInfo = positionResponse.getResponse();
//                    if (positionInfo != null) {
//                        LPMediaInfo lpMediaInfo = new LPMediaInfo();
//                        lpMediaInfo.parseMetaData(positionInfo.getTrackMetaData());
//                        try {
//                            MusicDataBean.DataBean musicDataBean = gson.fromJson(URLDecoder.decode(lpMediaInfo.getAlbumArtURI(),
//                                    "UTF-8"), MusicDataBean.DataBean.class);
//                            LogUtils.e("parse", SWDevice.getDevice().getDetails().getFriendlyName() + "Current URI metadata2: " + gson.toJson(musicDataBean));
//                            if (musicDataBean != null) {
//                                musicDataBean.setAlbum(lpMediaInfo.getAlbum());
//                                musicDataBean.setCreator(lpMediaInfo.getCreator());
//                                musicDataBean.setMediaType(lpMediaInfo.getMediaType());
//                                SWDevice.setMediaInfo(musicDataBean);
//                            } else {
//                                SWDevice.setMediaInfo(null);
//                            }
//                        } catch (Exception e) {
//                            SWDevice.setMediaInfo(null);
//                        }
//                    }else {
//                        SWDevice.setMediaInfo(null);
//                    }
//                    mSWDeviceList.add(SWDevice);
//                }
//
//                @Override
//                public void success(IResponse response) {
//
//                }
//
//                @Override
//                public void fail(IResponse response) {
//
//                }
//            });
//            SWDeviceUtils.getSWDeviceStatus(SWDevice.getDevice(), new ControlCallback() {
//                @Override
//                public void success(IResponse response) {
//                    ClingGetControlDeviceInfoResponse getControlDeviceInfoResponse = (ClingGetControlDeviceInfoResponse)response;
//                    Map<String, ActionArgumentValue> map = getControlDeviceInfoResponse.info;
//                    LogUtils.e("test","getSWDeviceStatus:" + map.toString());
//                    String slaveListStr = map.get("SlaveList").toString();
//                    String statusStr = map.get("Status").toString();
//                    Gson gson = gson;
//                    SlaveBean slaveBean = gson.fromJson(slaveListStr,SlaveBean.class);
//                    SWDeviceStatus SWDeviceStatus = gson.fromJson(statusStr,SWDeviceStatus.class);
//                    SWDevice.setSlaveList(slaveBean.getSlave_list());
//                    SWDevice.setSWDeviceStatus(SWDeviceStatus);
//                }
//
//                @Override
//                public void fail(IResponse response) {
//
//                }
//            });
//        }
    }

    @Nullable
    public SWDevice getClingDevice(Device device) {
        if (masterDevices != null && masterDevices.size() != 0) {
            for (SWDevice SWDevice : masterDevices) {
                Device deviceTemp = SWDevice.getDevice();
                if (deviceTemp != null && SWDevice.getUuid().equals(device.getIdentity().getUdn().getIdentifierString())) {
                    return SWDevice;
                }
            }
        }
        return null;
    }

    public static boolean contain(Device device, List<SWDevice> list) {
        for (SWDevice SWDevice : list) {
            Device deviceTemp = SWDevice.getDevice();
            if (deviceTemp != null && SWDevice.getUuid().equals(device.getIdentity().getUdn().getIdentifierString())) {
                return true;
            }
        }
        return false;
    }


    @Override
    public void destroy() {
        // Unbind UPnP service
        if (mActivity != null) {
            this.mActivity.unbindService(mUpnpServiceConnection);
            this.mActivity.unregisterReceiver(wifiChangedCast);
        }
        lpDeviceManager.clear();
        lpDeviceManager.stop();
        stopTask();
        mUpnpService.onDestroy();
        mDeviceManager.destroy();
        removeSomeMasterDevices(masterDevices);
    }
}
