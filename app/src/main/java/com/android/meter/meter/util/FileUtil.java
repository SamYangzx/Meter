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

    public static final String FILE_START = "start";
    public static final String FILE_END = "end";
    public static final String FOLDER_PATH = Environment.getExternalStorageDirectory() + File.separator + PACKAGE_FOLDER;

    public static int FILE_INDEX = 1;

    public static String getDefaultDateFolder() {
        return FOLDER_PATH + File.separator + TimeUtil.getDateYearMonthDay();
    }

    /**
     * 每天拍照的照片按照拍照存储的次数存放。
     *
     * @param existFolder true if you want to use exist folder.
     * @return
     */
    public static String getPicNumberFolder(boolean existFolder) {
        String dateFolder = getPicDateFolder();
        String numberFolder;
        if (existFolder) {
            numberFolder = dateFolder + File.separator + getFileCount(dateFolder);
        } else {
            numberFolder = dateFolder + File.separator + (getFileCount(dateFolder) + 1);
        }
        File file = new File(numberFolder);
        if (!file.exists()) {
            file.mkdirs();
        }
        return numberFolder;
    }

    public static String getPicDateFolder() {
        String folderPath = getDefaultDateFolder() + File.separator + PIC_FOLDER;
//        File file = new File(folderPath); //因为调用此函数的地方会新建文件夹，故此处不再判断。
//        if (!file.exists()) {
//            file.mkdirs();
//        }
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

    /**
     * 获取文件扩展名
     *
     * @param filename
     * @return
     */
    public static String getExtensionName(String filename) {
        if ((filename != null) && (filename.length() > 0)) {
            int dot = filename.lastIndexOf('.');
            if ((dot > -1) && (dot < (filename.length() - 1))) {
                return filename.substring(dot + 1);
            }
        }
        return "";
    }

    /**
     * @param foler file in
     * @param name  file name
     * @return
     */
    public static String getFilePath(String foler, String name) {
        return foler + File.separator + name;
    }

    /**
     * 根据字符串是否包含文件路径来判断是否是传输文件。
     *
     * @param hexOrFile
     * @return
     */
    public static boolean isFile(String hexOrFile) {
        if (hexOrFile.startsWith(FileUtil.FOLDER_PATH)) {
            return true;
        }
        return false;
    }

    public static int getFileCount(String folder) {
        File file = new File(folder);
        if (file.exists() && file.isDirectory()) {
            return file.list().length;
        }
        return 0;
    }
}
