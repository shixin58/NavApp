package com.passion.navapp.ui.my;

import android.content.Context;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.lifecycle.LifecycleOwner;
import androidx.paging.ItemKeyedDataSource;
import androidx.paging.PagedList;

import com.passion.navapp.R;
import com.passion.navapp.model.Feed;
import com.passion.navapp.ui.InteractionPresenter;
import com.passion.navapp.ui.MutableItemKeyedDataSource;
import com.passion.navapp.ui.home.FeedAdapter;
import com.passion.navapp.utils.TimeUtils;

public class ProfileListAdapter extends FeedAdapter {
    protected ProfileListAdapter(Context context, @ProfileActivity.TabType String category) {
        super(context, category);
    }

    @Override
    protected int getItemViewType2(int position) {
        if (TextUtils.equals(mCategory, ProfileActivity.TabType.TAB_TYPE_COMMENT)) {
            return R.layout.layout_feed_type_comment;
        }
        return super.getItemViewType2(position);
    }

    @Override
    protected void onBindViewHolder2(ViewHolder holder, int position) {
        super.onBindViewHolder2(holder, position);
        View dissView = holder.itemView.findViewById(R.id.diss);
        View deleteView = holder.itemView.findViewById(R.id.delete);
        TextView createTimeView = holder.itemView.findViewById(R.id.create_time);

        Feed item = getItem(position);
        createTimeView.setVisibility(View.VISIBLE);
        createTimeView.setText(TimeUtils.calculate(item.createTime));

        boolean isCommentTab = TextUtils.equals(mCategory, ProfileActivity.TabType.TAB_TYPE_COMMENT);
        if (isCommentTab) {
            dissView.setVisibility(View.GONE);
        }

        deleteView.setOnClickListener(v -> {
            if (isCommentTab) {
                InteractionPresenter.deleteFeedComment(mContext, item.itemId, item.topComment.commentId)
                        .observe((LifecycleOwner) mContext, success -> {
                            refreshList(item);
                        });
            } else {
                InteractionPresenter.deleteFeed(mContext, item.itemId)
                        .observe((LifecycleOwner) mContext, success -> {
                            refreshList(item);
                        });
            }
        });
    }

    private void refreshList(Feed delete) {
        PagedList<Feed> currentList = getCurrentList();
        MutableItemKeyedDataSource<Integer, Feed> itemKeyedDataSource = new MutableItemKeyedDataSource<>((ItemKeyedDataSource) currentList.getDataSource()) {
            @NonNull
            @Override
            public Integer getKey(@NonNull Feed item) {
                return item.id;
            }
        };
        // foreach循环一遍，过滤掉被删除的帖子
        for (Feed feed : currentList) {
            if (feed.id != delete.id) {
                itemKeyedDataSource.data.add(feed);
            }
        }
        PagedList<Feed> pagedList = itemKeyedDataSource.buildNewPagedList(currentList.getConfig());
        submitList(pagedList);
    }
}
