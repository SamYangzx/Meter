package com.android.meter.meter.util;

import android.content.Context;
import android.widget.Toast;

/**
 * Created by fenghe on 2017/5/18.
 */

public class ToastUtil {

    public static void showToast(Context context, int strId) {
        Toast.makeText(context, strId, Toast.LENGTH_SHORT).show();
    }

    public static void showToast(Context context, String strId) {
        Toast.makeText(context, strId, Toast.LENGTH_SHORT).show();
    }
}
