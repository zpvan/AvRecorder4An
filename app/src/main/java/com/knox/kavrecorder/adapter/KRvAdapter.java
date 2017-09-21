package com.knox.kavrecorder.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Knox.Tsang
 * @time 2017/8/25  20:17
 * @desc ${TODD}
 */


public abstract class KRvAdapter<T> extends RecyclerView.Adapter<KRvViewHolder> {

    protected List<T> mList = new ArrayList<>();
    private KRvAdapterListener mListener;

    public void setDatas(List<T> list) {
        mList.clear();
        if (list != null)
            mList.addAll(list);
        notifyDataSetChanged();
    }

    public void addData(T data) {
        mList.add(data);
        notifyDataSetChanged();
    }

    public T getData(int position) {
        return mList != null ? mList.get(position) : null;
    }

    public void clearData() {
        mList.clear();
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return mList != null ? mList.size() : 0;
    }

    @Override
    public void onBindViewHolder(KRvViewHolder holder, final int position) {
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mListener != null)
                    mListener.onKRvClick(v, position);
            }
        });
        onBindViewHolder2(holder, position, mList.get(position));
    }

    public abstract void onBindViewHolder2(KRvViewHolder holder, int position, T t);

    public void setOnListener(KRvAdapterListener listener) {
        mListener = listener;
    }
}
