package com.passion.navapp.ui.find;

import android.content.Context;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentOnAttachListener;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import com.passion.libnavannotation.FragmentDestination;
import com.passion.navapp.model.SofaTabs;
import com.passion.navapp.ui.sofa.SofaFragment;
import com.passion.navapp.utils.AppConfig;

@FragmentDestination(pageUrl = "main/tabs/find")
public class FindFragment extends SofaFragment {
    @Override
    protected Fragment getTabFragment(int pos) {
        TagListFragment fragment = TagListFragment.newInstance(getSofaTabs().tabs.get(pos).tag);
        return fragment;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        getChildFragmentManager().addFragmentOnAttachListener(new FragmentOnAttachListener() {
            @Override
            public void onAttachFragment(@NonNull FragmentManager fragmentManager, @NonNull Fragment fragment) {
                String tagType = fragment.getArguments().getString(TagListFragment.KEY_TAG_TYPE);
                if (TextUtils.equals(tagType, "onlyFollow")) {
                    // ViewModel数据共享实现tab切换
                    ViewModelProviders.of(fragment).get(TagListViewModel.class)
                            .getSwitchTabLiveData().observe(FindFragment.this, new Observer() {
                                @Override
                                public void onChanged(Object o) {
                                    mViewPager.setCurrentItem(1);
                                }
                            });
                }
            }
        });
    }

    @Override
    protected SofaTabs getSofaTabs() {
        return AppConfig.getFindTabs();
    }
}