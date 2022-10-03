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

/**
 * 发现tab页，包含关注和推荐两个二级tab页。
 * <p>两个标签列表页均由TagListFragment实现，点击标签item跳转至标签帖子列表页TagFeedListActivity。
 */
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
                    // 在发现tab实现从二级tab页关注切换到二级tab页推荐。
                    // ViewModel数据共享拿到二级页面的ViewModel，进而观察对应LiveData。ViewPager2#setCurrentItem()实现tab切换。
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