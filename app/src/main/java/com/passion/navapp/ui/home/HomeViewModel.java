package com.passion.navapp.ui.home;

import android.annotation.SuppressLint;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.arch.core.executor.ArchTaskExecutor;
import androidx.lifecycle.MutableLiveData;
import androidx.paging.DataSource;
import androidx.paging.ItemKeyedDataSource;
import androidx.paging.PagedList;

import com.alibaba.fastjson.TypeReference;
import com.passion.libnetwork.ApiResponse;
import com.passion.libnetwork.ApiService;
import com.passion.libnetwork.JsonCallback;
import com.passion.libnetwork.Request;
import com.passion.navapp.AbsViewModel;
import com.passion.navapp.model.Feed;
import com.passion.navapp.ui.MutableDataSource;
import com.passion.navapp.ui.login.UserManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class HomeViewModel extends AbsViewModel<Feed> {
    // 默认true，volatile多线程有序性和可见性
    private volatile boolean withCache = true;
    // 区别于pageData，专为cache定义的LiveData
    private final MutableLiveData<PagedList<Feed>> cacheLiveData = new MutableLiveData<>();
    private String mFeedType;
    private final AtomicBoolean loadAfter = new AtomicBoolean(false);

    @Override
    public DataSource<Integer,Feed> createDataSource() {
        return new FeedDataSource();
    }

    public MutableLiveData<PagedList<Feed>> getCacheLiveData() {
        return cacheLiveData;
    }

    public void setFeedType(String feedType) {
        mFeedType = feedType;
    }

    // loadInitial()/loadAfter()/loadBefore()均在子线程执行
    class FeedDataSource extends ItemKeyedDataSource<Integer, Feed> {
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
        if (key > 0) {
            loadAfter.set(true);
        }
        Request request = ApiService.get("/feeds/queryHotFeedsList")
                .addParam("feedType", mFeedType)
                .addParam("userId", UserManager.get().getUserId())
                .addParam("feedId", key)
                .addParam("pageCount", count)
                .responseType(new TypeReference<ArrayList<Feed>>(){}.getType());

        if (withCache) {
            request.cacheStrategy(Request.CacheStrategy.CACHE_ONLY);
            request.execute(new JsonCallback<List<Feed>>() {
                @Override
                public void onCacheSuccess(ApiResponse<List<Feed>> response) {
                    Log.i("loadData", "onCacheSuccess: "+response.body);
                    List<Feed> list = response.body;
                    MutableDataSource<Integer,Feed> dataSource = new MutableDataSource<>();
                    dataSource.data.addAll(list);

                    // PagedList构建时触发DataSource#loadInitial()
                    PagedList<Feed> pagedList = dataSource.buildNewPagedList(config);
                    cacheLiveData.postValue(pagedList);
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
                // 通过LiveData告诉UI层，是否关闭上拉分页动画，需手动切换UI线程
                getBoundaryPageData().postValue(!data.isEmpty());
                loadAfter.set(false);
            }
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }
        Log.i("loadData", "key: "+key);
    }

    @SuppressLint("RestrictedApi")
    public void loadAfter(int id, ItemKeyedDataSource.LoadCallback<Feed> callback) {
        if (loadAfter.get()) {
            callback.onResult(Collections.emptyList());
        }
        ArchTaskExecutor.getIOThreadExecutor().execute(new Runnable() {
            @Override
            public void run() {
                loadData(id, PAGE_SIZE, callback);
            }
        });
    }
}