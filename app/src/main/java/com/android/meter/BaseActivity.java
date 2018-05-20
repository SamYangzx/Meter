package com.android.meter;

import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.MenuItem;
import android.widget.TextView;

import com.android.meter.bluetooth.BluetoothHelper;
import com.android.meter.bluetooth.BtConstant;
import com.android.meter.bluetooth.DeviceListActivity;
import com.android.meter.general_ui.CustomEtDialog;
import com.android.meter.general_ui.CustomToastDialog;
import com.android.meter.general_ui.NetworkDialog;
import com.android.meter.http.IHttpListener;
import com.android.meter.http.SocketConstant;
import com.android.meter.http.SocketControl;
import com.android.meter.util.CommandUtil;
import com.android.meter.util.WeakHandler;
import com.android.meter.util.LogUtil;
import com.android.meter.util.SharedPreferenceUtils;
import com.android.meter.util.StringUtil;
import com.android.meter.util.ToastUtil;

import android.widget.Toast;
import android.os.Looper;

import java.util.ArrayList;
import java.util.List;

import static com.android.meter.bluetooth.BluetoothChatActivity.TOAST;
import static com.android.meter.http.SocketConstant.SAVE_IP;
import static com.android.meter.http.SocketConstant.SAVE_PORT;
import static com.android.meter.util.CommandUtil.UPLOCD_CMD_CODE;


/**
 * 基类，显示wifi, 蓝牙状态，添加接收数据后的接口。
 */
public class BaseActivity extends AppCompatActivity implements IHttpListener {
    private static final String TAG = LogUtil.COMMON_TAG + "BaseActivity";

    public static final int REQUEST_CONNECT_DEVICE = 1;

    String mBtStr = "", mWifiStr = "";
    TextView mTitle;
    Context mContext;
    Context mAppContext;

    NetworkDialog mNetworkDialog;
    AlertDialog mUnitDialog;

    static class MyHandler extends WeakHandler<BaseActivity> {
        MyHandler(BaseActivity activity) {
            super(Looper.getMainLooper(), activity);
        }

        @Override
        protected void handleMsg(Message msg, @NonNull BaseActivity activity) {
            LogUtil.d(TAG, "handleMsg.msg: " + msg.what);
            if (activity == null) {
                LogUtil.e(TAG, "activity == null!!!");
                return;
            }
            String data;
            switch (msg.what) {
                case BtConstant.MESSAGE_STATE_CHANGE:
                    LogUtil.i(TAG, "MESSAGE_STATE_CHANGE: " + msg.arg1);
                    switch (msg.arg1) {
                        case BluetoothHelper.STATE_CONNECTED:
                            activity.updateBtTitle(activity.getString(R.string.title_bt_connected));
                            break;
                        case BluetoothHelper.STATE_CONNECTING:
                            activity.updateBtTitle(activity.getString(R.string.title_bt_connecting));
                            break;
                        case BluetoothHelper.STATE_LISTEN:
                        case BluetoothHelper.STATE_NONE:
                            activity.updateBtTitle(activity.getString(R.string.title_not_bt_connected));
                            break;
                    }
                    break;
                case BtConstant.MESSAGE_WRITE:
                    byte[] writeBuf = (byte[]) msg.obj;
                    // construct a string from the buffer
                    String writeMessage = StringUtil.bytes2HexString(writeBuf);
                    ToastUtil.showToast(activity.mContext, "BT sendString: " + writeMessage, ToastUtil.DEBUG);
                    break;
                case BtConstant.MESSAGE_RECEIVE_SUCCESS:
//                    BluetoothHelper.getBluetoothHelper(mContext).sendHex(CommandUtil.CHECKSUM_SUCCESS_HEXCMD);
                    byte[] readBuf = (byte[]) msg.obj;
                    String readMessage = StringUtil.bytes2HexString(readBuf);
//                    mSampleTv.setText(readMessage);
                    ToastUtil.showToast(activity.mContext, "BT receive: " + readMessage, ToastUtil.DEBUG);
                    activity.handlerCmd(readMessage);
                    break;
                case BtConstant.MESSAGE_RECEIVE_FAILED:
                    BluetoothHelper.getBluetoothHelper(activity.mAppContext).sendHex(CommandUtil.CHECKSUM_FAILED_HEXCMD);
                    break;
                case BtConstant.MESSAGE_DEVICE_NAME:
                    break;
                case BtConstant.MESSAGE_TOAST:
                    Toast.makeText(activity.mContext, msg.getData().getString(TOAST),
                            Toast.LENGTH_SHORT).show();
                    break;
                case SocketConstant.SEND_FAIL:
                    ToastUtil.showToast(activity.mContext, "Socket发送数据失败，请检查网络连接是否正常!");
                    break;
                case SocketConstant.RECEIVE_SUCCESS:
                    data = (String) msg.obj;
                    activity.handlerCmd(data);
                    break;
                case SocketConstant.RECEIVE_CHECK_FAILED:
                    ToastUtil.showToast(activity.mContext, "电脑端未回复!");
                    break;
                case SocketConstant.COMPUTER_NOT_RESPONSE:
                    ToastUtil.showToast(activity.mContext, "上一条指令还未处理完，请等待！");
                    break;
                case SocketConstant.CONNECT_SUCCESS:
                    activity.updateWifiTitle(activity.getString(R.string.title_wifi_connected));
                    break;
                case SocketConstant.CONNECTING:
                    activity.updateWifiTitle(activity.getString(R.string.title_wifi_connecting));
                    break;
                case SocketConstant.CONNECT_FAIL:
                    activity.updateWifiTitle(activity.getString(R.string.title_wifi_not_connected));
                    break;
                default:
                    break;
            }
        }

    }

    MyHandler mHandler;

    private void initHandler() {
        mHandler = new MyHandler(this);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_base);
        LogUtil.i(TAG, "onCreate: " + getClass().getName());
        mContext = this;
        mAppContext = (MainApplication) getApplication();
        // 添加Activity到堆栈
        AtyContainer.getInstance().addActivity(this);

        initHandler();
    }

    @Override
    protected void onStart() {
        super.onStart();
        initSocketData();
    }

    private void initSocketData() {
        BluetoothHelper.getBluetoothHelper(mAppContext).enableBT();
        BluetoothHelper.getBluetoothHelper(mAppContext).setmHandler(mHandler);
        SocketControl.getInstance().setListener(this);
        if (!SocketControl.getInstance().isConneced()) {
            String server = (String) SharedPreferenceUtils.getParam(mAppContext, SocketConstant.SAVE_IP, SocketConstant.DEFAULT_SERVER);
            int port = (int) SharedPreferenceUtils.getParam(mAppContext, SocketConstant.SAVE_PORT, SocketConstant.DEFAULT_PORT);
            SocketControl.getInstance().connect(server, port);
        }
        if (!BluetoothHelper.getBluetoothHelper(mAppContext).isConnected()) {
            String bt = (String) SharedPreferenceUtils.getParam(mAppContext, BtConstant.SAVE_BT_ADDRESS, "");
            BluetoothHelper.getBluetoothHelper(mAppContext).connect(bt);
        }

        updateBtTitle(BluetoothHelper.getBluetoothHelper(mAppContext).getStateString());
        updateWifiTitle(SocketControl.getInstance().isConneced());
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.bluetooth_settings:
//                ToastUtil.showToast(mContext, R.string.bluetooth);
                Intent serverIntent = new Intent(mContext, DeviceListActivity.class);
                startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE);

                return true;
            case R.id.ip_settings:
//                if (mNetworkDialog == null) { //
                mNetworkDialog = new NetworkDialog(mContext);
//                }
                mNetworkDialog.setYesOnclickListener(new NetworkDialog.onEnterclickListener() {
                    @Override
                    public void onYesClick() {
//                        ToastUtil.showToast(mContext, "connect...");
                        connectServer(mNetworkDialog.getServer(), mNetworkDialog.getIp());
                        mNetworkDialog.dismiss();
                    }
                });
                mNetworkDialog.setNoOnclickListener(new NetworkDialog.onCancelclickListener() {
                    @Override
                    public void onNoClick() {
                        mNetworkDialog.cancel();
                    }
                });
                mNetworkDialog.show();
                return true;
//            case R.id.unit_settings:
//                getUnitDialog().show();
//                return true;

            case R.id.debug:
                final CustomEtDialog mDebugDialog = new CustomEtDialog(mContext);
                mDebugDialog.setMessage(CommandUtil.TEST_HEX_CMD);
                mDebugDialog.setYesOnclickListener(new CustomEtDialog.onEnterclickListener() {
                    @Override
                    public void onYesClick() {
//                        test();

                        BluetoothHelper.getBluetoothHelper(mAppContext).sendHex(mDebugDialog.getMessageStr());
                        SocketControl.getInstance().sendMsg(mDebugDialog.getMessageStr());
                    }
                });
                mDebugDialog.setNoOnclickListener(new CustomEtDialog.onCancelclickListener() {
                    @Override
                    public void onNoClick() {
                        mDebugDialog.cancel();
                    }
                });
                mDebugDialog.show();
                return true;
            case R.id.about:
                final CustomToastDialog mDialog = new CustomToastDialog(mContext);
                mDialog.setTitle(R.string.about);
                mDialog.setMessage(R.string.about_msg);
                mDialog.setNegativeButton(new CustomToastDialog.onCancelclickListener() {
                    @Override
                    public void onNoClick() {
                        mDialog.cancel();
                    }
                });
                mDialog.setPositiveButton(new CustomToastDialog.onEnterclickListener() {
                    @Override
                    public void onYesClick() {
                        mDialog.cancel();
                    }
                });
                mDialog.show();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        LogUtil.d(TAG, "onActivityResult " + resultCode);
        switch (requestCode) {
            case REQUEST_CONNECT_DEVICE:
                // When DeviceListActivity returns with a device to connect
                if (resultCode == AppCompatActivity.RESULT_OK) {
                    // Get the device MAC address
                    String address = data.getExtras()
                            .getString(DeviceListActivity.EXTRA_DEVICE_ADDRESS);
                    // Get the BLuetoothDevice object

                    BluetoothDevice device = BluetoothHelper.getBluetoothHelper(mAppContext).getBluetoothAdapter().getRemoteDevice(address);
                    if (device != null) {
                        // Attempt to connect to the device
                        BluetoothHelper.getBluetoothHelper(mAppContext).setmHandler(mHandler);
                        BluetoothHelper.getBluetoothHelper(mAppContext).connect(device);
                        SharedPreferenceUtils.setParam(mAppContext, BtConstant.SAVE_BT_ADDRESS, device.getAddress());
                    }
                }
                break;
            default:
                break;
        }
    }

    /**
     * 如果想要在activity中更新蓝牙和wifi状态，必须调用此函数设置title TextView.
     *
     * @param textView
     */
    void setTitleTv(TextView textView) {
        mTitle = textView;
    }

    void updateBtTitle(String bt) {
        if (mTitle == null) {
            return;
        }
        if (!TextUtils.isEmpty(bt)) {
            mBtStr = bt;
        }
        if (TextUtils.isEmpty(mWifiStr)) {
            mTitle.setText(mBtStr);
        } else {
            mTitle.setText(mBtStr + "/" + mWifiStr);
        }
    }

    void updateWifiTitle(String wifi) {
        if (mTitle == null) {
            return;
        }
        if (!TextUtils.isEmpty(wifi)) {
            mWifiStr = wifi;
        }
        mTitle.setText(mBtStr + "/" + mWifiStr);
    }

    void updateWifiTitle(boolean connect) {
        if (mTitle == null) {
            return;
        }
        if (connect) {
            mWifiStr = mContext.getString(R.string.title_wifi_connected);
        } else {
            mWifiStr = mContext.getString(R.string.title_wifi_not_connected);
        }
        mTitle.setText(mBtStr + "/" + mWifiStr);
    }

    public boolean checkPermission(@NonNull String permission) {
        return ActivityCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED;
    }

    /**
     * @param state
     * @param data
     */
    @Override
    public void onResult(int state, String data) {
        LogUtil.i(TAG, "onResult.state: " + state + " ,data: " + data);
        Message msg = new Message();
        msg.what = state;
        msg.obj = data;
        mHandler.sendMessage(msg);
    }

    @Override
    protected void onDestroy() {
        LogUtil.i(TAG, "onDestroy: " + getClass().getName());
        mHandler.removeCallbacksAndMessages(null);
        super.onDestroy();
        // 结束Activity&从栈中移除该Activity
        AtyContainer.getInstance().removeActivity(this);
    }

    public void connectServer(String server, int port) {
        SharedPreferenceUtils.setParam(mAppContext, SAVE_IP, server);
        SharedPreferenceUtils.setParam(mAppContext, SAVE_PORT, port);
        SocketControl.getInstance().connect(server, port);
    }

    /**
     * @param cmd      hex
     * @param socket
     * @param realSend
     */
    public void sendCmd(String cmd, boolean socket, boolean realSend) {
        if (socket) {
            SocketControl.getInstance().sendMsg(cmd, realSend);
        } else {
            BluetoothHelper.getBluetoothHelper(mAppContext).sendHex(cmd);
        }
    }

    /**
     * 处理从蓝牙和wifi端接收的消息，目前此处理蓝牙端接收的消息。
     *
     * @param hexCmd
     */
    public void handlerCmd(String hexCmd) {
        LogUtil.d(TAG, "handlerCmd: " + hexCmd);
        byte[] length = StringUtil.hexString2Bytes(hexCmd.substring(2, 4));
        int cmdLength = StringUtil.bytes2int(length);
        String cmdType = hexCmd.substring(4, 6);
        String content = "";
        /**
         * 0123456789
         * AA12E4016368616E656C3120303030302E304EBBCC
         *
         */
        if (6 + cmdLength * 2 <= hexCmd.length()) {
            content = hexCmd.substring(6, 6 + cmdLength * 2 - 4);
        } else {
            LogUtil.d(TAG, "cmd length is error");
            ToastUtil.showToast(mContext, "cmd length is error!");
        }
        LogUtil.d(TAG, "cmdLength: " + cmdLength + " ,cmdType: " + cmdType + " , content hex: " + content + " ,origin: " + StringUtil.hex2String(content));
        switch (cmdType) {
            case UPLOCD_CMD_CODE:
                String sampleValue = "";
                if (content.length() >= (cmdLength - 2) * 2) {
                    sampleValue = content.substring(18);
                    handleReceiveData(sampleValue);
                } else {
                    LogUtil.e(TAG, "Cmd error!!");
                }
                break;
            default:
                break;
        }
    }

    /**
     * 接收传感器端的数据后回调此方法。
     *
     * @param sampleValue 为解析的16进制数据
     */
    public void handleReceiveData(String sampleValue){};


}

class AtyContainer {

    private AtyContainer() {
    }

    private static AtyContainer instance = new AtyContainer();
    private static List<Activity> activityStack = new ArrayList<Activity>();

    public static AtyContainer getInstance() {
        return instance;
    }

    public void addActivity(Activity aty) {
        activityStack.add(aty);
    }

    public void removeActivity(Activity aty) {
        activityStack.remove(aty);
    }

    /**
     * 结束所有Activity
     */
    public void finishAllActivity() {
        for (int i = 0, size = activityStack.size(); i < size; i++) {
            if (null != activityStack.get(i)) {
                activityStack.get(i).finish();
            }
        }
        activityStack.clear();
        System.exit(0);
    }

}
