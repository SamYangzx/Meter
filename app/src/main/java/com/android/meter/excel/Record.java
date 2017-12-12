package com.android.meter.excel;

import java.util.ArrayList;

/**
 * Created by fenghe on 2017/9/8.
 */

public class Record {
    private String mMeasurePointValue;
    private String mSampleValue;

    public Record(String mMeasurePointValue, String mSampleValue) {
        this.mMeasurePointValue = mMeasurePointValue;
        this.mSampleValue = mSampleValue;
    }

    public String getmMeasurePointValue() {
        return mMeasurePointValue;
    }

    public String getmSampleValue() {
        return mSampleValue;
    }

    public ArrayList<String> toArrayList() {
        ArrayList<String> list = new ArrayList<String>();
        list.add(mMeasurePointValue);
        list.add(mSampleValue);
        return list;
    }
}
