package com.android.meter.util;

import android.app.Service;
import android.content.Context;
import android.os.Vibrator;

/**
 * Created by fenghe on 2017/6/16.
 */

public class VibratorHelper {
    private static final String TAG = LogUtil.COMMON_TAG + VibratorHelper.class.getSimpleName();
    private static final int DEFAULT_VIBRATE_TIME = 100;

    public static void vibrate(Context context) {
        vibrate(context, DEFAULT_VIBRATE_TIME);
    }

    public static void vibrate(Context context, long milliseconds) {
        Vibrator vibrator = (Vibrator) context
                .getSystemService(Service.VIBRATOR_SERVICE);
        vibrator.vibrate(milliseconds);
    }

    public static void vibrate(Context context, long[] pattern,
                               boolean isRepeat) {
        Vibrator vibrator = (Vibrator) context
                .getSystemService(Service.VIBRATOR_SERVICE);
        vibrator.vibrate(pattern, isRepeat ? 1 : -1);
    }
}
