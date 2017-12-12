package com.android.meter.http;

/**
 * Created by fenghe on 2017/6/28.
 */

public interface IHttpListener {
    void onResult(int state, String data);
}
