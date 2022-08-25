package com.passion.navapp.ui.detail;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.arch.core.executor.ArchTaskExecutor;
import androidx.fragment.app.DialogFragment;

import com.passion.libcommon.AppGlobals;
import com.passion.libcommon.PixUtils;
import com.passion.libcommon.ViewHelper;
import com.passion.libnetwork.ApiResponse;
import com.passion.libnetwork.ApiService;
import com.passion.libnetwork.JsonCallback;
import com.passion.navapp.databinding.LayoutCommentDialogBinding;
import com.passion.navapp.model.Comment;
import com.passion.navapp.ui.login.UserManager;

public class CommentDialog extends DialogFragment implements View.OnClickListener {
    private static final String KEY_ITEM_ID = "key_item_id";

    private LayoutCommentDialogBinding mBinding;
    private long mItemId;
    private CommentAddListener mCommentAddListener;

    public static CommentDialog newInstance(long itemId) {
        Bundle args = new Bundle();
        args.putLong(KEY_ITEM_ID, itemId);
        CommentDialog fragment = new CommentDialog();
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Window window = getDialog().getWindow();
        window.setWindowAnimations(0);
        mBinding = LayoutCommentDialogBinding.inflate(inflater, container, false);
        return mBinding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mBinding.commentDelete.setOnClickListener(this);
        mBinding.commentSend.setOnClickListener(this);
        mBinding.commentVideo.setOnClickListener(this);

        this.mItemId = getArguments().getLong(KEY_ITEM_ID);

        ViewHelper.setViewOutline(mBinding.getRoot(), PixUtils.dp2Px(10), ViewHelper.RADIUS_TOP);
        mBinding.getRoot().post(this::showSoftInputMethod);
        dismissWhenPressBack();
    }

    private void dismissWhenPressBack() {
        mBinding.inputView.setOnBackKeyEventListener(() -> {
            mBinding.inputView.postDelayed(new Runnable() {
                @Override
                public void run() {
                    dismiss();
                }
            }, 200L);
            return true;
        });
    }

    private void showSoftInputMethod() {
        mBinding.inputView.setFocusable(true);
        mBinding.inputView.setFocusableInTouchMode(true);
        // 请求获得焦点
        mBinding.inputView.requestFocus();
        InputMethodManager manager = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        manager.showSoftInput(mBinding.inputView, 0);
    }

    @Override
    public void onStart() {
        super.onStart();
        // 对话框宽度匹配屏幕宽度，且位置处于屏幕下方
        Window window = getDialog().getWindow();
        window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        // 设置宽度在onCreateView不生效，在onStart生效
        // window.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT);
        WindowManager.LayoutParams params = window.getAttributes();
        params.gravity = Gravity.BOTTOM;
        params.width =  ViewGroup.LayoutParams.MATCH_PARENT;
        params.height = ViewGroup.LayoutParams.WRAP_CONTENT;
        window.setAttributes(params);
    }

    @Override
    public void onClick(View v) {
        if (v==mBinding.commentDelete) {

        } else if (v==mBinding.commentSend) {
            publishComment();
        } else if (v==mBinding.commentVideo) {

        }
    }

    private void publishComment() {
        if (TextUtils.isEmpty(mBinding.inputView.getText())){ return; }
        String commentText = mBinding.inputView.getText().toString();
        ApiService.post("/comment/addComment")
                .addParam("userId", UserManager.get().getUserId())
                .addParam("itemId", mItemId)
                .addParam("commentText", commentText)
                .addParam("image_url", null)
                .addParam("videoUrl", null)
                .addParam("width", 0)
                .addParam("height", 0)
                .execute(new JsonCallback<Comment>() {
                    @Override
                    public void onSuccess(ApiResponse<Comment> response) {
                        onCommentSuccess(response.body);
                    }

                    @SuppressLint("RestrictedApi")
                    @Override
                    public void onError(ApiResponse<Comment> response) {
                        ArchTaskExecutor.getMainThreadExecutor().execute(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(AppGlobals.getApplication(), "评论失败: "+response.message, Toast.LENGTH_SHORT)
                                        .show();
                            }
                        });
                    }
                });
    }

    @SuppressLint("RestrictedApi")
    private void onCommentSuccess(Comment comment) {
        // onSuccess在子线程调用，弹Toast需切换主线程
        ArchTaskExecutor.getMainThreadExecutor().execute(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(AppGlobals.getApplication(), "评论发布成功", Toast.LENGTH_SHORT)
                        .show();
                if (mCommentAddListener != null) {
                    mCommentAddListener.onAddComment(comment);
                }
                dismiss();
            }
        });
    }

    public interface CommentAddListener {
        void onAddComment(Comment comment);
    }

    public void setCommentAddListener(CommentAddListener listener) {
        mCommentAddListener = listener;
    }
}
