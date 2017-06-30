package com.android.meter.meter.util;

/**
 * Created by fenghe on 2017/6/16.
 */

public class CommandUtil {

    public static final String PRE_CODE = "BB";
//    public static final String LENGTH_CODE = "BB";


    public static final String TEST_HEX_CMD = "BB03E501E7CC";

    public static String getCmd(String pre, String length, String cmd, String data, String check, String end) {
        StringBuilder sb = new StringBuilder();
        sb.append(pre).append(length).append(cmd).append(data).append(check).append(end);
        return sb.toString();
    }

    public static String getCmd(String cmd, String data) {
//        String check =
//        int length = cmd.length() + data.length() +
//        return getCmd(PRE_CODE, )

        return "";
    }

    public static byte getChecksum(byte[] data) {
        if (data == null) {
            return 0;
        }
        byte result = 0x00;
        for (int i = 0; i < data.length; i++) {
            result ^= data[i];
        }
        return result;
    }

    public static byte[] getWholeCmd(byte[] pre, byte[] cmdLength, byte[] cmdBody, byte[] endByte) {
        byte[] b = new byte[pre.length + cmdLength.length + cmdBody.length +1];
        for (int i = 0; i < b.length; i++) {
            if (i < pre.length) {
                b[i] = pre[i];
            } else if (i < pre.length + cmdLength.length) {
                b[i] = cmdLength[i - pre.length];
            } else if(i < pre.length + cmdLength.length + cmdBody.length){
                b[i] = cmdBody[i - pre.length - cmdLength.length];
            }else{
                b[i] = endByte[i - pre.length - cmdLength.length -cmdBody.length];
            }
        }
        return b;
    }

    public static byte[] getCheckBytes(int cmdLength,  byte[] cmdBody){
        if(cmdLength>1){
//            byte[]  checkByte = new byte[];
//            cmdBody.
        }
        return null;
    }


}
