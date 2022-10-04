package com.passion.navapp.ui.my;

import static com.passion.navapp.ui.my.ProfileActivity.TabType.*;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringDef;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.passion.navapp.R;
import com.passion.navapp.databinding.ActivityProfileBinding;
import com.passion.navapp.model.User;
import com.passion.navapp.ui.login.UserManager;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

public class ProfileActivity extends AppCompatActivity {
    public static final String KEY_TAB_TYPE = "key_tab_type";

    @Retention(RetentionPolicy.SOURCE)
    @Target({ElementType.FIELD,ElementType.METHOD,ElementType.PARAMETER,ElementType.LOCAL_VARIABLE})
    @StringDef({TAB_TYPE_ALL,TAB_TYPE_FEED,TAB_TYPE_COMMENT})
    public @interface TabType {
        String TAB_TYPE_ALL = "tab_type_all";
        String TAB_TYPE_FEED = "tab_type_feed";
        String TAB_TYPE_COMMENT = "tab_type_comment";
    }

    private ActivityProfileBinding mBinding;

    public static void openActivity(Context ctx, @TabType String tabType) {
        Intent intent = new Intent(ctx, ProfileActivity.class);
        intent.putExtra(KEY_TAB_TYPE, tabType);
        ctx.startActivity(intent);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = ActivityProfileBinding.inflate(getLayoutInflater());
        setContentView(mBinding.getRoot());

        User user = UserManager.get().getUser();
        mBinding.setUser(user);
        mBinding.actionBack.setOnClickListener(v -> finish());

        String[] tabs = getResources().getStringArray(R.array.profile_tabs);
        ViewPager2 viewPager = mBinding.viewPager;
        TabLayout tabLayout = mBinding.tabLayout;
        viewPager.setAdapter(new FragmentStateAdapter(getSupportFragmentManager(), getLifecycle()) {
            @NonNull
            @Override
            public Fragment createFragment(int position) {
                return ProfileListFragment.newInstance(getTabTypeByPosition(position));
            }

            private String getTabTypeByPosition(int position) {
                switch (position) {
                    case 1:
                        return TAB_TYPE_FEED;
                    case 2:
                        return TAB_TYPE_COMMENT;
                    default:
                        return TAB_TYPE_ALL;
                }
            }

            @Override
            public int getItemCount() {
                return tabs.length;
            }
        });

        // autoRefresh: 当调用ViewPager的Adapter#notifyChanged()，是否将TabLayout选项卡移除并重新配置
        new TabLayoutMediator(tabLayout, viewPager, false, new TabLayoutMediator.TabConfigurationStrategy() {
            @Override
            public void onConfigureTab(@NonNull TabLayout.Tab tab, int position) {
                tab.setText(tabs[position]);
            }
        }).attach();

        int initTabPosition = getInitTabPosition();
        if (initTabPosition != 0) {
            viewPager.post(() -> viewPager.setCurrentItem(initTabPosition));
        }

        mBinding.appbar.addOnOffsetChangedListener(new AppBarLayout.OnOffsetChangedListener() {
            @Override
            public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
                boolean expand = Math.abs(verticalOffset) < appBarLayout.getTotalScrollRange();
                mBinding.setExpand(expand);
            }
        });
    }

    private int getInitTabPosition() {
        @TabType String tabType = getIntent().getStringExtra(KEY_TAB_TYPE);
        switch (tabType) {
            case TAB_TYPE_FEED:
                return 1;
            case TAB_TYPE_COMMENT:
                return 2;
            default:
                return 0;
        }
    }
}
