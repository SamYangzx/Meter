package com.android.meter.meter.http;

import com.android.meter.meter.util.LogUtil;
import com.android.meter.meter.util.StringUtil;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.Socket;

public class SocketReceiver extends Thread {
    private static final String TAG = LogUtil.COMMON_TAG + SocketReceiver.class.getSimpleName();

    private Socket socket;
    private IHttpListener mIHttpListener;

    public SocketReceiver(Socket socket, IHttpListener listener) {
        this.socket = socket;
        mIHttpListener = listener;
    }

    @Override
    public void run() {
        String content = null;
        try {
            // 获取socket的输 出\入 流
            BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream(), "UTF-8"));
            //接收到的消息
            while (true) {
                if (socket.isClosed()) {
                    System.out.println("error: Socket closed, can not receive message!!!");
                    reader.close();
                    socket.close();
                    break;
                }
                content = reader.readLine();
                if (content == null || content == "") {
                    continue;
                }
                LogUtil.d(TAG, "Receive: " + StringUtil.hex2String(content));
                if (mIHttpListener != null) {
                    mIHttpListener.onResult(HTTPConstant.RECEIVE_SUCCESS, content);
                }
//                if (content.equals("bye")) {
//                    System.out.println("对方请求关闭连接,无法继续进行聊天");
//                    reader.close();
//                    socket.close();
//                    break;
//                }
            }
            reader.close();
            socket.close();
        } catch (Exception e) {
            e.printStackTrace();
            LogUtil.d(TAG, "e: " + e);
            if (mIHttpListener != null) {
                mIHttpListener.onResult(HTTPConstant.RECEIVE_FAILED, content);
            }
        }
    }

}
