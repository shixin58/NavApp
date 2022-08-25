package com.passion.navapp.ui;

import android.annotation.SuppressLint;

import androidx.annotation.NonNull;
import androidx.arch.core.executor.ArchTaskExecutor;
import androidx.paging.PageKeyedDataSource;
import androidx.paging.PagedList;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MutablePageKeyedDataSource<Key,Value> extends PageKeyedDataSource<Key,Value> {
    public List<Value> data = new ArrayList<>();

    // 采用PageKeyedDataSource+ContiguousPagedList
    public PagedList<Value> buildNewPagedList(PagedList.Config config) {
        @SuppressLint("RestrictedApi")
        PagedList<Value> pagedList = new PagedList.Builder<Key,Value>(this, config)
                .setFetchExecutor(ArchTaskExecutor.getIOThreadExecutor())
                .setNotifyExecutor(ArchTaskExecutor.getMainThreadExecutor())
                .build();
        return pagedList;
    }

    @Override
    public void loadInitial(@NonNull LoadInitialParams<Key> params, @NonNull LoadInitialCallback<Key, Value> callback) {
        // 未传前后相邻页码
        callback.onResult(data, null, null);
    }

    @Override
    public void loadAfter(@NonNull LoadParams<Key> params, @NonNull LoadCallback<Key, Value> callback) {
        // adjacent临近的，毗连的
        callback.onResult(Collections.emptyList(), null);
    }

    @Override
    public void loadBefore(@NonNull LoadParams<Key> params, @NonNull LoadCallback<Key, Value> callback) {
        callback.onResult(Collections.emptyList(), null);
    }
}
