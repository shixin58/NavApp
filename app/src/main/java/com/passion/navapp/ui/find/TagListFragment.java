package com.passion.navapp.ui.find;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.paging.ItemKeyedDataSource;
import androidx.paging.PagedList;
import androidx.paging.PagedListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.passion.navapp.model.FeedTag;
import com.passion.navapp.ui.AbsListFragment;
import com.passion.navapp.ui.MutableItemKeyedDataSource;
import com.scwang.smart.refresh.layout.api.RefreshLayout;

import java.util.List;

/**
 * 标签列表页
 */
public class TagListFragment extends AbsListFragment<FeedTag,TagListViewModel> {
    public static final String KEY_TAG_TYPE = "tag_type";

    public static TagListFragment newInstance(String tagType) {
        Bundle args = new Bundle();
        args.putString(KEY_TAG_TYPE, tagType);
        TagListFragment fragment = new TagListFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    protected void afterViewCreated() {

    }

    @Override
    public PagedListAdapter<FeedTag, RecyclerView.ViewHolder> getAdapter() {
        return null;
    }

    @Override
    public void onLoadMore(@NonNull RefreshLayout refreshLayout) {
        PagedList<FeedTag> currentList = getAdapter().getCurrentList();
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
                if (!data.isEmpty()) {
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
