package com.passion.navapp.exoplayer;

import android.app.Application;

import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.ProgressiveMediaSource;
import com.google.android.exoplayer2.upstream.DefaultHttpDataSource;
import com.google.android.exoplayer2.upstream.FileDataSource;
import com.google.android.exoplayer2.upstream.cache.Cache;
import com.google.android.exoplayer2.upstream.cache.CacheDataSink;
import com.google.android.exoplayer2.upstream.cache.CacheDataSource;
import com.google.android.exoplayer2.upstream.cache.LeastRecentlyUsedCacheEvictor;
import com.google.android.exoplayer2.upstream.cache.SimpleCache;
import com.google.android.exoplayer2.util.Util;
import com.passion.libcommon.AppGlobals;

import java.util.HashMap;

public class PageListPlayManager {
    // 管理多个页面的PageListPlay
    private static final HashMap<String,PageListPlay> sPageListPlayMap = new HashMap<>();

    private static final ProgressiveMediaSource.Factory sMediaSourceFactory;

    static {
        Application app = AppGlobals.getApplication();
        // 创建HttpDataSource工厂类，用于根据url下载视频
        DefaultHttpDataSource.Factory dataSourceFactory = new DefaultHttpDataSource.Factory()
                .setUserAgent(Util.getUserAgent(app, app.getPackageName()));

        // 创建Cache，用于保存缓存位置、大小(200MB)和策略
        // evictor清除者/逐出者，LRU算法
        Cache cache = new SimpleCache(app.getCacheDir(),
                new LeastRecentlyUsedCacheEvictor(1024 * 1024 * 200));

        // 创建缓存数据池工厂类，用于缓存写入
        CacheDataSink.Factory cacheWriteDataSinkFactory = new CacheDataSink.Factory()
                .setCache(cache)
                .setFragmentSize(Long.MAX_VALUE);

        // 创建缓存数据源工厂类，用于查询本地缓存
        CacheDataSource.Factory cacheDataSourceFactory = new CacheDataSource.Factory()
                .setCache(cache)
                .setUpstreamDataSourceFactory(dataSourceFactory)
                .setCacheReadDataSourceFactory(new FileDataSource.Factory())
                .setCacheWriteDataSinkFactory(cacheWriteDataSinkFactory)
                .setEventListener(new CacheDataSource.EventListener() {
                    @Override
                    public void onCachedBytesRead(long cacheSizeBytes, long cachedBytesRead) {}

                    @Override
                    public void onCacheIgnored(int reason) {}
                })
                // 查询本地缓存文件时，网络输入流正在写入该文件，等待写完再读取缓存
                .setFlags(CacheDataSource.FLAG_BLOCK_ON_CACHE);

        // 根据视频url创建媒体源MediaSource，供播放器使用
        sMediaSourceFactory = new ProgressiveMediaSource.Factory(cacheDataSourceFactory);
    }

    public static MediaSource createMediaSource(String url) {
        return sMediaSourceFactory.createMediaSource(MediaItem.fromUri(url));
    }

    public static PageListPlay get(String pageName) {
        PageListPlay pageListPlay = sPageListPlayMap.get(pageName);
        if (pageListPlay == null) {
            synchronized (sPageListPlayMap) {
                pageListPlay = sPageListPlayMap.get(pageName);
                if (pageListPlay == null) {
                    pageListPlay = new PageListPlay();
                    sPageListPlayMap.put(pageName, pageListPlay);
                }
            }
        }
        return pageListPlay;
    }

    public static void release(String pageName) {
        PageListPlay pageListPlay = sPageListPlayMap.remove(pageName);
        if (pageListPlay != null) {
            pageListPlay.release();
        }
    }
}
