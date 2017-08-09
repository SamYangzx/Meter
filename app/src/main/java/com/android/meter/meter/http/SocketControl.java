package com.android.meter.meter.http;

import android.util.Log;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;

public class SocketControl {
    private static final String TAG = SocketControl.class.getSimpleName();

    private static final int CONNECT_TIMEOUT = 5 * 1000;
    private static final int READ_TIMEOUT = 2 * 1000;

    private Socket mSocket;
    private SocketSender mSendThread;
    private SocketReceiver mReceiverThread;
    private IHttpListener mIHttpListener;
    private static SocketControl mSocketControl = new SocketControl();

    public static SocketControl getInstance() {
        if (mSocketControl == null) {
            mSocketControl = new SocketControl();
        }
        return mSocketControl;
    }

  /*  public static void main(String[] args) {
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
            new SocketSender(socket, mIHttpListener).start();
            new SocketReceiver(socket, mIHttpListener).start();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }*/

    public void setListener(IHttpListener listener) {
        mIHttpListener = listener;
    }

    /**
     * should set listener first.
     */
    public void connect(final String server, final int port) {
        Log.d(TAG, "connect server: " + server + " , port: " + port);
        disconnect();
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    mSocket = new Socket();
                    mSocket.connect(new InetSocketAddress(server, port),
                            CONNECT_TIMEOUT);
                    mSocket.setSoTimeout(READ_TIMEOUT);
                    if (mIHttpListener != null) {
                        mIHttpListener.onResult(HTTPConstant.CONNECT_SUCCESS, null);
                    }
                    mSendThread = new SocketSender(mSocket, mIHttpListener);
                    mReceiverThread = new SocketReceiver(mSocket, mIHttpListener);
                    mSendThread.start();
                    mReceiverThread.start();
                } catch (IOException e) {
                    Log.d(TAG, "connect server failed.e: " + e);
                    e.printStackTrace();
                    mIHttpListener.onResult(HTTPConstant.CONNECT_FAIL, null);
                }
            }
        }).start();
    }

    public void disconnect() {
        Log.d(TAG, "disconnect is invoked.");
        if (mSocket != null) {
            try {
                mSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "socket close exception: " + e);
            }
        }
    }

    public void sendMsg(String data) {
        if (mSendThread != null) {
            mSendThread.sendMsg(data);
        }

    }


}
