package com.android.meter.meter.http;

/**
 * Created by fenghe on 2017/6/28.
 */

public class HTTPConstant {
    public static final String DEFAULT_SERVER = "192.168.103.223";
    public static final int DEFAULT_PORT = 7001;

    public static final int SEND_SUCCESS = 0x101;
    public static final int SEND_FAIL = 0x102;
    public static final int CONNECT_SUCCESS = 0x103;
    public static final int CONNECT_FAIL = 0x104;
    public static final int RECEIVE_MSG = 0x105;

    public static final boolean WRITE_HEX = true;
    public static final String HEX_END = "0D0A";


}
