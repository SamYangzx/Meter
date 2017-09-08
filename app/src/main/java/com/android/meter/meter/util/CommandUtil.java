package com.android.meter.meter.util;

import android.util.Log;

/**
 * Created by fenghe on 2017/6/16.
 * <p>
 * 网络发送数据时，末尾要接回车和换行符号。用十六进制表示为：0x0D,0x0A.
 */

public class CommandUtil {
    private static final String TAG = LogUtil.COMMON_TAG + CommandUtil.class.getSimpleName();

    public static final String COLLECTOR_PRE_CODE = "BB";
    public static final String PLATFORM_PRE_CODE = "AA";

    public static final String END_CODE = "CC";
    /********* cmdCode begin*********************/
    public static final String RESET_CMD_CODE = "E0";
    public static final String CALIBRATE_CMD_CODE = "E1";
    public static final String SAVE_CMD_CODE = "E2";
    public static final String CHOOSE_CMD_CODE = "E3";
    public static final String UPLOCD_CMD_CODE = "E4";
    public static final String START_STOP_CMD_CODE = "E5";
    /********next code is for socket ***********************/
    public static final String Socket_DATA_CMD_CODE = "EF";
    /********************** cmdCode end*******************/
    public static final String RESPONSE_CMD_CODE = "FF";


    public static final String UNIT_CONSTANT_M = "M03";
    public static final String UNIT_CONSTANT_N = "N04";
    public static final String VALUE_CONSTANT_X = "X03";
    public static final String VALUE_CONSTANT_Y = "Y04";
    public static final String BT_HEX_SEPERATOR = "0A";
    public static final String SEPERATOR = "_";

    /***constant cmd begin*/
    public static final String START_CLOLLECT_HEXCMD = "BB03E500CC";
    public static final String END_CLOLLECT_HEXCMD = "BB03E501CC";
    /***constant cmd end*/

    public static final String TEST_HEX_CMD = "AA05E4414243BBCC";

    private static String getCmdHex(String pre, String cmdCode, String originData) {
        return getCmdHex(pre, cmdCode, originData, false);
    }

    /**
     * @param pre
     * @param cmdCode
     * @param originData
     * @param hex        if originData is hex ,this value should be true; else this value is false.
     * @return
     */
    private static String getCmdHex(String pre, String cmdCode, String originData, boolean hex) {
        StringBuilder sb = new StringBuilder();
        if (originData == null) {
            originData = "";
        }
        String hexData;
        if (hex) {
            hexData = originData;
        } else {
            hexData = StringUtil.string2HexString(originData);
        }
        int length = (cmdCode.length() + hexData.length() + 2) / 2; //校验
        String lengthS;
        String check;
        if (length < 0x10) {
            lengthS = "0" + Integer.toHexString(length).toUpperCase();
        } else {
            lengthS = Integer.toHexString(length);
        }
        check = getChecksum(lengthS + cmdCode + hexData);
        Log.w(TAG, "originData: " + originData + " ,lengths: " + lengthS + " , cmdCode: " + cmdCode + " ,hexData: " + hexData + " ,check: " + check);
        sb.append(pre).append(lengthS).append(cmdCode).append(hexData).append(check).append(END_CODE);
        Log.w(TAG, "getCmdHex: " + sb.toString());
        return sb.toString();
    }


    public static String getChecksum(String string) {
        byte[] checksum = new byte[1];
        checksum[0] = getChecksum(StringUtil.hexString2Bytes(string));
        return StringUtil.bytes2HexString(checksum);
    }

    public static byte getChecksum(byte[] data) {
        if (data == null) {
            return 0;
        }
//        LogUtil.d(TAG, "getChecksum.data: " + StringUtil.bytes2HexString(data));
        byte result = 0x00;
        for (int i = 0; i < data.length; i++) {
            result ^= data[i];
//            LogUtil.d(TAG, "getChecksum.i: " + i + " + result: " + result);
        }
        LogUtil.d(TAG, "result: " + result);
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

    public static String getResetCmd() {
        return getCmdHex(COLLECTOR_PRE_CODE, RESET_CMD_CODE, null);
    }

    /**
     * @param data hex String.
     * @return
     */
    public static String getCalibrateCmd(String data) {
        return getCmdHex(COLLECTOR_PRE_CODE, CALIBRATE_CMD_CODE, StringUtil.getCompletedHex(data), true);
    }

    public static String getSaveCmd() {
        return getCmdHex(COLLECTOR_PRE_CODE, SAVE_CMD_CODE, null);
    }

    public static String getChooseCmd(String data) {
        return getCmdHex(COLLECTOR_PRE_CODE, CHOOSE_CMD_CODE, data);
    }

    public static String getUploadCmd(String data) {
        return getCmdHex(PLATFORM_PRE_CODE, UPLOCD_CMD_CODE, data);
    }

    public static String getUploadCmd(String platformOrColl, String data) {
        return getCmdHex(platformOrColl, UPLOCD_CMD_CODE, data);
    }


    public static String getStartCmd() {
        return getCmdHex(COLLECTOR_PRE_CODE, START_STOP_CMD_CODE, "01", true);
    }

    public static String getStopCmd() {
        return getCmdHex(COLLECTOR_PRE_CODE, START_STOP_CMD_CODE, "00", true);
    }

    public static String getBTUnitHexData(String measureUnit, String sampleUnit) {
        return StringUtil.string2HexString(measureUnit) + BT_HEX_SEPERATOR + StringUtil.string2HexString(sampleUnit);
    }

    /**
     * This is only for socket communication.
     *
     * @param tap         tap in measureset UI.eg: a,b,c...
     * @param measureUnit
     * @param sampleUnit
     * @return origin String.
     */
    public static String getUnitData(String tap, String measureUnit, String sampleUnit) {
        if (tap == null || measureUnit == null || sampleUnit == null) {
            LogUtil.d(TAG, "tap: " + tap + " ,measure: " + measureUnit + ", sampleUnit: " + sampleUnit);
            return "";
        }
        StringBuilder sb = new StringBuilder();
        sb.append(tap).append(UNIT_CONSTANT_M).append(tap).append(UNIT_CONSTANT_N).append(SEPERATOR).
                append(measureUnit).append(SEPERATOR).append(sampleUnit);
        LogUtil.d(TAG, "getUnitData: " + sb.toString());
        return sb.toString();
    }

    /**
     * @param tap tap in measureset UI.eg: a,b,c...
     * @return
     */
    public static String getValueData(String tap, int sum, int count, String measure, String sample) {
        if (tap == null || measure == null || sample == null) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        sb.append(tap).append(VALUE_CONSTANT_X).append(tap).append(VALUE_CONSTANT_Y).append(SEPERATOR).append(StringUtil.getCompletedHex(Integer.toString(sum))).append(StringUtil.getCompletedHex(Integer.toString(count))).append(SEPERATOR).append(measure).append(SEPERATOR).append(sample);
        LogUtil.d(TAG, "getValueData: " + sb.toString());
        return sb.toString();
    }

    public static String getSocketDataCmd(String data) {
        return getCmdHex(COLLECTOR_PRE_CODE, Socket_DATA_CMD_CODE, data, false);
    }

    public static String getMeasurePointHexValue(int index) {
        return StringUtil.bytes2HexString(StringUtil.str2Bcd(Integer.toString(index)));
    }

}
