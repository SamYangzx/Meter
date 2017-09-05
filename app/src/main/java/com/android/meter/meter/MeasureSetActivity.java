package com.android.meter.meter;

import android.app.AlertDialog;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.android.meter.meter.bluetooth.BluetoothHelper;
import com.android.meter.meter.bluetooth.BtConstant;
import com.android.meter.meter.bluetooth.DeviceListActivity;
import com.android.meter.meter.general_ui.CustomDialog;
import com.android.meter.meter.general_ui.CustomToastDialog;
import com.android.meter.meter.general_ui.NetworkDialog;
import com.android.meter.meter.http.HTTPConstant;
import com.android.meter.meter.http.IHttpListener;
import com.android.meter.meter.http.SocketControl;
import com.android.meter.meter.numberpicker.NumberPickerView;
import com.android.meter.meter.util.CommandUtil;
import com.android.meter.meter.util.Constant;
import com.android.meter.meter.util.IMsgListener;
import com.android.meter.meter.util.LogUtil;
import com.android.meter.meter.util.StringUtil;
import com.android.meter.meter.util.ToastUtil;

import static com.android.meter.meter.bluetooth.BluetoothChatActivity.TOAST;


public class MeasureSetActivity extends AppCompatActivity {
    private static final String TAG = LogUtil.COMMON_TAG + MeasureSetActivity.class.getSimpleName();
    private static final int REQUEST_CONNECT_DEVICE = 1;

    private final static String mUnitArrays[][] = {
            {"N", "kN", "cN", "mN", "kgf", "daN", "Lbf"},
            {"N•m", "mN•m", "cN•m", "dN•m", "kgf•m", "kgf•cm", "Lbf•in", "Lbf•ft", "ozf•in",},
            {"MPa", "kPa", "hPa", "Pa", "kgf/cm²", "bar", "mbar", "psi", "mmWC", "inWC", "mmHg",},
            {"g", "mg", "kg", "t", "N", "cN", "mN"},
            {"Pa", "mbar", "torr", "hPa"},
            {"Pa·m³/s", "mbar·l/s", "g/a",},
            {"km", "m", "dm", "cm", "mm", "μm", "nm", "inch", "ft"},
            {"Ω", "kΩ", "MΩ", "GΩ",},
            {"F", "mF", "μF", "nF", "pF"},
            {"H", "mH", "μH"},
            {"V", "kV", "mV", "μV"},
            {"A", "kA", "mA", "μA"},
            {"W", "kW", "mW"},
            {"m3/h", "m3/min", "m3/s", "L/h", "L/min", "L/s", "mL/h", "mL/min", "mL/s", "ft3/h", "ft3/min", "ft3/s", "UKgal/s", "U.Sgal/s", "USbbl/s"},
            {"℃", "K", "οF", "οRa", "οR"},
    };

    private Context mContext;
    private String[] mStepArray;
    private String[] mTapArray;
    private String[] mCountArray;

    private NumberPickerView mStepPicker;
    private NumberPickerView mTapPicker;
    private NumberPickerView mCountPicker;

    private Button mStartBtn;
    private Button mEndBtn;
    private Spinner mMeasureUnitSp;
    private ArrayAdapter<String> mMeasureAdapter;
    private Spinner mSampleUnitSp;
    private ArrayAdapter<String> mSampleAdapter;

    //    private ActionBar mActionBar;
    private Toolbar mToolbar;
    private TextView mCustomerTitle;

    private NetworkDialog mNetworkDialog;

    private int mUnitIndex = 0;
    private String mMeasureUnit;
    private String mSampleUnit;
    private String mTap;
    private float mStep;
    private int mCount;
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
//            Log.d(TAG, "msg: " + msg.what)
            switch (msg.what) {
                case BtConstant.MESSAGE_STATE_CHANGE:
                    Log.i(TAG, "MESSAGE_STATE_CHANGE: " + msg.arg1);
                    switch (msg.arg1) {
                        case BluetoothHelper.STATE_CONNECTED:
                            setTitles("connected");
//                            ToastUtil.showToast(mContext, "connected");
                            break;
                        case BluetoothHelper.STATE_CONNECTING:
                            setTitles(R.string.title_connecting);
//                            ToastUtil.showToast(mContext, R.string.title_connecting);

                            break;
                        case BluetoothHelper.STATE_LISTEN:
                        case BluetoothHelper.STATE_NONE:
                            setTitles(R.string.title_not_connected);
//                            ToastUtil.showToast(mContext, R.string.title_not_connected);
                            break;
                        default:
                            break;
                    }
                    break;
                case BtConstant.MESSAGE_WRITE:
                    byte[] writeBuf = (byte[]) msg.obj;
                    // construct a string from the buffer
                    String writeMessage = StringUtil.bytes2HexString(writeBuf);
//                    mConversationArrayAdapter.add("Me:  " + writeMessage);
                    ToastUtil.showToast(mContext, "sendString : " + writeMessage);
                    break;
                case BtConstant.MESSAGE_READ:
                    byte[] readBuf = (byte[]) msg.obj;
                    String readMessage = StringUtil.bytes2HexString(readBuf);
                    ToastUtil.showToast(mContext, "Receive: " + readMessage);

                    break;
                case BtConstant.MESSAGE_DEVICE_NAME:
                    // save the connected device's name
//                    mConnectedDeviceName = msg.getData().getString(DEVICE_NAME);
//                    Toast.makeText(getApplicationContext(), "Connected to "
//                            + mConnectedDeviceName, Toast.LENGTH_SHORT).show();
                    break;
                case BtConstant.MESSAGE_TOAST:
                    Toast.makeText(getApplicationContext(), msg.getData().getString(TOAST),
                            Toast.LENGTH_SHORT).show();
                    break;
                case HTTPConstant.CONNECT_SUCCESS:
                    ToastUtil.showToast(mContext, "Http connect success");
                    mNetworkDialog.dismiss();
                    break;
                case HTTPConstant.CONNECT_FAIL:
                    ToastUtil.showToast(mContext, "Http connect fail");
                    break;
                case HTTPConstant.SEND_FAIL:
                    ToastUtil.showToast(mContext, "Http send msg fail!!");
                    break;
                default:
                    break;

            }
        }
    };

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        if (getActionBar() != null) {
//            getActionBar().setDisplayShowHomeEnabled(false);
////            getActionBar().setBackgroundDrawable(getResources().getDrawable(R.drawable.title_background));
//        }
        setContentView(R.layout.activity_measure_set);
        mContext = this;
        initData();
        initView();

        mStepPicker.postDelayed(new Runnable() {
            @Override
            public void run() {
                Log.d(TAG, "mStepArray.length / 2: " + mStepArray.length / 2);
                mStepPicker.setPickedIndexRelativeToRaw(mStepArray.length / 2);
                mStep = Integer.valueOf(mStepArray[mStepArray.length / 2]);

            }
        }, Constant.DELAY_REFRESH_TIME);

    }


    @Override
    protected void onRestart() {
        super.onRestart();
        if (isNeedResetHandler) {
            BluetoothHelper.getBluetoothChatService(mContext).setmHandler(mHandler);
            isNeedResetHandler = false;
        }
    }

    private boolean isNeedResetHandler = false;

    private void setTitles(int strId) {
//        if (mActionBar == null) {
//            mActionBar = getActionBar();
//        }
//        if (mActionBar != null) {
//            mActionBar.setTitle(getResources().getString(R.string.measure_title) + "/" + getResources().getString(strId));
//        }
        mCustomerTitle.setText(getResources().getString(R.string.measure_title) + "/" + getResources().getString(strId));
    }

    private void setTitles(String str) {
//        if (mActionBar == null) {
//            mActionBar = getActionBar();
//        }
//        if (mActionBar != null) {
//            mActionBar.setTitle(getResources().getString(R.string.measure_title) + "/" + str);
//        }
        mCustomerTitle.setText(getResources().getString(R.string.measure_title) + "/" + str);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.measure_settings_menu, menu);
        return true;
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
                if (mNetworkDialog == null) {
                    mNetworkDialog = new NetworkDialog(mContext);
                }
                mNetworkDialog.setYesOnclickListener(new NetworkDialog.onEnterclickListener() {
                    @Override
                    public void onYesClick() {
                        ToastUtil.showToast(mContext, "connect...");
                        connectServer(mNetworkDialog.getServer(), mNetworkDialog.getIp());

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
            case R.id.unit_settings:
                getUnitDialog().show();
                return true;

            case R.id.debug:
                final CustomDialog mDebugDialog = new CustomDialog(mContext);
                mDebugDialog.setMessage(CommandUtil.TEST_HEX_CMD);
                mDebugDialog.setYesOnclickListener(new CustomDialog.onEnterclickListener() {
                    @Override
                    public void onYesClick() {
                        BluetoothHelper.getBluetoothChatService(mContext).sendHex(mDebugDialog.getMessageStr());
                        SocketControl.getInstance().sendMsg(mDebugDialog.getMessageStr());
                    }
                });
                mDebugDialog.setNoOnclickListener(new CustomDialog.onCancelclickListener() {
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
                mDialog.setNoOnclickListener(new CustomToastDialog.onCancelclickListener() {
                    @Override
                    public void onNoClick() {
                        mDialog.cancel();
                    }
                });
                mDialog.setYesOnclickListener(new CustomToastDialog.onEnterclickListener() {
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
    public void onBackPressed() {
        super.onBackPressed();
        BluetoothHelper.getBluetoothChatService(mContext).disconnect();
        SocketControl.getInstance().disconnect();
    }

    @Override
    protected void onDestroy() {
        Log.d(TAG, "onDestroy is invoked...");
        BluetoothHelper.getBluetoothChatService(mContext).setmHandler(null);
//        BluetoothHelper.getBluetoothChatService(mContext).setIMsgListener(null);
        SocketControl.getInstance().setListener(null);
        mHandler.removeCallbacksAndMessages(null);
        mHandler = null;
        super.onDestroy();
    }

    private void initData() {
        mStepArray = getResources().getStringArray(R.array.step_array);
        mTapArray = getResources().getStringArray(R.array.tap_array);
        mCountArray = getResources().getStringArray(R.array.jinhui_array);

        mStep = Float.parseFloat(mStepArray[0]);
        mCount = Integer.valueOf(mCountArray[0]);

        BluetoothHelper.getBluetoothChatService(mContext).setmHandler(mHandler);
    }

    private void initBtStatus() {
        setTitles(BluetoothHelper.getBluetoothChatService(mContext).getStateString());
    }

    private void initView() {
        mToolbar = (Toolbar) findViewById(R.id.measure_set_toolbar);
        mCustomerTitle = (TextView) findViewById(R.id.customer_title);
        setSupportActionBar(mToolbar);

        initBtStatus();

        mMeasureUnitSp = (Spinner) findViewById(R.id.measure_unit_spinner);
        mMeasureUnitSp.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Log.d(TAG, "position: " + position + ", id: " + id);
                mMeasureUnit = mUnitArrays[mUnitIndex][position];
//                BluetoothHelper.getBluetoothChatService(mContext).sendHex(StringUtil.string2HexString(mUnitArrays[mUnitIndex][position]));
//                BluetoothHelper.getBluetoothChatService(mContext).sendHex(CommandUtil.TEST_HEX_CMD);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        mSampleUnitSp = (Spinner) findViewById(R.id.sample_unit_spinner);
        mSampleUnitSp.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                mSampleUnit = mUnitArrays[mUnitIndex][position];
//                BluetoothHelper.getBluetoothChatService(mContext).sendHex(StringUtil.string2HexString(mUnitArrays[mUnitIndex][position]));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        updateSpinnerArray(0);

        mStepPicker = (NumberPickerView) findViewById(R.id.step_picker);


        mTapPicker = (NumberPickerView) findViewById(R.id.tap_picker);
        mCountPicker = (NumberPickerView) findViewById(R.id.jinhui_picker);

        mStepPicker.refreshByNewDisplayedValues(mStepArray);
        mStepPicker.setOnValueChangedListener(new NumberPickerView.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPickerView picker, int oldVal, int newVal) {
                Log.d(TAG, "oldVal: " + oldVal + ", newVal: " + newVal + ", Value: " + mStepArray[newVal]);
                mStep = Float.parseFloat(mStepArray[newVal]);
//                BluetoothHelper.getBluetoothChatService(mContext).sendString(mStepArray[newVal] + " unit");

            }
        });
        mTapPicker.refreshByNewDisplayedValues(mTapArray);
        mTapPicker.setOnValueChangedListener(new NumberPickerView.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPickerView picker, int oldVal, int newVal) {
                mTap = mTapArray[newVal];
            }
        });

        mCountPicker.refreshByNewDisplayedValues(mCountArray);
        mCountPicker.setOnValueChangedListener(new NumberPickerView.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPickerView picker, int oldVal, int newVal) {
                Log.d(TAG, "oldVal: " + oldVal + ", newVal: " + newVal + ", Value: " + mCountArray[newVal]);
                mCount = Integer.valueOf(mCountArray[newVal]);
            }
        });

        mStartBtn = (Button) findViewById(R.id.start_btn);
        mEndBtn = (Button) findViewById(R.id.end_btn);
        mStartBtn.setOnClickListener(mListener);
        mEndBtn.setOnClickListener(mListener);
    }


    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d(TAG, "onActivityResult " + resultCode);
        switch (requestCode) {
            case REQUEST_CONNECT_DEVICE:
                // When DeviceListActivity returns with a device to connect
                if (resultCode == AppCompatActivity.RESULT_OK) {
                    // Get the device MAC address
                    String address = data.getExtras()
                            .getString(DeviceListActivity.EXTRA_DEVICE_ADDRESS);
                    // Get the BLuetoothDevice object
                    BluetoothDevice device = BluetoothHelper.getBluetoothChatService(mContext).getBluetoothAdapter().getRemoteDevice(address);
                    // Attempt to connect to the device
                    BluetoothHelper.getBluetoothChatService(mContext).connect(device);
//                    BluetoothHelper.getBluetoothChatService(mContext).setIMsgListener(mIBtMsgListener);
                }
                break;
//            case REQUEST_ENABLE_BT:
//                // When the request to enable Bluetooth returns
//                if (resultCode == Activity.RESULT_OK) {
//                    // Bluetooth is now enabled, so set up a chat session
//                    setupChat();
//                } else {
//                    // User did not enable Bluetooth or an error occured
//                    Log.d(TAG, "BT not enabled");
//                    Toast.makeText(this, R.string.bt_not_enabled_leaving, Toast.LENGTH_SHORT).show();
//                    finish();
//                }
//                break;
            default:
                break;
        }
    }


    private View.OnClickListener mListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.start_btn:
                    BluetoothHelper.getBluetoothChatService(mContext).sendHex(CommandUtil.getStartCmd());

                    Intent intent = new Intent();
                    intent.putExtra(MeasureActivity.EXTRA_MEASURE_UNIT, mMeasureUnit);
                    intent.putExtra(MeasureActivity.EXTRA_SAMPLE_UNIT, mSampleUnit);
                    intent.putExtra(MeasureActivity.EXTRA_STEP, mStep);
                    intent.putExtra(MeasureActivity.EXTRA_TAP, mTap);
                    Log.d(TAG, "sendString mStep: " + mStep);
                    intent.putExtra(MeasureActivity.EXTRA_COUNT, mCount);
                    intent.setClass(mContext, MeasureActivity.class);
                    isNeedResetHandler = true;
                    startActivity(intent);
                    break;
                case R.id.end_btn:
                    BluetoothHelper.getBluetoothChatService(mContext).sendHex(CommandUtil.getStopCmd());
//                    test();
                    break;
                default:
                    break;
            }
        }
    };


    private AlertDialog mUnitDialog;

    private AlertDialog getUnitDialog() {
        if (mUnitDialog == null) {
            final String[] items = getResources().getStringArray(R.array.unit_type_array);
            mUnitDialog = new AlertDialog.Builder(this)
                    .setItems(items, new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            updateSpinnerArray(which);
                        }
                    }).create();
        }
        return mUnitDialog;
    }


    private void updateSpinnerArray(int position) {
        mUnitIndex = position;
        mMeasureAdapter = new ArrayAdapter<String>(mContext, android.R.layout.simple_spinner_item, mUnitArrays[position]);
//        mMeasureAdapter = new ArrayAdapter<String>(mContext, R.layout.custom_spiner_text_item, mUnitArrays[position]);
//        mMeasureAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mMeasureAdapter.setDropDownViewResource(R.layout.custom_spinner_dropdown_itrm);
        mMeasureUnitSp.setAdapter(mMeasureAdapter);

        mSampleAdapter = new ArrayAdapter<String>(mContext, android.R.layout.simple_spinner_item, mUnitArrays[position]);
//        mSampleAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mSampleAdapter.setDropDownViewResource(R.layout.custom_spinner_dropdown_itrm);
        mSampleUnitSp.setAdapter(mSampleAdapter);
    }

    private boolean isContinue = true;
//    SocketControl mClient;

    private void test() {
//        mClient = new SocketControl(HTTPConstant.DEFAULT_SERVER, HTTPConstant.DEFAULT_PORT, mHttpListener);
        SocketControl.getInstance().sendMsg("AA12E4016368616E656C3120303030302E304EBBCC");
    }

    private void connectServer(String server, int ip) {
        SocketControl.getInstance().setListener(mHttpListener);
        SocketControl.getInstance().connect(server, ip);
    }

    private void sendTest(String data) {
        SocketControl.getInstance().sendMsg(data);
    }


    /*****This is for test**/
    private IMsgListener mIBtMsgListener = new IMsgListener() {
        @Override
        public void received(int state, final String msg) {
            sendTest(msg);
        }
    };


    private IHttpListener mHttpListener = new IHttpListener() {
        @Override
        public void onResult(int state, String data) {
            switch (state) {
                case HTTPConstant.CONNECT_SUCCESS:
                    Log.d(TAG, "connect success");
                    mHandler.sendEmptyMessage(HTTPConstant.CONNECT_SUCCESS);
                    break;
                case HTTPConstant.CONNECT_FAIL:
                    mHandler.sendEmptyMessage(HTTPConstant.CONNECT_FAIL);
                    Log.d(TAG, "connect fail");
                    break;
                case HTTPConstant.SEND_FAIL:
                    mHandler.sendEmptyMessage(HTTPConstant.SEND_FAIL);
                    break;
                default:
                    break;
            }

        }
    };

}
