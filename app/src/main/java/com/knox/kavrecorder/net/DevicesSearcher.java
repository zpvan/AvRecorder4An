package com.knox.kavrecorder.net;

import android.util.Log;

import com.knox.kavrecorder.bean.SearchQuery;
import com.knox.kavrecorder.bean.SearchReply;

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


public class DevicesSearcher implements KUdpReceiver.IReceiver {

    private static final String TAG = "DevicesSearcher";

    private String mSendIp;
    private int mSendPort;
    private int mReceivePort;
    private KUdpSender mKUdpSender;
    private KUdpReceiver mKUdpReceiver;
    private IDevicesSearch mListener;


    public DevicesSearcher(String ip, int sendPort, int receivePort) {

        if (ip == null || sendPort == 0 || receivePort == 0)
            return;

        mSendIp = ip;
        mSendPort = sendPort;
        mReceivePort = receivePort;

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
        private WeakReference<DevicesSearcher> mSearcher;

        public AsyncTask(DevicesSearcher searcher) {
            super("DevSea");
            mSearcher = new WeakReference<DevicesSearcher>(searcher);
        }

        @Override
        public void run() {
            if (mSearcher.get().mKUdpSender != null)
                mSearcher.get().mKUdpSender.send(mSearcher.get().parseQuery(new SearchQuery(5, 0)));
        }
    }

    private byte[] parseQuery(SearchQuery queryBean) {
        byte[] data = new byte[8];
        int validSize = 0;

        byte[] deviceType = int2LeBytes(queryBean.deviceType);
        byte[] deviceFunc = int2LeBytes(queryBean.deviceFunction);

        System.arraycopy(deviceType, 0, data, validSize, deviceType.length);
        validSize += deviceType.length;
        System.arraycopy(deviceFunc, 0, data, validSize, deviceFunc.length);
        validSize += deviceFunc.length;
        Log.e(TAG, "parseQuery: " + Arrays.toString(data));
        return data;
    }

    public void setListener(IDevicesSearch listener) {
        mListener = listener;
    }

    @Override
    public void onReceive(byte[] datas, String serverIp) {
        /*接收到各个设备返回的信息, 解析成bean*/
        SearchReply reply = packReply(datas);
        reply.serverIp = serverIp;
        if (mListener != null)
            mListener.onReceive(reply);
    }

    private SearchReply packReply(byte[] datas) {
        SearchReply reply = new SearchReply();

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
        void onReceive(SearchReply reply);
    }

    public void release() {
        if (mKUdpReceiver != null)
            mKUdpReceiver.release();

        if (mKUdpSender != null)
            mKUdpSender.release();
    }
}
