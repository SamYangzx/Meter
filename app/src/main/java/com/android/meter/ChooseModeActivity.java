package com.android.meter;

import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.View;

import android.content.Intent;
import android.widget.TextView;
import android.widget.Button;

import com.android.meter.bluetooth.BluetoothHelper;
import com.android.meter.general_ui.CustomToastDialog;
import com.android.meter.util.CommandUtil;
import com.android.meter.util.FileUtil;
import com.android.meter.util.LogUtil;
import com.android.meter.util.StringUtil;
import com.android.meter.util.ToastUtil;
import com.lzy.imagepicker.util.FlagUtils;

public class ChooseModeActivity extends BaseActivity {
    private static final String TAG = LogUtil.COMMON_TAG + "ChooseModeActivity";

    private TextView mSampleTv;
    private TextView mSampleUnitTv;
    private Button mABtn;
    private Button mBBtn;
    private Button mResetBtn;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_mode);
        initTitle();
        initView();
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        menu.findItem(R.id.unit_settings).setVisible(false);
        menu.findItem(R.id.debug).setVisible(false);
        menu.findItem(R.id.about).setVisible(false);
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.measure_settings_menu, menu);
        return true;
    }

    @Override
    public void onBackPressed() {
//        super.onBackPressed();
        AtyContainer.getInstance().finishAllActivity();
    }

    private void initTitle() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.measure_set_toolbar);
        TextView customerTitle = (TextView) findViewById(R.id.customer_title);
        setTitleTv(customerTitle);
        setSupportActionBar(toolbar);
    }

    private void initView() {
        mSampleTv = (TextView) findViewById(R.id.measure_value_textView);
        mSampleUnitTv = (TextView) findViewById(R.id.measure_unit_textView);
        mABtn = (Button) findViewById(R.id.button_a);
        mABtn.setOnClickListener(mListener);
        mBBtn = (Button) findViewById(R.id.button_b);
        mBBtn.setOnClickListener(mListener);
        mResetBtn = (Button) findViewById(R.id.button_reset);
        mResetBtn.setOnClickListener(mListener);
    }

    private View.OnClickListener mListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {

            int id = view.getId();
            switch (id) {
                case R.id.button_a:
                    chooseModeA(true);
                    break;
                case R.id.button_b:
                    chooseModeA(false);
                    break;
                case R.id.button_reset:
                    resetDialog();
                default:
                    break;
            }
        }
    };

    private void chooseModeA(boolean modeA) {
        ((MainApplication) getApplication()).setModeA(modeA);
        FlagUtils.setModeA(modeA);
        LogUtil.setLogFolder(FlagUtils.getFolderPath());
        FileUtil.setFolder(LogUtil.LOG_FOLDER);
        if (FlagUtils.iSModeA()) {
            sendCmd(CommandUtil.getSocketDataCmd(CommandUtil.MODE_A), true, true);
        } else {
            sendCmd(CommandUtil.getSocketDataCmd(CommandUtil.MODE_B), true, false);
        }
        Intent intent = new Intent();
        intent.setClass(ChooseModeActivity.this, CameraActivity.class);
        startActivity(intent);
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

    @Override
    public void handleReceiveData(String sampleValue) {
        int divideIndex = StringUtil.getValueUnitIndex(sampleValue);
        String s = StringUtil.getStrWithoutFront0(StringUtil.hex2String(sampleValue.substring(0, divideIndex)));
        String unit = StringUtil.getFormatUnit(StringUtil.hex2String(sampleValue.substring(divideIndex)));
        mSampleTv.setText(s);
        mSampleUnitTv.setText(unit);
    }

}
