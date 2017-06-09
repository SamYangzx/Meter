package com.android.meter.meter;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import com.android.meter.meter.numberpicker.NumberPickerView;
import com.android.meter.meter.util.LogUtil;
import com.android.meter.meter.util.StringUtil;
import com.android.meter.meter.util.ToastUtil;

import static com.android.meter.meter.R.layout.activity_measure;

public class MeasureActivity extends Activity {
    private static final String TAG = MeasureActivity.class.getSimpleName();

    public static final String EXTRA_MEASURE_UNIT = "measure_unit";
    public static final String EXTRA_STEP = "step";
    public static final String EXTRA_COUNT = "count";

    private String[] mSpeedArray;
    private String[] mCheckPointArray;
    private Context mContext;

    private NumberPickerView mSpeedPicker;
    private NumberPickerView mCheckPointPicker;

    private Button mResetBtn;
    private Button mCenterBtn;
    private Button mCalcelBtn;
//    private Button mUploadBtn;
//    private Button mDownloadBtn;

    private String mSampleUnit;
    private float mStep;
    private int mCount;

    private Toolbar mToolbar;

    @SuppressLint("NewApi")
    @RequiresApi(api = Build.VERSION_CODES.HONEYCOMB)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getActionBar().setIcon(null);
        getActionBar().setCustomView(R.layout.title_measure);
        getActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        View view = getActionBar().getCustomView();
        ImageButton ib = (ImageButton) view.findViewById(R.id.measure_title_ib);
        ib.setOnClickListener(mListener);

        setContentView(activity_measure);
        //使用布局文件来定义标题栏
        mContext = this;
//        mToolbar = (Toolbar)findViewById(R.id.activity_measure_toolbar);
//        if (mToolbar != null) {
//            //将Toolbar显示到界面
//
//            setSupportActionBar(mToolbar);
//        }else{
//            Log.d(TAG, "mToolbar == null");
//
//
//        }

        mSampleUnit = getIntent().getStringExtra(EXTRA_MEASURE_UNIT);
        mStep = getIntent().getFloatExtra(EXTRA_STEP, 1);
        Log.d(TAG, "onCreate.mStep: " + mStep);
        mCount = getIntent().getIntExtra(EXTRA_COUNT, 1);
        initData();
        initView();
    }


    private void initData() {
//        mSpeedArray = getResources().getStringArray(R.array.speed_array);
        //mCheckPointArray = getResources().getStringArray(R.array.check_array);
        mCheckPointArray = new String[mCount];
        for (int i = 0; i < mCount; i++) {
            mCheckPointArray[i] = String.valueOf(i + 1);
        }
    }

    private void initView() {
        mSpeedPicker = (NumberPickerView) findViewById(R.id.speed_picker);
        mCheckPointPicker = (NumberPickerView) findViewById(R.id.check_point_picker);
        mSpeedPicker.refreshByNewDisplayedValues(getStepArray(mStep));
        mCheckPointPicker.refreshByNewDisplayedValues(mCheckPointArray);
        NumberPickerView loadPicker = (NumberPickerView) findViewById(R.id.load_picker);
        loadPicker.setIsDrawLine(true);

        mResetBtn = (Button) findViewById(R.id.reset_btn);
        mCenterBtn = (Button) findViewById(R.id.center_btn);
        mCalcelBtn = (Button) findViewById(R.id.cancel_btn);
        mResetBtn.setOnClickListener(mListener);
        mCenterBtn.setOnClickListener(mListener);
        mCalcelBtn.setOnClickListener(mListener);
//        mUploadBtn = (Button) findViewById(R.id.upload_btn);
//        mDownloadBtn = (Button) findViewById(R.id.download_btn);
//        mUploadBtn.setOnClickListener(mListener);
//        mDownloadBtn.setOnClickListener(mListener);
        TextView unitTv = (TextView) findViewById(R.id.unit_tv_measure);
        unitTv.setText(mSampleUnit);
    }

    private View.OnClickListener mListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.reset_btn:
                    ToastUtil.showToast(mContext, getStringById(R.string.reset));
                    break;
                case R.id.center_btn:
                    ToastUtil.showToast(mContext, getStringById(R.string.center));
                    break;
                case R.id.cancel_btn:
                    ToastUtil.showToast(mContext, getStringById(R.string.cancel));
                    break;
                case R.id.measure_title_ib:
                    ToastUtil.showToast(mContext, "change UI");
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

    private String getStringById(int str) {
        return mContext.getResources().getString(str);
    }

    private String[] mStepArray = new String[21];
//    private int mPreMagnification = 1;

    private String[] getStepArray(float magnification) {
//        if (mPreMagnification != magnification) {
        for (int i = 0; i <= 20; i++) {
            mStepArray[i] = StringUtil.getNumber(magnification * (i - 10));
            Log.d(TAG, "i: " + mStepArray[i]);
        }
//        }
        return mStepArray;
    }

}
