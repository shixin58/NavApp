package com.passion.navapp.ui.find;

import androidx.fragment.app.Fragment;

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
    protected SofaTabs getSofaTabs() {
        return AppConfig.getFindTabs();
    }
}