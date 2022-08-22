package com.passion.navapp.ui.detail;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.lifecycle.LifecycleOwner;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.passion.libcommon.PixUtils;
import com.passion.libcommon.extension.AbsPagedListAdapter;
import com.passion.navapp.databinding.LayoutFeedCommentListItemBinding;
import com.passion.navapp.model.Comment;
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
