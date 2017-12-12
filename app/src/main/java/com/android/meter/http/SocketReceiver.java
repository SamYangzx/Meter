package com.android.meter.http;

import com.android.meter.util.LogUtil;
import com.android.meter.util.StringUtil;

import java.io.BufferedReader;
import java.io.InputStream;
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
        byte input[] = new byte[10]; //此值根据实际情况做修改
        try {
            // 获取socket的输 出\入 流
            BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream(), "UTF-8"));
            InputStream inputStream = socket.getInputStream();
            byte btyeCount;
            while (true) {
                if (socket.isClosed()) {
                    System.out.println("error: Socket closed, can not receive message!!!");
                    reader.close();
                    socket.close();
                    break;
                }
//                content = reader.readLine(); //服务器端需要发送一个回车符
//
//                if (content == null || content == "") {
//                    continue;
//                }

                if(inputStream.read(input) == -1){
                    continue;
                }
                LogUtil.receiveCmdResult(TAG, input);
                if (mIHttpListener != null) {
                    mIHttpListener.onResult(SocketConstant.RECEIVE_SUCCESS, StringUtil.bytes2HexString(input));
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
                mIHttpListener.onResult(SocketConstant.RECEIVE_FAILED, StringUtil.bytes2HexString(input));
            }
        }
    }

}
