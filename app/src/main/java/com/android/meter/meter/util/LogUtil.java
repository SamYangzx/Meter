package com.android.meter.meter.util;

import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by fenghe on 2017/6/8.
 */

public class LogUtil {

    public static final String TAG = LogUtil.class.getSimpleName();
    public static final String COMMON_TAG = "sam/";
    private static final String LOG_FOLDER = "com.android.meter";
    private static final String LOG_PATH = android.os.Environment.getExternalStorageDirectory()
            .getAbsolutePath() + File.separator + LOG_FOLDER;
    private static final String LOG_FILE = android.os.Environment.getExternalStorageDirectory()
            .getAbsolutePath() + File.separator + LOG_FOLDER + File.separator + "log.txt";
    private static boolean LOG_TO_FILE = true;


    public static void d(String tag, String msg) {
        Log.w(tag, msg);
        if (LOG_TO_FILE) {
            logtoFile("D", tag, msg, null);
        }
    }

    public static void w(String tag, String msg) {
        Log.w(tag, msg);
        if (LOG_TO_FILE) {
            logtoFile("W", tag, msg, null);
        }
    }

    public static void e(String tag, String msg) {
        Log.e(tag, msg);
        if (LOG_TO_FILE) {
            logtoFile("E", tag, msg, null);
        }
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

    public static void receiveCmdResult(String tag, String hexCmd) {
        d(tag, "receive origin String: " + StringUtil.hex2String(hexCmd) + " , hex String: " + hexCmd);
    }

    public static void receiveCmdResult(String tag, byte[] buffer) {
        d(tag, "receive origin String: " + StringUtil.bytes2String(buffer) + " , hex String: " + StringUtil.bytes2HexString(buffer));
    }

    /**
     * open log file and write to file
     *
     * @return
     **/
    private static void logtoFile(String mylogtype, String tag, String text, Throwable tr) {
        Date nowtime = new Date();
        SimpleDateFormat myLogSdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        StringBuilder logText = new StringBuilder();
        logText.append(myLogSdf.format(nowtime)).append("\t");
        logText.append(mylogtype).append("\t");
        logText.append(tag).append("\t");
        logText.append(text).append("\n");
        logText.append(Log.getStackTraceString(tr));

        File logFolder = new File(LOG_PATH);
        if (!logFolder.exists()) {
            Log.d(TAG, "Create log folder " + logFolder.mkdirs());
        }
        File logFile = new File(LOG_FILE);
        if (!logFile.exists()) {
            try {
                Log.d(TAG, "Create log file " + logFile.createNewFile());
            } catch (IOException e) {
                Log.d(TAG, "", e);
            }
        }

        try {
            OutputStreamWriter writer = null;
            writer = new OutputStreamWriter(new FileOutputStream(logFile, true), "UTF-8");
            writer.write(logText.toString());
            writer.close();
        } catch (IOException e1) {
            Log.e(TAG, "", e1);
        }
    }
}
