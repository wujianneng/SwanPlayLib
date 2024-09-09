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

                    }

                    @Override
                    public void onFailure(Exception var1) {

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

                    }

                    @Override
                    public void onFailure(Exception var1) {

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
                if (Utils.isNotNull(listener)) {
                    listener.onFailure(new Exception(arg2));
                }

            }

            public void success(ActionInvocation arg0) {
                super.success(arg0);
                if (Utils.isNotNull(listener)) {
                    listener.onSuccess(arg0.toString());
                }

            }
        });
    }

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

            final List var5 = var4.getPlayDataList();
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
                            appendTracksInQueue(controlPointImpl, (String) var5.get(1), avtService, listener);
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

        String time = Utils.getStringTime(currentProgressPercent);
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
    public static String playMusicSingleSource(LPPlayMusicList var1) {
        if (var1 == null) {
            return "";
        } else {
            String var12 = "";
            LPPlayHeader var3;
            if ((var3 = var1.getHeader()) == null) {
                return "";
            } else {
                List var13 = var1.getList();
                int var4 = var1.getIndex();
                if (TextUtils.isEmpty(var3.getHeadTitle())) {
                    var12 = "headTitle can't empty";
                } else if (!TextUtils.isEmpty(var3.getMediaType()) && !var3.getMediaType().equals("NONE") && !var3.getMediaType().equals("UNKNOWN")) {
                    if (TextUtils.isEmpty(var3.getMediaSource())) {
                        var12 = "mediaSource can't empty";
                    }
                } else {
                    var12 = "mediaType can't empty";
                }

                if (TextUtils.isEmpty(var12)) {
                    LPPlayMediaData var15;
                    LPPlayMediaData var19 = var15 = new LPPlayMediaData();
                    var19.setMediaSource(var3.getMediaSource());
                    String var5;
                    if (TextUtils.isEmpty(var5 = var3.getSearchUrl()) && var13 != null) {
                        ArrayList var6;
                        var6 = new ArrayList();
                        ArrayList var7;
                        var7 = new ArrayList();
                        ArrayList var8;
                        var8 = new ArrayList();
                        if (var13.size() <= 100) {
                            ArrayList var9;
                            var9 = new ArrayList();
                            ArrayList var10;
                            var10 = new ArrayList();

                            for (int var11 = 0; var11 < var13.size(); ++var11) {
                                if (var11 >= var4) {
                                    var10.add((LPPlayItem) var13.get(var11));
                                } else {
                                    var9.add((LPPlayItem) var13.get(var11));
                                }
                            }

                            var6.addAll(var10);
                            var6.addAll(var9);
                        } else {
                            Iterator var14;
                            List var17;
                            if ((var17 = var13.subList(var4, var13.size())).size() >= 100) {
                                var14 = var17.subList(0, 100).iterator();

                                while (var14.hasNext()) {
                                    var6.add((LPPlayItem) var14.next());
                                }
                            } else if (var17.size() < 100) {
                                var13 = var13.subList(0, 100 - var17.size());
                                Iterator var18 = var17.iterator();

                                while (var18.hasNext()) {
                                    var6.add((LPPlayItem) var18.next());
                                }

                                var14 = var13.iterator();

                                while (var14.hasNext()) {
                                    var6.add((LPPlayItem) var14.next());
                                }
                            }
                        }

                        if (var6.size() == 1) {
                            var7.add((LPPlayItem) var6.get(0));
                        } else if (var6.size() > 1) {
                            var7.add((LPPlayItem) var6.get(0));
                            var7.add((LPPlayItem) var6.get(1));

                            for (int var16 = 2; var16 < var6.size(); ++var16) {
                                var8.add((LPPlayItem) var6.get(var16));
                            }
                        }

                        LPPlayMusicList var10001 = var1 = new LPPlayMusicList();
                        var1.setList(var7);
                        var1.setHeader(var3);
                        ArrayList var20 = var6 = new ArrayList();
                        var20.add(createXmlString(var1));
                        if (var8.size() > 0) {
                            var10001 = var1 = new LPPlayMusicList();
                            var1.setList(var8);
                            var1.setHeader(var3);
                            var6.add(createXmlString(var10001));
                        }

                        var15.setPlayData(var6);
                        var15.setPlayIndex(var4);
                        var15.setQueueName(var3.getHeadTitle());
                        if (var6.size() > 0) {
                            var15.setAppendCount(var6.size() - 1);
                        } else {
                            var15.setAppendCount(0);
                        }
                    }

                    if (!TextUtils.isEmpty(var5)) {
                        LPPlayMusicList var10006 = var1 = new LPPlayMusicList();
                        var1.setList((List) null);
                        var1.setHeader(var3);
                        ArrayList var21 = new ArrayList();
                        var21.add(createXmlString(var1));
                        var15.setPlayData(var21);
                        var15.setPlayIndex(var4);
                        var15.setQueueName(var3.getHeadTitle());
                        var15.setAppendCount(0);
                    }

                    var12 = new Gson().toJson(var15);
                }

                return var12;
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
