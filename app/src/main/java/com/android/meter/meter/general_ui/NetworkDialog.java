package com.android.meter.meter.general_ui;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.android.meter.meter.R;
import com.android.meter.meter.http.HTTPConstant;
import com.android.meter.meter.util.SharedPreferenceUtils;

/**
 * Created by fenghe on 2017/6/17.
 */

public class NetworkDialog extends Dialog {
    private Button yes;//确定按钮
    private Button no;//取消按钮
    private TextView mServerTv;
    private TextView mPortTv;
    private EditText mServerEt;
    private EditText mPortEt;
    private String titleStr;
    private String messageStr;//从外界设置的消息文本
    private String yesStr, noStr;

    private onCancelclickListener noOnclickListener;//取消按钮被点击了的监听器
    private onEnterclickListener yesOnclickListener;//确定按钮被点击了的监听器
    private Context mContext;

    /**
     * 设置取消按钮的显示内容和监听
     *
     * @param str
     * @param onNoOnclickListener
     */
    public void setNoOnclickListener(String str, onCancelclickListener onNoOnclickListener) {
        if (str != null) {
            noStr = str;
        }
        this.noOnclickListener = onNoOnclickListener;
    }

    public void setNoOnclickListener(onCancelclickListener onNoOnclickListener) {
        this.setNoOnclickListener(null, onNoOnclickListener);
    }

    public void setYesOnclickListener(onEnterclickListener onYesOnclickListener) {
        this.setYesOnclickListener(null, onYesOnclickListener);
    }

    /**
     * 设置确定按钮的显示内容和监听
     *
     * @param str
     * @param onYesOnclickListener
     */
    public void setYesOnclickListener(String str, onEnterclickListener onYesOnclickListener) {
        if (str != null) {
            yesStr = str;
        }
        this.yesOnclickListener = onYesOnclickListener;
    }

    public NetworkDialog(Context context) {
        super(context, R.style.MyDialog);
        mContext = context;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.network_dialog);

        //按空白处不能取消动画
        setCanceledOnTouchOutside(false);

        //初始化界面控件
        initView();
        //初始化界面数据
        initData();
        //初始化界面控件的事件
        initEvent();

    }

    /**
     * 初始化界面的确定和取消监听器
     */
    private void initEvent() {
        //设置确定按钮被点击后，向外界提供监听
        yes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (yesOnclickListener != null) {
                    yesOnclickListener.onYesClick();
                }
            }
        });
        //设置取消按钮被点击后，向外界提供监听
        no.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (noOnclickListener != null) {
                    noOnclickListener.onNoClick();
                }
            }
        });
    }

    /**
     * 初始化界面控件的显示数据
     */
    private void initData() {
        //如果用户自定了title和message
        if (titleStr != null) {
            mServerTv.setText(titleStr);
        }
        if (messageStr != null) {
            mServerEt.setText(messageStr);
        }
        //如果设置按钮的文字
        if (yesStr != null) {
            yes.setText(yesStr);
        }
        if (noStr != null) {
            no.setText(noStr);
        }
    }

    public String getServer() {
        if (TextUtils.isEmpty(mServerEt.getText().toString())) {
            return HTTPConstant.DEFAULT_SERVER;
        }
        return mServerEt.getText().toString();
    }

    public int getIp() {
        if (TextUtils.isEmpty(mPortEt.getText().toString())) {
            return HTTPConstant.DEFAULT_PORT;
        }
        return Integer.valueOf(mPortEt.getText().toString());
    }


    /**
     * 初始化界面控件
     */
    private void initView() {
        yes = (Button) findViewById(R.id.yes);
        no = (Button) findViewById(R.id.no);
        mServerTv = (TextView) findViewById(R.id.server_toast);
        mServerEt = (EditText) findViewById(R.id.server_et);
//        mServerEt.setText(HTTPConstant.DEFAULT_SERVER);
        mServerEt.setText((String) SharedPreferenceUtils.getParam(mContext, HTTPConstant.SAVE_IP, HTTPConstant.DEFAULT_SERVER));
        mPortTv = (TextView) findViewById(R.id.port_toast);
        mPortEt = (EditText) findViewById(R.id.port_et);
//        mPortEt.setText(Integer.toString(HTTPConstant.DEFAULT_PORT));
        mPortEt.setText(Integer.toString((int) SharedPreferenceUtils.getParam(mContext, HTTPConstant.SAVE_PORT, HTTPConstant.DEFAULT_PORT)));
    }

    /**
     * 从外界Activity为Dialog设置标题
     *
     * @param title
     */
    public void setTitle(String title) {
        titleStr = title;
    }

    /**
     * 从外界Activity为Dialog设置dialog的message
     *
     * @param message
     */
    public void setMessage(String message) {
        messageStr = message;
    }

    /**
     * 设置确定按钮和取消被点击的接口
     */
    public interface onEnterclickListener {
        public void onYesClick();
    }

    public interface onCancelclickListener {
        public void onNoClick();
    }
}
