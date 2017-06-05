package com.android.meter.meter.util;

import android.os.Environment;

import java.io.File;

/**
 * Created by fenghe on 2017/6/5.
 */

public class FileUtil {
    private static final String TAG = FileUtil.class.getSimpleName();
    private static final String PIC_FOLDER = "PicFolder";
    private static final String PIC_SUFFIX = ".jpg";

    public static String getPicFolder(String date) {
        String folderPath = Environment.getExternalStorageDirectory() + File.separator + PIC_FOLDER + File.separator + date;
        File file = new File(folderPath);
        if (!file.exists()) {
            file.mkdirs();
        }
        return folderPath;
    }

    public static String getTimeFileName(){
        return TimeUtil.getDateYearMonthDayHourMinute() + PIC_SUFFIX;
    }
}
