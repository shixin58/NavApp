package com.passion.libcommon.dialog;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.Gravity;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;

import com.passion.libcommon.R;

// ProgressDialog已废弃。AlertDialog继承自AppCompatDialog
public class LoadingDialog extends AlertDialog {
    private TextView loadingTv;

    public LoadingDialog(@NonNull Context context) {
        super(context);
    }

    public LoadingDialog(@NonNull Context context, int themeResId) {
        super(context, themeResId);
    }

    public void setLoadingText(String loadingText) {
        if (loadingTv != null) {
            loadingTv.setText(loadingText);
        }
    }

    @Override
    public void show() {
        super.show();
        // 设置自定义布局若写在构造器或onCreate中，不能达到预期效果
        setContentView(R.layout.layout_loading_view);
        loadingTv = findViewById(R.id.loading_text);
        Window window = getWindow();
        WindowManager.LayoutParams attributes = window.getAttributes();
        attributes.width = WindowManager.LayoutParams.WRAP_CONTENT;
        attributes.height = WindowManager.LayoutParams.WRAP_CONTENT;
        attributes.gravity = Gravity.CENTER;
        attributes.dimAmount = 0.35f;
        window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        window.setAttributes(attributes);
    }
}
