package com.passion.navapp.ui.home;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.paging.ItemKeyedDataSource;
import androidx.paging.PagedList;
import androidx.paging.PagedListAdapter;

import com.passion.libnavannotation.FragmentDestination;
import com.passion.navapp.exoplayer.PageListPlayDetector;
import com.passion.navapp.model.Feed;
import com.passion.navapp.ui.AbsListFragment;
import com.passion.navapp.ui.MutablePageKeyedDataSource;
import com.scwang.smart.refresh.layout.api.RefreshLayout;

import java.util.List;

@FragmentDestination(pageUrl = "main/tabs/home",asStarter = true)
public class HomeFragment extends AbsListFragment<Feed,HomeViewModel> {
    private String mFeedType;
    private PageListPlayDetector mPlayDetector;

    private boolean mShouldPause = true;

    public static HomeFragment getInstance(String feedType) {
        HomeFragment fragment = new HomeFragment();
        Bundle bundle = new Bundle();
        bundle.putString("feedType", feedType);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    protected void afterViewCreated() {
        // onViewCreated()之后订阅cacheLiveData
        mViewModel.getCacheLiveData().observe(this, feeds -> {
            mAdapter.submitList(feeds);
        });
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mViewModel.setFeedType(mFeedType);
        mPlayDetector = new PageListPlayDetector(this, mRecyclerView);
    }

    @Override
    public PagedListAdapter createAdapter() {
        Bundle bundle = getArguments();
        mFeedType = bundle==null?"all":bundle.getString("feedType");
        return new FeedAdapter(getContext(), mFeedType) {

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
                boolean isVideo = feed.itemType == Feed.TYPE_VIDEO;
                mShouldPause = !isVideo;
            }

            @Override
            public void onCurrentListChanged(@Nullable PagedList<Feed> previousList, @Nullable PagedList<Feed> currentList) {
                // 每提交PagedList到PagedListAdapter，即PagedListAdapter#submitList(PagedList)，触发一次PagedListAdapter#onCurrentListChanged()
                if (previousList!=null && currentList!=null) {
                    if (!currentList.containsAll(previousList)) {
                        // fix下拉刷新顶部插入一条新数据mAdapter.notifyItemInserted(0)，未显示出来
                        mRecyclerView.scrollToPosition(0);
                    }
                }
            }
        };
    }

    @Override
    public void onLoadMore(@NonNull RefreshLayout refreshLayout) {
        final PagedList<Feed> currentList = mAdapter.getCurrentList();
        if (currentList == null || currentList.isEmpty()) {
            finishRefresh(false);
            return;
        }
        // 手动上拉加载更多，解决paging某次no data就禁用加载更多的问题
        Feed lastFeed = currentList.get(mAdapter.getItemCount() - 1);
        mViewModel.loadAfter(lastFeed.id, new ItemKeyedDataSource.LoadCallback<Feed>(){
            @Override
            public void onResult(@NonNull List<Feed> data) {
                PagedList.Config config = mAdapter.getCurrentList().getConfig();
                if (!data.isEmpty()) {
                    MutablePageKeyedDataSource<Integer,Feed> dataSource = new MutablePageKeyedDataSource<>();
                    dataSource.data.addAll(mAdapter.getCurrentList());
                    dataSource.data.addAll(data);
                    PagedList<Feed> pagedList = dataSource.buildNewPagedList(config);
                    submitList(pagedList);
                }
            }
        });
    }

    @Override
    public void onRefresh(@NonNull RefreshLayout refreshLayout) {
        // 手动下拉刷新，触发页面初始化数据加载
        // 1）首次调用compute()时注册InvalidatedCallback: dataSourceFactory.create()->mDataSource.addInvalidatedCallback(mCallback)
        // 2）DataSource#invalidate()触发InvalidatedCallback#onInvalidated()->ComputableLiveData#invalidate()
        // 3）当有活跃的观察者时触发compute()，之后Paging会重新创建一个DataSource和PagedList, 在PagedList()调用DataSource#loadInitial()方法加载初始化数据
        // 详情见LivePagedListBuilder#compute()方法
        mViewModel.getDataSource().invalidate();
    }

    @Override
    public void onResume() {
        super.onResume();
        mShouldPause = true;
        if (getParentFragment() != null) {
            if (getParentFragment().isVisible() && isVisible()) {
                mPlayDetector.onResume();
            }
        } else {
            if (isVisible()) {
                mPlayDetector.onResume();
            }
        }
    }

    @Override
    public void onPause() {
        if (mShouldPause) {
            mPlayDetector.onPause();
        }
        super.onPause();
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        // 切换底部tab时，控制视频暂停/恢复播放
        if (hidden) {
            mPlayDetector.onPause();
        } else {
            mPlayDetector.onResume();
        }
    }
}