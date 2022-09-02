package com.passion.navapp.view;

import android.content.Context;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.ui.PlayerControlView;
import com.google.android.exoplayer2.ui.PlayerView;
import com.passion.libcommon.PixUtils;
import com.passion.navapp.R;
import com.passion.navapp.exoplayer.PageListPlay;
import com.passion.navapp.exoplayer.PageListPlayManager;

// 列表到详情页无缝续播：
// 1）切换时前者视频不能暂停，否则画面会停顿；
// 2）使用同个ExoPlayer，视频资源不用重新加载，没缓冲效果；
// 3）使用新的PlayerView，否则会看到View缺失现象
public class FullscreenPlayerView extends ListPlayerView {
    private final PlayerView mExoPlayerView;

    public FullscreenPlayerView(@NonNull Context context) {
        this(context, null);
    }

    public FullscreenPlayerView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public FullscreenPlayerView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        // 首次播放时PlayerView才被添加到父容器
        mExoPlayerView = (PlayerView) LayoutInflater.from(context)
                .inflate(R.layout.layout_exo_player_view, null, false);
    }

    @Override
    protected void setSize(int widthPx, int heightPx) {
        if (widthPx >= heightPx) {
            super.setSize(widthPx, heightPx);
            return;
        }

        int maxWidth = PixUtils.getScreenWidth();
        int maxHeight = PixUtils.getScreenHeight();

        ViewGroup.LayoutParams params = getLayoutParams();
        params.width = maxWidth;
        params.height = maxHeight;
        setLayoutParams(params);

        FrameLayout.LayoutParams coverParams = (LayoutParams) mCoverView.getLayoutParams();
        coverParams.width = (int) (widthPx / (heightPx * 1.0f / maxHeight));
        coverParams.height = maxHeight;
        coverParams.gravity = Gravity.CENTER;
        mCoverView.setLayoutParams(coverParams);
    }

    @Override
    public void onActive() {
        PageListPlay pageListPlay = PageListPlayManager.get(mCategory);
        PlayerView playerView = mExoPlayerView;
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
    public void inactive() {// 视频详情页退出或切后台触发onPause()->inactive()，解绑PlayerView。
        super.inactive();
        PageListPlay pageListPlay = PageListPlayManager.get(mCategory);
        pageListPlay.switchPlayerView(null);
    }
}
