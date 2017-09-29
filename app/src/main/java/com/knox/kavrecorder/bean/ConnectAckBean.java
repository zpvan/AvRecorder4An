package com.knox.kavrecorder.bean;

/**
 * @author Knox.Tsang
 * @time 2017/9/25  9:09
 * @desc ${TODD}
 */


public class ConnectAckBean {
    public MsgHeaderBean header;  // 24byte
    public long ackCode;          // 4byte
    public long id;               // 4byte
    public long deviceType;       // 4byte

    @Override
    public String toString() {
        return "ConnectAckBean{" +
                "header=" + header +
                ", ackCode=" + ackCode +
                ", id=" + id +
                ", deviceType=" + deviceType +
                '}';
    }
}
