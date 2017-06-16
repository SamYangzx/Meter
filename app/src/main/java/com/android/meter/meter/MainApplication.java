package com.android.meter.meter;

import android.app.Application;

import com.android.meter.meter.bluetooth.BluetoothHelper;

/**
 * Created by fenghe on 2017/6/16.
 */

public class MainApplication extends Application {

    private static MainApplication sApp;
//    private BluetoothHelper mBluetoothHelper;

    @Override
    public void onCreate() {
        super.onCreate();
        sApp = this;
        //
//        mBluetoothHelper =  BluetoothHelper.getBluetoothChatService(sApp);
    }

    public static MainApplication getApp() {
        return sApp;
    }

}
