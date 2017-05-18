package com.android.meter.meter;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.android.meter.meter.numberpicker.NumberPickerView;

public class MainActivity extends AppCompatActivity implements View.OnClickListener,
        NumberPickerView.OnScrollListener, NumberPickerView.OnValueChangeListener,
        NumberPickerView.OnValueChangeListenerInScrolling{
    private static final String TAG = MainActivity.class.getSimpleName();

    private Context mContext;
    private String[] mStepArray;
    private String[] mTapArray;
    private String[] mJinhuiArray;

    private NumberPickerView mStepPicker;
    private NumberPickerView mTapPicker;
    private NumberPickerView mJinhuiPicker;

    private Button mStartBtn;
    private Button mEndBtn;


    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mContext = this;
        initData();
        initView();

    }

    private void initData() {
        mStepArray = getResources().getStringArray(R.array.step_array);
        mTapArray = getResources().getStringArray(R.array.tap_array);
        mJinhuiArray = getResources().getStringArray(R.array.jinhui_array);
    }

    private void initView() {
        mStepPicker = (NumberPickerView)findViewById(R.id.step_picker);
        mTapPicker = (NumberPickerView)findViewById(R.id.tap_picker);
        mJinhuiPicker = (NumberPickerView)findViewById(R.id.jinhui_picker);

        mStepPicker.refreshByNewDisplayedValues(mStepArray);
        mTapPicker.refreshByNewDisplayedValues(mTapArray);
        mJinhuiPicker.refreshByNewDisplayedValues(mJinhuiArray);

        mStartBtn =(Button)findViewById(R.id.start_btn);
        mEndBtn = (Button)findViewById(R.id.end_btn);
        mStartBtn.setOnClickListener(mListener);
        mEndBtn.setOnClickListener(mListener);
    }

    private View.OnClickListener mListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()){
                case R.id.start_btn:
                    startActivity(new Intent(mContext, MeasureActivity.class));
                    break;
                case R.id.end_btn:
                    Toast.makeText(mContext, "End check!!" ,Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    };

    @Override
    public void onClick(View v) {

    }

    @Override
    public void onScrollStateChange(NumberPickerView view, int scrollState) {

    }

    @Override
    public void onValueChange(NumberPickerView picker, int oldVal, int newVal) {

    }

    @Override
    public void onValueChangeInScrolling(NumberPickerView picker, int oldVal, int newVal) {

    }

}
