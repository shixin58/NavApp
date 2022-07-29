package com.passion.navapp.ui.publish;

import android.os.Bundle;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProviders;

import com.passion.libnavannotation.ActivityDestination;
import com.passion.navapp.databinding.ActivityPublishBinding;

@ActivityDestination(pageUrl = "main/tabs/publish",asStarter = false)
public class PublishActivity extends AppCompatActivity {
    private ActivityPublishBinding mBinding;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = ActivityPublishBinding.inflate(getLayoutInflater());
        setContentView(mBinding.getRoot());

        PublishViewModel viewModel = ViewModelProviders.of(this).get(PublishViewModel.class);

        final TextView textView = mBinding.textPublish;
        viewModel.getText().observe(this, textView::setText);
    }
}
