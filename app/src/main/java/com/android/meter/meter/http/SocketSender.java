package com.android.meter.meter.http;

import com.android.meter.meter.util.FileUtil;
import com.android.meter.meter.util.LogUtil;
import com.android.meter.meter.util.StringUtil;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.LinkedList;
import java.util.Queue;

import static com.android.meter.meter.util.CommandUtil.SEPERATOR;
import static com.android.meter.meter.util.FileUtil.FILE_END;
import static com.android.meter.meter.util.FileUtil.FILE_START;
import static com.android.meter.meter.util.FileUtil.TOTAL_FILE_END;

public class SocketSender extends Thread {
    private static final String TAG = LogUtil.COMMON_TAG + SocketSender.class.getSimpleName();

    private Socket mSocket;
    private boolean mContinue = true;
    private boolean mIsLatestFile = false;
    private Queue<String> mMsgQueue = new LinkedList<String>();
    private IHttpListener mIHttpListener;
    //    private DataOutputStream mOutStream;
//    private BufferedWriter mOutStream;
    private OutputStream mOutStream;   //优先使用此输出流
    private int mFileCount = 0;
    private int mFileIndex = 0;
    private String mCurrentCmd;

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
            while (mContinue) {
//                    mCurrentCmd = inputReader.readLine();
                if (mMsgQueue.isEmpty()) {
                    continue;
                }
                mCurrentCmd = mMsgQueue.poll();
                if (mCurrentCmd == null || mCurrentCmd == "") {
                    continue;
                }
                if (mSocket.isClosed()) {
                    LogUtil.d(TAG, "Socket id closed!");
                    break;
                }
//                    mOutStream.write(mCurrentCmd);
//                    mOutStream.newLine();
                if (FileUtil.isFile(mCurrentCmd)) {
                    String nextCmd = mMsgQueue.peek();
                    if (FileUtil.isFile(nextCmd)) {
                        mIsLatestFile = false;
                    } else {
                        mIsLatestFile = true;
                    }
                    sendFile(mCurrentCmd);
                } else {
                    writeMsg(mCurrentCmd);
                }
//                    mOutStream.write("\0".getBytes());
//                    writeMsg(SocketConstant.HEX_END);
                mOutStream.flush();
                LogUtil.sendCmdResult(TAG, mCurrentCmd, true);
                if (mIHttpListener != null) {
                    if (FileUtil.isFile(mCurrentCmd)) {
                        if (mIsLatestFile) {
                            mIHttpListener.onResult(SocketConstant.SEND_SUCCESS, mCurrentCmd);
                        }
                    } else {
                        mIHttpListener.onResult(SocketConstant.SEND_SUCCESS, mCurrentCmd);
                    }
                }
            }
        } catch (Exception e) {
            LogUtil.sendCmdResult(TAG, mCurrentCmd, false);
            LogUtil.e(TAG, "e: " + e);
            if (mIHttpListener != null) {
                mIHttpListener.onResult(SocketConstant.SEND_FAIL, mCurrentCmd);
            }
            resetData();
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

    /**
     * Use string if the msg is file path.
     *
     * @param hexOrFile
     */
    public synchronized void sendMsg(String hexOrFile) {
        if (mSocket != null) {
            LogUtil.d(TAG, "sendMsg.add: " + hexOrFile);
            mMsgQueue.offer(hexOrFile);
        } else {
            LogUtil.d(TAG, "sendMsg.add hexOrFile failed.");
            mIHttpListener.onResult(SocketConstant.SEND_FAIL, hexOrFile);
            LogUtil.sendCmdResult(TAG, hexOrFile, false);
        }
    }

    public void setFileCount(int count) {
        mFileCount = count;
    }

    /**
     * send string.
     *
     * @param data
     * @throws IOException
     */
    private void writeMsg(String data) throws IOException {
//        mIsLatestFile = false;
        if (SocketConstant.WRITE_HEX) {
            mOutStream.write(StringUtil.hexString2Bytes(data));
        } else {
            mOutStream.write(StringUtil.string2Bytes(data));
        }
    }

    public void setContinue(boolean conti) {
        mContinue = conti;
    }


    /**
     * Send file. 开始符_文件格式_文件大小_文件名_文件_结束符号
     *
     * @param file
     */
    private void sendFile(String file) {
        LogUtil.d(TAG, "sendFile: " + file);
        File f = new File(file);
        if (f == null || !f.exists()) {
            LogUtil.e(TAG, "sendFile. not exist: " + file);
            return;
        }
        try {
            InputStream is = new FileInputStream(f);
            String ext = FileUtil.getExtensionName(file);
            String name = f.getName();

            long fileLength = f.length();
            StringBuilder sb = new StringBuilder();
            if (mIsLatestFile) {
                sb.append(SEPERATOR);
            }
            sb.append(FILE_START).append(SEPERATOR)
                    .append(ext).append(SEPERATOR)
                    .append(Long.toString(fileLength)).append(SEPERATOR)
                    .append(name).append(SEPERATOR);
            LogUtil.v(TAG, "file is pref: " + sb);
            mOutStream.write(sb.toString().getBytes());

          /*发送图片文件，对应image*/
            int length;
            byte[] b = new byte[1024];
            while ((length = is.read(b)) > 0) {
                mOutStream.write(b, 0, length);
            }
            mFileIndex++;
            if (mIsLatestFile) {
                mOutStream.write((SEPERATOR + TOTAL_FILE_END).getBytes());
            } else {
                mOutStream.write((SEPERATOR + FILE_END).getBytes());
            }
        } catch (IOException e) {
            LogUtil.e(TAG, "send: " + file + " failed");
        }
    }

    private void resetData() {
        mFileCount = 0;
        mFileIndex = 0;
    }

    public String getCurrentCmd() {
        LogUtil.d(TAG, "mCurrentCmd: " + mCurrentCmd);
        return mCurrentCmd;
    }

}
