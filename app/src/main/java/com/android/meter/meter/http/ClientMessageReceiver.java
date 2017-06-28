package com.android.meter.meter.http;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.Socket;

public class ClientMessageReceiver extends Thread {

    private Socket socket;
    private IHttpListener mIHttpListener;

    public ClientMessageReceiver(Socket socket, IHttpListener listener) {
        this.socket = socket;
        mIHttpListener = listener;
    }

    @Override
    public void run() {
        try {
            // 获取socket的输 出\入 流
            BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream(), "UTF-8"));
            //接收到的消息
            String content;
            while (true) {
                if (socket.isClosed()) {
                    System.out.println("Socket已关闭，无法获取消息");
                    reader.close();
                    socket.close();
                    break;
                }
                content = reader.readLine();
                if(mIHttpListener != null){
                    mIHttpListener.onResult(HTTPConstant.RECEIVE_MSG, content);
                }

                if (content.equals("bye")) {
                    System.out.println("对方请求关闭连接,无法继续进行聊天");
                    reader.close();
                    socket.close();
                    break;
                }
                System.out.println("Receive: " + content + "\n");
            }
            reader.close();
            socket.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
