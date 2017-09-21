package com.knox.kavrecorder.net;

import android.util.Log;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.Arrays;

/**
 * @author Knox.Tsang
 * @time 2017/9/21  15:40
 * @desc ${TODD}
 */


public class KUdpReceiver {

    private static final String TAG = "KUdpReceiver";

    private DatagramSocket mSocket;
    private IReceiver mListener;

    public KUdpReceiver(int port) {
        if (port == 0)
            return;

        try {
            mSocket = new DatagramSocket(port);
            Log.e(TAG, "KUdpReceiver: " + "接收端创建成功");
        } catch (SocketException e) {
            e.printStackTrace();
            Log.e(TAG, "KUdpReceiver: " + "接收端创建失败");
        }
    }

    public void setListener(IReceiver listener) {
        mListener = listener;
    }

    public interface IReceiver {
        void onReceive(byte[] datas, String serverIp);
    }

    public void AsyncReceive() {
        new AsyncTask(this).start();
    }

    private static class AsyncTask extends Thread {

        WeakReference<KUdpReceiver> mReceiver;

        public AsyncTask(KUdpReceiver receiver) {
            mReceiver = new WeakReference<KUdpReceiver>(receiver);
        }

        @Override
        public void run() {
            /*接收数据*/
            while (true) {
                byte[] buff = new byte[1024];
                DatagramPacket packet = new DatagramPacket(buff, 1024);
                try {
                    if (mReceiver.get().mSocket != null)
                        mReceiver.get().mSocket.receive(packet);
                    Log.e(TAG, "createReceiver: 接收到的信息是 " + Arrays.toString(packet.getData()));
                    if (mReceiver.get().mListener != null && packet.getAddress() != null) {
                        mReceiver.get().mListener.onReceive(packet.getData(), packet.getAddress().getHostAddress());
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
