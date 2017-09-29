package com.knox.kavrecorder.bean;

/**
 * @author Knox.Tsang
 * @time 2017/9/26  10:13
 * @desc ${TODD}
 */


public class RtpPayloadBean {
    public int vpxccm = 0x90;       //8bit
    public byte pt = 0x21;          //8bit
    public int seq;                 //16bit
    public long timeStamp;          //32bit
    public int ssrc = 0;            //32bit
    public int extProfile = 0x4246; //16bit
    public int length = 5;          //16bit
    public byte version = 5;        //8bit
    public byte fType;              //4bit
    public byte pType;              //4bit
    public int width;               //16bit
    public int height;              //16bit
    public int reserved0;           //8bit
    public int reserved1;           //8bit
    public int reserved2;           //32bit
    public int reserved3;           //32bit
    public int fps = 0;             //8bit
    public int audioSample;         //8bit
    public int hdcp = 0;            //1bit
    public int reserved4;           //15bit

    public byte[] mediaPayload;
}
