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
import com.passion.libcommon.utils.StatusBar;
import com.passion.navapp.view.AppBottomBar;
import com.tencent.tauth.Tencent;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * LiveData数据倒灌：
 * <p>1）页面配置改变(旋转屏幕、切换系统语言、软键盘弹起)导致Activity重建；
 * <p>2）页面重建时，重新observe()，observer.mLastVersion重置为-1，但ViewModel保存之前数据，LiveData.mVersion恢复为重建前的值；
 * <p>3）Activity可见时，LiveData自动发送最后一条数据。<p>
 *
 * <p>杀后台不走onDestroy()和onRetainCustomNonConfigurationInstance()，不会造成数据倒灌。
 * <p>正常流程: setValue(T)->dispatchingValue(null)->considerNotify()->Observer#onChange()<p>
 *
 * <p>SingleLiveEvent修复了数据倒灌。用覆写observe方法和compareAndSet(true, false)保证只发送1次，非显式调用setValue不分发onChanged()。
 * <p>LiveData.LifecycleBoundObserver#onStateChanged()->activeStateChanged()
 * <p>->LiveData.dispatchingValue(LifecycleBoundObserver)->considerNotify()->Observer#onChange()
 */
public class MainActivity extends AppCompatActivity implements NavigationBarView.OnItemSelectedListener {
    private ActivityMainBinding mBinding;
    private NavController mNavController;
    private AppBottomBar mNavView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // 由于启动时设置了R.style.launcher下的windowBackground属性，势必要在进入主页后把窗口背景清理掉
        setTheme(R.style.Theme_NavApp);

        StatusBar.fitSystemBar(this);
        super.onCreate(savedInstanceState);
        mBinding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(mBinding.getRoot());
        mNavView = mBinding.navView;

        Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment_activity_main);
        mNavController = NavHostFragment.findNavController(fragment);

        mNavView.setOnItemSelectedListener(this);

        NavGraphBuilder.build(this, fragment.getChildFragmentManager(), mNavController, fragment.getId());

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
                    mNavView.setSelectedItemId(item.getItemId());
                });
                return false;
            }
        }
        mNavController.navigate(item.getItemId());
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

    @Override
    public void onBackPressed() {
        /*boolean shouldIntercept = false;
        int homeDestinationId = 0;

        Fragment fragment = getSupportFragmentManager().getPrimaryNavigationFragment();
        String tag = fragment.getTag();
        int currentPageDestId = Integer.parseInt(tag);
        HashMap<String, Destination> destConfig = AppConfig.getDestConfig();
        Iterator<Map.Entry<String, Destination>> iterator = destConfig.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, Destination> entry = iterator.next();
            Destination destination = entry.getValue();
            if (destination.id==currentPageDestId && !destination.asStarter) {
                shouldIntercept = true;
            }
            if (destination.asStarter) {
                homeDestinationId = destination.id;
            }
        }

        if (shouldIntercept && homeDestinationId > 0) {
            mNavView.setSelectedItemId(homeDestinationId);
            return;
        }
        super.onBackPressed();*/
        int currentPageId = mNavController.getCurrentDestination().getId();
        int homeDestId = mNavController.getGraph().getStartDestination();
        if (currentPageId != homeDestId) {
            mNavView.setSelectedItemId(homeDestId);
            return;
        }
        finish();
    }
}