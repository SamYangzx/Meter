package com.android.meter.meter.general_ui;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.android.meter.meter.R;
import com.android.meter.meter.util.CommandUtil;

import static android.content.ContentValues.TAG;

/**
 * Created by fenghe on 2017/6/17.
 */

public class CustomToastDialog extends Dialog {
    private Context mContext;
    private Button yes;//确定按钮
    private Button no;//取消按钮
    private TextView titleTv;//消息标题文本
    private TextView messageTv;//消息提示文本
    private String titleStr;//从外界设置的title文本
    private int mMessageId;//从外界设置的消息文本
    private String yesStr, noStr;

    private onCancelclickListener noOnclickListener;//取消按钮被点击了的监听器
    private onEnterclickListener yesOnclickListener;//确定按钮被点击了的监听器


    /**
     * 设置取消按钮的显示内容和监听
     *
     * @param str
     * @param onNoOnclickListener
     */
    public void setNegativeButton(String str, onCancelclickListener onNoOnclickListener) {
        if (str != null) {
            noStr = str;
        }
        this.noOnclickListener = onNoOnclickListener;
    }

    public void setNegativeButton(onCancelclickListener onNoOnclickListener) {
        this.setNegativeButton(null, onNoOnclickListener);
    }

    public void setNegativeButton(int id, onCancelclickListener onNoOnclickListener) {
        noStr = (String) mContext.getString(id);
        this.noOnclickListener = onNoOnclickListener;
    }

    public void setPositiveButton(onEnterclickListener onYesOnclickListener) {
        this.setPositiveButton(null, onYesOnclickListener);
    }

    /**
     * 设置确定按钮的显示内容和监听
     *
     * @param str
     * @param onYesOnclickListener
     */
    public void setPositiveButton(String str, onEnterclickListener onYesOnclickListener) {
        if (str != null) {
            yesStr = str;
        }
        this.yesOnclickListener = onYesOnclickListener;
    }

    public void setPositiveButton(int id, onEnterclickListener onYesOnclickListener) {
        yesStr = (String) mContext.getText(id);
        this.yesOnclickListener = onYesOnclickListener;
    }

    public CustomToastDialog(Context context) {
        super(context, R.style.MyDialog);
        mContext = context;

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.custom_toast_dialog);
        //按空白处不能取消动画
        setCanceledOnTouchOutside(false);

        //初始化界面控件
        initView();
//        初始化界面数据
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
            titleTv.setText(titleStr);
        }
        if (mMessageId != 0) {
            messageTv.setText(mMessageId);
        }
        //如果设置按钮的文字
        if (yesStr != null) {
            yes.setText(yesStr);
        }
        if (noStr != null) {
            no.setText(noStr);
        }
    }

    public String getmMessageId() {
        if (TextUtils.isEmpty(messageTv.getText().toString())) {
            return CommandUtil.TEST_HEX_CMD;
        }
        return messageTv.getText().toString();
    }

    /**
     * 初始化界面控件
     */
    private void initView() {
        Log.d(TAG, " initVIew is invoked...");
        yes = (Button) findViewById(R.id.yes);
        no = (Button) findViewById(R.id.no);
        titleTv = (TextView) findViewById(R.id.title);
        messageTv = (TextView) findViewById(R.id.message);
    }

    /**
     * 从外界Activity为Dialog设置标题
     *
     * @param title
     */
    public void setTitle(String title) {
        titleStr = title;
    }


    public void setMessage(int message) {
        mMessageId = message;
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
