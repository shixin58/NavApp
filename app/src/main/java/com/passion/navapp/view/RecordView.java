package com.passion.navapp.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.passion.libcommon.PixUtils;
import com.passion.navapp.R;

public class RecordView extends View implements View.OnClickListener, View.OnLongClickListener {
    private static final int PROGRESS_INTERVAL = 100;

    private int mProgressMaxValue;

    private final int radius;// format=dimension
    private final int progressWidth;// dimension
    private final int progressColor;// color
    private final int fillColor;// color
    private final int duration;// integer

    private Paint mFillPaint;
    private Paint mProgressPaint;

    private int mProgressValue;
    private boolean mIsRecording;
    private long mStartRecordTimeMillis;
    private OnRecordListener mOnRecordListener;

    public RecordView(Context context) {
        this(context, null);
    }

    public RecordView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public RecordView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public RecordView(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.RecordView, defStyleAttr, defStyleRes);
        radius = typedArray.getDimensionPixelOffset(R.styleable.RecordView_radius, 0);
        progressWidth = typedArray.getDimensionPixelOffset(R.styleable.RecordView_progress_width, PixUtils.dp2Px(3));
        progressColor = typedArray.getColor(R.styleable.RecordView_progress_color, Color.RED);
        fillColor = typedArray.getColor(R.styleable.RecordView_fill_color, Color.WHITE);
        duration = typedArray.getInteger(R.styleable.RecordView_duration, 10);// 默认10秒
        typedArray.recycle();
        setMaxDuration(duration);

        mFillPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mFillPaint.setColor(fillColor);
        mFillPaint.setStyle(Paint.Style.FILL);

        mProgressPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mProgressPaint.setColor(progressColor);
        mProgressPaint.setStyle(Paint.Style.STROKE);
        mProgressPaint.setStrokeWidth(progressWidth);

        Handler handler = new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(@NonNull Message msg) {
                super.handleMessage(msg);
                mProgressValue++;
                postInvalidate();
                if (mProgressValue < mProgressMaxValue) {
                    sendEmptyMessageDelayed(0, PROGRESS_INTERVAL);
                } else {
                    finishRecording();
                }
            }
        };
        setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    mIsRecording = true;
                    mStartRecordTimeMillis = System.currentTimeMillis();
                    handler.sendEmptyMessage(0);
                } else if (event.getAction() == MotionEvent.ACTION_UP) {
                    long now = System.currentTimeMillis();
                    if (now - mStartRecordTimeMillis >= ViewConfiguration.getLongPressTimeout()) {
                        finishRecording();
                    }
                    handler.removeCallbacksAndMessages(null);
                    mIsRecording = false;
                    mStartRecordTimeMillis = 0;
                    mProgressValue = 0;
                    postInvalidate();
                }
                return false;
            }
        });

        setOnClickListener(this);
        setOnLongClickListener(this);
    }

    private void finishRecording() {
        if (mOnRecordListener != null) {
            mOnRecordListener.onFinish();
        }
    }

    private void setMaxDuration(int maxDuration) {
        this.mProgressMaxValue = maxDuration * 1000 / PROGRESS_INTERVAL;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int width = getWidth();
        int height = getHeight();
        if (mIsRecording) {
            canvas.drawCircle(width/2f, height/2f, width/2f, mFillPaint);
            float left = 0;
            float right = width;
            float top = 0;
            float bottom = height;
            float sweepAngle = (mProgressValue * 1.0f / mProgressMaxValue) * 360;
            // wedge扇形
            canvas.drawArc(left, top, right, bottom, -90, sweepAngle, false, mProgressPaint);
        } else {
            canvas.drawCircle(width/2f, height/2f, radius, mFillPaint);
        }
    }

    public void setOnRecordListener(OnRecordListener onRecordListener) {
        mOnRecordListener = onRecordListener;
    }

    @Override
    public void onClick(View v) {
        if (mOnRecordListener != null) {
            mOnRecordListener.onClick();
        }
    }

    @Override
    public boolean onLongClick(View v) {
        if (mOnRecordListener != null) {
            mOnRecordListener.onLongClick();
        }
        return true;
    }

    public interface OnRecordListener {
        void onClick();

        void onLongClick();

        void onFinish();
    }
}
