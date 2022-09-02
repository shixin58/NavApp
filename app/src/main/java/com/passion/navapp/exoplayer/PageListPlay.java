package com.passion.navapp.exoplayer;

import android.app.Application;
import android.view.LayoutInflater;

import com.google.android.exoplayer2.DefaultLoadControl;
import com.google.android.exoplayer2.DefaultRenderersFactory;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.analytics.AnalyticsCollector;
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory;
import com.google.android.exoplayer2.source.DefaultMediaSourceFactory;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.ui.PlayerControlView;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer2.util.Clock;
import com.passion.libcommon.AppGlobals;
import com.passion.navapp.R;

public class PageListPlay {
    public SimpleExoPlayer mExoPlayer;
    public PlayerView mPlayerView;
    public PlayerControlView mControlView;

    public PlayerControlView.VisibilityListener mVisibilityListener;
    public String playUrl;

    public PageListPlay() {
        // MediaExtractor将音视频分离，跟MediaCodec一起用于视频解码播放。
        // MediaMuxer生成音频或视频文件，将视频track和音频track合成音视频文件。
        Application app = AppGlobals.getApplication();
        // ExoPlayerFactory在v2.14.0被移除
        mExoPlayer = new SimpleExoPlayer.Builder(app,
                new DefaultRenderersFactory(app)/* 视频帧画面渲染器集合工厂类，调用createRenderers()创建Renderer[] */,
                new DefaultTrackSelector(app)/* 音视频轨道选择器 */,
                new DefaultMediaSourceFactory(app, new DefaultExtractorsFactory()),
                new DefaultLoadControl()/* buffer加载到缓存控制器 */,
                DefaultBandwidthMeter.getSingletonInstance(app),
                new AnalyticsCollector(Clock.DEFAULT))
                .build();

        mPlayerView = (PlayerView) LayoutInflater.from(app)
                .inflate(R.layout.layout_exo_player_view, null, false);

        mControlView = (PlayerControlView) LayoutInflater.from(app)
                .inflate(R.layout.layout_exo_player_controller_view, null, false);

        mPlayerView.setPlayer(mExoPlayer);
        mControlView.setPlayer(mExoPlayer);
    }

    public void release() {
        // 销毁播放器
        if (mExoPlayer != null) {
            mExoPlayer.setPlayWhenReady(false);
            mExoPlayer.stop(true);
            mExoPlayer.release();
            mExoPlayer = null;
        }

        if (mPlayerView != null) {
            mPlayerView.setPlayer(null);
            mPlayerView = null;
        }

        if (mControlView != null) {
            mControlView.setPlayer(null);
            // PlayerControlView#setVisibilityListener()在v2.14.0被替换为addVisibilityListener()
            mControlView.removeVisibilityListener(mVisibilityListener);
            mControlView = null;
        }
    }

    public void switchPlayerView(PlayerView playerView) {
        if (playerView != null && playerView != mPlayerView) {
            this.mPlayerView.setPlayer(null);
            playerView.setPlayer(mExoPlayer);
        } else {
            this.mPlayerView.setPlayer(mExoPlayer);
        }
    }
}
