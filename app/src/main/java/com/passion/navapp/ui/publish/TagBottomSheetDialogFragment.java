package com.passion.navapp.ui.publish;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.arch.core.executor.ArchTaskExecutor;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.passion.libcommon.AppGlobals;
import com.passion.libcommon.PixUtils;
import com.passion.libnetwork.ApiResponse;
import com.passion.libnetwork.ApiService;
import com.passion.libnetwork.JsonCallback;
import com.passion.navapp.R;
import com.passion.navapp.model.FeedTag;
import com.passion.navapp.ui.login.UserManager;

import java.util.ArrayList;
import java.util.List;

// BottomSheetDialogFragment扩展自AppCompatDialogFragment(AppCompatDialog)->DialogFragment(Dialog)
// 利用BottomSheetBehavior达到滑动展开和收缩的效果
public class TagBottomSheetDialogFragment extends BottomSheetDialogFragment {
    private RecyclerView mRecyclerView;
    private TagAdapter mTagAdapter;
    private final List<FeedTag> mTagList = new ArrayList<>();
    private OnTagItemSelectedListener mOnTagItemSelectedListener;

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        // 默认创建BottomSheetDialog
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        View view = LayoutInflater.from(getContext()).inflate(R.layout.layout_tag_bottom_sheet_dialog, null, false);
        mRecyclerView = view.findViewById(R.id.recycler_view);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        mTagAdapter = new TagAdapter();
        mRecyclerView.setAdapter(mTagAdapter);

        dialog.setContentView(view);
        // 用BottomSheetBehavior设置默认展开的高度
        ViewGroup parent = (ViewGroup) view.getParent();
        BottomSheetBehavior<ViewGroup> behavior = BottomSheetBehavior.from(parent);
        behavior.setPeekHeight(PixUtils.getScreenHeight() / 3);
        behavior.setHideable(false);

        // 通过parent设置滑动展开时能达到的最大高度
        ViewGroup.LayoutParams layoutParams = parent.getLayoutParams();
        layoutParams.height = PixUtils.getScreenHeight() / 3 * 2;
        parent.setLayoutParams(layoutParams);

        // 从服务器接口请求标签列表数据
        // Intellij IDEA插件GsonFormat已升级为GsonFormatPlus。使用GsonFormat将json字符串转换为json model。
        queryTagList();
        return dialog;
    }

    private void queryTagList() {
        ApiService.get("/tag/queryTagList")
                .addParam("userId", UserManager.get().getUserId())
                .addParam("pageCount", 100)
                .addParam("tagId", 0)
                .execute(new JsonCallback<List<FeedTag>>() {
                    @SuppressLint("RestrictedApi")
                    @Override
                    public void onSuccess(ApiResponse<List<FeedTag>> response) {
                        if (response.body != null) {
                            List<FeedTag> tagList = response.body;
                            mTagList.clear();
                            mTagList.addAll(tagList);
                            ArchTaskExecutor.getMainThreadExecutor().execute(new Runnable() {
                                @Override
                                public void run() {
                                    mTagAdapter.notifyDataSetChanged();
                                }
                            });
                        }
                    }

                    @SuppressLint("RestrictedApi")
                    @Override
                    public void onError(ApiResponse<List<FeedTag>> response) {
                        ArchTaskExecutor.getMainThreadExecutor().execute(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(AppGlobals.getApplication(), response.message, Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                });
    }

    class TagAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            TextView textView = new TextView(parent.getContext());
            textView.setTextSize(13f);
            textView.setGravity(Gravity.CENTER_VERTICAL);
            textView.setTypeface(Typeface.DEFAULT_BOLD);
            textView.setTextColor(ContextCompat.getColor(parent.getContext(), R.color.color_000));
            textView.setLayoutParams(new RecyclerView.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, PixUtils.dp2Px(45f)));
            RecyclerView.ViewHolder viewHolder = new RecyclerView.ViewHolder(textView) {};
            return viewHolder;
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
            TextView textView = (TextView) holder.itemView;
            final FeedTag tag = mTagList.get(position);
            textView.setText(tag.title);
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mOnTagItemSelectedListener != null) {
                        mOnTagItemSelectedListener.onTagItemSelected(tag);
                        dismiss();
                    }
                }
            });
        }

        @Override
        public int getItemCount() {
            return mTagList.size();
        }
    }

    public void setOnTagItemSelectedListener(OnTagItemSelectedListener onTagItemSelectedListener) {
        mOnTagItemSelectedListener = onTagItemSelectedListener;
    }

    interface OnTagItemSelectedListener {
        void onTagItemSelected(FeedTag tag);
    }
}
