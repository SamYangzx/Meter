package com.android.meter.meter;

import android.app.Application;

import com.android.meter.meter.bluetooth.BluetoothHelper;
import com.android.meter.meter.bluetooth.BtConstant;
import com.android.meter.meter.http.SocketConstant;
import com.android.meter.meter.http.SocketControl;
import com.android.meter.meter.util.LogUtil;
import com.android.meter.meter.util.SharedPreferenceUtils;


/**
 * Created by fenghe on 2017/6/16.
 */

public class MainApplication extends Application {
    private static final String TAG = MainApplication.class.getSimpleName();

    private static MainApplication sApp;
//    private BluetoothHelper mBluetoothHelper;

    @Override
    public void onCreate() {
        LogUtil.v(TAG, "MainApplication.onCreate");
        super.onCreate();
        sApp = this;
//        initData();
    }

    public static MainApplication getContext() {
        return sApp;
    }

    private void initData() {
//        FileUtil.FILE_INDEX = 1;
        String server = (String) SharedPreferenceUtils.getParam(sApp, SocketConstant.SAVE_IP, SocketConstant.DEFAULT_SERVER);
        int port = (int) SharedPreferenceUtils.getParam(sApp, SocketConstant.SAVE_PORT, SocketConstant.DEFAULT_PORT);
        SocketControl.getInstance().connect(server, port);
        String bt = (String) SharedPreferenceUtils.getParam(sApp, BtConstant.SAVE_BT_ADDRESS,"");
        BluetoothHelper.getBluetoothHelper(sApp).connect(bt);
    }

}
