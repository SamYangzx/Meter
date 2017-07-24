package com.android.meter.meter.util;

import android.util.Log;

/**
 * Created by fenghe on 2017/6/16.
 * <p>
 * 网络发送数据时，末尾要接回车和换行符号。用十六进制表示为：0x0A,0x0A.
 */

public class CommandUtil {
    private static final String TAG = LogUtil.COMMON_TAG + CommandUtil.class.getSimpleName();

    public static final String PRE_CODE = "BB";
    public static final String RECEIVE_PRE_CODE = "AA";

    public static final String END_CODE = "CC";
    /********* cmdCode begin*********************/
    public static final String RESET_CMD_CODE = "E0";
    public static final String CALIBRATE_CMD_CODE = "E1";
    public static final String SAVE_CMD_CODE = "E2";
    public static final String CHOOSE_CMD_CODE = "E3";
    public static final String UPLOCD_CMD_CODE = "E4";
    public static final String START_STOP_CMD_CODE = "E5";
    /********************** cmdCode end*******************/

    /***constant cmd begin*/
    public static final String START_CLOLLECT_HEXCMD = "BB03E500CC";
    public static final String END_CLOLLECT_HEXCMD = "BB03E501CC";
    /***constant cmd end*/

    public static final String TEST_HEX_CMD = "BB03E501E7CC";

    public static String getCmdHex(String pre, String cmdCode, String originData) {
        StringBuilder sb = new StringBuilder();
        if (originData == null) {
            originData = "";
        }
        String hexData = StringUtil.string2HexString(originData);
        int length = (cmdCode.length() + hexData.length()) / 2;
        String lengthS;
        String check;
        if (length < 0x10) {
            lengthS = "0" + Integer.toHexString(length);
        } else {
            lengthS = Integer.toHexString(length);
        }
        check = getChecksum(lengthS + cmdCode + hexData);
        Log.d(TAG, "originData: " + originData + " ,lengths: " + lengthS + " , cmdCode: " + cmdCode + " ,hexData: " + hexData + " ,check: " + check);
        sb.append(pre).append(lengthS).append(cmdCode).append(hexData).append(check).append(END_CODE);
        Log.d(TAG, "getCmdHex: " + sb.toString());
        return sb.toString();
    }

    public static String getChecksum(String string) {
        byte[] checksum = new byte[1];
        checksum[0] = getChecksum(string.getBytes());
        Log.d(TAG, "checksum[0]: " + checksum[0]);
        return StringUtil.bytes2HexString(checksum);
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
        byte[] b = new byte[pre.length + cmdLength.length + cmdBody.length + 1];
        for (int i = 0; i < b.length; i++) {
            if (i < pre.length) {
                b[i] = pre[i];
            } else if (i < pre.length + cmdLength.length) {
                b[i] = cmdLength[i - pre.length];
            } else if (i < pre.length + cmdLength.length + cmdBody.length) {
                b[i] = cmdBody[i - pre.length - cmdLength.length];
            } else {
                b[i] = endByte[i - pre.length - cmdLength.length - cmdBody.length];
            }
        }
        return b;
    }

    public static byte[] getCheckBytes(int cmdLength, byte[] cmdBody) {
        if (cmdLength > 1) {
//            byte[]  checkByte = new byte[];
//            cmdBody.
        }
        return null;
    }

    public static String getResetCmd() {
        return RESET_CMD_CODE;
    }

    public static String getCalibrateCmd(String data) {
        return getCmdHex(PRE_CODE, CALIBRATE_CMD_CODE, data);
    }

    public static String getSaveCmd() {
        return getCmdHex(PRE_CODE, SAVE_CMD_CODE, null);
    }

    public static String getChooseCmd(String data) {
        return getCmdHex(PRE_CODE, CHOOSE_CMD_CODE, data);
    }

    public static String getUploadCmd(String data) {
        return getCmdHex(PRE_CODE, UPLOCD_CMD_CODE, data);
    }


    public static String getStartCmd() {
        return getCmdHex(PRE_CODE, START_STOP_CMD_CODE, "00");
    }

    public static String getStopCmd() {
        return getCmdHex(PRE_CODE, START_STOP_CMD_CODE, "01");
    }

}
