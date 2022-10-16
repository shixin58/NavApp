package com.passion.navapp.utils;

import static android.Manifest.permission.ADD_VOICEMAIL;
import static android.Manifest.permission.ANSWER_PHONE_CALLS;
import static android.Manifest.permission.CALL_PHONE;
import static android.Manifest.permission.CAMERA;
import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.Manifest.permission.READ_PHONE_NUMBERS;
import static android.Manifest.permission.READ_PHONE_STATE;
import static android.Manifest.permission.RECORD_AUDIO;
import static android.Manifest.permission.USE_SIP;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.passion.libcommon.AppGlobals;

import java.util.Iterator;
import java.util.LinkedList;

/**
 * <p>Created by shixin on 2018/12/8.
 */
public class PermissionUtils {
    private static final String TAG = PermissionUtils.class.getSimpleName();

    public static void requestStoragePermission(Activity activity, int requestCode) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M)
            return;
        Context ctx = AppGlobals.getApplication();
        if(ContextCompat.checkSelfPermission(ctx, READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            if(ActivityCompat.shouldShowRequestPermissionRationale(activity, READ_EXTERNAL_STORAGE)) {
                Toast.makeText(activity, "您需要到系统设置里打开存储权限", Toast.LENGTH_SHORT).show();
            } else {
                ActivityCompat.requestPermissions(activity, new String[]{READ_EXTERNAL_STORAGE,
                        WRITE_EXTERNAL_STORAGE}, requestCode);
            }
        }
    }

    public static void requestPhonePermission(Activity activity, int requestCode) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M)
            return;
        Context ctx = AppGlobals.getApplication();
        if(ContextCompat.checkSelfPermission(ctx, READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            if(ActivityCompat.shouldShowRequestPermissionRationale(activity, READ_PHONE_STATE)) {
                Toast.makeText(activity, "您需要到系统设置里打开读取手机信息权限", Toast.LENGTH_SHORT).show();
            } else {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    ActivityCompat.requestPermissions(activity, new String[]{READ_PHONE_STATE, READ_PHONE_NUMBERS,
                            CALL_PHONE, ANSWER_PHONE_CALLS, ADD_VOICEMAIL, USE_SIP}, requestCode);
                } else {
                    ActivityCompat.requestPermissions(activity, new String[]{READ_PHONE_STATE,
                            CALL_PHONE, ADD_VOICEMAIL, USE_SIP}, requestCode);
                }
            }
        }
    }

    public static void requestCameraPermission(Activity activity, int requestCode) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M)
            return;
        Context ctx = AppGlobals.getApplication();
        if(ContextCompat.checkSelfPermission(ctx, CAMERA) != PackageManager.PERMISSION_GRANTED) {
            if(ActivityCompat.shouldShowRequestPermissionRationale(activity, CAMERA)) {
                Toast.makeText(activity, "您需要到系统设置里打开Camera权限", Toast.LENGTH_SHORT).show();
            } else {
                ActivityCompat.requestPermissions(activity, new String[]{CAMERA,
                        WRITE_EXTERNAL_STORAGE}, requestCode);
            }
        }
    }

    public static void requestRecordAudioPermission(Activity activity, int requestCode) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M)
            return;
        Context ctx = AppGlobals.getApplication();
        if(ContextCompat.checkSelfPermission(ctx, RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            if(ActivityCompat.shouldShowRequestPermissionRationale(activity, RECORD_AUDIO)) {
                Toast.makeText(activity, "您需要到系统设置里打开录音权限", Toast.LENGTH_SHORT).show();
            } else {
                ActivityCompat.requestPermissions(activity, new String[]{RECORD_AUDIO,
                        WRITE_EXTERNAL_STORAGE}, requestCode);
            }
        }
    }

    public static void requestAllPermissions(Activity activity, int requestCode) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M)
            return;
        Context ctx = AppGlobals.getApplication();
        LinkedList<String> permissions = new LinkedList<>();
        permissions.add(READ_EXTERNAL_STORAGE);
        permissions.add(WRITE_EXTERNAL_STORAGE);
        permissions.add(READ_PHONE_STATE);
        permissions.add(CAMERA);
        permissions.add(RECORD_AUDIO);
        Iterator<String> iterator = permissions.iterator();
        while (iterator.hasNext()) {
            String permission = iterator.next();
            if (ContextCompat.checkSelfPermission(ctx, permission) == PackageManager.PERMISSION_GRANTED) {
                iterator.remove();
            }
        }
        if (!permissions.isEmpty()) {
            String[] arr = new String[permissions.size()];
            permissions.toArray(arr);
            ActivityCompat.requestPermissions(activity, arr, requestCode);
        }
    }
}
