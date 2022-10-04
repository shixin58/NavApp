package com.passion.navapp.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;
import androidx.paging.PagedList;
import androidx.paging.PagedListAdapter;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.passion.libcommon.view.EmptyView;
import com.passion.navapp.AbsViewModel;
import com.passion.navapp.R;
import com.passion.navapp.databinding.LayoutRefreshViewBinding;
import com.scwang.smart.refresh.layout.SmartRefreshLayout;
import com.scwang.smart.refresh.layout.constant.RefreshState;
import com.scwang.smart.refresh.layout.listener.OnLoadMoreListener;
import com.scwang.smart.refresh.layout.listener.OnRefreshListener;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

public abstract class AbsListFragment<T,M extends AbsViewModel<T>> extends Fragment implements OnRefreshListener, OnLoadMoreListener {
    protected LayoutRefreshViewBinding mBinding;
    protected M mViewModel;

    protected RecyclerView mRecyclerView;
    protected SmartRefreshLayout mRefreshLayout;
    protected EmptyView mEmptyView;
    protected PagedListAdapter<T, RecyclerView.ViewHolder> mAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mBinding = LayoutRefreshViewBinding.inflate(inflater, container, false);
        return mBinding.getRoot();
    }

    protected abstract void afterViewCreated();

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mRecyclerView = mBinding.recyclerView;
        mRefreshLayout = mBinding.refreshLayout;
        mEmptyView = mBinding.emptyView;

        mRefreshLayout.setEnableRefresh(true);
        mRefreshLayout.setEnableLoadMore(true);
        mRefreshLayout.setOnRefreshListener(this);
        mRefreshLayout.setOnLoadMoreListener(this);

        mAdapter = createAdapter();
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
        mRecyclerView.setItemAnimator(null);

        // 默认给列表中的item一个10dp的ItemDecoration
        DividerItemDecoration decoration = new DividerItemDecoration(getContext(), LinearLayoutManager.VERTICAL);
        decoration.setDrawable(ContextCompat.getDrawable(getContext(), R.drawable.list_divider));
        mRecyclerView.addItemDecoration(decoration);

        generateViewModel();
        afterViewCreated();
    }

    private void generateViewModel() {
        // 利用AbsViewModel子类传递的泛型参数，拿到子类class对象，进而拿到子类对象
        ParameterizedType type = (ParameterizedType) getClass().getGenericSuperclass();
        Type[] arguments = type.getActualTypeArguments();
        if (arguments.length > 1) {
            Type argument = arguments[1];
            Class modelClazz = ((Class) argument).asSubclass(AbsViewModel.class);
            mViewModel = (M) ViewModelProviders.of(this).get(modelClazz);

            // 调用LiveData#observe()触发页面初始化数据加载Observer#onChanged()
            mViewModel.getPageData().observe(getViewLifecycleOwner(), this::submitList);

            // 监听分页有无更多数据，来决定是否关闭上来加载动画
            mViewModel.getBoundaryPageData().observe(getViewLifecycleOwner(), this::finishRefresh);
        }
    }

    public void submitList(PagedList<T> pagedList) {
        if (!pagedList.isEmpty()) {
            mAdapter.submitList(pagedList);
        }
        finishRefresh(!pagedList.isEmpty());
    }

    public void finishRefresh(boolean remoteNotEmpty) {
        RefreshState state = mRefreshLayout.getState();
        if (state.isFooter && state.isOpening) {
            mRefreshLayout.finishLoadMore();
        } else if (state.isHeader && state.isOpening) {
            mRefreshLayout.finishRefresh();
        }

        PagedList<T> currentList = mAdapter.getCurrentList();
        boolean hasData = remoteNotEmpty || (currentList!=null && !currentList.isEmpty());
        if (hasData) {
            mEmptyView.setVisibility(View.GONE);
        } else {
            mEmptyView.setVisibility(View.VISIBLE);
        }
    }

    public abstract PagedListAdapter<T, RecyclerView.ViewHolder> createAdapter();
}
