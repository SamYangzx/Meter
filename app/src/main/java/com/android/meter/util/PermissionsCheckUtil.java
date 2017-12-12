package com.android.meter.util;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;

/**
 * Created by fenghe on 2017/9/29.
 */

public class PermissionsCheckUtil {
    private static final String TAG = PermissionsCheckUtil.class.getSimpleName();
    private static final int REQUEST_PERMISSION_CODE = 101;
    private static String[] PERMISSIONS_ARRAY = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.BLUETOOTH_ADMIN,
            Manifest.permission.INTERNET,
    };

    public static void verifyStoragePermissions(Context context) {
        // Check if we have write permission

        for (String permission : PERMISSIONS_ARRAY) {
            if (lacksPermission(context, permission)) {
                ActivityCompat.requestPermissions((Activity) context, PERMISSIONS_ARRAY,
                        REQUEST_PERMISSION_CODE);
            }
        }
    }

    public boolean lacksPermissions(String... permissions) {

        return false;
    }

    // 判断是否缺少权限
    private static boolean lacksPermission(Context context, String permission) {
        return ContextCompat.checkSelfPermission(context, permission) !=
                PackageManager.PERMISSION_GRANTED;
    }

}
