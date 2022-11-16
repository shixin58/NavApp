package com.passion.navapp.ui.publish;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.android.exoplayer2.DefaultLoadControl;
import com.google.android.exoplayer2.DefaultRenderersFactory;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.analytics.AnalyticsCollector;
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory;
import com.google.android.exoplayer2.source.DefaultMediaSourceFactory;
import com.google.android.exoplayer2.source.ProgressiveMediaSource;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DataSpec;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.upstream.DefaultHttpDataSource;
import com.google.android.exoplayer2.upstream.FileDataSource;
import com.google.android.exoplayer2.upstream.TransferListener;
import com.google.android.exoplayer2.util.Clock;
import com.google.android.exoplayer2.util.Util;
import com.passion.navapp.databinding.ActivityPreviewBinding;

import java.io.File;

public class PreviewActivity extends AppCompatActivity implements View.OnClickListener {
    public static final String KEY_PREVIEW_URL = "key_preview_url";
    public static final String KEY_IS_VIDEO = "key_is_video";
    public static final String KEY_BTN_TXT = "key_btn_txt";

    public static final int REQ_PREVIEW = 1000;

    private ActivityPreviewBinding mBinding;

    private SimpleExoPlayer mExoPlayer;

    /**
     *
     * @param activity
     * @param previewUrl 本地文件filePath，或网络url如imageUrl、videoUrl
     * @param isVideo 预览视频还是图片
     * @param btnTxt null不显示完成按钮；非空显示完成按钮、点击完成返回图片路径。
     */
    public static void openActivityForResult(Activity activity, String previewUrl, boolean isVideo, String btnTxt) {
        Intent intent = new Intent(activity, PreviewActivity.class);
        intent.putExtra(KEY_PREVIEW_URL, previewUrl);
        intent.putExtra(KEY_IS_VIDEO, isVideo);
        intent.putExtra(KEY_BTN_TXT, btnTxt);
        activity.startActivityForResult(intent, REQ_PREVIEW);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = ActivityPreviewBinding.inflate(getLayoutInflater());
        setContentView(mBinding.getRoot());

        String previewUrl = getIntent().getStringExtra(KEY_PREVIEW_URL);
        boolean isVideo = getIntent().getBooleanExtra(KEY_IS_VIDEO, false);
        String btnTxt = getIntent().getStringExtra(KEY_BTN_TXT);

        if (TextUtils.isEmpty(btnTxt)) {
            mBinding.actionOk.setVisibility(View.GONE);
        } else {
            mBinding.actionOk.setVisibility(View.VISIBLE);
            mBinding.actionOk.setText(btnTxt);
            mBinding.actionOk.setOnClickListener(this);
        }
        mBinding.actionClose.setOnClickListener(this);

        if (isVideo) {
            previewVideo(previewUrl);
        } else {
            previewImage(previewUrl);
        }
    }

    private void previewImage(String previewUrl) {
        // 用PhotoView预览图片，PhotoView是AppCompatImageView子类
        mBinding.photoView.setVisibility(View.VISIBLE);
        Glide.with(this)
                .load(previewUrl)
                .into(mBinding.photoView);
    }

    private void previewVideo(String previewUrl) {
        // 1）初始化PlayerView
        mBinding.playerView.setVisibility(View.VISIBLE);

        // 2）创建ExoPlayer。用ExoPlayer播放本地视频，实现预览
        mExoPlayer = new SimpleExoPlayer.Builder(this,
                new DefaultRenderersFactory(this),
                new DefaultTrackSelector(this),
                new DefaultMediaSourceFactory(this, new DefaultExtractorsFactory()),
                new DefaultLoadControl(),
                DefaultBandwidthMeter.getSingletonInstance(this),
                new AnalyticsCollector(Clock.DEFAULT))
                .build();

        // 3）将本地或网络url转化为Uri，供MediaSource使用
        Uri uri = null;
        File file = new File(previewUrl);
        if (file.exists()) {// 本地文件，如发布预览
            DataSpec dataSpec = new DataSpec(Uri.fromFile(file));
            FileDataSource fileDataSource = new FileDataSource();
            try {
                fileDataSource.open(dataSpec);
                uri = fileDataSource.getUri();
            } catch (FileDataSource.FileDataSourceException e) {
                e.printStackTrace();
            }
        } else {// 网络url，如点击item图片预览
            uri = Uri.parse(previewUrl);
        }

        // 4）创建MediaSource
        String userAgent = Util.getUserAgent(this, getPackageName());
        ProgressiveMediaSource.Factory mediaSourceFactory = new ProgressiveMediaSource.Factory(
                // DefaultDataSource会将网络请求委托给DefaultHttpDataSource
                new DefaultDataSourceFactory(this, new TransferListener() {
                    @Override
                    public void onTransferInitializing(DataSource source, DataSpec dataSpec, boolean isNetwork) {}

                    @Override
                    public void onTransferStart(DataSource source, DataSpec dataSpec, boolean isNetwork) {}

                    @Override
                    public void onBytesTransferred(DataSource source, DataSpec dataSpec, boolean isNetwork, int bytesTransferred) {}

                    @Override
                    public void onTransferEnd(DataSource source, DataSpec dataSpec, boolean isNetwork) {}
                }, new DefaultHttpDataSource.Factory().setUserAgent(userAgent)));
        MediaItem mediaItem = new MediaItem.Builder()
                .setUri(uri)
                .build();
        ProgressiveMediaSource mediaSource = mediaSourceFactory.createMediaSource(mediaItem);

        // 5）加载MediaSource，将PlayerView绑定到SimpleExoPlayer，开始播放
        mExoPlayer.setMediaSource(mediaSource);
        mExoPlayer.prepare();
        mExoPlayer.setPlayWhenReady(true);
        mBinding.playerView.setPlayer(mExoPlayer);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mExoPlayer != null) {
            mExoPlayer.setPlayWhenReady(true);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mExoPlayer != null) {
            mExoPlayer.setPlayWhenReady(false);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // 页面退出时销毁播放器
        if (mExoPlayer != null) {
            mExoPlayer.setPlayWhenReady(false);
            mExoPlayer.stop(true);
            mExoPlayer.release();
            mExoPlayer = null;
        }
    }

    @Override
    public void onClick(View v) {
        if (v == mBinding.actionClose) {
            finish();
        } else if (v == mBinding.actionOk) {
            Intent resultData = new Intent();
            setResult(Activity.RESULT_OK, resultData);
            finish();
        }
    }
}
