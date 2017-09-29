package com.knox.kavrecorder.net;

import android.util.Log;

import com.knox.kavrecorder.bean.SearchQryBean;
import com.knox.kavrecorder.bean.SearchRlyBean;
import com.knox.kavrecorder.utils.ParseBeanUtil;

import java.lang.ref.WeakReference;
import java.util.Arrays;

import static com.knox.kavrecorder.utils.KTypeConversion.bytes2String;
import static com.knox.kavrecorder.utils.KTypeConversion.int2LeBytes;
import static com.knox.kavrecorder.utils.KTypeConversion.leBytes2Int;


/**
 * @author Knox.Tsang
 * @time 2017/9/21  15:20
 * @desc ${TODD}
 */


public class DevSearcher implements KUdpReceiver.IReceiver {

    private static final String TAG = "DevSearcher";
    private static final String THREAD_NAME = "DSR";
    private KUdpSender mKUdpSender;
    private KUdpReceiver mKUdpReceiver;
    private IDevicesSearch mListener;


    public DevSearcher(String ip, int sendPort, int receivePort) {

        if (ip == null || sendPort == 0 || receivePort == 0) {
            Log.e(TAG, "constructor: err ip: " + ip + ", sendPort: " + sendPort + ", receivePort: " + receivePort);
            return;
        }

        mKUdpSender = new KUdpSender(ip, sendPort);
        mKUdpReceiver = new KUdpReceiver(receivePort);
        mKUdpReceiver.setListener(this);
        mKUdpReceiver.AsyncReceive();
    }

    public void search() {
        //TODO Fix: always create one new thread everytime be called func search
        new AsyncTask(this).start();
    }

    static class AsyncTask extends Thread {
        private WeakReference<DevSearcher> mSearcher;

        public AsyncTask(DevSearcher searcher) {
            super(THREAD_NAME);
            mSearcher = new WeakReference<DevSearcher>(searcher);
        }

        @Override
        public void run() {
            if (mSearcher.get().mKUdpSender != null)
                mSearcher.get().mKUdpSender.send(ParseBeanUtil.parseQuery(new SearchQryBean(5, 0)));
        }
    }

    public void setOnSrchListener(IDevicesSearch listener) {
        mListener = listener;
    }

    @Override
    public void onReceive(byte[] datas, String serverIp) {
        /*接收到各个设备返回的信息, 解析成bean*/
        SearchRlyBean reply = packReply(datas);
        reply.serverIp = serverIp;
        if (mListener != null)
            mListener.onReceive(reply);
    }

    private SearchRlyBean packReply(byte[] datas) {
        SearchRlyBean reply = new SearchRlyBean();

        int readPos = 0;
        reply.deviceType = leBytes2Int(datas, readPos);
        readPos += 4;
        reply.deviceFunction = leBytes2Int(datas, readPos);
        readPos += 4;
        reply.deviceStatus = bytes2String(datas, readPos, 32);
        readPos += 32;
        reply.deviceName = bytes2String(datas, readPos, 64);
        readPos += 64;
        reply.deviceAlias = bytes2String(datas, readPos, 64);
        readPos += 64;
        reply.reserved = bytes2String(datas, readPos, 128);

        return reply;
    }

    public interface IDevicesSearch {
        void onReceive(SearchRlyBean reply);
    }

    public void release() {
        if (mKUdpReceiver != null)
            mKUdpReceiver.release();

        if (mKUdpSender != null)
            mKUdpSender.release();
    }
}
