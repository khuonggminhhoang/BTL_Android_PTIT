package com.example.foodorderapp.features.main.ui.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast;

import com.example.foodorderapp.R;
import com.example.foodorderapp.config.Config;
import com.example.foodorderapp.core.model.Notification;
import com.example.foodorderapp.features.auth.ui.activity.LoginActivity;
import com.example.foodorderapp.features.main.ui.fragment.FavoritesFragment;
import com.example.foodorderapp.features.main.ui.fragment.HomeFragment;
import com.example.foodorderapp.features.main.ui.fragment.NotificationFragment;
import com.example.foodorderapp.features.profile.ui.fragment.ProfileFragment;
import com.example.foodorderapp.network.ApiService;
import com.example.foodorderapp.network.response.NotificationsApiResponse;
import com.google.android.material.badge.BadgeDrawable; // Quan trọng: Import BadgeDrawable
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;

import java.io.IOException;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private BottomNavigationView bottomNavigationView;
    private ApiService apiService;
    private String currentAccessToken;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bottomNavigationView = findViewById(R.id.bottomNavigationView);

        initApiService();
        loadAuthToken();

        if (savedInstanceState == null) {
            loadFragment(new HomeFragment(), "HOME_FRAGMENT");
        }

        setupBottomNavigation();

        // Kiểm tra thông báo chưa đọc khi MainActivity được tạo
        if (currentAccessToken != null) {
            fetchAndRefreshNotificationBadge();
        }
    }

    private void initApiService() {
        String baseUrl = Config.BE_URL;
        if (baseUrl == null || baseUrl.isEmpty()) {
            Log.e(TAG, "BE_URL is not configured!");
            Toast.makeText(this, "Lỗi cấu hình máy chủ.", Toast.LENGTH_LONG).show();
            return;
        }
        if (!baseUrl.endsWith("/")) {
            baseUrl += "/";
        }
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(baseUrl)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        apiService = retrofit.create(ApiService.class);
    }

    private void loadAuthToken() {
        SharedPreferences prefs = getSharedPreferences("AuthPrefs", Context.MODE_PRIVATE);
        currentAccessToken = prefs.getString("accessToken", null);
        if (currentAccessToken == null) {
            Log.w(TAG, "Access token is null. User might not be logged in.");
        }
    }

    private void setupBottomNavigation() {
        bottomNavigationView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                Fragment selectedFragment = null;
                String tag = null;
                int itemId = item.getItemId();

                if (itemId == R.id.navigation_home) {
                    selectedFragment = findOrCreateFragment(HomeFragment.class, "HOME_FRAGMENT");
                    tag = "HOME_FRAGMENT";
                } else if (itemId == R.id.navigation_favorites) {
                    selectedFragment = findOrCreateFragment(FavoritesFragment.class, "FAVORITES_FRAGMENT");
                    tag = "FAVORITES_FRAGMENT";
                } else if (itemId == R.id.navigation_chat) {
                    selectedFragment = findOrCreateFragment(NotificationFragment.class, "NOTIFICATION_FRAGMENT");
                    tag = "NOTIFICATION_FRAGMENT";
                    // Khi chọn tab thông báo, cũng nên cập nhật badge
                    if (currentAccessToken != null) {
                        fetchAndRefreshNotificationBadge();
                    }
                } else if (itemId == R.id.navigation_profile) {
                    selectedFragment = findOrCreateFragment(ProfileFragment.class, "PROFILE_FRAGMENT");
                    tag = "PROFILE_FRAGMENT";
                }

                if (selectedFragment != null) {
                    loadFragment(selectedFragment, tag);
                    return true;
                }
                return false;
            }
        });
    }

    private Fragment findOrCreateFragment(Class<? extends Fragment> fragmentClass, String tag) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        Fragment fragment = fragmentManager.findFragmentByTag(tag);
        if (fragment == null) {
            try {
                fragment = fragmentClass.newInstance();
            } catch (IllegalAccessException | InstantiationException e) {
                Log.e(TAG, "Error creating fragment instance", e);
                return new HomeFragment();
            }
        }
        return fragment;
    }


    private void loadFragment(Fragment fragment, String tag) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

        // Sử dụng replace thay vì add để tránh fragment chồng chéo khi chuyển tab
        fragmentTransaction.replace(R.id.fragment_container, fragment, tag);
        fragmentTransaction.commit();
    }

    public void fetchAndRefreshNotificationBadge() {
        if (apiService == null || currentAccessToken == null) {
            Log.w(TAG, "ApiService or AccessToken is null. Cannot fetch notifications for badge.");
            updateNotificationBadge(0); // Ẩn badge nếu không thể fetch
            return;
        }

        Log.d(TAG, "Fetching notifications to update badge...");
        apiService.getNotifications("Bearer " + currentAccessToken).enqueue(new Callback<NotificationsApiResponse>() {
            @Override
            public void onResponse(@NonNull Call<NotificationsApiResponse> call, @NonNull Response<NotificationsApiResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    List<Notification> notifications = response.body().getData();
                    int unreadCount = 0;
                    if (notifications != null) {
                        for (Notification notification : notifications) {
                            if (!notification.isRead()) {
                                unreadCount++;
                            }
                        }
                    }
                    Log.d(TAG, "Unread notification count: " + unreadCount);
                    updateNotificationBadge(unreadCount);
                } else {
                    Log.e(TAG, "Failed to fetch notifications for badge. Code: " + response.code());
                    if (response.code() == 401) {
                        handleLogout();
                    }
                    updateNotificationBadge(0); // Ẩn badge khi có lỗi
                }
            }

            @Override
            public void onFailure(@NonNull Call<NotificationsApiResponse> call, @NonNull Throwable t) {
                Log.e(TAG, "Network error fetching notifications for badge: " + t.getMessage(), t);
                updateNotificationBadge(0); // Ẩn badge khi có lỗi mạng
            }
        });
    }

    private void updateNotificationBadge(int unreadCount) {
        if (bottomNavigationView == null) return;

        BadgeDrawable badge = bottomNavigationView.getOrCreateBadge(R.id.navigation_chat);
        if (unreadCount > 0) {
            badge.setVisible(true);
            badge.setBackgroundColor(ContextCompat.getColor(this, R.color.red));
        } else {
            badge.setVisible(false);
        }
    }

    private void handleLogout() {
        // Xóa token và chuyển hướng đến LoginActivity
        SharedPreferences prefs = getSharedPreferences("AuthPrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.remove("accessToken");
        editor.remove("refreshToken");
        // Xóa các thông tin người dùng khác nếu có
        editor.apply();

        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
        Toast.makeText(this, "Phiên đăng nhập đã hết hạn.", Toast.LENGTH_LONG).show();
    }


    // Phương thức này được gọi từ NotificationFragment khi có sự thay đổi trạng thái đọc
    public void onNotificationStatusChanged() {
        if (currentAccessToken != null) {
            fetchAndRefreshNotificationBadge();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Cập nhật badge khi activity resume, ví dụ sau khi quay lại từ màn hình khác
        if (currentAccessToken != null) {
            fetchAndRefreshNotificationBadge();
        }
    }

    private void navigateToLogin() {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}