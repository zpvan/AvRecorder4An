package com.knox.kavrecorder.bean;

import static com.knox.kavrecorder.utils.KTypeConversion.uint32ToBeBytes;

/**
 * @author Knox.Tsang
 * @time 2017/9/21  9:25
 * @desc ${TODD}
 */


public class MsgHeaderBean {
    public long length;     // 4byte
    public long id;         // 4byte
    public long sequence;   // 4byte
    public long version;    // 4byte
    public long srcNo1;     // 4byte
    public long srcNo2;     // 4byte

    @Override
    public String toString() {
        return "MsgHeaderBean{" +
                "length=" + length +
                ", id=" + id +
                ", sequence=" + sequence +
                ", version=" + version +
                ", srcNo1=" + srcNo1 +
                ", srcNo2=" + srcNo2 +
                '}';
    }
}
