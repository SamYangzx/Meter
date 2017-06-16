package com.android.meter.meter.util;

/**
 * Created by fenghe on 2017/6/16.
 */

public class CommandUtil {

    public static final String PRE_CODE = "BB";
    public static final String LENGTH_CODE = "BB";


    public static final String TEST_CMD = "BB03E501E7CC";

    public static String getCmd(String pre, String length, String cmd, String data, String check, String end){
        StringBuilder sb = new StringBuilder();
        sb.append(pre).append(length).append(cmd).append(data).append(check).append(end);
        return  sb.toString();
    }

    public static String getCmd(String cmd, String data){
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

}
