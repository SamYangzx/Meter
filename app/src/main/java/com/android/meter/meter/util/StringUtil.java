package com.android.meter.meter.util;

import java.math.BigDecimal;
import java.text.NumberFormat;

/**
 * Created by fenghe on 2017/6/7.
 */

public class StringUtil {
    private static final int MAX_DIGIT = 4;

//    public static String big2(double d) {
//        BigDecimal d1 = new BigDecimal(Double.toString(d));
//        BigDecimal d2 = new BigDecimal(Integer.toString(1));
//        // 四舍五入,保留2位小数
//        return d1.divide(d2, 2, BigDecimal.ROUND_HALF_UP).toString();
//    }

    public static String getNumber(float number) {
        NumberFormat ddf1 = NumberFormat.getNumberInstance();
        ddf1.setMaximumFractionDigits(MAX_DIGIT);
        return ddf1.format(number);
    }
}
