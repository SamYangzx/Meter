package com.android.meter.util;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by fenghe on 2017/6/5.
 */

public class TimeUtil {
    private static final String yyyyMMdd = "yyyyMMdd";
    private static final String yyyyMMdd_HHmmss = "yyyyMMdd_HHmmss";
    private static String TODAY_DATE = null;

    public static String getDateByFormat(String format) {
        SimpleDateFormat formatter = new SimpleDateFormat(format);
        Date current = new Date(System.currentTimeMillis());
        String dateString = formatter.format(current);
        return dateString;
    }

    public static String getDateYearMonthDay() {
        if (TODAY_DATE != null) {
            return TODAY_DATE;
        } else {
            TODAY_DATE = getDateByFormat(yyyyMMdd);
        }
        return TODAY_DATE;
    }

    public static String getDateYearMonthDayHourMinute() {
        return getDateByFormat(yyyyMMdd_HHmmss);
    }

}
