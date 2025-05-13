package com.example.foodorderapp.features.main.ui.fragment;

import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.foodorderapp.R;
import com.example.foodorderapp.core.model.Notification;
import com.example.foodorderapp.features.jobs.ui.activity.JobDetailActivity;
import com.example.foodorderapp.features.main.ui.adapter.NotificationsAdapter;

import java.util.ArrayList;
import java.util.List;

public class NotificationFragment extends Fragment implements NotificationsAdapter.OnNotificationClickListener {

    private static final String TAG = "NotificationFragment";

    private RecyclerView recyclerViewNotifications;
    private NotificationsAdapter adapter;
    private List<Notification> notificationList;
    private LinearLayout emptyStateLayout;
    private ProgressBar progressBar;

    public NotificationFragment() {
        // Constructor rỗng
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        notificationList = new ArrayList<>();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_notifications, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Ánh xạ view
        recyclerViewNotifications = view.findViewById(R.id.recyclerView_notifications);
        emptyStateLayout = view.findViewById(R.id.layout_empty_notifications);
        progressBar = view.findViewById(R.id.progressBar_notifications);

        // Cài đặt RecyclerView
        setupRecyclerView();

        // Tải thông báo
        loadNotifications();
    }

    // Cài đặt RecyclerView
    private void setupRecyclerView() {
        if (adapter == null) {
            adapter = new NotificationsAdapter(getContext(), notificationList, this);
        }
        recyclerViewNotifications.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerViewNotifications.setAdapter(adapter);
    }

    // Tải danh sách thông báo
    private void loadNotifications() {
        showLoading(true);
        notificationList.clear();
        if (adapter != null) {
            adapter.updateData(new ArrayList<>(notificationList));
        }

        // Giả lập tải dữ liệu
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            List<Notification> fetchedNotifications = getMockNotifications();

            if (getActivity() != null && isAdded()) {
                getActivity().runOnUiThread(() -> {
                    showLoading(false);
                    notificationList.addAll(fetchedNotifications);
                    if (adapter != null) {
                        adapter.updateData(new ArrayList<>(notificationList));
                    }
                    updateEmptyStateVisibility();
                });
            }
        }, 1000);
    }

    // Tạo dữ liệu thông báo mẫu
    private List<Notification> getMockNotifications() {
        List<Notification> mockList = new ArrayList<>();
        Notification n1 = new Notification();
        n1.setId(1);
        n1.setTitle("Application Sent");
        n1.setMessage("Your application for UI/UX Designer has been sent to Twitter.");
        n1.setCreatedAt("10:00 AM");
        n1.setRead(false);
        n1.setUserId(1);
        n1.setApplicationId(123);
        mockList.add(n1);

        Notification n2 = new Notification();
        n2.setId(2);
        n2.setTitle("Application Viewed");
        n2.setMessage("Your application for Front-end Developer at Slack was viewed.");
        n2.setCreatedAt("Yesterday");
        n2.setRead(false);
        n2.setUserId(1);
        n2.setApplicationId(456);
        mockList.add(n2);

        Notification n3 = new Notification();
        n3.setId(3);
        n3.setTitle("New Message");
        n3.setMessage("You have a new message from John Doe.");
        n3.setCreatedAt("09:30 AM");
        n3.setRead(false);
        n3.setUserId(1);
        n3.setApplicationId(789);
        mockList.add(n3);

        Notification n4 = new Notification();
        n4.setId(4);
        n4.setTitle("Password Changed");
        n4.setMessage("Your password was successfully changed.");
        n4.setCreatedAt("2 days ago");
        n4.setRead(true);
        n4.setUserId(1);
        n4.setApplicationId(0);
        mockList.add(n4);

        return mockList;
    }

    // Hiển thị/ẩn ProgressBar
    private void showLoading(boolean isLoading) {
        if (progressBar != null) {
            progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        }
        if (!isLoading) {
            updateEmptyStateVisibility();
        } else {
            if (recyclerViewNotifications != null) recyclerViewNotifications.setVisibility(View.GONE);
            if (emptyStateLayout != null) emptyStateLayout.setVisibility(View.GONE);
        }
    }

    // Cập nhật trạng thái giao diện
    private void updateEmptyStateVisibility() {
        if (adapter == null || recyclerViewNotifications == null || emptyStateLayout == null) {
            return;
        }
        if (progressBar != null && progressBar.getVisibility() == View.VISIBLE) {
            return;
        }

        if (adapter.getItemCount() == 0) {
            recyclerViewNotifications.setVisibility(View.GONE);
            emptyStateLayout.setVisibility(View.VISIBLE);
        } else {
            recyclerViewNotifications.setVisibility(View.VISIBLE);
            emptyStateLayout.setVisibility(View.GONE);
        }
    }

    // Xử lý click thông báo
    @Override
    public void onNotificationClick(Notification notification, int position) {
        if (getContext() == null || notification == null) return;
        Log.d(TAG, "Clicked notification ID: " + notification.getId());

        if (!notification.isRead()) {
            if (adapter != null) {
                adapter.markItemAsRead(position);
            }
        }

        if (notification.getApplicationId() > 0) {
            Intent intent = new Intent(getContext(), JobDetailActivity.class);
            intent.putExtra("JOB_ID", String.valueOf(notification.getApplicationId()));
            startActivity(intent);
        } else {
            Toast.makeText(getContext(), "Không có hành động cho thông báo này.", Toast.LENGTH_SHORT).show();
        }
    }
}