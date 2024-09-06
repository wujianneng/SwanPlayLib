package com;

import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.gson.Gson;
import com.nesp.android.cling.Intents;
import com.nesp.android.cling.control.SWPlayControl;
import com.nesp.android.cling.control.callback.ControlCallback;
import com.nesp.android.cling.control.callback.ControlReceiveCallback;
import com.nesp.android.cling.entity.SWDevice;
import com.nesp.android.cling.entity.SWDeviceList;
import com.nesp.android.cling.entity.ClingGetControlDeviceInfoResponse;
import com.nesp.android.cling.entity.ClingMediaResponse;
import com.nesp.android.cling.entity.ClingPlayModeResponse;
import com.nesp.android.cling.entity.ClingPlaylistResponse;
import com.nesp.android.cling.entity.ClingTransportResponse;
import com.nesp.android.cling.entity.SWDeviceStatus;
import com.nesp.android.cling.entity.DLANPlayState;
import com.nesp.android.cling.entity.IDevice;
import com.nesp.android.cling.entity.IResponse;
import com.nesp.android.cling.entity.LPMediaInfo;
import com.nesp.android.cling.entity.MusicDataBean;
import com.nesp.android.cling.entity.PlayList;
import com.nesp.android.cling.entity.PlayStatusBean;
import com.nesp.android.cling.entity.SelectSWDeviceBean;
import com.nesp.android.cling.entity.SlaveBean;
import com.nesp.android.cling.listener.DeviceListChangedListener;
import com.nesp.android.cling.listener.LPDevicePlayerListener;
import com.nesp.android.cling.service.manager.SWDeviceManager;
import com.nesp.android.cling.util.SWDeviceUtils;
import com.nesp.android.cling.util.Utils;
import com.wujianneng.huiweilink.R;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.teleal.cling.model.action.ActionArgumentValue;
import org.teleal.cling.model.meta.RemoteDevice;
import org.teleal.cling.support.model.MediaInfo;
import org.teleal.cling.support.model.PositionInfo;
import org.teleal.cling.support.model.TransportInfo;

import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity implements SeekBar.OnSeekBarChangeListener {

    private static final String TAG = MainActivity.class.getSimpleName();
    /**
     * 连接设备状态: 播放状态
     */
    public static final int PLAY_ACTION = 0xa1;
    /**
     * 连接设备状态: 暂停状态
     */
    public static final int PAUSE_ACTION = 0xa2;
    /**
     * 连接设备状态: 停止状态
     */
    public static final int STOP_ACTION = 0xa3;
    /**
     * 连接设备状态: 转菊花状态
     */
    public static final int TRANSITIONING_ACTION = 0xa4;
    /**
     * 获取进度
     */
    public static final int GET_POSITION_INFO_ACTION = 0xa5;
    /**
     * 投放失败
     */
    public static final int ERROR_ACTION = 0xa5;

    public static final int REFRESH_LIST_VIEW = 0xa6;

    public static final int REFRESH_TIME_VIEW = 0xa7;

    public static final int REFRESH_MEDIA_VIEW = 0xa8;

    private Context mContext;
    private Handler mHandler = new InnerHandler();

    private ListView mDeviceList;
    private TextView timeTv, durationTv, tracknameTv, artistnameTv;
    private Button playModeBtn,channelBtn;
    private SeekBar mSeekProgress;
    private SeekBar mSeekVolume;
    private Switch mSwitchMute;
    ImageView imageView;

    private BroadcastReceiver mTransportStateBroadcastReceiver;
    private DevicesAdapter mDevicesAdapter;
    /**
     * 投屏控制器
     */
    private SWPlayControl mSWPlayControl = new SWPlayControl();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mContext = this;
        initView();
        initListeners();
        registerReceivers();
        EventBus.getDefault().register(this);

        SWDeviceManager.getInstance().init(this);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void handleEventBus(String event) {
        if(event.equals(SWDeviceList.REFRESH_LIST_UI_KEY)){
            mDevicesAdapter.notifyDataSetChanged();
        }
    }


    private void registerReceivers() {
        //Register play status broadcast
        mTransportStateBroadcastReceiver = new TransportStateBroadcastReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intents.ACTION_PLAYING);
        filter.addAction(Intents.ACTION_PAUSED_PLAYBACK);
        filter.addAction(Intents.ACTION_STOPPED);
        filter.addAction(Intents.ACTION_TRANSITIONING);
        registerReceiver(mTransportStateBroadcastReceiver, filter);
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
        mHandler.removeCallbacksAndMessages(null);
        // UnRegister Receiver
        unregisterReceiver(mTransportStateBroadcastReceiver);
        SWDeviceManager.getInstance().destroy();
    }

    private void initView() {
        mDeviceList = findViewById(R.id.lv_devices);
        timeTv = findViewById(R.id.time_tv);
        tracknameTv = findViewById(R.id.track_name_tv);
        artistnameTv = findViewById(R.id.artist_name_tv);
        imageView = findViewById(R.id.image);
        durationTv = findViewById(R.id.duration_tv);
        mSeekProgress = findViewById(R.id.seekbar_progress);
        mSeekVolume = findViewById(R.id.seekbar_volume);
        mSwitchMute = findViewById(R.id.sw_mute);

        playModeBtn = findViewById(R.id.bt_playmode);
        channelBtn = findViewById(R.id.bt_channel);
        mDevicesAdapter = new DevicesAdapter(mContext, SWDeviceList.getInstance().masterDevices);
        mDeviceList.setAdapter(mDevicesAdapter);

        mSeekProgress.setMax(100);

        // 最大音量就是 100，不要问我为什么
        mSeekVolume.setMax(100);
    }

    private void initListeners() {
        mDeviceList.setOnItemClickListener((parent, view, position, id) -> {
            // 选择连接设备
            SWDevice item = (SWDevice) mDevicesAdapter.getItem(position);
            if (Utils.isNull(item)) {
                return;
            }

            SWDeviceManager.getInstance().setSelectedDevice(item);
            mDevicesAdapter.notifyDataSetChanged();
            SWDeviceManager.getInstance().registerAVTransport(mContext);
            SWDeviceManager.getInstance().registerRenderingControl(mContext);
            SWDeviceManager.getInstance().registerMediaRenderer(mContext);
            SWDeviceManager.getInstance().registerMediaServer(mContext);
            SWDeviceManager.getInstance().registerConnectionManager(mContext);
            SWDeviceManager.getInstance().registerContentDirectory(mContext);
        });

        // 静音开关
        mSwitchMute.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                mSWPlayControl.setMute(isChecked, new ControlCallback() {
                    @Override
                    public void success(IResponse response) {
                        Log.e(TAG, "setMute success");
                    }

                    @Override
                    public void fail(IResponse response) {
                        Log.e(TAG, "setMute fail");
                    }
                });
            }
        });

        mSeekProgress.setOnSeekBarChangeListener(this);
        mSeekVolume.setOnSeekBarChangeListener(this);

        // 设置发现设备监听
        SWDeviceManager.getInstance().setOnDeviceListChangedListener(new DeviceListChangedListener() {
            @Override
            public void onDeviceAdded(final IDevice device) {
                Log.e("test", "onDeviceAdded():" + ((SWDevice) device).getDevice().getDetails().getSsidName());
                mHandler.sendEmptyMessage(REFRESH_LIST_VIEW);

            }

            @Override
            public void onDeviceRemoved(final IDevice device) {
                Log.e("test", "onDeviceRemoved():" + device.toString());
            }
        });

        SWDeviceManager.getInstance().setGetInfoTask(new SWDeviceManager.WorkInTimeTask() {
            @Override
            public void work(PositionInfo positionInfo,String fromUuid) {
                if (positionInfo == null) {
                    Message message = new Message();
                    message.what = REFRESH_MEDIA_VIEW;
                    message.obj = null;
                    mHandler.sendMessage(message);
                } else {
                    SWDevice swDevice = (SWDevice) SWDeviceManager.getInstance().getSelectedDevice();
                    if(swDevice.getUuid().equals(fromUuid)) {
                        Message message = new Message();
                        message.what = REFRESH_TIME_VIEW;
                        message.obj = positionInfo;
                        mHandler.sendMessage(message);
                    }
                    Log.e("test", "getPositionInfo()receive:" + positionInfo.getTrackMetaData() + " uri:" + positionInfo.getTrackURI());
                    LPMediaInfo lpMediaInfo = new LPMediaInfo();
                    lpMediaInfo.parseMetaData(positionInfo.getTrackMetaData());
                    try {
                        MusicDataBean.DataBean musicDataBean = new Gson().fromJson(URLDecoder.decode(lpMediaInfo.getAlbumArtURI(),
                                "UTF-8"), MusicDataBean.DataBean.class);
                        Log.e("parse", "Current URI metadata: " + new Gson().toJson(musicDataBean));
                        if (musicDataBean != null && swDevice.getUuid().equals(fromUuid)) {
                            musicDataBean.setAlbum(lpMediaInfo.getAlbum());
                            musicDataBean.setCreator(lpMediaInfo.getCreator());
                            musicDataBean.setMediaType(lpMediaInfo.getMediaType());
                            swDevice.setMediaInfo(musicDataBean);
                            Message msg = new Message();
                            msg.what = REFRESH_MEDIA_VIEW;
                            msg.obj = musicDataBean;
                            mHandler.sendMessage(msg);
                            mHandler.sendEmptyMessage(REFRESH_LIST_VIEW);
                        } else {
                            swDevice.setMediaInfo(null);
                            Message msg = new Message();
                            msg.what = REFRESH_MEDIA_VIEW;
                            msg.obj = null;
                            mHandler.sendMessage(msg);
                            mHandler.sendEmptyMessage(REFRESH_LIST_VIEW);
                        }
                    } catch (Exception e) {
                        SWDevice SWDevice = (SWDevice) SWDeviceManager.getInstance().getSelectedDevice();
                        SWDevice.setMediaInfo(null);
                        Message msg = new Message();
                        msg.what = REFRESH_MEDIA_VIEW;
                        msg.obj = null;
                        mHandler.sendMessage(msg);
                        mHandler.sendEmptyMessage(REFRESH_LIST_VIEW);
                    }
                }
            }
        }, new SWDeviceManager.WorkInInfoTask() {
            @Override
            public void work() {
                if (SWDeviceManager.getInstance().getSelectedDevice() == null && SWDeviceList.getInstance().masterDevices.size() != 0) {
                    SWDevice item = (SWDevice) mDevicesAdapter.getItem(0);
                    if (Utils.isNull(item)) {
                        return;
                    }
                    SWDeviceManager.getInstance().setSelectedDevice(item);
                    SWDeviceManager.getInstance().registerAVTransport(mContext);
                    SWDeviceManager.getInstance().registerRenderingControl(mContext);
                    SWDeviceManager.getInstance().registerMediaRenderer(mContext);
                    SWDeviceManager.getInstance().registerMediaServer(mContext);
                    SWDeviceManager.getInstance().registerConnectionManager(mContext);
                    SWDeviceManager.getInstance().registerContentDirectory(mContext);
                }
                getVolumeInfo();
                getDevicePlayerStatus();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        SWDeviceManager.getInstance().pauseAllTask(false);
    }

    @Override
    protected void onPause() {
        super.onPause();
        SWDeviceManager.getInstance().pauseAllTask(true);
    }

    private void getPlayMode() {
        mSWPlayControl.getPlayMode(new ControlReceiveCallback() {
            @Override
            public void receive(IResponse response) {
                ClingPlayModeResponse clingPlayModeResponse = (ClingPlayModeResponse) response;
                Log.e(TAG, "getPlayMode: " + clingPlayModeResponse.getResponse().get("LoopMode").toString());
                SWDevice SWDevice = (SWDevice) SWDeviceManager.getInstance().getSelectedDevice();
                PlayStatusBean playStatusBean = new PlayStatusBean();
                playStatusBean.setLoop(clingPlayModeResponse.getResponse().get("LoopMode").toString());
                SWDevice.setPlayStatusBean(playStatusBean);
                runOnUiThread(() -> {
                    playModeBtn.setText(getPlayModeText(playStatusBean.getLoop()));
                });
            }

            @Override
            public void success(IResponse response) {

            }

            @Override
            public void fail(IResponse response) {

            }
        });
    }

    private void getDevicePlayerStatus() {
        if (SWDeviceManager.getInstance().getSelectedDevice() != null) {
            RemoteDevice remoteDevice = (RemoteDevice) ((SWDevice) SWDeviceManager.getInstance().getSelectedDevice()).getDevice();
            SWDeviceUtils.getDevicePlayerStatus(remoteDevice.getIdentity().getDescriptorURL().getHost(), new SWDeviceUtils.GetDevicePlayerStatusCallback() {
                @Override
                public void onResponse(PlayStatusBean deviceInfoBean) {
                    SWDevice SWDevice = (SWDevice) SWDeviceManager.getInstance().getSelectedDevice();
                    SWDevice.setPlayStatusBean(deviceInfoBean);
                    runOnUiThread(() -> {
                        playModeBtn.setText(getPlayModeText(deviceInfoBean.getLoop()));
                        channelBtn.setText(getChannelText(deviceInfoBean.getCh()));
                    });
                }

                @Override
                public void onFailure(String msg) {

                }
            });
        }
    }

    private String getChannelText(String channel) {//0立体声，1左声道，2右声道
        if (channel.equals("0")) {//立体声
            return "立体声";
        } else if (channel.equals("1")) {//左声道
            return "左声道";
        } else if (channel.equals("2")) {//右声道
            return "右声道";
        } else {
            return "立体声";
        }
    }

    private String getPlayModeText(String loop) {//0列表循环，1单曲循环，2，3随机播放，4，默认顺序播放
        if (loop.equals("0")) {//列表循环
            return "列表循环";
        } else if (loop.equals("1")) {//单曲循环
            return "单曲循环";
        } else if (loop.equals("2") || loop.equals("3")) {//随机播放
            return "随机播放";
        } else if (loop.equals("4")) {//默认顺序播放
            return "顺序播放";
        } else {
            return "列表循环";
        }
    }


    private void updateUiDeviceList() {
        mHandler.sendEmptyMessage(REFRESH_LIST_VIEW);
    }

    private void getVolumeInfo() {
        Log.e("test", "getVolumeInfo()");
        mSWPlayControl.getVolume(new ControlReceiveCallback() {
            @Override
            public void receive(IResponse response) {
                String result = response.getResponse().toString();
                Log.e("test", "getVolumeInfo()receive:" + response.getResponse().toString());
                runOnUiThread(() -> {
                    mSeekVolume.setProgress(Integer.parseInt(result));
                });
            }

            @Override
            public void success(IResponse response) {

            }

            @Override
            public void fail(IResponse response) {

            }
        });
    }


    public void onClick(View view) {
        int id = view.getId();
        switch (id) {
            case R.id.bt_play:
                play();
                break;
            case R.id.bt_pause:
                pause();
                break;

            case R.id.bt_stop:
                stop();
                break;
            case R.id.bt_next:
                next();
                break;
            case R.id.bt_previous:
                previous();
                break;
            case R.id.bt_playmode:
                setPlayMode();
                break;
            case R.id.bt_playlist:
                Log.e("test", "getCurrentPlaylist： ");
                getPlaylist();
                break;
            case R.id.bt_devices:
                showSelectDeviceDialog();
                break;
            case R.id.bt_channel:
                setChannel();
                break;
        }
    }

    private void setChannel() {
        SWDevice SWDevice = (SWDevice) SWDeviceManager.getInstance().getSelectedDevice();
        if (SWDevice == null || SWDevice.getPlayStatusBean() == null || SWDevice.getSwDeviceInfo().getSWDeviceStatus() == null) return;
        if (SWDevice.getPlayStatusBean().getCh().equals("0")) {
            SWDevice.getPlayStatusBean().setCh("1");
            channelBtn.setText("左声道");
        } else if (SWDevice.getPlayStatusBean().getCh().equals("1")) {
            SWDevice.getPlayStatusBean().setCh("2");
            channelBtn.setText("右声道");
        } else if (SWDevice.getPlayStatusBean().getCh().equals("2")) {
            SWDevice.getPlayStatusBean().setCh("0");
            channelBtn.setText("立体声");
        }
        SWDeviceUtils.setMasterDeviceChannel(SWDevice.getSwDeviceInfo().getSWDeviceStatus().getApcli0(),
                Integer.parseInt(SWDevice.getPlayStatusBean().getCh()), new SWDeviceUtils.BaseCallback() {
            @Override
            public void onResponse(String result) {

            }

            @Override
            public void onFailure(String msg) {

            }
        });
    }

    private void showSelectDeviceDialog() {
        Log.e("test", "showSelectDeviceDialog");
        if (dialog == null) {
            if (SWDeviceManager.getInstance().getSelectedDevice() == null)
                return;
            Log.e("test", "showSelectDeviceDialog1");
            SWDevice selectDevice = (SWDevice) SWDeviceManager.getInstance().getSelectedDevice();
            SWDeviceUtils.getSlaveList(selectDevice.getSwDeviceInfo().getSWDeviceStatus().getApcli0(), new SWDeviceUtils.GetSlaveListCallback() {
                @Override
                public void onResponse(List<SlaveBean.SlaveListDTO> slaveListDTOList) {
                    Log.e("test", "showSelectDeviceDialog2");
                    List<SelectSWDeviceBean> lpDevices = new ArrayList<>();
                    List<String> slavelpDeviceuuidS = new ArrayList<>();
                    for (SlaveBean.SlaveListDTO listDTO : slaveListDTOList) {
                        lpDevices.add(new SelectSWDeviceBean(listDTO.getSsid(), listDTO.getName(), listDTO.getIp(), listDTO.getVersion(),
                                true, true));
                        slavelpDeviceuuidS.add(listDTO.getUuid());
                    }
                    for (SWDevice device : SWDeviceList.masterDevices) {
                        if (device.getSwDeviceInfo().getSWDeviceStatus() != null && !device.getSwDeviceInfo().getSWDeviceStatus().getUpnp_uuid().equals(selectDevice.getSwDeviceInfo().getSWDeviceStatus().getUpnp_uuid()) &&
                                !slavelpDeviceuuidS.contains(device.getSwDeviceInfo().getSWDeviceStatus().getUpnp_uuid())) {
                            lpDevices.add(new SelectSWDeviceBean(device.getSwDeviceInfo().getSWDeviceStatus().getSsid(), device.getSwDeviceInfo().getSWDeviceStatus().getDeviceName(), device.getSwDeviceInfo().getSWDeviceStatus().getApcli0(),
                                    device.getSwDeviceInfo().getSWDeviceStatus().getHardware(), false, false));
                        }
                    }

                    doMultiroomAction(lpDevices,selectDevice);
                }

                @Override
                public void onFailure(String msg) {
                    Log.e("test", "showSelectDeviceDialog3:" + msg);
                    List<SelectSWDeviceBean> lpDevices = new ArrayList<>();
                    List<String> slavelpDeviceuuidS = new ArrayList<>();
                    for (SWDevice device : SWDeviceList.masterDevices) {
                        if (device.getSwDeviceInfo().getSWDeviceStatus() != null && !device.getSwDeviceInfo().getSWDeviceStatus().getUpnp_uuid().equals(selectDevice.getSwDeviceInfo().getSWDeviceStatus().getUpnp_uuid()) &&
                                !slavelpDeviceuuidS.contains(device.getSwDeviceInfo().getSWDeviceStatus().getUpnp_uuid())) {
                            lpDevices.add(new SelectSWDeviceBean(device.getSwDeviceInfo().getSWDeviceStatus().getSsid(), device.getSwDeviceInfo().getSWDeviceStatus().getDeviceName(), device.getSwDeviceInfo().getSWDeviceStatus().getApcli0(),
                                    device.getSwDeviceInfo().getSWDeviceStatus().getHardware(), false, false));
                        }
                    }
                    doMultiroomAction(lpDevices,selectDevice);
                }
            });
        }
    }

    public void doMultiroomAction(List<SelectSWDeviceBean> lpDevices,SWDevice selectDevice){
        if (lpDevices.size() == 0) {
            runOnUiThread(() -> Toast.makeText(MainActivity.this, "没有可用设备", Toast.LENGTH_SHORT).show());
            return;
        }
        runOnUiThread(() -> {
            View view = LayoutInflater.from(MainActivity.this).inflate(R.layout.dialog_select_device, null);
            dialog = new Dialog(MainActivity.this);
            dialog.setContentView(view);
            dialog.setCancelable(true);
            dialog.create();
            view.findViewById(R.id.close_btn).setOnClickListener(v -> {
                if (dialog != null)
                    dialog.dismiss();
            });
            TextView songname = view.findViewById(R.id.song_name_tv);
            TextView artist = view.findViewById(R.id.artist_tv);
            ImageView coverimg = view.findViewById(R.id.cover_img);
            MusicDataBean.DataBean musicDataBean = selectDevice.getMediaInfo();
            if (musicDataBean != null) {
                songname.setText(musicDataBean.getName());
                artist.setText(musicDataBean.getArtist());
                String path = musicDataBean.getCoverUrl();
                if (musicDataBean.getCoverUrl() == null || TextUtils.isEmpty(musicDataBean.getCoverUrl()) || path.equals("un_known")) {
                    coverimg.setImageResource(R.drawable.df_cover);
                } else {
                    if (MainActivity.this != null)
                        Glide.with(MainActivity.this)
                                .load(path)
                                .centerCrop()
                                .into(coverimg);
                }
            }
            RecyclerView recyclerView = view.findViewById(R.id.recyclerview);
            LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) recyclerView.getLayoutParams();
            if (lpDevices.size() > 4) {
                lp.height = dp2px(188);
            } else {
                lp.height = LinearLayout.LayoutParams.WRAP_CONTENT;
            }
            recyclerView.setLayoutParams(lp);
            recyclerView.setLayoutManager(new LinearLayoutManager(MainActivity.this, LinearLayoutManager.VERTICAL, false));
            SelectDeviceAdapter playlistAdapter = new SelectDeviceAdapter(MainActivity.this, R.layout.select_device_list_item, lpDevices);
            recyclerView.setAdapter(playlistAdapter);
            playlistAdapter.setOnItemClickListener(((adapter, view1, position) -> {
                if (lpDevices.get(position).isSlaveDevice()) {
                    lpDevices.get(position).setSelected(!lpDevices.get(position).isSelected());
                    playlistAdapter.notifyItemChanged(position);
                } else {
                    if (lpDevices.get(position).getHardware().equals(selectDevice.getSwDeviceInfo().getSWDeviceStatus().getHardware())
                            || lpDevices.get(position).isSlaveDevice()
                            || (lpDevices.get(position).getHardware().contains("SWAN") && selectDevice.getSwDeviceInfo().getSWDeviceStatus().getHardware().contains("SWAN"))
                    ) {
                        lpDevices.get(position).setSelected(!lpDevices.get(position).isSelected());
                        playlistAdapter.notifyItemChanged(position);
                    } else {
                        Toast.makeText(MainActivity.this, "型号不匹配", Toast.LENGTH_SHORT).show();
                    }
                }

            }));
            view.findViewById(R.id.finish_tv).setOnClickListener(v -> {
                if (lpDevices.size() != 0) {
                    List<SelectSWDeviceBean> kicInList = new ArrayList<>();
                    List<SelectSWDeviceBean> kicOutList = new ArrayList<>();
                    for (SelectSWDeviceBean device : lpDevices) {
                        if (!device.isSelected() && device.isSlaveDevice()) {
                            kicOutList.add(device);
                        }
                        if (device.isSelected() && !device.isSlaveDevice()) {
                            kicInList.add(device);
                        }
                    }
                    SWDeviceUtils.slaveListKicOut(this,selectDevice, kicOutList, new SWDeviceUtils.BaseCallback() {
                        @Override
                        public void onResponse(String result) {
                            runOnUiThread(()-> Toast.makeText(MainActivity.this, "解绑成功", Toast.LENGTH_SHORT).show());
                        }

                        @Override
                        public void onFailure(String msg) {

                        }
                    });
                    SWDeviceUtils.slaveListKicIn(this,selectDevice, kicInList, new SWDeviceUtils.BaseCallback() {
                        @Override
                        public void onResponse(String result) {
                            runOnUiThread(()-> Toast.makeText(MainActivity.this, "同步成功", Toast.LENGTH_SHORT).show());
                        }

                        @Override
                        public void onFailure(String msg) {

                        }
                    });
                    Toast.makeText(MainActivity.this, "设备多房间操作中...", Toast.LENGTH_SHORT).show();
                }
                if (dialog != null)
                    dialog.dismiss();
            });
            dialog.setOnDismissListener(idialog -> dialog = null);
            dialog.setCanceledOnTouchOutside(true);
            dialog.setCancelable(true);
            dialog.show();
            Window window = dialog.getWindow();
            WindowManager.LayoutParams layoutParams = window.getAttributes();
            layoutParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
            layoutParams.width = WindowManager.LayoutParams.MATCH_PARENT;
            window.setGravity(Gravity.BOTTOM);
        });

    }

    private void getPlaylist() {
        mSWPlayControl.getCurrentPlaylist(new ControlReceiveCallback() {
            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
            @Override
            public void receive(IResponse response) {
                ClingPlaylistResponse clingPlaylistResponse = (ClingPlaylistResponse) response;
                runOnUiThread(() -> {
                    showPlaylistDialog(clingPlaylistResponse.getResponse());
                });
            }

            @Override
            public void success(IResponse response) {

            }

            @Override
            public void fail(IResponse response) {

            }
        });
    }

    Dialog dialog = null;

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void showPlaylistDialog(PlayList playList) {
        if (dialog == null) {
            View view = LayoutInflater.from(this).inflate(R.layout.dialog_play_list, null);
            dialog = new Dialog(this);
            dialog.setContentView(view);
            dialog.setCancelable(true);
            dialog.create();
            view.findViewById(R.id.close_btn).setOnClickListener(v -> {
                dialog.dismiss();
            });
            ((TextView) view.findViewById(R.id.count_tv)).setText("(" + playList.trackList.size() + ")");
            RecyclerView recyclerView = view.findViewById(R.id.recyclerview);
            recyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
            PlaylistAdapter playlistAdapter = new PlaylistAdapter(this, R.layout.playlist_list_item, playList.trackList);
            playlistAdapter.currentIndex = playList.LastPlayIndex - 1;
            recyclerView.setAdapter(playlistAdapter);
            playlistAdapter.setOnItemClickListener(((adapter, view1, position) -> {
                Log.e("test", "playindex:" + position);
                mSWPlayControl.playQueueWithIndex(position + 1, new LPDevicePlayerListener() {
                    @Override
                    public void onSuccess(String var1) {
                        runOnUiThread(() -> {
                            playlistAdapter.setCurrentIndex(position);
                            handler.postDelayed(() -> {
                                playlistAdapter.notifyDataSetChanged();
                            }, 1000);
                        });
                    }

                    @Override
                    public void onFailure(Exception var1) {
                        Log.e("test", "playindexf:" + var1.getMessage());
                    }
                });
            }));

            playlistAdapter.setOnItemChildClickListener(((adapter, view1, position) -> {
                if (view1.getId() == R.id.delete_img) {
                    mSWPlayControl.deleteTrackWithIndex(position + 1, new ControlCallback() {
                        @Override
                        public void success(IResponse response) {
                            playList.trackList.remove(position);
                            runOnUiThread(() -> {
                                handler.postDelayed(() -> {
                                    playlistAdapter.notifyDataSetChanged();
                                }, 500);
                            });

                        }

                        @Override
                        public void fail(IResponse response) {

                        }
                    });

                }
            }));
            dialog.setOnDismissListener(idialog -> dialog = null);
            dialog.show();
            dialog.setCanceledOnTouchOutside(true);
            dialog.setCancelable(true);
            Window window = dialog.getWindow();
//            window.setBackgroundDrawable(getDrawable(R.drawable.dialog_bg));
            WindowManager.LayoutParams layoutParams = window.getAttributes();
            layoutParams.height = dp2px(480);
            layoutParams.width = WindowManager.LayoutParams.MATCH_PARENT;
            window.setGravity(Gravity.BOTTOM);
        }
    }

    private static float density = Resources.getSystem().getDisplayMetrics().density;

    public static int dp2px(float dpValue) {
        return (int) (0.5f + dpValue * density);
    }

    private void setPlayMode() {//0列表循环，1单曲循环，2，3随机播放，4，默认顺序播放
        SWDevice SWDevice = (SWDevice) SWDeviceManager.getInstance().getSelectedDevice();
        if (SWDevice == null || SWDevice.getPlayStatusBean() == null) return;
        if (SWDevice.getPlayStatusBean().getLoop().equals("0")) {
            SWDevice.getPlayStatusBean().setLoop("1");
            playModeBtn.setText("单曲循环");
        } else if (SWDevice.getPlayStatusBean().getLoop().equals("1")) {
            SWDevice.getPlayStatusBean().setLoop("2");
            playModeBtn.setText("随机播放");
        } else if (SWDevice.getPlayStatusBean().getLoop().equals("2") || SWDevice.getPlayStatusBean().getLoop().equals("3")) {
            SWDevice.getPlayStatusBean().setLoop("4");
            playModeBtn.setText("顺序播放");
        } else if (SWDevice.getPlayStatusBean().getLoop().equals("4")) {
            SWDevice.getPlayStatusBean().setLoop("0");
            playModeBtn.setText("列表循环");
        }
        mSWPlayControl.setPlayMode(Integer.parseInt(SWDevice.getPlayStatusBean().getLoop()), new ControlCallback() {
            @Override
            public void success(IResponse response) {

            }

            @Override
            public void fail(IResponse response) {

            }
        });
    }


    private void previous() {
        mSWPlayControl.previous(new ControlCallback() {
            @Override
            public void success(IResponse response) {

            }

            @Override
            public void fail(IResponse response) {

            }
        });
    }

    private void next() {
        mSWPlayControl.next(new ControlCallback() {
            @Override
            public void success(IResponse response) {

            }

            @Override
            public void fail(IResponse response) {

            }
        });
    }

    /**
     * 停止
     */
    private void stop() {
        mSWPlayControl.stop(new ControlCallback() {
            @Override
            public void success(IResponse response) {
                Log.e(TAG, "stop success");
            }

            @Override
            public void fail(IResponse response) {
                Log.e(TAG, "stop fail");
            }
        });
    }

    /**
     * 暂停
     */
    private void pause() {
        mSWPlayControl.pause(new ControlCallback() {
            @Override
            public void success(IResponse response) {
                Log.e(TAG, "pause success");
            }

            @Override
            public void fail(IResponse response) {
                Log.e(TAG, "pause fail");
            }
        });
    }

    Handler handler = new Handler();

    public void getTransportInfo() {
        mSWPlayControl.getTransportInfo(new ControlReceiveCallback() {
            @Override
            public void receive(IResponse response) {
                ClingTransportResponse transportResponse = (ClingTransportResponse) response;
                TransportInfo transportInfo = transportResponse.getResponse();
                Log.e("test", "getTransportInfo()：" + transportInfo.getCurrentTransportState().getValue());
            }

            @Override
            public void success(IResponse response) {

            }

            @Override
            public void fail(IResponse response) {

            }
        });
    }


    public void getMediaInfo() {
        mSWPlayControl.getMediaInfo(new ControlReceiveCallback() {
            @Override
            public void receive(IResponse response) {
                ClingMediaResponse mediaResponse = (ClingMediaResponse) response;
                MediaInfo mediaInfo = mediaResponse.getResponse();
                String currentURI = mediaInfo.getCurrentURIMetaData();
                Log.e("parse", "Current URI metadata: " + currentURI);
                LPMediaInfo lpMediaInfo = new LPMediaInfo();
                lpMediaInfo.parseMetaData(currentURI);
                try {
                    MusicDataBean.DataBean musicDataBean = new Gson().fromJson(URLDecoder.decode(lpMediaInfo.getAlbumArtURI(),
                            "UTF-8"), MusicDataBean.DataBean.class);
                    Log.e("parse", "Current URI metadata: " + new Gson().toJson(musicDataBean));
                    runOnUiThread(() -> {
                        tracknameTv.setText(musicDataBean.getName());
                        artistnameTv.setText(musicDataBean.getArtist());
                        Glide.with(MainActivity.this).load(musicDataBean.getCoverUrl()).into(imageView);
                    });
                } catch (Exception e) {
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


    /**
     * 播放视频
     */
    private void play() {
//        @DLANPlayState.DLANPlayStates int currentState = mSWPlayControl.getCurrentState();
//
//        /**
//         * 通过判断状态 来决定 是继续播放 还是重新播放
//         */
//
////        if (currentState == DLANPlayState.STOP) {
//        Gson gson = new Gson();
//        MusicDataBean.DataBean dataBean = new MusicDataBean.DataBean();
//        dataBean.setDataType("music");
//        dataBean.setName("有愿无恙");
//        dataBean.setId("0");
//        dataBean.setArtist("张杰");
//        dataBean.setCoverUrl("http://y.gtimg.cn/music/photo_new/T001R150x150M000002azErJ0UcDN6_3.jpg");
//        dataBean.setVip(true);
//        dataBean.setUploadType("0");
//        dataBean.setPlayUrl(Config.TEST_URL);
////            dataBean.setAuthorization(UIApplication.instance.myRetrofitManager.headerValue);
////            dataBean.setBitrate(ToolsUtil.gePlayUrlevel(UIApplication.instance));
////            dataBean.setSnCode(ToolsUtil.getAndroidId(UIApplication.instance));
//        LPPlayItem track = new LPPlayItem();
//        track.setTrackUrl(dataBean.getPlayUrl());
//        track.setTrackName(dataBean.getName());
//        track.setTrackDuration(100000);
//        track.setTrackArtist(dataBean.getArtist());
//        track.setTrackImage(gson.toJson(dataBean));
//
//        MusicDataBean.DataBean dataBean2 = new MusicDataBean.DataBean();
//        dataBean2.setDataType("music");
//        dataBean2.setName("腾空");
//        dataBean2.setId("0");
//        dataBean2.setArtist("汪苏泷");
//        dataBean2.setCoverUrl("http://y.gtimg.cn/music/photo_new/T002R120x120M000003On52X1iPxey_1.jpg");
//        dataBean2.setVip(true);
//        dataBean2.setUploadType("0");
//        dataBean2.setPlayUrl(Config.TEST_URL2);
//        LPPlayItem track2 = new LPPlayItem();
//        track2.setTrackUrl(dataBean2.getPlayUrl());
//        track2.setTrackName(dataBean2.getName());
//        track2.setTrackDuration(100000);
//        track2.setTrackArtist(dataBean2.getArtist());
//        track2.setTrackImage(gson.toJson(dataBean2));
//        List<LPPlayItem> currentPlaylist = new ArrayList<>();
//        currentPlaylist.add(track);
//        currentPlaylist.add(track2);
//        Log.e(TAG, "playAudio");
//        mSWPlayControl.playAudio(SWPlayControl.getMediaData(0, currentPlaylist), new ControlCallback() {
//            @Override
//            public void success(IResponse response) {
//                Log.e(TAG, "playAudio success");
//                SWDeviceManager.getInstance().registerAVTransport(mContext);
//                SWDeviceManager.getInstance().registerRenderingControl(mContext);
//                SWDeviceManager.getInstance().registerMediaRenderer(mContext);
//                SWDeviceManager.getInstance().registerMediaServer(mContext);
//                SWDeviceManager.getInstance().registerConnectionManager(mContext);
//                SWDeviceManager.getInstance().registerContentDirectory(mContext);
//
//            }
//
//            @Override
//            public void fail(IResponse response) {
//                Log.e(TAG, "playAudio fail");
//            }
//        });
        mSWPlayControl.play(new ControlCallback() {
            @Override
            public void success(IResponse response) {

            }

            @Override
            public void fail(IResponse response) {

            }
        });
    }

    /******************* start progress changed listener *************************/

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
        Log.e(TAG, "Start Seek");
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        Log.e(TAG, "Stop Seek");
        int id = seekBar.getId();
        switch (id) {
            case R.id.seekbar_progress: // 进度

                int currentProgress = seekBar.getProgress() * Utils.getIntTime(durationTv.getText().toString()) / 100; // 转为毫秒
                Log.e(TAG, "getProgress:" + seekBar.getProgress() + " duration:" + Utils.getIntTime
                        (durationTv.getText().toString()) + " durationTv:" + durationTv.getText().toString() + " currentProgress:" + currentProgress);
                mSWPlayControl.seek(currentProgress, new ControlCallback() {
                    @Override
                    public void success(IResponse response) {
                        Log.e(TAG, "seek success");
                    }

                    @Override
                    public void fail(IResponse response) {
                        Log.e(TAG, "seek fail");
                    }
                });
                break;

            case R.id.seekbar_volume:   // 音量

                int currentVolume = seekBar.getProgress();
                mSWPlayControl.setVolume(currentVolume, new ControlCallback() {
                    @Override
                    public void success(IResponse response) {
                        Log.e(TAG, "volume success");
                    }

                    @Override
                    public void fail(IResponse response) {
                        Log.e(TAG, "volume fail");
                    }
                });
                break;
        }
    }

    /******************* end progress changed listener *************************/

    private final class InnerHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case PLAY_ACTION:
                    Log.i(TAG, "Execute PLAY_ACTION");
                    Toast.makeText(mContext, "正在投放", Toast.LENGTH_SHORT).show();
                    mSWPlayControl.setCurrentState(DLANPlayState.PLAY);

                    break;
                case PAUSE_ACTION:
                    Log.i(TAG, "Execute PAUSE_ACTION");
                    mSWPlayControl.setCurrentState(DLANPlayState.PAUSE);

                    break;
                case STOP_ACTION:
                    Log.i(TAG, "Execute STOP_ACTION");
                    mSWPlayControl.setCurrentState(DLANPlayState.STOP);

                    break;
                case TRANSITIONING_ACTION:
                    Log.i(TAG, "Execute TRANSITIONING_ACTION");
                    Toast.makeText(mContext, "正在连接", Toast.LENGTH_SHORT).show();
                    break;
                case ERROR_ACTION:
                    Log.e(TAG, "Execute ERROR_ACTION");
                    Toast.makeText(mContext, "投放失败", Toast.LENGTH_SHORT).show();
                    break;
                case REFRESH_LIST_VIEW:
                    mDevicesAdapter.notifyDataSetChanged();
                    break;
                case REFRESH_TIME_VIEW:
                    PositionInfo positionInfo = (PositionInfo) msg.obj;
                    timeTv.setText(positionInfo.getRelTime());
                    durationTv.setText(positionInfo.getTrackDuration());
                    mSeekProgress.setMax(100);
                    mSeekProgress.setProgress(positionInfo.getElapsedPercent());
                    break;
                case REFRESH_MEDIA_VIEW:
                    if (msg.obj == null) {
                        tracknameTv.setText("");
                        artistnameTv.setText("");
                        imageView.setImageDrawable(null);
                    } else {
                        MusicDataBean.DataBean musicDataBean = (MusicDataBean.DataBean) msg.obj;
                        tracknameTv.setText(musicDataBean.getName());
                        artistnameTv.setText(musicDataBean.getArtist());
                        Glide.with(MainActivity.this).load(musicDataBean.getCoverUrl()).into(imageView);
                    }
                    break;
            }
        }
    }

    /**
     * 接收状态改变信息
     */
    private class TransportStateBroadcastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Log.e(TAG, "Receive playback intent:" + action);
            if (Intents.ACTION_PLAYING.equals(action)) {
                mHandler.sendEmptyMessage(PLAY_ACTION);

            } else if (Intents.ACTION_PAUSED_PLAYBACK.equals(action)) {
                mHandler.sendEmptyMessage(PAUSE_ACTION);

            } else if (Intents.ACTION_STOPPED.equals(action)) {
                mHandler.sendEmptyMessage(STOP_ACTION);

            } else if (Intents.ACTION_TRANSITIONING.equals(action)) {
                mHandler.sendEmptyMessage(TRANSITIONING_ACTION);
            }
        }
    }
}