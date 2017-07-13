package com.android.meter.meter;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.RequiresApi;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import com.android.meter.meter.bluetooth.BluetoothHelper;
import com.android.meter.meter.bluetooth.BtConstant;
import com.android.meter.meter.http.HTTPConstant;
import com.android.meter.meter.http.IHttpListener;
import com.android.meter.meter.http.SocketControl;
import com.android.meter.meter.numberpicker.NumberPickerView;
import com.android.meter.meter.util.CommandUtil;
import com.android.meter.meter.util.Constant;
import com.android.meter.meter.util.LogUtil;
import com.android.meter.meter.util.StringUtil;
import com.android.meter.meter.util.ToastUtil;

import static com.android.meter.meter.util.CommandUtil.UPLOCD_CMD_CODE;

public class MeasureActivity extends Activity {
    private static final String TAG = LogUtil.COMMON_TAG + MeasureActivity.class.getSimpleName();

    public static final String EXTRA_MEASURE_UNIT = "measure_unit";
    public static final String EXTRA_STEP = "step";
    public static final String EXTRA_COUNT = "count";
    private static final int MEASURE_MODE = 0;
    private static final int CALIBRATE_MODE = 1;

    private String[] mLoadArray = new String[Constant.LINES_COUNT];
    private String[] mTimesArray;
    private Context mContext;

    private NumberPickerView mMeasurePointPicker;
    private NumberPickerView mLoadPicker;
    private NumberPickerView mTimesPicker;

    private Button mResetBtn;
    private Button mEnterBtn;
    private Button mCalcelBtn;
//    private Button mUploadBtn;
//    private Button mDownloadBtn;

    private String mMeasureValueUnit;
    private String mSampleUnit;

    private String mMeasurePointValue;
    private String mMeasureValue;
    private float mStep;
    private int mTotalTimes;
    private int mTimes;

    private TextView mBtStateTv;
    private TextView mSampleTv;
    private TextView mUnitTv;
    private int mMode = MEASURE_MODE;
    private boolean mFirstTime = true;

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
//            Log.d(TAG, "msg: " + msg.what)

            switch (msg.what) {
                case BtConstant.MESSAGE_STATE_CHANGE:
                    Log.i(TAG, "MESSAGE_STATE_CHANGE: " + msg.arg1);
                    switch (msg.arg1) {
                        case BluetoothHelper.STATE_CONNECTED:
//                            ToastUtil.showToast(mContext, "bluetooth connected");
                            mBtStateTv.setText("Connected");
                            break;
                        case BluetoothHelper.STATE_CONNECTING:
                            mBtStateTv.setText(R.string.title_connecting);
//                            ToastUtil.showToast(mContext, R.string.title_connecting);
                            break;
                        case BluetoothHelper.STATE_LISTEN:
                        case BluetoothHelper.STATE_NONE:
                            mBtStateTv.setText(R.string.title_not_connected);
//                            ToastUtil.showToast(mContext, R.string.title_not_connected);
                            break;
                    }
                    break;
                case BtConstant.MESSAGE_WRITE:
                    byte[] writeBuf = (byte[]) msg.obj;
                    // construct a string from the buffer
                    String writeMessage = StringUtil.bytes2HexString(writeBuf);
//                    mConversationArrayAdapter.add("Me:  " + writeMessage);
                    ToastUtil.showToast(mContext, "sendString: " + writeMessage);
                    break;
                case BtConstant.MESSAGE_READ:
                    byte[] readBuf = (byte[]) msg.obj;
                    String readMessage = StringUtil.bytes2HexString(readBuf);
//                    mSampleTv.setText(readMessage);
                    ToastUtil.showToast(mContext, "receice: " + readMessage);
                    handlerCmd(readMessage);
                    break;
                case BtConstant.MESSAGE_DEVICE_NAME:
                    break;
                case BtConstant.MESSAGE_TOAST:
//                    Toast.makeText(getApplicationContext(), msg.getData().getString(TOAST),
//                            Toast.LENGTH_SHORT).show();
                    break;
                case HTTPConstant.RECEIVE_MSG:
                    String data = (String) msg.obj;
                    handlerCmd(data);
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
        getActionBar().setIcon(null);
        getActionBar().setCustomView(R.layout.title_measure);
        getActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        View view = getActionBar().getCustomView();

        mTitleTv = (TextView) findViewById(R.id.measure_title_tv);
        mBtStateTv = (TextView) findViewById(R.id.bt_state_tv);
        ImageButton ib = (ImageButton) view.findViewById(R.id.measure_title_ib);
        ib.setOnClickListener(mListener);
        setContentView(R.layout.activity_measure);
        mContext = this;

        mSampleUnit = getIntent().getStringExtra(EXTRA_MEASURE_UNIT);
        mMeasureValueUnit = mSampleUnit;
        mStep = getIntent().getFloatExtra(EXTRA_STEP, 1);
        mTotalTimes = getIntent().getIntExtra(EXTRA_COUNT, 1);
        initData();
        initView();


        mMeasurePointPicker.postDelayed(new Runnable() {
            @Override
            public void run() {
                mMeasurePointPicker.setPickedIndexRelativeToRaw(mStepArray.length / 2);
            }
        }, Constant.DELAY_REFRESH_TIME);

        mLoadPicker.postDelayed(new Runnable() {
            @Override
            public void run() {
                mLoadPicker.setPickedIndexRelativeToRaw(mLoadArray.length / 2);
            }
        }, Constant.DELAY_REFRESH_TIME);

    }


    private void initData() {
        BluetoothHelper.getBluetoothChatService(mContext).setmHandler(mHandler);
        SocketControl.getInstance().setListener(mHttpListener);

//        mMeasurePointArray = getResources().getStringArray(R.array.speed_array);
        //mTimesArray = getResources().getStringArray(R.array.check_array);
        mTimesArray = new String[mTotalTimes];
        for (int i = 0; i < mTotalTimes; i++) {
            mTimesArray[i] = String.valueOf(i + 1);
        }

    }

    private void initView() {
        mMeasurePointPicker = (NumberPickerView) findViewById(R.id.measure_point_picker);
        mMeasurePointPicker.setHintText(mSampleUnit);
        mMeasurePointPicker.setOnValueChangedListener(new NumberPickerView.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPickerView picker, int oldVal, int newVal) {

            }
        });
        mTimesPicker = (NumberPickerView) findViewById(R.id.times_picker);
        mMeasurePointPicker.refreshByNewDisplayedValues(getStepArray(mStep));
        mTimesPicker.refreshByNewDisplayedValues(mTimesArray);
        mLoadPicker = (NumberPickerView) findViewById(R.id.load_picker);
        mLoadPicker.setIsDrawLine(true);
        mLoadPicker.refreshByNewDisplayedValues(mLoadArray);

        mResetBtn = (Button) findViewById(R.id.reset_btn);
        mEnterBtn = (Button) findViewById(R.id.center_btn);
        mCalcelBtn = (Button) findViewById(R.id.cancel_btn);
        mResetBtn.setOnClickListener(mListener);
        mEnterBtn.setOnClickListener(mListener);
        mCalcelBtn.setOnClickListener(mListener);
//        mUploadBtn = (Button) findViewById(R.id.upload_btn);
//        mDownloadBtn = (Button) findViewById(R.id.download_btn);
//        mUploadBtn.setOnClickListener(mListener);
//        mDownloadBtn.setOnClickListener(mListener);
        mUnitTv = (TextView) findViewById(R.id.unit_tv_measure);
        mUnitTv.setText(getFormatUnit(mSampleUnit));
        mSampleTv = (TextView) findViewById(R.id.measure_value_textView);
    }

    private View.OnClickListener mListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.reset_btn:
//                    ToastUtil.showToast(mContext, getStringById(R.string.reset));
                    dialog();
                    break;
                case R.id.center_btn:
//                    BluetoothHelper.getBluetoothChatService(mContext).sendHex(CommandUtil.TEST_HEX_CMD);
//                    SocketControl.getInstance().sendMsg(CommandUtil.TEST_HEX_CMD);
                    sendMsg(null);
                    mTimes++;
                    break;
                case R.id.cancel_btn:
                    ToastUtil.showToast(mContext, getStringById(R.string.cancel));
                    break;
                case R.id.measure_title_ib:
//                    ToastUtil.showToast(mContext, "change UI");
                    changeMode();
                    break;
//                case R.id.upload_btn:
//                    ToastUtil.showToast(mContext, "Up");
//                    break;
//                case R.id.download_btn:
//                    ToastUtil.showToast(mContext, "Down");
//                    break;
                default:
                    break;
            }
        }

    };

    private void changeMode() {
        if (MEASURE_MODE == mMode) {
            mMode = CALIBRATE_MODE;
            mTitleTv.setText(R.string.measure_title_calibrate);
        } else {
            mMode = MEASURE_MODE;
            mTitleTv.setText(R.string.measure_title_measure);
        }
    }

    private String getStringById(int str) {
        return mContext.getResources().getString(str);
    }

    private String[] mStepArray = new String[21];
//    private int mPreMagnification = 1;

    private String[] getStepArray(float magnification) {
//        if (mPreMagnification != magnification) {
        for (int i = 0; i <= 20; i++) {
            mStepArray[i] = StringUtil.getNumber(magnification * (i - 10));
//            Log.d(TAG, "i: " + mStepArray[i]);
        }
//        }
        return mStepArray;
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
                sendMsg(CommandUtil.RESET_CMD_CODE);
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
                case HTTPConstant.RECEIVE_MSG:
                    Message msg = new Message();
                    msg.arg1 = HTTPConstant.RECEIVE_MSG;
                    msg.obj = data;
                    mHandler.sendMessage(msg);
                    break;
                default:
                    break;
            }

        }
    };


    private void sendMsg(String msg) {
        String hexCmd;
        if (MEASURE_MODE == mMode) {
            if (mFirstTime) {
                hexCmd = CommandUtil.getUploadCmd(mSampleUnit);
                SocketControl.getInstance().sendMsg(hexCmd);
                hexCmd = CommandUtil.getUploadCmd(mMeasureValueUnit);
                SocketControl.getInstance().sendMsg(hexCmd);
                mFirstTime = false;
            }
            hexCmd = CommandUtil.getUploadCmd(mSampleTv.getText().toString());
            SocketControl.getInstance().sendMsg(hexCmd);
            hexCmd = CommandUtil.getUploadCmd(mMeasurePointValue);
            SocketControl.getInstance().sendMsg(hexCmd);
            hexCmd = CommandUtil.getUploadCmd(Integer.toString(mTimes));
            SocketControl.getInstance().sendMsg(hexCmd);
        }else{
            BluetoothHelper.getBluetoothChatService(mContext).sendHex(CommandUtil.TEST_HEX_CMD);
	}
    }


    public void handlerCmd(String hexCmd) {
        Log.d(TAG, "handlerCmd: "+ hexCmd);
        byte[] length = StringUtil.hexString2Bytes(hexCmd.substring(2, 4));
        int cmdLength = StringUtil.bytes2int(length);
        String cmdType = hexCmd.substring(4, 6);
        String content = "";
        if (6 + cmdLength *2 <= hexCmd.length()) {
            content = hexCmd.substring(6, 6 + cmdLength *2);
        }else{
            ToastUtil.showToast(mContext, "cmd lenght is error!");
        }
        Log.d(TAG, "cmdLength: " + cmdLength + " ,cmdType: " + cmdType + " , content: " + content);
        switch (cmdType) {
            case UPLOCD_CMD_CODE:
                //TODO replease it after separation is OK.
                String sampleValue = content.substring(18,36);
                mSampleTv.setText(StringUtil.hex2String(sampleValue));
                mUnitTv.setVisibility(View.GONE);
                break;
            default:
                break;
        }
    }
}
