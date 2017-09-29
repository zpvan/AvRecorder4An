package com.knox.kavrecorder.utils;

import android.util.Log;

import com.knox.kavrecorder.bean.CancelPresentQryBean;
import com.knox.kavrecorder.bean.ConnectQryBean;
import com.knox.kavrecorder.bean.KeepAliveQryBean;
import com.knox.kavrecorder.bean.MsgHeaderBean;
import com.knox.kavrecorder.bean.PresentQryBean;
import com.knox.kavrecorder.bean.SearchQryBean;

import java.util.Arrays;

import static com.knox.kavrecorder.utils.KTypeConversion.int2BeBytes;
import static com.knox.kavrecorder.utils.KTypeConversion.int2LeBytes;
import static com.knox.kavrecorder.utils.KTypeConversion.uint32ToBeBytes;

/**
 * @author Knox.Tsang
 * @time 2017/9/29  10:35
 * @desc ${TODD}
 */


public class ParseBeanUtil {

    private static final String TAG = "ParseBeanUtil";

    public static byte[] parseQuery(SearchQryBean queryBean) {
        byte[] data = new byte[8];
        int validSize = 0;

        byte[] deviceType = int2LeBytes(queryBean.deviceType);
        byte[] deviceFunc = int2LeBytes(queryBean.deviceFunction);

        System.arraycopy(deviceType, 0, data, validSize, deviceType.length);
        validSize += deviceType.length;
        System.arraycopy(deviceFunc, 0, data, validSize, deviceFunc.length);
        validSize += deviceFunc.length;
        Log.e(TAG, "parseQuery: " + Arrays.toString(data));
        return data;
    }

    public static byte[] parseMsgHeader(MsgHeaderBean header) {
        byte[] b = new byte[24];
        int wPos = 0;
        System.arraycopy(uint32ToBeBytes(header.length), 0, b, wPos, 4);
        wPos += 4;
        System.arraycopy(uint32ToBeBytes(header.id), 0, b, wPos, 4);
        wPos += 4;
        System.arraycopy(uint32ToBeBytes(header.sequence), 0, b, wPos, 4);
        wPos += 4;
        System.arraycopy(uint32ToBeBytes(header.version), 0, b, wPos, 4);
        wPos += 4;
        System.arraycopy(uint32ToBeBytes(header.srcNo1), 0, b, wPos, 4);
        wPos += 4;
        System.arraycopy(uint32ToBeBytes(header.srcNo2), 0, b, wPos, 4);
        return b;
    }

    public static byte[] parseConnect(ConnectQryBean query) {
        query.nameLength = query.name.getBytes().length + 4;
        int size = 32 + query.nameLength;
        byte[] b = new byte[size];
        int wPos = 0;
        System.arraycopy(parseMsgHeader(query.header), 0, b, wPos, 24);
        wPos += 24;
        int zeroNum = 4 - query.code.getBytes().length;
        while (zeroNum > 0) {
            query.code = 0 + query.code;
            zeroNum--;
        }
        System.arraycopy(query.code.getBytes(), 0, b, wPos, 4);
        wPos += 4;
        System.arraycopy(int2BeBytes(query.nameLength), 0, b, wPos, 4);
        wPos += 4;
        System.arraycopy(query.name.getBytes(), 0, b, wPos, query.nameLength - 4);
        return b;
    }

    public static byte[] parsePresent(PresentQryBean query) {
        byte[] b = new byte[32];
        int wPos = 0;
        System.arraycopy(parseMsgHeader(query.header), 0, b, wPos, 24);
        wPos += 24;
        System.arraycopy(uint32ToBeBytes(query.position), 0, b, wPos, 4);
        wPos += 4;
        System.arraycopy(uint32ToBeBytes(query.force), 0, b, wPos, 4);
        return b;
    }

    public static byte[] parseKeepAlive(KeepAliveQryBean query) {
        byte[] b = new byte[32];
        int wPos = 0;
        System.arraycopy(parseMsgHeader(query.header), 0, b, wPos, 24);
        wPos += 24;
        System.arraycopy(uint32ToBeBytes(query.type), 0, b, wPos, 4);
        wPos += 4;
        System.arraycopy(uint32ToBeBytes(query.id), 0, b, wPos, 4);
        return b;
    }

    public static byte[] parseCancelPresent(CancelPresentQryBean query) {
        byte[] b = new byte[24];
        int wPos = 0;
        System.arraycopy(parseMsgHeader(query.header), 0, b, wPos, 24);
        return b;
    }
}
