package com.knox.kavrecorder.bean;

/**
 * @author Knox.Tsang
 * @time 2017/9/20  16:25
 * @desc ${TODD}
 */


public class SearchRlyBean {

    private static final String TAG = "SearchRlyBean";

    public int deviceType;      // byte[4]
    public int deviceFunction;  // byte[4]
    public String deviceStatus; // byte[32]
    public String deviceName;   // byte[64]
    public String deviceAlias;  // byte[64]
    public String reserved;     // byte[128]
    public String serverIp;

    @Override
    public String toString() {
        return "SearchRlyBean{" +
                "deviceType=" + deviceType +
                ", deviceFunction=" + deviceFunction +
                ", deviceStatus='" + deviceStatus + '\'' +
                ", deviceName='" + deviceName + '\'' +
                ", deviceAlias='" + deviceAlias + '\'' +
                ", reserved='" + reserved + '\'' +
                ", serverIp='" + serverIp + '\'' +
                '}';
    }
}
