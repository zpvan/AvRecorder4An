package com.knox.kavrecorder;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.knox.kavrecorder.adapter.DeviceRvAdapter;
import com.knox.kavrecorder.bean.SearchReply;
import com.knox.kavrecorder.net.DevicesSearcher;

import java.lang.ref.WeakReference;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static com.knox.kavrecorder.constant.NetInfo.listenPort;
import static com.knox.kavrecorder.constant.NetInfo.searchIp;
import static com.knox.kavrecorder.constant.NetInfo.searchPort;

public class MainActivity extends AppCompatActivity implements DevicesSearcher.IDevicesSearch {

    private static final String TAG = "MainActivity";

    @BindView(R.id.rv_devices)
    RecyclerView mRvDevices;
    @BindView(R.id.btn_search)
    ImageView mBtnSearch;
    @BindView(R.id.btn_pause)
    ImageView mBtnPause;
    @BindView(R.id.btn_mute)
    ImageView mBtnMute;
    @BindView(R.id.btn_present)
    ImageView mBtnPresent;
    @BindView(R.id.btn_setting)
    ImageView mBtnSetting;
    @BindView(R.id.btn_canel)
    TextView mBtnCanel;
    @BindView(R.id.ll_bottom)
    LinearLayout mLlBottom;

    private DevicesSearcher mSearcher;
    private KHandler mKHandler = new KHandler(this);
    private static final int RECEIVEBEAN = 0x101;
    private DeviceRvAdapter mDeviceRvAdapter;
    private boolean needClear = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        initView();
        initComponent();
    }

    private void initView() {
        mRvDevices.setLayoutManager(new LinearLayoutManager(this));
        mDeviceRvAdapter = new DeviceRvAdapter();
        mRvDevices.setAdapter(mDeviceRvAdapter);
    }

    private void initComponent() {
        mSearcher = new DevicesSearcher(searchIp, searchPort, listenPort);
        mSearcher.setListener(this);
    }


    @OnClick({R.id.btn_search, R.id.btn_pause, R.id.btn_mute, R.id.btn_present, R.id.btn_setting, R.id.btn_canel})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.btn_search:
                searchDevices();
                break;
            case R.id.btn_pause:
                break;
            case R.id.btn_mute:
                break;
            case R.id.btn_present:
                break;
            case R.id.btn_setting:
                break;
            case R.id.btn_canel:
                break;
        }
    }

    private void searchDevices() {
        needClear = true;
        mSearcher.search();
    }

    @Override
    public void onReceive(SearchReply reply) {
        Log.e(TAG, "onReceive: " + reply);
        mKHandler.obtainMessage(RECEIVEBEAN, reply).sendToTarget();
    }

    private static class KHandler extends Handler {
        private WeakReference<Context> reference;

        public KHandler(Context context) {
            reference = new WeakReference<>(context);
        }

        @Override
        public void handleMessage(Message msg) {
            MainActivity activity = (MainActivity) reference.get();
            switch (msg.what) {
                case RECEIVEBEAN:
                    if (activity != null) {
                        activity.mDeviceRvAdapter.addData((SearchReply) msg.obj, activity.needClear);
                        activity.needClear = false;
                    }
                    break;
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mSearcher != null)
            mSearcher.release();
    }
}
