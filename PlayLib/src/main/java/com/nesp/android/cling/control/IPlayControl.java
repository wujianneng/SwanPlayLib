package com.nesp.android.cling.control;

import androidx.annotation.Nullable;
import com.nesp.android.cling.control.callback.ControlCallback;
import com.nesp.android.cling.control.callback.ControlReceiveCallback;
import com.nesp.android.cling.entity.LPMSLibraryPlayItem;
import com.nesp.android.cling.entity.LPPlayItem;

import org.teleal.cling.support.model.PlayMode;

import java.util.List;

/**
 * 说明：对视频的控制操作定义
 */
public interface IPlayControl {

    /**
     * 播放歌曲
     *
     */
    void playAudio(List<LPPlayItem> lpPlayItemList, @Nullable ControlCallback callback);

    /**
     * 播放
     */
    void play(@Nullable ControlCallback callback);

    /**
     * 暂停
     */
    void pause(@Nullable ControlCallback callback);

    /**
     * 停止
     */
    void stop(@Nullable ControlCallback callback);

    /**
     * 视频 seek
     *
     * @param pos   seek到的位置(单位:毫秒)
     */
    void seek(int pos, @Nullable ControlCallback callback);

    /**
     * 设置音量
     *
     * @param pos   音量值，最大为 100，最小为 0
     */
    void setVolume(int pos, @Nullable ControlCallback callback);

    /**
     * 设置静音
     *
     * @param desiredMute   是否静音
     */
    void setMute(boolean desiredMute, @Nullable ControlCallback callback);

    /**
     * 获取tv进度
     */
    void getPositionInfo(@Nullable ControlReceiveCallback callback);

    /**
     * 获取媒体信息
     */
    void getMediaInfo(@Nullable ControlReceiveCallback callback);

    /**
     * 获取传输信息
     */
    void getTransportInfo(@Nullable ControlReceiveCallback callback);

    /**
     * 获取播放模式
     */
    void getPlayMode(@Nullable ControlReceiveCallback callback);

    /**
     * 设置播放模式
     */
    void setPlayMode(int playMode, @Nullable ControlCallback callback);

    /**
     * 获取音量
     */
    void getVolume(@Nullable ControlReceiveCallback callback);

    /**
     * 获取当前播放列表
     */
    void getCurrentPlaylist(@Nullable SWPlayControl.GetCurrentPlaylistCallback callback);

    /**
     * 删除当前播放列表某一首歌
     */
    void deleteTrackWithIndex(int position,@Nullable ControlCallback callback);

    /**
     * 下一首
     */
    void next(@Nullable ControlCallback callback);

    /**
     * 上一首
     */
    void previous(@Nullable ControlCallback callback);
}
