package com.knox.kavrecorder.bean;

/**
 * @author Knox.Tsang
 * @time 2017/9/21  8:46
 * @desc ${TODD}
 */


public class ConnectQryBean {
    public MsgHeaderBean header;  // 24byte
    public String code;           // 4byte
    public int nameLength;        // 4byte
    public String name;

    @Override
    public String toString() {
        return "ConnectQryBean{" +
                "header=" + header +
                ", code='" + code + '\'' +
                ", nameLength=" + nameLength +
                ", name='" + name + '\'' +
                '}';
    }
}