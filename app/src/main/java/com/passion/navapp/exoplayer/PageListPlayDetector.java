package com.passion.navapp.exoplayer;

import android.util.Pair;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleEventObserver;
import androidx.lifecycle.LifecycleOwner;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

// 列表滚动时视频item自动播放逻辑
public class PageListPlayDetector {
    private final List<IPlayTarget> mTarget = new ArrayList<>();
    private IPlayTarget mPlayingTarget;

    private RecyclerView mRecyclerView;
    private Pair<Integer,Integer> rvLocation = null;

    public PageListPlayDetector(LifecycleOwner lifecycleOwner, RecyclerView recyclerView) {
        mRecyclerView = recyclerView;

        lifecycleOwner.getLifecycle().addObserver(new LifecycleEventObserver() {
            @Override
            public void onStateChanged(@NonNull LifecycleOwner source, @NonNull Lifecycle.Event event) {
                if (event == Lifecycle.Event.ON_DESTROY) {
                    mPlayingTarget = null;
                    mTarget.clear();
                    recyclerView.getAdapter().unregisterAdapterDataObserver(mDataObserver);
                    lifecycleOwner.getLifecycle().removeObserver(this);
                }
            }
        });
        recyclerView.getAdapter().registerAdapterDataObserver(mDataObserver);

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    autoPlay();
                }
            }

            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                if (mPlayingTarget != null && mPlayingTarget.isPlaying() && !isTargetInBounds(mPlayingTarget)) {
                    mPlayingTarget.inactive();
                }
            }
        });
    }

    public void addTarget(IPlayTarget target) {
        // 列表上下滚动时，有新item出现在屏幕上，如果是视频则添加
        mTarget.add(target);
    }

    public void removeTarget(IPlayTarget target) {
        mTarget.remove(target);
    }

    // 监听有新数据插入列表，触发自动播放检测逻辑
    private final RecyclerView.AdapterDataObserver mDataObserver = new RecyclerView.AdapterDataObserver() {
        @Override
        public void onItemRangeInserted(int positionStart, int itemCount) {
            autoPlay();
        }
    };

    private void autoPlay() {
        if (mTarget.isEmpty() || mRecyclerView.getChildCount()<=0) { return; }

        if (mPlayingTarget != null && mPlayingTarget.isPlaying() && isTargetInBounds(mPlayingTarget)) {
            return;
        }

        IPlayTarget activeTarget = null;
        for (IPlayTarget target : mTarget) {
            boolean inBounds = isTargetInBounds(target);
            if (inBounds) {
                activeTarget = target;
                break;
            }
        }

        if (activeTarget != null) {
            if (mPlayingTarget != null && mPlayingTarget.isPlaying()) {
                mPlayingTarget.inactive();
            }
            mPlayingTarget = activeTarget;
            mPlayingTarget.onActive();
        }
    }

    // 检测是否PlayerView有一半在屏幕内
    private boolean isTargetInBounds(IPlayTarget target) {
        ViewGroup owner = target.getOwner();
        ensureRecyclerViewLocation();
        if (!owner.isShown() || !owner.isAttachedToWindow()) { return false; }

        int[] location = new int[2];
        owner.getLocationOnScreen(location);
        int centerY = location[1] + owner.getHeight()/2;
        return centerY >= rvLocation.first && centerY <= rvLocation.second;
    }

    private Pair<Integer,Integer> ensureRecyclerViewLocation() {
        if (rvLocation == null) {
            int[] location = new int[2];
            mRecyclerView.getLocationOnScreen(location);

            int top = location[1];
            int bottom = location[1] + mRecyclerView.getHeight();

            rvLocation = new Pair<>(top, bottom);
        }
        return rvLocation;
    }

    public void onPause() {
        if (mPlayingTarget != null) {
            mPlayingTarget.inactive();
        }
    }

    public void onResume() {
        if (mPlayingTarget != null) {
            mPlayingTarget.onActive();
        }
    }
}
