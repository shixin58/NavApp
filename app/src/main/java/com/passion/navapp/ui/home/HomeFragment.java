package com.passion.navapp.ui.home;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.paging.PagedListAdapter;

import com.passion.libnavannotation.FragmentDestination;
import com.passion.navapp.model.Feed;
import com.passion.navapp.ui.AbsListFragment;
import com.scwang.smart.refresh.layout.api.RefreshLayout;

@FragmentDestination(pageUrl = "main/tabs/home",asStarter = true)
public class HomeFragment extends AbsListFragment<Feed,HomeViewModel> {
    private String feedType;

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mViewModel.setFeedType(feedType);
    }

    @Override
    public PagedListAdapter getAdapter() {
        Bundle bundle = getArguments();
        feedType = bundle==null?"all":bundle.getString("feedType");
        return new FeedAdapter(getContext(), feedType);
    }

    @Override
    public void onLoadMore(@NonNull RefreshLayout refreshLayout) {

    }

    @Override
    public void onRefresh(@NonNull RefreshLayout refreshLayout) {

    }
}