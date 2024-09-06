package com;

import android.content.Context;
import android.graphics.Color;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.nesp.android.cling.entity.SWDevice;
import com.nesp.android.cling.entity.SlaveBean;
import com.nesp.android.cling.service.manager.SWDeviceManager;
import com.wujianneng.huiweilink.R;

import org.teleal.cling.model.meta.Device;

import java.util.ConcurrentModificationException;
import java.util.List;


public class DevicesAdapter extends BaseAdapter {
    private LayoutInflater mInflater;
    List<SWDevice> datalist;

    public DevicesAdapter(Context context,List<SWDevice> datalist) {
        this.datalist = datalist;
        mInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    /**
     * How many items are in the data set represented by this Adapter.
     *
     * @return Count of items.
     */
    @Override
    public int getCount() {
        return datalist.size();
    }

    /**
     * Get the data item associated with the specified position in the data set.
     *
     * @param position Position of the item whose data we want within the adapter's
     *                 data set.
     * @return The data at the specified position.
     */
    @Override
    public Object getItem(int position) {
        return datalist.get(position);
    }

    /**
     * Get the row id associated with the specified position in the list.
     *
     * @param position The position of the item within the adapter's data set whose row id we want.
     * @return The id of the item at the specified position.
     */
    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null)
            convertView = mInflater.inflate(R.layout.devices_items, null);

        SWDevice item = datalist.get(position);
        if (item == null || item.getDevice() == null) {
            return convertView;
        }

        Device device = item.getDevice();

        ImageView imageView = convertView.findViewById(R.id.listview_item_image);

        if(item.getMediaInfo() == null || TextUtils.isEmpty(item.getMediaInfo().getCoverUrl())){
            imageView.setBackgroundResource(R.drawable.ic_action_dock);
        }else {
            Glide.with(convertView.getContext()).load(item.getMediaInfo().getCoverUrl()).into(imageView);
        }
        TextView textView = convertView.findViewById(R.id.listview_item_line_one);
        SWDevice selectedDevice = (SWDevice)SWDeviceManager.getInstance().getSelectedDevice();
        TextView textView2 = convertView.findViewById(R.id.listview_item_line_two);
        if(item.getSwDeviceInfo().getSWDeviceStatus() != null) {
            textView.setText(item.getSwDeviceInfo().getSWDeviceStatus().getDeviceName());
            textView.setText(item.getSwDeviceInfo().getSWDeviceStatus().getDeviceName());
            if(selectedDevice!= null && selectedDevice.getSwDeviceInfo().getSWDeviceStatus().getUuid().equals(item.getSwDeviceInfo().getSWDeviceStatus().getUuid())){
                textView.setTextColor(Color.RED);
            }else {
                textView.setTextColor(Color.BLACK);
            }
        }
        TextView titletv = convertView.findViewById(R.id.title_tv);
        TextView artisttv = convertView.findViewById(R.id.artist_tv);
        if(item.getMediaInfo() != null) {

            titletv.setText(item.getMediaInfo().getName());

            artisttv.setText(item.getMediaInfo().getArtist());
        }else {
            titletv.setText("---");

            artisttv.setText("--");
        }
        String slavelist = "";
        Log.e("test", "slaveListDTO:" + item.getSwDeviceInfo().getSlaveList().size());
        if(item.getSwDeviceInfo().getSlaveList().size() != 0){
            try {
                for (SlaveBean.SlaveListDTO slaveListDTO : item.getSwDeviceInfo().getSlaveList()) {
                    Log.e("test", "slaveListDTO:" + slaveListDTO.getName());
                    slavelist = slavelist + slaveListDTO.getName() + "\n";
                }
            }catch (ConcurrentModificationException e){}
        }
        textView2.setText(slavelist);
        return convertView;
    }
}