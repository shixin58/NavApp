package com.passion.navapp.view;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.databinding.BindingAdapter;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestBuilder;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.passion.libcommon.PixUtils;

public class PPImageView extends AppCompatImageView {
    public PPImageView(@NonNull Context context) {
        super(context);
    }

    public PPImageView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public PPImageView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @BindingAdapter(value = {"imageUrl", "isCircle"}, requireAll = false)
    public static void setImageUrl(PPImageView imageView, String imageUrl, boolean isCircle) {
//        imageView.getContext()
        RequestBuilder<Drawable> builder = Glide.with(imageView)
                .load(imageUrl);
        if (isCircle) {
            builder.transform(new CircleCrop());
        }
        ViewGroup.LayoutParams layoutParams = imageView.getLayoutParams();
        if (layoutParams!=null && layoutParams.width>0 && layoutParams.height>0) {
            builder.override(layoutParams.width, layoutParams.height);
        }
        builder.into(imageView);
    }

    public void bindData(int widthPx, int heightPx, int marginLeft, String imageUrl) {
        bindData(widthPx, heightPx, marginLeft, PixUtils.getScreenWidth(), PixUtils.getScreenHeight(), imageUrl);
    }

    public void bindData(int widthPx, int heightPx, int marginLeft, int maxWidth, int maxHeight, String imageUrl) {
        if (widthPx<=0 || heightPx<=0) {
            Glide.with(this).load(imageUrl).into(new SimpleTarget<Drawable>() {
                @Override
                public void onResourceReady(@NonNull Drawable resource, @Nullable Transition<? super Drawable> transition) {
                    int width = resource.getIntrinsicWidth();
                    int height = resource.getIntrinsicHeight();
                    setSize(width, height, marginLeft, maxWidth, maxHeight);
                    setImageDrawable(resource);
                }
            });
            return;
        }

        setSize(widthPx, heightPx, marginLeft, maxWidth, maxHeight);
        setImageUrl(this, imageUrl, false);
    }

    /**
     * 设置显示宽高
     * @param width
     * @param height
     * @param marginLeft 单位为dp
     * @param maxWidth 屏幕宽度
     * @param maxHeight 屏幕高度
     */
    private void setSize(int width, int height, int marginLeft, int maxWidth, int maxHeight) {
        int finalWidth, finalHeight;
        if (width >= height) {
            finalWidth = maxWidth;
            finalHeight = (int) (height/(width*1.0f/finalWidth));
        } else {
            finalHeight = maxHeight;
            finalWidth = (int) (width/(height*1.0f/finalHeight));
        }
        ViewGroup.MarginLayoutParams lp = new ViewGroup.MarginLayoutParams(finalWidth, finalHeight);
        lp.leftMargin = width>=height?0: PixUtils.dp2Px(marginLeft);
        setLayoutParams(lp);
    }
}