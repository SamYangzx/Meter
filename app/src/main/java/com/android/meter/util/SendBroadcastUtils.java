package com.android.meter.util;

import android.content.Intent;
import android.util.Log;

import com.android.meter.MainApplication;


public class SendBroadcastUtils {
    private static final String TAG = "SendBroadcastUtils";

    public static void sendBroadcast(String action) {
        Intent intent = new Intent(action);
        Log.d(TAG, "send broadcast: " + action);
        MainApplication.getContext().sendBroadcast(intent);
    }
}
