package com.passion.navapp.ui.home;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.databinding.ViewDataBinding;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.Observer;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.passion.libcommon.extension.AbsPagedListAdapter;
import com.passion.libcommon.extension.LiveDataBus;
import com.passion.navapp.BR;
import com.passion.navapp.R;
import com.passion.navapp.databinding.LayoutFeedTypeImageBinding;
import com.passion.navapp.databinding.LayoutFeedTypeVideoBinding;
import com.passion.navapp.model.Feed;
import com.passion.navapp.ui.InteractionPresenter;
import com.passion.navapp.ui.detail.FeedDetailActivity;
import com.passion.navapp.view.ListPlayerView;

public class FeedAdapter extends AbsPagedListAdapter<Feed, FeedAdapter.ViewHolder> {
    private final LayoutInflater mInflater;
    protected final String mCategory;
    protected final Context mContext;

    private FeedObserver mFeedObserver;

    protected FeedAdapter(Context context, String category) {
        // 差分回调DiffUtil.ItemCallback由具体类传入，需重写model类equals方法
        super(new DiffUtil.ItemCallback<Feed>(){
            @Override
            public boolean areItemsTheSame(@NonNull Feed oldItem, @NonNull Feed newItem) {
                return oldItem.id==newItem.id;
            }

            @Override
            public boolean areContentsTheSame(@NonNull Feed oldItem, @NonNull Feed newItem) {
                return oldItem.equals(newItem);
            }
        });

        mContext = context;
        mInflater = LayoutInflater.from(context);
        mCategory = category;
    }

    @Override
    protected int getItemViewType2(int position) {
        Feed feed = getItem(position);
        if (feed.itemType == Feed.TYPE_IMAGE_TEXT) {
            return R.layout.layout_feed_type_image;
        } else if (feed.itemType == Feed.TYPE_VIDEO) {
            return R.layout.layout_feed_type_video;
        }
        return 0;
    }

    @Override
    protected ViewHolder onCreateViewHolder2(ViewGroup parent, int viewType) {
        ViewDataBinding binding = DataBindingUtil.inflate(mInflater, viewType, parent, false);
        return new ViewHolder(binding.getRoot(), binding);
    }

    @Override
    protected void onBindViewHolder2(ViewHolder holder, int position) {
        final Feed feed = getItem(position);
        holder.bindData(feed);
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int pos = holder.getAdapterPosition();
                if (pos != RecyclerView.NO_POSITION) {
                    // 视频详情页无缝续播用到mCategory
                    FeedDetailActivity.openActivity(mContext, getItem(pos), mCategory);
                    onStartFeedDetailActivity(feed);
                    if (mFeedObserver == null) {
                        mFeedObserver = new FeedObserver();
                        LiveDataBus.get()
                                .with(InteractionPresenter.DATA_FROM_INTERACTION)
                                .observe((LifecycleOwner) mContext, mFeedObserver);
                    }
                    mFeedObserver.setFeed(feed);
                }
            }
        });
    }

    protected void onStartFeedDetailActivity(Feed feed) {}

    private static class FeedObserver implements Observer<Feed> {
        private Feed mFeed;

        @Override
        public void onChanged(Feed newOne) {
            if (mFeed.id != newOne.id) return;

            mFeed.author = newOne.author;
            mFeed.ugc = newOne.ugc;
            mFeed.notifyChange();
        }

        public void setFeed(Feed feed) {
            mFeed = feed;
        }
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private final ViewDataBinding mBinding;
        private ListPlayerView listPlayerView;
        public ImageView feedImage;

        public ViewHolder(@NonNull View itemView, ViewDataBinding binding) {
            super(itemView);
            mBinding = binding;
        }

        public void bindData(Feed item) {
            mBinding.setVariable(BR.feed, item);
            if (mBinding instanceof LayoutFeedTypeImageBinding) {
                LayoutFeedTypeImageBinding imageBinding = (LayoutFeedTypeImageBinding) mBinding;
                imageBinding.setOwner((LifecycleOwner) mContext);
                imageBinding.feedImage.bindData(item.width, item.height, 16, item.cover);
                feedImage = imageBinding.feedImage;
            } else {
                LayoutFeedTypeVideoBinding videoBinding = (LayoutFeedTypeVideoBinding) mBinding;
                videoBinding.setOwner((LifecycleOwner) mContext);
                videoBinding.listPlayerView.bindData(mCategory, item.width, item.height, item.cover, item.url);
                listPlayerView = videoBinding.listPlayerView;
            }
        }

        public boolean isVideoItem() {
            return mBinding instanceof LayoutFeedTypeVideoBinding;
        }

        public ListPlayerView getListPlayerView() {
            return listPlayerView;
        }
    }
}
