package com.android.meter.meter.http;

import android.os.Handler;
import android.os.Message;

import com.android.meter.meter.util.LogUtil;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;


/*******
 * 说明：此控制是校验发送命令后是否有接收到正确的反馈，带有连续三次发送功能。当服务器端和控制端同时发送和接收数据时，错误率会
 * 极大上升。（此程序暂时未对此中情况做判断处理。）
 */
public class SocketControl {
    private static final String TAG = SocketControl.class.getSimpleName();

    private static final int CONNECT_TIMEOUT = 5 * 1000;
    private static final int READ_TIMEOUT = 1 * 1000;
    private static final int MAX_RESPONSE_MILL_TIME = 1000;
    private static final int MAX_SEND_TIMES = 3;
    private boolean mHasResponsed = true;

    private Handler mHanlder = new Handler() {

        @Override
        public void handleMessage(Message msg) {


        }
    };

    private Socket mSocket;
    private SocketSender mSendThread;
    private SocketReceiver mReceiverThread;
    private IHttpListener mIHttpListener;
    private IHttpListener mControlListener = new IHttpListener() {
        @Override
        public void onResult(int state, String data) {
            LogUtil.d(TAG, "state: " + state + " ,data: " + data);
            if (mIHttpListener != null) {
                switch (state) {
                    case HTTPConstant.CONNECT_SUCCESS:
                    case HTTPConstant.CONNECT_FAIL:
                        mIHttpListener.onResult(state, data);
                        break;
                    case HTTPConstant.RECEIVE_SUCCESS:
                        if (mHasResponsed) {
                            mIHttpListener.onResult(HTTPConstant.RECEIVE_SUCCESS, data);
                            break;
                        } else {
                            if (System.currentTimeMillis() - mCmdSendTime <= MAX_RESPONSE_MILL_TIME && HTTPConstant.RECEIVED_SUCCESS.equals(data)) {
//                            mIHttpListener.onResult(HTTPConstant.RECEIVE_SUCCESS, data);
                                response(HTTPConstant.SEND_SUCCESS, data);
                                mSendTimes = 0;
                                mHasResponsed = true;
                                mHanlder.removeCallbacksAndMessages(null);
                            } else {
                                retry(HTTPConstant.RECEIVE_CHECK_FAILED, data);
                            }
                        }
                        break;
//                    case HTTPConstant.RECEIVE_CHECK_SUCCESS:
//                        break;
                    case HTTPConstant.RECEIVE_CHECK_FAILED:
                        retry(HTTPConstant.RECEIVE_CHECK_FAILED, data);
                        break;
                    case HTTPConstant.SEND_FAIL:
                        retry(HTTPConstant.SEND_FAIL, data);
                        break;
                    default:
                        break;
                }
            }
        }
    };

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
        LogUtil.d(TAG, "connect server: " + server + " , port: " + port);
        disconnect();
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    mSocket = new Socket();
//                    mSocket.setSoTimeout(READ_TIMEOUT);
                    mSocket.connect(new InetSocketAddress(server, port),
                            CONNECT_TIMEOUT);
                    LogUtil.d(TAG, "connect success");
                    if (mControlListener != null) {
                        mControlListener.onResult(HTTPConstant.CONNECT_SUCCESS, null);
                    }
                    mSendThread = new SocketSender(mSocket, mControlListener);
                    mReceiverThread = new SocketReceiver(mSocket, mControlListener);
                    mSendThread.start();
                    mReceiverThread.start();
                } catch (Exception e) {
                    LogUtil.e(TAG, "connect server failed.e: " + e);
                    e.printStackTrace();
                    mControlListener.onResult(HTTPConstant.CONNECT_FAIL, null);
                }
            }
        }).start();
    }

    public void disconnect() {
        LogUtil.d(TAG, "disconnect is invoked.");
        if (mSocket != null) {
            try {
                mSocket.close();
            } catch (IOException e) {
                LogUtil.e(TAG, "socket close exception: " + e);
            }
        }
        if (mHanlder != null) {
            mHanlder.removeCallbacksAndMessages(null);
        }
    }

    private int mSendTimes = 0;
    private long mCmdSendTime = 0;
    private String mTempString;

    public void sendMsg(String hex) {
        mCmdSendTime = System.currentTimeMillis();
        LogUtil.d(TAG, "sendMsg.hex: " + hex + " ,time: " + mCmdSendTime + ", mSendTimes: " + mSendTimes);
        if (mSendThread != null && mHasResponsed) {
            mSendTimes++;
            mHasResponsed = false;
            mTempString = hex;
            mHanlder.postDelayed(mRunnable, MAX_RESPONSE_MILL_TIME);
            mSendThread.sendMsg(hex);
        } else {
            LogUtil.sendCmdResult(TAG, hex, false);
            if (mControlListener != null) {
                mControlListener.onResult(HTTPConstant.SEND_FAIL, hex);
            }
        }

    }

    private Runnable mRunnable = new Runnable() {
        @Override
        public void run() {
            mControlListener.onResult(HTTPConstant.RECEIVE_CHECK_FAILED, mTempString);
//            mSendTimes = 0;
            mHasResponsed = true;
        }
    };

    private void retry(int state, String data) {
        if (mSendTimes < MAX_SEND_TIMES) {
            mHasResponsed = true;
            sendMsg(data);
        } else {
            if (HTTPConstant.RECEIVE_CHECK_FAILED == state) {
                response(HTTPConstant.RECEIVE_CHECK_FAILED, data);
            } else {
                response(HTTPConstant.SEND_FAIL, data);
            }
        }
    }


    private void response(int state, String data) {
        LogUtil.d(TAG, "state: " + state + " ,data: " + data + ", mHasResponsed: " + mHasResponsed);
        if (!mHasResponsed) {
            if (mIHttpListener != null) {
                mIHttpListener.onResult(state, data);
            }
            mHasResponsed = true;
            mSendTimes = 0;
            mHanlder.removeCallbacksAndMessages(null);
        }
    }

}
