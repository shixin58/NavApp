package com.passion.libcommon.extension;

import androidx.annotation.NonNull;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleEventObserver;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;

import java.util.concurrent.ConcurrentHashMap;

// 对于页面再次可见时再分发、处理消息的情形，LiveData比Handler有优势。
// 跨页面全局LiveData支持发送粘性事件。有活跃的观察者，即宿主可见(onStart以上)，LiveData才派发消息。
public class LiveDataBus {
    private static class Lazy {
        private static final LiveDataBus sLiveDataBus = new LiveDataBus();
    }

    public static LiveDataBus get() {
        return Lazy.sLiveDataBus;
    }

    private final ConcurrentHashMap<String,StickyLiveData> mHashMap = new ConcurrentHashMap<>();

    // 参考Glide#with()返回RequestManager
    public StickyLiveData with(String eventName) {
        StickyLiveData liveData = mHashMap.get(eventName);
        if (liveData == null) {
            liveData = new StickyLiveData(eventName);
            mHashMap.put(eventName, liveData);
        }
        return liveData;
    }

    public class StickyLiveData<T> extends LiveData<T> {
        private final String mEventName;

        // 区别LiveData#mData
        private T mStickyData;

        // 覆盖了LiveData#mVersion。
        // 标记LiveData发送了几次事件，用以过滤老数据重复接收
        private int mVersion = 0;

        public StickyLiveData(String eventName) {
            super();// 默认调用父类空参构造
            mEventName = eventName;
        }

        @Override
        public void setValue(T value) {
            mVersion++;
            super.setValue(value);
        }

        @Override
        public void postValue(T value) {
            mVersion++;
            super.postValue(value);
        }

        public void setStickyData(T stickyData) {
            mStickyData = stickyData;
            setValue(stickyData);
        }

        public void postStickyData(T stickyData) {
            mStickyData = stickyData;
            postValue(stickyData);
        }

        @Override
        public void observe(@NonNull LifecycleOwner owner, @NonNull Observer<? super T> observer) {
            // Java上边界<? extend T>，相当于Kotlin协变<out T>，用于取值(生产者)场景；
            // Java下边界<? super T>，相当于逆变<in T>，用于存值(消费者)场景。
            observeSticky(owner, observer, false);
        }

        public void observeSticky(LifecycleOwner owner, Observer<? super T> observer, boolean sticky) {
            super.observe(owner, new WrapperObserver(this, observer, sticky));
            owner.getLifecycle().addObserver(new LifecycleEventObserver() {
                @Override
                public void onStateChanged(@NonNull LifecycleOwner source, @NonNull Lifecycle.Event event) {
                    if (event == Lifecycle.Event.ON_DESTROY) {
                        mHashMap.remove(mEventName);
                    }
                }
            });
        }

        private class WrapperObserver<E> implements Observer<E> {
            private final StickyLiveData<E> mLiveData;
            private final Observer<E> mObserver;
            private final boolean mSticky;

            // 标记Observer接收过几次数据
            private int mLastVersion = 0;

            public WrapperObserver(StickyLiveData<E> liveData, Observer<E> observer, boolean sticky) {
                mLiveData = liveData;
                mObserver = observer;
                mSticky = sticky;

                mLastVersion = mLiveData.mVersion;
            }

            @Override
            public void onChanged(E t) {
                if (mLastVersion >= mLiveData.mVersion) {
                    if (mSticky && mLiveData.mStickyData != null) {
                        // 给先发送后注册的粘性观察者，发送粘性事件
                        mObserver.onChanged(mLiveData.mStickyData);
                    }
                    return;
                }

                mLastVersion = mLiveData.mVersion;
                mObserver.onChanged(t);
            }
        }
    }
}
