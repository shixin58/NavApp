package com.passion.navapp.ui.detail;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
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
import androidx.lifecycle.Observer;

import com.passion.libcommon.AppGlobals;
import com.passion.libcommon.PixUtils;
import com.passion.libcommon.ViewHelper;
import com.passion.libcommon.dialog.LoadingDialog;
import com.passion.libcommon.utils.FileUploadManager;
import com.passion.libcommon.utils.FileUtils;
import com.passion.libnetwork.ApiResponse;
import com.passion.libnetwork.ApiService;
import com.passion.libnetwork.JsonCallback;
import com.passion.navapp.R;
import com.passion.navapp.databinding.LayoutCommentDialogBinding;
import com.passion.navapp.model.Comment;
import com.passion.navapp.ui.login.UserManager;
import com.passion.navapp.ui.publish.CaptureActivity;

import java.util.concurrent.atomic.AtomicInteger;

public class CommentDialog extends DialogFragment implements View.OnClickListener {
    private static final String KEY_ITEM_ID = "key_item_id";

    private LayoutCommentDialogBinding mBinding;
    private long mItemId;
    private CommentAddListener mCommentAddListener;

    private String mFilePath;
    private int mFileWidth;
    private int mFileHeight;
    private boolean mIsVideo;

    private LoadingDialog mLoadingDialog;

    private String mFileUrl;
    private String mCoverUrl;

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
            // 重置状态
            mFilePath = null;
            mFileWidth = 0;
            mFileHeight = 0;
            mIsVideo = false;
            mBinding.commentExtLayout.setVisibility(View.GONE);
            mBinding.commentCover.setImageUrl(null);
            mBinding.commentIconVideo.setVisibility(View.GONE);
            mBinding.commentVideo.setEnabled(true);
            mBinding.commentVideo.setImageAlpha(100);
        } else if (v==mBinding.commentSend) {
            publishComment();
        } else if (v==mBinding.commentVideo) {
            CaptureActivity.openActivityForResult(getActivity());
        }
    }

    private void publishComment() {
        if (TextUtils.isEmpty(mBinding.inputView.getText())){ return; }

        // 先上传视频和封面图，再调用发布评论接口
        // 用MediaMetadataRetriever由视频文件异步生成视频封面图
        if (mIsVideo && !TextUtils.isEmpty(mFilePath)) {
            FileUtils.generateVideoCover(mFilePath).observe(this, new Observer<String>() {
                @Override
                public void onChanged(String coverPath) {
                    uploadFile(mFilePath, coverPath);
                }
            });
        } else if (!TextUtils.isEmpty(mFilePath)) {
            // 图文格式的评论，图片未压缩会比较大，上传较慢
            uploadFile(mFilePath, null);
        } else {// 纯文本
            publish();
        }
    }

    /**
     * 用aliyun oss上传图像及视频文件
     * @param filePath 非空
     * @param coverPath 视频缩略图
     */
    @SuppressLint("RestrictedApi")
    private void uploadFile(String filePath, String coverPath) {
        // 文件上传较慢，显示转菊花
        showLoadingDialog();
        // 多个文件上传任务并发，保证多线程同步。
        // 此处使用了AtomicInteger保证俩文件上传同步。WorkManager、CountDownLatch和CyclicBarrier也可以。
        AtomicInteger count = new AtomicInteger(1);
        if (!TextUtils.isEmpty(coverPath)) {
            count.set(2);
            ArchTaskExecutor.getIOThreadExecutor().execute(new Runnable() {
                @Override
                public void run() {
                    mCoverUrl = FileUploadManager.upload(coverPath);
                    int remaining = count.decrementAndGet();// 0表示上传都结束了
                    if (remaining <= 0) {// 每个任务结束时，判断下是否所有任务都结束了。若所有任务都结束，调用发布评论接口。
                        if (!TextUtils.isEmpty(mFileUrl) && !TextUtils.isEmpty(mCoverUrl)) {
                            publish();
                        } else {
                            dismissLoadingDialog();
                            showToast(getString(R.string.file_upload_failed));
                        }
                    }
                }
            });
        }
        ArchTaskExecutor.getIOThreadExecutor().execute(new Runnable() {
            @Override
            public void run() {
                mFileUrl = FileUploadManager.upload(filePath);
                int remaining = count.decrementAndGet();
                if (remaining <= 0) {
                    if (!TextUtils.isEmpty(mFileUrl)
                            && (TextUtils.isEmpty(coverPath) || !TextUtils.isEmpty(mCoverUrl))) {
                        publish();
                    } else {
                        dismissLoadingDialog();
                        showToast(getString(R.string.file_upload_failed));
                    }
                }
            }
        });
    }

    private void publish() {
        String commentText = mBinding.inputView.getText().toString();
        ApiService.post("/comment/addComment")
                .addParam("userId", UserManager.get().getUserId())
                .addParam("itemId", mItemId)
                .addParam("commentText", commentText)
                .addParam("image_url", mIsVideo?mCoverUrl:mFileUrl)
                .addParam("video_url", mIsVideo?mFileUrl:null)
                .addParam("width", mFileWidth)
                .addParam("height", mFileHeight)
                .execute(new JsonCallback<Comment>() {
                    @Override
                    public void onSuccess(ApiResponse<Comment> response) {
                        onCommentSuccess(response.body);
                        dismissLoadingDialog();
                    }

                    @SuppressLint("RestrictedApi")
                    @Override
                    public void onError(ApiResponse<Comment> response) {
                        showToast("评论失败: "+response.message);
                        dismissLoadingDialog();
                    }
                });
    }

    @SuppressLint("RestrictedApi")
    private void showToast(String msg) {
        ArchTaskExecutor.getMainThreadExecutor().execute(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(AppGlobals.getApplication(), msg, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showLoadingDialog() {
        if (mLoadingDialog == null) {
            mLoadingDialog = new LoadingDialog(getContext());
            mLoadingDialog.setCanceledOnTouchOutside(false);
            mLoadingDialog.setCancelable(false);
        }
        if (!mLoadingDialog.isShowing()) {
            mLoadingDialog.show();
            mLoadingDialog.setLoadingText(getString(R.string.upload_text));
        }
    }

    private void dismissLoadingDialog() {
        if (mLoadingDialog != null) {
            mLoadingDialog.dismiss();
        }
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

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CaptureActivity.REQ_CAPTURE && resultCode == Activity.RESULT_OK) {
            mFilePath = data.getStringExtra(CaptureActivity.RESULT_FILE_PATH);
            mFileWidth = data.getIntExtra(CaptureActivity.RESULT_FILE_WIDTH, 720);
            mFileHeight = data.getIntExtra(CaptureActivity.RESULT_FILE_HEIGHT, 1280);
            mIsVideo = data.getBooleanExtra(CaptureActivity.RESULT_FILE_IS_VIDEO, false);

            if (!TextUtils.isEmpty(mFilePath)) {
                mBinding.commentExtLayout.setVisibility(View.VISIBLE);
                mBinding.commentCover.setImageUrl(mFilePath);
                if (mIsVideo) {
                    mBinding.commentIconVideo.setVisibility(View.VISIBLE);
                }
            }

            // 拿到结果后不能再点击
            mBinding.commentVideo.setEnabled(false);
            mBinding.commentVideo.setImageAlpha(50);
        }
    }
}
