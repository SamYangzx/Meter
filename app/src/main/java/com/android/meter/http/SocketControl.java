package com.android.meter.http;

import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;

import com.android.meter.util.LogUtil;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.List;

import static com.android.meter.http.SocketConstant.COMPUTER_NOT_RESPONSE;
import static com.android.meter.http.SocketConstant.SEND_ALL_SUCCESS;


/*******
 * 说明：此控制是校验发送命令后是否有接收到正确的反馈，带有连续三次发送功能。当服务器端和控制端同时发送和接收数据时，错误率会
 * 极大上升。（此程序暂时未对此中情况做判断处理。）
 */
public class SocketControl {
    private static final String TAG = LogUtil.COMMON_TAG + SocketControl.class.getSimpleName();

    private static final int CONNECT_TIMEOUT = 3 * 1000;
    private static final int READ_TIMEOUT = 1 * 1000;
    private static final int MAX_RESPONSE_MILL_TIME = 1500;
    private static final int MAX_SEND_TIMES = 1;
    private boolean mHasResponsed = true;
    private boolean mIsFile = false;

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
                    case SocketConstant.CONNECT_SUCCESS:
                    case SocketConstant.CONNECTING:
                    case SocketConstant.CONNECT_FAIL:
                        mIHttpListener.onResult(state, data);
                        break;
                    case SocketConstant.RECEIVE_SUCCESS:
                        if (mHasResponsed) {
                            mIHttpListener.onResult(SocketConstant.RECEIVE_SUCCESS, data);
                            break;
                        } else {
                            if (!mIsFile) {
                                if (System.currentTimeMillis() - mCmdSendTime <= MAX_RESPONSE_MILL_TIME && data != null && data.startsWith(SocketConstant.RECEIVED_SUCCESS)) {
//                            mIHttpListener.onResult(SocketConstant.RECEIVE_SUCCESS, data);
                                    response(SocketConstant.SEND_SUCCESS, data);
                                    mSendTimes = 0;
                                    mHasResponsed = true;
                                    mHanlder.removeCallbacksAndMessages(null);
                                } else {
                                    retry(SocketConstant.RECEIVE_CHECK_FAILED, data);
                                }
                            }
                        }
                        break;
//                    case SocketConstant.RECEIVE_CHECK_SUCCESS:
//                        break;
                    case SocketConstant.RECEIVE_CHECK_FAILED:
                        retry(SocketConstant.RECEIVE_CHECK_FAILED, data);
                        break;
                    case SEND_ALL_SUCCESS:
                        response(SEND_ALL_SUCCESS, data);
                        break;
                    case SocketConstant.SEND_SUCCESS:
                        if (mIsFile) {
                            response(SocketConstant.SEND_SUCCESS, data);
                        }
                        break;
                    case SocketConstant.SEND_FAIL: //发送线程遇到异常后，socket会断开。
                        disconnect();
                        response(SocketConstant.CONNECT_FAIL, data);
//                        retry(SocketConstant.SEND_FAIL, data);
                        break;
                    case COMPUTER_NOT_RESPONSE:
                        response(COMPUTER_NOT_RESPONSE, data);
                        break;
                    default:
                        break;
                }
            } else {
                LogUtil.e(TAG, "mIHttpListener is null");
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
                    mControlListener.onResult(SocketConstant.CONNECTING, null);
                    mSocket.connect(new InetSocketAddress(server, port),
                            CONNECT_TIMEOUT);
                    LogUtil.d(TAG, "connect success");
                    if (mControlListener != null) {
                        mControlListener.onResult(SocketConstant.CONNECT_SUCCESS, null);
                    }
                    mSendThread = new SocketSender(mSocket, mControlListener);
                    mReceiverThread = new SocketReceiver(mSocket, mControlListener);
                    mSendThread.start();
                    mReceiverThread.start();
                } catch (Exception e) {
                    LogUtil.e(TAG, "connect server failed.e: " + e);
                    e.printStackTrace();
                    mControlListener.onResult(SocketConstant.CONNECT_FAIL, null);
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
        mSendThread = null;
        mReceiverThread = null;
        if (mHanlder != null) {
            mHanlder.removeCallbacksAndMessages(null);
        }
    }

    private int mSendTimes = 0;
    private long mCmdSendTime = 0;
    private String mTempString;

    /**
     * @param list 要发送的文件路径列表
     * @param realSend true代表真正发送，false表示仅存储指令
     */
    public void sendFiles(List<String> list, boolean realSend) {
        if (list == null || list.size() == 0) {
            LogUtil.e(TAG, "paramater is error!!");
            LogUtil.printStack(TAG);
            return;
        }
        LogUtil.d(TAG, "sendFile.size: " + list.size());
        for (int i = 0; i < list.size(); i++) {
            String s = list.get(i);
            LogUtil.d(TAG, "Will send: " + s);
        }
        if (realSend) {
//            if (!mHasResponsed) {
//                response(HANDLEING_CMD, file);
//                return;
//            }
            mIsFile = true;
            mHasResponsed = false;
            if (isConneced()) {
//                mSendThread.setFileCount(list.size());
                for (int i = 0; i < list.size(); i++) {
                    String file = list.get(i);
                    mSendThread.sendMsg(file);
                }
            } else {
                response(SocketConstant.SEND_FAIL, list.get(0), true);
            }
        } else {
            for (int i = 0; i < list.size(); i++) {
                String s = list.get(i);
                LogUtil.saveCmd(s);
            }
        }
    }

    /**
     * @deprecated 被遗弃，20180402后仅维护{@link #sendFiles}
     * @param file
     * @param count
     * @param realSend
     */
    public void sendFile(String file, int count, boolean realSend) {
        LogUtil.d(TAG, "sendFile: " + file + " ,count: " + count);
        if (realSend) {
//            if (!mHasResponsed) {
//                response(HANDLEING_CMD, file);
//                return;
//            }
            mIsFile = true;
            mHasResponsed = false;
            if (isConneced()) {
                mSendThread.sendMsg(file);
            } else {
                response(SocketConstant.SEND_FAIL, file, true);
            }
        } else {
            LogUtil.saveCmd(file);
        }
    }

    public void sendMsg(String hex, boolean realSend) {
        LogUtil.d(TAG, "sendMsg: " + hex);
        if (realSend) {
//            if (!mHasResponsed) {
//                response(HANDLEING_CMD, hex);
//                return;
//            }
            mCmdSendTime = System.currentTimeMillis();
            LogUtil.d(TAG, "sendMsg.hex: " + hex + " ,time: " + mCmdSendTime + ", mSendTimes: " + mSendTimes);
            mSendTimes++;
            mTempString = hex;
            mIsFile = false;
            if (!isConneced()) {
                mHasResponsed = false;
                mControlListener.onResult(SocketConstant.CONNECT_FAIL, hex);
                response(SocketConstant.SEND_FAIL, hex, true);
            } else {
                mHasResponsed = false;
//                    mHanlder.postDelayed(mRunnable, MAX_RESPONSE_MILL_TIME);
                mSendThread.sendMsg(hex);
            }

        } else {
            LogUtil.saveCmd(hex);
        }
    }


    public void sendMsg(String hex) {
        sendMsg(hex, false);
    }

    private Runnable mRunnable = new Runnable() {
        @Override
        public void run() {
            LogUtil.d(TAG, "Timeout!!!");
            mControlListener.onResult(SocketConstant.RECEIVE_CHECK_FAILED, mTempString);
        }
    };

    private void retry(int state, String data) {
        if (mSendTimes < MAX_SEND_TIMES) {
            mHasResponsed = true;
            sendMsg(data);
        } else {
            if (SocketConstant.RECEIVE_CHECK_FAILED == state) {
                response(SocketConstant.RECEIVE_CHECK_FAILED, data);
            } else {
                response(SocketConstant.SEND_FAIL, data);
            }
        }
    }


    private void response(int state, String data) {
        response(state, data, false);
    }

    /**
     * @param state
     * @param data         当发送的是文件时，只需返回其中一个文件路径就行。
     * @param responseSoon 是否立即反馈
     */
    private void response(int state, String data, boolean responseSoon) {
        LogUtil.d(TAG, "state: " + state + " ,data: " + data + ", mHasResponsed: " + mHasResponsed);
        if (isConneced()) {
            if (data.equals(mSendThread.getCurrentCmd())) {
//                mHasResponsed = true;
                mSendTimes = 0;
                mHanlder.removeCallbacksAndMessages(null);
                if (mIHttpListener != null) {
                    mIHttpListener.onResult(state, data);
                }
            }
        } else {
            if (responseSoon) {
                mHasResponsed = true;
                mSendTimes = 0;
                mHanlder.removeCallbacksAndMessages(null);
                if (mIHttpListener != null) {
                    mIHttpListener.onResult(state, data);
                }
            }
        }
    }

    public boolean isConneced() {
        if (mSocket != null && mSocket.isConnected() && mSendThread != null) {
            return true;
        }
        return false;
    }

}
