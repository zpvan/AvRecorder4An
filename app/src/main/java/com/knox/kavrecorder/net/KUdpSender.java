package com.knox.kavrecorder.net;

import android.util.Log;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Arrays;

/**
 * @author Knox.Tsang
 * @time 2017/9/21  15:24
 * @desc ${TODD}
 */


public class KUdpSender {

    private static final String TAG = "KUdpSender";
    private int mPort;
    DatagramSocket mSender;
    private InetAddress mIP;

    public KUdpSender(String ip, int port) {

        if (ip == null || port == 0) {
            return;
        }

        try {
            mSender = new DatagramSocket();
            Log.e(TAG, "KUdpSender: " + "发送端创建成功");
            mIP = InetAddress.getByName(ip);
            mPort = port;
        } catch (SocketException e) {
            e.printStackTrace();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }

    }

    public void send(byte[] data) {
        if (mSender == null)
            return;

        DatagramPacket packet = new DatagramPacket(data,
                0,
                data.length,
                mIP,
                mPort);

        try {
            mSender.send(packet);
            Log.e(TAG, "createSender: 已经发送数据 " + Arrays.toString(data));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}