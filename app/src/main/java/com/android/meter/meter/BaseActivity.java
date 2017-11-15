package com.android.meter.meter;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.widget.TextView;

import com.android.meter.meter.http.IHttpListener;

import java.util.ArrayList;
import java.util.List;

public class BaseActivity extends AppCompatActivity implements IHttpListener {

    String mBtStr = "", mWifiStr = "";
    TextView mTitle;
    Context mContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_base);
        mContext = this;
        // 添加Activity到堆栈
        AtyContainer.getInstance().addActivity(this);
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
        if (connect) {
            mWifiStr = mContext.getString(R.string.title_wifi_connected);
        } else {
            mWifiStr = mContext.getString(R.string.title_wifi_not_connected);
        }
        mTitle.setText(mBtStr + "/" + mWifiStr);
    }

    public boolean checkPermission(@NonNull String permission) {
        return ActivityCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED;
    }

    @Override
    public void onResult(int state, String data) {

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // 结束Activity&从栈中移除该Activity
        AtyContainer.getInstance().removeActivity(this);
    }

}

class AtyContainer {

    private AtyContainer() {
    }

    private static AtyContainer instance = new AtyContainer();
    private static List<Activity> activityStack = new ArrayList<Activity>();

    public static AtyContainer getInstance() {
        return instance;
    }

    public void addActivity(Activity aty) {
        activityStack.add(aty);
    }

    public void removeActivity(Activity aty) {
        activityStack.remove(aty);
    }

    /**
     * 结束所有Activity
     */
    public void finishAllActivity() {
        for (int i = 0, size = activityStack.size(); i < size; i++) {
            if (null != activityStack.get(i)) {
                activityStack.get(i).finish();
            }
        }
        activityStack.clear();
        System.exit(0);
    }

}
