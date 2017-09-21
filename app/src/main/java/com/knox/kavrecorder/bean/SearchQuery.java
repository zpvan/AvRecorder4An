package com.knox.kavrecorder.bean;

/**
 * @author Knox.Tsang
 * @time 2017/9/20  16:18
 * @desc ${TODD}
 */


public class SearchQuery {
    public int deviceType;
    public int deviceFunction;

    public SearchQuery() {}

    public SearchQuery(int deviceType, int deviceFunction) {
        this.deviceType = deviceType;
        this.deviceFunction = deviceFunction;
    }

    @Override
    public String toString() {
        return "SearchQuery{" +
                "deviceType=" + deviceType +
                ", deviceFunction=" + deviceFunction +
                '}';
    }
}
