package com.knox.kavrecorder.net;

import android.util.Log;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ConnectException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Arrays;

import static com.knox.kavrecorder.utils.KMd5.bytes2Hex0;


/**
 * @author Knox.Tsang
 * @time 2017/9/22  8:39
 * @desc ${TODD}
 */


public class KTcpClient {

    private static final String TAG = "KTcpClient";
    private static int CONNECT_MAX_COUNT = 1 * 1000;
    private static int sConnCnt = 0;
    private Socket mSocket;
    private IReceiver mListener;
    private OutputStream mOs;
    private InputStream mIs;
    private static final int BUF_SIZE = 1024;

    public KTcpClient(String ip, int port) {
        if (ip == null || port == 0) {
            return;
        }

        while (sConnCnt < CONNECT_MAX_COUNT) {
            sConnCnt ++;
            try {
                mSocket = new Socket(ip, port);
                Log.e(TAG, "constructor: " + "socket创建成功 " + ip + ":" + port + ", currThread: " + Thread.currentThread().getName());
                mOs = new BufferedOutputStream(mSocket.getOutputStream());
                Log.e(TAG, "constructor: " + "打开tcp OutputStream成功");
                mIs = mSocket.getInputStream();
                Log.e(TAG, "constructor: " + "打开tcp InputStream成功");
                return;
            } catch (ConnectException e) {
                Log.e(TAG, "constructor: ConnectException");
                continue;
            } catch (UnknownHostException e) {
                Log.e(TAG, "constructor: UnknownHostException");
                e.printStackTrace();
            } catch (IllegalArgumentException e) {
                Log.e(TAG, "constructor: IllegalArgumentException");
                e.printStackTrace();
            } catch (IOException e) {
                Log.e(TAG, "constructor: IOException");
                e.printStackTrace();
            }
        }
    }


    public void writeAndResponse(byte[] buff) {
        if (mOs != null) {
            try {
                //Log.e(TAG, "writeAndResponse: " + Arrays.toString(buff));
                mOs.write(buff);
                mOs.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        if (mIs != null) {
            try {
                byte[] buf = new byte[BUF_SIZE];
                //TODO make sure buff contains one complete data, if source length is bigger than 1024, need to find a way resolve it
                int length = mIs.read(buf);
                if (length != -1 && mListener != null) {
                    mListener.onReceive(buf, length);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    static int seq = 0;

    public void writeNoResponse(byte[] buff) {
        if (mOs != null) {
            try {
                //Log.e(TAG, "writeNoResponse: seq " + (++seq) + ": " + bytes2Hex0(buff));
                mOs.write(buff);
                mOs.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void release() {
        Log.e(TAG, "release: ");
        try {
            if (mIs != null) {
                mIs.close();
                mIs = null;
                Log.e(TAG, "release: tcp inputStream");
            }
        } catch (IOException e) {
            Log.e(TAG, "release: tcp inputStream IOException");
            e.printStackTrace();
        }

        try {
            if (mOs != null) {
                mOs.close();
                mOs = null;
                Log.e(TAG, "release: tcp outputStream");
            }
        } catch (IOException e) {
            Log.e(TAG, "release: tcp outputStream IOException");
            e.printStackTrace();
        }

        try {
            if (mSocket != null) {
                mSocket.close();
                mSocket = null;
                Log.e(TAG, "release: tcp socket");
            }
        } catch (IOException e) {
            Log.e(TAG, "release: tcp socket IOException");
            e.printStackTrace();
        }

    }

    public void setOnRevListener(IReceiver listener) {
        mListener = listener;
    }

public interface IReceiver {
    void onReceive(byte[] datas, int length);
}
}
