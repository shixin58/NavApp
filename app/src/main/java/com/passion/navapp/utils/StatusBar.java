package com.passion.navapp.utils;

import android.app.Activity;
import android.graphics.Color;
import android.os.Build;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

// 沉浸式布局：状态栏背景色跟内容区域主体色一致，且状态栏字体颜色可修改
// 5.0仅能修改状态栏背景色，6.0增加修改字体色
public class StatusBar {
    public static void fitSystemBar(Activity activity) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) return;

        Window window = activity.getWindow();
        View decorView = window.getDecorView();
        // View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN: 页面布局延伸到状态栏之下
        // View.SYSTEM_UI_FLAG_FULLSCREEN：隐藏状态栏，类似WindowManager.LayoutParams.FLAG_FULLSCREEN
        // View.SYSTEM_UI_FLAG_LAYOUT_STABLE: 应对底部虚拟导航栏显示/隐藏切换重新layout的情况
        // View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR：状态栏白底黑字
        decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                |View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                |View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);

        // FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS: 允许Window对状态栏开启绘制
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.setStatusBarColor(Color.TRANSPARENT);
    }
}
