package com.nesp.android.cling.entity;

import org.teleal.cling.model.action.ActionInvocation;
import org.teleal.cling.model.message.UpnpResponse;

/**
 * 说明：Cling 服务回调基类
 */

public class BaseClingResponse<T> implements IResponse<T> {

    public ActionInvocation mActionInvocation;
    public UpnpResponse operation;
    public String defaultMsg;
    public T info;

    /**
     * 控制操作成功 构造器
     *
     * @param actionInvocation  cling action 调用
     */
    public BaseClingResponse(ActionInvocation actionInvocation) {
        mActionInvocation = actionInvocation;
    }

    /**
     * 控制操作失败 构造器
     *
     * @param actionInvocation  cling action 调用
     * @param operation     执行状态
     * @param defaultMsg    错误信息
     */
    public BaseClingResponse(ActionInvocation actionInvocation, UpnpResponse operation, String defaultMsg) {
        mActionInvocation = actionInvocation;
        this.operation = operation;
        this.defaultMsg = defaultMsg;
    }

    /**
     * 接收时的回调
     *
     * @param actionInvocation  cling action 调用
     * @param info      回调的对象
     */
    public BaseClingResponse(ActionInvocation actionInvocation, T info) {
        mActionInvocation = actionInvocation;
        this.info = info;
    }

    @Override
    public T getResponse() {
        return info;
    }

    @Override
    public void setResponse(T response) {
        info = response;
    }
}
