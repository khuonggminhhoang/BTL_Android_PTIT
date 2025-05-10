package com.example.foodorderapp.features.main.ui.adapter; // Tạo package này nếu chưa có


import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.foodorderapp.R; // Đảm bảo import R đúng
import com.example.foodorderapp.core.model.Notification;
import com.example.foodorderapp.core.model.Application;
import com.example.foodorderapp.core.model.Job;
import com.example.foodorderapp.core.model.Company;

import java.util.ArrayList;
import java.util.List;

public class NotificationsAdapter extends RecyclerView.Adapter<NotificationsAdapter.ViewHolder> {

    private List<Notification> notifications;
    private final Context context;
    private final OnNotificationClickListener listener;

    public interface OnNotificationClickListener {
        void onNotificationClick(Notification notification, int position);
    }

    public NotificationsAdapter(Context context, List<Notification> notifications, OnNotificationClickListener listener) {
        this.context = context;
        this.notifications = notifications != null ? new ArrayList<>(notifications) : new ArrayList<>();
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_notification, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Notification notification = notifications.get(position);

        holder.tvTitle.setText(notification.getTitle());
        holder.tvMessage.setText(notification.getMessage());
        holder.tvTimestamp.setText(notification.getCreatedAt()); // TODO: Format timestamp nếu cần

        // Kiểm tra null an toàn cho application, job, company
        String logoUrl = null;
        Application application = notification.getApplication();
        if (application != null) {
            Job job = application.getJob();
            if (job != null) {
                Company company = job.getCompany();
                if (company != null) {
                    logoUrl = company.getLogoUrl();
                }
            }
        }
        Glide.with(context)
                .load(logoUrl)
                .placeholder(R.mipmap.ic_launcher_round) // Thay placeholder
                .error(R.drawable.ic_notifications_24) // Icon chuông mặc định
                .circleCrop()
                .into(holder.ivIcon);

        if (notification.isRead()) {
            holder.tvTitle.setTypeface(null, Typeface.NORMAL);
            // Sử dụng màu đã định nghĩa trong colors.xml
            holder.tvMessage.setTextColor(ContextCompat.getColor(context, R.color.grey_medium));
            holder.unreadIndicator.setVisibility(View.GONE);
        } else {
            holder.tvTitle.setTypeface(null, Typeface.BOLD);
            // Sử dụng màu đã định nghĩa trong colors.xml
            holder.tvMessage.setTextColor(ContextCompat.getColor(context, R.color.grey_dark));
            holder.unreadIndicator.setVisibility(View.VISIBLE);
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                int currentPosition = holder.getBindingAdapterPosition();
                if (currentPosition != RecyclerView.NO_POSITION) {
                    listener.onNotificationClick(notifications.get(currentPosition), currentPosition);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return notifications.size();
    }

    // Cập nhật dữ liệu
    public void updateData(List<Notification> newNotifications) {
        this.notifications = newNotifications != null ? new ArrayList<>(newNotifications) : new ArrayList<>();
        notifyDataSetChanged(); // Hoặc dùng DiffUtil
    }

    // Đánh dấu item đã đọc (chỉ cập nhật UI)
    public void markItemAsRead(int position) {
        if (position >= 0 && position < notifications.size()) {
            Notification item = notifications.get(position);
            if (!item.isRead()) {
                item.setRead(true);
                notifyItemChanged(position);
            }
        }
    }

    // ViewHolder
    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivIcon;
        TextView tvTitle;
        TextView tvMessage;
        TextView tvTimestamp;
        View unreadIndicator;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivIcon = itemView.findViewById(R.id.iv_notification_icon);
            tvTitle = itemView.findViewById(R.id.tv_notification_title);
            tvMessage = itemView.findViewById(R.id.tv_notification_message);
            tvTimestamp = itemView.findViewById(R.id.tv_notification_timestamp);
            unreadIndicator = itemView.findViewById(R.id.view_unread_indicator);
        }
    }
}