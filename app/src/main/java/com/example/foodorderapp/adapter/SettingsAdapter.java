package com.example.foodorderapp.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.foodorderapp.R;
import com.example.foodorderapp.model.ClickableItem;
import com.example.foodorderapp.model.HeaderItem;
import com.example.foodorderapp.model.LogoutItem;
import com.example.foodorderapp.model.SettingsItem;
import com.example.foodorderapp.model.SwitchItem;
import com.google.android.material.switchmaterial.SwitchMaterial;

import java.util.List;

public class SettingsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private List<SettingsItem> mItems; // Dùng m prefix cho biến instance

    public SettingsAdapter(List<SettingsItem> items) {
        this.mItems = items;
    }

    @Override
    public int getItemViewType(int position) {
        return mItems.get(position).getType(); // Lấy view type từ item
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view;
        switch (viewType) {
            case SettingsItem.VIEW_TYPE_HEADER:
                view = inflater.inflate(R.layout.item_setting_header, parent, false);
                return new HeaderViewHolder(view); // Trả về ViewHolder tương ứng
            case SettingsItem.VIEW_TYPE_CLICKABLE:
                view = inflater.inflate(R.layout.item_setting_clickable, parent, false);
                return new ClickableViewHolder(view);
            case SettingsItem.VIEW_TYPE_SWITCH:
                view = inflater.inflate(R.layout.item_setting_switch, parent, false);
                return new SwitchViewHolder(view);
            case SettingsItem.VIEW_TYPE_LOGOUT:
                view = inflater.inflate(R.layout.item_setting_logout, parent, false);
                return new LogoutViewHolder(view);
            default:
                throw new IllegalArgumentException("Invalid view type provided: " + viewType);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        SettingsItem item = mItems.get(position);
        switch (holder.getItemViewType()) {
            case SettingsItem.VIEW_TYPE_HEADER:
                ((HeaderViewHolder) holder).bind((HeaderItem) item); // Gọi bind của ViewHolder tương ứng
                break;
            case SettingsItem.VIEW_TYPE_CLICKABLE:
                ((ClickableViewHolder) holder).bind((ClickableItem) item);
                break;
            case SettingsItem.VIEW_TYPE_SWITCH:
                ((SwitchViewHolder) holder).bind((SwitchItem) item);
                break;
            case SettingsItem.VIEW_TYPE_LOGOUT:
                ((LogoutViewHolder) holder).bind((LogoutItem) item);
                break;
        }
    }

    @Override
    public int getItemCount() {
        return mItems != null ? mItems.size() : 0;
    }


    // --- ViewHolder Classes (Static Inner Classes) ---

    // Tên ViewHolder: HeaderViewHolder
    public static class HeaderViewHolder extends RecyclerView.ViewHolder {
        TextView textViewHeaderTitle; // Dùng ID đã đặt trong XML
        public HeaderViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewHeaderTitle = itemView.findViewById(R.id.textViewHeaderTitle);
        }
        void bind(HeaderItem item) {
            textViewHeaderTitle.setText(item.getTitle());
        }
    }

    // Tên ViewHolder: ClickableViewHolder
    public static class ClickableViewHolder extends RecyclerView.ViewHolder {
        ImageView imageViewSettingIcon; // Dùng các ID đã đặt
        TextView textViewSettingTitle;
        TextView textViewSettingSubtitle;
        // ImageView imageViewSettingArrow;

        public ClickableViewHolder(@NonNull View itemView) {
            super(itemView);
            imageViewSettingIcon = itemView.findViewById(R.id.imageViewSettingIcon);
            textViewSettingTitle = itemView.findViewById(R.id.textViewSettingTitle);
            textViewSettingSubtitle = itemView.findViewById(R.id.textViewSettingSubtitle);
            // imageViewSettingArrow = itemView.findViewById(R.id.imageViewSettingArrow); // Bỏ comment nếu dùng ID này
        }
        void bind(ClickableItem item) {
            imageViewSettingIcon.setImageResource(item.getIconRes());
            textViewSettingTitle.setText(item.getTitle());
            if (item.getSubtitle() != null && !item.getSubtitle().isEmpty()) { // Check null và empty
                textViewSettingSubtitle.setText(item.getSubtitle());
                textViewSettingSubtitle.setVisibility(View.VISIBLE);
            } else {
                textViewSettingSubtitle.setVisibility(View.GONE);
            }
            // Set click listener
            itemView.setOnClickListener(v -> {
                if (item.getAction() != null) {
                    item.getAction().run(); // Thực thi Runnable action
                }
            });
        }
    }

    // Tên ViewHolder: SwitchViewHolder
    public static class SwitchViewHolder extends RecyclerView.ViewHolder {
        ImageView imageViewSettingIcon;
        TextView textViewSettingTitle;
        SwitchMaterial switchSettingToggle; // Dùng ID đã đặt

        public SwitchViewHolder(@NonNull View itemView) {
            super(itemView);
            imageViewSettingIcon = itemView.findViewById(R.id.imageViewSettingIcon);
            textViewSettingTitle = itemView.findViewById(R.id.textViewSettingTitle);
            switchSettingToggle = itemView.findViewById(R.id.switchSettingToggle);
        }
        void bind(SwitchItem item) {
            imageViewSettingIcon.setImageResource(item.getIconRes());
            textViewSettingTitle.setText(item.getTitle());

            // Quan trọng: set listener về null trước khi set trạng thái mới
            switchSettingToggle.setOnCheckedChangeListener(null);
            switchSettingToggle.setChecked(item.isChecked());

            // Set listener mới
            switchSettingToggle.setOnCheckedChangeListener((buttonView, isChecked) -> {
                item.setChecked(isChecked); // Cập nhật model trước khi gọi action
                if (item.getAction() != null) {
                    item.getAction().onCheckedChanged(isChecked); // Gọi lambda hành động
                }
            });
        }
    }

    // Tên ViewHolder: LogoutViewHolder
    public static class LogoutViewHolder extends RecyclerView.ViewHolder {
        ImageView imageViewSettingIcon;
        TextView textViewSettingTitle;
        public LogoutViewHolder(@NonNull View itemView) {
            super(itemView);
            imageViewSettingIcon = itemView.findViewById(R.id.imageViewSettingIcon);
            textViewSettingTitle = itemView.findViewById(R.id.textViewSettingTitle);
        }
        void bind(LogoutItem item) {
            imageViewSettingIcon.setImageResource(item.getIconRes());
            textViewSettingTitle.setText(item.getTitle());
            // Set click listener
            itemView.setOnClickListener(v -> {
                if (item.getAction() != null) {
                    item.getAction().run();
                }
            });
        }
    }
}
