package com.knox.kavrecorder.net;

import android.util.Log;

import java.io.IOException;
import java.io.InterruptedIOException;
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
    private AsyncTask mRecieveThread = null;
    private static final int BUF_SIZE = 1024;

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
        mRecieveThread = new AsyncTask(this);
        mRecieveThread.start();
    }

    private static class AsyncTask extends Thread {

        WeakReference<KUdpReceiver> mReceiver;

        public AsyncTask(KUdpReceiver receiver) {
            mReceiver = new WeakReference<KUdpReceiver>(receiver);
        }

        @Override
        public void run() {
            /*接收数据*/
            while (!interrupted()) {
                byte[] buff = new byte[BUF_SIZE];
                DatagramPacket packet = new DatagramPacket(buff, BUF_SIZE);
                try {
                    if (mReceiver.get().mSocket != null)
                        mReceiver.get().mSocket.receive(packet);
                    Log.e(TAG, "createReceiver: 接收到的信息是 " + Arrays.toString(packet.getData()));
                    if (mReceiver.get().mListener != null && packet.getAddress() != null) {
                        mReceiver.get().mListener.onReceive(packet.getData(), packet.getAddress().getHostAddress());
                    }
                } catch (InterruptedIOException e) {
                    e.printStackTrace();
                    break;
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void release() {
        if (mRecieveThread != null) {
            mRecieveThread.interrupt();
            mRecieveThread = null;
        }

        if (mSocket != null)
            mSocket.close();
    }
}
