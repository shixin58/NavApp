package com.passion.navapp;

import static android.Manifest.permission.READ_PHONE_STATE;

import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import com.google.android.material.navigation.NavigationBarView;
import com.passion.navapp.databinding.ActivityMainBinding;
import com.passion.navapp.model.Destination;
import com.passion.navapp.ui.login.UserManager;
import com.passion.navapp.utils.AppConfig;
import com.passion.navapp.utils.NavGraphBuilder;
import com.passion.navapp.utils.PermissionUtils;
import com.passion.navapp.view.AppBottomBar;
import com.tencent.tauth.Tencent;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class MainActivity extends AppCompatActivity implements NavigationBarView.OnItemSelectedListener {
    private ActivityMainBinding mBinding;
    private NavController navController;
    private AppBottomBar navView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(mBinding.getRoot());
        navView = mBinding.navView;

        Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment_activity_main);
        navController = NavHostFragment.findNavController(fragment);

        navView.setOnItemSelectedListener(this);

        NavGraphBuilder.build(this, fragment.getChildFragmentManager(), navController, fragment.getId());

        if(ContextCompat.checkSelfPermission(this, READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED) {
            Tencent.setIsPermissionGranted(true);
        }
        PermissionUtils.requestAllPermissions(this, 1);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        HashMap<String, Destination> destConfig = AppConfig.getDestConfig();
        Iterator<Map.Entry<String, Destination>> iterator = destConfig.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, Destination> entry = iterator.next();
            Destination value = entry.getValue();
            if (value!=null && item.getItemId()==value.id && value.needLogin
                    && !UserManager.get().isLogin()) {
                UserManager.get().login(this).observe(this, user -> {
                    navView.setSelectedItemId(item.getItemId());
                });
                return false;
            }
        }
        navController.navigate(item.getItemId());
        return !TextUtils.isEmpty(item.getTitle());
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1) {
            for (int i=0;i<permissions.length;i++) {
                if (TextUtils.equals(permissions[i], READ_PHONE_STATE) && grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                    Tencent.setIsPermissionGranted(true);
                }
            }
        }
    }
}