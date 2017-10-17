package com.knox.kavrecorder.adapter;

import android.support.v7.widget.RecyclerView;
import android.util.SparseArray;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;


/**
 * @author Knox.Tsang
 * @time 2017/8/25  20:18
 * @desc ${TODD}
 */


public class KRvViewHolder extends RecyclerView.ViewHolder {

    SparseArray<View> mMap = new SparseArray<>();
    View mView;

    public KRvViewHolder(View itemView) {
        super(itemView);
        mView = itemView;
    }

    public void setText(int vId, String text) {
        View textView = findView(vId);
        if (textView instanceof TextView) {
            ((TextView) textView).setText(text);
        }
    }

    public void setImgVisibility(int vId, boolean isVisible) {
        View imageView = findView(vId);
        if (imageView instanceof ImageView) {
            ((ImageView) imageView).setVisibility(isVisible ? View.VISIBLE : View.INVISIBLE);
        }
    }

    private View findView(int vId) {
        View view = mMap.get(vId);
        if (view == null) {
            view = mView.findViewById(vId);
            mMap.put(vId, view);
        }
        return view;
    }

}
