package com.android.meter.meter.util;

import android.os.Environment;

import java.io.File;

/**
 * Created by fenghe on 2017/6/5.
 */

public class FileUtil {
    private static final String TAG = FileUtil.class.getSimpleName();
//    private static final String PACKAGE_FOLDER = "Meter1";
    private static final String PACKAGE_FOLDER = LogUtil.LOG_FOLDER;
    private static final String PIC_FOLDER = "PicFolder";
    private static final String PIC_SUFFIX = ".jpg";
    private static final String EXCEL_NAME = "measure.xls";

    public static final String FILE_START = "start";
    public static final String FILE_END = "end";
    public static final String TOTAL_FILE_END = "sendend";
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
        String date = TimeUtil.getDateYearMonthDay();
//        String numberFolder;
        StringBuilder sb = new StringBuilder();
        if (existFolder) {
            int count = getFileCount(dateFolder);
            if(count == 0){
                count =1;
            }
            sb.append(dateFolder).append(File.separator).append(date).append("_").append(count).append(File.separator).append(PIC_FOLDER);
//            numberFolder = dateFolder + File.separator + date + "_" + getFileCount(dateFolder) + File.separator + PIC_FOLDER;
        } else {
            sb.append(dateFolder).append(File.separator).append(date).append("_").append((getFileCount(dateFolder) + 1)).append(File.separator).append(PIC_FOLDER);
//            numberFolder = dateFolder + File.separator + date + "_" + (getFileCount(dateFolder) + 1) + File.separator + PIC_FOLDER;
        }
        File file = new File(sb.toString());
        if (!file.exists()) {
            file.mkdirs();
        }
        return sb.toString();
    }

    public static String getLogFolder() {
        String dateFolder = getPicDateFolder();
        String date = TimeUtil.getDateYearMonthDay();
        StringBuilder sb = new StringBuilder();
        int count = getFileCount(dateFolder);
        if(count == 0){
            count =1;
        }
        sb.append(dateFolder).append(File.separator).append(date).append("_").append(count);
        return sb.toString();
    }

    public static String getPicDateFolder() {
        String folderPath = getDefaultDateFolder();
//        String folderPath = getDefaultDateFolder() + File.separator + PIC_FOLDER;
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
