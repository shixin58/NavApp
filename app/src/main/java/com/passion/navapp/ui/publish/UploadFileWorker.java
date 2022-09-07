package com.passion.navapp.ui.publish;

import android.content.Context;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.work.Data;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.passion.libcommon.utils.FileUploadManager;

public class UploadFileWorker extends Worker {
    public UploadFileWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        // doWork本身在自线程执行，且需要返回值。内部不再切线程
        Data inputData = getInputData();
        String filePath = inputData.getString("file");
        String fileUrl = FileUploadManager.upload(filePath);
        if (TextUtils.isEmpty(fileUrl)) {// 上传失败
            return Result.failure();
        } else {
            Data outputData = new Data.Builder()
                    .putString("fileUrl", fileUrl)
                    .build();
            return Result.success(outputData);
        }
    }
}
