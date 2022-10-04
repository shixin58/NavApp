package com.passion.navapp.ui.my;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.paging.PagedListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.passion.navapp.exoplayer.PageListPlayDetector;
import com.passion.navapp.model.Feed;
import com.passion.navapp.ui.AbsListFragment;
import com.scwang.smart.refresh.layout.api.RefreshLayout;

public class ProfileListFragment extends AbsListFragment<Feed,ProfileViewModel> {
    @ProfileActivity.TabType private String mTabType;
    private PageListPlayDetector mPlayDetector;

    private boolean mShouldPause = true;

    public static ProfileListFragment newInstance(@ProfileActivity.TabType String tabType) {
        Bundle args = new Bundle();
        args.putString(ProfileActivity.KEY_TAB_TYPE, tabType);
        ProfileListFragment fragment = new ProfileListFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    protected void afterViewCreated() {
        mPlayDetector = new PageListPlayDetector(this, mRecyclerView);
        mViewModel.setProfileType(mTabType);
        mRefreshLayout.setEnableRefresh(false);// 禁掉下拉刷新
    }

    @Override
    public PagedListAdapter<Feed, RecyclerView.ViewHolder> createAdapter() {
        mTabType = getArguments().getString(ProfileActivity.KEY_TAB_TYPE);
        return new ProfileListAdapter(getContext(), mTabType) {
            @Override
            public void onViewAttachedToWindow2(ViewHolder holder) {
                if (holder.isVideoItem()) {
                    mPlayDetector.addTarget(holder.getListPlayerView());
                }
            }

            @Override
            public void onViewDetachedFromWindow2(ViewHolder holder) {
                if (holder.isVideoItem()) {
                    mPlayDetector.removeTarget(holder.getListPlayerView());
                }
            }

            @Override
            protected void onStartFeedDetailActivity(Feed feed) {
                super.onStartFeedDetailActivity(feed);
            }
        };
    }

    @Override
    public void onResume() {
        super.onResume();
        mPlayDetector.onResume();
    }

    @Override
    public void onPause() {
        if (mShouldPause) {
            mPlayDetector.onPause();
        }
        super.onPause();
    }

    @Override
    public void onLoadMore(@NonNull RefreshLayout refreshLayout) {}

    @Override
    public void onRefresh(@NonNull RefreshLayout refreshLayout) {}
}
