package com.example.foodorderapp.features.main.ui.fragment;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.foodorderapp.R;
import com.example.foodorderapp.config.Config;
import com.example.foodorderapp.core.model.Application;
import com.example.foodorderapp.core.model.Job;
import com.example.foodorderapp.core.model.Notification;
import com.example.foodorderapp.features.auth.ui.activity.LoginActivity;
import com.example.foodorderapp.features.jobs.ui.activity.JobDetailActivity;
import com.example.foodorderapp.features.jobs.ui.adapter.JobAdapter;
import com.example.foodorderapp.features.main.ui.activity.MainActivity;
import com.example.foodorderapp.features.main.ui.adapter.NotificationsAdapter;
import com.example.foodorderapp.network.ApiService;
import com.example.foodorderapp.network.response.NotificationDetailApiResponse;
import com.example.foodorderapp.network.response.NotificationsApiResponse;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class NotificationFragment extends Fragment implements NotificationsAdapter.OnNotificationClickListener {

    private static final String TAG = "NotificationFragment";

    private RecyclerView recyclerViewNotifications;
    private NotificationsAdapter adapter;
    private List<Notification> notificationList;
    private LinearLayout emptyStateLayout;
    private ProgressBar progressBar;

    private ApiService apiService;
    private String currentAccessToken;

    public NotificationFragment() {
        // Hàm khởi tạo rỗng
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Khởi tạo danh sách thông báo và API
        notificationList = new ArrayList<>();
        initApiService();
        loadAuthToken();
    }

    // Khởi tạo dịch vụ API
    private void initApiService() {
        String baseUrl = Config.BE_URL;
        if (baseUrl == null || baseUrl.isEmpty()) {
            Log.e(TAG, "Lỗi cấu hình BE_URL");
            if (getContext() != null) Toast.makeText(getContext(), "Lỗi cấu hình máy chủ.", Toast.LENGTH_LONG).show();
            return;
        }
        if (!baseUrl.endsWith("/")) baseUrl += "/";
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(baseUrl)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        apiService = retrofit.create(ApiService.class);
        Log.d(TAG, "Khởi tạo ApiService với URL: " + baseUrl);
    }

    // Tải access token từ SharedPreferences
    private void loadAuthToken() {
        if (getContext() != null) {
            SharedPreferences prefs = getContext().getSharedPreferences("AuthPrefs", Context.MODE_PRIVATE);
            currentAccessToken = prefs.getString("accessToken", null);
            Log.d(TAG, currentAccessToken == null ? "Token rỗng" : "Đã tải token");
        } else {
            Log.e(TAG, "Context rỗng, không tải được token");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Gán layout cho fragment
        View view = inflater.inflate(R.layout.fragment_notifications, container, false);
        recyclerViewNotifications = view.findViewById(R.id.recyclerView_notifications);
        emptyStateLayout = view.findViewById(R.id.layout_empty_notifications);
        progressBar = view.findViewById(R.id.progressBar_notifications);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        // Thiết lập RecyclerView
        setupRecyclerView();
        if (currentAccessToken == null && getContext() != null) loadAuthToken();
    }

    // Thiết lập RecyclerView
    private void setupRecyclerView() {
        if (getContext() == null) {
            Log.e(TAG, "Context rỗng, không thiết lập được RecyclerView");
            return;
        }
        if (adapter == null) adapter = new NotificationsAdapter(getContext(), notificationList, this);
        recyclerViewNotifications.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerViewNotifications.setAdapter(adapter);
    }

    // Tải danh sách thông báo
    private void loadNotifications() {
        if (getContext() == null) {
            Log.e(TAG, "Context rỗng, không tải được thông báo");
            showLoading(false);
            updateEmptyStateVisibility();
            return;
        }
        if (apiService == null) {
            Log.e(TAG, "ApiService rỗng, khởi tạo lại");
            initApiService();
            if (apiService == null) {
                Toast.makeText(getContext(), "Lỗi dịch vụ API.", Toast.LENGTH_SHORT).show();
                showLoading(false);
                updateEmptyStateVisibility();
                return;
            }
        }
        if (currentAccessToken == null) {
            Log.w(TAG, "Token rỗng");
            notificationList.clear();
            if (adapter != null) adapter.updateData(new ArrayList<>(notificationList));
            showLoading(false);
            updateEmptyStateVisibility();
            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).onNotificationStatusChanged();
            }
            return;
        }

        showLoading(true);
        Log.d(TAG, "Tải thông báo...");
        Call<NotificationsApiResponse> call = apiService.getNotifications("Bearer " + currentAccessToken);
        call.enqueue(new Callback<NotificationsApiResponse>() {
            @Override
            public void onResponse(@NonNull Call<NotificationsApiResponse> call, @NonNull Response<NotificationsApiResponse> response) {
                showLoading(false);
                if (getActivity() == null || !isAdded()) return;
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    Log.d(TAG, "Đã tải thông báo: " + (response.body().getData() != null ? response.body().getData().size() : 0));
                    notificationList.clear();
                    if (response.body().getData() != null) notificationList.addAll(response.body().getData());
                    if (adapter != null) adapter.updateData(new ArrayList<>(notificationList));
                } else {
                    Log.e(TAG, "Lỗi tải thông báo: " + response.code());
                    if (response.code() == 401) {
                        Toast.makeText(getContext(), "Phiên đăng nhập hết hạn.", Toast.LENGTH_LONG).show();
                        navigateToLogin();
                    } else {
                        String errorMsg = "Lỗi tải thông báo: " + response.code();
                        try {
                            if (response.errorBody() != null) errorMsg += " - " + response.errorBody().string();
                        } catch (IOException e) {
                            Log.e(TAG, "Lỗi đọc thông báo lỗi", e);
                        }
                        Toast.makeText(getContext(), errorMsg, Toast.LENGTH_SHORT).show();
                    }
                    notificationList.clear();
                    if (adapter != null) adapter.updateData(new ArrayList<>(notificationList));
                }
                updateEmptyStateVisibility();
                if (getActivity() instanceof MainActivity) {
                    ((MainActivity) getActivity()).onNotificationStatusChanged();
                }
            }

            @Override
            public void onFailure(@NonNull Call<NotificationsApiResponse> call, @NonNull Throwable t) {
                showLoading(false);
                if (getActivity() == null || !isAdded()) return;
                Log.e(TAG, "Lỗi mạng khi tải thông báo", t);
                Toast.makeText(getContext(), "Lỗi mạng: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                notificationList.clear();
                if (adapter != null) adapter.updateData(new ArrayList<>(notificationList));
                updateEmptyStateVisibility();
                if (getActivity() instanceof MainActivity) {
                    ((MainActivity) getActivity()).onNotificationStatusChanged();
                }
            }
        });
    }

    // Hiển thị trạng thái tải
    private void showLoading(boolean isLoading) {
        if (progressBar != null) progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        if (recyclerViewNotifications != null) recyclerViewNotifications.setVisibility(isLoading ? View.GONE : View.VISIBLE);
        if (emptyStateLayout != null && !isLoading) {
            updateEmptyStateVisibility();
        } else if (emptyStateLayout != null && isLoading) {
            emptyStateLayout.setVisibility(View.GONE);
        }
    }

    // Cập nhật trạng thái giao diện khi không có thông báo
    private void updateEmptyStateVisibility() {
        if (adapter == null || recyclerViewNotifications == null || emptyStateLayout == null || getContext() == null) return;
        if (progressBar != null && progressBar.getVisibility() == View.VISIBLE) {
            recyclerViewNotifications.setVisibility(View.GONE);
            emptyStateLayout.setVisibility(View.GONE);
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

    // Xử lý khi nhấn vào thông báo
    @Override
    public void onNotificationClick(Notification notification, int position) {
        if (getContext() == null || notification == null) {
            Log.w(TAG, "Context hoặc thông báo rỗng");
            return;
        }
        Log.d(TAG, "Nhấn thông báo ID: " + notification.getId() + ", Đã đọc: " + notification.isRead());

        if (apiService == null) {
            Log.e(TAG, "ApiService rỗng, khởi tạo lại");
            initApiService();
            if (apiService == null) {
                Toast.makeText(getContext(), "Lỗi dịch vụ API.", Toast.LENGTH_SHORT).show();
                proceedWithNotificationAction(notification);
                return;
            }
        }
        if (currentAccessToken == null) {
            Log.w(TAG, "Token rỗng");
            proceedWithNotificationAction(notification);
            return;
        }

        if (!notification.isRead()) {
            Log.d(TAG, "Đánh dấu thông báo đã đọc");
            Call<NotificationDetailApiResponse> call = apiService.markNotificationAsRead("Bearer " + currentAccessToken, notification.getId());
            call.enqueue(new Callback<NotificationDetailApiResponse>() {
                @Override
                public void onResponse(@NonNull Call<NotificationDetailApiResponse> call, @NonNull Response<NotificationDetailApiResponse> response) {
                    if (getActivity() != null && isAdded()) {
                        if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                            Notification updatedNotification = response.body().getData();
                            if (updatedNotification != null && updatedNotification.isRead()) {
                                Log.d(TAG, "Đã đánh dấu thông báo " + notification.getId() + " là đã đọc");
                                if (position >= 0 && position < notificationList.size()) {
                                    notificationList.get(position).setRead(true);
                                    if (adapter != null) adapter.notifyItemChanged(position);
                                    if (getActivity() instanceof MainActivity) {
                                        ((MainActivity) getActivity()).onNotificationStatusChanged();
                                    }
                                }
                            }
                        }
                        proceedWithNotificationAction(notification);
                    }
                }

                @Override
                public void onFailure(@NonNull Call<NotificationDetailApiResponse> call, @NonNull Throwable t) {
                    if (getActivity() != null && isAdded()) {
                        Log.e(TAG, "Lỗi mạng khi đánh dấu thông báo", t);
                        proceedWithNotificationAction(notification);
                    }
                }
            });
        } else {
            Log.d(TAG, "Thông báo đã đọc, chỉ điều hướng");
            proceedWithNotificationAction(notification);
        }
    }

    // Chuyển hướng dựa trên thông báo
    private void proceedWithNotificationAction(Notification notification) {
        if (getContext() == null || notification == null) {
            Log.w(TAG, "Context hoặc thông báo rỗng");
            Toast.makeText(getContext(), "Không thể xử lý thông báo.", Toast.LENGTH_SHORT).show();
            return;
        }

        Log.d(TAG, "Xử lý hành động cho thông báo ID: " + notification.getId());
        int finalJobId = 0;
        Job jobForIntent = null;
        Application application = notification.getApplication();

        if (application != null) {
            Log.d(TAG, "Application ID: " + application.getId());
            if (application.getJob() != null && application.getJob().getId() != 0) {
                finalJobId = application.getJob().getId();
                jobForIntent = application.getJob();
                Log.d(TAG, "Job ID từ Application.getJob(): " + finalJobId);
            } else if (application.getJobId() != 0) {
                finalJobId = application.getJobId();
                jobForIntent = new Job();
                jobForIntent.setId(finalJobId);
                Log.d(TAG, "Job ID từ Application.getJobId(): " + finalJobId);
            }
        } else {
            Log.w(TAG, "Application rỗng");
        }

        if (finalJobId != 0) {
            Intent intent = new Intent(getContext(), JobDetailActivity.class);
            if (jobForIntent != null) {
                intent.putExtra(JobAdapter.JOB_DETAIL_KEY, jobForIntent);
                Log.d(TAG, "Mở JobDetailActivity với Job ID: " + finalJobId);
            } else {
                intent.putExtra("JOB_ID", finalJobId);
                Log.d(TAG, "Mở JobDetailActivity với JOB_ID: " + finalJobId);
            }
            try {
                startActivity(intent);
            } catch (Exception e) {
                Log.e(TAG, "Lỗi mở JobDetailActivity", e);
                Toast.makeText(getContext(), "Lỗi mở chi tiết công việc.", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(getContext(), "Không thể mở chi tiết công việc.", Toast.LENGTH_LONG).show();
            Log.w(TAG, "Không tìm thấy Job ID cho thông báo ID: " + notification.getId());
        }
    }

    // Điều hướng đến màn hình đăng nhập
    private void navigateToLogin() {
        if (getActivity() != null && isAdded()) {
            Intent intent = new Intent(getActivity(), LoginActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            getActivity().finish();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        // Tải lại thông báo khi fragment hiển thị
        Log.d(TAG, "NotificationFragment onResume");
        if (currentAccessToken == null && getContext() != null) loadAuthToken();
        loadNotifications();
    }
}