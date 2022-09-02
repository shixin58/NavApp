package com.passion.navapp.exoplayer;

import android.view.ViewGroup;

public interface IPlayTarget {
    /**
     * 返回PlayerView/PlayerControlView所在父容器，用于计算位置
     * @return
     */
    ViewGroup getOwner();

    /**
     * onResume()触发播放
     */
    void onActive();

    /**
     * onPause()触发暂停播放
     */
    void inactive();

    boolean isPlaying();
}
