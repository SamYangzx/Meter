package com.android.meter.meter.http;

public class HttpState {

    public static final int SEND_MSG_SUCCESS = 0X04;
    public static final int SEND_MSG_FAILED = 0X05;
    
    public static final String ACTION_CONNECT_SFTPSERVER_CONNECTING = "connecting_ftpserver";
    public static final String ACTION_CONNECT_SFTPSERVER_SUCCESS = "connect_ftpserver_success";
    public static final String ACTION_CONNECT_SFTPSERVER_FAILED = "connect_ftpserver_failed";
    public static final String ACTION_UPLOAD_PIC_SUCCESS = "upload_pic_success";
    public static final String ACTION_UPLOADING_PIC = "uploading_pic";
    public static final String ACTION_UPLOAD_PIC_FAILED = "upload_pic_failed";
    public static final String ACTION_DISCONNECT_FTPSERVER = "disconnect_ftpserver";
    public static final String ACTION_CONNECT_SOCKETSERVER_CONNECTING = "connecting_socketserver";
    public static final String ACTION_CONNECT_SOCKETSERVER_SUCCESS = "connect_socketserver_success";
    public static final String ACTION_CONNECT_SOCKETSERVER_FAILED = "connect_socketserver_failed";
    public static final String ACTION_SENDMSG_SUCCESS = "sendmsg_success";
    public static final String ACTION_SENDMSG_FAILED = "sendmsg_failed";

}
