package com.example.foodorderapp.features.main.ui.fragment; // Package của bạn

import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity; // Import nếu dùng Toolbar
import androidx.appcompat.widget.Toolbar;    // Import nếu dùng Toolbar
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;         // Import nếu dùng Menu
import android.view.MenuInflater; // Import nếu dùng Menu
import android.view.MenuItem;    // Import nếu dùng Menu
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.foodorderapp.R;
import com.example.foodorderapp.core.model.NotificationItem; // Import adapter
// Import các Activity bạn muốn điều hướng đến
import com.example.foodorderapp.features.jobs.ui.activity.JobDetailActivity;
import com.example.foodorderapp.features.main.ui.adapter.NotificationsAdapter;

import java.util.ArrayList;
import java.util.List;

public class NotificationFragment extends Fragment implements NotificationsAdapter.OnNotificationClickListener {

    private static final String TAG = "NotificationFragment";

    // --- Biến thành viên ---
    private RecyclerView recyclerViewNotifications;
    private NotificationsAdapter adapter; // Adapter là biến thành viên
    private List<NotificationItem> notificationList;
    private LinearLayout emptyStateLayout;
    private ProgressBar progressBar;
    // private Toolbar toolbar; // Bỏ comment nếu layout fragment_notifications.xml có Toolbar

    public NotificationFragment() { } // Constructor trống

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        notificationList = new ArrayList<>(); // Khởi tạo list ở đây
        // setHasOptionsMenu(true); // Chỉ gọi nếu Fragment này có menu riêng
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_notifications, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // --- Ánh xạ Views ---
        // toolbar = view.findViewById(R.id.toolbar_notifications); // Bỏ comment nếu có Toolbar
        recyclerViewNotifications = view.findViewById(R.id.recyclerView_notifications);
        emptyStateLayout = view.findViewById(R.id.layout_empty_notifications);
        progressBar = view.findViewById(R.id.progressBar_notifications);

        // --- Setup UI ---
        // setupToolbar(); // Bỏ comment nếu có Toolbar riêng
        setupRecyclerView();

        // --- Load dữ liệu ---
        loadNotifications();
    }

    /* // Bỏ comment nếu có Toolbar riêng
    private void setupToolbar() {
        if (getActivity() instanceof AppCompatActivity) {
            ((AppCompatActivity) getActivity()).setSupportActionBar(toolbar);
            // Optional: Set tiêu đề nếu cần
            // ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle("Notifications");
        }
    }
    */

    private void setupRecyclerView() {
        // Chỉ khởi tạo adapter một lần
        if (adapter == null) {
            adapter = new NotificationsAdapter(getContext(), notificationList, this);
        }
        recyclerViewNotifications.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerViewNotifications.setAdapter(adapter);
    }

    private void loadNotifications() {
        showLoading(true);
        // Luôn clear list trước khi thêm dữ liệu mới (quan trọng khi load lại)
        notificationList.clear();
        // Cập nhật adapter ngay cả khi list rỗng để xóa item cũ trên UI
        if (adapter != null) {
            adapter.updateData(new ArrayList<>(notificationList)); // Update với list rỗng
        }

        // === TODO: Thay thế bằng logic gọi API/Database thực tế ===
        // Ví dụ gọi API/DB ở đây...
        // Giả lập thành công sau 1 giây
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            List<NotificationItem> fetchedNotifications = getMockNotifications(); // Hàm lấy dữ liệu mẫu

            // --- Xử lý kết quả ---
            if (getActivity() != null && isAdded()) { // Kiểm tra Fragment còn tồn tại
                getActivity().runOnUiThread(() -> { // Đảm bảo chạy trên Main Thread
                    showLoading(false);
                    notificationList.addAll(fetchedNotifications); // Thêm dữ liệu mới vào list
                    if (adapter != null) {
                        // Cập nhật adapter với dữ liệu mới
                        adapter.updateData(new ArrayList<>(notificationList));
                    }
                    updateEmptyStateVisibility(); // Cập nhật trạng thái trống/có
                });
            }
            // --- Kết thúc xử lý kết quả ---

        }, 1000); // Delay 1 giây
        // === Kết thúc TODO ===
    }

    // Hàm tạo dữ liệu mẫu (thay bằng logic thật)
    private List<NotificationItem> getMockNotifications() {
        List<NotificationItem> mockList = new ArrayList<>();
        mockList.add(new NotificationItem("noti_1", "Application Sent", "Your application for UI/UX Designer has been sent to Twitter.", "10:00 AM", "https://upload.wikimedia.org/wikipedia/commons/thumb/6/6f/Logo_of_Twitter.svg/512px-Logo_of_Twitter.svg.png", false, "job_status", "job_123"));
        mockList.add(new NotificationItem("noti_2", "Application Viewed", "Your application for Front-end Developer at Slack was viewed.", "Yesterday", "https://cdn.worldvectorlogo.com/logos/slack-new-logo.svg", false, "job_status", "job_456"));
        mockList.add(new NotificationItem("noti_3", "New Message", "You have a new message from John Doe.", "09:30 AM", "https://www.w3schools.com/w3images/avatar2.png", false, "new_message", "chat_789"));
        mockList.add(new NotificationItem("noti_4", "Password Changed", "Your password was successfully changed.", "2 days ago", "https://static.vecteezy.com/system/resources/thumbnails/009/663/927/small/shield-check-mark-security-logo-icon-design-free-vector.jpg", true, "security", null));
        return mockList;
    }


    private void showLoading(boolean isLoading) {
        if (progressBar != null) {
            progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        }
        // Chỉ ẩn/hiện list và empty state SAU KHI load xong
        if (!isLoading) {
            updateEmptyStateVisibility(); // Cập nhật lại trạng thái hiển thị
        } else {
            // Ẩn cả hai khi đang load
            if (recyclerViewNotifications != null) recyclerViewNotifications.setVisibility(View.GONE);
            if (emptyStateLayout != null) emptyStateLayout.setVisibility(View.GONE);
        }
    }

    private void updateEmptyStateVisibility() {
        // Kiểm tra null an toàn hơn
        if (adapter == null || recyclerViewNotifications == null || emptyStateLayout == null) {
            return; // Views chưa sẵn sàng
        }
        // Chỉ cập nhật nếu không đang loading
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

    // --- Xử lý Click ---
    @Override
    public void onNotificationClick(NotificationItem notification, int position) {
        if (getContext() == null || notification == null || notification.getId() == null) return;

        Log.d(TAG, "Clicked notification ID: " + notification.getId() + " Type: " + notification.getType());

        // 1. Đánh dấu đã đọc (UI + Backend/DB)
        if (!notification.isRead()) {
            if (adapter != null) {
                adapter.markItemAsRead(position);
            }
            // TODO: Gọi hàm cập nhật trạng thái trong Database/API
            // markNotificationAsReadInBackend(notification.getId());
        }

        // 2. Điều hướng
        String type = notification.getType();
        String relatedId = notification.getRelatedId();

        if (type == null) {
            Log.w(TAG, "Notification type is null for ID: " + notification.getId());
            Toast.makeText(getContext(), "Cannot determine notification action.", Toast.LENGTH_SHORT).show();
            return;
        }

        Intent intent = null;
        switch (type.toLowerCase()) {
            case "job_status": // Gộp các loại job lại
            case "job_applied":
            case "job_viewed":
                if (isValidId(relatedId)) {
                    intent = new Intent(getContext(), JobDetailActivity.class);
                    intent.putExtra("JOB_ID", relatedId);
                } else {
                    showInvalidDataToast("Job details");
                }
                break;
            case "new_message":
                if (isValidId(relatedId)) {
                    // intent = new Intent(getContext(), ChatActivity.class);
                    // intent.putExtra("CHAT_ID", relatedId);
                    Toast.makeText(getContext(), "Open chat: " + relatedId, Toast.LENGTH_SHORT).show(); // Placeholder
                } else {
                    showInvalidDataToast("Chat");
                }
                break;
            // Thêm các case khác...
            default:
                Log.w(TAG, "Unhandled notification type: " + type);
                Toast.makeText(getContext(), "No action defined for this notification.", Toast.LENGTH_SHORT).show();
                break;
        }

        if (intent != null) {
            try {
                startActivity(intent);
            } catch (Exception e) {
                Log.e(TAG, "Error starting activity for notification type: " + type, e);
                Toast.makeText(getContext(), "Error opening details.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    // Hàm kiểm tra ID hợp lệ (không null và không rỗng)
    private boolean isValidId(String id) {
        return id != null && !id.trim().isEmpty();
    }

    // Hàm hiển thị Toast lỗi dữ liệu không hợp lệ
    private void showInvalidDataToast(String detailType) {
        if (getContext() != null) {
            Toast.makeText(getContext(), detailType + " not available.", Toast.LENGTH_SHORT).show();
        }
        Log.w(TAG, "Missing or invalid relatedId for notification.");
    }

    // --- TODO: Hàm gọi backend/DB để đánh dấu đã đọc ---
    // private void markNotificationAsReadInBackend(String notificationId) { ... }

    /* // Bỏ comment nếu cần xử lý menu item trong fragment này
    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        // inflater.inflate(R.menu.your_notification_menu, menu); // Tạo file menu riêng nếu cần
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        // if (item.getItemId() == R.id.action_delete_all_notifications) { ... }
        return super.onOptionsItemSelected(item);
    }
    */
}