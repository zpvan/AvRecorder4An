package com.knox.kavrecorder.bean;

/**
 * @author Knox.Tsang
 * @time 2017/9/26  10:11
 * @desc ${TODD}
 */


public class RtpPacketBean {
    public byte magic = 0x24;      //8bit '$'
    public byte channel;           //8bit
    public int length;             //16bit
    public RtpPayloadBean payload;
}
