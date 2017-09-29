package com.knox.kavrecorder.utils;

/**
 * @author Knox.Tsang
 * @time 2017/9/21  16:25
 * @desc ${TODD}
 */


public class KTypeConversion {
    public static byte[] int2LeBytes(int value) {
        int temp = value;
        byte[] bs = new byte[4];
        for (int i = 0; i < bs.length; i++) {
            bs[i] = new Integer(temp & 0xFF).byteValue();//将最低位保存在最低位
            temp = temp >> 8; // 向右移8位
        }
        return bs;
    }

    public static byte[] int2BeBytes(int value) {
        int temp = value;
        byte[] bs = new byte[4];
        for (int i = 0; i < bs.length; i++) {
            bs[bs.length - 1 - i] = new Integer(temp & 0xFF).byteValue();//将最低位保存在最高位
            temp = temp >> 8; // 向右移8位
        }
        return bs;
    }

    public static int leBytes2Int(byte[] bs, int offset) {
        int value;
        value = (int) ((bs[offset] & 0xFF)
                | ((bs[offset + 1] & 0xFF) << 8)
                | ((bs[offset + 2] & 0xFF) << 16)
                | ((bs[offset + 3] & 0xFF) << 24));
        return value;
    }

    public static int beBytes2Int(byte[] bs, int offset) {
        int value;
        value = (int) ((bs[offset] & 0xFF) << 24
                | ((bs[offset + 1] & 0xFF) << 16)
                | ((bs[offset + 2] & 0xFF) << 8)
                | ((bs[offset + 3] & 0xFF)));
        return value;
    }

    public static String bytes2String(byte[] bs, int offset, int length) {
        int index = 0;
        while (index < length) {
            if (bs[offset + index] == 0x00)
                break;
            index++;
        }

        return (index > 0) ? new String(bs, offset, index) : new String("0");
    }

    public static byte[] uint32ToBeBytes(long number) {
        long temp = number;
        byte[] b = new byte[4];
        for (int i = 0; i < b.length; i++) {
            b[b.length - 1 - i] = new Long(temp & 0xFF).byteValue();
            temp >>= 8;
        }
        return b;
    }

    public static long beBytes2Uint32(byte[] bs, int offset) {
        if (bs.length <= offset + 3)
            return 0;

        long value;
        value = (long) ((bs[offset] & 0xFF) << 24
                | ((bs[offset + 1] & 0xFF) << 16)
                | ((bs[offset + 2] & 0xFF) << 8)
                | ((bs[offset + 3] & 0xFF)));
        return value;
    }
}
