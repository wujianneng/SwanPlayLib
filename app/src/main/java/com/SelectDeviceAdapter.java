package com;

import android.content.Context;
import android.graphics.Color;

import androidx.annotation.LayoutRes;
import androidx.annotation.Nullable;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.nesp.android.cling.entity.SelectSWDeviceBean;
import com.wujianneng.huiweilink.R;

import java.util.List;

public class SelectDeviceAdapter extends BaseQuickAdapter<SelectSWDeviceBean, BaseViewHolder> {

    Context context;

    public SelectDeviceAdapter(Context context, @LayoutRes int layoutResId, @Nullable List<SelectSWDeviceBean> data) {
        super(layoutResId, data);
        this.context = context;
    }


    @Override
    protected void convert(BaseViewHolder helper, SelectSWDeviceBean item) {
        helper.setText(R.id.device_name_tv,item.getLpDeviceName());
        helper.setImageResource(R.id.checkbox_img,item.isSelected() ? R.drawable.check_h : R.drawable.check_df);
        helper.setGone(R.id.tag_img,false);
    }
}

