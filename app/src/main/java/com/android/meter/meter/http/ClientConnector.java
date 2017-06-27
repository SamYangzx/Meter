package com.android.meter.meter.http;

import android.util.Log;

import com.android.meter.meter.util.LogUtil;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

/**
 * Created by fenghe on 2017/6/26.
 */

public class ClientConnector extends Thread {
    private static final String TAG = LogUtil.COMMON_TAG + ClientConnector.class.getSimpleName();

    public static final String DEFAULT_SERVER = "192.168.1.153";
    public static final int DEFAULT_PORT = 2345;


    @Override
    public void run() {
        try {
            connect();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }


    private Socket mClient;
    /**
     * 服务端的ip
     */
    private String mDstName;
    /**
     * 服务端端口号
     */
    private int mDesPort;

    private ConnectLinstener mListener;


    public ClientConnector(String dstName, int dstPort) {
        this.mDstName = dstName;
        this.mDesPort = dstPort;
    }

    /**
     * 与服务端进行连接
     *
     * @throws IOException
     */
    public void connect() throws IOException {
        if (mClient == null) {
            mClient = new Socket(mDstName, mDesPort);
        }

        //获取其他客户端发送过来的数据
        mInputStream = mClient.getInputStream();
        mOutputStream = mClient.getOutputStream();
        byte[] buffer = new byte[1024];
        int len = -1;
        while (mRead) {
            if ((len = mInputStream.read(buffer)) != -1) {

                String data = new String(buffer, 0, len);
                Log.d(TAG, "receive: " + data);

                //通过回调接口将获取到的数据推送出去
                if (mListener != null) {
                    mListener.onReceiveData(data);
                }
            }
        }
    }

    InputStream mInputStream;
    private boolean mRead = true;


    /**
     * 认证方法，这个方法是用来进行客户端一对一发送消息的
     * 在实际项目中进行即时通讯时都需要进行登录，这里就是
     * 模拟客户端的账号
     *
     * @param authName
     */
    public void auth(String authName) throws IOException {
        if (mClient != null) {
            //将客户端账号发送给服务端，让服务端保存
            OutputStream outputStream = mClient.getOutputStream();
            //模拟认证格式，以#开头
            outputStream.write(("#" + authName).getBytes());
        }
    }

    OutputStream mOutputStream;

    public void send(String data) {
        try {
            Log.d(TAG, "thread: " + Thread.currentThread().getId());
            mOutputStream.write(data.getBytes());
            Log.d(TAG, "send: " + data);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 将数据发送给指定的接收者
     *
     * @param receiver 信息接数者
     * @param data     需要发送的内容
     */
    public void send(String receiver, String data) throws IOException {
        OutputStream outputStream = mClient.getOutputStream();
        //模拟内容格式：receiver+  # + content
        outputStream.write((receiver + "#" + data).getBytes());
    }

    /**
     * 断开连接
     *
     * @throws IOException
     */
    public void disconnect() throws IOException {
        if (mClient != null) {
            mClient.close();
            mClient = null;
        }
    }


    public void setOnConnectLinstener(ConnectLinstener linstener) {
        this.mListener = linstener;
    }

    /**
     * 数据接收回调接口
     */
    public interface ConnectLinstener {
        void onReceiveData(String data);
    }

    public boolean isConnected() {
        if (mClient != null) {
            return mClient.isConnected();
        }
        return false;
    }
}
