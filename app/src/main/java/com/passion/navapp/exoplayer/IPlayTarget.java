package com.passion.navapp.exoplayer;

import android.view.ViewGroup;

public interface IPlayTarget {
    // PlayerView所在容器，用于计算位置
    ViewGroup getOwner();

    // 播放
    void onActive();

    // 停止播放
    void inactive();

    boolean isPlaying();
}
