package com.passion.navapp.utils;

import java.util.Calendar;

public class TimeUtils {
    public static String calculate(long createTimeMillis) {
        long currentTimeMillis = Calendar.getInstance().getTimeInMillis();
        long diff = (currentTimeMillis - createTimeMillis) / 1000;
        if (diff < 60) {
            return diff + "秒前";
        } else if (diff < 3600) {
            return diff/60 + "分钟前";
        } else if (diff < 3600*24) {
            return diff/3600 + "小时前";
        } else {
            return diff/(3600 * 24) + "天前";
        }
    }
}
