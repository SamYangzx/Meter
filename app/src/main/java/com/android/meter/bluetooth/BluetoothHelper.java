package com.android.meter.bluetooth;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;

import com.android.meter.R;
import com.android.meter.util.CommandUtil;
import com.android.meter.util.LogUtil;
import com.android.meter.util.StringUtil;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Set;
import java.util.UUID;

public class BluetoothHelper {

    private static final String TAG = LogUtil.COMMON_TAG + "BluetoothHelper";
    private static final boolean D = true;
    private static final String NAME = "BluetoothChatActivity";
    private static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private final BluetoothAdapter mAdapter;

    private static BluetoothHelper mBluetoothHelper;
    private BluetoothDevice mCurrentDevice;

    private Handler mHandler;
    private AcceptThread mAcceptThread;
    private ConnectThread mConnectThread;
    private ConnectedThread mConnectedThread;
    private int mState;
    //蓝牙链接失败或蓝牙断开并未包含在下面。
    public static final int STATE_NONE = 0;
    public static final int STATE_LISTEN = 1;
    public static final int STATE_CONNECTING = 2;
    public static final int STATE_CONNECTED = 3;

    private Context mContext;
//    private IMsgListener mIMsgListener;
//
//    public void setIMsgListener(IMsgListener iMsgListener) {
//        mIMsgListener = iMsgListener;
//    }

    private BluetoothHelper(Context context) {
        this(context, null);
    }

    public static BluetoothHelper getBluetoothHelper(Context context) {
        if (mBluetoothHelper == null) {
            mBluetoothHelper = new BluetoothHelper(context);
        }
        return mBluetoothHelper;
    }

    private BluetoothHelper(Context context, Handler handler) {
        mAdapter = BluetoothAdapter.getDefaultAdapter();
        mState = STATE_NONE;
        mContext = context;
        setmHandler(handler);
    }

    public void setmHandler(Handler handler) {
        LogUtil.d(TAG, "setmHandler is invoked. handler: " + handler);
        if (mHandler != null) {
            mHandler.removeCallbacksAndMessages(null);
            mHandler = null;
        } else {
            LogUtil.printStack(TAG);
        }
        mHandler = handler;
    }

    public BluetoothAdapter getBluetoothAdapter() {
        return mAdapter;
    }

    public void enableBT() {
        if (mAdapter != null && !mAdapter.isEnabled()) {
            mAdapter.enable();
        }
    }


    private BluetoothDevice getSavedBTDevice(String address) {
        Set<BluetoothDevice> pairedDevices = mAdapter.getBondedDevices();

        // If there are paired devices, add each one to the ArrayAdapter
        if (pairedDevices.size() > 0) {
            for (BluetoothDevice device : pairedDevices) {
                if (device.getAddress().equals(address)) {
                    return device;
                }
            }
        }
        return null;
    }

    /**
     * Set the current state of the chat connection
     *
     * @param state An integer defining the current connection state
     */
    private synchronized void setState(int state) {
        if (D) LogUtil.d(TAG, "setState() " + mState + " -> " + state);
        mState = state;

        // Give the new state to the Handler so the UI Activity can update
        if (mHandler != null) {
            mHandler.obtainMessage(BtConstant.MESSAGE_STATE_CHANGE, state, -1).sendToTarget();
        } else {
            LogUtil.d(TAG, "setState.mHandler is null!");
        }
    }

    public String getStateString() {
        switch (getState()) {
            case STATE_NONE:
//                return "no BT device";
            case STATE_LISTEN:
                return mContext.getString(R.string.title_not_bt_connected);
            case STATE_CONNECTING:
//                return "BT connecting";
                return mContext.getString(R.string.title_bt_connecting);
            case STATE_CONNECTED:
//                return "BT connected";
                return mContext.getString(R.string.title_bt_connected);
        }
        return "";
    }

    /**
     * Return the current connection state.
     */
    public synchronized int getState() {
        return mState;
    }

    /**
     * Start the chat service. Specifically start AcceptThread to begin a
     * session in listening (server) mode. Called by the Activity onResume()
     */
    public synchronized void start() {
        if (D) LogUtil.d(TAG, "start");

        // Cancel any thread attempting to make a connection
        if (mConnectThread != null) {
            mConnectThread.cancel();
            mConnectThread = null;
        }

        // Cancel any thread currently running a connection
        if (mConnectedThread != null) {
            mConnectedThread.cancel();
            mConnectedThread = null;
        }

        // Start the thread to listen on a BluetoothServerSocket
        if (mAcceptThread == null) {
            mAcceptThread = new AcceptThread();
            mAcceptThread.start();
        }
        setState(STATE_LISTEN);
    }

    public void connect(String address) {
        connect(getSavedBTDevice(address));
    }


    /**
     * Start the ConnectThread to initiate a connection to a remote device.
     *
     * @param device The BluetoothDevice to connect
     */
    public synchronized void connect(BluetoothDevice device) {
        LogUtil.d(TAG, "connect to: " + device);
        if (device == null) {
            LogUtil.d(TAG, "device == null");
            setState(STATE_NONE);
            return;
        }

        // Cancel any thread attempting to make a connection
        if (mState == STATE_CONNECTING) {
            if (mConnectThread != null) {
                mConnectThread.cancel();
                mConnectThread = null;
            }
        }

        //If current device is connected, it will not be connected again.add 20170807
        if (mCurrentDevice != null && mCurrentDevice.getAddress().equals(device.getAddress())) {
            if ((mConnectedThread != null) && (mConnectedThread.mmSocket != null) && (mConnectedThread.mmSocket.isConnected())) {
                LogUtil.d(TAG, "mmSocket has connect!");
                return;
            }
        }


        // Cancel any thread currently running a connection
        if (mConnectedThread != null) {
            mConnectedThread.cancel();
            mConnectedThread = null;
        }

        // Start the thread to connect with the given device
        mConnectThread = new ConnectThread(device);
        mConnectThread.start();
        setState(STATE_CONNECTING);
    }


    private synchronized void connected(BluetoothSocket socket, BluetoothDevice device) {
        if (D) LogUtil.d(TAG, "connected is invoked");


        if (mConnectThread != null) {
            mConnectThread.cancel();
            mConnectThread = null;
        }


        if (mConnectedThread != null) {
            mConnectedThread.cancel();
            mConnectedThread = null;
        }


        if (mAcceptThread != null) {
            mAcceptThread.cancel();
            mAcceptThread = null;
        }


        mConnectedThread = new ConnectedThread(socket);
        mConnectedThread.start();

        if (mHandler != null) {
            Message msg = mHandler.obtainMessage(BtConstant.MESSAGE_DEVICE_NAME);
            Bundle bundle = new Bundle();
            bundle.putString(BluetoothChatActivity.DEVICE_NAME, device.getName());
            msg.setData(bundle);
            mHandler.sendMessage(msg);
        }

        setState(STATE_CONNECTED);
    }

    public synchronized void stop() {
        if (D) LogUtil.d(TAG, "stop");
        if (mConnectThread != null) {
            mConnectThread.cancel();
            mConnectThread = null;
        }
        if (mConnectedThread != null) {
            mConnectedThread.cancel();
            mConnectedThread = null;
        }
        if (mAcceptThread != null) {
            mAcceptThread.cancel();
            mAcceptThread = null;
        }
        setState(STATE_NONE);
    }

    private void sendString(String s) {
        write(s.getBytes());
    }

    /**
     * @param hexString normal String.
     *                  sendString s in hex in fact.
     */
    public void sendHex(String hexString) {
        write(StringUtil.hexString2Bytes(hexString));
    }

    /**
     * transform s to hex string, then send it .
     *
     * @param s origin string.
     */
    private void sendStringInHex(String s) {
        sendString(StringUtil.string2HexString(s));
    }


    private void write(byte[] out) {

        ConnectedThread r;

        synchronized (this) {
            if (mState != STATE_CONNECTED) {
                LogUtil.sendCmdResult(TAG, out, false);
                if (mHandler != null) {
                    connectionLost();
                }
                return;
            }
            r = mConnectedThread;
        }

        r.write(out);
    }


    private void connectionFailed() {
        setState(STATE_LISTEN);
        if (mHandler != null) {
            Message msg = mHandler.obtainMessage(BtConstant.MESSAGE_TOAST);
            Bundle bundle = new Bundle();
            bundle.putString(BluetoothChatActivity.TOAST, "BT connect failed");
            msg.setData(bundle);
            mHandler.sendMessage(msg);
        }
    }


    private void connectionLost() {
        setState(STATE_LISTEN);
        if (mHandler != null) {
            Message msg = mHandler.obtainMessage(BtConstant.MESSAGE_TOAST);
            Bundle bundle = new Bundle();
            bundle.putString(BluetoothChatActivity.TOAST, "蓝牙已经断开");
            msg.setData(bundle);
            mHandler.sendMessage(msg);
        }
        disconnect();
    }


    private class AcceptThread extends Thread {

        private final BluetoothServerSocket mmServerSocket;

        public AcceptThread() {
            BluetoothServerSocket tmp = null;

            try {
                tmp = mAdapter.listenUsingRfcommWithServiceRecord(NAME, MY_UUID);
            } catch (IOException e) {
                LogUtil.e(TAG, "listen() failed", e);
            }
            mmServerSocket = tmp;
        }

        public void run() {
            if (D) LogUtil.d(TAG, "BEGIN mAcceptThread" + this);
            setName("AcceptThread");
            BluetoothSocket socket = null;


            while (mState != STATE_CONNECTED) {
                try {

                    socket = mmServerSocket.accept();
                } catch (IOException e) {
                    LogUtil.e(TAG, "accept() failed", e);
                    break;
                }

                if (socket != null) {
                    synchronized (BluetoothHelper.this) {
                        switch (mState) {
                            case STATE_LISTEN:
                            case STATE_CONNECTING:

                                connected(socket, socket.getRemoteDevice());
                                break;
                            case STATE_NONE:
                            case STATE_CONNECTED:

                                try {
                                    socket.close();
                                } catch (IOException e) {
                                    LogUtil.e(TAG, "Could not close unwanted socket", e);
                                }
                                break;
                        }
                    }
                }
            }
            if (D) LogUtil.i(TAG, "END mAcceptThread");
        }

        public void cancel() {
            if (D) LogUtil.d(TAG, "cancel " + this);
            try {
                mmServerSocket.close();
            } catch (IOException e) {
                LogUtil.e(TAG, "close() of server failed", e);
            }
        }
    }


    private class ConnectThread extends Thread {
        private final BluetoothSocket mmSocket;
//        private final BluetoothDevice mCurrentDevice;

        public ConnectThread(BluetoothDevice device) {
            mCurrentDevice = device;
            BluetoothSocket tmp = null;


            try {
                tmp = device.createRfcommSocketToServiceRecord(MY_UUID);
            } catch (IOException e) {
                LogUtil.e(TAG, "create() failed", e);
            }
            mmSocket = tmp;
        }

        public void run() {
            LogUtil.i(TAG, "BEGIN mConnectThread");
            setName("ConnectThread");


            mAdapter.cancelDiscovery();


            try {
                mmSocket.connect();
            } catch (IOException e) {
                connectionFailed();

                try {
                    mmSocket.close();
                } catch (IOException e2) {
                    LogUtil.e(TAG, "unable to close() socket during connection failure", e2);
                }

                BluetoothHelper.this.start();
                return;
            }

            synchronized (BluetoothHelper.this) {
                mConnectThread = null;
            }
            connected(mmSocket, mCurrentDevice);
        }

        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
                LogUtil.e(TAG, "close() of connect socket failed", e);
            }
        }
    }


    private class ConnectedThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;

        public ConnectedThread(BluetoothSocket socket) {
            LogUtil.d(TAG, "create ConnectedThread");
            mmSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;


            try {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) {
                LogUtil.e(TAG, "temp sockets not created", e);
            }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }

        public void run() {
            LogUtil.i(TAG, "BEGIN mConnectedThread");
            byte[] preByte = new byte[1];
            int byteCount;

            while (true) {
                try {
                    byteCount = mmInStream.read(preByte);
                    LogUtil.v(TAG, "origin head: " + StringUtil.bytes2HexString(preByte));
                    if (!(byteCount == 1 && CommandUtil.COLLECTOR_PRE_CODE.equals(StringUtil.bytes2HexString(preByte)))) {
                        continue;
                    }
                    byte[] lengthByte = new byte[1];
                    mmInStream.read(lengthByte);
                    int messageLength = StringUtil.byte2int(lengthByte[0]);// it has a end code.
                    int readCnt = 0;
                    byte[] readbuff = new byte[messageLength];
                    int remain = messageLength;
                    long curTime = SystemClock.currentThreadTimeMillis();
                    while (remain > 0) {
                        readCnt += mmInStream.read(readbuff, messageLength
                                - remain, remain);
                        remain = messageLength - readCnt;
                        //set 10ms as timeout.
                        if (SystemClock.currentThreadTimeMillis() - curTime > 10) {//invode wrong code cause blocking.
                            break;
                        }
                    }
                    byte[] endByte = new byte[1];
                    mmInStream.read(endByte, 0, 1);
                    byte[] wholeByte = CommandUtil.getWholeCmd(preByte, lengthByte, readbuff, endByte);
                    byte chechSum = CommandUtil.getChecksum(StringUtil.byteMerger(lengthByte, readbuff));

                    LogUtil.d(TAG, "checksum: " + CommandUtil.getChecksum(StringUtil.byteMerger(lengthByte, readbuff)) + " ,end: " + StringUtil.byte2int(endByte[0]));
                    LogUtil.d(TAG, "receive origin msg: " + StringUtil.bytes2HexString(wholeByte));
                    if (mHandler != null) {
                        if (chechSum == 0) {
                            mHandler.obtainMessage(BtConstant.MESSAGE_RECEIVE_SUCCESS, wholeByte.length, -1, wholeByte)
                                    .sendToTarget();
                        } else {
                            mHandler.obtainMessage(BtConstant.MESSAGE_RECEIVE_FAILED, wholeByte.length, -1, wholeByte)
                                    .sendToTarget();
                        }
                    } else {
                        LogUtil.d(TAG, "ConnectedThread.run.mHandler is null");
                    }
                } catch (IOException e) {
                    LogUtil.e(TAG, "read exception: ", e);
                    connectionLost();
                    break;
                }
            }
        }

        public void write(byte[] buffer) {
            try {
                mmOutStream.write(buffer);
                LogUtil.sendCmdResult(TAG, buffer, true);
//                LogUtil.d(TAG, "send origin String: " + StringUtil.bytes2String(buffer) + " , hex String: " + StringUtil.bytes2HexString(buffer));
                if (mHandler != null) {
                    mHandler.obtainMessage(BtConstant.MESSAGE_WRITE, -1, -1, buffer)
                            .sendToTarget();
                }
            } catch (IOException e) {
                connectionLost();
                LogUtil.sendCmdResult(TAG, buffer, false);
                LogUtil.e(TAG, "Exception during write", e);
            }
        }

        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
                LogUtil.e(TAG, "close() of connect socket failed", e);
            }
        }


        public void disconnect() {
            try {
                mmOutStream.close();
                mmInStream.close();
                mmSocket.close();
            } catch (IOException e) {
                LogUtil.e(TAG, "close() of connect socket failed", e);
            }
        }
    }

    public boolean isConnected() {
        return (mConnectedThread != null) && (mConnectedThread.mmSocket != null) && (mConnectedThread.mmSocket.isConnected());
    }

    public void disconnect() {
        LogUtil.d(TAG, "disconnect is invoked.");
        if (mConnectedThread != null) {
            mConnectedThread.disconnect();
        }
    }


}
