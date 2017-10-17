package com.knox.kavrecorder.bean;

/**
 * @author Knox.Tsang
 * @time 2017/9/20  16:25
 * @desc ${TODD}
 */


public class SearchRlyBean {

    private static final String TAG = "SearchRlyBean";

    public int deviceType;      // 4byte
    public int deviceFunction;  // 4byte
    public String deviceStatus; // 4byte
    public String deviceName;   // 64byte
    public String deviceAlias;  // 64byte
    public String reserved;     // 128byte
    public String serverIp;

    public boolean isVisible = false;

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
