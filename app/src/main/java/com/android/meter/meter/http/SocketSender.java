package com.android.meter.meter.http;

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
        String msg = "";
        try {
//            mOutStream = new BufferedWriter(new OutputStreamWriter(mSocket.getOutputStream(), "UTF-8"));
//            BufferedReader inputReader = new BufferedReader(new InputStreamReader(System.in));
//            mOutStream = new DataOutputStream(mSocket.getOutputStream());
            mOutStream = mSocket.getOutputStream();
//            os.write();
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
                    LogUtil.d(TAG, "Socket id closed!");
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
        } catch (Exception e) {
            LogUtil.sendCmdResult(TAG, msg, false);
            LogUtil.e(TAG, "e: " + e);
            if (mIHttpListener != null) {
                mIHttpListener.onResult(HTTPConstant.SEND_FAIL, msg);
            }
            e.printStackTrace();
        } finally {
            if (mOutStream != null) {
                try {
                    mOutStream.close();
                } catch (IOException e) {

                }
            }
            if (mSocket != null) {
                try {
                    mSocket.close();
                } catch (IOException e) {

                }
            }
        }
    }

    public synchronized void sendMsg(String hex) {
        if (mSocket != null) {
            LogUtil.d(TAG, "sendMsg.add hex");
            mMsgQueue.offer(hex);
        } else {
            LogUtil.d(TAG, "sendMsg.add hex failed.");
            mIHttpListener.onResult(HTTPConstant.SEND_FAIL, hex);
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
