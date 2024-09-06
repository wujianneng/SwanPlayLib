package com.nesp.android.cling.service.manager;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
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
import com.nesp.android.cling.entity.ClingPositionResponse;
import com.nesp.android.cling.entity.DeviceInfoBean;
import com.nesp.android.cling.entity.IControlPoint;
import com.nesp.android.cling.entity.IDevice;
import com.nesp.android.cling.entity.IResponse;
import com.nesp.android.cling.entity.LPMediaInfo;
import com.nesp.android.cling.entity.MusicDataBean;
import com.nesp.android.cling.entity.PlayStatusBean;
import com.nesp.android.cling.entity.SWDevice;
import com.nesp.android.cling.entity.SWDeviceInfo;
import com.nesp.android.cling.entity.SWDeviceList;
import com.nesp.android.cling.entity.SWDeviceStatus;
import com.nesp.android.cling.entity.SlaveBean;
import com.nesp.android.cling.listener.BrowseRegistryListener;
import com.nesp.android.cling.listener.DeviceListChangedListener;
import com.nesp.android.cling.service.ClingUpnpService;
import com.nesp.android.cling.util.OkHttp3Util;
import com.nesp.android.cling.util.SWDeviceUtils;
import com.nesp.android.cling.util.Utils;

import org.greenrobot.eventbus.EventBus;
import org.teleal.cling.model.action.ActionArgumentValue;
import org.teleal.cling.model.message.header.InvalidHeaderException;
import org.teleal.cling.model.message.header.UpnpHeader;
import org.teleal.cling.model.meta.RemoteDevice;
import org.teleal.cling.model.types.DeviceType;
import org.teleal.cling.model.types.ServiceType;
import org.teleal.cling.model.types.UDADeviceType;
import org.teleal.cling.model.types.UDAServiceType;
import org.teleal.cling.registry.Registry;
import org.teleal.cling.support.model.PositionInfo;

import java.io.IOException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import io.reactivex.internal.operators.single.SingleTimer;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

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

    private static SWDeviceManager INSTANCE = null;

    private ClingUpnpService mUpnpService;
    private IDeviceManager mDeviceManager;

    //    private SystemService mSystemService;
    private Timer positionTimer, infoTimer;
    private List<SWDevice> offLineDeviceList = new ArrayList<>();
    private boolean isPauseAllTask = false;

    public void pauseAllTask(boolean isPauseAllTask) {
        this.isPauseAllTask = isPauseAllTask;
        if(!isPauseAllTask){
          runOhterInfoExTask();
        }
    }

    TimerTask taskGetPositionInfoEx, taskOhterInfoEx;


    private SWPlayControl mSWPlayControl = new SWPlayControl();

    public void getPositionInfo() {
        Log.e("test", "getPositionInfodo");
        SWDevice swDevice = getSelectedDevice();
        mSWPlayControl.getPositionInfo(new ControlReceiveCallback() {
            @Override
            public void receive(IResponse response) {
                Log.e("test", "getPositionInfodoreceive");
                ClingPositionResponse positionResponse = (ClingPositionResponse) response;
                PositionInfo positionInfo = positionResponse.getResponse();
                if (workInTimeTask != null)
                    workInTimeTask.work(positionInfo,swDevice.getUuid());
            }

            @Override
            public void success(IResponse response) {
                Log.e("test", "getPositionInfodosuccess");
            }

            @Override
            public void fail(IResponse response) {
                Log.e("test", "getPositionInfodofail");
                if (workInTimeTask != null)
                    workInTimeTask.work(null,swDevice.getUuid());
            }
        });
    }

    public interface WorkInTimeTask {
        void work(PositionInfo positionInfo,String fromUuid);
    }

    public interface WorkInInfoTask {
        void work();
    }

    WorkInTimeTask workInTimeTask;
    WorkInInfoTask workInInfoTask;

    public void setGetInfoTask(WorkInTimeTask timeTask, WorkInInfoTask infoTask) {
        this.workInTimeTask = timeTask;
        this.workInInfoTask = infoTask;
    }

    public void removeDuplicationDevices(){
        Map<String,Integer> mutrimap = new HashMap<>();
        for(SWDevice swDevice : SWDeviceList.masterDevices){
            if(mutrimap.containsKey(swDevice.getUuid())){
                mutrimap.put(swDevice.getUuid(),1 + mutrimap.get(swDevice.getUuid()));
            }else {
                mutrimap.put(swDevice.getUuid(),1);
            }
        }
        List<SWDevice> mutriDevices = new ArrayList<>();
        for(SWDevice swDevice : SWDeviceList.masterDevices){
            if(mutrimap.containsKey(swDevice.getUuid())){
                int count = mutrimap.get(swDevice.getUuid());
                if(count > 1){
                    mutriDevices.add(swDevice);
                    mutrimap.put(swDevice.getUuid(),count - 1);
                }
            }
        }
        SWDeviceList.masterDevices.removeAll(mutriDevices);
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
                getPositionInfo();
            }
        };
        taskOhterInfoEx = new TimerTask() {
            @Override
            public void run() {
                runOhterInfoExTask();
            }
        };
    }

    public void runOhterInfoExTask(){
        if (isPauseAllTask) return;
        SWDeviceList.masterDevices.removeAll(offLineDeviceList);
        removeDuplicationDevices();
        offLineDeviceList.clear();
        if (SWDeviceList.masterDevices.size() != 0)
            for (SWDevice SWDevice : SWDeviceList.masterDevices) {
                if (SWDevice == null) continue;
                RemoteDevice device1 = (RemoteDevice) SWDevice.getDevice();
                SWDeviceUtils.getDeviceInfo(device1.getIdentity().getDescriptorURL().getHost(), new SWDeviceUtils.GetDeviceInfoCallback() {
                    @Override
                    public void onResponse(DeviceInfoBean deviceInfoBean) {
                        Log.e("test", "testOnlines:" + SWDevice.getDevice().getDetails().getFriendlyName());
                    }

                    @Override
                    public void onFailure(String msg) {
                        Log.e("test", "testOnlinef:" + SWDevice.getDevice().getDetails().getFriendlyName() + " e:" + msg);
                        offLineDeviceList.add(SWDevice);
                    }
                });
            }

        Log.e("test", "mSWDeviceList:" + SWDeviceList.getInstance().mSWDeviceList.size()
                + " masterDevicessize:" + SWDeviceList.masterDevices.size() + " offLineDeviceList:" + offLineDeviceList.size());

//                offLineDeviceList.clear();
//                for(SWDevice swDevice : SWDeviceList.masterDevices){
//                    if(!isContainsUuidDevice(swDevice.getUuid(),SWDeviceList.getInstance().mSWDeviceList)){
//                        offLineDeviceList.add(swDevice);
//                    }
//                }
//                for(SWDevice swDevice : SWDeviceList.getInstance().mSWDeviceList){
//                    if(!isContainsUuidDevice(swDevice.getUuid(),SWDeviceList.masterDevices)){
//                        SWDeviceList.masterDevices.add(swDevice);
//                    }
//                }
//                SWDeviceList.masterDevices.removeAll(offLineDeviceList);
//                Log.e("test", "mSWDeviceList:" + SWDeviceList.getInstance().mSWDeviceList.size()
//                        + " masterDevicessize:" + SWDeviceList.masterDevices.size() + " offLineDeviceList:" + offLineDeviceList.size());
//                SWDeviceList.getInstance().mSWDeviceList.clear();

        refreshDevicesList();
        if (SWDeviceList.getInstance().masterDevices != null && SWDeviceList.getInstance().masterDevices.size() != 0) {
            for (SWDevice swDevice : SWDeviceList.getInstance().masterDevices) {
                if (swDevice == null) continue;
                SWDeviceUtils.getControlDeviceInfo(swDevice.getDevice(), new ControlCallback() {
                    @Override
                    public void success(IResponse response) {
                        ClingGetControlDeviceInfoResponse getControlDeviceInfoResponse = (ClingGetControlDeviceInfoResponse) response;
                        Map<String, ActionArgumentValue> map = getControlDeviceInfoResponse.info;
                        Log.e("test", "getSWDeviceStatus:" + map.toString());

                        Gson gson = new Gson();

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
                        swDevice.setSwDeviceInfo(swDeviceInfo);
                        SWDeviceUtils.getDevicePlayerStatus(swDevice.getIp(), new SWDeviceUtils.GetDevicePlayerStatusCallback() {
                            @Override
                            public void onResponse(PlayStatusBean deviceInfoBean) {
                                swDevice.setPlayStatusBean(deviceInfoBean);
                                SWDeviceUtils.getDevicePositionInfo(swDevice.getDevice(), new ControlReceiveCallback() {
                                    @Override
                                    public void receive(IResponse response) {
                                        ClingPositionResponse positionResponse = (ClingPositionResponse) response;
                                        PositionInfo positionInfo = positionResponse.getResponse();
                                        LPMediaInfo lpMediaInfo = new LPMediaInfo();
                                        lpMediaInfo.parseMetaData(positionInfo.getTrackMetaData());
                                        try {
                                            MusicDataBean.DataBean musicDataBean = new Gson().fromJson(URLDecoder.decode(lpMediaInfo.getAlbumArtURI(),
                                                    "UTF-8"), MusicDataBean.DataBean.class);
                                            Log.e("parse", "Current URI metadata: " + new Gson().toJson(musicDataBean));
                                            if (musicDataBean != null) {
                                                musicDataBean.setAlbum(lpMediaInfo.getAlbum());
                                                musicDataBean.setCreator(lpMediaInfo.getCreator());
                                                musicDataBean.setMediaType(lpMediaInfo.getMediaType());
                                                swDevice.setMediaInfo(musicDataBean);
                                                EventBus.getDefault().post(SWDeviceList.REFRESH_LIST_UI_KEY);
                                                if (workInInfoTask != null)
                                                    workInInfoTask.work();
                                            } else {
                                                swDevice.setMediaInfo(null);
                                                EventBus.getDefault().post(SWDeviceList.REFRESH_LIST_UI_KEY);
                                                if (workInInfoTask != null)
                                                    workInInfoTask.work();
                                            }
                                        } catch (Exception e) {
                                            swDevice.setMediaInfo(null);
                                            EventBus.getDefault().post(SWDeviceList.REFRESH_LIST_UI_KEY);
                                            if (workInInfoTask != null)
                                                workInInfoTask.work();
                                        }
                                    }

                                    @Override
                                    public void success(IResponse response) {

                                    }

                                    @Override
                                    public void fail(IResponse response) {
                                        swDevice.setMediaInfo(null);
                                        EventBus.getDefault().post(SWDeviceList.REFRESH_LIST_UI_KEY);
                                        if (workInInfoTask != null)
                                            workInInfoTask.work();
                                    }
                                });

                            }

                            @Override
                            public void onFailure(String msg) {
                                swDevice.setMediaInfo(null);
                                EventBus.getDefault().post(SWDeviceList.REFRESH_LIST_UI_KEY);
                                if (workInInfoTask != null)
                                    workInInfoTask.work();
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

    public boolean isContainsUuidDevice(String uuid, List<SWDevice> datalist) {
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
        return SWDeviceList.getInstance().getClingDeviceList();
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
            Log.e("test", "mUpnpServiceConnection onServiceConnected");

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
            Log.e("test", "mUpnpServiceConnection onServiceDisconnected");
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
                Log.e("test", "LPLogUtilI:" + s1);
            }

            @Override
            public void d(String s, String s1) {
                Log.e("test", "LPLogUtilD:" + s1);
            }

            @Override
            public void e(String s, String s1) {
                Log.e("test", "LPLogUtilE:" + s1);
            }

            @Override
            public void v(String s, String s1) {
                Log.e("test", "LPLogUtilV:" + s1);
            }

            @Override
            public void w(String s, String s1) {
                Log.e("test", "LPLogUtilW:" + s1);
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
        Log.e("test", "refreshDevicesList");
        if (mUpnpService != null) {
            Log.e("test", "refreshDevicesList2");
            mUpnpService.getRegistry().removeAllRemoteDevices();  // 清除当前列表
            mUpnpService.getRegistry().removeAllRemoteDevices();
            searchDevices();  // 重新搜索设备
            if (onRefreshSearchDevicesListener != null) {
                Log.e("test", "refreshDevicesList3");
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
        SWDeviceList.getInstance().destroy();
    }
}
