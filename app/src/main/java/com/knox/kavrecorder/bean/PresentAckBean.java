package com.knox.kavrecorder.bean;

import java.util.ArrayList;

/**
 * @author Knox.Tsang
 * @time 2017/9/25  11:56
 * @desc ${TODD}
 */


public class PresentAckBean {
    public MsgHeaderBean header;               // 24byte
    public long ackCode;                       // 4byte
    public long port;                          // 4byte
    public long conflictCnt;                   // 4byte
    public ArrayList<ConflictBean> conflicts;

    @Override
    public String toString() {
        return "PresentAckBean{" +
                "header=" + header +
                ", ackCode=" + ackCode +
                ", port=" + port +
                ", conflictCnt=" + conflictCnt +
                ", conflicts=" + conflicts +
                '}';
    }
}
