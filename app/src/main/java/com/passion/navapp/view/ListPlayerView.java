package com.passion.navapp.view;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.ui.PlayerControlView;
import com.google.android.exoplayer2.ui.PlayerView;
import com.passion.libcommon.PixUtils;
import com.passion.navapp.R;
import com.passion.navapp.exoplayer.IPlayTarget;
import com.passion.navapp.exoplayer.PageListPlay;
import com.passion.navapp.exoplayer.PageListPlayManager;

import jp.wasabeef.glide.transformations.BlurTransformation;

// PlayerView和PlayerControlView的父容器，可直接用于列表视频item。
// 详情页和列表视频控件大小计算规则不同，详情页视频控件可继承ListPlayerView并重写setSize()方法。
public class ListPlayerView extends FrameLayout implements IPlayTarget,
        PlayerControlView.VisibilityListener, Player.Listener {
    private final PPImageView mBlurView;
    protected final PPImageView mCoverView;
    private final ImageView mPlayBtn;
    private final ProgressBar mBufferView;

    protected String mCategory;
    protected String mVideoUrl;

    private boolean isPlaying;

    protected int mWidthPx;
    protected int mHeightPx;

    public ListPlayerView(@NonNull Context context) {
        this(context, null);
    }

    public ListPlayerView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ListPlayerView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        LayoutInflater.from(context).inflate(R.layout.layout_player_view, this, true);

        mBlurView = findViewById(R.id.blur_background);
        mCoverView = findViewById(R.id.cover);
        mPlayBtn = findViewById(R.id.play_btn);
        mBufferView = findViewById(R.id.buffer_view);

        mPlayBtn.setOnClickListener(v -> {
            if (isPlaying()) {
                inactive();
            } else {
                onActive();
            }
        });
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        // 接管itemView点击事件
        PageListPlay pageListPlay = PageListPlayManager.get(mCategory);
        pageListPlay.mControlView.show();
        return true;
    }

    public void bindData(String category, int widthPx, int heightPx, String coverUrl, String videoUrl) {
        mCategory = category;
        mVideoUrl = videoUrl;
        mWidthPx = widthPx;
        mHeightPx = heightPx;

        PPImageView.setImageUrl(mCoverView, coverUrl, false);

        if (widthPx < heightPx) {
            setBlurImageUrl(coverUrl, 10);
            mBlurView.setVisibility(VISIBLE);
        } else {
            mBlurView.setVisibility(INVISIBLE);
        }

        setSize(widthPx, heightPx);
    }

    protected void setSize(int widthPx, int heightPx) {
        final int maxWidth = PixUtils.getScreenWidth();
        final int maxHeight = maxWidth;

        int layoutWidth = maxWidth;
        int layoutHeight = 0;

        int coverWidth;
        int coverHeight;
        if (widthPx >= heightPx) {
            coverWidth = maxWidth;
            layoutHeight = coverHeight = (int) (heightPx/(widthPx*1.0f/coverWidth));
        } else {
            layoutHeight = coverHeight = maxHeight;
            coverWidth = (int) (widthPx/(heightPx*1.0f/coverHeight));
        }

        // 设置控件大小
        ViewGroup.LayoutParams layoutParams = getLayoutParams();
        layoutParams.width = layoutWidth;
        layoutParams.height = layoutHeight;
        setLayoutParams(layoutParams);

        // 设置背景虚化View大小
        ViewGroup.LayoutParams blurParams = mBlurView.getLayoutParams();
        blurParams.width = layoutWidth;
        blurParams.height = layoutHeight;
        mBlurView.setLayoutParams(blurParams);

        // 设置封面图View大小
        LayoutParams coverParams = (LayoutParams) mCoverView.getLayoutParams();
        coverParams.width = coverWidth;
        coverParams.height = coverHeight;
        coverParams.gravity = Gravity.CENTER;
        mCoverView.setLayoutParams(coverParams);

        // 设置播放按钮居中
        LayoutParams playBtnParams = (LayoutParams) mPlayBtn.getLayoutParams();
        playBtnParams.gravity = Gravity.CENTER;
        mPlayBtn.setLayoutParams(playBtnParams);
    }

    public void setBlurImageUrl(String coverUrl, int radius) {
        Glide.with(this).load(coverUrl).override(50)
                .transform(new BlurTransformation())
                .dontAnimate()
                .into(new SimpleTarget<Drawable>() {
                    @Override
                    public void onResourceReady(@NonNull Drawable resource, @Nullable Transition<? super Drawable> transition) {
                        setBackground(resource);
                    }
                });
    }

    @Override
    public ViewGroup getOwner() {
        return this;
    }

    @Override
    public void onActive() {
        PageListPlay pageListPlay = PageListPlayManager.get(mCategory);
        PlayerView playerView = pageListPlay.mPlayerView;
        PlayerControlView controlView = pageListPlay.mControlView;
        SimpleExoPlayer exoPlayer = pageListPlay.mExoPlayer;
        if (playerView == null) {
            return;
        }
        pageListPlay.switchPlayerView(playerView);
        ViewParent viewParent = playerView.getParent();
        if (viewParent != this) {
            if (viewParent != null) {
                ((ViewGroup)viewParent).removeView(playerView);
            }
            // PlayerView位于高斯模糊图之上，大小跟封面图一致
            ViewGroup.LayoutParams lp = mCoverView.getLayoutParams();
            this.addView(playerView, 1, lp);
        }

        ViewParent ctrlParent = controlView.getParent();
        if (ctrlParent != this) {
            if (ctrlParent != null) {
                ((ViewGroup)ctrlParent).removeView(controlView);
            }
            FrameLayout.LayoutParams lp = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            lp.gravity = Gravity.BOTTOM;
            this.addView(controlView, lp);

            controlView.addVisibilityListener(this);
            pageListPlay.mVisibilityListener = this;// 用于PlayerControlView#removeVisibilityListener()删除
        }
        controlView.show();

        // app前后台切换，调用inactive()后仅需恢复播放
        if (!TextUtils.equals(pageListPlay.playUrl, mVideoUrl)) {
            MediaSource mediaSource = PageListPlayManager.createMediaSource(mVideoUrl);
            exoPlayer.setMediaSource(mediaSource);
            exoPlayer.prepare();
            exoPlayer.setRepeatMode(Player.REPEAT_MODE_ONE);// REPEAT_MODE_ONE表示无限循环播放
            // 监听正在播放还是缓存，在回调里改变页面UI。
            // EventListener在v2.14.0被Listener代替。
            // onPlayerStateChanged()被onPlaybackStateChanged()+onPlayWhenReadyChanged()代替
            exoPlayer.addListener(this);
        }
        exoPlayer.setPlayWhenReady(true);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        // 滑动过程中itemView复用，removeView()后在此做些状态重置
        isPlaying = false;
        mBufferView.setVisibility(GONE);
        mCoverView.setVisibility(VISIBLE);
        mPlayBtn.setVisibility(VISIBLE);
        mPlayBtn.setImageResource(R.drawable.icon_video_play);
    }

    @Override
    public void inactive() {
        PageListPlay pageListPlay = PageListPlayManager.get(mCategory);
        pageListPlay.mExoPlayer.setPlayWhenReady(false);
        mPlayBtn.setVisibility(VISIBLE);
        mPlayBtn.setImageResource(R.drawable.icon_video_play);
    }

    @Override
    public boolean isPlaying() {
        return isPlaying;
    }

    @Override
    public void onVisibilityChange(int visibility) {
        mPlayBtn.setVisibility(visibility);
        mPlayBtn.setImageResource(isPlaying()?R.drawable.icon_video_pause:R.drawable.icon_video_play);
    }

    @Override
    public void onPlaybackStateChanged(int state) {
        Player.Listener.super.onPlaybackStateChanged(state);
        PageListPlay pageListPlay = PageListPlayManager.get(mCategory);
        SimpleExoPlayer exoPlayer = pageListPlay.mExoPlayer;
        // STATE_READY有可能还未开始播放，需判断缓存区不为0
        if (state == Player.STATE_READY && exoPlayer.getBufferedPosition() != 0) {
            mCoverView.setVisibility(INVISIBLE);
            mBufferView.setVisibility(INVISIBLE);
        } else if (state == Player.STATE_BUFFERING) {
            mBufferView.setVisibility(VISIBLE);
        }

        isPlaying = state == Player.STATE_READY && exoPlayer.getBufferedPosition() != 0 && exoPlayer.getPlayWhenReady();
        mPlayBtn.setImageResource(isPlaying?R.drawable.icon_video_pause:R.drawable.icon_video_play);
    }

    @Override
    public void onPlayWhenReadyChanged(boolean playWhenReady, int reason) {
        Player.Listener.super.onPlayWhenReadyChanged(playWhenReady, reason);
        if (!playWhenReady) {
            isPlaying = false;
        }
    }

    public PlayerControlView getControlView() {
        PageListPlay pageListPlay = PageListPlayManager.get(mCategory);
        return pageListPlay.mControlView;
    }
}
