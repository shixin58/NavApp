package com.passion.navapp.ui.login;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import androidx.arch.core.executor.ArchTaskExecutor;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.passion.libcommon.AppGlobals;
import com.passion.libnetwork.ApiResponse;
import com.passion.libnetwork.ApiService;
import com.passion.libnetwork.JsonCallback;
import com.passion.libnetwork.cache.CacheManager;
import com.passion.navapp.model.User;

public class UserManager {
    private static final String KEY_CACHE_USER = "cache_user";
    private static volatile UserManager sInstance;

    private final MutableLiveData<User> userLiveData = new MutableLiveData<>();
    private User mUser;

    private UserManager() {
        User cache = (User) CacheManager.getCache(KEY_CACHE_USER);
        if (cache!=null && cache.expires_time > System.currentTimeMillis()) {
            mUser = cache;
        }
    }

    public static UserManager get() {
        if (sInstance==null) {
            synchronized (UserManager.class) {
                if (sInstance==null) {
                    sInstance = new UserManager();
                }
            }
        }
        return sInstance;
    }

    public void saveUser(User user) {
        mUser = user;
        CacheManager.save(KEY_CACHE_USER, user);
        if (userLiveData.hasObservers()) {
            userLiveData.postValue(mUser);
        }
    }

    /**
     * 统一登录入口
     * @param ctx
     * @return 供调用方监听返回值，但不能发送数据
     */
    public LiveData<User> login(Context ctx) {
        Intent intent = new Intent(ctx, LoginActivity.class);
        // AndroidRuntimeException: Calling startActivity() from outside of an Activity  context requires the FLAG_ACTIVITY_NEW_TASK flag
        if (!(ctx instanceof Activity)) {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        }
        ctx.startActivity(intent);
        return userLiveData;
    }

    public boolean isLogin() {
        return mUser != null && mUser.expires_time > System.currentTimeMillis();
    }

    public User getUser() {
        return isLogin()?mUser:null;
    }

    public long getUserId() {
        return isLogin()? mUser.userId : 0;
    }

    public LiveData<User> refresh() {
        if (!isLogin()) {
            return login(AppGlobals.getApplication());
        }

        MutableLiveData<User> liveData = new MutableLiveData<>();
        ApiService.get("/user/query")
                .addParam("userId", getUserId())
                .execute(new JsonCallback<User>() {
                    @Override
                    public void onSuccess(ApiResponse<User> response) {
                        saveUser(response.body);
                        liveData.postValue(getUser());
                    }

                    @SuppressLint("RestrictedApi")
                    @Override
                    public void onError(ApiResponse<User> response) {
                        ArchTaskExecutor.getMainThreadExecutor().execute(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(AppGlobals.getApplication(), response.message, Toast.LENGTH_SHORT)
                                        .show();
                            }
                        });
                        liveData.postValue(null);
                    }
                });
        return liveData;
    }

    public void logout() {
        CacheManager.deleteCache(KEY_CACHE_USER, mUser);
        mUser = null;
    }
}
