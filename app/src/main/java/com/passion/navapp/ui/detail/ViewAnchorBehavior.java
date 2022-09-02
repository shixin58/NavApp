package com.passion.navapp.ui.detail;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.coordinatorlayout.widget.CoordinatorLayout;

import com.passion.libcommon.PixUtils;
import com.passion.navapp.R;

public class ViewAnchorBehavior extends CoordinatorLayout.Behavior<View> {
    private int mExtraUsed;
    private int mAnchorId;

    public ViewAnchorBehavior() {

    }

    // 供布局文件引用Behavior时使用
    public ViewAnchorBehavior(Context ctx, AttributeSet attrs) {
        TypedArray typedArray = ctx.obtainStyledAttributes(attrs, R.styleable.view_anchor_behavior, 0, 0);
        mAnchorId = typedArray.getResourceId(R.styleable.view_anchor_behavior_anchorId, 0);
        typedArray.recycle();
        mExtraUsed = PixUtils.dp2Px(48);// 底部互动区域高度，防止RecyclerView最后一个item被底部互动区域盖住
    }

    @Override
    public boolean layoutDependsOn(@NonNull CoordinatorLayout parent, @NonNull View child, @NonNull View dependency) {
        return mAnchorId == dependency.getId();
    }

    // CoordinatorLayout在测量每个child前，会调用该方法。若返回true，不再测量child，而是使用我们提供的测量值去摆放child位置。
    @Override
    public boolean onMeasureChild(@NonNull CoordinatorLayout parent,
                                  @NonNull View child,
                                  int parentWidthMeasureSpec,
                                  int widthUsed,
                                  int parentHeightMeasureSpec,
                                  int heightUsed) {
        View anchorView = parent.findViewById(mAnchorId);
        if (anchorView == null) {
            return false;
        }
        CoordinatorLayout.LayoutParams layoutParams = (CoordinatorLayout.LayoutParams) child.getLayoutParams();
        int topMargin = layoutParams.topMargin;
        int bottom = anchorView.getBottom();
        heightUsed = bottom + topMargin + mExtraUsed;
        parent.onMeasureChild(child, parentWidthMeasureSpec, 0, parentHeightMeasureSpec, heightUsed);
        return true;
    }

    // CoordinatorLayout在摆放每个child前，会调用该方法。若返回true，不再摆放child。
    @Override
    public boolean onLayoutChild(@NonNull CoordinatorLayout parent, @NonNull View child, int layoutDirection) {
        View anchorView = parent.findViewById(mAnchorId);
        if (anchorView == null) {
            return false;
        }
        return super.onLayoutChild(parent, child, layoutDirection);
    }
}
