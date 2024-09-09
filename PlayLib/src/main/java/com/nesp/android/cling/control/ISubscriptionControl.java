package com.nesp.android.cling.control;

import android.content.Context;
import com.nesp.android.cling.entity.IDevice;


/**
 * 说明：tv端回调
 */

public interface ISubscriptionControl<T> {

    /**
     * 监听投屏端 AVTransport 回调
     */
    void registerAVTransport(IDevice<T> device, Context context);

    /**
     * 监听投屏端 RenderingControl 回调
     */
    void registerRenderingControl(IDevice<T> device, Context context);

    /**
     * 监听投屏端 MediaRenderer 回调
     */
    void registerMediaRenderer(IDevice<T> device, Context context);

    /**
     * 监听投屏端 MediaServer 回调
     */
    void registerMediaServer(IDevice<T> device, Context context);

    /**
     * 监听投屏端 ConnectionManager 回调
     */
    void registerConnectionManager(IDevice<T> device, Context context);

    /**
     * 监听投屏端 ContentDirectory 回调
     */
    void registerContentDirectory(IDevice<T> device, Context context);

    /**
     * 销毁: 释放资源
     */
    void destroy();
}
