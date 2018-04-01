package com.android.meter;

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

import com.android.meter.bluetooth.BluetoothHelper;
import com.android.meter.bluetooth.BtConstant;
import com.android.meter.bluetooth.DeviceListActivity;
import com.android.meter.general_ui.CustomEtDialog;
import com.android.meter.general_ui.CustomToastDialog;
import com.android.meter.general_ui.NetworkDialog;
import com.android.meter.http.SocketConstant;
import com.android.meter.http.SocketControl;
import com.android.meter.numberpicker.NumberPickerView;
import com.android.meter.util.CommandUtil;
import com.android.meter.util.Constant;
import com.android.meter.util.LogUtil;
import com.android.meter.util.SharedPreferenceUtils;
import com.android.meter.util.StringUtil;
import com.android.meter.util.ToastUtil;

import static com.android.meter.bluetooth.BluetoothChatActivity.TOAST;
import static com.android.meter.http.SocketConstant.SAVE_IP;
import static com.android.meter.http.SocketConstant.SAVE_PORT;
import static com.android.meter.util.CommandUtil.getUnitData;


public class MeasureSetActivity extends BaseActivity {
    private static final String TAG = LogUtil.COMMON_TAG + MeasureSetActivity.class.getSimpleName();
    public static final String EXTRA_PHOTO = "photo";

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
            {"J"},
            {""},
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

    private String mPhotoName;

    private boolean firstStart = true;

    private int mTotalUnitIndex = 0;
    private int mUnitIndex = 0;
    private String mMeasurePointUnit;
    private String mSampleUnit;
    private String mTap;
    private float mStep;
    private int mCount;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LogUtil.v(TAG, "onCreate");
//        if (getActionBar() != null) {
//            getActionBar().setDisplayShowHomeEnabled(false);
////            getActionBar().setBackgroundDrawable(getResources().getDrawable(R.drawable.title_background));
//        }
        setContentView(R.layout.activity_measure_set);
        mContext = this;
        initTitle();
        initData();
        initView();

        mStepPicker.postDelayed(new Runnable() {
            @Override
            public void run() {
//                Log.d(TAG, "mStepArray.length / 2: " + mStepArray.length / 2);
                int step = (int) SharedPreferenceUtils.getParam(mContext, Constant.STEP, mStepArray.length / 2);
                mStepPicker.setPickedIndexRelativeToRaw(step);
                mStep = Float.valueOf(mStepArray[step]);

            }
        }, Constant.DELAY_REFRESH_TIME);
//        mTapPicker.postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                int tap = (int) SharedPreferenceUtils.getParam(mContext, Constant.TAP, mTapArray.length / 2);
//                mTapPicker.setPickedIndexRelativeToRaw(tap);
//                mTap = mTapArray[tap];
//            }
//        }, Constant.DELAY_REFRESH_TIME);
//        mCountPicker.postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                int count = (int) SharedPreferenceUtils.getParam(mContext, Constant.COUNT, mCountArray.length / 2);
//                mCountPicker.setPickedIndexRelativeToRaw(count);
//                mCount = Integer.valueOf(mCountArray[count]);
//            }
//        }, Constant.DELAY_REFRESH_TIME);

    }

    @Override
    protected void onStart() {
        LogUtil.v(TAG, "onStart");
        if (firstStart) {
            firstStart = false;
//            if (!TextUtils.isEmpty(mPhotoName)) {
//                SocketControl.getInstance().sendFile(FileUtil.getPicNumberFolder(true) + File.separator + mPhotoName);
//            }
        }
        updateBtTitle(BluetoothHelper.getBluetoothHelper(mContext).getStateString());
        updateWifiTitle(SocketControl.getInstance().isConneced());
//        setTitles(BluetoothHelper.getBluetoothHelper(mContext).getStateString());
        initViewData();
        super.onStart();
    }

//    @Override
//    protected void onRestart() {
//        LogUtil.v(TAG, "onRestart");
//        super.onRestart();
//        if (isNeedResetHandler) {
//            BluetoothHelper.getBluetoothHelper(mContext).setmHandler(mHandler);
//            isNeedResetHandler = false;
//        }
//
//        setTitles(BluetoothHelper.getBluetoothHelper(mContext).getStateString());
//    }

//    private boolean isNeedResetHandler = false;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.measure_settings_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.unit_settings:
                getUnitDialog().show();
                return true;

            case R.id.debug:
                final CustomEtDialog mDebugDialog = new CustomEtDialog(mContext);
                mDebugDialog.setMessage(CommandUtil.TEST_HEX_CMD);
                mDebugDialog.setYesOnclickListener(new CustomEtDialog.onEnterclickListener() {
                    @Override
                    public void onYesClick() {
//                        test();

                        BluetoothHelper.getBluetoothHelper(mContext).sendHex(mDebugDialog.getMessageStr());
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
    public void onBackPressed() {
//        exitDialog();
        super.onBackPressed();
//        BluetoothHelper.getBluetoothHelper(mContext).disconnect();
//        SocketControl.getInstance().disconnect();
    }

    @Override
    protected void onDestroy() {
        LogUtil.d(TAG, "onDestroy is invoked...");
//        BluetoothHelper.getBluetoothHelper(mContext).setmHandler(null);
//        BluetoothHelper.getBluetoothHelper(mContext).setIMsgListener(null);
//        SocketControl.getInstance().setListener(null);
        super.onDestroy();
    }

    private void initData() {
        mPhotoName = getIntent().getStringExtra(EXTRA_PHOTO);
        mStepArray = getResources().getStringArray(R.array.step_array);
        mTapArray = getResources().getStringArray(R.array.tap_array);
        mCountArray = getResources().getStringArray(R.array.jinhui_array);
        mTap = mTapArray[0];
        mCount = 1;
    }

    private void initViewData() {
//        mStep = Float.parseFloat(mStepArray[0]);
//        mTap = mTapArray[0];
//        mCount = Integer.valueOf(mCountArray[0]);
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                mTotalUnitIndex = (int) SharedPreferenceUtils.getParam(mContext, Constant.TOTAL_UNIT, 0);
                LogUtil.d(TAG, "mTotalUnitIndex: " + mTotalUnitIndex);
                final int measureIndex = (int) SharedPreferenceUtils.getParam(mContext, Constant.MEASURE_UNIT, 0);
                final int sampleIndex = (int) SharedPreferenceUtils.getParam(mContext, Constant.SAMPLE_UNIT, 0);

                updateSpinnerArray(mTotalUnitIndex);
                mMeasurePointUnit = mUnitArrays[mTotalUnitIndex][measureIndex];
                mSampleUnit = mUnitArrays[mTotalUnitIndex][sampleIndex];
                mMeasureUnitSp.setSelection(measureIndex, true);
                mSampleUnitSp.setSelection(sampleIndex);
            }
        }, Constant.DELAY_REFRESH_TIME);
    }

    private void initTitle() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.measure_set_toolbar);
        TextView customerTitle = (TextView) findViewById(R.id.customer_title);
        setTitleTv(customerTitle);
        setSupportActionBar(toolbar);
    }

    private void initView() {
        mMeasureUnitSp = (Spinner) findViewById(R.id.measure_unit_spinner);
        mMeasureUnitSp.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Log.d(TAG, "position: " + position + ", id: " + id);
                mMeasurePointUnit = mUnitArrays[mUnitIndex][position];
//                BluetoothHelper.getBluetoothHelper(mContext).sendHex(StringUtil.string2HexString(mUnitArrays[mUnitIndex][position]));
//                BluetoothHelper.getBluetoothHelper(mContext).sendHex(CommandUtil.TEST_HEX_CMD);
                SharedPreferenceUtils.setParam(mContext, Constant.MEASURE_UNIT, position);
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
//                BluetoothHelper.getBluetoothHelper(mContext).
//
// Hex(StringUtil.string2HexString(mUnitArrays[mUnitIndex][position]));
                SharedPreferenceUtils.setParam(mContext, Constant.SAMPLE_UNIT, position);
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
                SharedPreferenceUtils.setParam(mContext, Constant.STEP, newVal);
//                BluetoothHelper.getBluetoothHelper(mContext).sendString(mStepArray[newVal] + " unit");

            }
        });

        mTapPicker.refreshByNewDisplayedValues(mTapArray);
        mTapPicker.setOnValueChangedListener(new NumberPickerView.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPickerView picker, int oldVal, int newVal) {
                mTap = mTapArray[newVal];
                SharedPreferenceUtils.setParam(mContext, Constant.TAP, newVal);
            }
        });

        mCountPicker.refreshByNewDisplayedValues(mCountArray);
        mCountPicker.setOnValueChangedListener(new NumberPickerView.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPickerView picker, int oldVal, int newVal) {
                Log.d(TAG, "oldVal: " + oldVal + ", newVal: " + newVal + ", Value: " + mCountArray[newVal]);
                mCount = Integer.valueOf(mCountArray[newVal]);
                SharedPreferenceUtils.setParam(mContext, Constant.COUNT, newVal);
            }
        });

        mStartBtn = (Button) findViewById(R.id.start_btn);
        mEndBtn = (Button) findViewById(R.id.end_btn);
        mStartBtn.setOnClickListener(mListener);
        mEndBtn.setOnClickListener(mListener);
    }


    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        LogUtil.d(TAG, "onActivityResult " + resultCode);
        switch (requestCode) {
            case REQUEST_CONNECT_DEVICE:
                // When DeviceListActivity returns with a device to connect
                if (resultCode == AppCompatActivity.RESULT_OK) {
                    // Get the device MAC address
                    String address = data.getExtras()
                            .getString(DeviceListActivity.EXTRA_DEVICE_ADDRESS);
                    // Get the BLuetoothDevice object

                    BluetoothDevice device = BluetoothHelper.getBluetoothHelper(mContext).getBluetoothAdapter().getRemoteDevice(address);
                    if (device != null) {
                        // Attempt to connect to the device
                        BluetoothHelper.getBluetoothHelper(mContext).setmHandler(mHandler);
                        BluetoothHelper.getBluetoothHelper(mContext).connect(device);
                        SharedPreferenceUtils.setParam(mContext, BtConstant.SAVE_BT_ADDRESS, device.getAddress());
                    }
                }
                break;
            default:
                break;
        }
    }


    private View.OnClickListener mListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.start_btn:
                    BluetoothHelper.getBluetoothHelper(mContext).sendHex(CommandUtil.getStartCmd());
//                    BluetoothHelper.getBluetoothHelper(mContext).sendHex(CommandUtil.getCalibrateCmd(getBTUnitHexData(mMeasurePointUnit, mSampleUnit)));
                    SocketControl.getInstance().sendMsg(CommandUtil.getSocketDataCmd(getUnitData(mTap, mMeasurePointUnit, mSampleUnit)));
                    Intent intent = new Intent();
                    intent.putExtra(MeasureActivity.EXTRA_MEASURE_UNIT, mMeasurePointUnit);
                    intent.putExtra(MeasureActivity.EXTRA_SAMPLE_UNIT, mSampleUnit);
                    intent.putExtra(MeasureActivity.EXTRA_STEP, mStep);
                    intent.putExtra(MeasureActivity.EXTRA_TAP, mTap);
                    intent.putExtra(MeasureActivity.EXTRA_COUNT, mCount);
                    intent.setClass(mContext, MeasureActivity.class);
//                    isNeedResetHandler = true;
                    startActivity(intent);
                    break;
                case R.id.end_btn:
                    BluetoothHelper.getBluetoothHelper(mContext).sendHex(CommandUtil.getStopCmd());
//                    ToastUtil.showToast(mContext,WifiUtil.getWifiAddress(mContext));
                    break;
                default:
                    break;
            }
        }
    };

    private AlertDialog getUnitDialog() {
        if (mUnitDialog == null) {
            final String[] items = getResources().getStringArray(R.array.unit_type_array);
            mUnitDialog = new AlertDialog.Builder(this)
                    .setItems(items, new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            updateSpinnerArray(which);

                            SharedPreferenceUtils.setParam(mContext, Constant.TOTAL_UNIT, which);
                        }
                    }).create();
        }
        return mUnitDialog;
    }

    private void exitDialog() {
        final CustomToastDialog dialog = new CustomToastDialog(mContext);
        dialog.setTitle(R.string.warn);
        dialog.setMessage(R.string.exit_confirm);
        dialog.setPositiveButton(R.string.confirm, new CustomToastDialog.onEnterclickListener() {
            @Override
            public void onYesClick() {
                dialog.dismiss();
                AtyContainer.getInstance().finishAllActivity();
            }
        });
        dialog.setNegativeButton(R.string.cancel, new CustomToastDialog.onCancelclickListener() {
            @Override
            public void onNoClick() {
                dialog.dismiss();
                MeasureSetActivity.this.finish();
            }
        });
        dialog.show();
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

//    private boolean isContinue = true;

    private void test() {
//        mClient = new SocketControl(SocketConstant.DEFAULT_SERVER, SocketConstant.DEFAULT_PORT, mHttpListener);
        SocketControl.getInstance().sendMsg("AA12E4016368616E656C3120303030302E304EBBCC");

        String s = "AM03AN04_kN_N";
        LogUtil.d(TAG, "origin String: " + s + "cmdString: " + CommandUtil.getSocketDataCmd(s));
        s = "AX03AY04_0601_3_12.3";
        LogUtil.d(TAG, "origin String: " + s + "cmdString: " + CommandUtil.getSocketDataCmd(s));
    }

    private void sendTest(String data) {
        SocketControl.getInstance().sendMsg(data);
    }


    /*****
     * This is for test
     **/
//    private IMsgListener mIBtMsgListener = new IMsgListener() {
//        @Override
//        public void received(int state, final String msg) {
//            sendTest(msg);
//        }
//    };

}
