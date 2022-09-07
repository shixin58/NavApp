package com.passion.navapp.ui.publish;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Rational;
import android.util.Size;
import android.view.Surface;
import android.view.TextureView;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.arch.core.executor.ArchTaskExecutor;
import androidx.camera.core.CameraX;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureConfig;
import androidx.camera.core.Preview;
import androidx.camera.core.PreviewConfig;
import androidx.camera.core.VideoCapture;
import androidx.camera.core.VideoCaptureConfig;
import androidx.core.app.ActivityCompat;

import com.passion.navapp.R;
import com.passion.navapp.databinding.ActivityCaptureBinding;
import com.passion.navapp.view.RecordView;

import java.io.File;
import java.util.LinkedList;

public class CaptureActivity extends AppCompatActivity {
    private static final String[] PERMISSIONS = new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};
    private static final int REQUEST_CODE_PERMISSIONS = 1000;

    public static final int REQ_CAPTURE = 1001;

    public static final String RESULT_FILE_PATH = "file_path";
    public static final String RESULT_FILE_WIDTH = "file_width";
    public static final String RESULT_FILE_HEIGHT = "file_height";
    public static final String RESULT_FILE_IS_VIDEO = "file_is_video";

    private ActivityCaptureBinding mBinding;
    private final LinkedList<String> deniedPermissions = new LinkedList<>();

    private final CameraX.LensFacing mLensFacing = CameraX.LensFacing.BACK;
    private final int mRotation = Surface.ROTATION_0;
    // 从拍照源头限制分辨率，以免上传过慢。拍照、录视频基于横屏。
    private final Size mResolution = new Size(1280, 720);
    private final Rational mRational = new Rational(9, 16);

    // Preview/ImageCapture/VideoCapture均是UseCase子类，均通过Builder模式构建
    private Preview mPreview;
    private ImageCapture mImageCapture;
    private VideoCapture mVideoCapture;

    private boolean takingPicture;
    private String outputFilePath;

    public static void openActivityForResult(Activity activity) {
        Intent intent = new Intent(activity, CaptureActivity.class);
        activity.startActivityForResult(intent, REQ_CAPTURE);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = ActivityCaptureBinding.inflate(getLayoutInflater());
        setContentView(mBinding.getRoot());
        ActivityCompat.requestPermissions(this, PERMISSIONS, REQUEST_CODE_PERMISSIONS);
        mBinding.recordView.setOnRecordListener(new RecordView.OnRecordListener() {
            @Override
            public void onClick() {
                takingPicture = true;
                // Android 10+，有权限也不能写入Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                File file = new File(getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS),
                        System.currentTimeMillis()+".jpeg");
                // takePicture传递文件路径，及文件保存状态回调
                // OnImageSavedListener回调在子线程，刷新UI需切换线程
                mImageCapture.takePicture(file, new ImageCapture.OnImageSavedListener() {

                    @Override
                    public void onImageSaved(@NonNull File file) {
                        onFileSaved(file);
                    }

                    @Override
                    public void onError(@NonNull ImageCapture.UseCaseError useCaseError, @NonNull String message, @Nullable Throwable cause) {
                        showErrorToast(message);
                    }
                });
            }

            @SuppressLint("RestrictedApi")
            @Override
            public void onLongClick() {
                takingPicture = false;
                File file = new File(getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS),
                        System.currentTimeMillis()+".mp4");
                // onLongClick启动录视频，ACTION_UP结束录视频
                mVideoCapture.startRecording(file, new VideoCapture.OnVideoSavedListener() {
                    @Override
                    public void onVideoSaved(File file) {
                        onFileSaved(file);
                    }

                    @Override
                    public void onError(VideoCapture.UseCaseError useCaseError, String message, @Nullable Throwable cause) {
                        showErrorToast(message);
                    }
                });
            }

            @SuppressLint("RestrictedApi")
            @Override
            public void onFinish() {
                // stopRecording()触发onVideoSaved()
                mVideoCapture.stopRecording();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PreviewActivity.REQ_PREVIEW && resultCode == Activity.RESULT_OK) {
            // 预览ok
            Intent intent = new Intent();
            intent.putExtra(RESULT_FILE_PATH, outputFilePath);
            // 当设备处于竖屏，宽高切换
            intent.putExtra(RESULT_FILE_WIDTH, mResolution.getHeight());
            intent.putExtra(RESULT_FILE_HEIGHT, mResolution.getWidth());
            intent.putExtra(RESULT_FILE_IS_VIDEO, !takingPicture);
            setResult(Activity.RESULT_OK, intent);
            finish();
        }
    }

    @SuppressLint("RestrictedApi")
    private void showErrorToast(String errorMsg) {
        ArchTaskExecutor.getMainThreadExecutor().execute(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(CaptureActivity.this, errorMsg, Toast.LENGTH_SHORT)
                        .show();
            }
        });
    }

    private void onFileSaved(File file) {
        outputFilePath = file.getAbsolutePath();
        String mimeType = takingPicture?"image/jpeg":"video/mp4";
        // 通知相册扫描，使文件出现在相册，在onImageSaved()或onVideoSaved()之后。
        MediaScannerConnection.scanFile(this, new String[]{outputFilePath}, new String[]{mimeType}, new MediaScannerConnection.OnScanCompletedListener() {
            @Override
            public void onScanCompleted(String path, Uri uri) {}
        });
        // 跳转到全屏预览，传递图片或视频本地文件路径
        PreviewActivity.openActivityForResult(CaptureActivity.this, outputFilePath, !takingPicture, getString(R.string.preview_ok));
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            deniedPermissions.clear();
            for (int i=0;i<permissions.length;i++) {
                if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                    deniedPermissions.add(permissions[i]);
                }
            }
            if (!deniedPermissions.isEmpty()) {
                new AlertDialog.Builder(this)
                        .setMessage(R.string.capture_permission_message)
                        .setNegativeButton(R.string.capture_permission_no, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                                CaptureActivity.this.finish();
                            }
                        })
                        .setPositiveButton(R.string.capture_permission_ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                ActivityCompat.requestPermissions(CaptureActivity.this, PERMISSIONS, REQUEST_CODE_PERMISSIONS);
                            }
                        })
                        .create()
                        .show();
            } else {
                bindCameraX();
            }
        }
    }

    @SuppressLint("RestrictedApi")
    private void bindCameraX() {
        PreviewConfig previewConfig = new PreviewConfig.Builder()
                .setLensFacing(mLensFacing)
                .setTargetRotation(mRotation)
                .setTargetResolution(mResolution)
                .setTargetAspectRatio(mRational)
                .build();
        mPreview = new Preview(previewConfig);

        mImageCapture = new ImageCapture(new ImageCaptureConfig.Builder()
                .setLensFacing(mLensFacing)// 摄像头位置
                .setTargetRotation(mRotation)// 旋转角度
                .setTargetResolution(mResolution)// 分辨率
                .setTargetAspectRatio(mRational)// 宽高比
                .build());

        mVideoCapture = new VideoCapture(new VideoCaptureConfig.Builder()
                .setLensFacing(mLensFacing)
                .setTargetRotation(mRotation)
                .setTargetResolution(mResolution)
                .setTargetAspectRatio(mRational)
                .setVideoFrameRate(25)// frame per second
                .setBitRate(3 * 1024 * 1024)// bit rate
                .build());

        mPreview.setOnPreviewOutputUpdateListener(new Preview.OnPreviewOutputUpdateListener() {
            @Override
            public void onUpdated(Preview.PreviewOutput output) {
                TextureView textureView = mBinding.textureView;
                ViewGroup viewGroup = (ViewGroup) textureView.getParent();
                viewGroup.removeView(textureView);
                viewGroup.addView(textureView, 0);

                // 将视频流关联到TextureView，即设置SurfaceTexture
                textureView.setSurfaceTexture(output.getSurfaceTexture());
            }
        });

        // 先解绑所有UseCase，再绑定用到的UseCase
        CameraX.unbindAll();
        CameraX.bindToLifecycle(this, mPreview, mImageCapture, mVideoCapture);
    }
}
