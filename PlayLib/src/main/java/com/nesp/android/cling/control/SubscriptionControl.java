package com.nesp.android.cling.control;

import android.content.Context;
import androidx.annotation.NonNull;
import com.nesp.android.cling.entity.IDevice;
import com.nesp.android.cling.service.callback.AVTransportSubscriptionCallback;
import com.nesp.android.cling.service.callback.ConnectionManagerSubscriptionCallback;
import com.nesp.android.cling.service.callback.ContentDirectorySubscriptionCallback;
import com.nesp.android.cling.service.callback.MediaRendererSubscriptionCallback;
import com.nesp.android.cling.service.callback.MediaServerSubscriptionCallback;
import com.nesp.android.cling.service.callback.RenderingControlSubscriptionCallback;
import com.nesp.android.cling.service.manager.SWDeviceManager;
import com.nesp.android.cling.util.SWDeviceUtils;
import com.nesp.android.cling.util.Utils;
import org.teleal.cling.controlpoint.ControlPoint;
import org.teleal.cling.model.meta.Device;
import org.teleal.cling.model.meta.Service;


public class SubscriptionControl implements ISubscriptionControl<Device> {

    private AVTransportSubscriptionCallback mAVTransportSubscriptionCallback;
    private RenderingControlSubscriptionCallback mRenderingControlSubscriptionCallback;
    private MediaRendererSubscriptionCallback mediaRendererSubscriptionCallback;
    private MediaServerSubscriptionCallback mediaServerSubscriptionCallback;
    private ConnectionManagerSubscriptionCallback connectionManagerSubscriptionCallback;
    private ContentDirectorySubscriptionCallback contentDirectorySubscriptionCallback;

    public SubscriptionControl() {
    }

    @Override
    public void registerAVTransport(@NonNull IDevice<Device> device, @NonNull Context context) {
        if (Utils.isNotNull(mAVTransportSubscriptionCallback)) {
            mAVTransportSubscriptionCallback.end();
        }
        final ControlPoint controlPointImpl = SWDeviceUtils.getControlPoint();
        if (Utils.isNull(controlPointImpl)) {
            return;
        }

        Service service = device.getDevice().findService(SWDeviceManager.AV_TRANSPORT_SERVICE);
        if(service == null)return;
        mAVTransportSubscriptionCallback = new AVTransportSubscriptionCallback(service, context);
        controlPointImpl.execute(mAVTransportSubscriptionCallback);
    }

    @Override
    public void registerRenderingControl(@NonNull IDevice<Device> device, @NonNull Context context) {
        if (Utils.isNotNull(mRenderingControlSubscriptionCallback)) {
            mRenderingControlSubscriptionCallback.end();
        }
        final ControlPoint controlPointImpl = SWDeviceUtils.getControlPoint();
        if (Utils.isNull(controlPointImpl)) {
            return;
        }
        Service service = device.getDevice().findService(SWDeviceManager
                .RENDERING_CONTROL_SERVICE);
        if(service == null)return;
        mRenderingControlSubscriptionCallback = new RenderingControlSubscriptionCallback(service, context);
        controlPointImpl.execute(mRenderingControlSubscriptionCallback);
    }

    /**
     * 监听投屏端 MediaRenderer 回调
     *
     * @param device
     * @param context
     */
    @Override
    public void registerMediaRenderer(IDevice<Device> device, Context context) {
        if (Utils.isNotNull(mediaRendererSubscriptionCallback)) {
            mediaRendererSubscriptionCallback.end();
        }
        final ControlPoint controlPointImpl = SWDeviceUtils.getControlPoint();
        if (Utils.isNull(controlPointImpl)) {
            return;
        }
        Service service = device.getDevice().findService(SWDeviceManager
                .MEDIA_RENDERER_SERVICE);
        if(service == null)return;
        mediaRendererSubscriptionCallback = new MediaRendererSubscriptionCallback(service, context);
        controlPointImpl.execute(mediaRendererSubscriptionCallback);
    }

    /**
     * 监听投屏端 MediaServer 回调
     *
     * @param device
     * @param context
     */
    @Override
    public void registerMediaServer(IDevice<Device> device, Context context) {
        if (Utils.isNotNull(mediaServerSubscriptionCallback)) {
            mediaServerSubscriptionCallback.end();
        }
        final ControlPoint controlPointImpl = SWDeviceUtils.getControlPoint();
        if (Utils.isNull(controlPointImpl)) {
            return;
        }
        Service service = device.getDevice().findService(SWDeviceManager
                .MEDIA_SERVER_SERVICE);
        if(service == null)return;
        mediaServerSubscriptionCallback = new MediaServerSubscriptionCallback(service, context);
        controlPointImpl.execute(mediaServerSubscriptionCallback);
    }

    /**
     * 监听投屏端 ConnectionManager 回调
     *
     * @param device
     * @param context
     */
    @Override
    public void registerConnectionManager(IDevice<Device> device, Context context) {
        if (Utils.isNotNull(connectionManagerSubscriptionCallback)) {
            connectionManagerSubscriptionCallback.end();
        }
        final ControlPoint controlPointImpl = SWDeviceUtils.getControlPoint();
        if (Utils.isNull(controlPointImpl)) {
            return;
        }
        Service service = device.getDevice().findService(SWDeviceManager
                .CONNECTION_MANAGER_SERVICE);
        if(service == null)return;
        connectionManagerSubscriptionCallback = new ConnectionManagerSubscriptionCallback(service, context);
        controlPointImpl.execute(connectionManagerSubscriptionCallback);
    }

    /**
     * 监听投屏端 ContentDirectory 回调
     *
     * @param device
     * @param context
     */
    @Override
    public void registerContentDirectory(IDevice<Device> device, Context context) {
        if (Utils.isNotNull(contentDirectorySubscriptionCallback)) {
            contentDirectorySubscriptionCallback.end();
        }
        final ControlPoint controlPointImpl = SWDeviceUtils.getControlPoint();
        if (Utils.isNull(controlPointImpl)) {
            return;
        }
        Service service = device.getDevice().findService(SWDeviceManager
                .CONTENT_DIRECTORY_SERVICE);
        if(service == null)return;
        contentDirectorySubscriptionCallback = new ContentDirectorySubscriptionCallback(service, context);
        controlPointImpl.execute(contentDirectorySubscriptionCallback);
    }

    @Override
    public void destroy() {
        if (Utils.isNotNull(mAVTransportSubscriptionCallback)) {
            mAVTransportSubscriptionCallback.end();
        }
        if (Utils.isNotNull(mRenderingControlSubscriptionCallback)) {
            mRenderingControlSubscriptionCallback.end();
        }
    }
}
