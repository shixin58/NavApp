package com.passion.navapp.ui.home;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.paging.DataSource;
import androidx.paging.ItemKeyedDataSource;

import com.alibaba.fastjson.TypeReference;
import com.passion.libnetwork.ApiResponse;
import com.passion.libnetwork.ApiService;
import com.passion.libnetwork.JsonCallback;
import com.passion.libnetwork.Request;
import com.passion.navapp.AbsViewModel;
import com.passion.navapp.model.Feed;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class HomeViewModel extends AbsViewModel<Feed> {
    private volatile boolean withCache = true;
    private String mFeedType;

    @Override
    public DataSource createDataSource() {
        return mDataSource;
    }

    public void setFeedType(String feedType) {
        mFeedType = feedType;
    }

    // loadInitial()/loadAfter()/loadBefore()均在子线程执行
    ItemKeyedDataSource<Integer,Feed> mDataSource = new ItemKeyedDataSource<Integer, Feed>() {
        @Override
        public void loadInitial(@NonNull LoadInitialParams<Integer> params, @NonNull LoadInitialCallback<Feed> callback) {
            // 加载初始化数据
            loadData(0, params.requestedLoadSize, callback);
            withCache = false;
        }

        @Override
        public void loadAfter(@NonNull LoadParams<Integer> params, @NonNull LoadCallback<Feed> callback) {
            // 向后加载分页数据
            loadData(params.key, params.requestedLoadSize, callback);
        }

        @Override
        public void loadBefore(@NonNull LoadParams<Integer> params, @NonNull LoadCallback<Feed> callback) {
            // 向前加载
            callback.onResult(Collections.emptyList());
        }

        @NonNull
        @Override
        public Integer getKey(@NonNull Feed item) {
            return item.id;
        }
    };

    private void loadData(int key, int count, ItemKeyedDataSource.LoadCallback<Feed> callback) {
        Request request = ApiService.get("/feeds/queryHotFeedsList")
                .addParam("feedType", mFeedType)
                .addParam("userId", 0)
                .addParam("feedId", key)
                .addParam("pageCount", count)
                .responseType(new TypeReference<ArrayList<Feed>>(){}.getType());

        if (withCache) {
            request.cacheStrategy(Request.CacheStrategy.CACHE_ONLY);
            request.execute(new JsonCallback<List<Feed>>() {
                @Override
                public void onCacheSuccess(ApiResponse<List<Feed>> response) {
                    Log.i("loadData", "onCacheSuccess: "+response.body);
                }
            });
        }

        try {
            Request netRequest = withCache?request.clone():request;
            netRequest.cacheStrategy(key==0?Request.CacheStrategy.NETWORK_CACHE:Request.CacheStrategy.NETWORK_ONLY);
            ApiResponse<List<Feed>> response = netRequest.execute();
            List<Feed> data = response.body==null?Collections.emptyList():response.body;
            callback.onResult(data);
            if (key > 0) {
                // 通过LiveData告诉UI层，是否关闭上拉分页动画
                getBoundaryPageData().postValue(!data.isEmpty());
            }
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }
    }
}