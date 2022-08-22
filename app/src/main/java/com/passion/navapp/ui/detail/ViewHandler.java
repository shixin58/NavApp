package com.passion.navapp.ui.detail;

import android.view.ViewGroup;

import androidx.annotation.CallSuper;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.paging.PagedList;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.passion.libcommon.EmptyView;
import com.passion.navapp.R;
import com.passion.navapp.databinding.LayoutFeedDetailBottomInteractionBinding;
import com.passion.navapp.model.Comment;
import com.passion.navapp.model.Feed;

// 图文详情页和视频详情页相同的功能在基类完成。
// 评论列表和底部互动区域
public abstract class ViewHandler {
    protected final FragmentActivity mActivity;
    protected Feed mFeed;
    protected RecyclerView mRecyclerView;
    protected LayoutFeedDetailBottomInteractionBinding mBottomInteractionBinding;
    protected FeedCommentAdapter mCommentAdapter;
    protected FeedDetailViewModel viewModel;

    public ViewHandler(FragmentActivity activity) {
        mActivity = activity;
        viewModel = ViewModelProviders.of(activity).get(FeedDetailViewModel.class);
    }

    @CallSuper
    public void bindInitData(Feed feed) {
        mBottomInteractionBinding.setOwner(mActivity);
        mFeed = feed;
        mRecyclerView.setLayoutManager(new LinearLayoutManager(mActivity, LinearLayoutManager.VERTICAL, false));
        // 默认DefaultItemAnimator，设置null来禁用ItemAnimator
        mRecyclerView.setItemAnimator(null);
        mCommentAdapter = new FeedCommentAdapter(mActivity);
        mRecyclerView.setAdapter(mCommentAdapter);

        viewModel.setItemId(mFeed.itemId);
        // LiveData首次订阅触发onActive，最后1个订阅者被移除触发onInactive()，宿主可见触发dispatchingValue()->onChanged()。
        // 1）调用由LivePagedListBuilder创建的LiveData#observe()首次订阅，会触发onActive()->ComputableLiveData#compute()，
        // 2）compute()内部构建PagedList, ContiguousPagedList()->ContiguousDataSource#dispatchLoadInitial()
        // 3）进而触发ItemKeyedDataSource#loadInitial()下载数据，调用ItemKeyedDataSource.LoadCallback#onResult()回传，
        // 4）调用Observer#onChanged() -> PagedListAdapter#submitList()刷新RecyclerView
        viewModel.getPageData().observe(mActivity, new Observer<PagedList<Comment>>() {
            @Override
            public void onChanged(PagedList<Comment> comments) {
                mCommentAdapter.submitList(comments);
                handleEmpty(!comments.isEmpty());
            }
        });
    }

    private EmptyView mEmptyView;
    private void handleEmpty(boolean hasData) {
        if (hasData) {
            if (mEmptyView != null) {
                mCommentAdapter.removeHeaderView(mEmptyView);
            }
        } else {
            if (mEmptyView == null) {
                mEmptyView = new EmptyView(mActivity);
                mEmptyView.setLayoutParams(new RecyclerView.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                mEmptyView.setTitle(mActivity.getString(R.string.feed_comment_empty));
                mCommentAdapter.addHeaderView(mEmptyView);
            }
        }
    }
}
