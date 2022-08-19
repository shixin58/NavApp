package com.passion.navapp.ui.sofa;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.passion.libnavannotation.FragmentDestination;
import com.passion.navapp.databinding.FragmentSofaBinding;
import com.passion.navapp.model.SofaTabs;
import com.passion.navapp.ui.home.HomeFragment;
import com.passion.navapp.utils.AppConfig;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@FragmentDestination(pageUrl = "main/tabs/sofa")
public class SofaFragment extends Fragment {
    private FragmentSofaBinding mBinding;

    private TabLayout tabLayout;
    private ViewPager2 viewPager;
    private TabLayoutMediator tabLayoutMediator;

    private SofaTabs sofaConfig;
    private List<SofaTabs.Tab> mTabs;
    private final Map<Integer,Fragment> mFragmentMap = new HashMap<>();

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        Log.i("SofaFragment", "onCreateView");
        mBinding = FragmentSofaBinding.inflate(inflater, container, false);
        return mBinding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        tabLayout = mBinding.tabLayout;
        viewPager = mBinding.viewPager;

        sofaConfig = getSofaTabs();
        mTabs = new ArrayList<>();
        for (SofaTabs.Tab tab : sofaConfig.tabs) {
            if (tab.enable) {
                mTabs.add(tab);
            }
        }
        viewPager.setOffscreenPageLimit(ViewPager2.OFFSCREEN_PAGE_LIMIT_DEFAULT);
        viewPager.setAdapter(new FragmentStateAdapter(getChildFragmentManager(), getLifecycle()) {
            @NonNull
            @Override
            public Fragment createFragment(int position) {
                Fragment fragment = mFragmentMap.get(position);
                if (fragment == null) {
                    fragment = getTabFragment(position);
                    mFragmentMap.put(position, fragment);
                }
                return fragment;
            }

            @Override
            public int getItemCount() {
                return mTabs.size();
            }
        });

        // 通过TabLayoutMediator让TabLayout和ViewPager2联动
        tabLayoutMediator = new TabLayoutMediator(tabLayout, viewPager, false, new TabLayoutMediator.TabConfigurationStrategy() {
            @Override
            public void onConfigureTab(@NonNull TabLayout.Tab tab, int position) {
                tab.setCustomView(makeTabView(position));
            }
        });
        tabLayoutMediator.attach();

        // 监听ViewPager2页面选中
        viewPager.registerOnPageChangeCallback(mOnPageChangeCallback);

        // TabLayout和ViewPager2都初始化完成后，设置默认选中项
        viewPager.post(new Runnable() {
            @Override
            public void run() {
                viewPager.setCurrentItem(sofaConfig.select);
            }
        });
    }

    ViewPager2.OnPageChangeCallback mOnPageChangeCallback = new ViewPager2.OnPageChangeCallback() {
        @Override
        public void onPageSelected(int position) {
            int tabCount = tabLayout.getTabCount();
            for (int i=0;i<tabCount;i++) {
                TabLayout.Tab tab = tabLayout.getTabAt(i);
                TextView customView = (TextView) tab.getCustomView();
                if (tab.getPosition() == position) {
                    customView.setTextSize(sofaConfig.activeSize);
                    customView.setTypeface(Typeface.DEFAULT_BOLD);
                } else {
                    customView.setTextSize(sofaConfig.normalSize);
                    customView.setTypeface(Typeface.DEFAULT);
                }
            }
        }
    };

    private View makeTabView(int pos) {
        TextView tabView = new TextView(getContext());

        int[][] states = new int[2][];
        states[0] = new int[]{android.R.attr.state_selected};
        states[1] = new int[]{};

        int[] colors = new int[]{Color.parseColor(sofaConfig.activeColor),Color.parseColor(sofaConfig.normalColor)};
        ColorStateList colorStateList = new ColorStateList(states, colors);
        tabView.setTextColor(colorStateList);
        tabView.setText(mTabs.get(pos).title);
        tabView.setTextSize(sofaConfig.normalSize);

        return tabView;
    }

    private Fragment getTabFragment(int pos) {
        return HomeFragment.getInstance(mTabs.get(pos).tag);
    }

    private SofaTabs getSofaTabs() {
        return AppConfig.getSofaTabs();
    }

    @Override
    public void onDestroy() {
        tabLayoutMediator.detach();
        viewPager.unregisterOnPageChangeCallback(mOnPageChangeCallback);
        super.onDestroy();
    }
}