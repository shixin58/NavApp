package com.passion.navapp.ui.detail;

import android.view.LayoutInflater;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.passion.navapp.databinding.ActivityFeedDetailTypeImageBinding;
import com.passion.navapp.databinding.LayoutFeedDetailTypeImageHeaderBinding;
import com.passion.navapp.model.Feed;
import com.passion.navapp.view.PPImageView;

public class ImageViewHandler extends ViewHandler {
    private final ActivityFeedDetailTypeImageBinding mImageBinding;
    private LayoutFeedDetailTypeImageHeaderBinding mHeaderBinding;

    public ImageViewHandler(FragmentActivity activity) {
        super(activity);
        mImageBinding = ActivityFeedDetailTypeImageBinding.inflate(activity.getLayoutInflater());
        activity.setContentView(mImageBinding.getRoot());

        mRecyclerView = mImageBinding.recyclerView;
        mBottomInteractionBinding = mImageBinding.bottomInteractionLayout;
    }

    @Override
    public void bindInitData(Feed feed) {
        super.bindInitData(feed);
        mImageBinding.setFeed(mFeed);
        mImageBinding.setOwner(mActivity);

        mHeaderBinding = LayoutFeedDetailTypeImageHeaderBinding.inflate(
                LayoutInflater.from(mActivity), mRecyclerView, false);
        mHeaderBinding.setFeed(mFeed);
        mHeaderBinding.setOwner(mActivity);
        PPImageView headerImage = mHeaderBinding.headerImage;
        headerImage.bindData(feed.width, feed.height, feed.width>=feed.height?0:16, feed.cover);
        mCommentAdapter.addHeaderView(mHeaderBinding.getRoot());

        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                boolean visible = mHeaderBinding.getRoot().getTop() <= -mImageBinding.titleLayout.getMeasuredHeight();
                mImageBinding.authorInfoLayout.getRoot().setVisibility(visible? View.VISIBLE:View.GONE);
                mImageBinding.title.setVisibility(visible?View.GONE:View.VISIBLE);
            }
        });
    }
}
