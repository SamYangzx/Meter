package com.android.meter.util;

import android.os.Environment;

import java.io.File;

import static java.io.File.separatorChar;

/**
 * Created by fenghe on 2017/6/5.
 */

public class FileUtil {
    private static final String TAG = FileUtil.class.getSimpleName();
    private static final String PACKAGE_FOLDER = LogUtil.LOG_FOLDER;
    private static final String PIC_FOLDER = "PicFolder";
    private static final String PIC_SUFFIX = ".jpg";
    private static final String EXCEL_NAME = "measure.xls";

    public static final String FILE_START = "start";
    public static final String FILE_END = "end";
    public static final String TOTAL_FILE_END = "sendend";
    //修改下面的值时要修改ImageDataSource中的FOLDER_PATH 值。
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
            if (count == 0) {
                count = 1;
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
        if (count == 0) {
            count = 1;
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
     * 获取文件路径的父文件夹路径
     *
     * @param path
     * @return 文件路径的父文件夹路径
     */
    public static String getParentFolderPath(String path) {
        if ((path != null) && (path.length() > 0)) {
            int index = path.lastIndexOf(separatorChar);
            if ((index > 0) && (path.length() > index)) {
                return path.substring(0, index);
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
        if(hexOrFile==null || hexOrFile == ""){
            return false;
        }
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

    /*****文件的删除begin*********************************/
    /**
     * 递归删除目录下的所有文件及子目录下所有文件
     *
     * @param dir 将要删除的文件目录
     * @return boolean Returns "true" if all deletions were successful.
     * If a deletion fails, the method stops attempting to
     * delete and returns "false".
     */
    public static boolean deleteDir(File dir) {
        if (dir.isDirectory()) {
            String[] children = dir.list();
            //递归删除目录中的子目录下
            for (int i = 0; i < children.length; i++) {
                boolean success = deleteDir(new File(dir, children[i]));
                if (!success) {
                    return false;
                }
            }
        }
        // 目录此时为空，可以删除
        return dir.delete();
    }

    public static boolean deleteDir(String dir) {
        File folder = new File(dir);
        return deleteDir(folder);
    }

    /**文件的删除end************************************/

}
