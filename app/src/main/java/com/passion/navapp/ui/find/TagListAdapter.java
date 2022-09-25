package com.passion.navapp.ui.find;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.lifecycle.LifecycleOwner;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.passion.libcommon.extension.AbsPagedListAdapter;
import com.passion.navapp.databinding.LayoutTagListItemBinding;
import com.passion.navapp.model.FeedTag;
import com.passion.navapp.ui.InteractionPresenter;

public class TagListAdapter extends AbsPagedListAdapter<FeedTag,TagListAdapter.ViewHolder> {
    private final Context mContext;
    private final LayoutInflater mLayoutInflater;

    protected TagListAdapter(Context context) {
        super(new DiffUtil.ItemCallback<>() {
            @Override
            public boolean areItemsTheSame(@NonNull FeedTag oldItem, @NonNull FeedTag newItem) {
                return oldItem.tagId== newItem.tagId;
            }

            @Override
            public boolean areContentsTheSame(@NonNull FeedTag oldItem, @NonNull FeedTag newItem) {
                return oldItem.equals(newItem);
            }
        });
        mContext = context;
        mLayoutInflater = LayoutInflater.from(mContext);
    }

    @Override
    protected int getItemViewType2(int position) {
        return 0;
    }

    @Override
    protected ViewHolder onCreateViewHolder2(ViewGroup parent, int viewType) {
        LayoutTagListItemBinding binding = LayoutTagListItemBinding.inflate(mLayoutInflater, parent, false);
        return new ViewHolder(binding.getRoot(), binding);
    }

    @Override
    protected void onBindViewHolder2(ViewHolder holder, final int position) {
        FeedTag item = getItem(position);
        holder.bindData(item);
        holder.mBinding.actionFollow.setOnClickListener(v -> InteractionPresenter.toggleTagLike((LifecycleOwner) mContext, item));
        holder.itemView.setOnClickListener(v -> TagFeedListActivity.openActivity(mContext, item));
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final com.passion.navapp.databinding.LayoutTagListItemBinding mBinding;

        public ViewHolder(@NonNull View itemView, LayoutTagListItemBinding binding) {
            super(itemView);
            mBinding = binding;
        }

        public void bindData(FeedTag item) {
            mBinding.setFeedTag(item);
        }
    }
}
