package com.android.meter.meter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import com.android.meter.meter.numberpicker.NumberPickerView;
import com.android.meter.meter.util.ToastUtil;

public class MeasureSetActivity extends Activity {
    private static final String TAG = MeasureSetActivity.class.getSimpleName();

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
    private Spinner mSampleUnitSp;
    private ArrayAdapter<String> mSampleAdapter;

    private int mUnitIndex = 0;
    private String mSampleUnit;
    private float mStep;
    private int mCount;

    @RequiresApi(api = Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getActionBar() != null) {
            getActionBar().setDisplayShowHomeEnabled(false);
        }
        setContentView(R.layout.activity_measure_set);
        mContext = this;
        initData();
        initView();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.measure_settings_menu, menu);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.bluetooth_settings:
                ToastUtil.showToast(mContext, R.string.bluetooth);
                return true;
            case R.id.unit_settings:
                ToastUtil.showToast(mContext, R.string.unit);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void initData() {
        mStepArray = getResources().getStringArray(R.array.step_array);
        mTapArray = getResources().getStringArray(R.array.tap_array);
        mCountArray = getResources().getStringArray(R.array.jinhui_array);
        mStepArray = getResources().getStringArray(R.array.step_array);

        mStep = Float.parseFloat(mStepArray[0]);
        mCount = Integer.valueOf(mCountArray[0]);

    }

    private void initView() {
        Spinner measureUnitSp = (Spinner) findViewById(R.id.measure_unit_spinner);
        measureUnitSp.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Log.d(TAG, "position: " + position + ", id: " + id);
                mUnitIndex = position;
                mSampleAdapter = new ArrayAdapter<String>(mContext, android.R.layout.simple_spinner_item, mUnitArrays[position]);
                mSampleAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                mSampleUnitSp.setAdapter(mSampleAdapter);
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
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        mStepPicker = (NumberPickerView) findViewById(R.id.step_picker);
        mTapPicker = (NumberPickerView) findViewById(R.id.tap_picker);
        mCountPicker = (NumberPickerView) findViewById(R.id.jinhui_picker);

        mStepPicker.refreshByNewDisplayedValues(mStepArray);
        mStepPicker.setOnValueChangedListener(new NumberPickerView.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPickerView picker, int oldVal, int newVal) {
                Log.d(TAG, "oldVal: " + oldVal + ", newVal: " + newVal + ", Value: " + mStepArray[newVal]);
                mStep = Float.parseFloat(mStepArray[newVal]);
            }
        });
        mTapPicker.refreshByNewDisplayedValues(mTapArray);
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

    private View.OnClickListener mListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.start_btn:
                    Intent intent = new Intent();
                    intent.putExtra(MeasureActivity.EXTRA_MEASURE_UNIT, mSampleUnit);
                    intent.putExtra(MeasureActivity.EXTRA_STEP, mStep);
                    Log.d(TAG, "send mStep: " + mStep);
                    intent.putExtra(MeasureActivity.EXTRA_COUNT, mCount);
                    intent.setClass(mContext, MeasureActivity.class);
                    startActivity(intent);
                    break;
                case R.id.end_btn:
                    Toast.makeText(mContext, "End check!!", Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    };


//    public void onSettingPressed(View view) {
//        Log.d(TAG, "Settings is pressed");
//    }

}
