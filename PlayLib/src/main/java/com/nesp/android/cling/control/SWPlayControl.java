package com.nesp.android.cling.control;

import android.annotation.SuppressLint;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.Nullable;

import com.google.gson.Gson;
import com.nesp.android.cling.callback.AppendTracksInQueue;
import com.nesp.android.cling.callback.BrowseQueue;
import com.nesp.android.cling.callback.CreateQueue;
import com.nesp.android.cling.callback.GetQueueLoopMode;
import com.nesp.android.cling.callback.PlayQueueWithIndex;
import com.nesp.android.cling.callback.RemoveTracksInQueue;
import com.nesp.android.cling.callback.SetQueueLoopMode;
import com.nesp.android.cling.control.callback.ControlCallback;
import com.nesp.android.cling.control.callback.ControlReceiveCallback;
import com.nesp.android.cling.entity.*;
import com.nesp.android.cling.listener.LPDevicePlayerListener;
import com.nesp.android.cling.service.manager.SWDeviceManager;
import com.nesp.android.cling.util.SWDeviceUtils;
import com.nesp.android.cling.util.LPMSUtil;
import com.nesp.android.cling.util.LPXmlUtil;
import com.nesp.android.cling.util.Utils;

import org.teleal.cling.controlpoint.ControlPoint;
import org.teleal.cling.model.action.ActionArgumentValue;
import org.teleal.cling.model.action.ActionInvocation;
import org.teleal.cling.model.message.UpnpResponse;
import org.teleal.cling.model.meta.Device;
import org.teleal.cling.model.meta.Service;
import org.teleal.cling.model.types.ServiceId;
import org.teleal.cling.model.types.UnsignedIntegerFourBytes;
import org.teleal.cling.support.avtransport.callback.*;
import org.teleal.cling.support.model.DIDLObject;
import org.teleal.cling.support.model.MediaInfo;
import org.teleal.cling.support.model.PositionInfo;
import org.teleal.cling.support.model.ProtocolInfo;
import org.teleal.cling.support.model.Res;
import org.teleal.cling.support.model.TransportInfo;
import org.teleal.cling.support.model.item.MusicTrack;
import org.teleal.cling.support.renderingcontrol.callback.GetVolume;
import org.teleal.cling.support.renderingcontrol.callback.SetMute;
import org.teleal.cling.support.renderingcontrol.callback.SetVolume;
import org.json.JSONArray;
import org.json.JSONObject;
import org.teleal.common.util.MimeType;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import fr.arnaudguyon.xmltojsonlib.XmlToJson;



public class SWPlayControl implements IPlayControl {

    private static final String TAG = SWPlayControl.class.getSimpleName();
    /**
     * 每次接收 500ms 延迟
     */
    private static final int RECEIVE_DELAY = 500;
    /**
     * 上次设置音量时间戳, 防抖动
     */
    private long mVolumeLastTime;
    /**
     * 当前状态
     */
    private @DLANPlayState.DLANPlayStates
    int mCurrentState = DLANPlayState.STOP;
    private static final String DIDL_LITE_FOOTER = "</DIDL-Lite>";
    private static final String DIDL_LITE_HEADER = "<?xml version=\"1.0\"?>" + "<DIDL-Lite " + "xmlns=\"urn:schemas-upnp-org:metadata-1-0/DIDL-Lite/\" " +
            "xmlns:dc=\"http://purl.org/dc/elements/1.1/\" " + "xmlns:upnp=\"urn:schemas-upnp-org:metadata-1-0/upnp/\" " +
            "xmlns:dlna=\"urn:schemas-dlna-org:metadata-1-0/\">";

    /**
     * 播放歌曲
     *
     * @param callback
     */
    @Override
    public void playAudio(List<LPPlayItem> lpPlayItemList, @Nullable ControlCallback callback) {
        stop(new ControlCallback() { // 1、 停止当前播放视频
            @Override
            public void success(IResponse response) {
                Log.e(TAG, "playAudio createQueue");
                createQueue(SWPlayControl.getMediaData(0, lpPlayItemList), new LPDevicePlayerListener() {
                    @Override
                    public void onSuccess(String var1) {
                        if (Utils.isNotNull(callback)) {
                            callback.success(response);
                        }
                    }

                    @Override
                    public void onFailure(Exception var1) {
                        if (Utils.isNotNull(callback)) {
                            callback.fail(response);
                        }
                    }
                });
            }

            @Override
            public void fail(IResponse response) {
                if (Utils.isNotNull(callback)) {
                    callback.fail(response);
                }
            }
        });
    }

    /**
     * 播放歌曲
     *
     * @param callback
     */
    public void playOneAudio(LPPlayItem lpPlayItem, @Nullable ControlCallback callback) {
        List<LPPlayItem> playItemList = new ArrayList<>();
        playItemList.add(lpPlayItem);
        stop(new ControlCallback() { // 1、 停止当前播放视频
            @Override
            public void success(IResponse response) {
                createQueue(SWPlayControl.getMediaData(0, playItemList), new LPDevicePlayerListener() {
                    @Override
                    public void onSuccess(String var1) {
                        if (Utils.isNotNull(callback)) {
                            callback.success(response);
                        }
                    }

                    @Override
                    public void onFailure(Exception var1) {
                        if (Utils.isNotNull(callback)) {
                            callback.fail(response);
                        }
                    }
                });
            }

            @Override
            public void fail(IResponse response) {
                if (Utils.isNotNull(callback)) {
                    callback.fail(response);
                }
            }
        });
    }



    private void playQueueWithIndex(ControlPoint controlPointImpl, LPPlayInfoItem statusItem, Service avtService, final LPDevicePlayerListener listener) {
        try {
            if (statusItem.getMediaSource().equals(LPPlaySourceType.LP_LOCALMUSIC)) {
                statusItem.setPlayIndex(0);
            }

            UnsignedIntegerFourBytes var5;
            var5 = new UnsignedIntegerFourBytes((long) statusItem.getPlayIndex());
            controlPointImpl.execute(new PlayQueueWithIndex(avtService, statusItem.getPlaylistName(), var5) {
                public void failure(ActionInvocation arg0, UpnpResponse arg1, String defaultMsg) {
                    Log.e(TAG, "playAudio playQueueWithIndex execute failure：" + defaultMsg);
                    if (Utils.isNotNull(listener)) {
                        listener.onFailure(new Exception(defaultMsg));
                    }
                }

                public void success(ActionInvocation invocation) {
                    Log.e(TAG, "playAudio playQueueWithIndex execute success:" + invocation.getOutputMap().toString());
                    if (Utils.isNotNull(listener)) {
                        listener.onSuccess(invocation.getOutputMap().toString());
                    }

                }
            });

        } catch (Exception var5) {
            if (listener != null) {
                listener.onFailure(var5);
            }
        }

    }

    /**
     * 播放某一个序号的播放列表里的歌曲
     * @param index
     * @param listener
     */
    public void playQueueWithIndex(int index, final LPDevicePlayerListener listener) {

        final Service avtService = SWDeviceUtils.findServiceFromSelectedDevice(ServiceId.valueOf("urn:wiimu-com:serviceId:PlayQueue"));
        if (Utils.isNull(avtService)) {
            return;
        }

        final ControlPoint controlPointImpl = SWDeviceUtils.getControlPoint();
        if (Utils.isNull(controlPointImpl)) {
            return;
        }
        try {
            UnsignedIntegerFourBytes var5;
            var5 = new UnsignedIntegerFourBytes((long) index);
            controlPointImpl.execute(new PlayQueueWithIndex(avtService, LPPlayQueueType.LP_CURRENT_QUEUE.getValue(), var5) {
                public void failure(ActionInvocation arg0, UpnpResponse arg1, String defaultMsg) {
                    Log.e(TAG, "playAudio playQueueWithIndex execute failure：" + defaultMsg);
                    if (Utils.isNotNull(listener)) {
                        listener.onFailure(new Exception(defaultMsg));
                    }
                }

                public void success(ActionInvocation invocation) {
                    Log.e(TAG, "playAudio playQueueWithIndex execute success:" + invocation.getOutputMap().toString());
                    if (Utils.isNotNull(listener)) {
                        listener.onSuccess(invocation.getOutputMap().toString());
                    }

                }
            });

        } catch (Exception var5) {
            if (listener != null) {
                listener.onFailure(var5);
            }
        }

    }


    private void appendTracksInQueue(ControlPoint controlPointImpl, String queueContext, Service avtService, LPDevicePlayerListener listener) {
        controlPointImpl.execute(new AppendTracksInQueue(avtService, queueContext) {
            public void failure(ActionInvocation arg0, UpnpResponse arg1, String arg2) {
                super.failure(arg0, arg1, arg2);
                Log.e(TAG, "appendTracksInQueuefailure:" + arg2);
                if (Utils.isNotNull(listener)) {
                    listener.onFailure(new Exception(arg2));
                }

            }

            public void success(ActionInvocation arg0) {
                Log.e(TAG, "appendTracksInQueuesuccess:");
                super.success(arg0);
                if (Utils.isNotNull(listener)) {
                    listener.onSuccess(arg0.toString());
                }

            }
        });
    }

    int appendTracksInQueueIndex = 1;
    private void createQueue(String playData, LPDevicePlayerListener listener) {

        try {
            if (TextUtils.isEmpty(playData)) {
                if (listener != null) {
                    listener.onFailure(new Exception("play media data empty"));
                }

                return;
            }

            final LPPlayInfoItem var4 = (LPPlayInfoItem) (new Gson()).fromJson(playData, LPPlayInfoItem.class);
            if (var4 == null) {
                if (listener != null) {
                    listener.onFailure(new Exception("play data empty"));
                }

                return;
            }

            final List<String> var5 = var4.getPlayDataList();
            if (var5 == null || var5.size() == 0) {
                if (listener != null) {
                    listener.onFailure(new Exception("play media data empty"));
                }

                return;
            }

            final Service avtService = SWDeviceUtils.findServiceFromSelectedDevice(ServiceId.valueOf("urn:wiimu-com:serviceId:PlayQueue"));
            if (Utils.isNull(avtService)) {
                return;
            }

            final ControlPoint controlPointImpl = SWDeviceUtils.getControlPoint();
            if (Utils.isNull(controlPointImpl)) {
                return;
            }
            Log.e(TAG, "playAudio createQueue execute:" + (String) var5.get(0));
            controlPointImpl.execute(new CreateQueue(avtService, (String) var5.get(0)) {

                @Override
                public void success(ActionInvocation invocation) {
                    super.success(invocation);
                    Log.e(TAG, "playAudio createQueue execute success:" + invocation.getOutputMap().toString());
                    if (Utils.isNotNull(listener)) {
                        playQueueWithIndex(controlPointImpl, var4, avtService, listener);
                        if (var4.getAppendCount() >= 1 && var5.size() >= 1) {
                            appendTracksInQueueIndex = 1;
                            LPDevicePlayerListener lpDevicePlayerListener = new LPDevicePlayerListener() {
                                @Override
                                public void onSuccess(String var1) {
                                    appendTracksInQueueIndex++;
                                    if(appendTracksInQueueIndex < var5.size()){
                                        appendTracksInQueue(controlPointImpl, (String) var5.get(appendTracksInQueueIndex), avtService, this);
                                    }
                                }

                                @Override
                                public void onFailure(Exception var1) {
                                    appendTracksInQueueIndex++;
                                    if(appendTracksInQueueIndex < var5.size()){
                                        appendTracksInQueue(controlPointImpl, (String) var5.get(appendTracksInQueueIndex), avtService, this);
                                    }
                                }
                            };
                            appendTracksInQueue(controlPointImpl, (String) var5.get(appendTracksInQueueIndex), avtService, lpDevicePlayerListener);
                        }
                    }
                }

                @Override
                public void failure(ActionInvocation invocation, UpnpResponse operation, String defaultMsg) {
                    Log.e(TAG, "playAudio createQueue execute failure：" + defaultMsg + " state:" + avtService.getServiceType());
                    if (listener != null) {
                        listener.onFailure(new Exception(defaultMsg));
                    }

                }
            });

        } catch (Exception var6) {
            if (listener != null) {
                listener.onFailure(var6);
            }
        }

    }


    @Override
    public void play(final ControlCallback callback) {
        final Service avtService = SWDeviceUtils.findServiceFromSelectedDevice(SWDeviceManager.AV_TRANSPORT_SERVICE);
        if (Utils.isNull(avtService)) {
            return;
        }

        final ControlPoint controlPointImpl = SWDeviceUtils.getControlPoint();
        if (Utils.isNull(controlPointImpl)) {
            return;
        }

        controlPointImpl.execute(new Play(avtService) {

            @Override
            public void success(ActionInvocation invocation) {
                super.success(invocation);
                if (Utils.isNotNull(callback)) {
                    callback.success(new ClingResponse(invocation));
                }
            }

            @Override
            public void failure(ActionInvocation invocation, UpnpResponse operation, String defaultMsg) {
                if (Utils.isNotNull(callback)) {
                    callback.fail(new ClingResponse(invocation, operation, defaultMsg));
                }
            }
        });
    }

    @Override
    public void pause(final ControlCallback callback) {
        final Service avtService = SWDeviceUtils.findServiceFromSelectedDevice(SWDeviceManager.AV_TRANSPORT_SERVICE);
        if (Utils.isNull(avtService)) {
            return;
        }

        final ControlPoint controlPointImpl = SWDeviceUtils.getControlPoint();
        if (Utils.isNull(controlPointImpl)) {
            return;
        }

        controlPointImpl.execute(new Pause(avtService) {

            @Override
            public void success(ActionInvocation invocation) {
                super.success(invocation);
                if (Utils.isNotNull(callback)) {
                    callback.success(new ClingResponse(invocation));
                }
            }

            @Override
            public void failure(ActionInvocation invocation, UpnpResponse operation, String defaultMsg) {
                if (Utils.isNotNull(callback)) {
                    callback.fail(new ClingResponse(invocation, operation, defaultMsg));
                }
            }
        });
    }

    @Override
    public void stop(final ControlCallback callback) {
        final Service avtService = SWDeviceUtils.findServiceFromSelectedDevice(SWDeviceManager.AV_TRANSPORT_SERVICE);
        if (Utils.isNull(avtService)) {
            return;
        }

        final ControlPoint controlPointImpl = SWDeviceUtils.getControlPoint();
        if (Utils.isNull(controlPointImpl)) {
            return;
        }

        controlPointImpl.execute(new Stop(avtService) {

            @Override
            public void success(ActionInvocation invocation) {
                super.success(invocation);
                if (Utils.isNotNull(callback)) {
                    callback.success(new ClingResponse(invocation));
                }
            }

            @Override
            public void failure(ActionInvocation invocation, UpnpResponse operation, String defaultMsg) {
                if (Utils.isNotNull(callback)) {
                    callback.fail(new ClingResponse(invocation, operation, defaultMsg));
                }
            }
        });
    }

    /**
     * 下一首
     *
     * @param callback
     */
    @Override
    public void next(@Nullable ControlCallback callback) {
        final Service avtService = SWDeviceUtils.findServiceFromSelectedDevice(SWDeviceManager.AV_TRANSPORT_SERVICE);
        if (Utils.isNull(avtService)) {
            return;
        }

        final ControlPoint controlPointImpl = SWDeviceUtils.getControlPoint();
        if (Utils.isNull(controlPointImpl)) {
            return;
        }

        controlPointImpl.execute(new Next(avtService) {

            @Override
            public void success(ActionInvocation invocation) {
                super.success(invocation);
                if (Utils.isNotNull(callback)) {
                    callback.success(new ClingResponse(invocation));
                }
            }

            @Override
            public void failure(ActionInvocation invocation, UpnpResponse operation, String defaultMsg) {
                if (Utils.isNotNull(callback)) {
                    callback.fail(new ClingResponse(invocation, operation, defaultMsg));
                }
            }
        });
    }

    /**
     * 上一首
     *
     * @param callback
     */
    @Override
    public void previous(@Nullable ControlCallback callback) {
        final Service avtService = SWDeviceUtils.findServiceFromSelectedDevice(SWDeviceManager.AV_TRANSPORT_SERVICE);
        if (Utils.isNull(avtService)) {
            return;
        }

        final ControlPoint controlPointImpl = SWDeviceUtils.getControlPoint();
        if (Utils.isNull(controlPointImpl)) {
            return;
        }

        controlPointImpl.execute(new Previous(avtService) {

            @Override
            public void success(ActionInvocation invocation) {
                super.success(invocation);
                if (Utils.isNotNull(callback)) {
                    callback.success(new ClingResponse(invocation));
                }
            }

            @Override
            public void failure(ActionInvocation invocation, UpnpResponse operation, String defaultMsg) {
                if (Utils.isNotNull(callback)) {
                    callback.fail(new ClingResponse(invocation, operation, defaultMsg));
                }
            }
        });
    }


    @Override
    public void seek(int currentProgressPercent, final ControlCallback callback) {
        final Service avtService = SWDeviceUtils.findServiceFromSelectedDevice(SWDeviceManager.AV_TRANSPORT_SERVICE);
        if (Utils.isNull(avtService)) {
            return;
        }

        final ControlPoint controlPointImpl = SWDeviceUtils.getControlPoint();
        if (Utils.isNull(controlPointImpl)) {
            return;
        }

        String time = Utils.getStringTime2(currentProgressPercent);
        Log.e(TAG, "seek->pos: " + currentProgressPercent + ", time: " + time);
        controlPointImpl.execute(new Seek(avtService, time) {

            @Override
            public void success(ActionInvocation invocation) {
                super.success(invocation);
                if (Utils.isNotNull(callback)) {
                    callback.success(new ClingResponse(invocation));
                }
            }

            @Override
            public void failure(ActionInvocation invocation, UpnpResponse operation, String defaultMsg) {
                if (Utils.isNotNull(callback)) {
                    callback.fail(new ClingResponse(invocation, operation, defaultMsg));
                }
            }
        });
    }

    @Override
    public void setVolume(int pos, @Nullable final ControlCallback callback) {
        final Service rcService = SWDeviceUtils.findServiceFromSelectedDevice(SWDeviceManager.RENDERING_CONTROL_SERVICE);
        if (Utils.isNull(rcService)) {
            return;
        }

        final ControlPoint controlPointImpl = SWDeviceUtils.getControlPoint();
        if (Utils.isNull(controlPointImpl)) {
            return;
        }
        long currentTimeMillis = System.currentTimeMillis();
        if (currentTimeMillis > mVolumeLastTime + RECEIVE_DELAY) {
            controlPointImpl.execute(new SetVolume(rcService, pos) {

                @Override
                public void success(ActionInvocation invocation) {
                    if (Utils.isNotNull(callback)) {
                        callback.success(new ClingResponse(invocation));
                    }
                }

                @Override
                public void failure(ActionInvocation invocation, UpnpResponse operation, String defaultMsg) {
                    if (Utils.isNotNull(callback)) {
                        callback.fail(new ClingResponse(invocation, operation, defaultMsg));
                    }
                }
            });
        }
        mVolumeLastTime = currentTimeMillis;
    }

    @Override
    public void setMute(boolean desiredMute, @Nullable final ControlCallback callback) {
        final Service rcService = SWDeviceUtils.findServiceFromSelectedDevice(SWDeviceManager.RENDERING_CONTROL_SERVICE);
        if (Utils.isNull(rcService)) {
            return;
        }

        final ControlPoint controlPointImpl = SWDeviceUtils.getControlPoint();
        if (Utils.isNull(controlPointImpl)) {
            return;
        }

        controlPointImpl.execute(new SetMute(rcService, desiredMute) {

            @Override
            public void success(ActionInvocation invocation) {
                if (Utils.isNotNull(callback)) {
                    callback.success(new ClingResponse(invocation));
                }
            }

            @Override
            public void failure(ActionInvocation invocation, UpnpResponse operation, String defaultMsg) {
                if (Utils.isNotNull(callback)) {
                    callback.fail(new ClingResponse(invocation, operation, defaultMsg));
                }
            }
        });
    }


    @Override
    public void getMediaInfo(@Nullable ControlReceiveCallback callback) {
        final Service avtService = SWDeviceUtils.findServiceFromSelectedDevice(SWDeviceManager.AV_TRANSPORT_SERVICE);
        if (Utils.isNull(avtService)) {
            return;
        }

        GetMediaInfo getMediaInfo = new GetMediaInfo(avtService) {
            @Override
            public void failure(ActionInvocation invocation, UpnpResponse operation, String defaultMsg) {
                Log.d(TAG, "SWPlayControl.failure:defaultMsg " + defaultMsg);
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
            public void received(ActionInvocation invocation, MediaInfo info) {
                Log.d(TAG, "SWPlayControl.received:info " + info);
                if (Utils.isNotNull(callback)) {
                    callback.receive(new ClingMediaResponse(invocation, info));
                }
            }
        };

        ControlPoint controlPointImpl = SWDeviceUtils.getControlPoint();
        if (Utils.isNull(controlPointImpl)) {
            return;
        }

        controlPointImpl.execute(getMediaInfo);
    }

    /**
     * 获取传输信息
     *
     * @param callback
     */
    @Override
    public void getTransportInfo(@Nullable ControlReceiveCallback callback) {
        final Service avtService = SWDeviceUtils.findServiceFromSelectedDevice(SWDeviceManager.AV_TRANSPORT_SERVICE);
        if (Utils.isNull(avtService)) {
            return;
        }

        GetTransportInfo getMediaInfo = new GetTransportInfo(avtService) {
            @Override
            public void failure(ActionInvocation invocation, UpnpResponse operation, String defaultMsg) {
                Log.d(TAG, "SWPlayControl.failure:defaultMsg " + defaultMsg);
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
            public void received(ActionInvocation invocation, TransportInfo info) {
                Log.d(TAG, "SWPlayControl.received:info " + info);
                if (Utils.isNotNull(callback)) {
                    callback.receive(new ClingTransportResponse(invocation, info));
                }
            }
        };

        ControlPoint controlPointImpl = SWDeviceUtils.getControlPoint();
        if (Utils.isNull(controlPointImpl)) {
            return;
        }

        controlPointImpl.execute(getMediaInfo);

    }

    /**
     * 设置播放模式
     *
     * @param callback
     */
    @Override
    public void setPlayMode(int playMode, @Nullable ControlCallback callback) {
        final Service rcService = SWDeviceUtils.findServiceFromSelectedDevice(ServiceId.valueOf("urn:wiimu-com:serviceId:PlayQueue"));
        if (Utils.isNull(rcService)) {
            return;
        }

        final ControlPoint controlPointImpl = SWDeviceUtils.getControlPoint();
        if (Utils.isNull(controlPointImpl)) {
            return;
        }

        controlPointImpl.execute(new SetQueueLoopMode(rcService, playMode) {

            @Override
            public void success(ActionInvocation invocation) {
                if (Utils.isNotNull(callback)) {
                    callback.success(new ClingResponse(invocation));
                }
            }

            @Override
            public void failure(ActionInvocation invocation, UpnpResponse operation, String defaultMsg) {
                if (Utils.isNotNull(callback)) {
                    callback.fail(new ClingResponse(invocation, operation, defaultMsg));
                }
            }
        });
    }

    @Override
    public void getPlayMode(final ControlReceiveCallback callback) {
        final Service avtService = SWDeviceUtils.findServiceFromSelectedDevice(ServiceId.valueOf("urn:wiimu-com:serviceId:PlayQueue"));
        if (Utils.isNull(avtService)) {
            return;
        }
        GetQueueLoopMode getQueueLoopMode = new GetQueueLoopMode(avtService) {
            /**
             * Called when the action invocation succeeded.
             *
             * @param invocation The successful invocation, call its <code>getOutput()</code> method for results.
             */
            @Override
            public void success(ActionInvocation invocation) {
                if (callback != null) {
                    Map var2;
                    ((ActionArgumentValue) (var2 = invocation.getOutputMap()).get("LoopMode")).getValue().toString();
                    callback.receive(new ClingPlayModeResponse(actionInvocation, var2));
                }
            }

            @Override
            public void failure(ActionInvocation invocation, UpnpResponse operation, String defaultMsg) {
                if (Utils.isNotNull(callback)) {
                    callback.fail(new ClingPlayModeResponse(invocation, operation, defaultMsg));
                }
            }
        };

        ControlPoint controlPointImpl = SWDeviceUtils.getControlPoint();
        if (Utils.isNull(controlPointImpl)) {
            return;
        }

        controlPointImpl.execute(getQueueLoopMode);
    }

    public void getPositionInfoByDevice(Device device,ControlReceiveCallback callback){
        final Service avtService = SWDeviceUtils.findAVTServiceByDevice(device);
        if (Utils.isNull(avtService)) {
            return;
        }

        Log.d(TAG, "SWPlayControl.getPositionInfo:Found media render service in device, sending get position");

        GetPositionInfo getPositionInfo = new GetPositionInfo(avtService) {
            @Override
            public void failure(ActionInvocation invocation, UpnpResponse operation, String defaultMsg) {
                Log.d(TAG, "SWPlayControl.failure:defaultMsg " + defaultMsg);
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
                Log.d(TAG, "SWPlayControl.received:info " + info);
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

    @Override
    public void getPositionInfo(final ControlReceiveCallback callback) {

        final Service avtService = SWDeviceUtils.findServiceFromSelectedDevice(SWDeviceManager.AV_TRANSPORT_SERVICE);
        if (Utils.isNull(avtService)) {
            return;
        }

        Log.d(TAG, "SWPlayControl.getPositionInfo:Found media render service in device, sending get position");

        GetPositionInfo getPositionInfo = new GetPositionInfo(avtService) {
            @Override
            public void failure(ActionInvocation invocation, UpnpResponse operation, String defaultMsg) {
                Log.d(TAG, "SWPlayControl.failure:defaultMsg " + defaultMsg);
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
                Log.d(TAG, "SWPlayControl.received:info " + info);
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

    @Override
    public void getVolume(final ControlReceiveCallback callback) {
        final Service avtService = SWDeviceUtils.findServiceFromSelectedDevice(SWDeviceManager.RENDERING_CONTROL_SERVICE);
        if (Utils.isNull(avtService)) {
            return;
        }
        GetVolume getVolume = new GetVolume(avtService) {
            @Override
            public void received(ActionInvocation actionInvocation, int currentVolume) {
                if (Utils.isNotNull(callback)) {
                    callback.receive(new ClingVolumeResponse(actionInvocation, currentVolume));
                }
            }

            @Override
            public void failure(ActionInvocation invocation, UpnpResponse operation, String defaultMsg) {
                if (Utils.isNotNull(callback)) {
                    callback.fail(new ClingVolumeResponse(invocation, operation, defaultMsg));
                }
            }
        };

        ControlPoint controlPointImpl = SWDeviceUtils.getControlPoint();
        if (Utils.isNull(controlPointImpl)) {
            return;
        }

        controlPointImpl.execute(getVolume);
    }

    /**
     * 获取当前播放列表
     *
     * @param callback
     */
    @Override
    public void getCurrentPlaylist(@Nullable GetCurrentPlaylistCallback callback) {
        final Service avtService = SWDeviceUtils.findServiceFromSelectedDevice(ServiceId.valueOf("urn:wiimu-com:serviceId:PlayQueue"));
        if (Utils.isNull(avtService)) {
            return;
        }
        BrowseQueue browseQueue = new BrowseQueue(avtService, BrowseQueue.BrowseQueueType.CurrentQueue) {
            @Override
            public void received(String var1, Object var2) {

            }

            /**
             * Called when the action invocation succeeded.
             *
             * @param invocation The successful invocation, call its <code>getOutput()</code> method for results.
             */
            @Override
            public void success(ActionInvocation invocation) {
                if (callback != null) {
                    Map<String, ActionArgumentValue> dataMap = invocation.getOutputMap();
                    if (dataMap != null && dataMap.size() != 0) {
                        PlayList playList = new PlayList();
                        String s = dataMap.get("QueueContext").toString();
                        Log.e("test", "getCurrentPlaylistsuccess： " + dataMap.toString());
                        LPPlayMusicList lpPlayMusicList = LPXmlUtil.convert2CurrentQueueItem(s);
                        Log.e("test", "getCurrentPlaylistsuccess1： " + (lpPlayMusicList.getList().size() == 0));
                        if (lpPlayMusicList.getList().size() == 0) {
                            Log.e("test", "getCurrentPlaylistsuccess2： " + s);
                            XmlToJson xmlToJson = new XmlToJson.Builder(s).build();
                            try {
                                JSONObject jsonObject = new JSONObject(xmlToJson.toString().replaceAll("\\\\", "").
                                        replaceAll("\"\\{", "\\{").replaceAll("\\}\"", "\\}"));
                                Log.i("test", "jsonObject: " + xmlToJson.toString().replaceAll("\\\\", "").
                                        replaceAll("\"\\{", "\\{").replaceAll("\\}\"", "\\}"));
                                playList.LastPlayIndex = Integer.parseInt(s.split("LastPlayIndex")[1].replace(">", "").replace("</", ""));
                                JSONObject tracks = jsonObject.getJSONObject("PlayList").getJSONObject("Tracks");
                                Iterator iterator = tracks.keys();
                                while (iterator.hasNext()) {
                                    String key = (String) iterator.next();
                                    JSONObject trackx = tracks.getJSONObject(key);
                                    JSONObject metadata = trackx.getJSONObject("Metadata");
                                    LPPlayItem track = new LPPlayItem();
                                    JSONArray trackURIs = metadata.getJSONArray("trackURIs");
                                    track.setTrackId(key.replace("Track", ""));
                                    track.setTrackUrl(trackURIs.get(0).toString());
                                    track.setTrackName(metadata.getString("title"));
                                    track.setTrackArtist(metadata.getString("creator"));
                                    playList.trackList.add(track);
                                }
                                Collections.sort(playList.trackList, new Comparator<LPPlayItem>() {
                                    public int compare(LPPlayItem o1, LPPlayItem o2) {
                                        return Integer.parseInt(o1.getTrackId()) - Integer.parseInt(o2.getTrackId());
                                    }
                                });
                                playList.TrackNumber = playList.trackList.size();

                            } catch (Exception e) {
                                Log.e("test", "getCurrentPlaylistsuccessf: " + e.getMessage());
                            }
                        } else {
                            playList.TrackNumber = lpPlayMusicList.getList().size();
                            playList.LastPlayIndex = lpPlayMusicList.getIndex();
                            playList.trackList = lpPlayMusicList.getList();
                            Log.e("test", "getCurrentPlaylistsuccess3： " + new Gson().toJson(playList));
                        }
                        Log.e("test", "getCurrentPlaylistsuccess4： " + new Gson().toJson(playList));
                        callback.onSuccess(playList);
                    }
                }
            }

            @Override
            public void failure(ActionInvocation invocation, UpnpResponse operation, String defaultMsg) {
                Log.e("test", "getCurrentPlaylistfailure： " + defaultMsg);
                if (Utils.isNotNull(callback)) {
                    callback.onFailed(defaultMsg);
                }
            }
        };

        ControlPoint controlPointImpl = SWDeviceUtils.getControlPoint();
        if (Utils.isNull(controlPointImpl)) {
            return;
        }

        controlPointImpl.execute(browseQueue);
    }

    /**
     * 删除当前播放列表某一首歌
     *
     * @param callback
     */
    @Override
    public void deleteTrackWithIndex(int position, @Nullable ControlCallback callback) {
        final Service avtService = SWDeviceUtils.findServiceFromSelectedDevice(ServiceId.valueOf("urn:wiimu-com:serviceId:PlayQueue"));
        if (Utils.isNull(avtService)) {
            return;
        }
        RemoveTracksInQueue removeTracksInQueue = new RemoveTracksInQueue(avtService, LPPlayQueueType.LP_CURRENT_QUEUE.getValue(), position, position) {
            /**
             * Called when the action invocation succeeded.
             *
             * @param invocation The successful invocation, call its <code>getOutput()</code> method for results.
             */
            @Override
            public void success(ActionInvocation invocation) {
                if (callback != null) {
                    callback.success(new ClingResponse(actionInvocation));
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

        controlPointImpl.execute(removeTracksInQueue);
    }


    public @DLANPlayState.DLANPlayStates
    int getCurrentState() {
        return mCurrentState;
    }

    public void setCurrentState(@DLANPlayState.DLANPlayStates int currentState) {
        if (this.mCurrentState != currentState) {
            this.mCurrentState = currentState;
        }
    }


    private String pushMediaToRender(List<LPMSLibraryPlayItem> playlist, int playIndex) {
        long size = 0;
        long bitrate = 0;
        Res res = new Res(new MimeType(ProtocolInfo.WILDCARD, ProtocolInfo.WILDCARD), size, playlist.get(playIndex).getTrackUrl());
        MusicTrack musicTrack = new MusicTrack("id", "0", playlist.get(playIndex).getTrackName(), playlist.get(playIndex).getTrackArtist(), "resolution", "resolution", res);
        String mediaData = createItemMetadata(musicTrack);
        Log.e(TAG, "metadata: " + mediaData);
        return mediaData;
    }


    //组装音箱播放音乐数据
    public static String getMediaData(int pos, List<LPPlayItem> mediaInfoList) {
        LPPlayHeader playHeader = new LPPlayHeader();
        playHeader.setHeadTitle("My Music");

        playHeader.setMediaType(LPPlayHeader.LPPlayMediaType.LP_SONGLIST_LOCAL);
        playHeader.setMediaSource(LPPlaySourceType.LP_LOCALMUSIC);

        LPPlayMusicList lpPlayMusicList = new LPPlayMusicList();
        lpPlayMusicList.setHeader(playHeader);
        lpPlayMusicList.setIndex(pos);
//        lpPlayMusicList.setAccount(LPMSLibraryManager.getInstance(UIApplication.instance).getAccount());
        lpPlayMusicList.setList(mediaInfoList);

        for (LPPlayItem item : lpPlayMusicList.getList()) {
            if (item.getTrackDuration() == 0) {
                item.setTrackDuration(1);
            }
        }
        return playMusicSingleSource(lpPlayMusicList);
    }

    @SuppressLint({"DefaultLocale"})
    public static int maxCountOnePage = 300;
    public static String playMusicSingleSource(LPPlayMusicList lpPlayMusicList) {
        if (lpPlayMusicList == null) {
            return "";
        } else {
            String soureStr = "";
            LPPlayHeader lpPlayHeader;
            if ((lpPlayHeader = lpPlayMusicList.getHeader()) == null) {
                return "";
            } else {
                List lpitemlist = lpPlayMusicList.getList();
                int index = lpPlayMusicList.getIndex();
                if (TextUtils.isEmpty(lpPlayHeader.getHeadTitle())) {
                    soureStr = "headTitle can't empty";
                } else if (!TextUtils.isEmpty(lpPlayHeader.getMediaType()) && !lpPlayHeader.getMediaType().equals("NONE") && !lpPlayHeader.getMediaType().equals("UNKNOWN")) {
                    if (TextUtils.isEmpty(lpPlayHeader.getMediaSource())) {
                        soureStr = "mediaSource can't empty";
                    }
                } else {
                    soureStr = "mediaType can't empty";
                }

                if (TextUtils.isEmpty(soureStr)) {
                    LPPlayMediaData lpPlayMediaData = new LPPlayMediaData();
                    lpPlayMediaData.setMediaSource(lpPlayHeader.getMediaSource());
                    String searchUrl;
                    if (TextUtils.isEmpty(searchUrl = lpPlayHeader.getSearchUrl()) && lpitemlist != null) {
                        ArrayList lpitemlist1;
                        lpitemlist1 = new ArrayList();
                        ArrayList lpitemlist2;
                        lpitemlist2 = new ArrayList();
                        ArrayList<LPPlayItem> lpitemlist3;
                        lpitemlist3 = new ArrayList();
                        if (lpitemlist.size() <= maxCountOnePage) {
                            ArrayList lpitemlist4;
                            lpitemlist4 = new ArrayList();
                            ArrayList lpitemlist5;
                            lpitemlist5 = new ArrayList();

                            for (int i = 0; i < lpitemlist.size(); ++i) {
                                if (i >= index) {
                                    lpitemlist5.add((LPPlayItem) lpitemlist.get(i));
                                } else {
                                    lpitemlist4.add((LPPlayItem) lpitemlist.get(i));
                                }
                            }

                            lpitemlist1.addAll(lpitemlist5);
                            lpitemlist1.addAll(lpitemlist4);
                        } else {
                            Iterator var14;
                            List var17;
                            if ((var17 = lpitemlist.subList(index, lpitemlist.size())).size() >= maxCountOnePage) {
                                var14 = var17.subList(0, maxCountOnePage).iterator();

                                while (var14.hasNext()) {
                                    lpitemlist1.add((LPPlayItem) var14.next());
                                }
                            } else if (var17.size() < maxCountOnePage) {
                                lpitemlist = lpitemlist.subList(0, maxCountOnePage - var17.size());
                                Iterator var18 = var17.iterator();

                                while (var18.hasNext()) {
                                    lpitemlist1.add((LPPlayItem) var18.next());
                                }

                                var14 = lpitemlist.iterator();

                                while (var14.hasNext()) {
                                    lpitemlist1.add((LPPlayItem) var14.next());
                                }
                            }
                        }

                        if (lpitemlist1.size() == 1) {
                            lpitemlist2.add((LPPlayItem) lpitemlist1.get(0));
                        } else if (lpitemlist1.size() > 1) {
                            lpitemlist2.add((LPPlayItem) lpitemlist1.get(0));
                            lpitemlist2.add((LPPlayItem) lpitemlist1.get(1));

                            for (int j = 2; j < lpitemlist1.size(); ++j) {
                                lpitemlist3.add((LPPlayItem) lpitemlist1.get(j));
                            }
                        }


                        LPPlayMusicList var10001  = new LPPlayMusicList();
                        var10001.setList(lpitemlist2);
                        var10001.setHeader(lpPlayHeader);
                        ArrayList var20  = new ArrayList();
                        var20.add(createXmlString(var10001));

                        if (lpitemlist3.size() > 0) {
                            List<List<LPPlayItem>> listList = new ArrayList<>();
                            for(int k = 0 ; k < lpitemlist3.size() ; k++){
                                LPPlayItem lpPlayItem = lpitemlist3.get(k);
                                if(listList.size() == 0){
                                    List<LPPlayItem> tempList = new ArrayList<>();
                                    tempList.add(lpPlayItem);
                                    listList.add(tempList);
                                }else {
                                    List<LPPlayItem> tempList = listList.get(listList.size() - 1);
                                    if(tempList.size() == 50){
                                        List<LPPlayItem> newList = new ArrayList<>();
                                        newList.add(lpPlayItem);
                                        listList.add(newList);
                                    }else {
                                        tempList.add(lpPlayItem);
                                    }
                                }
                            }
                            for(List<LPPlayItem> list : listList){
                                LPPlayMusicList var10002 = new LPPlayMusicList();
                                var10002.setList(list);
                                var10002.setHeader(lpPlayHeader);
                                var20.add(createXmlString(var10002));
                                Log.e("test","listList:" + listList.size() + " list:" + list.size());
                            }
                        }

                        lpPlayMediaData.setPlayData(var20);
                        lpPlayMediaData.setPlayIndex(index);
                        lpPlayMediaData.setQueueName(lpPlayHeader.getHeadTitle());
                        if (lpitemlist1.size() > 0) {
                            lpPlayMediaData.setAppendCount(var20.size() - 1);
                        } else {
                            lpPlayMediaData.setAppendCount(0);
                        }

                        Log.e("test","playMusicSingleSource:" + lpitemlist.size() + " lpitemlist1:" + lpitemlist1.size() + " lpitemlist3:" +
                                lpitemlist3.size() + " lpPlayMediaData:" + lpPlayMediaData.getAppendCount());
                    }

                    if (!TextUtils.isEmpty(searchUrl)) {
                        LPPlayMusicList var10006 = lpPlayMusicList = new LPPlayMusicList();
                        lpPlayMusicList.setList((List) null);
                        lpPlayMusicList.setHeader(lpPlayHeader);
                        ArrayList var21 = new ArrayList();
                        var21.add(createXmlString(lpPlayMusicList));
                        lpPlayMediaData.setPlayData(var21);
                        lpPlayMediaData.setPlayIndex(index);
                        lpPlayMediaData.setQueueName(lpPlayHeader.getHeadTitle());
                        lpPlayMediaData.setAppendCount(0);
                    }

                    soureStr = new Gson().toJson(lpPlayMediaData);
                }

                return soureStr;
            }
        }
    }

    public static void appendStrings(StringBuffer var0, String var1) {
        Log.e("LPQueueXmlCreator", "..." + var1);
        var0.append(var1);
    }

    public static String createXmlString(LPPlayMusicList var0) {
        LPPlayHeader var1;
        LPPlayHeader var10000 = var1 = var0.getHeader();
        List var2 = var0.getList();
        if (var10000 == null) {
            Log.i("LPQueueXmlCreator", "alarm_backqueue: header is null");
            return "";
        } else {
            StringBuffer var4;
            StringBuffer var10001 = var4 = new StringBuffer();
            appendStrings(var4, "<?xml version=\"1.0\" ?>");
            appendStrings(var4, "<PlayList>");
            appendStrings(var4, "<ListName>" + LPXmlUtil.Encode(LPXmlUtil.getCommonStr(var1.getHeadTitle())) + "</ListName>");
            appendStrings(var4, "<ListInfo>");
            appendStrings(var10001, "<Radio>" + var1.getMediaType().equals("STATION-NETWORK") + "</Radio>");
            int var5;
            if (var2 == null) {
                var5 = 0;
            } else {
                var5 = var2.size();
            }

            String var6 = var1.getSearchUrl();
            appendStrings(var4, "<SourceName>" + LPXmlUtil.Encode(LPXmlUtil.getCommonStr(var1.getMediaSource())) + "</SourceName>");
            appendStrings(var4, "<SearchUrl>" + LPXmlUtil.Encode(LPXmlUtil.getCommonStr(var6)) + "</SearchUrl>");
            appendStrings(var4, "<TrackNumber>" + var5 + "</TrackNumber>");
            String var13;
            if (!TextUtils.isEmpty(var13 = var1.getQuality()) && !LPMSUtil.isNumber(var13)) {
                appendStrings(var4, "<requestQuality>" + var13 + "</requestQuality>");
            } else {
                StringBuilder var14 = (new StringBuilder()).append("<Quality>");
                if (TextUtils.isEmpty(var13)) {
                    var6 = "0";
                } else {
                    var6 = var1.getQuality();
                }

                appendStrings(var4, var14.append(var6).append("</Quality>").toString());
            }

            appendStrings(var4, "<UpdateTime>0</UpdateTime>");
            appendStrings(var4, "<LastPlayIndex>" + var0.getIndex() + "</LastPlayIndex>");
            appendStrings(var4, "<SwitchPageMode>0</SwitchPageMode>");
            appendStrings(var4, "<CurrentPage>" + var1.getCurrentPage() + "</CurrentPage>");
            appendStrings(var4, "<TotalPages>" + var1.getTotalPage() + "</TotalPages>");
            appendStrings(var4, "</ListInfo>");
            if (var2 != null && var2.size() > 0) {
                List var10;
                List var18 = var10 = var0.getList();
                String var9 = var0.getHeader().getCreator();
                String var11 = var0.getHeader().getMediaSource();
                if (var18 != null && var10.size() > 0) {
                    appendStrings(var4, "<Tracks>");
                    int var12 = 0;

                    while (var12 < var10.size()) {
                        LPPlayItem var15 = (LPPlayItem) var10.get(var12);
                        StringBuffer var16;
                        StringBuffer var19 = var16 = new StringBuffer();
                        String var21 = var9;
                        LPPlayItem var10002 = var15;
                        appendStrings(var4, "<Track" + ++var12 + ">");
                        var16.append("<URL>" + LPXmlUtil.Encode(LPXmlUtil.getCommonStr(var15.getTrackUrl())) + "</URL>");
                        var16.append("<Metadata>");

                        try {
                            var19.append(LPXmlUtil.getMetadata(var21, var10002));
                        } catch (Exception var8) {
                            var8.printStackTrace();
                            var16.append("");
                        }

                        var16.append("</Metadata>");
                        var16.append("<Id>" + var15.getTrackId() + "</Id>");
                        var16.append("<Source>" + LPXmlUtil.Encode(LPXmlUtil.getCommonStr(var11)) + "</Source>");
                        var16.append("</Track" + var12 + ">");
                        appendStrings(var4, var16.toString());
                    }

                    appendStrings(var4, "</Tracks>");
                }
            }

            appendStrings(var4, "</PlayList>");
            return var4.toString();
        }
    }

    private String createItemMetadata(DIDLObject item) {
        StringBuilder metadata = new StringBuilder();
        metadata.append(DIDL_LITE_HEADER);

        metadata.append(String.format("<item id=\"%s\" parentID=\"%s\" restricted=\"%s\">", item.getId(), item.getParentID(), item.isRestricted() ? "1" : "0"));

        metadata.append(String.format("<dc:title>%s</dc:title>", item.getTitle()));
        String creator = item.getCreator();
        if (creator != null) {
            creator = creator.replaceAll("<", "_");
            creator = creator.replaceAll(">", "_");
        }
        metadata.append(String.format("<upnp:artist>%s</upnp:artist>", creator));

        metadata.append(String.format("<upnp:class>%s</upnp:class>", item.getClazz().getValue()));

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        Date now = new Date();
        String time = sdf.format(now);
        metadata.append(String.format("<dc:date>%s</dc:date>", time));

        // metadata.append(String.format("<upnp:album>%s</upnp:album>",
        // item.get);

        // <res protocolInfo="http-get:*:audio/mpeg:*"
        // resolution="640x478">http://192.168.1.104:8088/Music/07.我醒著做夢.mp3</res>

        Res res = item.getFirstResource();
        if (res != null) {
            // protocol info
            String protocolinfo = "";
            ProtocolInfo pi = res.getProtocolInfo();
            if (pi != null) {
                protocolinfo = String.format("protocolInfo=\"%s:%s:%s:%s\"", pi.getProtocol(), pi.getNetwork(), pi.getContentFormatMimeType(), pi
                        .getAdditionalInfo());
            }
            Log.e(TAG, "protocolinfo: " + protocolinfo);

            // resolution, extra info, not adding yet
            String resolution = "";
            if (res.getResolution() != null && res.getResolution().length() > 0) {
                resolution = String.format("resolution=\"%s\"", res.getResolution());
            }

            // duration
            String duration = "";
            if (res.getDuration() != null && res.getDuration().length() > 0) {
                duration = String.format("duration=\"%s\"", res.getDuration());
            }

            // res begin
            //            metadata.append(String.format("<res %s>", protocolinfo)); // no resolution & duration yet
            metadata.append(String.format("<res %s %s %s>", protocolinfo, resolution, duration));

            // url
            String url = res.getValue();
            metadata.append(url);

            // res end
            metadata.append("</res>");
        }
        metadata.append("</item>");

        metadata.append(DIDL_LITE_FOOTER);

        return metadata.toString();
    }

    public interface GetCurrentPlaylistCallback{
        void onSuccess(PlayList playList);

        void onFailed(String msg);
    }
}
