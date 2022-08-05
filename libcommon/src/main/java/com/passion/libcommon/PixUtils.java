package com.passion.libcommon;

import android.util.DisplayMetrics;

public class PixUtils {
    public static int dp2Px(float dpVal) {
        DisplayMetrics metrics = AppGlobals.getApplication().getResources().getDisplayMetrics();
        return (int) (metrics.density * dpVal + 0.5f);
    }

    public static int getScreenWidth() {
        DisplayMetrics metrics = AppGlobals.getApplication().getResources().getDisplayMetrics();
        return metrics.widthPixels;
    }

    public static int getScreenHeight() {
        DisplayMetrics metrics = AppGlobals.getApplication().getResources().getDisplayMetrics();
        return metrics.heightPixels;
    }
}
