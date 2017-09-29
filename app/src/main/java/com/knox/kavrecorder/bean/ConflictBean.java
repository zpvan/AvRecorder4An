package com.knox.kavrecorder.bean;

/**
 * @author Knox.Tsang
 * @time 2017/9/25  11:58
 * @desc ${TODD}
 */


public class ConflictBean {
    public long pos;        // 4byte
    public int nameLength;  // 4byte
    public String name;

    @Override
    public String toString() {
        return "ConflictBean{" +
                "pos=" + pos +
                ", nameLength=" + nameLength +
                ", name='" + name + '\'' +
                '}';
    }
}
