package com.android.meter.meter.util;

import android.util.Log;

/**
 * Created by fenghe on 2017/6/8.
 */

public class LogUtil {

    public static final String COMMON_TAG = "meter/";

    public static void d(String tag, String msg) {
        Log.w(tag, msg);
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
}
