package com.passion.navapp.ui.my;

import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;

import com.passion.libcommon.utils.StatusBar;
import com.passion.libnavannotation.FragmentDestination;
import com.passion.navapp.R;
import com.passion.navapp.databinding.FragmentMyBinding;
import com.passion.navapp.model.User;
import com.passion.navapp.ui.login.UserManager;

@FragmentDestination(pageUrl = "main/tabs/my",needLogin = true)
public class MyFragment extends Fragment {
    private FragmentMyBinding mBinding;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        Log.i("MyFragment", "onCreateView");
        mBinding = FragmentMyBinding.inflate(inflater, container, false);
        return mBinding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        User user = UserManager.get().getUser();
        mBinding.setUser(user);

        UserManager.get().refresh().observe(getViewLifecycleOwner(), new Observer<User>() {
            @Override
            public void onChanged(User user) {
                mBinding.setUser(user);
            }
        });

        mBinding.actionLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new AlertDialog.Builder(requireContext())
                        .setMessage(R.string.fragment_my_logout)
                        .setPositiveButton(R.string.fragment_my_logout_ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                                UserManager.get().logout();
                                requireActivity().onBackPressed();
                            }
                        })
                        .setNegativeButton(R.string.fragment_my_logout_cancel, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        })
                        .show();
            }
        });
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        StatusBar.lightStatusBar(requireActivity(), false);
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        StatusBar.lightStatusBar(requireActivity(), hidden);
    }
}