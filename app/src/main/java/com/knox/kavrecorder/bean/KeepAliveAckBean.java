package com.knox.kavrecorder.bean;

/**
 * @author Knox.Tsang
 * @time 2017/9/25  14:37
 * @desc ${TODD}
 */


public class KeepAliveAckBean {
    public MsgHeaderBean header;  // 24byte
    public long ackCode;          // 4byte

    @Override
    public String toString() {
        return "KeepAliveAckBean{" +
                "header=" + header +
                ", ackCode=" + ackCode +
                '}';
    }
}
