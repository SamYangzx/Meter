package com.android.meter.meter.util;

import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.text.NumberFormat;

import static android.R.attr.src;

/**
 * Created by fenghe on 2017/6/7.
 */

public class StringUtil {
    private static final String TAG = StringUtil.class.getSimpleName();

    private static final int MAX_DIGIT = 4;

//    public static String big2(double d) {
//        BigDecimal d1 = new BigDecimal(Double.toString(d));
//        BigDecimal d2 = new BigDecimal(Integer.toString(1));
//        // 四舍五入,保留2位小数
//        return d1.divide(d2, 2, BigDecimal.ROUND_HALF_UP).toString();
//    }

    public static String getNumber(float number) {
        NumberFormat ddf1 = NumberFormat.getNumberInstance();
        ddf1.setMaximumFractionDigits(MAX_DIGIT);
        return ddf1.format(number);
    }


    /**
     * 从一个byte[]数组中截取一部分
     *
     * @param src
     * @param count
     * @return
     */
    public static byte[] subBytes(byte[] src, int count) {
        byte[] bs = new byte[count];
        for (int i = 0; i < count; i++) bs[i] = src[i];
        return bs;
    }

    /**
     * 将byte数组bRefArr转为一个整数,字节数组的低位是整型的低字节位
     */
    public static int bytes2int(byte[] bRefArr) {
        int iOutcome = 0;
        byte bLoop;

        for (int i = 0; i < bRefArr.length; i++) {
            bLoop = bRefArr[i];
            iOutcome += (bLoop & 0xFF) << (8 * i);
        }
        return iOutcome;
    }

    public static int byte2int(byte b) {
        return b & 0xFF;
    }

    /*******String, byte ,hex transform begin*
     * **********************************************
     *readme:
     * String: "XYZ"
     * byte:   0x58,0x59,0x5A
     * hex string:"58595A"
     * ************************************************/

    /**
     * 字节数组转16进制字符串
     */
    public static String bytes2HexString(byte[] b) {
        String r = "";

        for (int i = 0; i < b.length; i++) {
            String hex = Integer.toHexString(b[i] & 0xFF);
            if (hex.length() == 1) {
                hex = '0' + hex;
            }
            r += hex.toUpperCase();
        }

        return r;
    }


    /**
     * 字符转换为字节
     */
    private static byte charToByte(char c) {
        return (byte) "0123456789ABCDEF".indexOf(c);
    }

    /**
     * 16进制字符串转字节数组
     * "58" --> 0x58.
     * hex should be even number.
     */
    public static byte[] hexString2Bytes(String hex) {
        if ((hex == null) || (hex.equals(""))) {
            return null;
        } else if (hex.length() % 2 != 0) {
            return null;
        } else {
            hex = hex.toUpperCase();
            int len = hex.length() / 2;
            byte[] b = new byte[len];
            char[] hc = hex.toCharArray();
            for (int i = 0; i < len; i++) {
                int p = 2 * i;
                b[i] = (byte) (charToByte(hc[p]) << 4 | charToByte(hc[p + 1]));
            }
            return b;
        }
    }

    /**
     * 字节数组转字符串
     */
    public static String bytes2String(byte[] b) {
        String r = "";
        try {
            r = new String(b, "UTF-8");
        } catch (Exception e) {
            Log.e(TAG, "UnsupportedOperationException.e :" + e.getMessage());
        }
        return r;
    }

    /**
     * 字符串转字节数组
     */
    public static byte[] string2Bytes(String s) {
        byte[] r = s.getBytes();
        return r;
    }

    /**
     * 16进制字符串转字符串
     */
    public static String hex2String(String hex) {
        String r = bytes2String(hexString2Bytes(hex));
        return r;
    }

    /**
     * 字符串转16进制字符串
     */
    public static String string2HexString(String s) {
        String r = bytes2HexString(string2Bytes(s));
        return r;
    }

    /*******String, byte ,hex transform end************************************************/


}
