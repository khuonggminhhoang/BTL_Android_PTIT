package com.example.foodorderapp.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.Group;
import androidx.recyclerview.widget.RecyclerView;
import com.example.foodorderapp.R;
import com.example.foodorderapp.model.SettingItem;
import com.google.android.material.switchmaterial.SwitchMaterial;

import java.util.List;
public class SettingsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final List<SettingItem> settingItems;
    private final LayoutInflater inflater;
    private final OnSettingItemClickListener listener;

    public interface OnSettingItemClickListener {
        void onItemClick(SettingItem item, int position);
        void onSwitchChange(SettingItem item, int position, boolean isChecked);
    }

    public SettingsAdapter(Context context, List<SettingItem> settingItems, OnSettingItemClickListener listener) {
        this.inflater = LayoutInflater.from(context);
        this.settingItems = settingItems;
        this.listener = listener;
    }

    @Override
    public int getItemViewType(int position) {
        return settingItems.get(position).getItemType();
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        switch (viewType) {
            case SettingItem.TYPE_HEADER:
                View headerView = inflater.inflate(R.layout.item_setting_header, parent, false);
                return new HeaderViewHolder(headerView);
            case SettingItem.TYPE_NAVIGATION:
                View navView = inflater.inflate(R.layout.item_setting_navigation, parent, false);
                return new NavigationViewHolder(navView);
            case SettingItem.TYPE_SWITCH:
                View switchView = inflater.inflate(R.layout.item_setting_switch, parent, false);
                return new SwitchViewHolder(switchView);
            case SettingItem.TYPE_ACTION:
                View actionView = inflater.inflate(R.layout.item_setting_action, parent, false);
                return new ActionViewHolder(actionView);
            default:
                // Nên có layout trống hoặc báo lỗi rõ ràng
                throw new RuntimeException("Invalid view type received: " + viewType);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        SettingItem item = settingItems.get(position);
        switch (holder.getItemViewType()) {
            case SettingItem.TYPE_HEADER:
                ((HeaderViewHolder) holder).bind(item);
                break;
            case SettingItem.TYPE_NAVIGATION:
                ((NavigationViewHolder) holder).bind(item, listener);
                break;
            case SettingItem.TYPE_SWITCH:
                ((SwitchViewHolder) holder).bind(item, listener);
                break;
            case SettingItem.TYPE_ACTION:
                ((ActionViewHolder) holder).bind(item, listener);
                break;
        }
    }

    @Override
    public int getItemCount() {
        return settingItems == null ? 0 : settingItems.size();
    }

    // --- ViewHolders ---

    static class HeaderViewHolder extends RecyclerView.ViewHolder {
        TextView tvHeaderTitle;
        HeaderViewHolder(@NonNull View itemView) {
            super(itemView);
            tvHeaderTitle = itemView.findViewById(R.id.tv_header_title);
        }
        void bind(SettingItem item) {
            tvHeaderTitle.setText(item.getTitle());
        }
    }

    static class NavigationViewHolder extends RecyclerView.ViewHolder {
        ImageView ivIcon;
        TextView tvTitle;
        TextView tvSubtitle;
        ImageView ivArrow;
        Group groupSubtitle; // Group để ẩn/hiện subtitle

        NavigationViewHolder(@NonNull View itemView) {
            super(itemView);
            ivIcon = itemView.findViewById(R.id.iv_setting_icon);
            tvTitle = itemView.findViewById(R.id.tv_setting_title);
            tvSubtitle = itemView.findViewById(R.id.tv_setting_subtitle);
            ivArrow = itemView.findViewById(R.id.iv_setting_arrow);
            groupSubtitle = itemView.findViewById(R.id.group_subtitle);
        }

        void bind(final SettingItem item, final OnSettingItemClickListener listener) {
            ivIcon.setImageResource(item.getIconResId());
            tvTitle.setText(item.getTitle());

            if (item.getSubtitle() != null && !item.getSubtitle().isEmpty()) {
                tvSubtitle.setText(item.getSubtitle());
                groupSubtitle.setVisibility(View.VISIBLE);
            } else {
                groupSubtitle.setVisibility(View.GONE);
            }
            // Arrow luôn hiển thị cho loại này
            ivArrow.setVisibility(View.VISIBLE);

            itemView.setOnClickListener(v -> {
                if (listener != null && getAdapterPosition() != RecyclerView.NO_POSITION) {
                    listener.onItemClick(item, getAdapterPosition());
                }
            });
        }
    }

    static class SwitchViewHolder extends RecyclerView.ViewHolder {
        ImageView ivIcon;
        TextView tvTitle;
        SwitchMaterial switchCompat;

        SwitchViewHolder(@NonNull View itemView) {
            super(itemView);
            ivIcon = itemView.findViewById(R.id.iv_setting_icon);
            tvTitle = itemView.findViewById(R.id.tv_setting_title);
            switchCompat = itemView.findViewById(R.id.switch_setting);
        }

        void bind(final SettingItem item, final OnSettingItemClickListener listener) {
            ivIcon.setImageResource(item.getIconResId());
            tvTitle.setText(item.getTitle());

            // Rất quan trọng: Gỡ listener cũ -> set trạng thái -> Gán listener mới
            switchCompat.setOnCheckedChangeListener(null);
            switchCompat.setChecked(item.isSwitchOn());
            switchCompat.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (listener != null && getAdapterPosition() != RecyclerView.NO_POSITION) {
                    // Cập nhật trạng thái model ngay lập tức (tùy chọn, nhưng thường hữu ích)
                    item.setSwitchOn(isChecked);
                    listener.onSwitchChange(item, getAdapterPosition(), isChecked);
                }
            });

            // Cho phép click cả dòng để toggle switch (tùy chọn)
            itemView.setOnClickListener(v -> {
                if (getAdapterPosition() != RecyclerView.NO_POSITION) {
                    switchCompat.toggle(); // Trigger onCheckedChanged listener
                }
            });
        }
    }

    static class ActionViewHolder extends RecyclerView.ViewHolder {
        ImageView ivIcon;
        TextView tvTitle;

        ActionViewHolder(@NonNull View itemView) {
            super(itemView);
            ivIcon = itemView.findViewById(R.id.iv_setting_icon);
            tvTitle = itemView.findViewById(R.id.tv_setting_title);
        }

        void bind(final SettingItem item, final OnSettingItemClickListener listener) {
            ivIcon.setImageResource(item.getIconResId());
            tvTitle.setText(item.getTitle());

            itemView.setOnClickListener(v -> {
                if (listener != null && getAdapterPosition() != RecyclerView.NO_POSITION) {
                    listener.onItemClick(item, getAdapterPosition());
                }
            });
        }
    }
}
