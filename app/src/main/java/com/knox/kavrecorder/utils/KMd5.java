package com.knox.kavrecorder.utils;

import java.security.MessageDigest;

/**
 * @author Knox.Tsang
 * @time 2017/9/27  9:58
 * @desc ${TODD}
 */


public class KMd5 {
    public static String getMD5(String message) {
        String md5str = "";
        try {
            // 1 创建一个提供信息摘要算法的对象，初始化为md5算法对象
            MessageDigest md = MessageDigest.getInstance("MD5");

            // 2 将消息变成byte数组
            byte[] input = message.getBytes();

            // 3 计算后获得字节数组,这就是那128位了
            byte[] buff = md.digest(input);

            // 4 把数组每一字节（一个字节占八位）换成16进制连成md5字符串
            md5str = bytes2Hex0(buff);

        } catch (Exception e) {
            e.printStackTrace();
        }
        return md5str;
    }

    public static String bytes2Hex0(byte[] buffer)
    {
        final String HEX = "0123456789ABCDEF";
        StringBuilder sb = new StringBuilder(buffer.length * 2);
        for (byte b : buffer)
        {
            // 取出这个字节的高4位，然后与0x0f与运算，得到一个0-15之间的数据，通过HEX.charAt(0-15)即为16进制数
            sb.append(HEX.charAt((b >> 4) & 0x0f));
            // 取出这个字节的低位，与0x0f与运算，得到一个0-15之间的数据，通过HEX.charAt(0-15)即为16进制数
            sb.append(HEX.charAt(b & 0x0f));
        }

        return sb.toString();
    }
}
