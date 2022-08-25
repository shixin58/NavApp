package com.passion.navapp.ui.detail;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.passion.navapp.model.Feed;

public class FeedDetailActivity extends AppCompatActivity {
    private static final String KEY_FEED = "key_feed";
    private static final String KEY_CATEGORY = "key_category";

    // Feed binder跨进程传递，经过序列化和反序列化，列表和详情不再是同个对象，改了数据不会自动同步
    public static void openActivity(Context context, Feed item, String category) {
        Intent intent = new Intent(context, FeedDetailActivity.class);
        intent.putExtra(KEY_FEED, item);
        intent.putExtra(KEY_CATEGORY, category);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final Feed feed = (Feed) getIntent().getSerializableExtra(KEY_FEED);
        if (feed == null) {
            finish();
            return;
        }

        ViewHandler viewHandler;
        if (feed.itemType == Feed.TYPE_IMAGE_TEXT) {
            viewHandler = new ImageViewHandler(this);
        } else {
            viewHandler = new VideoViewHandler(this);
        }
        viewHandler.bindInitData(feed);
    }
}