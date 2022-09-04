package com.passion.navapp.ui.detail;

import android.view.LayoutInflater;
import android.view.View;

import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.fragment.app.FragmentActivity;

import com.passion.navapp.R;
import com.passion.navapp.databinding.ActivityFeedDetailTypeVideoBinding;
import com.passion.navapp.databinding.LayoutFeedDetailTypeVideoHeaderBinding;
import com.passion.navapp.model.Feed;
import com.passion.navapp.view.FullscreenPlayerView;

public class VideoViewHandler extends ViewHandler {
    private final ActivityFeedDetailTypeVideoBinding mVideoBinding;
    private LayoutFeedDetailTypeVideoHeaderBinding mHeaderBinding;
    private String mCategory;
    private boolean mBackPressed;

    private final CoordinatorLayout mCoordinatorLayout;
    private final FullscreenPlayerView mPlayerView;

    public VideoViewHandler(FragmentActivity activity) {
        super(activity);
        mVideoBinding = ActivityFeedDetailTypeVideoBinding.inflate(activity.getLayoutInflater());
        activity.setContentView(mVideoBinding.getRoot());

        mRecyclerView = mVideoBinding.recyclerView;
        mBottomInteractionBinding = mVideoBinding.bottomInteractionLayout;
        mPlayerView = mVideoBinding.playerView;
        mCoordinatorLayout = mVideoBinding.coordinator;

        View authorInfoView = mVideoBinding.authorInfoLayout.getRoot();
        CoordinatorLayout.LayoutParams authorInfoParams = (CoordinatorLayout.LayoutParams) authorInfoView.getLayoutParams();
        authorInfoParams.setBehavior(new ViewAnchorBehavior(R.id.player_view));

        CoordinatorLayout.LayoutParams playerParams = (CoordinatorLayout.LayoutParams) mVideoBinding.playerView.getLayoutParams();
        ViewZoomBehavior playerBehavior = (ViewZoomBehavior) playerParams.getBehavior();
        playerBehavior.setViewZoomCallback(new ViewZoomBehavior.ViewZoomCallback() {
            @Override
            public void onDragZoom(int height) {
                int bottom = mPlayerView.getBottom();
                boolean moveUp = height < bottom;
                boolean fullscreen = moveUp ? height >= mCoordinatorLayout.getBottom()-mBottomInteractionBinding.getRoot().getHeight()
                        :height >= mCoordinatorLayout.getBottom();
                setViewAppearance(fullscreen);
            }
        });
    }

    @Override
    public void bindInitData(Feed feed) {
        super.bindInitData(feed);
        mVideoBinding.setFeed(feed);
        mVideoBinding.setOwner(mActivity);

        mCategory = mActivity.getIntent().getStringExtra(FeedDetailActivity.KEY_CATEGORY);
        mVideoBinding.playerView.bindData(mCategory, mFeed.width, mFeed.height, mFeed.cover, mFeed.url);
        mVideoBinding.playerView.post(new Runnable() {
            @Override
            public void run() {
                boolean fullscreen = mVideoBinding.playerView.getBottom() >= mVideoBinding.coordinator.getBottom();
                setViewAppearance(fullscreen);
            }
        });

        mHeaderBinding = LayoutFeedDetailTypeVideoHeaderBinding.inflate(
                LayoutInflater.from(mActivity), mRecyclerView, false);
        mHeaderBinding.setFeed(feed);
        mCommentAdapter.addHeaderView(mHeaderBinding.getRoot());
    }

    private void setViewAppearance(boolean fullscreen) {
        mVideoBinding.setFullscreen(fullscreen);
        mVideoBinding.fullscreenAuthorInfo.getRoot().setVisibility(fullscreen?View.VISIBLE:View.GONE);

        int interactionHeight = mBottomInteractionBinding.getRoot().getMeasuredHeight();
        int ctrlViewHeight = mVideoBinding.playerView.getControlView().getMeasuredHeight();
        int bottom = mVideoBinding.playerView.getControlView().getBottom();
        mVideoBinding.playerView.getControlView().setY(fullscreen?(bottom-ctrlViewHeight-interactionHeight):(bottom-ctrlViewHeight));
    }

    @Override
    public void onResume() {
        super.onResume();
        mBackPressed = false;
        mVideoBinding.playerView.onActive();
    }

    @Override
    public void onPause() {
        super.onPause();
        if (!mBackPressed) {
            mVideoBinding.playerView.inactive();
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        mBackPressed = true;
        mVideoBinding.playerView.getControlView().setTranslationY(0f);
    }
}
