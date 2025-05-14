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
        notificationList = new ArrayList<>();
        initApiService();
        loadAuthToken();
    }

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
        View view = inflater.inflate(R.layout.fragment_notifications, container, false);
        recyclerViewNotifications = view.findViewById(R.id.recyclerView_notifications);
        emptyStateLayout = view.findViewById(R.id.layout_empty_notifications);
        progressBar = view.findViewById(R.id.progressBar_notifications);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupRecyclerView();
        if (currentAccessToken == null && getContext() != null) loadAuthToken();
    }

    private void setupRecyclerView() {
        if (getContext() == null) {
            Log.e(TAG, "Context rỗng, không thiết lập được RecyclerView");
            return;
        }
        if (adapter == null) adapter = new NotificationsAdapter(getContext(), notificationList, this);
        recyclerViewNotifications.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerViewNotifications.setAdapter(adapter);
    }

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
                Toast.makeText(getContext(), "Lỗi dịch vụ.", Toast.LENGTH_SHORT).show();
                showLoading(false);
                updateEmptyStateVisibility();
                return;
            }
        }
        if (currentAccessToken == null) {
            Log.w(TAG, "Token rỗng, yêu cầu đăng nhập");
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
                if (getActivity() != null && isAdded()) {
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
                                if (response.errorBody() != null) {
                                    errorMsg += " - " + response.errorBody().string();
                                }
                            } catch (IOException e) {
                                Log.e(TAG, "Error reading error body", e);
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
            }

            @Override
            public void onFailure(@NonNull Call<NotificationsApiResponse> call, @NonNull Throwable t) {
                showLoading(false);
                if (getActivity() != null && isAdded()) {
                    Log.e(TAG, "Lỗi mạng khi tải thông báo", t);
                    Toast.makeText(getContext(), "Lỗi mạng: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    notificationList.clear();
                    if (adapter != null) adapter.updateData(new ArrayList<>(notificationList));
                    updateEmptyStateVisibility();
                    if (getActivity() instanceof MainActivity) {
                        ((MainActivity) getActivity()).onNotificationStatusChanged();
                    }
                }
            }
        });
    }

    private void showLoading(boolean isLoading) {
        if (progressBar != null) progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        if (recyclerViewNotifications != null) recyclerViewNotifications.setVisibility(isLoading ? View.GONE : View.VISIBLE);
        if (emptyStateLayout != null) emptyStateLayout.setVisibility(View.GONE);
        if (!isLoading) {
            updateEmptyStateVisibility();
        }
    }

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

    @Override
    public void onNotificationClick(Notification notification, int position) {
        if (getContext() == null || notification == null) return;

        Log.d(TAG, "--- Processing notification click ---");
        Log.d(TAG, "Notification ID: " + notification.getId() + ", isRead: " + notification.isRead());
        if (notification.getApplication() != null) {
            Log.d(TAG, "Application present. Application ID (from getApplication().getId()): " + notification.getApplication().getId());
            Log.d(TAG, "Application.getJobId() (from getApplication().getJobId()): " + notification.getApplication().getJobId());
            if (notification.getApplication().getJob() != null) {
                Log.d(TAG, "Job object present in Application. Job Title: " + notification.getApplication().getJob().getTitle());
                Log.d(TAG, "Job ID from Application.getJob().getId(): " + notification.getApplication().getJob().getId());
            } else {
                Log.d(TAG, "Application.getJob() is NULL");
            }
        } else {
            Log.d(TAG, "Notification.getApplication() is NULL");
        }
        Log.d(TAG, "Notification.getApplicationId() (direct field of Notification object): " + notification.getApplicationId());
        Log.d(TAG, "--- End of notification details ---");


        if (apiService == null) {
            Log.e(TAG, "ApiService rỗng, khởi tạo lại");
            initApiService();
            if (apiService == null) {
                Toast.makeText(getContext(), "Lỗi dịch vụ.", Toast.LENGTH_SHORT).show();
                proceedWithNotificationAction(notification);
                return;
            }
        }
        if (currentAccessToken == null) {
            Log.w(TAG, "Token rỗng, không thể đánh dấu đã đọc");
            proceedWithNotificationAction(notification);
            return;
        }

        if (!notification.isRead()) {
            Call<NotificationDetailApiResponse> call = apiService.markNotificationAsRead("Bearer " + currentAccessToken, notification.getId());
            call.enqueue(new Callback<NotificationDetailApiResponse>() {
                @Override
                public void onResponse(@NonNull Call<NotificationDetailApiResponse> call, @NonNull Response<NotificationDetailApiResponse> response) {
                    if (getActivity() != null && isAdded()) {
                        if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                            Notification updatedNotification = response.body().getData();
                            if (updatedNotification != null && updatedNotification.isRead()) {
                                Log.d(TAG, "Đã đánh dấu thông báo " + notification.getId() + " là đã đọc trên server.");
                                if (position >= 0 && position < notificationList.size()) {
                                    notificationList.get(position).setRead(true);
                                    if (adapter != null) {
                                        adapter.notifyItemChanged(position);
                                    }
                                    if (getActivity() instanceof MainActivity) {
                                        ((MainActivity) getActivity()).onNotificationStatusChanged();
                                    }
                                }
                            }
                        } else {
                            String errorMsg = "Lỗi đánh dấu thông báo: " + response.code();
                            try {
                                if (response.errorBody() != null) {
                                    errorMsg += " - " + response.errorBody().string();
                                }
                            } catch (IOException e) {
                                Log.e(TAG, "Error reading error body for mark as read", e);
                            }
                            Log.e(TAG, errorMsg);
                            Toast.makeText(getContext(), errorMsg, Toast.LENGTH_SHORT).show();
                        }
                        proceedWithNotificationAction(notification);
                    }
                }

                @Override
                public void onFailure(@NonNull Call<NotificationDetailApiResponse> call, @NonNull Throwable t) {
                    if (getActivity() != null && isAdded()) {
                        Log.e(TAG, "Lỗi mạng khi đánh dấu thông báo", t);
                        Toast.makeText(getContext(), "Lỗi mạng: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                        proceedWithNotificationAction(notification);
                    }
                }
            });
        } else {
            Log.d(TAG, "Thông báo ID: " + notification.getId() + " đã được đọc trước đó, chỉ điều hướng.");
            proceedWithNotificationAction(notification);
        }
    }

    private void proceedWithNotificationAction(Notification notification) {
        if (getContext() == null || notification == null) {
            Log.w(TAG, "Context hoặc Notification là null trong proceedWithNotificationAction.");
            Toast.makeText(getContext(), "Không thể xử lý thông báo.", Toast.LENGTH_SHORT).show();
            return;
        }

        Log.d(TAG, ">>> proceedWithNotificationAction for Notification ID: " + notification.getId());

        Job jobToView = null;
        int jobIdToUse = 0;

        // Kịch bản 1: Thông báo có Application object, và Application object đó có Job object bên trong.
        if (notification.getApplication() != null && notification.getApplication().getJob() != null) {
            jobToView = notification.getApplication().getJob();
            jobIdToUse = jobToView.getId();
            Log.d(TAG, "Case 1: Found Job object within Application. Job ID from jobToView.getId(): " + jobIdToUse);

            if (jobIdToUse == 0 && notification.getApplication().getJobId() != 0) {
                jobIdToUse = notification.getApplication().getJobId();
                jobToView.setId(jobIdToUse);
                Log.d(TAG, "Case 1 Fallback: jobToView.getId() was 0. Used Application.getJobId(): " + jobIdToUse);
            }
        }
        // Kịch bản 2: Thông báo có Application object, không có Job object lồng, nhưng Application object có trường jobId.
        else if (notification.getApplication() != null && notification.getApplication().getJobId() != 0) {
            jobIdToUse = notification.getApplication().getJobId();
            Log.d(TAG, "Case 2: No Job object in Application, but Application.getJobId() is present: " + jobIdToUse);
            jobToView = new Job();
            jobToView.setId(jobIdToUse);
        }
        // Kịch bản 3: Sử dụng notification.getApplicationId() nếu các trường hợp trên không tìm thấy Job ID.
        // Đây là ID của đơn ứng tuyển liên kết trực tiếp với thông báo.
        // Giả định rằng notification.getApplicationId() có thể được sử dụng như một Job ID
        // để JobDetailActivity tải chi tiết công việc.
        else if (notification.getApplicationId() != 0) {
            jobIdToUse = notification.getApplicationId(); // Sử dụng ApplicationId của Notification làm JobId
            Log.w(TAG, "Case 3 (Using Notification.getApplicationId() as JobId): " +
                    "No direct Job ID from Application object. Using Notification.getApplicationId(): " + jobIdToUse +
                    ". JobDetailActivity will need to fetch details using this ID.");
            jobToView = new Job();
            jobToView.setId(jobIdToUse); // Tạo Job object tạm thời với ID này
        }

        // Sau khi đã xác định jobIdToUse và jobToView (có thể chỉ chứa ID)
        if (jobToView != null && jobIdToUse != 0) {
            Intent intent = new Intent(getContext(), JobDetailActivity.class);
            intent.putExtra(JobAdapter.JOB_DETAIL_KEY, jobToView);
            Log.d(TAG, "Attempting to open JobDetailActivity for resolved Job ID: " + jobIdToUse + " with Job object: " + jobToView.toString());
            try {
                startActivity(intent);
            } catch (Exception e) {
                Log.e(TAG, "Error starting JobDetailActivity: " + e.getMessage(), e);
                Toast.makeText(getContext(), "Lỗi khi mở chi tiết công việc.", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(getContext(), "Không đủ thông tin để mở chi tiết công việc từ thông báo này (Notification ID: " + notification.getId() + ").", Toast.LENGTH_LONG).show();
            Log.w(TAG, "No valid Job ID or Job object could be determined to open JobDetailActivity for Notification ID: " + notification.getId() +
                    ". Final jobIdToUse: " + jobIdToUse + ", jobToView: " + (jobToView != null ? jobToView.toString() : "null"));
        }
    }


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
        Log.d(TAG, "NotificationFragment onResume called.");
        if (currentAccessToken != null) {
            loadNotifications();
        } else {
            notificationList.clear();
            if(adapter != null) adapter.updateData(new ArrayList<>(notificationList));
            updateEmptyStateVisibility();
            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).onNotificationStatusChanged();
            }
        }
    }
}