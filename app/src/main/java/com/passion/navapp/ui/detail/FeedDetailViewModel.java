package com.passion.navapp.ui.detail;

import androidx.annotation.NonNull;
import androidx.paging.DataSource;
import androidx.paging.ItemKeyedDataSource;

import com.alibaba.fastjson.TypeReference;
import com.passion.libnetwork.ApiResponse;
import com.passion.libnetwork.ApiService;
import com.passion.navapp.AbsViewModel;
import com.passion.navapp.model.Comment;
import com.passion.navapp.ui.login.UserManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class FeedDetailViewModel extends AbsViewModel<Comment> {
    private long itemId;// 帖子id

    @Override
    public DataSource<Integer, Comment> createDataSource() {
        return new CommentDataSource();
    }

    public void setItemId(long itemId) {
        this.itemId = itemId;
    }

    class CommentDataSource extends ItemKeyedDataSource<Integer,Comment> {

        @Override
        public void loadInitial(@NonNull LoadInitialParams<Integer> params, @NonNull LoadInitialCallback<Comment> callback) {
            loadData(params.requestedInitialKey, params.requestedLoadSize, callback);
        }

        @Override
        public void loadAfter(@NonNull LoadParams<Integer> params, @NonNull LoadCallback<Comment> callback) {
            loadData(params.key, params.requestedLoadSize, callback);
        }

        @Override
        public void loadBefore(@NonNull LoadParams<Integer> params, @NonNull LoadCallback<Comment> callback) {
            callback.onResult(Collections.emptyList());
        }

        @NonNull
        @Override
        public Integer getKey(@NonNull Comment item) {
            return item.id;
        }
    }

    private void loadData(int key, int requestedLoadSize, ItemKeyedDataSource.LoadCallback<Comment> callback) {
        ApiResponse<List<Comment>> response = ApiService.get("/comment/queryFeedComments")
                .addParam("id", key)
                .addParam("itemId", itemId)
                .addParam("userId", UserManager.get().getUserId())
                .addParam("pageCount", requestedLoadSize)
                .responseType(new TypeReference<ArrayList<Comment>>() {
                }.getType())
                .execute();// loadInitial()在子线程调用，所以loadData()可用同步execute方法()

        List<Comment> list = response.body==null?Collections.emptyList():response.body;
        callback.onResult(list);
    }
}
