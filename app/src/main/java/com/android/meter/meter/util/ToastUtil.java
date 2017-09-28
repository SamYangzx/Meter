package com.android.meter.meter.util;

import android.content.Context;
import android.widget.Toast;

/**
 * Created by fenghe on 2017/5/18.
 */

public class ToastUtil {

    public static final int DEBUG = 1;
    public static final int INFO = 2;

    private static final int SHOW_TOAST_LEVEL = INFO;

    public static void showToast(Context context, int strId) {
        Toast.makeText(context, strId, Toast.LENGTH_SHORT).show();
    }

    public static void showToast(Context context, String strId) {
        Toast.makeText(context, strId, Toast.LENGTH_SHORT).show();
    }

    public static void showToast(Context context, String strId, int level) {
        if (level > SHOW_TOAST_LEVEL) {
            Toast.makeText(context, strId, Toast.LENGTH_SHORT).show();
        }
    }

    public static void showToast(Context context, int strId, int level) {
        if (level >= SHOW_TOAST_LEVEL) {
            Toast.makeText(context, strId, Toast.LENGTH_SHORT).show();
        }
    }
}
