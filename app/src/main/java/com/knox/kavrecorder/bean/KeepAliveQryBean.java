package com.knox.kavrecorder.bean;

/**
 * @author Knox.Tsang
 * @time 2017/9/25  14:24
 * @desc ${TODD}
 */


public class KeepAliveQryBean {
    public MsgHeaderBean header;  // 24byte
    public long type;             // 4byte
    public long id;               // 4byte

    @Override
    public String toString() {
        return "KeepAliveQryBean{" +
                "header=" + header +
                ", type=" + type +
                ", id=" + id +
                '}';
    }
}
