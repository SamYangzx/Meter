package com.android.meter.meter.http;

/**
 * Created by fenghe on 2017/6/28.
 */

public class SocketConstant {
    public static final String DEFAULT_SERVER = "192.168.5.1";
    public static final int DEFAULT_PORT = 7001;

    public static final int SEND_SUCCESS = 101;
    public static final int SEND_FAIL = 102;
    public static final int CONNECT_SUCCESS = 103;
    public static final int CONNECT_FAIL = 104;
    public static final int RECEIVE_SUCCESS = 105;
    public static final int RECEIVE_FAILED = 106;
    public static final int RECEIVE_CHECK_SUCCESS = 107;
    public static final int RECEIVE_CHECK_FAILED = 108;
    public static final int COMPUTER_NOT_RESPONSE = 109; //电脑端未反馈
    public static final int HANDLEING_CMD = 110;// 正在执行指令中
    public static final int CONNECTING = 111;
    public static final String RECEIVED_SUCCESS = "BB03EF01EDCC";

    public static final boolean WRITE_HEX = true;
    public static final String HEX_END = "0D0A";

    public static final String SAVE_IP = "save_ip";
    public static final String SAVE_PORT = "save_port";

}
