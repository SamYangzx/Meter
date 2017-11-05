package com.android.meter.meter;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.widget.TextView;

public class BaseActivity extends AppCompatActivity {

    String mBtStr = "", mWifiStr = "";
    TextView mTitle;
    Context mContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_base);
        mContext = this;
    }

    void setTitleTv(TextView textView) {
        mTitle = textView;
    }

    void updateBtTitle(String bt) {
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
        if (!TextUtils.isEmpty(wifi)) {
            mWifiStr = wifi;
        }
        mTitle.setText(mBtStr + "/" + mWifiStr);
    }

    void updateWifiTitle(boolean connect) {
        if(connect){
        mWifiStr = mContext.getString(R.string.title_wifi_connected);
        }else{
            mWifiStr = mContext.getString(R.string.title_wifi_not_connected);
        }
        mTitle.setText(mBtStr + "/" + mWifiStr);
    }

}
