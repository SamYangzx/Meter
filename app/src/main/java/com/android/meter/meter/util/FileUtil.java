package com.android.meter.meter.util;

import android.os.Environment;

import java.io.File;

/**
 * Created by fenghe on 2017/6/5.
 */

public class FileUtil {
    private static final String TAG = FileUtil.class.getSimpleName();
    private static final String PACKAGE_FOLDER = "Meter";
    private static final String PIC_FOLDER = "PicFolder";
    private static final String PIC_SUFFIX = ".jpg";
    private static final String EXCEL_NAME = "measure.xls";
    private static final String FOLDER_PATH = Environment.getExternalStorageDirectory() + File.separator + PACKAGE_FOLDER;


    public static String getDefaultDateFolder() {
        return FOLDER_PATH + File.separator + TimeUtil.getDateYearMonthDay();
    }

    public static String getPicFolder() {
        String folderPath = getDefaultDateFolder() + File.separator + PIC_FOLDER;
        File file = new File(folderPath);
        if (!file.exists()) {
            file.mkdirs();
        }
        return folderPath;
    }

    public static String getTimeFileName() {
        return TimeUtil.getDateYearMonthDayHourMinute() + PIC_SUFFIX;
    }

    public static String getExcelPath() {
        File file = new File(getDefaultDateFolder());
        if (!file.exists()) {
            file.mkdirs();
        }
        return getDefaultDateFolder() + File.separator + EXCEL_NAME;
    }
}
