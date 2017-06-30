package com.android.meter.meter.http;

import android.util.Log;

import com.android.meter.meter.util.LogUtil;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.LinkedList;
import java.util.Queue;

public class SocketSender extends Thread {
    private static final String TAG = LogUtil.COMMON_TAG + SocketSender.class.getSimpleName();

    private Socket mSocket;
    private boolean mContinue = true;
    private Queue<String> mMsgQueue = new LinkedList<String>();
    private IHttpListener mIHttpListener;


    public SocketSender(Socket socket, IHttpListener listener) {
        mSocket = socket;
        mIHttpListener = listener;
    }

    @Override
    public void run() {
        try {
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(mSocket.getOutputStream(), "UTF-8"));
//            BufferedReader inputReader = new BufferedReader(new InputStreamReader(System.in));
            try {
                String msg;
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
                        writer.close();
                        mSocket.close();
                        break;
                    }
                    writer.write(msg);
                    writer.newLine();
                    writer.flush();
                    Log.d(TAG, "send: " + msg);
                    if (mIHttpListener != null) {
                        mIHttpListener.onResult(HTTPConstant.SEND_SUCCESS, msg);
                    }
                }
            } catch (IOException e) {
                Log.d(TAG, "e: " + e);
                mIHttpListener.onResult(HTTPConstant.SEND_FAIL, null);
                if (writer != null) {
                    writer.close();
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

    public synchronized void sendMsg(String data) {
        if (mSocket != null) {
            Log.d(TAG, "add data");
            mMsgQueue.offer(data);
        } else {
            Log.d(TAG, "add data failed.");
            mIHttpListener.onResult(HTTPConstant.SEND_FAIL, null);
        }
    }

    public void setContinue(boolean conti) {
        mContinue = conti;
    }

}
