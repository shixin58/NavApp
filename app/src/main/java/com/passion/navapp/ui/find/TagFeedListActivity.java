package com.passion.navapp.ui.find;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.paging.PagedList;
import androidx.paging.PagedListAdapter;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.passion.libcommon.view.EmptyView;
import com.passion.libcommon.PixUtils;
import com.passion.libcommon.extension.AbsPagedListAdapter;
import com.passion.libcommon.utils.StatusBar;
import com.passion.navapp.R;
import com.passion.navapp.databinding.ActivityTagFeedListBinding;
import com.passion.navapp.databinding.LayoutTagFeedListHeaderBinding;
import com.passion.navapp.exoplayer.PageListPlayDetector;
import com.passion.navapp.exoplayer.PageListPlayManager;
import com.passion.navapp.model.Feed;
import com.passion.navapp.model.FeedTag;
import com.passion.navapp.ui.home.FeedAdapter;
import com.scwang.smart.refresh.layout.SmartRefreshLayout;
import com.scwang.smart.refresh.layout.api.RefreshLayout;
import com.scwang.smart.refresh.layout.constant.RefreshState;
import com.scwang.smart.refresh.layout.listener.OnLoadMoreListener;
import com.scwang.smart.refresh.layout.listener.OnRefreshListener;

/**
 * 标签帖子列表页
 */
public class TagFeedListActivity extends AppCompatActivity implements View.OnClickListener, OnRefreshListener, OnLoadMoreListener {
    public static final String KEY_FEED_TAG = "key_feed_tag";
    public static final String VALUE_FEED_TYPE = "tag_feed_list";

    private ActivityTagFeedListBinding mBinding;

    private RecyclerView mRecyclerView;
    private SmartRefreshLayout mRefreshLayout;
    private EmptyView mEmptyView;

    private FeedTag mFeedTag;
    private PageListPlayDetector mPlayDetector;
    private boolean mShouldPause = true;
    private AbsPagedListAdapter mAdapter;
    private int mTotalScrollY;
    private TagFeedListViewModel mViewModel;

    public static void openActivity(Context ctx, FeedTag feedTag) {
        Intent intent = new Intent(ctx, TagFeedListActivity.class);
        intent.putExtra(KEY_FEED_TAG, feedTag);
        ctx.startActivity(intent);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        StatusBar.fitSystemBar(this);
        super.onCreate(savedInstanceState);
        mBinding = ActivityTagFeedListBinding.inflate(getLayoutInflater());
        setContentView(mBinding.getRoot());

        mRecyclerView = mBinding.refreshLayout.recyclerView;
        mRefreshLayout = mBinding.refreshLayout.refreshLayout;
        mEmptyView = mBinding.refreshLayout.emptyView;

        mBinding.actionBack.setOnClickListener(this);

        mBinding.setOwner(this);
        mFeedTag = (FeedTag) getIntent().getSerializableExtra(KEY_FEED_TAG);
        mBinding.setFeedTag(mFeedTag);

        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        mAdapter = (AbsPagedListAdapter) createAdapter();
        mRecyclerView.setAdapter(mAdapter);
        mPlayDetector = new PageListPlayDetector(this, mRecyclerView);
        addHeaderView();

        mViewModel = ViewModelProviders.of(this).get(TagFeedListViewModel.class);
        mViewModel.setFeedType(mFeedTag.title);
        mViewModel.getPageData().observe(this, new Observer<PagedList<Feed>>() {
            @Override
            public void onChanged(PagedList<Feed> feeds) {
                submitList(feeds);
            }
        });

        mRefreshLayout.setOnRefreshListener(this);
        mRefreshLayout.setOnLoadMoreListener(this);
    }

    private void submitList(PagedList<Feed> feeds) {
        if (feeds.size() > 0) {
            mAdapter.submitList(feeds);
        }
        finishRefresh(feeds.size() > 0);
    }

    private void finishRefresh(boolean hasData) {
        PagedList currentList = mAdapter.getCurrentList();
        hasData = hasData || (currentList != null && !currentList.isEmpty());
        mEmptyView.setVisibility(hasData?View.GONE:View.VISIBLE);

        RefreshState refreshState = mRefreshLayout.getState();
        if (refreshState.isOpening && refreshState.isHeader) {
            mRefreshLayout.finishRefresh();
        } else if (refreshState.isOpening && refreshState.isFooter) {
            mRefreshLayout.finishLoadMore();
        }
    }

    public PagedListAdapter createAdapter() {
        return new FeedAdapter(this, VALUE_FEED_TYPE) {
            @Override
            public void onViewAttachedToWindow(@NonNull RecyclerView.ViewHolder holder) {
                super.onViewAttachedToWindow(holder);
                if (holder instanceof FeedAdapter.ViewHolder) {
                    FeedAdapter.ViewHolder normalHolder = (ViewHolder) holder;
                    if (normalHolder.isVideoItem()) {
                        mPlayDetector.addTarget(normalHolder.getListPlayerView());
                    }
                }
            }

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

    private void addHeaderView() {
        LayoutTagFeedListHeaderBinding headerBinding = LayoutTagFeedListHeaderBinding.inflate(
                LayoutInflater.from(this), mRecyclerView, false);
        headerBinding.setOwner(this);
        headerBinding.setFeedTag(mFeedTag);
        mAdapter.addHeaderView(headerBinding.getRoot());

        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                mTotalScrollY += dy;
                if (mTotalScrollY > PixUtils.dp2Px(48f)) {
                    mBinding.tagLogo.setVisibility(View.VISIBLE);
                    mBinding.tagTitle.setVisibility(View.VISIBLE);
                    mBinding.topBarFollow.setVisibility(View.VISIBLE);
                    mBinding.actionBack.setImageResource(R.drawable.icon_back_black);
                    mBinding.topLine.setVisibility(View.VISIBLE);
                    mBinding.topBar.setBackgroundColor(Color.WHITE);
                } else {
                    mBinding.tagLogo.setVisibility(View.GONE);
                    mBinding.tagTitle.setVisibility(View.GONE);
                    mBinding.topBarFollow.setVisibility(View.GONE);
                    mBinding.actionBack.setImageResource(R.drawable.icon_back_white);
                    mBinding.topLine.setVisibility(View.INVISIBLE);
                    mBinding.topBar.setBackgroundColor(Color.TRANSPARENT);
                }
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        mPlayDetector.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mShouldPause) {
            mPlayDetector.onPause();
        }
    }

    @Override
    protected void onDestroy() {
        // 页面销毁时释放播放器对象
        PageListPlayManager.release(VALUE_FEED_TYPE);
        super.onDestroy();
    }

    @Override
    public void onClick(View v) {
        if (v == mBinding.actionBack) {
            this.finish();
        }
    }

    @Override
    public void onRefresh(@NonNull RefreshLayout refreshLayout) {
        mViewModel.getDataSource().invalidate();
    }

    @Override
    public void onLoadMore(@NonNull RefreshLayout refreshLayout) {
        // 委托给paging框架
    }
}
