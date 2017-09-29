package com.knox.kavrecorder.net;

import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;

import com.knox.kavrecorder.bean.EsBufferBean;
import com.knox.kavrecorder.bean.RtpPacketBean;
import com.knox.kavrecorder.bean.RtpPayloadBean;
import com.knox.kavrecorder.bean.SimpleUrlBean;
import com.knox.kavrecorder.utils.ParseBeanUtil;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.ref.WeakReference;

import static com.knox.kavrecorder.utils.KTypeConversion.int2BeBytes;
import static com.knox.kavrecorder.utils.KTypeConversion.intTo16bits;
import static com.knox.kavrecorder.utils.KTypeConversion.intTo8bits;
import static com.knox.kavrecorder.utils.KTypeConversion.uint32ToBeBytes;

/**
 * @author Knox.Tsang
 * @time 2017/9/25  16:30
 * @desc ${TODD}
 */


public class StreamWrapper {

    private static final String TAG = "StreamWrapper";

    private static final String THREAD_NAME = "STM";

    private static final int MSG_OPEN = 0x101;
    private static final int MSG_PUSH = 0x102;
    private static final int MSG_CLOSE = 0x103;

    private static StreamWrapper mInstance = null;
    private Handler mStHandler;
    private StreamThread mStreamThread;
    private KTcpClient mClient;
    private IStreamWrapper mListener;
    private BufferedOutputStream mVbos;
    private byte[] mPSBuf = null;
    private boolean mPS = false;
    private boolean mVDump = false;

    private StreamWrapper() {
        mStreamThread = new StreamThread(this);
        mStreamThread.start();
    }

    public static StreamWrapper getInstance() {
        if (mInstance == null) {
            synchronized (ClientWrapper.class) {
                if (mInstance == null) {
                    mInstance = new StreamWrapper();
                }
            }
        }
        return mInstance;
    }

    public void connect(String ip, int port) {
        if (TextUtils.isEmpty(ip) || port == 0) {
            return;
        }
        mStHandler.obtainMessage(MSG_OPEN, new SimpleUrlBean(ip, port)).sendToTarget();
    }

    public void disconnect() {
        mStHandler.obtainMessage(MSG_CLOSE, null).sendToTarget();
    }

    public void write264Es(byte[] buff, long timeStamp, int width, int height, int fps) {
        if (timeStamp == -1) {
            //sps pps
            mPSBuf = buff;
            mPS = true;
            return;
        } else {
            EsBufferBean buffer = new EsBufferBean();
            buffer.timeStamp = timeStamp;
            buffer.width = width;
            buffer.height = height;
            buffer.fps = fps;

            if (mPS) {
                byte[] buff2 = null;
                int size = buff.length;
                size += mPSBuf.length;
                buff2 = new byte[size];
                //sps pps
                System.arraycopy(mPSBuf, 0, buff2, 0, mPSBuf.length);
                //key frame
                System.arraycopy(buff, 0, buff2, mPSBuf.length, buff.length);
                mPS = false;
                buffer.buff = buff2;
            } else {
                buffer.buff = buff;
            }
            mStHandler.obtainMessage(MSG_PUSH, buffer).sendToTarget();
        }
    }

    public interface IStreamWrapper {
        void onStreamOpened();
    }

    public void setOnListener(IStreamWrapper listener) {
        mListener = listener;
    }

    static class StreamThread extends Thread {
        private WeakReference<StreamWrapper> mStreamWrapper;


        public StreamThread(StreamWrapper streamWrapper) {
            super(THREAD_NAME);
            mStreamWrapper = new WeakReference<>(streamWrapper);
        }

        @Override
        public void run() {
            Looper.prepare();
            mStreamWrapper.get().mStHandler = new Handler() {
                @Override
                public void handleMessage(Message msg) {
                    Log.e(TAG, "handleMessage: " + Thread.currentThread().getName() + ": " + Integer.toHexString(msg.what));
                    switch (msg.what) {
                        case MSG_OPEN:
                            openStreamSocket((SimpleUrlBean) msg.obj);
                            break;

                        case MSG_PUSH:
                            pushEsStream((EsBufferBean) msg.obj);
                            break;

                        case MSG_CLOSE:
                            closeStreamSocket();
                            break;
                    }
                }
            };
            Looper.loop();
        }

        private void closeStreamSocket() {
            StreamWrapper streamWrapper = mStreamWrapper.get();
            KTcpClient client = mStreamWrapper.get().mClient;
            if (client != null) {
                client.release();
                client = null;
            }

            if (streamWrapper.mVbos != null) {
                try {
                    streamWrapper.mVbos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                streamWrapper.mVbos = null;
            }
        }

        static int pushCnt = 0;
        static int seq = 0;
        private void pushEsStream(EsBufferBean buffer) {
            byte[] buf = buffer.buff;
            pushCnt++;
            //TODO parseRtpPacket
            int readPos = 0;
            for (int i = 0; i <= (buf.length - 1) / 60_000; i++) {

                RtpPayloadBean payload = new RtpPayloadBean();
                payload.seq = ++seq;
                payload.timeStamp = 0;
                payload.ssrc = 0;
                payload.timeStamp = buffer.timeStamp;
                if (readPos + 60_000 >= buf.length) {
                    // 这次可以读完
                    if (i == 0)
                        payload.pType = 7;
                    else
                        payload.pType = 4;
                    byte[] buff2 = new byte[buf.length - readPos];
                    System.arraycopy(buf, readPos, buff2, 0, buf.length - readPos);
                    payload.mediaPayload = buff2;
                } else {
                    // 这次不能读完
                    if (i == 0)
                        payload.pType = 1;
                    else
                        payload.pType = 2;
                    byte[] buff2 = new byte[60_000];
                    System.arraycopy(buf, readPos, buff2, 0, 60_000);
                    payload.mediaPayload = buff2;
                    readPos += 60_000;
                }
                RtpPacketBean packet = new RtpPacketBean();
                packet.channel = buffer.channel;
                if (packet.channel == 0) {
                    // video
                    payload.fps = buffer.fps;
                    payload.width = buffer.width;
                    payload.height = buffer.height;
                } else if (packet.channel == 1) {
                    // audio
                    payload.audioSample = buffer.audioSample;
                }
                packet.length = 36 + payload.mediaPayload.length;
                packet.payload = payload;

                byte[] bs = ParseBeanUtil.parseRtpPacket(packet);

                //TODO send2Server
                /*
                Log.e(TAG, "pushEsStream: seq " + seq + ": " + bytes2Hex0(bs));
                if (mStreamWrapper.get() == null) {
                    Log.e(TAG, "pushEsStream: mStreamWrapper.get() == null");
                } else if (mStreamWrapper.get().mClient == null) {
                    Log.e(TAG, "pushEsStream: mStreamWrapper.get().mClient == null");
                } else {
                    mStreamWrapper.get().mClient.writeNoResponse(bs);
                }
                */
                StreamWrapper streamWrapper = mStreamWrapper.get();
                // Log.e(TAG, "pushEsStream: media data size: " + payload.mediaPayload.length);
                if (streamWrapper.mVbos != null) {
                    byte[] dumpBufs = new byte[payload.mediaPayload.length];
                    System.arraycopy(payload.mediaPayload, 0, dumpBufs, 0, payload.mediaPayload.length);
                    try {
                        streamWrapper.mVbos.write(dumpBufs);
                        streamWrapper.mVbos.flush();
                    } catch (IOException e) {
                        Log.e(TAG, "pushEsStream: IOException");
                        e.printStackTrace();
                    }
                }
                streamWrapper.mClient.writeNoResponse(bs);
            }
        }

        private void openStreamSocket(SimpleUrlBean simpleUrl) {
            Log.e(TAG, "openStreamSocket: " + simpleUrl.ip + ":" + simpleUrl.port);
            StreamWrapper streamWrapper = mStreamWrapper.get();

            if (streamWrapper.mVDump) {
                File file = new File(Environment.getExternalStorageDirectory(), "Download/encode/" +
                        "VRecorder-" + System.currentTimeMillis() + ".264");
                Log.e(TAG, "StreamWrapper: file " + file);
                try {
                    streamWrapper.mVbos = new BufferedOutputStream(new FileOutputStream(file));
                    Log.e(TAG, "StreamWrapper: mVbos " + streamWrapper.mVbos);
                } catch (FileNotFoundException e) {
                    Log.e(TAG, "StreamWrapper: FileNotFoundException");
                    e.printStackTrace();
                }
            }

            streamWrapper.mClient = new KTcpClient(simpleUrl.ip, simpleUrl.port);
            Log.e(TAG, "openStreamSocket: " + "tcp stream client ok" + streamWrapper.mClient);
            if (streamWrapper.mListener != null)
                streamWrapper.mListener.onStreamOpened();

            //TODO listen stream port
        }
    }
}
