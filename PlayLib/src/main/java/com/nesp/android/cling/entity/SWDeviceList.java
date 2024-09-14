package com.nesp.android.cling.entity;

import android.util.Log;

import androidx.annotation.Nullable;

import com.google.gson.Gson;
import com.nesp.android.cling.control.SWPlayControl;
import com.nesp.android.cling.control.callback.ControlCallback;
import com.nesp.android.cling.control.callback.ControlReceiveCallback;
import com.nesp.android.cling.service.manager.SWDeviceManager;
import com.nesp.android.cling.util.SWDeviceUtils;
import com.nesp.android.cling.util.Utils;

import org.greenrobot.eventbus.EventBus;
import org.teleal.cling.model.action.ActionArgumentValue;
import org.teleal.cling.model.meta.Device;
import org.teleal.cling.support.model.PositionInfo;

import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 说明：单例设备列表, 保证全局只有一个设备列表
 * <p>
 * 日期：17/6/30 11:25
 */

public class SWDeviceList {

    private static SWDeviceList INSTANCE = null;

    public static List<SWDevice> masterDevices = new ArrayList<>();//主设备集合

    public final static String REFRESH_LIST_UI_KEY = "refresh_list_ui_key";


    public static SWDeviceList getInstance() {
        if (Utils.isNull(INSTANCE)) {
            INSTANCE = new SWDeviceList();
        }
        return INSTANCE;
    }


    private SWPlayControl mSWPlayControl = new SWPlayControl();

    public void addDevice(SWDevice SWDevice) {
        if (!contain(SWDevice.getDevice(), masterDevices)) {
            SWDeviceUtils.getControlDeviceInfo(SWDevice.getDevice(), new ControlCallback() {
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
                                    MusicDataBean.DataBean musicDataBean = new Gson().fromJson(URLDecoder.decode(lpMediaInfo.getAlbumArtURI(),
                                            "UTF-8"), MusicDataBean.DataBean.class);
                                    Log.e("parse", SWDevice.getDevice().getDetails().getFriendlyName() + "Current URI metadata2: " + new Gson().toJson(musicDataBean));
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
                            if(!contain(SWDevice.getDevice(), masterDevices)) {
                                masterDevices.add(SWDevice);
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
//                            MusicDataBean.DataBean musicDataBean = new Gson().fromJson(URLDecoder.decode(lpMediaInfo.getAlbumArtURI(),
//                                    "UTF-8"), MusicDataBean.DataBean.class);
//                            Log.e("parse", SWDevice.getDevice().getDetails().getFriendlyName() + "Current URI metadata2: " + new Gson().toJson(musicDataBean));
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
//                    Log.e("test","getSWDeviceStatus:" + map.toString());
//                    String slaveListStr = map.get("SlaveList").toString();
//                    String statusStr = map.get("Status").toString();
//                    Gson gson = new Gson();
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

    @Nullable
    public List<SWDevice> getClingDeviceList() {
        return masterDevices;
    }


    public void destroy() {
        masterDevices = null;
        INSTANCE = null;
    }
}