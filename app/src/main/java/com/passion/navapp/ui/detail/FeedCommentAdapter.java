package com.passion.navapp.ui.detail;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;
import androidx.paging.ItemKeyedDataSource;
import androidx.paging.PagedList;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.passion.libcommon.PixUtils;
import com.passion.libcommon.extension.AbsPagedListAdapter;
import com.passion.navapp.databinding.LayoutFeedCommentListItemBinding;
import com.passion.navapp.model.Comment;
import com.passion.navapp.ui.InteractionPresenter;
import com.passion.navapp.ui.MutableItemKeyedDataSource;
import com.passion.navapp.ui.login.UserManager;

public class FeedCommentAdapter extends AbsPagedListAdapter<Comment,FeedCommentAdapter.ViewHolder> {
    private final Context mContext;

    protected FeedCommentAdapter(Context ctx) {
        super(new DiffUtil.ItemCallback<Comment>() {
            @Override
            public boolean areItemsTheSame(@NonNull Comment oldItem, @NonNull Comment newItem) {
                return oldItem.id == newItem.id;
            }

            @Override
            public boolean areContentsTheSame(@NonNull Comment oldItem, @NonNull Comment newItem) {
                return oldItem.equals(newItem);
            }
        });
        mContext = ctx;
    }

    @Override
    protected int getItemViewType2(int position) {
        return 0;
    }

    @Override
    protected ViewHolder onCreateViewHolder2(ViewGroup parent, int viewType) {
        LayoutFeedCommentListItemBinding binding = LayoutFeedCommentListItemBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding.getRoot(), binding);
    }

    @Override
    protected void onBindViewHolder2(ViewHolder holder, int position) {
        Comment comment = getItem(position);
        holder.bindData(comment);
        holder.mBinding.commentDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LiveData<Boolean> liveData = InteractionPresenter.deleteFeedComment(mContext, comment.itemId, comment.commentId);
                liveData.observe((LifecycleOwner) mContext, new Observer<Boolean>() {
                    @Override
                    public void onChanged(Boolean success) {
                        if (success) {
                            PagedList<Comment> currentList = getCurrentList();
                            MutableItemKeyedDataSource<Integer,Comment> itemKeyedDataSource = new MutableItemKeyedDataSource<Integer, Comment>((ItemKeyedDataSource) currentList.getDataSource()) {
                                @NonNull
                                @Override
                                public Integer getKey(@NonNull Comment item) {
                                    return item.id;
                                }
                            };
                            for (Comment c:currentList) {
                                if (c.id != comment.id) {
                                    itemKeyedDataSource.data.add(c);
                                }
                            }
                            PagedList<Comment> newPagedList = itemKeyedDataSource.buildNewPagedList(currentList.getConfig());
                            submitList(newPagedList);
                        }
                    }
                });
            }
        });
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private final com.passion.navapp.databinding.LayoutFeedCommentListItemBinding mBinding;

        public ViewHolder(@NonNull View itemView, LayoutFeedCommentListItemBinding binding) {
            super(itemView);
            mBinding = binding;
        }

        public void bindData(Comment comment) {
            mBinding.setOwner((LifecycleOwner) mContext);
            mBinding.setComment(comment);
            mBinding.labelAuthor.setVisibility(UserManager.get().getUserId()==comment.author.userId?View.VISIBLE:View.GONE);
            mBinding.commentDelete.setVisibility(UserManager.get().getUserId()==comment.author.userId?View.VISIBLE:View.GONE);
            if (!TextUtils.isEmpty(comment.imageUrl)) {
                mBinding.commentCover.setVisibility(View.VISIBLE);
                mBinding.commentCover.bindData(comment.width, comment.height, 0, PixUtils.dp2Px(200), PixUtils.dp2Px(200), comment.imageUrl);
                if (!TextUtils.isEmpty(comment.videoUrl)) {
                    mBinding.videoIcon.setVisibility(View.VISIBLE);
                } else {
                    mBinding.videoIcon.setVisibility(View.GONE);
                }
            } else {
                mBinding.commentCover.setVisibility(View.GONE);
                mBinding.videoIcon.setVisibility(View.GONE);
            }
        }
    }
}
