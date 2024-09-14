package com.nesp.android.cling.util;

import android.app.Activity;
import android.os.CountDownTimer;
import android.util.Log;

import androidx.annotation.Nullable;

import com.google.gson.Gson;
import com.nesp.android.cling.callback.GetControlDeviceInfo;
import com.nesp.android.cling.control.callback.ControlCallback;
import com.nesp.android.cling.control.callback.ControlReceiveCallback;
import com.nesp.android.cling.entity.ClingGetControlDeviceInfoResponse;
import com.nesp.android.cling.entity.ClingPositionResponse;
import com.nesp.android.cling.entity.ClingResponse;
import com.nesp.android.cling.entity.DeviceInfoBean;
import com.nesp.android.cling.entity.IControlPoint;
import com.nesp.android.cling.entity.IDevice;
import com.nesp.android.cling.entity.PlayStatusBean;
import com.nesp.android.cling.entity.SWDevice;
import com.nesp.android.cling.entity.SWDeviceList;
import com.nesp.android.cling.entity.SelectSWDeviceBean;
import com.nesp.android.cling.entity.SlaveBean;
import com.nesp.android.cling.entity.SwanRomDownloadStatusResultBean;
import com.nesp.android.cling.service.manager.SWDeviceManager;
import com.nesp.android.cling.service.manager.SWWiFiSetupManager;

import org.json.JSONException;
import org.json.JSONObject;
import org.teleal.cling.controlpoint.ControlPoint;
import org.teleal.cling.model.action.ActionArgumentValue;
import org.teleal.cling.model.action.ActionInvocation;
import org.teleal.cling.model.message.UpnpResponse;
import org.teleal.cling.model.meta.Device;
import org.teleal.cling.model.meta.Service;
import org.teleal.cling.model.types.ServiceId;
import org.teleal.cling.model.types.ServiceType;
import org.teleal.cling.support.avtransport.callback.GetPositionInfo;
import org.teleal.cling.support.model.PositionInfo;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

/**
 * 说明：Cling 库使用工具类
 * <p>
 * 日期：17/7/4 10:27
 */

public class SWDeviceUtils {


    /**
     * 通过 ServiceType 获取已选择设备的服务
     *
     * @param serviceType 服务类型
     * @return 服务
     */
    @Nullable
    public static Service findServiceFromSelectedDevice(ServiceType serviceType) {
        IDevice selectedDevice = SWDeviceManager.getInstance().getSelectedDevice();
        if (Utils.isNull(selectedDevice)) {
            return null;
        }

        Device device = (Device) selectedDevice.getDevice();
        return device.findService(serviceType);
    }

    /**
     * 通过 serviceId 获取已选择设备的服务
     *
     * @return 服务
     */
    @Nullable
    public static Service findServiceFromSelectedDevice(ServiceId serviceId) {
        IDevice selectedDevice = SWDeviceManager.getInstance().getSelectedDevice();
        if (Utils.isNull(selectedDevice)) {
            return null;
        }

        Device device = (Device) selectedDevice.getDevice();
        return device.findService(serviceId);
    }


    /**
     * 获取 device 的 avt 服务
     *
     * @param device 设备
     * @return 服务
     */
    @Nullable
    public static Service findAVTServiceByDevice(Device device) {
        return device.findService(SWDeviceManager.AV_TRANSPORT_SERVICE);
    }

    @Nullable
    public static Service findRenderingControlServiceByDevice(Device device) {
        return device.findService(SWDeviceManager.RENDERING_CONTROL_SERVICE);
    }

    /**
     * 获取控制点
     *
     * @return 控制点
     */
    @Nullable
    public static ControlPoint getControlPoint() {
        IControlPoint controlPoint = SWDeviceManager.getInstance().getControlPoint();
        if (Utils.isNull(controlPoint)) {
            return null;
        }

        return (ControlPoint) controlPoint.getControlPoint();
    }

    public static void getDevicePositionInfo(Device device, final ControlReceiveCallback callback) {

        final Service avtService = SWDeviceUtils.findAVTServiceByDevice(device);
        if (Utils.isNull(avtService)) {
            return;
        }

        Log.d("test", "SWPlayControl.getPositionInfo:Found media render service in device, sending get position");

        GetPositionInfo getPositionInfo = new GetPositionInfo(avtService) {
            @Override
            public void failure(ActionInvocation invocation, UpnpResponse operation, String defaultMsg) {
                Log.d("test", "SWPlayControl.failure:defaultMsg " + defaultMsg);
                if (Utils.isNotNull(callback)) {
                    callback.fail(new ClingPositionResponse(invocation, operation, defaultMsg));
                }
            }

            @Override
            public void success(ActionInvocation invocation) {
                super.success(invocation);
                if (Utils.isNotNull(callback)) {
                    callback.success(new ClingPositionResponse(invocation));
                }
            }

            @Override
            public void received(ActionInvocation invocation, PositionInfo info) {
                Log.d("test", "SWPlayControl.received:info " + info);
                if (Utils.isNotNull(callback)) {
                    callback.receive(new ClingPositionResponse(invocation, info));
                }
            }
        };

        ControlPoint controlPointImpl = SWDeviceUtils.getControlPoint();
        if (Utils.isNull(controlPointImpl)) {
            return;
        }

        controlPointImpl.execute(getPositionInfo);
    }

    public static void getSlaveList(String deviceIp, GetSlaveListCallback callback) {
        //获取子设备列表
        OkHttp3Util.doGet("http://" + deviceIp + "/httpapi.asp?command=multiroom:getSlaveList", new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e("test", "slavef:" + e.getMessage());
                callback.onFailure("onFailure");
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                SlaveBean slaveBean = null;
                try {
                    slaveBean = new Gson().fromJson(response.body().string(), SlaveBean.class);
                    Log.e("test", "slaveBean.getSlave_list().size():" + slaveBean.getSlave_list().size());
                    if (slaveBean == null || slaveBean.getSlave_list().size() == 0) {
                        callback.onFailure("slaveBean.getSlave_list().size() == 0");
                    } else {
                        callback.onResponse(slaveBean.getSlave_list());
                    }
                } catch (Exception e) {
                    callback.onFailure(e.getMessage());
                }

            }
        });
    }

    public static void setDeviceName(String deviceIp, String name, BaseCallback callback) {
        //设置设备别名
        OkHttp3Util.doGet("http://" + deviceIp + "/httpapi.asp?command=setDeviceName:" + name, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                callback.onFailure(e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                callback.onResponse("success");
            }
        });
    }

    public static void deviceRestoreToDefault(String masterIp, BaseCallback callback) {
        //恢复出厂设置
        OkHttp3Util.doGet("http://" + masterIp + "/httpapi.asp?command=restoreToDefault", new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                callback.onFailure(e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                callback.onResponse("success");
            }
        });
    }

    public static void slaveListKicIn(Activity activity, SWDevice masterDevice, List<SelectSWDeviceBean> slaveList, BaseCallback callback) {
        //同步多台设备
        for (SelectSWDeviceBean swDevice : slaveList) {
            SWDeviceUtils.slaveKicIn(masterDevice.getSwDeviceInfo().getSWDeviceStatus().getApcli0(), swDevice.getLpDeviceIp(), masterDevice.
                            getSwDeviceInfo().getSWDeviceStatus().getSsid(), masterDevice.getSwDeviceInfo().getSWDeviceStatus().getWifiChannel(), masterDevice.
                            getSwDeviceInfo().getSWDeviceStatus().getEth2(), masterDevice.getSwDeviceInfo().getSWDeviceStatus().getUpnp_uuid(),
                    new SWDeviceUtils.BaseCallback() {
                        @Override
                        public void onResponse(String result) {
                            Log.e("test", "同步onSuccess");
                            activity.runOnUiThread(() -> {
                                CountDownTimer countDownTimer = new CountDownTimer(120000, 2000) {
                                    @Override
                                    public void onTick(long millisUntilFinished) {
                                        SWDeviceUtils.getSlaveList(masterDevice.getIp(), new SWDeviceUtils.GetSlaveListCallback() {
                                            @Override
                                            public void onResponse(List<SlaveBean.SlaveListDTO> slaveListDTOList) {
                                                if (slaveListDTOList.size() != 0) {
                                                    int needcount = slaveList.size();
                                                    Log.e("test", "addCount:start:" + needcount + "offsize:" + SWDeviceManager.getInstance().offLineDeviceList.size());
                                                    for (SelectSWDeviceBean device : slaveList) {
                                                        for (SlaveBean.SlaveListDTO slaveListDTO : slaveListDTOList) {
                                                            if (device.getSsid().equals(slaveListDTO.getSsid())) {
                                                                SWDevice offlineDevice = deviceForSsid(device.getSsid());
                                                                if(offlineDevice != null) {
                                                                    SWDeviceManager.getInstance().offLineDeviceList.add(offlineDevice);
                                                                    Log.e("test", "addCount:offLineDeviceList.add:" +
                                                                            "offsize:" + SWDeviceManager.getInstance().offLineDeviceList.size());
                                                                }
                                                                needcount--;
                                                            }
                                                        }
                                                        Log.e("test", "addCount:devicessid:" + device.getSsid() + " count:" + needcount
                                                                + "offsize:" + SWDeviceManager.getInstance().offLineDeviceList.size());
                                                    }
                                                    boolean NotContainsActionList = masterDeviceListNotContainsActionList(slaveList);
                                                    Log.e("test", "addCount:NotContainsActionList:" + NotContainsActionList + " " +
                                                            "size:" + SWDeviceManager.getInstance().getMasterDeviceList().size());
                                                    if (needcount == 0 && NotContainsActionList) {
                                                        Log.e("test", "addCount:" + needcount);
                                                        callback.onResponse("success");
                                                        cancel();
                                                    }
                                                }
                                            }

                                            @Override
                                            public void onFailure(String msg) {

                                            }
                                        });
                                    }

                                    @Override
                                    public void onFinish() {
                                        callback.onResponse("success");
                                    }
                                };
                                countDownTimer.start();
                            });

                        }

                        @Override
                        public void onFailure(String msg) {
                            callback.onFailure(msg);
                        }
                    });

        }
    }

    public static SWDevice deviceForSsid(String lpDevicessid) {
        Log.e("test", "deviceForSsid:" + lpDevicessid);
        for (SWDevice lpDevice : SWDeviceList.masterDevices) {
            Log.e("test", "deviceForSsid:" + lpDevicessid + " itemssid:" + lpDevice.getSwDeviceInfo().getSWDeviceStatus().getSsid());
            if (lpDevice.getSwDeviceInfo().getSWDeviceStatus().getSsid().trim().equals(lpDevicessid.trim())) {
                return lpDevice;
            }
        }
        return null;
    }

    public static boolean masterDeviceListNotContainsActionList(List<SelectSWDeviceBean> slaveList) {
        boolean result = true;
        for(SWDevice swDevice : SWDeviceManager.getInstance().getMasterDeviceList()){
            for(SelectSWDeviceBean selectSWDeviceBean : slaveList){
                if(selectSWDeviceBean.getSsid().equals(swDevice.getSwDeviceInfo().getSsid())){
                    return false;
                }
            }
        }
        return result;
    }

    public static void slaveListKicOut(Activity activity,SWDevice masterDevice, List<SelectSWDeviceBean> swDeviceList, BaseCallback callback) {
        //解绑多台设备
        for (SelectSWDeviceBean selectDevice : swDeviceList) {
            SWDeviceUtils.slaveKicOut( masterDevice.getSwDeviceInfo().getSWDeviceStatus()
                    .getApcli0(),selectDevice.getLpDeviceIp(), new SWDeviceUtils.BaseCallback() {
                @Override
                public void onResponse(String result) {
                    activity.runOnUiThread(() -> {
                        CountDownTimer countDownTimer = new CountDownTimer(60000, 1000) {
                            @Override
                            public void onTick(long millisUntilFinished) {
                                if (masterDeviceListContainsAllActionList(swDeviceList)) {
                                    callback.onResponse("success");
                                    cancel();
                                }
                            }

                            @Override
                            public void onFinish() {
                                callback.onResponse("success");
                            }
                        };
                        countDownTimer.start();
                    });
                }

                @Override
                public void onFailure(String msg) {
                    Log.e("test","slaveKicOutf:" + msg);
                    callback.onFailure(msg);
                }
            });
        }
    }

    private static boolean masterDeviceListContainsAllActionList(List<SelectSWDeviceBean> swDeviceList) {
        int size = swDeviceList.size();
        for(SelectSWDeviceBean selectSWDeviceBean : swDeviceList) {
            for (SWDevice swDevice : SWDeviceManager.getInstance().getMasterDeviceList()) {
                if (swDevice.getSwDeviceInfo().getSsid().equals(selectSWDeviceBean.getSsid())){
                    size--;
                }
            }
        }
        return size == 0;
    }


    public static void slaveKicIn(String masterIp, String slaveIp, String masterSsid, String masterWifiChannel, String masterEth2, String masterUuid, BaseCallback callback) {
        //同步设备
        OkHttp3Util.doGet("http://" + slaveIp + "/httpapi.asp?command=ConnectMasterAp:ssid="
                + Utils.strTo16(masterSsid) + ":ch=" +
                masterWifiChannel + ":auth=OPEN:encry=NONE:pwd=:chext=0:JoinGroupMaster:eth"
                + masterEth2 + ":wifi" + masterIp
                + ":uuid" + masterUuid, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                callback.onFailure(e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                callback.onResponse("success");
            }
        });
    }


    public static void slaveKicOut(String deviceIp, String slaveip, BaseCallback callback) {
        //解绑设备
        OkHttp3Util.doGet("http://" + deviceIp + "/httpapi.asp?command=multiroom:SlaveKickout:"
                + slaveip, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                callback.onFailure(e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                callback.onResponse("success");
            }
        });
    }

    public static void setSlaveDeviceVolume(String deviceIp, String slaveip, int volume, BaseCallback callback) {
        //设置子设备音量
        //volume 1-100
        OkHttp3Util.doGet("http://" + deviceIp + "/httpapi.asp?command=multiroom:SlaveVolume:src_ip:" + slaveip + ":" + volume, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                callback.onFailure(e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                callback.onResponse("success");
            }
        });
    }

    public static void setSlaveDeviceChannel(String deviceIp, String slaveip, int channel, BaseCallback callback) {
        //设置子设备声道
        //channel 0 正常播放，1左声道，2右声道
        OkHttp3Util.doGet("http://" + deviceIp + "/httpapi.asp?command=multiroom:SlaveChannel:src_ip:" + slaveip + ":" + channel, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                callback.onFailure(e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                callback.onResponse("success");
            }
        });
    }

    public static void setMasterDeviceChannel(String deviceIp, int channel, BaseCallback callback) {
        //设置主设备声道
        //channel 0 正常播放，1左声道，2右声道
        OkHttp3Util.doGet("http://" + deviceIp + "/httpapi.asp?command=setPlayerCmd:slave_channel:" + channel, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                callback.onFailure(e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                callback.onResponse("success");
            }
        });
    }

    public static void getControlDeviceInfo(Device device, @Nullable ControlCallback callback) {
        //获取设备信息
        final Service avtService = SWDeviceUtils.findRenderingControlServiceByDevice(device);
        if (Utils.isNull(avtService)) {
            return;
        }
        GetControlDeviceInfo getControlDeviceInfo = new GetControlDeviceInfo(avtService, device.getIdentity().getUdn().toString()) {
            /**
             * Called when the action invocation succeeded.
             *
             * @param invocation The successful invocation, call its <code>getOutput()</code> method for results.
             */
            @Override
            public void success(ActionInvocation invocation) {
                if (callback != null) {
                    Map<String, ActionArgumentValue> map = actionInvocation.getOutputMap();
                    callback.success(new ClingGetControlDeviceInfoResponse(actionInvocation, map));
                }
            }

            @Override
            public void failure(ActionInvocation invocation, UpnpResponse operation, String defaultMsg) {
                if (Utils.isNotNull(callback)) {
                    callback.fail(new ClingResponse(invocation, operation, defaultMsg));
                }
            }
        };

        ControlPoint controlPointImpl = SWDeviceUtils.getControlPoint();
        if (Utils.isNull(controlPointImpl)) {
            return;
        }

        controlPointImpl.execute(getControlDeviceInfo);
    }

    public static void getDeviceInfo(String deviceIp, GetDeviceInfoCallback callback) {
        //http方式获取设备信息
        OkHttp3Util.doGet("http://" + deviceIp + "/httpapi.asp?command=getStatusEx", new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e("test", "NsdManagerongetDeviceInfoonFailure:" + e.getMessage());
                callback.onFailure(e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                try {
                    String result = response.body().string();
                    Log.e("test", "NsdManagerongetDeviceInfo:" + result);
                    DeviceInfoBean deviceInfoBean = new Gson().fromJson(result, DeviceInfoBean.class);
                    callback.onResponse(deviceInfoBean);
                } catch (Exception e) {
                    callback.onFailure(e.getMessage());
                    Log.e("test", "NsdManagerongetDeviceInfoException:" + e.getMessage());
                }
            }
        });
    }

    public static void getDevicePlayerStatus(String deviceIp, GetDevicePlayerStatusCallback callback) {
        //http方式获取设备播放状态
        OkHttp3Util.doGet("http://" + deviceIp + "/httpapi.asp?command=getPlayerStatus", new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e("test", "NsdManagerongetDevicePlayStatusonFailure:" + e.getMessage());
                callback.onFailure(e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                try {
                    String result = response.body().string();
                    PlayStatusBean deviceInfoBean = new Gson().fromJson(result, PlayStatusBean.class);
                    Log.e("test", "NsdManagerongetDevicePlayStatus:" + result);
                    callback.onResponse(deviceInfoBean);
                } catch (Exception e) {
                    callback.onFailure(e.getMessage());
                    Log.e("test", "NsdManageronggetDevicePlayStatusException:" + e.getMessage());
                }
            }
        });
    }


    public static void remoteUpdate(String deviceIp, String url, BaseCallback callback) {
        //固件更新
        OkHttp3Util.doGet("http://" + deviceIp + "/httpapi.asp?command=SetRemoteUpdateUrl:" + url, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                callback.onFailure(e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                callback.onResponse("success");
            }
        });
    }

    public static void getRemoteUpdateDownloadStatus(String deviceIp, GetRemoteUpdateDownloadStatusCallback callback) {
        //固件更新，下载进度查询
        OkHttp3Util.doGet("http://" + deviceIp + "/httpapi.asp?command=getMvRomDownloadStatus", new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                callback.onFailure(e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String result = new String(response.body().bytes());
                Log.e("test", "getMvRomDownloadStatus:" + result);
                try {
                    JSONObject jsonObject = new JSONObject(result);
                    SwanRomDownloadStatusResultBean bean = new SwanRomDownloadStatusResultBean();
                    bean.setStatus(jsonObject.getInt("status"));
                    if (bean.getStatus() == 1) {
                        bean.setProgress(jsonObject.getInt("progress"));
                    } else if (bean.getStatus() == 3) {
                        int twsize = jsonObject.getInt("twsize");
                        int wsize = jsonObject.getInt("wsize");
                        bean.setProgress(wsize * 100 / twsize);
                    }
                    callback.onResponse(bean);
                } catch (JSONException e) {
                    e.printStackTrace();
                    callback.onFailure(e.getMessage());
                }
            }
        });
    }


    public interface GetRemoteUpdateDownloadStatusCallback {
        void onResponse(SwanRomDownloadStatusResultBean deviceInfoBean);

        void onFailure(String msg);
    }

    public interface GetDevicePlayerStatusCallback {
        void onResponse(PlayStatusBean deviceInfoBean);

        void onFailure(String msg);
    }

    public interface GetDeviceInfoCallback {
        void onResponse(DeviceInfoBean deviceInfoBean);

        void onFailure(String msg);
    }

    public interface GetSlaveListCallback {
        void onResponse(List<SlaveBean.SlaveListDTO> slaveListDTOList);

        void onFailure(String msg);
    }

    public interface BaseCallback {
        void onResponse(String result);

        void onFailure(String msg);
    }
}
