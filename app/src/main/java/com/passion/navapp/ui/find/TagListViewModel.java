package com.passion.navapp.ui.find;

import android.annotation.SuppressLint;

import androidx.annotation.NonNull;
import androidx.arch.core.executor.ArchTaskExecutor;
import androidx.lifecycle.MutableLiveData;
import androidx.paging.ItemKeyedDataSource;

import com.passion.libnetwork.ApiResponse;
import com.passion.libnetwork.ApiService;
import com.passion.navapp.AbsViewModel;
import com.passion.navapp.model.FeedTag;
import com.passion.navapp.ui.login.UserManager;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class TagListViewModel extends AbsViewModel<FeedTag> {
    private String tagType;
    private int offset;
    private final AtomicBoolean loadAfter = new AtomicBoolean(false);

    @Override
    public DataSource createDataSource() {
        return new DataSource();
    }

    public void setTagType(String tagType) {
        this.tagType = tagType;
    }

    private class DataSource extends ItemKeyedDataSource<Long, FeedTag> {

        @Override
        public void loadInitial(@NonNull LoadInitialParams<Long> params, @NonNull LoadInitialCallback<FeedTag> callback) {
            loadData(params.requestedInitialKey, callback);
        }

        @Override
        public void loadAfter(@NonNull LoadParams<Long> params, @NonNull LoadCallback<FeedTag> callback) {
            loadData(params.key, callback);
        }

        @Override
        public void loadBefore(@NonNull LoadParams<Long> params, @NonNull LoadCallback<FeedTag> callback) {
            callback.onResult(Collections.emptyList());
        }

        private void loadData(Long requestedKey, LoadCallback<FeedTag> callback) {
            if (requestedKey > 0) {
                loadAfter.set(true);
            }
            ApiResponse<List<FeedTag>> response = ApiService.get("/tag/queryTagList")
                    .addParam("userId", UserManager.get().getUserId())
                    .addParam("tagId", requestedKey)
                    .addParam("tagType", tagType)
                    .addParam("pageCount", 10)
                    .addParam("offset", offset)
                    .execute();

            List<FeedTag> result = response.body==null?Collections.emptyList():response.body;
            callback.onResult(result);
            if (requestedKey > 0) {
                loadAfter.set(false);
                offset += result.size();
                ((MutableLiveData<Boolean>)getBoundaryPageData()).postValue(result.size() > 0);
            } else {
                offset = result.size();
            }
        }

        @NonNull
        @Override
        public Long getKey(@NonNull FeedTag item) {
            return item.tagId;
        }
    }

    @SuppressLint("RestrictedApi")
    public void loadData(long tagId, ItemKeyedDataSource.LoadCallback callback) {
        // loadAfter为true表示正在分页当中
        if (tagId <= 0 || loadAfter.get()) {
            callback.onResult(Collections.emptyList());
            return;
        }
        ArchTaskExecutor.getIOThreadExecutor().execute(new Runnable() {
            @Override
            public void run() {
                ((TagListViewModel.DataSource)getDataSource()).loadData(tagId, callback);
            }
        });
    }
}
