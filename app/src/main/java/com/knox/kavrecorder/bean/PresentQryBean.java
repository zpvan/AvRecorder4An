package com.knox.kavrecorder.bean;

/**
 * @author Knox.Tsang
 * @time 2017/9/25  10:00
 * @desc ${TODD}
 */


public class PresentQryBean {
    public MsgHeaderBean header;  // 24byte
    public long position;     // 4byte
    public long force;        // 4byte

    @Override
    public String toString() {
        return "PresentQryBean{" +
                "header=" + header +
                ", position=" + position +
                ", force=" + force +
                '}';
    }
}
