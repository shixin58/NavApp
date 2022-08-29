package com.passion.libcommon;

import android.content.res.TypedArray;
import android.graphics.Outline;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewOutlineProvider;

public class ViewHelper {
    public static final int RADIUS_ALL = 0;
    public static final int RADIUS_LEFT = 1;
    public static final int RADIUS_TOP = 2;
    public static final int RADIUS_RIGHT = 3;
    public static final int RADIUS_BOTTOM = 4;

    public static void setViewOutline(View v, AttributeSet set, int defStyleAttr, int defStyleRes) {
        TypedArray typedArray = v.getContext()
                .obtainStyledAttributes(set, R.styleable.ViewOutlineStrategy, defStyleAttr, defStyleRes);
        // format=dimension
        int radius = typedArray.getDimensionPixelOffset(R.styleable.ViewOutlineStrategy_radius, 0);
        // format=enum
        int radiusSide = typedArray.getIndex(R.styleable.ViewOutlineStrategy_radiusSide);
        typedArray.recycle();

        setViewOutline(v, radius, radiusSide);
    }

    public static void setViewOutline(View view, final int radius, final int radiusSide) {
        view.setOutlineProvider(new ViewOutlineProvider() {
            @Override
            public void getOutline(View view, Outline outline) {
                int width = view.getWidth();
                int height = view.getHeight();
                if (width<=0 || height<=0) { return; }

                if (radius <= 0) {
                    outline.setRect(0, 0, width, height);
                    return;
                }

                if (radiusSide != RADIUS_ALL) {
                    int left = 0, top = 0, right = width, bottom = height;

                    if (radiusSide == RADIUS_LEFT) {
                        right += radius;
                    } else if (radiusSide == RADIUS_TOP) {
                        bottom += radius;
                    } else if (radiusSide == RADIUS_RIGHT) {
                        left -= radius;
                    } else if (radiusSide == RADIUS_BOTTOM) {
                        top -= radius;
                    }
                    outline.setRoundRect(left, top, right, bottom, radius);
                    return;
                }

                outline.setRoundRect(0, 0, width, height, radius);
            }
        });
        view.setClipToOutline(radius > 0);
        view.invalidate();
    }
}
