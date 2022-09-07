package com.passion.navapp.ui;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.arch.core.executor.ArchTaskExecutor;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;

import com.alibaba.fastjson.JSONObject;
import com.passion.libcommon.AppGlobals;
import com.passion.libcommon.extension.LiveDataBus;
import com.passion.libnetwork.ApiResponse;
import com.passion.libnetwork.ApiService;
import com.passion.libnetwork.JsonCallback;
import com.passion.navapp.model.Comment;
import com.passion.navapp.model.Feed;
import com.passion.navapp.model.User;
import com.passion.navapp.ui.login.UserManager;

import java.util.Date;

public class InteractionPresenter {
    public static final String DATA_FROM_INTERACTION = "data_from_interaction";

    private static final String URL_TOGGLE_FEED_LIKE = "/ugc/toggleFeedLike";
    private static final String URL_TOGGLE_FEED_DISS = "/ugc/dissFeed";
    private static final String URL_SHARE = "/ugc/increaseShareCount";
    private static final String URL_TOGGLE_COMMENT_LIKE = "/ugc/toggleCommentLike";

    // 帖子点赞
    public static void toggleFeedLike(LifecycleOwner lifecycleOwner, Feed feed) {
        if (!UserManager.get().isLogin()) {
            LiveData<User> loginLiveData = UserManager.get().login(AppGlobals.getApplication());
            loginLiveData.observe(lifecycleOwner, new Observer<User>() {
                @Override
                public void onChanged(User user) {
                    if (user != null) {
                        toggleFeedLikeInternal(feed);
                    }
                    loginLiveData.removeObserver(this);
                }
            });
            return;
        }
        toggleFeedLikeInternal(feed);
    }

    private static void toggleFeedLikeInternal(Feed feed) {
        ApiService.get(URL_TOGGLE_FEED_LIKE)
                .addParam("userId", UserManager.get().getUserId())
                .addParam("itemId", feed.itemId)
                .execute(new JsonCallback<JSONObject>() {
                    @Override
                    public void onSuccess(ApiResponse<JSONObject> response) {
                        if (response.body != null) {
                            boolean hasLiked = response.body.getBoolean("hasLiked").booleanValue();
                            feed.getUgc().setHasLiked(hasLiked);
                            LiveDataBus.get()
                                    .with(DATA_FROM_INTERACTION)
                                    .postValue(feed);
                        }
                    }
                });
    }

    // 帖子踩
    public static void toggleFeedDiss(LifecycleOwner lifecycleOwner, Feed feed) {
        if (!UserManager.get().isLogin()) {
            LiveData<User> loginLiveData = UserManager.get().login(AppGlobals.getApplication());
            loginLiveData.observe(lifecycleOwner, new Observer<User>() {
                @Override
                public void onChanged(User user) {
                    if (user != null) {
                        toggleFeedDissInternal(feed);
                    }
                    loginLiveData.removeObserver(this);
                }
            });
            return;
        }
        toggleFeedDissInternal(feed);
    }

    private static void toggleFeedDissInternal(Feed feed) {
        ApiService.get(URL_TOGGLE_FEED_DISS)
                .addParam("userId", UserManager.get().getUserId())
                .addParam("itemId", feed.itemId)
                .execute(new JsonCallback<JSONObject>() {
                    @Override
                    public void onSuccess(ApiResponse<JSONObject> response) {
                        if (response.body != null) {
                            boolean hasLiked = response.body.getBoolean("hasLiked").booleanValue();
                            feed.getUgc().setHasdiss(hasLiked);
                        }
                    }
                });
    }

    // 分享帖子
    public static void openShare(Context ctx, Feed feed) {
        String url = "http://h5.aliyun.ppjoke.com/item/%s?timestamp=%s&user_id=%s";
        String format = String.format(url, feed.itemId, new Date().getTime(), UserManager.get().getUserId());
        ShareDialog shareDialog = new ShareDialog(ctx);
        shareDialog.setShareContent(format);
        shareDialog.setShareItemClickListener(v -> {
            ApiService.get(URL_SHARE)
                    .addParam("itemId", feed.itemId)
                    .execute(new JsonCallback<JSONObject>() {
                        @Override
                        public void onSuccess(ApiResponse<JSONObject> response) {
                            if (response.body != null) {
                                int count = response.body.getIntValue("count");
                                feed.getUgc().setShareCount(count);
                            }
                        }
                    });
        });
        shareDialog.show();
    }

    // 评论点赞
    public static void toggleCommentLike(LifecycleOwner lifecycleOwner, Comment comment) {
        if (!UserManager.get().isLogin()) {
            LiveData<User> liveData = UserManager.get().login(AppGlobals.getApplication());
            liveData.observe(lifecycleOwner, new Observer<User>() {
                @Override
                public void onChanged(User user) {
                    liveData.removeObserver(this);
                    if (user != null) {
                        toggleCommentLikeInternal(comment);
                    }
                }
            });
            return;
        }
        toggleCommentLikeInternal(comment);
    }

    private static void toggleCommentLikeInternal(Comment comment) {
        ApiService.get(URL_TOGGLE_COMMENT_LIKE)
                .addParam("commentId", comment.commentId)
                .addParam("userId", UserManager.get().getUserId())
                .execute(new JsonCallback<JSONObject>() {
                    @Override
                    public void onSuccess(ApiResponse<JSONObject> response) {
                        if (response.body != null) {
                            boolean hasLiked = response.body.getBooleanValue("hasLiked");
                            comment.getUgc().setHasLiked(hasLiked);
                        }
                    }
                });
    }

    // 帖子收藏
    public static void toggleFeedFavorite(LifecycleOwner lifecycleOwner, Feed feed) {
        if (!UserManager.get().isLogin()) {
            LiveData<User> liveData = UserManager.get().login(AppGlobals.getApplication());
            liveData.observe(lifecycleOwner, new Observer<User>() {
                @Override
                public void onChanged(User user) {
                    liveData.removeObserver(this);
                    if (user != null) {
                        toggleFeedFavoriteInternal(feed);
                    }
                }
            });
        }
        toggleFeedFavoriteInternal(feed);
    }

    private static void toggleFeedFavoriteInternal(Feed feed) {
        ApiService.get("/ugc/toggleFavorite")
                .addParam("itemId", feed.itemId)
                .addParam("userId", UserManager.get().getUserId())
                .execute(new JsonCallback<JSONObject>() {
                    @Override
                    public void onSuccess(ApiResponse<JSONObject> response) {
                        if (response.body != null) {
                            boolean hasFavorite = response.body.getBooleanValue("hasFavorite");
                            feed.getUgc().setHasFavorite(hasFavorite);
                            LiveDataBus.get()
                                    .with(DATA_FROM_INTERACTION)
                                    .postValue(feed);
                        }
                    }

                    @Override
                    public void onError(ApiResponse<JSONObject> response) {
                        showToast(response.message);
                    }
                });
    }

    // 关注用户
    public static void toggleFollowUser(LifecycleOwner lifecycleOwner, Feed feed) {
        if (!UserManager.get().isLogin()) {
            LiveData<User> liveData = UserManager.get().login(AppGlobals.getApplication());
            liveData.observe(lifecycleOwner, new Observer<User>() {
                @Override
                public void onChanged(User user) {
                    if (user != null) {
                        toggleFollowUserInternal(feed);
                    }
                    liveData.removeObserver(this);
                }
            });
            return;
        }
        toggleFollowUserInternal(feed);
    }

    private static void toggleFollowUserInternal(Feed feed) {
        ApiService.get("/ugc/toggleUserFollow")
                .addParam("followUserId", UserManager.get().getUserId())
                .addParam("userId", feed.author.userId)
                .execute(new JsonCallback<JSONObject>() {
                    @Override
                    public void onSuccess(ApiResponse<JSONObject> response) {
                        if (response != null) {
                            boolean hasFollow = response.body.getBooleanValue("hasLiked");
                            feed.getAuthor().setHasFollow(hasFollow);
                            LiveDataBus.get()
                                    .with(DATA_FROM_INTERACTION)
                                    .postValue(feed);
                        }
                    }

                    @Override
                    public void onError(ApiResponse<JSONObject> response) {
                        showToast(response.message);
                    }
                });
    }

    public static LiveData<Boolean> deleteFeedComment(Context context, long itemId, long commentId) {
        MutableLiveData<Boolean> liveData = new MutableLiveData<>();
        new AlertDialog.Builder(context)
                .setNegativeButton("删除", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        deleteFeedComment(liveData, itemId, commentId);
                    }
                })
                .setPositiveButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .setMessage("确定要删除这条评论么？")
                .create()
                .show();
        return liveData;
    }

    private static void deleteFeedComment(MutableLiveData<Boolean> liveData, long itemId, long commentId) {
        ApiService.get("/comment/deleteComment")
                .addParam("commentId", commentId)
                .addParam("itemId", itemId)
                .addParam("userId", UserManager.get().getUserId())
                .execute(new JsonCallback<JSONObject>() {
                    @Override
                    public void onSuccess(ApiResponse<JSONObject> response) {
                        if (response.body != null) {
                            boolean result = response.body.getBooleanValue("result");
                            liveData.postValue(result);
                        }
                    }

                    @Override
                    public void onError(ApiResponse<JSONObject> response) {
                        showToast(response.message);
                    }
                });
    }

    @SuppressLint("RestrictedApi")
    private static void showToast(String message) {
        ArchTaskExecutor.getMainThreadExecutor().execute(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(AppGlobals.getApplication(), message, Toast.LENGTH_SHORT).show();
            }
        });
    }
}
