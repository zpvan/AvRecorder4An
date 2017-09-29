package com.knox.kavrecorder.utils;

import android.util.Log;

import com.knox.kavrecorder.bean.CancelPresentAckBean;
import com.knox.kavrecorder.bean.ConnectAckBean;
import com.knox.kavrecorder.bean.KeepAliveAckBean;
import com.knox.kavrecorder.bean.MsgHeaderBean;
import com.knox.kavrecorder.bean.PresentAckBean;

import static com.knox.kavrecorder.utils.KTypeConversion.beBytes2Uint32;

/**
 * @author Knox.Tsang
 * @time 2017/9/29  10:47
 * @desc ${TODD}
 */


public class PackBeanUtil {

    private static final String TAG = "PackBeanUtil";

    public static MsgHeaderBean packMsgHeader(byte[] bs) {
        MsgHeaderBean msgHeader = new MsgHeaderBean();
        int rPos = 0;
        msgHeader.length = beBytes2Uint32(bs, rPos);
        rPos += 4;
        msgHeader.id = beBytes2Uint32(bs, rPos);
        rPos += 4;
        msgHeader.sequence = beBytes2Uint32(bs, rPos);
        rPos += 4;
        msgHeader.version = beBytes2Uint32(bs, rPos);
        rPos += 4;
        msgHeader.srcNo1 = beBytes2Uint32(bs, rPos);
        rPos += 4;
        msgHeader.srcNo2 = beBytes2Uint32(bs, rPos);
        return msgHeader;
    }

    public static ConnectAckBean packConnectAck(byte[] bs, int offset) {
        ConnectAckBean connectAck = new ConnectAckBean();
        int rPos = offset;
        connectAck.ackCode = beBytes2Uint32(bs, rPos);
        rPos += 4;
        connectAck.id = beBytes2Uint32(bs, rPos);
        rPos += 4;
        connectAck.deviceType = beBytes2Uint32(bs, rPos);
        return connectAck;
    }

    public static PresentAckBean packPresentAck(byte[] bs, int offset) {
        PresentAckBean presentAck = new PresentAckBean();
        int rPos = offset;
        presentAck.ackCode = beBytes2Uint32(bs, rPos);
        rPos += 4;
        presentAck.port = beBytes2Uint32(bs, rPos);
        rPos += 4;
        //server don't send this field
        presentAck.conflictCnt = beBytes2Uint32(bs, rPos);
        rPos += 4;
        if (presentAck.conflictCnt > 0) {
            Log.e(TAG, "packPresentAck: conflictCnt > 0");
            //TODO packArrayList<ConflictBean>
        }
        return presentAck;
    }

    public static KeepAliveAckBean packKeepAliveAck(byte[] bs, int offset) {
        KeepAliveAckBean keepAliveAck = new KeepAliveAckBean();
        int rPos = offset;
        keepAliveAck.ackCode = beBytes2Uint32(bs, rPos);
        return keepAliveAck;
    }

    public static CancelPresentAckBean packCancelPresentAck(byte[] bs, int offset) {
        CancelPresentAckBean cancelPresentAck = new CancelPresentAckBean();
        int rPos = offset;
        cancelPresentAck.ackCode = beBytes2Uint32(bs, rPos);
        return cancelPresentAck;
    }
}
