package com.android.meter.meter.util;

import android.util.Log;

/**
 * Created by fenghe on 2017/6/8.
 */

public class LogUtil {

    public static final String COMMON_TAG = "sam/";

    public static void d(String tag, String msg) {
        Log.w(tag, msg);
    }

    public static void e(String tag, String msg) {
        Log.e(tag, msg);
    }

    public static void callStack(String tag, String msg) {
        StringBuilder sb = new StringBuilder();
        sb.append(msg).append("\n").append(track());
        Log.d(tag, sb.toString());
    }


    private static String track() {
        StackTraceElement[] straceTraceElements = Thread.currentThread().getStackTrace();
        int length = straceTraceElements.length;
        String file = "";
        int line = 0;
        String method = "";
        StringBuilder sb = new StringBuilder();
        for (int i = 4; i < length; i++) {
            file = straceTraceElements[i].getFileName();
            line = straceTraceElements[i].getLineNumber();
            method = straceTraceElements[i].getMethodName();
            sb.append("\t").append(file).append(":").append(method).append("(").append(line)
                    .append(")\n");
        }
        return sb.toString();
    }


    public static void sendCmdResult(String tag, String hexCmd, boolean result) {
        d(tag, "send origin String: " + StringUtil.hex2String(hexCmd) + " , hex String: " + hexCmd + "  result: " + (result ? "success" : "failed"));
    }

    public static void sendCmdResult(String tag, byte[] buffer, boolean result) {
        d(tag, "send origin String: " + StringUtil.bytes2String(buffer) + " , hex String: " + StringUtil.bytes2HexString(buffer) + "  result: " + (result ? "success" : "failed"));
    }
}
