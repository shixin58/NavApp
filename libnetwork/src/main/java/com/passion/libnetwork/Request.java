package com.passion.libnetwork;

import android.annotation.SuppressLint;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.IntDef;
import androidx.annotation.NonNull;
import androidx.arch.core.executor.ArchTaskExecutor;

import java.io.IOException;
import java.io.Serializable;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

import static com.passion.libnetwork.Request.CacheStrategy.*;

import com.passion.libnetwork.cache.CacheManager;

public abstract class Request<T, R extends Request> implements Cloneable {
    protected String mUrl;
    protected HashMap<String, String> headers = new HashMap<>();
    protected HashMap<String, Object> params = new HashMap<>();
    private String cacheKey;

    private Type mType;
//    private Class mClazz;

    @CacheStrategy
    private int mCacheStrategy;

    // StringDef, LongDef
    @Retention(RetentionPolicy.SOURCE)
    @Target({ElementType.PARAMETER, ElementType.METHOD, ElementType.FIELD})
    @IntDef({CACHE_ONLY, CACHE_FIRST, NETWORK_ONLY, NETWORK_CACHE})
    public @interface CacheStrategy {
        // 默认public static final
        int CACHE_ONLY = 1;
        int CACHE_FIRST = 2;
        int NETWORK_ONLY = 3;
        int NETWORK_CACHE = 4;
    }

    public Request(String url) {
        mUrl = url;
    }

    public R addHeader(String key, String value) {
        headers.put(key, value);
        return (R) this;
    }

    public R addParam(String key, Object value) {
        if (value == null) {
            return (R) this;
        }

        try {
            if (value.getClass()==String.class) {
                params.put(key, value);
            } else {
                Field field = value.getClass().getField("TYPE");
                Class clazz = (Class) field.get(null);
                if (clazz.isPrimitive()) {
                    params.put(key, value);
                }
            }
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }
        return (R) this;
    }

    public R cacheStrategy(@CacheStrategy int cacheStrategy) {
        mCacheStrategy = cacheStrategy;
        return (R) this;
    }

    public R cacheKey(String key) {
        this.cacheKey = key;
        return (R) this;
    }

    public R responseType(Type type) {
        mType = type;
        return (R) this;
    }

//    public R responseType(Class clazz) {
//        mClazz = clazz;
//        return (R) this;
//    }

    @SuppressLint("RestrictedApi")
    public void execute(JsonCallback<T> callback) {
        if (mCacheStrategy != NETWORK_ONLY) {
            ArchTaskExecutor.getIOThreadExecutor().execute(new Runnable() {
                @Override
                public void run() {
                    ApiResponse<T> response = readCache();
                    if (callback != null) {
                        callback.onCacheSuccess(response);
                    }
                }
            });
        }

        if (mCacheStrategy != CACHE_ONLY) {
            getCall().enqueue(new Callback() {
                @Override
                public void onFailure(@NonNull Call call, @NonNull IOException e) {
                    ApiResponse<T> apiResponse = new ApiResponse<>();
                    apiResponse.message = e.getMessage();
                    callback.onError(apiResponse);
                }

                @Override
                public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                    ApiResponse<T> apiResponse = parseResponse(response, callback);
                    if (apiResponse.success) {
                        callback.onSuccess(apiResponse);
                    } else {
                        callback.onError(apiResponse);
                    }
                }
            });
        }
    }

    private ApiResponse<T> readCache() {
        String key = TextUtils.isEmpty(cacheKey)?generateCacheKey():cacheKey;
        Object cache = CacheManager.getCache(key);
        ApiResponse<T> response = new ApiResponse<>();
        response.success = true;
        response.message = "缓存获取成功";
        response.status = 304;
        response.body = (T) cache;
        return response;
    }

    // 传递callback来获取泛型实际类型
    private ApiResponse<T> parseResponse(Response response, JsonCallback<T> callback) {
        String message = null;
        int status = response.code();
        boolean success = response.isSuccessful();
        ApiResponse<T> result = new ApiResponse<>();
        Converter converter = ApiService.sConverter;
        try {
            String content = response.body().string();
            if (success) {
                if (callback != null) {
                    ParameterizedType type = (ParameterizedType) callback.getClass().getGenericSuperclass();
                    Type argument = type.getActualTypeArguments()[0];
                    result.body = (T) converter.convert(content, argument);
                } else if (mType != null) {
                    result.body = (T) converter.convert(content, mType);
                } /*else if (mClazz != null) {
                    result.body = (T) converter.convert(content, mClazz);
                }*/ else {
                    Log.e("Request", "parseResponse: 无法解析");
                }
            } else {
                message = content;
            }
        } catch (IOException e) {
            message = e.getMessage();
            success = false;
        }
        result.success = success;
        result.status = status;
        result.message = message;

        if (mCacheStrategy!=NETWORK_ONLY && result.success && result.body instanceof Serializable) {
            saveCache(result.body);
        }
        return result;
    }

    private void saveCache(T body) {
        String key = TextUtils.isEmpty(cacheKey)?generateCacheKey():cacheKey;
        CacheManager.save(key, body);
    }

    private String generateCacheKey() {
        cacheKey = UrlCreator.createUrlFromParams(mUrl, params);
        return cacheKey;
    }

    public ApiResponse<T> execute() {
        if (mType == null) {
            throw new RuntimeException("同步方法，response返回值类型必须设置");
        }

        if (mCacheStrategy == CACHE_ONLY) {
            return readCache();
        }

        ApiResponse<T> result = null;
        try {
            Response response = getCall().execute();
            result = parseResponse(response, null);
        } catch (IOException e) {
            e.printStackTrace();
            if (result == null) {
                result = new ApiResponse<>();
                result.message = e.getMessage();
            }
        }
        return result;
    }

    private Call getCall() {
        okhttp3.Request.Builder builder = new okhttp3.Request.Builder();
        addHeaders(builder);
        okhttp3.Request request = generateRequest(builder);
        Call call = ApiService.okHttpClient.newCall(request);
        return call;
    }

    protected abstract okhttp3.Request generateRequest(okhttp3.Request.Builder builder);

    private void addHeaders(okhttp3.Request.Builder builder) {
        for (Map.Entry<String,String> entry: headers.entrySet()) {
            builder.addHeader(entry.getKey(), entry.getValue());
        }
    }

    @NonNull
    @Override
    public Request<T,R> clone() throws CloneNotSupportedException {
        return (Request<T,R>) super.clone();
    }
}
