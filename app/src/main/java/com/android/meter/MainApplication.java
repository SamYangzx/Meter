package com.android.meter;

import android.app.Application;

import com.android.meter.util.Constant;
import com.android.meter.util.LogUtil;
import com.android.meter.util.SharedPreferenceUtils;


/**
 * Created by fenghe on 2017/6/16.
 */

public class MainApplication extends Application {
    private static final String TAG = MainApplication.class.getSimpleName();

    private static MainApplication sApp;
    private static boolean mIsModeA;

    @Override
    public void onCreate() {
        LogUtil.v(TAG, "MainApplication.onCreate");
        super.onCreate();
        sApp = this;
        initData();
    }

    public static MainApplication getContext() {
        return sApp;
    }

    private void initData() {
        SharedPreferenceUtils.setParam(this, Constant.SAME_PHOTO_FOLDER, false);

    }

    public boolean isModeA() {
        return mIsModeA;
    }

    public void setModeA(boolean modeA) {
        mIsModeA = modeA;
    }

}
