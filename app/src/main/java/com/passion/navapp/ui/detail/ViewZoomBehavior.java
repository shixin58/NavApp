package com.passion.navapp.ui.detail;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.OverScroller;

import androidx.annotation.NonNull;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.view.ViewCompat;
import androidx.customview.widget.ViewDragHelper;

import com.passion.libcommon.PixUtils;
import com.passion.navapp.R;
import com.passion.navapp.view.FullscreenPlayerView;

// Zoom Cloud Meetings, zoom in/out; Room数据库
// 处理跟手滑动效果用到
public class ViewZoomBehavior extends CoordinatorLayout.Behavior<FullscreenPlayerView> {
    private int mMinHeight;
    private int mScrollingId;

    private ViewDragHelper mViewDragHelper;
    private View mScrollingView;
    private FullscreenPlayerView mRefChild;
    private int mChildOriginalHeight;
    private boolean mCanFullscreen;

    private OverScroller mOverScroller;
    private ViewZoomCallback mViewZoomCallback;

    public ViewZoomBehavior() {}

    public ViewZoomBehavior(Context ctx, AttributeSet attrs) {
        TypedArray typedArray = ctx.obtainStyledAttributes(attrs, R.styleable.view_zoom_behavior, 0, 0);
        mMinHeight = typedArray.getDimensionPixelOffset(R.styleable.view_zoom_behavior_min_height, PixUtils.dp2Px(200f));
        mScrollingId = typedArray.getResourceId(R.styleable.view_zoom_behavior_scrolling_id, 0);
        typedArray.recycle();

        mOverScroller = new OverScroller(ctx);
    }

    @Override
    public boolean onLayoutChild(@NonNull CoordinatorLayout parent,
                                 @NonNull FullscreenPlayerView child,
                                 int layoutDirection) {
        // FullscreenPlayerView本身不支持滚动，通过ViewDragHelper实现滑动手势监听和派发
        if (mViewDragHelper == null) {
            mViewDragHelper = ViewDragHelper.create(parent, 1.0f, mCallback);
            this.mScrollingView = parent.findViewById(mScrollingId);
            this.mRefChild = child;
            this.mChildOriginalHeight = child.getMeasuredHeight();
            this.mCanFullscreen = mChildOriginalHeight > parent.getMeasuredWidth();
        }
        return super.onLayoutChild(parent, child, layoutDirection);
    }

    private final ViewDragHelper.Callback mCallback = new ViewDragHelper.Callback() {
        // 告诉ViewDragHelper，什么时候可以拦截手指触摸View的手势分发
        @Override
        public boolean tryCaptureView(@NonNull View child, int pointerId) {
            if (mCanFullscreen && mRefChild.getBottom() >= mMinHeight) {
                return true;
            }
            return false;
        }

        // 告诉ViewDragHelper，在屏幕上滑动多少距离才算拖拽
        @Override
        public int getViewVerticalDragRange(@NonNull View child) {
            // slop斜率
            return mViewDragHelper.getTouchSlop();
        }

        // clamp夹紧，紧紧抓住
        // 告诉ViewDragHelper，手指拖拽的这个View，本次滑动最终能够移动的距离
        @Override
        public int clampViewPositionVertical(@NonNull View child, int top, int dy) {
            // dy>0手指从屏幕从上往下滑动，dy<0手指从屏幕从下往上滑动
            if (mRefChild == null || dy==0)
                return 0;
            if ((dy<0 && mRefChild.getBottom()<mMinHeight)
                    ||(dy>0 && mRefChild.getBottom()>mChildOriginalHeight)
                    // 手指从上往下滑，评论列表未滑动到顶部，评论列表可以向下滑动，禁止PlayerView向下滑动
                    ||(dy>0 && mScrollingView!=null && mScrollingView.canScrollVertically(-1))) {
                // canScrollVertically传递负数，scroll up，向下滑动到顶部；传递正数，scroll down，向上滑动到底部
                return 0;
            }

            int maxConsumed;
            if (dy>0) {
                if (mRefChild.getBottom()+dy > mChildOriginalHeight) {
                    maxConsumed = mChildOriginalHeight - mRefChild.getBottom();
                } else {
                    maxConsumed = dy;
                }
            } else {
                if (mRefChild.getBottom()+dy < mMinHeight) {
                    maxConsumed = mMinHeight - mRefChild.getBottom();
                } else {
                    maxConsumed = dy;
                }
            }

            ViewGroup.LayoutParams layoutParams = mRefChild.getLayoutParams();
            layoutParams.height += maxConsumed;
            // 保证FullscreenPlayerView child等比缩放
            mRefChild.setLayoutParams(layoutParams);
            if (mViewZoomCallback != null) {
                mViewZoomCallback.onDragZoom(layoutParams.height);
            }
            return maxConsumed;
        }

        // 手指从屏幕离开时调用，实现惯性滑动效果
        @Override
        public void onViewReleased(@NonNull View releasedChild, float xVel, float yVel) {
            super.onViewReleased(releasedChild, xVel, yVel);
            if (mRefChild.getBottom()>mMinHeight && mRefChild.getBottom()<mChildOriginalHeight && yVel!=0) {
                FlingRunnable flingRunnable = new FlingRunnable(mRefChild);
                flingRunnable.fling(xVel, yVel);
            }
        }
    };

    @Override
    public boolean onTouchEvent(@NonNull CoordinatorLayout parent, @NonNull FullscreenPlayerView child, @NonNull MotionEvent ev) {
        if (!mCanFullscreen || mViewDragHelper==null) {
            return super.onTouchEvent(parent, child, ev);
        }
        mViewDragHelper.processTouchEvent(ev);
        return true;
    }

    @Override
    public boolean onInterceptTouchEvent(@NonNull CoordinatorLayout parent, @NonNull FullscreenPlayerView child, @NonNull MotionEvent ev) {
        if (!mCanFullscreen || mViewDragHelper==null) {
            return super.onInterceptTouchEvent(parent, child, ev);
        }
        return mViewDragHelper.shouldInterceptTouchEvent(ev);
    }

    public void setViewZoomCallback(ViewZoomCallback viewZoomCallback) {
        mViewZoomCallback = viewZoomCallback;
    }

    public interface ViewZoomCallback {
        void onDragZoom(int height);
    }

    private class FlingRunnable implements Runnable {
        private final View mFlingView;

        public FlingRunnable(View flingView) {
            mFlingView = flingView;
        }

        public void fling(float xVel, float yVel) {
            mOverScroller.fling(0, mFlingView.getBottom(), (int)xVel, (int)yVel,
                    0, Integer.MAX_VALUE, 0, Integer.MAX_VALUE);
            run();
        }

        @Override
        public void run() {
            ViewGroup.LayoutParams params = mFlingView.getLayoutParams();
            int height = params.height;
            // computeScrollOffset()表示本次滑动未结束
            if (mOverScroller.computeScrollOffset() && height > mMinHeight && height < mChildOriginalHeight) {
                int newHeight = Math.min(mOverScroller.getCurrY(), mChildOriginalHeight);
                if (height != newHeight) {
                    params.height = newHeight;
                    mFlingView.setLayoutParams(params);

                    if (mViewZoomCallback != null) {
                        mViewZoomCallback.onDragZoom(newHeight);
                    }
                }
                // 实现惯性连续滑动
                ViewCompat.postOnAnimation(mFlingView, this);
            } else {
                mFlingView.removeCallbacks(this);
            }
        }
    }
}
