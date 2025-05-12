package com.example.foodorderapp.features.settings.ui.activity;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
// import androidx.appcompat.app.AppCompatDelegate; // Bỏ nếu không dùng Dark Mode nữa
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.example.foodorderapp.R;
import com.example.foodorderapp.config.Config;
import com.example.foodorderapp.features.auth.ui.activity.LoginActivity;
import com.example.foodorderapp.features.settings.ui.adapter.SettingsAdapter;
import com.example.foodorderapp.features.settings.ui.model.SettingItem;
import com.example.foodorderapp.network.ApiService;
import com.google.android.material.appbar.MaterialToolbar;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class SettingsActivity extends AppCompatActivity implements SettingsAdapter.OnSettingItemClickListener {

    private static final String TAG = "SettingsActivity";
    private static final String PREFS_NAME = "AppSettingsPrefs";
    private static final String KEY_APP_NOTIFICATION = "appNotificationEnabled";

    private RecyclerView recyclerViewSettings;
    private SettingsAdapter adapter;
    private List<SettingItem> settingItems;
    private SharedPreferences sharedPreferences;
    private ApiService apiService;

    private static final String ACTION_CONTACT_US = "action_contact_us";
    private static final String ACTION_SHARE_APP = "action_share_app";
    private static final String ACTION_RATE_US = "action_rate_us";
    private static final String ACTION_APP_NOTIFICATION = "action_app_notification";
    private static final String ACTION_LOGOUT = "action_logout";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        // Nếu bạn đã loại bỏ hoàn toàn Dark Mode, không cần dòng applyDarkMode() này nữa.
        // Hoặc nếu bạn muốn app vẫn theo theme hệ thống nhưng chỉ bỏ switch, thì giữ lại.
        // Giả sử bạn bỏ hoàn toàn:
        // applyDarkMode(isDarkModeEnabled());

        setContentView(R.layout.activity_settings);

        initApiService(); // Khởi tạo apiService

        MaterialToolbar toolbar = findViewById(R.id.toolbar_settings);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        recyclerViewSettings = findViewById(R.id.recyclerView_settings);
        recyclerViewSettings.setLayoutManager(new LinearLayoutManager(this));

        loadSettingsData();

        adapter = new SettingsAdapter(this, settingItems, this);
        recyclerViewSettings.setAdapter(adapter);
    }

    private void initApiService() {
        String baseUrl = Config.BE_URL;
        // Đảm bảo baseUrl không null và kết thúc bằng dấu /
        if (baseUrl != null && !baseUrl.isEmpty()) {
            if (!baseUrl.endsWith("/")) {
                baseUrl += "/"; // Thêm dấu / nếu thiếu
            }
        } else {
            Log.e(TAG, "Config.BE_URL is null or empty!");
            Toast.makeText(this, "Lỗi cấu hình URL máy chủ.", Toast.LENGTH_LONG).show();
            // Cân nhắc finish() activity hoặc không cho phép thực hiện các hành động API
            // finish(); // Hoặc return để ngăn khởi tạo Retrofit
            return;
        }

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(baseUrl) // baseUrl bây giờ đã được đảm bảo có dấu /
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        apiService = retrofit.create(ApiService.class);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.settings_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        if (item.getItemId() == R.id.action_more) {
            Toast.makeText(this, "More options clicked!", Toast.LENGTH_SHORT).show();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }


    private void loadSettingsData() {
        settingItems = new ArrayList<>();
        settingItems.add(new SettingItem(getString(R.string.settings_general_header)));
        settingItems.add(new SettingItem(SettingItem.TYPE_NAVIGATION, R.drawable.ic_phone_24,
                getString(R.string.settings_contact_us_title), getString(R.string.settings_contact_us_subtitle), ACTION_CONTACT_US));
        settingItems.add(new SettingItem(SettingItem.TYPE_NAVIGATION, R.drawable.ic_share_24,
                getString(R.string.settings_share_title), getString(R.string.settings_share_subtitle), ACTION_SHARE_APP));
        settingItems.add(new SettingItem(SettingItem.TYPE_NAVIGATION, R.drawable.ic_star_24,
                getString(R.string.settings_rate_us_title), getString(R.string.settings_rate_us_subtitle), ACTION_RATE_US));

        settingItems.add(new SettingItem(getString(R.string.settings_notification_header)));
        settingItems.add(new SettingItem(R.drawable.ic_notifications_24,
                getString(R.string.settings_app_notification_title), isAppNotificationEnabled(), ACTION_APP_NOTIFICATION));
        // Đã bỏ mục Dark Mode

        settingItems.add(new SettingItem(SettingItem.TYPE_ACTION, R.drawable.ic_logout_24,
                getString(R.string.settings_logout_title), null, ACTION_LOGOUT));
    }

    @Override
    public void onItemClick(SettingItem item, int position) {
        if (item.getActionKey() == null) return;

        switch (item.getActionKey()) {
            case ACTION_CONTACT_US:
                Intent emailIntent = new Intent(Intent.ACTION_SENDTO, Uri.fromParts(
                        "mailto","contact@example.com", null));
                emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Question about FoodOrderApp");
                if (emailIntent.resolveActivity(getPackageManager()) != null) {
                    startActivity(emailIntent);
                } else {
                    Toast.makeText(this, "No email app found", Toast.LENGTH_SHORT).show();
                }
                break;
            case ACTION_SHARE_APP:
                Intent shareIntent = new Intent(Intent.ACTION_SEND);
                shareIntent.setType("text/plain");
                shareIntent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.share_subject));
                shareIntent.putExtra(Intent.EXTRA_TEXT, getString(R.string.share_text));
                startActivity(Intent.createChooser(shareIntent, getString(R.string.share_via)));
                break;
            case ACTION_RATE_US:
                final String appPackageName = getPackageName();
                try {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + appPackageName)));
                } catch (android.content.ActivityNotFoundException anfe) {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + appPackageName)));
                }
                break;
            case ACTION_LOGOUT:
                showLogoutConfirmationDialog();
                break;
        }
    }

    @Override
    public void onSwitchChange(SettingItem item, int position, boolean isChecked) {
        if (item.getActionKey() == null) return;

        if (ACTION_APP_NOTIFICATION.equals(item.getActionKey())) {
            saveAppNotificationPreference(isChecked);
            Toast.makeText(this, "Notifications " + (isChecked ? "Enabled" : "Disabled"), Toast.LENGTH_SHORT).show();
        }
    }

    private void performLogout() {
        SharedPreferences authPrefs = getSharedPreferences("AuthPrefs", MODE_PRIVATE);
        String accessToken = authPrefs.getString("accessToken", null);

        if (apiService == null) { // Kiểm tra apiService đã được khởi tạo chưa
            Toast.makeText(this, "Lỗi: Dịch vụ API chưa sẵn sàng.", Toast.LENGTH_SHORT).show();
            Log.e(TAG, "ApiService is null in performLogout. Check initApiService.");
            return;
        }

        if (accessToken == null || accessToken.isEmpty()) {
            Toast.makeText(this, "Không tìm thấy thông tin đăng nhập.", Toast.LENGTH_SHORT).show();
            navigateToLogin();
            return;
        }

        Toast.makeText(this, "Đang đăng xuất...", Toast.LENGTH_SHORT).show();

        Call<Void> call = apiService.logout("Bearer " + accessToken);
        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(SettingsActivity.this, "Đăng xuất thành công!", Toast.LENGTH_SHORT).show();
                    SharedPreferences.Editor authEditor = authPrefs.edit();
                    authEditor.clear();
                    authEditor.apply();
                    navigateToLogin();
                } else {
                    String errorMessage = "Đăng xuất thất bại.";
                    if (response.errorBody() != null) {
                        try {
                            errorMessage += " Lỗi: " + response.code() + " - " + response.errorBody().string();
                        } catch (IOException e) {
                            Log.e(TAG, "Lỗi đọc errorBody", e);
                            errorMessage += " Mã lỗi: " + response.code();
                        }
                    } else {
                        errorMessage += " Mã lỗi: " + response.code();
                    }
                    Toast.makeText(SettingsActivity.this, errorMessage, Toast.LENGTH_LONG).show();
                    Log.e(TAG, "Logout failed: " + errorMessage);
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(SettingsActivity.this, "Lỗi mạng: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                Log.e(TAG, "Logout network failure: " + t.getMessage(), t);
            }
        });
    }

    private void navigateToLogin() {
        Intent intent = new Intent(SettingsActivity.this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void showLogoutConfirmationDialog() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.settings_logout_title)
                .setMessage("Bạn có chắc chắn muốn đăng xuất không?")
                .setPositiveButton("Đăng xuất", (dialog, which) -> {
                    performLogout();
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void saveAppNotificationPreference(boolean isEnabled) {
        sharedPreferences.edit().putBoolean(KEY_APP_NOTIFICATION, isEnabled).apply();
    }

    private boolean isAppNotificationEnabled() {
        return sharedPreferences.getBoolean(KEY_APP_NOTIFICATION, true);
    }
}