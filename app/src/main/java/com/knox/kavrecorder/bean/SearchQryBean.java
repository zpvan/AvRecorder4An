package com.knox.kavrecorder.bean;

/**
 * @author Knox.Tsang
 * @time 2017/9/20  16:18
 * @desc ${TODD}
 */


public class SearchQryBean {
    public int deviceType;
    public int deviceFunction;

    public SearchQryBean() {}

    public SearchQryBean(int deviceType, int deviceFunction) {
        this.deviceType = deviceType;
        this.deviceFunction = deviceFunction;
    }

    @Override
    public String toString() {
        return "SearchQryBean{" +
                "deviceType=" + deviceType +
                ", deviceFunction=" + deviceFunction +
                '}';
    }
}
