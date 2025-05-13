package com.example.foodorderapp.features.profile.ui.adapter;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.foodorderapp.R;
import com.example.foodorderapp.features.settings.ui.activity.SettingsActivity;
import com.example.foodorderapp.features.profile.ui.model.MenuItem;
import com.example.foodorderapp.features.profile.ui.activity.PersonalDataActivity;
import com.example.foodorderapp.features.profile.ui.activity.ResumeMyInfoActivity;

import java.util.List;

public class MenuAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final int TYPE_HEADER = 0;
    private static final int TYPE_ITEM = 1;

    private List<Object> items;
    private Context context;

    public MenuAdapter(Context context, List<Object> items) {
        this.context = context;
        this.items = items;
    }

    @Override
    public int getItemViewType(int position) {
        // Xác định loại view (header hoặc item)
        return items.get(position) instanceof String ? TYPE_HEADER : TYPE_ITEM;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Tạo view cho header hoặc item
        if (viewType == TYPE_HEADER) {
            View view = LayoutInflater.from(parent.getContext()).inflate(android.R.layout.simple_list_item_1, parent, false);
            return new HeaderViewHolder(view);
        } else {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_menu, parent, false);
            return new ItemViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (getItemViewType(position) == TYPE_HEADER) {
            // Gán tiêu đề cho header
            HeaderViewHolder headerHolder = (HeaderViewHolder) holder;
            headerHolder.headerText.setText((String) items.get(position));
        } else {
            // Gán dữ liệu và sự kiện click cho item
            ItemViewHolder itemHolder = (ItemViewHolder) holder;
            final MenuItem menuItem = (MenuItem) items.get(position);
            itemHolder.icon.setImageResource(menuItem.getIconResId());
            itemHolder.title.setText(menuItem.getTitle());

            itemHolder.itemView.setOnClickListener(v -> {
                String title = menuItem.getTitle();
                Intent intent = null;

                if (title.equals(context.getString(R.string.personal_data))) {
                    intent = new Intent(context, PersonalDataActivity.class);
                } else if (title.equals(context.getString(R.string.resume_my_info))) {
                    intent = new Intent(context, ResumeMyInfoActivity.class);
                } else if (title.equals(context.getString(R.string.settings_title))) {
                    intent = new Intent(context, SettingsActivity.class);
                } else if (title.equals(context.getString(R.string.faq))) {
                    intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/khuonggminhhoang/BTL_Android_PTIT/issues"));
                } else if (title.equals(context.getString(R.string.privacy_policy))) {
                    intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/khuonggminhhoang/BTL_Android_PTIT/wiki"));
                }

                if (intent != null) {
                    try {
                        context.startActivity(intent);
                    } catch (android.content.ActivityNotFoundException e) {
                        Toast.makeText(context, "Không tìm thấy ứng dụng trình duyệt.", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    // ViewHolder cho header
    static class HeaderViewHolder extends RecyclerView.ViewHolder {
        TextView headerText;

        HeaderViewHolder(View itemView) {
            super(itemView);
            headerText = itemView.findViewById(android.R.id.text1);
        }
    }

    // ViewHolder cho item menu
    static class ItemViewHolder extends RecyclerView.ViewHolder {
        ImageView icon;
        TextView title;
        ImageView arrow;

        ItemViewHolder(View itemView) {
            super(itemView);
            icon = itemView.findViewById(R.id.iv_icon);
            title = itemView.findViewById(R.id.tv_title);
            arrow = itemView.findViewById(R.id.iv_arrow);
        }
    }
}