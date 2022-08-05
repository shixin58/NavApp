package com.passion.navapp.utils;

public class StringConverter {
    public static String convertFeedUgc(int count) {
        if (count < 0) {
            return String.valueOf(0);
        }
        if (count < 10000) {
            return String.valueOf(count);
        }
        return count/10000 + "万";
    }
}
