package com.knox.kavrecorder.net;

import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;

import com.knox.kavrecorder.bean.CancelPresentQryBean;
import com.knox.kavrecorder.bean.ConnectAckBean;
import com.knox.kavrecorder.bean.ConnectQryBean;
import com.knox.kavrecorder.bean.KeepAliveAckBean;
import com.knox.kavrecorder.bean.KeepAliveQryBean;
import com.knox.kavrecorder.bean.MsgHeaderBean;
import com.knox.kavrecorder.bean.PresentAckBean;
import com.knox.kavrecorder.bean.PresentQryBean;
import com.knox.kavrecorder.bean.SimpleUrlBean;
import com.knox.kavrecorder.constant.MsgDefine;
import com.knox.kavrecorder.utils.ParseBeanUtil;

import java.lang.ref.WeakReference;

import static com.knox.kavrecorder.utils.PackBeanUtil.packConnectAck;
import static com.knox.kavrecorder.utils.PackBeanUtil.packKeepAliveAck;
import static com.knox.kavrecorder.utils.PackBeanUtil.packMsgHeader;
import static com.knox.kavrecorder.utils.PackBeanUtil.packPresentAck;

/**
 * @author Knox.Tsang
 * @time 2017/9/22  8:50
 * @desc ${TODD}
 */


public class ClientWrapper implements KTcpClient.IReceiver {

    private static final String TAG = "ClientWrapper";

    private static final String THREAD_NAME = "CLT";

    private static final int MSG_CONNECT       = 0x101;
    private static final int MSG_PRESENT       = 0x102;
    private static final int MSG_KEEPALIVE     = 0x103;
    private static final int MSG_CANCELPRESENT = 0x104;
    private static final int MSG_RELEASE       = 0x105;

    private static final String FOUR_CHARS_CHECK_CODE = "1359";

    private static final int TYPE_KA_PRESENTATION = 3;
    private static final int ID_KA_PRESENTATION   = 0xFFFF_FFFF;

    private static final int POSITION_PS_FULL_SCREEN  = 0;
    private static final int POSITION_PS_LEFT_HALF    = 1;
    private static final int POSITION_PS_RIGHT_HALF   = 2;
    private static final int POSITION_PS_LEFT_TOP     = 3;
    private static final int POSITION_PS_RIGHT_TOP    = 4;
    private static final int POSITION_PS_LEFT_BOTTOM  = 5;
    private static final int POSITION_PS_RIGHT_BOTTOM = 6;

    private static final int FORCE_PS_FALSE = 0;
    private static final int FORCE_PS_TURE  = 1;

    private static final int DELAY_TEN_SECONDS = 10 * 1000;

    private KTcpClient mClient;
    private ClientThread mClientThread;
    private Handler mCtHandler;
    private IClientWrapper mListener;
    private boolean mIsPresenting;

    //Singleton mode
    private static ClientWrapper mInstance = null;

    private ClientWrapper() {
        mClientThread = new ClientThread(this);
        //TODO how to release this type of thread, loop by Looper. here I use singleton mode
        mClientThread.start();
    }

    public static ClientWrapper getInstance() {
        if (mInstance == null) {
            synchronized (ClientWrapper.class) {
                if (mInstance == null) {
                    mInstance = new ClientWrapper();
                }
            }
        }
        return mInstance;
    }

    public interface IClientWrapper {
        void onPresent(long port);
        void onKickOff();
    }

    public void setOnListener(IClientWrapper listener) {
        mListener = listener;
    }

    public void connect(String ip, int port) {
        if (TextUtils.isEmpty(ip) || port == 0) {
            Log.e(TAG, "connect: ip " + ip + ":" + port);
            return;
        }
        mCtHandler.obtainMessage(MSG_CONNECT, new SimpleUrlBean(ip, port)).sendToTarget();
    }

    public void disconnect() {
        if (mIsPresenting)
            cancelPresent();
        else
            mCtHandler.obtainMessage(MSG_RELEASE, null).sendToTarget();

    }

    private void release() {
        if (mClient != null) {
            mClient.release();
            mClient = null;
        }
    }

    private void cancelPresent() {
        Log.e(TAG, "cancelPresent: ");
        mCtHandler.obtainMessage(MSG_CANCELPRESENT, null).sendToTarget();
    }

    @Override
    public void onReceive(byte[] datas, int length) {
        //Log.e(TAG, "onReceive: " + ", thread: " + Thread.currentThread().getName() + ", data: " + Arrays.toString(datas));
        byte[] bs = new byte[length];
        System.arraycopy(datas, 0, bs, 0, length);
        MsgHeaderBean msgHeader = packMsgHeader(bs);
        Log.e(TAG, "onReceive: id " + Long.toHexString(msgHeader.id));

        if (msgHeader.id == MsgDefine.ID_Client_Connect_Ack) {
            ConnectAckBean connectAck = packConnectAck(bs, 24);
            if (connectAck.ackCode == 0) {
                mCtHandler.obtainMessage(MSG_PRESENT, null).sendToTarget();
            } else {
                Log.e(TAG, "onReceive: connectAck.ackCode " + connectAck.ackCode);
            }
        } else if (msgHeader.id == MsgDefine.ID_Client_Present_Ack) {
            PresentAckBean presentAck = packPresentAck(bs, 24);
            //Log.e(TAG, "onReceive: present port " + presentAck.port);
            if (presentAck.ackCode == 0) {
                mIsPresenting = true;
                //TODO new tcp 4 stream transfer
                if (mListener != null)
                    mListener.onPresent(presentAck.port);

                //TODO keep alive
                mCtHandler.obtainMessage(MSG_KEEPALIVE, null).sendToTarget();
            } else {
                Log.e(TAG, "onReceive: presentAck.ackCode " + presentAck.ackCode);
            }
        } else if (msgHeader.id == MsgDefine.ID_KeepAlive_Ack) {
            Log.e(TAG, "onReceive: keepAliveAck");
            KeepAliveAckBean keepAliveAck = packKeepAliveAck(bs, 24);
            if (keepAliveAck.ackCode == 0) {
                mCtHandler.sendMessageDelayed(mCtHandler.obtainMessage(MSG_KEEPALIVE, null), DELAY_TEN_SECONDS);
            } else {
                Log.e(TAG, "onReceive: keepAliveAck.ackCode " + keepAliveAck.ackCode);
            }
        } else if (msgHeader.id == MsgDefine.ID_Client_Kick) {
            mIsPresenting = false;
            //TODO remote device Preempted
            //TODO stream push should be closed
            if (mListener != null)
                mListener.onKickOff();
        } else if (msgHeader.id == MsgDefine.ID_Client_Cancel_Present_Ack) {
            mIsPresenting = false;
            //CancelPresentAckBean cancelPresentAck = packCancelPresentAck(bs, 24);
            mCtHandler.obtainMessage(MSG_PRESENT, null).sendToTarget();
        }
    }

    static class ClientThread extends Thread {

        private WeakReference<ClientWrapper> mClientWrapper;


        public ClientThread(ClientWrapper clientWrapper) {
            super(THREAD_NAME);
            mClientWrapper = new WeakReference<>(clientWrapper);
        }

        @Override
        public void run() {
            Looper.prepare();
            mClientWrapper.get().mCtHandler = new Handler() {
                @Override
                public void handleMessage(Message msg) {
                    Log.e(TAG, "handleMessage: " + Thread.currentThread().getName() + ": " + Integer.toHexString(msg.what));
                    switch (msg.what) {
                        case MSG_CONNECT:
                            connect2server((SimpleUrlBean) msg.obj);
                            break;

                        case MSG_PRESENT:
                            present2server();
                            break;

                        case MSG_KEEPALIVE:
                            keepAlive2server();
                            break;

                        case MSG_CANCELPRESENT:
                            cancelPresent2server();
                            break;

                        case MSG_RELEASE:
                            mClientWrapper.get().release();
                            break;
                    }
                }
            };
            Looper.loop();
        }

        private void connect2server(SimpleUrlBean simpleUrl) {
            ClientWrapper clientWrapper = mClientWrapper.get();
            clientWrapper.mClient = new KTcpClient(simpleUrl.ip, simpleUrl.port);
            //TODO if new Socket getOutputStream getInputStream failed, should report to user
            KTcpClient client = clientWrapper.mClient;
            client.setOnRevListener(clientWrapper);

            ConnectQryBean query = new ConnectQryBean();
            MsgHeaderBean header = new MsgHeaderBean();
            header.id = MsgDefine.ID_Client_Connect;
            query.header = header;
            query.code = FOUR_CHARS_CHECK_CODE;
            query.name = Build.BRAND + "-" + Build.MODEL;
            //Log.e(TAG, "connect2server: " + Arrays.toString(clientWrapper.parseConnect(query)));
            client.writeAndResponse(ParseBeanUtil.parseConnect(query));
        }

        private void present2server() {
            KTcpClient client = mClientWrapper.get().mClient;

            PresentQryBean query = new PresentQryBean();
            MsgHeaderBean header = new MsgHeaderBean();
            header.id = MsgDefine.ID_Client_Present;
            query.header = header;
            query.position = POSITION_PS_FULL_SCREEN;
            query.force = FORCE_PS_TURE;
            //Log.e(TAG, "present2server: " + Arrays.toString(clientWrapper.parsePresent(query)));
            client.writeAndResponse(ParseBeanUtil.parsePresent(query));
        }

        private void keepAlive2server() {
            KTcpClient client = mClientWrapper.get().mClient;

            KeepAliveQryBean query = new KeepAliveQryBean();
            MsgHeaderBean header = new MsgHeaderBean();
            header.id = MsgDefine.ID_KeepAlive;
            query.header = header;
            query.type = TYPE_KA_PRESENTATION;
            query.id = ID_KA_PRESENTATION;
            //Log.e(TAG, "keepAlive2server: " + Arrays.toString(clientWrapper.parseKeepAlive(query)));
            if (client != null)
                client.writeAndResponse(ParseBeanUtil.parseKeepAlive(query));
        }

        private void cancelPresent2server() {
            KTcpClient client = mClientWrapper.get().mClient;

            CancelPresentQryBean query = new CancelPresentQryBean();
            MsgHeaderBean header = new MsgHeaderBean();
            header.id = MsgDefine.ID_Client_Cancel_Present;
            query.header = header;
            //Log.e(TAG, "cancelPresent2server: " + bytes2Hex0(clientWrapper.parseCancelPresent(query)));
            if (client != null) {
                Log.e(TAG, "cancelPresent2server: ");
                client.writeAndResponse(ParseBeanUtil.parseCancelPresent(query));
            }
        }
    }
}