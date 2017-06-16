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

    public static String stringToHexString(String strPart) {
        String hexString = "";
        for (int i = 0; i < strPart.length(); i++) {
            int ch = (int) strPart.charAt(i);
            String strHex = Integer.toHexString(ch);
            hexString = hexString + strHex;
        }
        return hexString;
    }

    public static String bytesToHexString(byte[] bytes) {
        String result = "";
        for (int i = 0; i < bytes.length; i++) {
            String hexString = Integer.toHexString(bytes[i] & 0xFF);
            if (hexString.length() == 1) {
                hexString = '0' + hexString;
            }
            result += hexString.toUpperCase();
        }
        return result;
    }

    private static String hexString = "0123456789ABCDEF";

    /**
     * 将字符串编码成16进制数字,适用于所有字符（包括中文）
     */
    public static String hexEncode(String str) {
// 根据默认编码获取字节数组
        byte[] bytes = str.getBytes();
        StringBuilder sb = new StringBuilder(bytes.length * 2);
// 将字节数组中每个字节拆解成2位16进制整数
        for (int i = 0; i < bytes.length; i++) {
            sb.append(hexString.charAt((bytes[i] & 0xf0) >> 4));
            sb.append(hexString.charAt((bytes[i] & 0x0f) >> 0));
        }
        return sb.toString();
    }

    /**
     * 将16进制数字解码成字符串,适用于所有字符（包括中文）
     */
    public static String hexDecode(String bytes) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream(bytes.length() / 2);
        // 将每2位16进制整数组装成一个字节
        if (bytes.length() % 2 == 0) {
            for (int i = 0; i < bytes.length(); i += 2)
                baos.write((hexString.indexOf(bytes.charAt(i)) << 4 | hexString.indexOf(bytes.charAt(i + 1))));
        } else {
            Log.e(TAG, "exception: hexDecode.bytes is not right!!");
        }
        return new String(baos.toByteArray());
    }

    private static byte uniteBytes(byte src0, byte src1) {
        byte _b0 = Byte.decode("0x" + new String(new byte[]{src0})).byteValue();
        _b0 = (byte) (_b0 << 4);
        byte _b1 = Byte.decode("0x" + new String(new byte[]{src1})).byteValue();
        byte ret = (byte) (_b0 | _b1);
        return ret;
    }

    public static byte[] HexString2Bytes(String src) {
        byte[] ret = new byte[6];
        byte[] tmp = src.getBytes();
        for (int i = 0; i < 6; ++i) {
            ret[i] = uniteBytes(tmp[i * 2], tmp[i * 2 + 1]);
        }
        return ret;
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

}
