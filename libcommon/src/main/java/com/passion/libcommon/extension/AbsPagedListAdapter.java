package com.passion.libcommon.extension;

import android.util.SparseArray;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.paging.PagedListAdapter;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

// 使用paging框架时，下拉刷新或分页得到的PagedList跟PagedListAdapter强关联。
// 外部传入的Adapter拿不到数据集合对象，无法在onBindViewHolder完成数据绑定。
// 传统RecyclerView设置header/footer的方式变得不可用，新方案继承PagedListAdapter完成相应功能。
public abstract class AbsPagedListAdapter<T, VH extends RecyclerView.ViewHolder> extends PagedListAdapter<T,VH> {
    private static final int BASE_ITEM_TYPE_HEADER = 100_000;
    private static final int BASE_ITEM_TYPE_FOOTER = 200_000;

    // SparseArray<View>保存多个header或footer，比HashMap<Integer,View>性能好
    private final SparseArray<View> mHeaders = new SparseArray<>();
    private final SparseArray<View> mFooters = new SparseArray<>();

    private int itemTypeHeader = BASE_ITEM_TYPE_HEADER;
    private int itemTypeFooter = BASE_ITEM_TYPE_FOOTER;

    protected AbsPagedListAdapter(@NonNull DiffUtil.ItemCallback<T> diffCallback) {
        super(diffCallback);
    }

    public void addHeaderView(View view) {
        if (mHeaders.indexOfValue(view) < 0) {
            mHeaders.put(itemTypeHeader++, view);
            notifyDataSetChanged();
        }
    }

    public void removeHeaderView(View view) {
        int idx = mHeaders.indexOfValue(view);
        if (idx >= 0) {
            mHeaders.removeAt(idx);
            notifyDataSetChanged();
        }
    }

    public void addFooterView(View view) {
        if (mFooters.indexOfValue(view) < 0) {
            mFooters.put(itemTypeFooter++, view);
            notifyDataSetChanged();
        }
    }

    public void removeFooterView(View view) {
        int idx = mFooters.indexOfValue(view);
        if (idx >= 0) {
            mFooters.removeAt(idx);
            notifyDataSetChanged();
        }
    }

    @Override
    public int getItemCount() {
        int itemCount = super.getItemCount();
        return itemCount + mHeaders.size() + mFooters.size();
    }

    public int getOriginalItemCount() {
        return getItemCount() - mHeaders.size() - mFooters.size();
    }

    @Override
    public int getItemViewType(int position) {
        if (isHeaderPosition(position)) {
            return mHeaders.keyAt(position);
        }

        if (isFooterPosition(position)) {
            return mFooters.keyAt(position - mHeaders.size() - getOriginalItemCount());
        }

        position -= mHeaders.size();
        return getItemViewType2(position);
    }

    protected abstract int getItemViewType2(int position);

    private boolean isHeaderPosition(int position) {
        return position < mHeaders.size();
    }

    private boolean isFooterPosition(int position) {
        return position >= mHeaders.size() + getOriginalItemCount();
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (mHeaders.indexOfKey(viewType) >= 0) {
            View view = mHeaders.get(viewType);
            return (VH) new RecyclerView.ViewHolder(view){};
        }
        if (mFooters.indexOfKey(viewType) >= 0) {
            View view = mFooters.get(viewType);
            return (VH) new RecyclerView.ViewHolder(view){};
        }
        return onCreateViewHolder2(parent, viewType);
    }

    protected abstract VH onCreateViewHolder2(ViewGroup parent, int viewType);

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        if (isHeaderPosition(position) || isFooterPosition(position))
            return;
        position -= mHeaders.size();
        onBindViewHolder2(holder, position);
    }

    protected abstract void onBindViewHolder2(VH holder, int position);

    @Override
    public void registerAdapterDataObserver(@NonNull RecyclerView.AdapterDataObserver observer) {
        // RecyclerView#setAdapter()内部调用Adapter#registerAdapterDataObserver()注册AdapterDataObserver
        super.registerAdapterDataObserver(new AdapterDataObserverProxy(observer));
    }

    private class AdapterDataObserverProxy extends RecyclerView.AdapterDataObserver {
        private final RecyclerView.AdapterDataObserver mObserver;

        public AdapterDataObserverProxy(RecyclerView.AdapterDataObserver observer) {
            mObserver = observer;
        }

        public void onChanged() {
            mObserver.onChanged();
        }

        public void onItemRangeChanged(int positionStart, int itemCount) {
            mObserver.onItemRangeChanged(positionStart + mHeaders.size(), itemCount);
        }

        public void onItemRangeChanged(int positionStart, int itemCount, @Nullable Object payload) {
            mObserver.onItemRangeChanged(positionStart + mHeaders.size(), itemCount, payload);
        }

        public void onItemRangeInserted(int positionStart, int itemCount) {
            mObserver.onItemRangeInserted(positionStart + mHeaders.size(), itemCount);
        }

        public void onItemRangeRemoved(int positionStart, int itemCount) {
            mObserver.onItemRangeRemoved(positionStart + mHeaders.size(), itemCount);
        }

        public void onItemRangeMoved(int fromPosition, int toPosition, int itemCount) {
            mObserver.onItemRangeMoved(fromPosition + mHeaders.size(), toPosition + mHeaders.size(), itemCount);
        }
    }
}
