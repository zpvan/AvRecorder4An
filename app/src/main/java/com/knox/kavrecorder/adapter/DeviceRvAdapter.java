package com.knox.kavrecorder.adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.knox.kavrecorder.R;
import com.knox.kavrecorder.bean.SearchReply;


/**
 * @author Knox.Tsang
 * @time 2017/9/21  17:34
 * @desc ${TODD}
 */


public class DeviceRvAdapter extends KRvAdapter<SearchReply> {

    @Override
    public void onBindViewHolder2(KRvViewHolder holder, int position, SearchReply searchReply) {
        holder.setText(R.id.tv_name, searchReply.deviceName);
    }

    @Override
    public KRvViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new KRvViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_devices, null, false));
    }
}
