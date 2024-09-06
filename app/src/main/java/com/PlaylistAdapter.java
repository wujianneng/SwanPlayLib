package com;

import android.content.Context;
import android.graphics.Color;

import androidx.annotation.LayoutRes;
import androidx.annotation.Nullable;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.nesp.android.cling.entity.SWDevice;
import com.nesp.android.cling.entity.LPPlayItem;
import com.nesp.android.cling.service.manager.SWDeviceManager;
import com.wujianneng.huiweilink.R;

import java.util.List;


public class PlaylistAdapter extends BaseQuickAdapter<LPPlayItem, BaseViewHolder> {

    Context context;
    public int currentIndex = 0;

    public PlaylistAdapter(Context context, @LayoutRes int layoutResId, @Nullable List<LPPlayItem> data) {
        super(layoutResId, data);
        this.context = context;
    }

    public void setCurrentIndex(int index){
        currentIndex = index;
        notifyDataSetChanged();
    }

    @Override
    protected void convert(BaseViewHolder helper, LPPlayItem item) {
//        String playuri = UIApplication.currDevice.getMediaInfo().getPlayUri().replaceAll("&amp;","&").split("guid=")[0];
//        Log.e("test","playuri:" + playuri + " trackurl:" + item.getTrackUrl().split("guid=")[0]);
        if (SWDeviceManager.getInstance().getSelectedDevice() != null && ((SWDevice) SWDeviceManager.getInstance().getSelectedDevice()).getMediaInfo() != null
                && (((SWDevice) SWDeviceManager.getInstance().getSelectedDevice()).getMediaInfo().getName().equals(item.getTrackName())
                && ((SWDevice) SWDeviceManager.getInstance().getSelectedDevice()).getMediaInfo().getArtist().equals(item.getTrackArtist()))) {
            helper.setTextColor(R.id.song_name_tv, Color.parseColor("#E71915"));
            helper.setBackgroundRes(R.id.index_tv, R.drawable.icon_playing_red);
            helper.setText(R.id.index_tv, "");
            helper.setGone(R.id.index_tv, true);
        } else {
            helper.setTextColor(R.id.song_name_tv, Color.parseColor("#6D7275"));
            helper.setBackgroundColor(R.id.index_tv, Color.TRANSPARENT);
            helper.setText(R.id.index_tv, String.valueOf(helper.getAdapterPosition() + 1));
            helper.setGone(R.id.index_tv, false);
        }
        helper.setGone(R.id.index_tv, true);
        helper.setText(R.id.song_name_tv,item.getTrackName());
        helper.setText(R.id.artist_tv,item.getTrackArtist());
        helper.setTextColor(R.id.artist_tv, Color.parseColor("#6D7275"));
        helper.addOnClickListener(R.id.delete_img);
    }
}
