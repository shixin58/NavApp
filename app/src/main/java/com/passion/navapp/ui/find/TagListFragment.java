package com.passion.navapp.ui.find;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;
import androidx.paging.ItemKeyedDataSource;
import androidx.paging.PagedList;
import androidx.paging.PagedListAdapter;

import com.passion.navapp.R;
import com.passion.navapp.model.FeedTag;
import com.passion.navapp.ui.AbsListFragment;
import com.passion.navapp.ui.MutableItemKeyedDataSource;
import com.scwang.smart.refresh.layout.api.RefreshLayout;

import java.util.List;

/**
 * 发现tab标签列表页
 */
public class TagListFragment extends AbsListFragment<FeedTag,TagListViewModel> {
    public static final String KEY_TAG_TYPE = "tag_type";
    private String mTagType;

    public static TagListFragment newInstance(String tagType) {
        Bundle args = new Bundle();
        args.putString(KEY_TAG_TYPE, tagType);
        TagListFragment fragment = new TagListFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    protected void afterViewCreated() {
        if (TextUtils.equals(mTagType, "onlyFollow")) {
            mEmptyView.setTitle(getString(R.string.tag_list_no_follow));
            mEmptyView.setButton(getString(R.string.tag_list_no_follow_button), new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ((MutableLiveData)mViewModel.getSwitchTabLiveData()).setValue(new Object());
                }
            });
        }
        mRecyclerView.removeItemDecorationAt(0);
        mViewModel.setTagType(mTagType);
    }

    @Override
    public PagedListAdapter createAdapter() {
        mTagType = getArguments().getString(KEY_TAG_TYPE);
        TagListAdapter adapter = new TagListAdapter(getContext());
        return adapter;
    }

    @Override
    public void onLoadMore(@NonNull RefreshLayout refreshLayout) {
        PagedList<FeedTag> currentList = createAdapter().getCurrentList();
        long tagId = (currentList==null || currentList.isEmpty())?0:currentList.get(currentList.size()-1).tagId;
        mViewModel.loadData(tagId, new ItemKeyedDataSource.LoadCallback<FeedTag>() {
            @Override
            public void onResult(@NonNull List<FeedTag> data) {
                MutableItemKeyedDataSource<Long, FeedTag> mutableItemKeyedDataSource = new MutableItemKeyedDataSource<Long, FeedTag>((ItemKeyedDataSource) mViewModel.getDataSource()) {
                    @NonNull
                    @Override
                    public Long getKey(@NonNull FeedTag item) {
                        return item.tagId;
                    }
                };
                mutableItemKeyedDataSource.data.addAll(currentList);
                mutableItemKeyedDataSource.data.addAll(data);
                PagedList<FeedTag> newPagedList = mutableItemKeyedDataSource.buildNewPagedList(currentList.getConfig());
                if (!data.isEmpty()) {// 分页有新数据
                    submitList(newPagedList);
                }
            }
        });
    }

    @Override
    public void onRefresh(@NonNull RefreshLayout refreshLayout) {
        mViewModel.getDataSource().invalidate();
    }
}
