package com.passion.navapp.ui;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.passion.libcommon.PixUtils;
import com.passion.libcommon.RoundFrameLayout;
import com.passion.libcommon.ViewHelper;
import com.passion.navapp.R;
import com.passion.navapp.view.PPImageView;

import java.util.ArrayList;
import java.util.List;

public class ShareDialog extends AlertDialog {
    private ShareAdapter mShareAdapter;
    private final List<ResolveInfo> mShareItems = new ArrayList<>();
    private String mShareContent;
    private View.OnClickListener mOnClickListener;

    public ShareDialog(@NonNull Context context) {
        super(context);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);

        RoundFrameLayout layout = new RoundFrameLayout(getContext());
        layout.setBackgroundColor(Color.WHITE);
        layout.setViewOutline(PixUtils.dp2Px(20f), ViewHelper.RADIUS_TOP);

        RecyclerView gridView = new RecyclerView(getContext());
        gridView.setLayoutManager(new GridLayoutManager(getContext(), 4));
        mShareAdapter = new ShareAdapter();
        gridView.setAdapter(mShareAdapter);

        FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        int margin = PixUtils.dp2Px(20f);
        lp.leftMargin = lp.rightMargin = lp.topMargin = lp.bottomMargin = margin;
        lp.gravity = Gravity.CENTER;
        layout.addView(gridView, lp);

        setContentView(layout);
        getWindow().setGravity(Gravity.BOTTOM);

        getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        queryShareItems();
    }

    public void setShareContent(String shareContent) {
        this.mShareContent = shareContent;
    }

    public void setShareItemClickListener(View.OnClickListener onClickListener) {
        mOnClickListener = onClickListener;
    }

    private void queryShareItems() {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_SEND);
        intent.setType("text/plain");

        mShareItems.clear();
        List<ResolveInfo> resolveInfoList = getContext().getPackageManager().queryIntentActivities(intent, 0);
        for (ResolveInfo resolveInfo:resolveInfoList) {
            String packageName = resolveInfo.activityInfo.packageName;
            if (TextUtils.equals(packageName, "com.tencent.mm") || TextUtils.equals(packageName, "com.tencent.mobileqq")) {
                mShareItems.add(resolveInfo);
            }
        }
        mShareAdapter.notifyDataSetChanged();
    }

    private class ShareAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
        PackageManager pm;

        public ShareAdapter() {
            pm = getContext().getPackageManager();
        }

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(getContext()).inflate(R.layout.layout_share_item, parent, false);
            return new RecyclerView.ViewHolder(itemView){};
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
            ResolveInfo resolveInfo = mShareItems.get(position);
            PPImageView imageView = holder.itemView.findViewById(R.id.share_icon);
            Drawable drawable = resolveInfo.loadIcon(pm);
            imageView.setImageDrawable(drawable);

            TextView textView = holder.itemView.findViewById(R.id.share_text);
            CharSequence label = resolveInfo.loadLabel(pm);
            textView.setText(label);

            holder.itemView.setOnClickListener(view -> {
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_SEND);
                intent.setType("text/plain");
                String pkg = resolveInfo.activityInfo.packageName;
                String cls = resolveInfo.activityInfo.name;
                intent.setComponent(new ComponentName(pkg, cls));
                intent.putExtra(Intent.EXTRA_TEXT, mShareContent);

                getContext().startActivity(intent);

                if (mOnClickListener != null) {
                    mOnClickListener.onClick(view);
                }
            });
        }

        @Override
        public int getItemCount() {
            return mShareItems.size();
        }
    }
}
