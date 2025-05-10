package com.example.foodorderapp.features.main.ui.fragment; // Package của bạn

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
// Import các Activity bạn muốn điều hướng đến
import com.example.foodorderapp.core.model.Notification;
import com.example.foodorderapp.features.jobs.ui.activity.JobDetailActivity;
import com.example.foodorderapp.features.main.ui.adapter.NotificationsAdapter;

import java.util.ArrayList;
import java.util.List;

public class NotificationFragment extends Fragment implements NotificationsAdapter.OnNotificationClickListener {

    private static final String TAG = "NotificationFragment";

    // --- Biến thành viên ---
    private RecyclerView recyclerViewNotifications;
    private NotificationsAdapter adapter; // Adapter là biến thành viên
    private List<Notification> notificationList;
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
            List<Notification> fetchedNotifications = getMockNotifications(); // Hàm lấy dữ liệu mẫu

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
    public void onNotificationClick(Notification notification, int position) {
        if (getContext() == null || notification == null) return;
        Log.d(TAG, "Clicked notification ID: " + notification.getId());
        // Đánh dấu đã đọc
        if (!notification.isRead()) {
            if (adapter != null) {
                adapter.markItemAsRead(position);
            }
            // TODO: Gọi hàm cập nhật trạng thái trong Database/API
        }
        // Điều hướng ví dụ: mở JobDetailActivity nếu có applicationId
        if (notification.getApplicationId() > 0) {
            Intent intent = new Intent(getContext(), JobDetailActivity.class);
            intent.putExtra("JOB_ID", String.valueOf(notification.getApplicationId()));
            startActivity(intent);
        } else {
            Toast.makeText(getContext(), "No action defined for this notification.", Toast.LENGTH_SHORT).show();
        }
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