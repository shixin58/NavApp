package com.passion.navapp;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import androidx.paging.DataSource;
import androidx.paging.LivePagedListBuilder;
import androidx.paging.PagedList;

public abstract class AbsViewModel<T> extends ViewModel {
    public static final int INITIAL_LOAD_SIZE = 2;
    public static final int PAGE_SIZE = 1;

    protected PagedList.Config config;
    private DataSource<Integer,T> dataSource;

    private final LiveData<PagedList<T>> pageData;
    private final MutableLiveData<Boolean> boundaryPageData = new MutableLiveData<>();

    public AbsViewModel() {
        config = new PagedList.Config.Builder()
                .setInitialLoadSizeHint(INITIAL_LOAD_SIZE)
                .setPageSize(PAGE_SIZE)
//                .setMaxSize(100)
//                .setEnablePlaceholders()
//                .setPrefetchDistance()
                .build();

        pageData = new LivePagedListBuilder<>(factory, config)
                // 多个参数可封装
                .setInitialLoadKey(0)
                // 异步线程池使用内置
//                .setFetchExecutor()
                .setBoundaryCallback(callback)
                .build();
    }

    public DataSource getDataSource() {
        return dataSource;
    }

    public LiveData<PagedList<T>> getPageData() {
        return pageData;
    }

    public LiveData<Boolean> getBoundaryPageData() {
        return boundaryPageData;
    }

    PagedList.BoundaryCallback<T> callback = new PagedList.BoundaryCallback<T>() {
        @Override
        public void onZeroItemsLoaded() {
            boundaryPageData.postValue(false);
        }

        @Override
        public void onItemAtFrontLoaded(@NonNull T itemAtFront) {
            boundaryPageData.postValue(true);
        }

        @Override
        public void onItemAtEndLoaded(@NonNull T itemAtEnd) {
            super.onItemAtEndLoaded(itemAtEnd);
        }
    };

    DataSource.Factory<Integer, T> factory = new DataSource.Factory<Integer, T>() {
        @NonNull
        @Override
        public DataSource<Integer, T> create() {
            if (dataSource==null || dataSource.isInvalid()) {
                dataSource = createDataSource();
            }
            return dataSource;
        }
    };

    public abstract DataSource createDataSource();
}
