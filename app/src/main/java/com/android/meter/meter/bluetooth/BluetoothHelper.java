package com.android.meter.meter.bluetooth;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.util.Log;

import com.android.meter.meter.util.CommandUtil;
import com.android.meter.meter.util.IMsgListener;
import com.android.meter.meter.util.LogUtil;
import com.android.meter.meter.util.StringUtil;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

public class BluetoothHelper {

    private static final String TAG = LogUtil.COMMON_TAG + "BluetoothHelper";
    private static final boolean D = true;
    private static final String NAME = "BluetoothChatActivity";
    private static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private final BluetoothAdapter mAdapter;

    private static BluetoothHelper mBluetoothHelper;
    private Handler mHandler;
    private AcceptThread mAcceptThread;
    private ConnectThread mConnectThread;
    private ConnectedThread mConnectedThread;
    private int mState;
    public static final int STATE_NONE = 0;
    public static final int STATE_LISTEN = 1;
    public static final int STATE_CONNECTING = 2;
    public static final int STATE_CONNECTED = 3;

    private IMsgListener mIMsgListener;

    public void setIMsgListener(IMsgListener iMsgListener) {
        mIMsgListener = iMsgListener;
    }

    private BluetoothHelper(Context context) {
        this(context, null);
    }

    public static BluetoothHelper getBluetoothChatService(Context context) {
        if (mBluetoothHelper == null) {
            mBluetoothHelper = new BluetoothHelper(context);
        }
        return mBluetoothHelper;
    }

    private BluetoothHelper(Context context, Handler handler) {
        mAdapter = BluetoothAdapter.getDefaultAdapter();
        mState = STATE_NONE;
        setmHandler(handler);
    }

    public void setmHandler(Handler handler) {
        if (mHandler != null) {
            mHandler.removeCallbacksAndMessages(null);
            mHandler = null;
        }
        mHandler = handler;
    }

    public BluetoothAdapter getBluetoothAdapter() {
        return mAdapter;
    }

    /**
     * Set the current state of the chat connection
     *
     * @param state An integer defining the current connection state
     */
    private synchronized void setState(int state) {
        if (D) Log.d(TAG, "setState() " + mState + " -> " + state);
        mState = state;

        // Give the new state to the Handler so the UI Activity can update
        mHandler.obtainMessage(BtConstant.MESSAGE_STATE_CHANGE, state, -1).sendToTarget();
    }

    public String getStateString() {
        switch (getState()) {
            case STATE_NONE:
                return "no device";
            case STATE_CONNECTING:
                return "connecting";
            case STATE_CONNECTED:
                return "connected";
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
        if (D) Log.d(TAG, "start");

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

    /**
     * Start the ConnectThread to initiate a connection to a remote device.
     *
     * @param device The BluetoothDevice to connect
     */
    public synchronized void connect(BluetoothDevice device) {
        if (D) Log.d(TAG, "connect to: " + device);

        // Cancel any thread attempting to make a connection
        if (mState == STATE_CONNECTING) {
            if (mConnectThread != null) {
                mConnectThread.cancel();
                mConnectThread = null;
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


    public synchronized void connected(BluetoothSocket socket, BluetoothDevice device) {
        if (D) Log.d(TAG, "connected");


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


        Message msg = mHandler.obtainMessage(BtConstant.MESSAGE_DEVICE_NAME);
        Bundle bundle = new Bundle();
        bundle.putString(BluetoothChatActivity.DEVICE_NAME, device.getName());
        msg.setData(bundle);
        mHandler.sendMessage(msg);

        setState(STATE_CONNECTED);
    }

    public synchronized void stop() {
        if (D) Log.d(TAG, "stop");
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
            if (mState != STATE_CONNECTED) return;
            r = mConnectedThread;
        }

        r.write(out);
    }


    private void connectionFailed() {
        setState(STATE_LISTEN);
        Message msg = mHandler.obtainMessage(BtConstant.MESSAGE_TOAST);
        Bundle bundle = new Bundle();
        bundle.putString(BluetoothChatActivity.TOAST, "Unable to connect device");
        msg.setData(bundle);
        mHandler.sendMessage(msg);
    }


    private void connectionLost() {
        setState(STATE_LISTEN);


        Message msg = mHandler.obtainMessage(BtConstant.MESSAGE_TOAST);
        Bundle bundle = new Bundle();
        bundle.putString(BluetoothChatActivity.TOAST, "Device connection was lost");
        msg.setData(bundle);
        mHandler.sendMessage(msg);
    }


    private class AcceptThread extends Thread {

        private final BluetoothServerSocket mmServerSocket;

        public AcceptThread() {
            BluetoothServerSocket tmp = null;

            try {
                tmp = mAdapter.listenUsingRfcommWithServiceRecord(NAME, MY_UUID);
            } catch (IOException e) {
                Log.e(TAG, "listen() failed", e);
            }
            mmServerSocket = tmp;
        }

        public void run() {
            if (D) Log.d(TAG, "BEGIN mAcceptThread" + this);
            setName("AcceptThread");
            BluetoothSocket socket = null;


            while (mState != STATE_CONNECTED) {
                try {

                    socket = mmServerSocket.accept();
                } catch (IOException e) {
                    Log.e(TAG, "accept() failed", e);
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
                                    Log.e(TAG, "Could not close unwanted socket", e);
                                }
                                break;
                        }
                    }
                }
            }
            if (D) Log.i(TAG, "END mAcceptThread");
        }

        public void cancel() {
            if (D) Log.d(TAG, "cancel " + this);
            try {
                mmServerSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "close() of server failed", e);
            }
        }
    }


    private class ConnectThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final BluetoothDevice mmDevice;

        public ConnectThread(BluetoothDevice device) {
            mmDevice = device;
            BluetoothSocket tmp = null;


            try {
                tmp = device.createRfcommSocketToServiceRecord(MY_UUID);
            } catch (IOException e) {
                Log.e(TAG, "create() failed", e);
            }
            mmSocket = tmp;
        }

        public void run() {
            Log.i(TAG, "BEGIN mConnectThread");
            setName("ConnectThread");


            mAdapter.cancelDiscovery();


            try {

                mmSocket.connect();
            } catch (IOException e) {
                connectionFailed();

                try {
                    mmSocket.close();
                } catch (IOException e2) {
                    Log.e(TAG, "unable to close() socket during connection failure", e2);
                }

                BluetoothHelper.this.start();
                return;
            }

            synchronized (BluetoothHelper.this) {
                mConnectThread = null;
            }
            connected(mmSocket, mmDevice);
        }

        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "close() of connect socket failed", e);
            }
        }
    }


    private class ConnectedThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;

        public ConnectedThread(BluetoothSocket socket) {
            Log.d(TAG, "create ConnectedThread");
            mmSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;


            try {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) {
                Log.e(TAG, "temp sockets not created", e);
            }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }

        public void run() {
            Log.i(TAG, "BEGIN mConnectedThread");
            byte[] preByte = new byte[1];
            int byteCount;

            while (true) {
                try {
                    byteCount = mmInStream.read(preByte);
                    if (!(byteCount == 1 && CommandUtil.PRE_CODE.equals(StringUtil.bytes2HexString(preByte)))) {
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
                    byte[] wholeByte = CommandUtil.getWholeCmd(preByte, lengthByte, readbuff,endByte);
                    Log.d(TAG, "checksum: " + CommandUtil.getChecksum(StringUtil.byteMerger(lengthByte, readbuff)) +" ,end: " + StringUtil.byte2int(endByte[0]));
                    Log.d(TAG, "origin: " + StringUtil.bytes2HexString(wholeByte));
                    mHandler.obtainMessage(BtConstant.MESSAGE_READ, wholeByte.length, -1, wholeByte)
                            .sendToTarget();
//                    if (mIMsgListener != null) {
//                        Log.d(TAG, "thread: " + Thread.currentThread().getId());
//                        mIMsgListener.received(BtConstant.MESSAGE_READ, StringUtil.bytes2HexString(wholeByte));
//                    }

                } catch (IOException e) {
                    Log.e(TAG, "read exception: ", e);
                    connectionLost();
                    break;
                }
            }
        }

        public void write(byte[] buffer) {
            try {
                mmOutStream.write(buffer);
                mHandler.obtainMessage(BtConstant.MESSAGE_WRITE, -1, -1, buffer)
                        .sendToTarget();
            } catch (IOException e) {
                Log.e(TAG, "Exception during write", e);
            }
        }

        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "close() of connect socket failed", e);
            }
        }
    }
}
