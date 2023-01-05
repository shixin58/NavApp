package com.passion.navapp.ui.publish;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.arch.core.executor.ArchTaskExecutor;
import androidx.lifecycle.Observer;
import androidx.work.Constraints;
import androidx.work.Data;
import androidx.work.NetworkType;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkContinuation;
import androidx.work.WorkInfo;
import androidx.work.WorkManager;

import com.alibaba.fastjson.JSONObject;
import com.passion.libcommon.AppGlobals;
import com.passion.libcommon.dialog.LoadingDialog;
import com.passion.libcommon.utils.FileUtils;
import com.passion.libnavannotation.ActivityDestination;
import com.passion.libnetwork.ApiResponse;
import com.passion.libnetwork.ApiService;
import com.passion.libnetwork.JsonCallback;
import com.passion.navapp.R;
import com.passion.navapp.databinding.ActivityPublishBinding;
import com.passion.navapp.model.Feed;
import com.passion.navapp.model.FeedTag;
import com.passion.navapp.ui.login.UserManager;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@ActivityDestination(pageUrl = "main/tabs/publish")
public class PublishActivity extends AppCompatActivity implements View.OnClickListener {
    private ActivityPublishBinding mBinding;

    private int mWidth, mHeight;
    private String mFilePath;
    private boolean mIsVideo;
    private String mCoverPath;

    private FeedTag mSelectedTag;
    private String mFileUrl;
    private String mCoverUrl;

    private UUID mCoverUuid;
    private UUID mFileUploadUuid;

    private LoadingDialog mLoadingDialog;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = ActivityPublishBinding.inflate(getLayoutInflater());
        setContentView(mBinding.getRoot());

        mBinding.actionAddFile.setOnClickListener(this);
        mBinding.actionPublish.setOnClickListener(this);
        mBinding.actionAddTag.setOnClickListener(this);
        mBinding.actionClose.setOnClickListener(this);
        mBinding.actionDeleteFile.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if (v==mBinding.actionAddFile) {
            CaptureActivity.openActivityForResult(this);
        } else if (v==mBinding.actionPublish) {
            publish();
//            PreviewActivity.openActivityForResult(this, "https://v.aldelo.com/tutorial/help/whyaldeloexpress.m3u8", true, null);
        } else if (v==mBinding.actionAddTag) {
            TagBottomSheetDialogFragment fragment = new TagBottomSheetDialogFragment();
            fragment.setOnTagItemSelectedListener(new TagBottomSheetDialogFragment.OnTagItemSelectedListener() {
                @Override
                public void onTagItemSelected(FeedTag tag) {
                    mSelectedTag = tag;
                    mBinding.actionAddTag.setText(tag.title);
                }
            });
            fragment.show(getSupportFragmentManager(), TagBottomSheetDialogFragment.class.getName());
        } else if (v==mBinding.actionClose) {
            showExitDialog();
        } else if (v==mBinding.actionDeleteFile) {
            mBinding.actionAddFile.setVisibility(View.VISIBLE);
            mBinding.fileContainer.setVisibility(View.GONE);
            mBinding.cover.setImageDrawable(null);
            mBinding.videoIcon.setVisibility(View.GONE);
            mWidth = 0;
            mHeight = 0;
            mFilePath = null;
            mIsVideo = false;
        }
    }

    private void publish() {
        showLoadingDialog();
        List<OneTimeWorkRequest> workRequests = new ArrayList<>();
        if (!TextUtils.isEmpty(mFilePath)) {
            if (mIsVideo) {
                FileUtils.generateVideoCover(mFilePath).observe(this, new Observer<String>() {
                    @Override
                    public void onChanged(String coverPath) {
                        mCoverPath = coverPath;
                        OneTimeWorkRequest workRequest = getOneTimeWorkRequest(coverPath);
                        mCoverUuid = workRequest.getId();
                        workRequests.add(workRequest);

                        enqueue(workRequests);
                    }
                });
            }
            OneTimeWorkRequest workRequest = getOneTimeWorkRequest(mFilePath);
            mFileUploadUuid = workRequest.getId();
            workRequests.add(workRequest);
            if (!mIsVideo) {
                enqueue(workRequests);
            }
        } else {
            publishFeed();
        }
    }

    private void enqueue(List<OneTimeWorkRequest> workRequests) {
        WorkContinuation workContinuation = WorkManager.getInstance(PublishActivity.this)
                .beginWith(workRequests);
        workContinuation.enqueue();
        workContinuation.getWorkInfosLiveData().observe(PublishActivity.this, new Observer<List<WorkInfo>>() {
            @Override
            public void onChanged(List<WorkInfo> workInfoList) {
                int completedCount = 0;
                int failedCount = 0;
                for (WorkInfo workInfo : workInfoList) {
                    UUID uuid = workInfo.getId();
                    WorkInfo.State state = workInfo.getState();
                    Data outputData = workInfo.getOutputData();
                    if (state == WorkInfo.State.FAILED) {
                        if (uuid.equals(mCoverUuid)) {
                            showToast(getString(R.string.file_upload_cover_message));
                        } else if (uuid.equals(mFileUploadUuid)) {
                            showToast(getString(R.string.file_upload_original_message));
                        }
                        failedCount++;
                    } else if (state == WorkInfo.State.SUCCEEDED) {
                        String fileUrl = outputData.getString("fileUrl");
                        if (uuid.equals(mCoverUuid)) {
                            mCoverUrl = fileUrl;
                        } else if (uuid.equals(mFileUploadUuid)) {
                            mFileUrl = fileUrl;
                        }
                        completedCount++;
                    }
                }
                if (completedCount == workInfoList.size()) {
                    publishFeed();
                } else if (failedCount > 0) {
                    dismissLoadingDialog();
                }
            }
        });
    }

    private void publishFeed() {
        ApiService.post("/feeds/publish")
                .addParam("fileUrl", mFileUrl)
                .addParam("coverUrl", mCoverUrl)
                .addParam("fileWidth", mWidth)
                .addParam("fileHeight", mHeight)
                .addParam("userId", UserManager.get().getUserId())
                .addParam("tagId", mSelectedTag==null?0:mSelectedTag.tagId)
                .addParam("tagTitle", mSelectedTag==null?0:mSelectedTag.title)
                .addParam("feedText", mBinding.inputView.getText().toString())
                .addParam("feedType", mIsVideo? Feed.TYPE_VIDEO:Feed.TYPE_IMAGE_TEXT)
                .execute(new JsonCallback<JSONObject>() {
                    @Override
                    public void onSuccess(ApiResponse<JSONObject> response) {
                        showToast(getString(R.string.feed_publish_success));
                        PublishActivity.this.finish();
                        dismissLoadingDialog();
                    }

                    @Override
                    public void onError(ApiResponse<JSONObject> response) {
                        showToast(response.message);
                        dismissLoadingDialog();
                    }
                });
    }

    @SuppressLint("RestrictedApi")
    private static void showToast(String message) {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            Toast.makeText(AppGlobals.getApplication(), message, Toast.LENGTH_SHORT).show();
        } else {
            ArchTaskExecutor.getMainThreadExecutor().execute(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(AppGlobals.getApplication(), message, Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void showLoadingDialog() {
        if (mLoadingDialog == null) {
            mLoadingDialog = new LoadingDialog(this);
            mLoadingDialog.setCanceledOnTouchOutside(false);
            mLoadingDialog.setCancelable(false);
        }
        if (!mLoadingDialog.isShowing()) {
            mLoadingDialog.show();
            mLoadingDialog.setLoadingText(getString(R.string.upload_text));
        }
    }

    private void dismissLoadingDialog() {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            if (mLoadingDialog != null) {
                mLoadingDialog.dismiss();
            }
        } else {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (mLoadingDialog != null) {
                        mLoadingDialog.dismiss();
                    }
                }
            });
        }
    }

    @SuppressLint("RestrictedApi")
    private OneTimeWorkRequest getOneTimeWorkRequest(String coverPath) {
        // Builder构建Worker入参
        Data inputData = new Data.Builder()
                .putString("file", coverPath)
                .build();

        // Builder构建Constraints，传给WorkRequest
        Constraints.Builder constraintsBuilder = new Constraints.Builder();
        constraintsBuilder.setRequiresStorageNotLow(true)/* 设备存储空间大于15% */
                .setRequiredNetworkType(NetworkType.UNMETERED)/* 网络好的情况下执行，如Wi-Fi不计流量 */
                .setRequiresBatteryNotLow(true)/* 电量充足下执行，如大于15% */
                .setRequiresCharging(true);/* 设备充电情况下执行 */
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            constraintsBuilder.setRequiresDeviceIdle(true);/* 设备空闲情况下执行 */
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            // WorkManager利用ContentObserver监听Uri对应内容变化，内容发生变化任务才执行
//                            constraintsBuilder.addContentUriTrigger(null, true);
            // 设置content变化到任务执行之间的延迟。如果在此期间content变化，重新计算延迟
//                            constraintsBuilder.setTriggerContentUpdateDelay(0L, TimeUnit.SECONDS);
            // 设置content变化到任务执行之间最大延迟
//                            constraintsBuilder.setTriggerContentMaxDelay(0L, TimeUnit.SECONDS);
        }
        Constraints constraints = constraintsBuilder.build();

        // Builder构建WorkRequest
        OneTimeWorkRequest workRequest = new OneTimeWorkRequest.Builder(UploadFileWorker.class)
                .setInputData(inputData)
//                .setConstraints(constraints)
                // 设置一个拦截器，在任务执行之前做一次拦截，修改入参数据返回新数据，交由Worker使用
//                                .setInputMerger(null)
                // 设置任务调度失败重试策略，通过BackoffPolicy执行具体策略
                // IllegalArgumentException: Cannot set backoff criteria on an idle mode job
//                .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 10L, TimeUnit.SECONDS)
                // 任务被调度执行的延迟时间
//                .setInitialDelay(10L, TimeUnit.SECONDS)
                // 任务失败重试最大次数
//                .setInitialRunAttemptCount(2)
//                .setPeriodStartTime(0, TimeUnit.SECONDS)
//                .setScheduleRequestedAt(0, TimeUnit.SECONDS)
                // 当任务状态变成finish，但没后续观察者消费结果。那么WorkManager会在内存里保存一段时间该结果，
                // 超时将结果存数据库，下次查询结果触发WorkManager数据库查询操作，可通过UUID查询任务状态
//                .keepResultsForAtLeast(10L, TimeUnit.SECONDS)
                .build();
        return workRequest;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CaptureActivity.REQ_CAPTURE
                && resultCode == Activity.RESULT_OK && data != null) {
            mFilePath = data.getStringExtra(CaptureActivity.RESULT_FILE_PATH);
            mWidth = data.getIntExtra(CaptureActivity.RESULT_FILE_WIDTH, 720);
            mHeight = data.getIntExtra(CaptureActivity.RESULT_FILE_HEIGHT, 1280);
            mIsVideo = data.getBooleanExtra(CaptureActivity.RESULT_FILE_IS_VIDEO, false);

            showFileThumbnail();
        }
    }

    private void showFileThumbnail() {
        if (TextUtils.isEmpty(mFilePath)) { return; }

        mBinding.fileContainer.setVisibility(View.VISIBLE);
        mBinding.cover.setImageUrl(mFilePath);
        if (mIsVideo) {
            mBinding.videoIcon.setVisibility(View.VISIBLE);
        }
        mBinding.actionAddFile.setVisibility(View.GONE);
        mBinding.cover.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PreviewActivity.openActivityForResult(PublishActivity.this, mFilePath, mIsVideo, null);
            }
        });
    }

    private void showExitDialog() {
        // AlertDialog统一使用androidx.appcompat.app包下的，弃用android.app包下的。
        new AlertDialog.Builder(this)
                .setMessage(R.string.publish_exit_message)
                .setNegativeButton(R.string.publish_exit_action_cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .setPositiveButton(R.string.publish_exit_action_ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        finish();
                    }
                }).create()
                .show();
    }
}
