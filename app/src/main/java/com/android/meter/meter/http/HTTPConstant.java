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
    public static final int RECEIVE_SUCCESS = 0x105;
    public static final int RECEIVE_FAILED = 0x106;
    public static final int RECEIVE_CHECK_SUCCESS = 0x107;
    public static final int RECEIVE_CHECK_FAILED = 0x108;
    public static final int HAS_NOT_RESPONSE = 0x109;
    public static final String RECEIVED_SUCCESS = "BB03EF01EDCC";

    public static final boolean WRITE_HEX = true;
    public static final String HEX_END = "0D0A";

    public static final String SAVE_IP = "save_ip";
    public static final String SAVE_PORT = "save_port";

}
