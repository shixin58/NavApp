package com.passion.libcommon.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;
import android.os.Environment;

import androidx.arch.core.executor.ArchTaskExecutor;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.passion.libcommon.AppGlobals;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class FileUtils {
    // 从视频文件获取其封面，即获取关键帧
    @SuppressLint("RestrictedApi")
    public static LiveData<String> generateVideoCover(final String filePath) {
        MutableLiveData<String> liveData = new MutableLiveData<>();
        ArchTaskExecutor.getIOThreadExecutor().execute(new Runnable() {
            @Override
            public void run() {
                MediaMetadataRetriever retriever = new MediaMetadataRetriever();
                retriever.setDataSource(filePath);
                Bitmap frame = retriever.getFrameAtTime();
                if (frame != null) {
                    // 图片可能很大，先压缩至200k以下
                    byte[] bytes = compressBitmap(frame, 200);
                    // 将压缩后的byte数组写入文件
                    Context ctx = AppGlobals.getApplication();
                    File file = new File(ctx.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS),
                            System.currentTimeMillis()+".jpeg");
                    try {
                        file.createNewFile();
                        FileOutputStream fos = new FileOutputStream(file);
                        fos.write(bytes);
                        fos.flush();
                        fos.close();
                        fos = null;
                        liveData.postValue(file.getAbsolutePath());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } else {
                    liveData.postValue(null);
                }
            }
        });
        return liveData;
    }

    /**
     * 用Bitmap#compress()压缩图片，用ByteArrayOutputStream接收压缩后的图片流。
     * 最后将字节数组保存到本地文件，文件路径通知调用方。
     * @param frame 待压缩的Bitmap图片
     * @param limit kilobyte
     */
    private static byte[] compressBitmap(Bitmap frame, int limit) {
        if (frame != null && limit > 0) {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            int quality = 100;
            frame.compress(Bitmap.CompressFormat.JPEG, quality, baos);
            while (baos.toByteArray().length > limit * 1024) {
                baos.reset();
                quality -= 5;
                frame.compress(Bitmap.CompressFormat.JPEG, quality, baos);
            }

            byte[] bytes = baos.toByteArray();
            if (baos != null) {
                try {
                    baos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                baos = null;
            }
            return bytes;
        }
        return null;
    }
}
