package com.knox.kavrecorder.bean;

/**
 * @author Knox.Tsang
 * @time 2017/9/27  8:54
 * @desc ${TODD}
 */


public class CancelPresentAckBean {
    public MsgHeaderBean header;  // 24byte
    public long ackCode;          // 4byte

    @Override
    public String toString() {
        return "CancelPresentAckBean{" +
                "header=" + header +
                ", ackCode=" + ackCode +
                '}';
    }
}
