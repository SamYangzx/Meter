package com.android.meter.meter;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.android.meter.meter.bluetooth.BluetoothHelper;
import com.android.meter.meter.bluetooth.BtConstant;
import com.android.meter.meter.excel.ExcelUtils;
import com.android.meter.meter.excel.Record;
import com.android.meter.meter.http.HTTPConstant;
import com.android.meter.meter.http.IHttpListener;
import com.android.meter.meter.http.SocketControl;
import com.android.meter.meter.numberpicker.LoadPickerView;
import com.android.meter.meter.numberpicker.NumberPickerView;
import com.android.meter.meter.numberpicker.NumberPickerView.OnValueChangeListener;
import com.android.meter.meter.util.CommandUtil;
import com.android.meter.meter.util.Constant;
import com.android.meter.meter.util.FileUtil;
import com.android.meter.meter.util.LogUtil;
import com.android.meter.meter.util.StringUtil;
import com.android.meter.meter.util.TimeUtil;
import com.android.meter.meter.util.ToastUtil;
import com.android.meter.meter.util.VibratorHelper;

import java.util.ArrayList;

import static com.android.meter.meter.bluetooth.BluetoothChatActivity.TOAST;
import static com.android.meter.meter.util.CommandUtil.UPLOCD_CMD_CODE;
import static com.android.meter.meter.util.CommandUtil.getValueData;

public class MeasureActivity extends AppCompatActivity {
    private static final String TAG = LogUtil.COMMON_TAG + MeasureActivity.class.getSimpleName();

    public static final String EXTRA_MEASURE_UNIT = "measure_unit";
    public static final String EXTRA_SAMPLE_UNIT = "sample_unit";
    public static final String EXTRA_TAP = "tap";
    public static final String EXTRA_STEP = "step";
    public static final String EXTRA_COUNT = "count";
    private static final int MEASURE_MODE = 0;
    private static final int CALIBRATE_MODE = 1;
    private static final int GENERAL_DELAY_TIME = 500;

    private static final int MEASURE_POINT_INDEX_OFF = 10;//

    private static String[] mTitleArray = {"测量点值", "采样值"};

    private String[] mLoadArray = new String[Constant.LINES_COUNT];
    private String[] mTimesArray;
    private Context mContext;

    private NumberPickerView mMeasurePointPicker;
    private LoadPickerView mLoadPicker;
    private NumberPickerView mTimesPicker;
    private String[] mMeasurePointArray = new String[21];
    private int mMeasurePointIndex = 0;

    private Button mResetBtn;
    private Button mEnterBtn;
    private Button mCalcelBtn;

    private String mMeasurePointUnit;
    private String mSampleUnit;

    private String mMeasurePointValue;
    //    private String mMeasureValue;
    private String mTap;
    private float mStep;
    private int mTotalTimes;
    private int mTimes;

    private TextView mBtStateTv;
    private TextView mSampleTv;
    private TextView mUnitTv;
    private TextView mLoadSpeedTv;
    private int mMode = MEASURE_MODE;
    private boolean mFirstTime = true;
    private boolean mNeedOffset = true;
    private int mInitRow = 0;

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
//            LogUtil.d(TAG, "msg: " + msg.what)
            String data;
            switch (msg.what) {
                case BtConstant.MESSAGE_STATE_CHANGE:
                    Log.i(TAG, "MESSAGE_STATE_CHANGE: " + msg.arg1);
                    switch (msg.arg1) {
                        case BluetoothHelper.STATE_CONNECTED:
//                            ToastUtil.showToast(mContext, "bluetooth connected");
                            mBtStateTv.setText("Connected");
                            break;
                        case BluetoothHelper.STATE_CONNECTING:
                            mBtStateTv.setText(R.string.title_bt_connecting);
//                            ToastUtil.showToast(mContext, R.string.title_connecting);
                            break;
                        case BluetoothHelper.STATE_LISTEN:
                        case BluetoothHelper.STATE_NONE:
                            mBtStateTv.setText(R.string.title_not_bt_connected);
//                            ToastUtil.showToast(mContext, R.string.title_not_bt_connected);
                            break;
                    }
                    break;
                case BtConstant.MESSAGE_WRITE:
                    byte[] writeBuf = (byte[]) msg.obj;
                    // construct a string from the buffer
                    String writeMessage = StringUtil.bytes2HexString(writeBuf);
//                    mConversationArrayAdapter.add("Me:  " + writeMessage);
                    ToastUtil.showToast(mContext, "BT sendString: " + writeMessage, ToastUtil.DEBUG);
                    break;
                case BtConstant.MESSAGE_RECEIVE_SUCCESS:
//                    BluetoothHelper.getBluetoothHelper(mContext).sendHex(CommandUtil.CHECKSUM_SUCCESS_HEXCMD);
                    byte[] readBuf = (byte[]) msg.obj;
                    String readMessage = StringUtil.bytes2HexString(readBuf);
//                    mSampleTv.setText(readMessage);
                    ToastUtil.showToast(mContext, "BT receive: " + readMessage, ToastUtil.DEBUG);
                    handlerCmd(readMessage);
                    break;
                case BtConstant.MESSAGE_RECEIVE_FAILED:
                    BluetoothHelper.getBluetoothHelper(mContext).sendHex(CommandUtil.CHECKSUM_FAILED_HEXCMD);
                    break;
                case BtConstant.MESSAGE_DEVICE_NAME:
                    break;
                case BtConstant.MESSAGE_TOAST:
                    Toast.makeText(getApplicationContext(), msg.getData().getString(TOAST),
                            Toast.LENGTH_SHORT).show();
                    break;
                case HTTPConstant.SEND_FAIL:
                    ToastUtil.showToast(mContext, "Socket发送数据失败，请检查网络连接是否正常!");
                    break;
                case HTTPConstant.RECEIVE_SUCCESS:
                    data = (String) msg.obj;
                    handlerCmd(data);
                    break;
                case HTTPConstant.RECEIVE_CHECK_FAILED:
                    ToastUtil.showToast(mContext, "电脑端未回复!");
                    break;
                case HTTPConstant.HAS_NOT_RESPONSE:
                    ToastUtil.showToast(mContext, "上一条指令还未处理完，请等待！");
                    break;
                default:
                    break;
            }
        }
    };


    private TextView mTitleTv;

    @SuppressLint("NewApi")
    @RequiresApi(api = Build.VERSION_CODES.HONEYCOMB)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_measure);
        mContext = this;
        initTitle();

        mMeasurePointUnit = getIntent().getStringExtra(EXTRA_MEASURE_UNIT);
        mSampleUnit = getIntent().getStringExtra(EXTRA_SAMPLE_UNIT);
        mTap = getIntent().getStringExtra(EXTRA_TAP);
        mStep = getIntent().getFloatExtra(EXTRA_STEP, 1);
        mTotalTimes = getIntent().getIntExtra(EXTRA_COUNT, 1);

        initData();
        initView();

        mMeasurePointPicker.postDelayed(new Runnable() {
            @Override
            public void run() {
                mMeasurePointPicker.setPickedIndexRelativeToRaw(mMeasurePointArray.length / 2);
            }
        }, Constant.DELAY_REFRESH_TIME);

        mLoadPicker.postDelayed(new Runnable() {
            @Override
            public void run() {
                mLoadPicker.setPickedIndexRelativeToRaw(mLoadArray.length / 2);
            }
        }, Constant.DELAY_REFRESH_TIME);

    }

    @Override
    protected void onDestroy() {
        LogUtil.d(TAG, "onDestroy is invoked.");
        super.onDestroy();
        BluetoothHelper.getBluetoothHelper(mContext).setmHandler(null);
    }

    private void initTitle() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.measure_toolbar);
        mTitleTv = (TextView) findViewById(R.id.measure_title_tv);
        mBtStateTv = (TextView) findViewById(R.id.bt_state_tv);
        ImageButton ib = (ImageButton) findViewById(R.id.measure_title_ib);
        ib.setOnClickListener(mListener);
    }


    private void initData() {
        BluetoothHelper.getBluetoothHelper(mContext).setmHandler(mHandler);
        SocketControl.getInstance().setListener(mHttpListener);

//        mMeasurePointArray = getResources().getStringArray(R.array.speed_array);
        //mTimesArray = getResources().getStringArray(R.array.check_array);
        mTimesArray = new String[mTotalTimes + 1];
        mTimesArray[0] = "——>";
        for (int i = 1; i < mTotalTimes + 1; i++) {
            mTimesArray[i] = String.valueOf(i);
        }
        mMeasurePointIndex = MEASURE_POINT_INDEX_OFF;
        mNeedOffset = true;


        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                BluetoothHelper.getBluetoothHelper(mContext).sendHex(CommandUtil.getCalibrateCmd(CommandUtil.getBTUnitHexData(mMeasurePointUnit, mSampleUnit)));
            }
        }, GENERAL_DELAY_TIME);
    }

    private void initView() {
        mMeasurePointPicker = (NumberPickerView) findViewById(R.id.measure_point_picker);
        mMeasurePointPicker.setHintText(mMeasurePointUnit);
        mMeasurePointPicker.refreshByNewDisplayedValues(getMeasurePointArray(mStep));
        mMeasurePointValue = "0"; //init mMeasurePointValue.
        mMeasurePointPicker.setOnValueChangedListener(new OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPickerView picker, int oldVal, int newVal) {
                mMeasurePointValue = mMeasurePointArray[newVal];
                LogUtil.d(TAG, "newVal: " + newVal + ", mMeasurePointValue: " + mMeasurePointValue);
                mMeasurePointIndex = newVal;
            }
        });
        mTimesPicker = (NumberPickerView) findViewById(R.id.times_picker);
        mTimesPicker.refreshByNewDisplayedValues(mTimesArray);
        mTimesPicker.setOnValueChangedListener(new NumberPickerView.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPickerView picker, int oldVal, int newVal) {
                mTimes = newVal;
            }
        });
        mLoadPicker = (LoadPickerView) findViewById(R.id.load_picker);
        mLoadPicker.setIsDrawLine(true);
        mLoadPicker.refreshByNewDisplayedValues(mLoadArray);
        mLoadPicker.setOnScrollListener(new LoadPickerView.OnScrollListener() {
            @Override
            public void onScrollStateChange(LoadPickerView view, int scrollState) {

            }

            @Override
            public void onScrollFling(LoadPickerView view, final float speedRatio) {
//                sendMsg(Float.toString(speedRatio), false);
                byte[] ratio = new byte[1];
                ratio[0] = (byte) (speedRatio * 100);
                LogUtil.d(TAG, "speedRatio: " + speedRatio + " ,intRatio: " + ratio[0] + ", Integer.toString(ratio): " + StringUtil.bytes2HexString(ratio));
                sendCmd(CommandUtil.getLoadCmd(StringUtil.bytes2HexString(ratio)), false);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mLoadSpeedTv.setText(Float.toString(speedRatio));
                    }
                });
            }
        });

        mResetBtn = (Button) findViewById(R.id.reset_btn);
        mEnterBtn = (Button) findViewById(R.id.center_btn);
        mCalcelBtn = (Button) findViewById(R.id.cancel_btn);
        mResetBtn.setOnClickListener(mListener);
        mEnterBtn.setOnClickListener(mListener);
        mCalcelBtn.setOnClickListener(mListener);
        mUnitTv = (TextView) findViewById(R.id.unit_tv_measure);
        mUnitTv.setText(getFormatUnit(mSampleUnit));
        mSampleTv = (TextView) findViewById(R.id.measure_value_textView);
        mLoadSpeedTv = (TextView) findViewById(R.id.load_speed_tv);
    }

    private View.OnClickListener mListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.reset_btn:
//                    ToastUtil.showToast(mContext, getStringById(R.string.reset));
                    VibratorHelper.vibrate(mContext);
                    dialog();
                    break;
                case R.id.center_btn:
                    VibratorHelper.vibrate(mContext);
                    String hexStr;
//                    hexStr = Float.toHexString(Float.valueOf(mMeasurePointValue));
//                    hexStr = StringUtil.bytes2HexString(StringUtil.int2byte(mMeasurePointIndex));
                    hexStr = CommandUtil.getMeasurePointHexValue(mMeasurePointIndex);
                    LogUtil.d(TAG, "mMeasurePointValue: " + mMeasurePointValue + ", hexStr: " + hexStr);
//                    BluetoothHelper.getBluetoothHelper(mContext).sendHex(CommandUtil.TEST_HEX_CMD);
//                    SocketControl.getInstance().sendMsg(CommandUtil.TEST_HEX_CMD);
                    if (CALIBRATE_MODE == mMode) {
                        sendCmd(CommandUtil.getConfirmCalibrateCmd(hexStr, mMeasurePointValue), false);
                    } else {
                        sendCmd(CommandUtil.getSocketDataCmd(getValueData(mTap, mTotalTimes, mTimes, mMeasurePointValue, mSampleTv.getText().toString())), true);
                        ExcelUtils.initExcel(FileUtil.getExcelPath(), mTitleArray);
                        int rows = 0;
                        if (mNeedOffset) {
                            mRecordList.clear();
                            mRecordList.add(new Record(mMeasurePointUnit, mSampleUnit).toArrayList());
                            mRecordList.add(new Record(TimeUtil.getDateYearMonthDayHourMinute(), "").toArrayList());
                            ExcelUtils.writeObjListToExcel(mRecordList, FileUtil.getExcelPath(), mNeedOffset);
                            mInitRow = ExcelUtils.getRows(FileUtil.getExcelPath());
                            rows = mInitRow;
                            mNeedOffset = false;
                        } else {
                            rows = ExcelUtils.getRows(FileUtil.getExcelPath());
                        }
                        Record record = new Record(mMeasurePointValue, mSampleTv.getText().toString());
                        LogUtil.d(TAG, "mInitRow: " + mInitRow + ", mTotalTimes: " + mTotalTimes + ", mTimes: " + mTimes + " , ExcelUtils.getRows: " + rows);
                        if (mInitRow + mTotalTimes >= rows && mTimes == 0) {
                            mRecordList.clear();
                            mRecordList.add(record.toArrayList());
                            ExcelUtils.writeObjListToExcel(mRecordList, FileUtil.getExcelPath(), mNeedOffset);
                        } else {
                            if (mTimes == 0) {
                                ExcelUtils.writeObjToRow(record.toArrayList(), FileUtil.getExcelPath(), mInitRow + 1);
                            } else {
                                ExcelUtils.writeObjToRow(record.toArrayList(), FileUtil.getExcelPath(), mInitRow + mTimes);
                            }
                        }

                        rows = ExcelUtils.getRows(FileUtil.getExcelPath());
                        if (mInitRow + mTotalTimes == rows) {
                            ExcelUtils.writeObjToRow(new Record(TimeUtil.getDateYearMonthDayHourMinute(), "").toArrayList(), FileUtil.getExcelPath(), mInitRow + mTotalTimes + 1);
                        }

                    }
//                    mTimes++;
                    break;
                case R.id.cancel_btn:
//                    ToastUtil.showToast(mContext, getStringById(R.string.cancel));
                    VibratorHelper.vibrate(mContext);
                    if (MEASURE_MODE == mMode) {
                        mSampleTv.setText("0");
                    } else {
                        sendCmd(CommandUtil.getSaveCalibrateCmd(), false);
                    }
                    break;
                case R.id.measure_title_ib:
                    changeModeDialog();
                    break;
                default:
                    break;
            }
        }

    };

    private void changeMode() {
        if (MEASURE_MODE == mMode) {
            mMode = CALIBRATE_MODE;
            mTitleTv.setText(R.string.measure_title_calibrate);
            mCalcelBtn.setText(R.string.save_calibrate);
        } else {
            mMode = MEASURE_MODE;
            mTitleTv.setText(R.string.measure_title_measure);
            mCalcelBtn.setText(R.string.cancel);
        }
    }

    private String getStringById(int str) {
        return mContext.getResources().getString(str);
    }

//    private int mPreMagnification = 1;

    private String[] getMeasurePointArray(float magnification) {
//        if (mPreMagnification != magnification) {
        for (int i = 0; i <= 20; i++) {
            mMeasurePointArray[i] = StringUtil.getNumber(magnification * (i - 10));
//            LogUtil.d(TAG, "i: " + mMeasurePointArray[i]);
        }
//        }
        return mMeasurePointArray;
    }


    protected void dialog() {
        AlertDialog.Builder builder = new android.app.AlertDialog.Builder(mContext);
        builder.setMessage(R.string.reset_confirm);
        builder.setTitle(R.string.warn);
        builder.setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                ToastUtil.showToast(mContext, R.string.reset);
//                MeasureActivity.this.finish();
//                sendMsg(CommandUtil.RESET_CMD_CODE, true);
//                SocketControl.getInstance().sendMsg(CommandUtil.RESET_CMD_CODE);
                BluetoothHelper.getBluetoothHelper(mContext).sendHex(CommandUtil.getResetCmd());
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.create().show();
    }


    private void changeModeDialog() {
        AlertDialog.Builder builder = new android.app.AlertDialog.Builder(mContext);
        builder.setMessage(R.string.change_confirm);
        builder.setTitle(R.string.warn);
        builder.setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                changeMode();
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.create().show();
    }


    private String getFormatUnit(String unit) {
        return "(" + unit + ")";
    }


    private IHttpListener mHttpListener = new IHttpListener() {
        @Override
        public void onResult(int state, String data) {
            Message msg = new Message();
            msg.what = state;
            msg.obj = data;
            mHandler.sendMessage(msg);
        }
    };

    /**
     * msg origin string data.
     * changeMode if need change send method BT or http, this will be true.
     */
//    private void sendMsg(String msg, boolean changeMode) {
//        LogUtil.d(TAG, "sendMsg.msg: " + msg + ", changeMode: " + changeMode);
//        String hexCmd;
//        if (changeMode) {
//            if (MEASURE_MODE == mMode) {
//                if (mFirstTime) {
//                    hexCmd = CommandUtil.getUploadCmd(COLLECTOR_PRE_CODE, mSampleUnit);
//                    SocketControl.getInstance().sendMsg(hexCmd);
//                    hexCmd = CommandUtil.getUploadCmd(mMeasurePointUnit);
//                    SocketControl.getInstance().sendMsg(hexCmd);
//                    mFirstTime = false;
//                }
//                hexCmd = CommandUtil.getUploadCmd(mSampleTv.getText().toString());
//                SocketControl.getInstance().sendMsg(hexCmd);
//                hexCmd = CommandUtil.getUploadCmd(mMeasurePointValue);
//                SocketControl.getInstance().sendMsg(hexCmd);
//                hexCmd = CommandUtil.getUploadCmd(Integer.toString(mTimes));
//                SocketControl.getInstance().sendMsg(hexCmd);
//            } else {
//                hexCmd = CommandUtil.getUploadCmd(mSampleTv.getText().toString());
//                BluetoothHelper.getBluetoothHelper(mContext).sendHex(hexCmd);
//            }
//        } else {
//            hexCmd = CommandUtil.getUploadCmd(CommandUtil.PLATFORM_PRE_CODE, msg);
//            BluetoothHelper.getBluetoothHelper(mContext).sendHex(hexCmd);
//        }
//    }
    private void sendCmd(String cmd, boolean socket) {
//        LogUtil.d(TAG, "sendMsg.msg: " + cmd + ", socket: " + socket);
        if (socket) {
            SocketControl.getInstance().sendMsg(cmd);
        } else {
            BluetoothHelper.getBluetoothHelper(mContext).sendHex(cmd);
        }
    }


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
                //TODO replease it after separation is OK.
                String sampleValue = "";
                if (content.length() >= (cmdLength - 2) * 2) {
                    sampleValue = content.substring(18);
                    int divideIndex = StringUtil.getValueUnitIndex(sampleValue);
                    String s = Float.valueOf(StringUtil.hex2String(sampleValue.substring(0, divideIndex))).toString();
                    LogUtil.d(TAG, "without more 0: " + s);
                    mSampleTv.setText(s);
                    mUnitTv.setText(getFormatUnit(StringUtil.hex2String(sampleValue.substring(divideIndex))));
                }
                break;
            default:
                break;
        }
    }

    private ArrayList<ArrayList<String>> mRecordList = new ArrayList<ArrayList<String>>();

    private ArrayList<ArrayList<String>> addRecord(ArrayList<String> record) {
        mRecordList.clear();
        mRecordList.add(record);
        return mRecordList;
    }

}