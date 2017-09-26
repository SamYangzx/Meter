package com.android.meter.meter.util;

import android.text.TextUtils;
import android.util.Log;

import java.math.BigDecimal;
import java.text.NumberFormat;

/**
 * Created by fenghe on 2017/6/7.
 */

public class StringUtil {
    private static final String TAG = StringUtil.class.getSimpleName();

    private static final int MAX_DIGIT = 4;

    public static float big2(float f) {
        BigDecimal b = new BigDecimal(f);
        float f1 = b.setScale(2, BigDecimal.ROUND_HALF_UP).floatValue();
        return f1;
    }

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
     * 字节数组转16进制字符串. 0x58 --> "58"
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
        } else {
            if (hex.length() % 2 != 0) {
                Log.d(TAG, "hex.length is error number: " + hex.length());
                hex = "0" + hex;
            }

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
     * 字节数组转字符串,ex: 0x58 -->"X"
     */
    public static String bytes2String(byte[] b) {
        String r = "";
        try {
            r = new String(b, "UTF-8");
        } catch (Exception e) {
            LogUtil.e(TAG, "UnsupportedOperationException.e :" + e.getMessage());
        }
        return r;
    }

    /**
     * 字符串转字节数组. "X" --> 0x58
     */
    public static byte[] string2Bytes(String s) {
        byte[] r = s.getBytes();
        return r;
    }

    /**
     * 16进制字符串转字符串
     */
    public static String hex2String(String hex) {
        if (TextUtils.isEmpty(hex)) {
            return "";
        }
        String r = bytes2String(hexString2Bytes(hex));
        LogUtil.d(TAG, "hex2String generate string: " + r);
        return r;
    }

    /**
     * 字符串转16进制字符串
     */
    public static String string2HexString(String s) {
        String r = bytes2HexString(string2Bytes(s));
        return r;
    }


    public static String getCompletedHex(String hex) {
        if (hex == null || hex == "") {
            return hex;
        }
        if (hex.length() % 2 != 0) {
            hex = "0" + hex;
        }
        return hex;
    }

    public static byte[] int2byte(int s) {
        byte[] b = new byte[1];
        b[0] = (byte) (s & 0xFF);
        return b;
    }

    /** */
    /**
     * @函数功能: BCD码转为10进制串(阿拉伯数据)
     * @输入参数: BCD码
     * @输出结果: 10进制串
     */
    public static String bcd2Str(byte[] bytes) {
        StringBuffer temp = new StringBuffer(bytes.length * 2);

        for (int i = 0; i < bytes.length; i++) {
            temp.append((byte) ((bytes[i] & 0xf0) >>> 4));
            temp.append((byte) (bytes[i] & 0x0f));
        }
        return temp.toString().substring(0, 1).equalsIgnoreCase("0") ? temp.toString().substring(1) : temp.toString();
    }

    /**
     * @函数功能: 10进制串转为BCD码
     * @输入参数: 10进制串
     * @输出结果: BCD码
     */
    public static byte[] str2Bcd(String asc) {
        int len = asc.length();
        int mod = len % 2;

        if (mod != 0) {
            asc = "0" + asc;
            len = asc.length();
        }

        byte abt[] = new byte[len];
        if (len >= 2) {
            len = len / 2;
        }

        byte bbt[] = new byte[len];
        abt = asc.getBytes();
        int j, k;

        for (int p = 0; p < asc.length() / 2; p++) {
            if ((abt[2 * p] >= '0') && (abt[2 * p] <= '9')) {
                j = abt[2 * p] - '0';
            } else if ((abt[2 * p] >= 'a') && (abt[2 * p] <= 'z')) {
                j = abt[2 * p] - 'a' + 0x0a;
            } else {
                j = abt[2 * p] - 'A' + 0x0a;
            }

            if ((abt[2 * p + 1] >= '0') && (abt[2 * p + 1] <= '9')) {
                k = abt[2 * p + 1] - '0';
            } else if ((abt[2 * p + 1] >= 'a') && (abt[2 * p + 1] <= 'z')) {
                k = abt[2 * p + 1] - 'a' + 0x0a;
            } else {
                k = abt[2 * p + 1] - 'A' + 0x0a;
            }

            int a = (j << 4) + k;
            byte b = (byte) a;
            bbt[p] = b;
        }
        return bbt;
    }


    /*******String, byte ,hex transform end************************************************/

    /********array merger***********************/
    public static byte[] byteMerger(byte[] byte_1, byte[] byte_2) {
        byte[] byte_3 = new byte[byte_1.length + byte_2.length];
        System.arraycopy(byte_1, 0, byte_3, 0, byte_1.length);
        System.arraycopy(byte_2, 0, byte_3, byte_1.length, byte_2.length);
        return byte_3;
    }

    /*********************************************************/

    /**
     * @param hexString
     * @return first character's index which is not number.
     * eg: input "123.3N"'s hex "3132332e334e" ,return 5*2;
     */
    public static int getValueUnitIndex(String hexString) {
        byte[] b = hexString2Bytes(hexString);
        int index = 0;
        for (index = 0; index < b.length; index++) {
            if (b[index] > '9') {
                break;
            }
        }
        return index * 2;
    }


}
