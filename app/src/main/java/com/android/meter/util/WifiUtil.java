package com.android.meter.util;

import android.content.Context;
import android.net.DhcpInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;

/**
 * Created by fenghe on 2017/9/29.
 */

public class WifiUtil {
    private static final String TAG = WifiUtil.class.getSimpleName();

    public static String getWifiAddress(Context context) {
        WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        if (!wifiManager.isWifiEnabled()) {
            wifiManager.setWifiEnabled(true);
        }

        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        String IPAddress = intToIp(wifiInfo.getIpAddress());

        DhcpInfo dhcpinfo = wifiManager.getDhcpInfo();
        String serverAddress = intToIp(dhcpinfo.serverAddress);
        //其中IPAddress 是本机的IP地址， serverAddress 是你所连接的wifi热点对应的IP地址
        LogUtil.d(TAG, "IPAddress: " + IPAddress + ", serverAddress: " + serverAddress);
        return serverAddress;

    }

    private static String intToIp(int paramInt) {
        return (paramInt & 0xFF) + "." + (0xFF & paramInt >> 8) + "." + (0xFF & paramInt >> 16) + "."
                + (0xFF & paramInt >> 24);
    }

}
