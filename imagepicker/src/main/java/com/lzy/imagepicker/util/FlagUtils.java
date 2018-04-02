package com.lzy.imagepicker.util;

public class FlagUtils {
    private static boolean mIsModeA = true;
    private static final String FOLDER_PATH_A = "MeterA";
    private static final String FOLDER_PATH_B = "MeterB";

    public static void setModeA(boolean modeA) {
        mIsModeA = modeA;
    }

    public static boolean iSModeA() {
        return mIsModeA;
    }

    public static String getFolderPath() {
        if (mIsModeA) {
            return FOLDER_PATH_A;
        } else {
            return FOLDER_PATH_B;
        }
    }

}
