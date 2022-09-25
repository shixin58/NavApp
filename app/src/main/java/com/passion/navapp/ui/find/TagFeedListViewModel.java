package com.passion.navapp.ui.find;

import androidx.annotation.NonNull;
import androidx.paging.ItemKeyedDataSource;

import com.alibaba.fastjson.TypeReference;
import com.passion.libnetwork.ApiResponse;
import com.passion.libnetwork.ApiService;
import com.passion.navapp.AbsViewModel;
import com.passion.navapp.model.Feed;
import com.passion.navapp.ui.login.UserManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class TagFeedListViewModel extends AbsViewModel<Feed> {
    private String mFeedType;

    @Override
    public DataSource createDataSource() {
        return new DataSource();
    }

    public void setFeedType(String feedType) {
        this.mFeedType = feedType;
    }

    private class DataSource extends ItemKeyedDataSource<Integer,Feed> {

        @Override
        public void loadInitial(@NonNull LoadInitialParams<Integer> params, @NonNull LoadInitialCallback<Feed> callback) {
            loadData(params.requestedInitialKey, callback);
        }

        @Override
        public void loadAfter(@NonNull LoadParams<Integer> params, @NonNull LoadCallback<Feed> callback) {
            loadData(params.key, callback);
        }

        @Override
        public void loadBefore(@NonNull LoadParams<Integer> params, @NonNull LoadCallback<Feed> callback) {
            callback.onResult(Collections.emptyList());
        }

        @NonNull
        @Override
        public Integer getKey(@NonNull Feed item) {
            return item.id;
        }
    }

    private void loadData(Integer feedId, ItemKeyedDataSource.LoadCallback<Feed> callback) {
        ApiResponse<List<Feed>> response = ApiService.get("/feeds/queryHotFeedsList")
                .addParam("feedType", mFeedType)
                .addParam("userId", UserManager.get().getUserId())
                .addParam("feedId", feedId)
                .addParam("pageCount", 10)
                .responseType(new TypeReference<ArrayList<Feed>>(){}.getType())
                .execute();
        List<Feed> list = response.body==null?Collections.emptyList():response.body;
        callback.onResult(list);
    }
}
