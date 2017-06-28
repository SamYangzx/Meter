package com.android.meter.meter.http;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;

public class ClientControl {
    // 监听端口号
    private static final int DEFAULT_PORT = 2345;
    // 绑定到本机的IP地址
    private static final String DEFAULT_SERVER = "192.168.1.153";
    private Socket mSocket;
    private ClientMessageSender mSendThread;
    private ClientMessageReceiver mReceiverThread;
    private IHttpListener mIHttpListener;

    public static void main(String[] args) {
        try {
            System.out.println("正在连接Socket服务器");
            Socket socket = new Socket(InetAddress.getByName(DEFAULT_SERVER), DEFAULT_PORT);
            System.out.println("已连接\n==================================");
            IHttpListener
                    mIHttpListener = new IHttpListener() {
                @Override
                public void onResult(int state, String data) {

                }
            };
            new ClientMessageSender(socket, mIHttpListener).start();
            new ClientMessageReceiver(socket, mIHttpListener).start();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public ClientControl(final String server, final int port, final IHttpListener iHttpListener) {

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    mSocket = new Socket(InetAddress.getByName(server), port);
                    iHttpListener.onResult(HTTPConstant.CONNECT_SUCCESS,null);
                    mSendThread = new ClientMessageSender(mSocket, mIHttpListener);
                    mReceiverThread = new ClientMessageReceiver(mSocket, mIHttpListener);
                    mSendThread.start();
                    mReceiverThread.start();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    public ClientControl(IHttpListener iHttpListener) {
        this(DEFAULT_SERVER, DEFAULT_PORT, iHttpListener);
    }

    public void sendMsg(String data) {
        if (mSendThread != null) {
            mSendThread.sendMsg(data);
        }

    }


}
