package com.passion.navapp.ui.my;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;
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

public class ProfileViewModel extends AbsViewModel<Feed> {
    private String mProfileType;

    @Override
    public DataSource createDataSource() {
        return new DataSource();
    }

    public void setProfileType(String profileType) {
        this.mProfileType = profileType;
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

        private void loadData(int key, LoadCallback<Feed> callback) {
            ApiResponse<List<Feed>> response = ApiService.get("/feeds/queryProfileFeeds")
                    .addParam("inId", key)
                    .addParam("userId", UserManager.get().getUserId())
                    .addParam("pageCount", 10)
                    .addParam("profileType", mProfileType)
                    .responseType(new TypeReference<ArrayList<Feed>>() {
                    }.getType())
                    .execute();
            List<Feed> result = response.body==null? Collections.emptyList():response.body;
            callback.onResult(result);
            if (key > 0) {
                ((MutableLiveData<Boolean>)getBoundaryPageData()).postValue(result.size() > 0);
            }
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
}
