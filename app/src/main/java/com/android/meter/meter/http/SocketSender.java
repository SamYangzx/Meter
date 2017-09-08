package com.android.meter.meter.http;

import android.util.Log;

import com.android.meter.meter.util.LogUtil;
import com.android.meter.meter.util.StringUtil;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.util.LinkedList;
import java.util.Queue;

public class SocketSender extends Thread {
    private static final String TAG = LogUtil.COMMON_TAG + SocketSender.class.getSimpleName();

    private Socket mSocket;
    private boolean mContinue = true;
    private Queue<String> mMsgQueue = new LinkedList<String>();
    private IHttpListener mIHttpListener;
    //    private DataOutputStream mOutStream;
//    private BufferedWriter mOutStream;
    private OutputStream mOutStream;   //优先使用此输出流

    public SocketSender(Socket socket, IHttpListener listener) {
        mSocket = socket;
        mIHttpListener = listener;
    }

    @Override
    public void run() {
        try {
//            mOutStream = new BufferedWriter(new OutputStreamWriter(mSocket.getOutputStream(), "UTF-8"));
//            BufferedReader inputReader = new BufferedReader(new InputStreamReader(System.in));
//            mOutStream = new DataOutputStream(mSocket.getOutputStream());
            mOutStream = mSocket.getOutputStream();

//            os.write();
            String msg = "";
            try {
                while (mContinue) {
//                    msg = inputReader.readLine();
                    if (mMsgQueue.isEmpty()) {
                        continue;
                    }
                    msg = mMsgQueue.poll();
                    if (msg == null || msg == "") {
                        continue;
                    }
                    if (mSocket.isClosed()) {
                        System.out.println("Socket id closed!");
                        mOutStream.close();
                        mSocket.close();
                        break;
                    }
//                    mOutStream.write(msg);
//                    mOutStream.newLine();
                    writeMsg(msg);
//                    mOutStream.write("\0".getBytes());
//                    writeMsg(HTTPConstant.HEX_END);
                    mOutStream.flush();
                    LogUtil.sendCmdResult(TAG, msg, true);
                    if (mIHttpListener != null) {
                        mIHttpListener.onResult(HTTPConstant.SEND_SUCCESS, msg);
                    }
                }
            } catch (IOException e) {
                LogUtil.sendCmdResult(TAG, msg, false);
                Log.e(TAG, "e: " + e);
                mIHttpListener.onResult(HTTPConstant.SEND_FAIL, null);
                if (mOutStream != null) {
                    mOutStream.close();
                }
                if (mSocket != null) {
                    mSocket.close();
                }
                e.printStackTrace();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public synchronized void sendMsg(String hex) {
        if (mSocket != null) {
            LogUtil.d(TAG, "add hex");
            mMsgQueue.offer(hex);
        } else {
            LogUtil.d(TAG, "add hex failed.");
            mIHttpListener.onResult(HTTPConstant.SEND_FAIL, null);
            LogUtil.sendCmdResult(TAG, hex, false);
        }
    }

    private void writeMsg(String data) throws IOException {
        if (HTTPConstant.WRITE_HEX) {
            mOutStream.write(StringUtil.hexString2Bytes(data));
        } else {
            mOutStream.write(StringUtil.string2Bytes(data));
        }
    }

    public void setContinue(boolean conti) {
        mContinue = conti;
    }

}
