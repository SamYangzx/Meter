package com.android.meter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.RequiresApi;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.android.meter.bluetooth.BluetoothHelper;
import com.android.meter.bluetooth.BtConstant;
import com.android.meter.excel.ExcelUtils;
import com.android.meter.excel.Record;
import com.android.meter.general_ui.CustomToastDialog;
import com.android.meter.http.SocketConstant;
import com.android.meter.http.SocketControl;
import com.android.meter.numberpicker.LoadPickerView;
import com.android.meter.numberpicker.NumberPickerView;
import com.android.meter.numberpicker.NumberPickerView.OnValueChangeListener;
import com.android.meter.util.CommandUtil;
import com.android.meter.util.Constant;
import com.android.meter.util.FileUtil;
import com.android.meter.util.LogUtil;
import com.android.meter.util.SharedPreferenceUtils;
import com.android.meter.util.StringUtil;
import com.android.meter.util.TimeUtil;
import com.android.meter.util.ToastUtil;
import com.android.meter.util.VibratorHelper;
import com.lzy.imagepicker.util.FlagUtils;

import java.util.ArrayList;

import static com.android.meter.bluetooth.BluetoothChatActivity.TOAST;
import static com.android.meter.util.CommandUtil.UPLOCD_CMD_CODE;
import static com.android.meter.util.CommandUtil.getUnitData;
import static com.android.meter.util.CommandUtil.getValueData;

public class MeasureActivity extends BaseActivity {
    private static final String TAG = LogUtil.COMMON_TAG + MeasureActivity.class.getSimpleName();

    public static final String EXTRA_MEASURE_UNIT = "measure_unit";
    public static final String EXTRA_SAMPLE_UNIT = "sample_unit";
    public static final String EXTRA_TAP = "tap";
    public static final String EXTRA_STEP = "step";
    public static final String EXTRA_COUNT = "count";
    private static final int MEASURE_MODE = 0;
    private static final int CALIBRATE_MODE = 1;
    private static final int GENERAL_DELAY_TIME = 300;

    private static final int MEASURE_POINT_INDEX_OFF = 10;//

    private static String[] mTitleArray = {"测量点值", "采样值"};

    private String[] mLoadArray = new String[Constant.LINES_COUNT];
    private String[] mTimesArray;

    private NumberPickerView mMeasurePointPicker;
    private LoadPickerView mLoadPicker;
    private NumberPickerView mTimesPicker;
    private String[] mMeasurePointArray;
    private String[] mTempMeaseureArray = new String[2001];
    private String[] mTempCalculArray = new String[21];

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

    private TextView mDeviceStateTv;
    private TextView mSampleTv;
    private TextView mUnitTv;
    private TextView mLoadSpeedTv;
    private TextView mMeasurePointUnitTv;
    private int mMode = MEASURE_MODE;
    private boolean mFirstTime = true;
    private boolean mNeedOffset = true;
    private int mInitRow = 0;

    private TextView mTitleTv;

    @SuppressLint("NewApi")
    @RequiresApi(api = Build.VERSION_CODES.HONEYCOMB)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LogUtil.i(TAG, "onCreate");
        setContentView(R.layout.activity_measure);
        initTitle();

        mMeasurePointUnit = getIntent().getStringExtra(EXTRA_MEASURE_UNIT);
        mSampleUnit = getIntent().getStringExtra(EXTRA_SAMPLE_UNIT);
        LogUtil.v(TAG, "mSampleUnit: " + mSampleUnit);
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
    }

    private void initTitle() {
//        Toolbar toolbar = (Toolbar) findViewById(R.id.measure_toolbar);
        mTitleTv = (TextView) findViewById(R.id.measure_title_tv);
        mDeviceStateTv = (TextView) findViewById(R.id.device_state_tv);
        setTitleTv(mDeviceStateTv);
        ImageButton changeIb = (ImageButton) findViewById(R.id.measure_title_ib);
        changeIb.setOnClickListener(mListener);

        ImageButton stopIb = (ImageButton) findViewById(R.id.stop_ib);
        stopIb.setOnClickListener(mListener);
        ImageButton connectIb = (ImageButton) findViewById(R.id.connect_ib);
        connectIb.setOnClickListener(mListener);

        if (((MainApplication) getApplication()).isModeA()) {
            mTitleTv.setVisibility(View.VISIBLE);
            stopIb.setVisibility(View.GONE);
        } else {
            mTitleTv.setVisibility(View.GONE);
        }
    }


    private void initData() {
        mTimesArray = new String[mTotalTimes + 1];
        mTimesArray[0] = "——>";
        for (int i = 1; i < mTotalTimes + 1; i++) {
            mTimesArray[i] = String.valueOf(i);
        }

        for (int i = 0; i <= 20; i++) {
            mTempCalculArray[i] = StringUtil.getNumber(mStep * (i - 10));
//            LogUtil.d(TAG, "i: " + mMeasurePointArray[i]);
        }
        for (int i = 0; i <= 2000; i++) {
            mTempMeaseureArray[i] = StringUtil.getNumber(mStep * (i - 1000));
//            LogUtil.d(TAG, "i: " + mMeasurePointArray[i]);
        }

        mMeasurePointIndex = MEASURE_POINT_INDEX_OFF;
        mNeedOffset = true;

        //若立刻发送指令，蓝牙端有时数据会接收不到。
        LogUtil.d(TAG, "mHanlder is invoked");
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                LogUtil.d(TAG, "postDelayed.run");
                BluetoothHelper.getBluetoothHelper(mAppContext).sendHex(CommandUtil.getCalibrateCmd(CommandUtil.getBTUnitHexData(mMeasurePointUnit, mSampleUnit)));
                BluetoothHelper.getBluetoothHelper(mAppContext).sendHex(CommandUtil.getStartCmd());
                sendCmd(CommandUtil.getSocketDataCmd(getUnitData(mTap, mMeasurePointUnit, mSampleUnit)), true, FlagUtils.iSModeA());
            }
        }, GENERAL_DELAY_TIME);
    }

    private void initView() {
        mMeasurePointPicker = (NumberPickerView) findViewById(R.id.measure_point_picker);
//        mMeasurePointPicker.setHintText(mMeasurePointUnit);
        mMeasurePointPicker.refreshByNewDisplayedValues(getMeasurePointArray(mStep));
        mMeasurePointValue = "0"; //init mMeasurePointValue.
        mMeasurePointUnitTv = (TextView) findViewById(R.id.measure_point_unit);
        mMeasurePointUnitTv.setText(mMeasurePointUnit);
        updateMeasurePointPicker();

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
                byte[] ratio = new byte[1];
                ratio[0] = (byte) (speedRatio * 100);
                LogUtil.d(TAG, "speedRatio: " + speedRatio + " ,intRatio: " + ratio[0] + ", Integer.toString(ratio): " + StringUtil.bytes2HexString(ratio));
                sendCmd(CommandUtil.getLoadCmd(StringUtil.bytes2HexString(ratio)), false, true);
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
        mUnitTv.setText(StringUtil.getFormatUnit(mSampleUnit));
        mSampleTv = (TextView) findViewById(R.id.measure_value_textView);
        mLoadSpeedTv = (TextView) findViewById(R.id.load_speed_tv);
    }

    @Override
    public void onBackPressed() {
        backDialog();
    }

    private void updateMeasurePointPicker() {
        mMeasurePointPicker.setOnValueChangedListener(new OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPickerView picker, int oldVal, int newVal) {
                mMeasurePointValue = mMeasurePointArray[newVal];
                LogUtil.d(TAG, "newVal: " + newVal + ", mMeasurePointValue: " + mMeasurePointValue);
                mMeasurePointIndex = newVal;
            }
        });
    }


    private View.OnClickListener mListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.reset_btn:
                    VibratorHelper.vibrate(mContext);
                    resetDialog();
                    break;
                case R.id.center_btn:
                    handleCenterBtn();
                    break;
                case R.id.cancel_btn:
                    VibratorHelper.vibrate(mContext);
                    if (MEASURE_MODE == mMode) {
                        mSampleTv.setText("0");
                    } else {
                        sendCmd(CommandUtil.getSaveCalibrateCmd(), false, true);
                    }
                    break;
                case R.id.measure_title_ib:
                    if (((MainApplication) getApplication()).isModeA()) {
                        changeModeDialog();
                    } else {
                        startSendCmdActivity();
                    }
                    break;
                case R.id.stop_ib:
                    stopDialog();
                    break;
                case R.id.connect_ib:
                    LogUtil.v(TAG, "start connect");
                    String server = (String) SharedPreferenceUtils.getParam(mAppContext, SocketConstant.SAVE_IP, SocketConstant.DEFAULT_SERVER);
                    int port = (int) SharedPreferenceUtils.getParam(mAppContext, SocketConstant.SAVE_PORT, SocketConstant.DEFAULT_PORT);
                    SocketControl.getInstance().connect(server, port);

                    String btAddress = (String) SharedPreferenceUtils.getParam(mAppContext, BtConstant.SAVE_BT_ADDRESS, "");
                    BluetoothHelper.getBluetoothHelper(mAppContext).connect(btAddress);
                    break;
                default:
                    break;
            }
        }

    };

    private void handleCenterBtn() {
        VibratorHelper.vibrate(mContext);
        if (CALIBRATE_MODE == mMode && mMeasurePointIndex > 20) { //切换到测量模式时短时间内未及时更新。@{
            LogUtil.d(TAG, "force calibrate!!");
            mMeasurePointIndex = mMeasurePointIndex - 990;
        }//@}
        String hexStr = CommandUtil.getMeasurePointHexValue(mMeasurePointIndex);
        LogUtil.d(TAG, "mMeasurePointValue: " + mMeasurePointValue + ", mMeasurePointIndex: " + mMeasurePointIndex + ", hexStr: " + hexStr);
        if (FlagUtils.iSModeA()) {
            if (CALIBRATE_MODE == mMode) {
                sendCmd(CommandUtil.getConfirmCalibrateCmd(hexStr, mMeasurePointValue), false, true);
            } else {
//              initTestData();
                //此种模式要直接发送真实数据
                SocketControl.getInstance().sendMsg(CommandUtil.getSocketDataCmd(getValueData(mTap, mTotalTimes, mTimes, mMeasurePointValue, mSampleTv.getText().toString())), true);
            }
        } else {
            sendCmd(CommandUtil.getSocketDataCmd(getValueData(mTap, mTotalTimes, mTimes, mMeasurePointValue, mSampleTv.getText().toString())), true, false);
        }
    }

    /**
     * 存储要发送的数据到excel表格
     */
    private void saveDataIntoExcel() {
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

    private void startSendCmdActivity() {
        Intent intent = new Intent();
        intent.setClass(MeasureActivity.this, SendCmdActivity.class);
        startActivity(intent);
    }

    /**
     * change measure and calculate mode.
     */
    private void changeMode() {
        if (MEASURE_MODE == mMode) {
            LogUtil.d(TAG, "changeMode CALIBRATE_MODE");
            mMode = CALIBRATE_MODE;
            mTitleTv.setText(R.string.measure_title_calibrate);
            mCalcelBtn.setText(R.string.save_calibrate);
        } else {
            mMode = MEASURE_MODE;
            LogUtil.d(TAG, "changeMode MEASURE_MODE");
            mTitleTv.setText(R.string.measure_title_measure);
            mCalcelBtn.setText(R.string.cancel);
        }
        mMeasurePointPicker.refreshByNewDisplayedValues(getMeasurePointArray(mStep));
        mMeasurePointPicker.setPickedIndexRelativeToRaw(mMeasurePointArray.length / 2);
        updateMeasurePointPicker();
    }

    private String getStringById(int str) {
        return mContext.getResources().getString(str);
    }

    private String[] getMeasurePointArray(float magnification) {
        if (CALIBRATE_MODE == mMode) {
            mMeasurePointArray = mTempCalculArray;
        } else {
            mMeasurePointArray = mTempMeaseureArray;
        }
        LogUtil.d(TAG, "mMeasurePointArray.length: " + mMeasurePointArray.length);
        return mMeasurePointArray;
    }

    protected void resetDialog() {
        final CustomToastDialog dialog = new CustomToastDialog(mContext);
        dialog.setTitle(R.string.warn);
        dialog.setMessage(R.string.reset_confirm);
        dialog.setPositiveButton(R.string.confirm, new CustomToastDialog.onEnterclickListener() {
            @Override
            public void onYesClick() {
                dialog.dismiss();
                ToastUtil.showToast(mContext, R.string.reset);
                BluetoothHelper.getBluetoothHelper(mAppContext).sendHex(CommandUtil.getResetCmd());
            }
        });
        dialog.setNegativeButton(R.string.cancel, new CustomToastDialog.onCancelclickListener() {
            @Override
            public void onNoClick() {
                dialog.dismiss();
            }
        });
        dialog.show();
    }

    private void backDialog() {
        final CustomToastDialog dialog = new CustomToastDialog(mContext);
        dialog.setTitle(R.string.warn);
        dialog.setMessage(R.string.back_confirm);
        dialog.setPositiveButton(R.string.confirm, new CustomToastDialog.onEnterclickListener() {
            @Override
            public void onYesClick() {
                dialog.dismiss();
                finish();
            }
        });
        dialog.setNegativeButton(R.string.cancel, new CustomToastDialog.onCancelclickListener() {
            @Override
            public void onNoClick() {
                dialog.dismiss();
            }
        });
        dialog.show();
    }

    private void stopDialog() {
        final CustomToastDialog dialog = new CustomToastDialog(mContext);
        dialog.setTitle(R.string.warn);
        dialog.setMessage(R.string.end_measure_confirm);
        dialog.setPositiveButton(R.string.confirm, new CustomToastDialog.onEnterclickListener() {
            @Override
            public void onYesClick() {
                dialog.dismiss();
                SharedPreferenceUtils.setParam(mAppContext, Constant.SAME_PHOTO_FOLDER, false);
                ToastUtil.showToast(mContext, R.string.measure_end);
            }
        });
        dialog.setNegativeButton(R.string.cancel, new CustomToastDialog.onCancelclickListener() {
            @Override
            public void onNoClick() {
                dialog.dismiss();
            }
        });
        dialog.show();
    }

    private void changeModeDialog() {
        final CustomToastDialog dialog = new CustomToastDialog(mContext);
        dialog.setTitle(R.string.warn);
        dialog.setMessage(R.string.change_confirm);
        dialog.setPositiveButton(R.string.confirm, new CustomToastDialog.onEnterclickListener() {
            @Override
            public void onYesClick() {
                dialog.dismiss();
                changeMode();
            }
        });
        dialog.setNegativeButton(R.string.cancel, new CustomToastDialog.onCancelclickListener() {
            @Override
            public void onNoClick() {
                dialog.dismiss();
            }
        });
        dialog.show();
    }

    @Override
    public void handleReceiveData(String sampleValue) {
        super.handleReceiveData(sampleValue);
        int divideIndex = StringUtil.getValueUnitIndex(sampleValue);
        String s = StringUtil.getStrWithoutFront0(StringUtil.hex2String(sampleValue.substring(0, divideIndex)));
        LogUtil.v(TAG, "without more 0: " + s);
        mSampleTv.setText(s);

    }

    private ArrayList<ArrayList<String>> mRecordList = new ArrayList<ArrayList<String>>();

    private void initTestData() {
        mSampleTv.setText("12.345");
    }
}