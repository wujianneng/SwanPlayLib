package com.nesp.android.cling.service.manager;

import android.content.Context;
import com.nesp.android.cling.entity.IDevice;
import com.nesp.android.cling.entity.SWDevice;


/**
 * 说明：

 * 日期：17/7/21 16:34
 */

public interface IDeviceManager {

    /**
     * 获取选中设备
     */
    SWDevice getSelectedDevice();

    /**
     * 设置选中设备
     */
    void setSelectedDevice(SWDevice selectedDevice);

    /**
     * 取消选中设备
     */
    void cleanSelectedDevice();

    /**
     * 监听投屏端 AVTransport 回调
     * @param context   用于接收到消息发广播
     */
    void registerAVTransport(Context context);

    /**
     * 监听投屏端 RenderingControl 回调
     * @param context   用于接收到消息发广播
     */
    void registerRenderingControl(Context context);

    /**
     * 监听投屏端 MediaRenderer 回调
     */
    void registerMediaRenderer(Context context);

    /**
     * 监听投屏端 MediaServer 回调
     */
    void registerMediaServer(Context context);

    /**
     * 监听投屏端 ConnectionManager 回调
     */
    void registerConnectionManager(Context context);

    /**
     * 监听投屏端 ContentDirectory 回调
     */
    void registerContentDirectory(Context context);


    /**
     * 销毁
     */
    void destroy();
}
