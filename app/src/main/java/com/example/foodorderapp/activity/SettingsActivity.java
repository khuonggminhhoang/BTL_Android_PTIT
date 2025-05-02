package com.example.foodorderapp.activity;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.example.foodorderapp.R; // Quan trọng: Import R của app bạn
import com.example.foodorderapp.adapter.SettingsAdapter;
import com.example.foodorderapp.model.SettingItem;
import com.google.android.material.appbar.MaterialToolbar;

import java.util.ArrayList;
import java.util.List;

public class SettingsActivity extends AppCompatActivity implements SettingsAdapter.OnSettingItemClickListener {

    private static final String PREFS_NAME = "AppSettingsPrefs";
    private static final String KEY_DARK_MODE = "darkModeEnabled";
    private static final String KEY_APP_NOTIFICATION = "appNotificationEnabled";

    private RecyclerView recyclerViewSettings;
    private SettingsAdapter adapter;
    private List<SettingItem> settingItems;
    private SharedPreferences sharedPreferences;

    // Định nghĩa action keys để xử lý click đáng tin cậy hơn
    private static final String ACTION_CONTACT_US = "action_contact_us";
    private static final String ACTION_SHARE_APP = "action_share_app";
    private static final String ACTION_RATE_US = "action_rate_us";
    private static final String ACTION_APP_NOTIFICATION = "action_app_notification";
    private static final String ACTION_DARK_MODE = "action_dark_mode";
    private static final String ACTION_LOGOUT = "action_logout";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Áp dụng Dark Mode trước khi setContentView nếu nó đang bật
        sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        applyDarkMode(isDarkModeEnabled()); // Áp dụng theme hiện tại

        setContentView(R.layout.activity_settings);

        MaterialToolbar toolbar = findViewById(R.id.toolbar_settings);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        // Không cần set NavigationOnClickListener nếu dùng onOptionsItemSelected cho nút back

        recyclerViewSettings = findViewById(R.id.recyclerView_settings);
        recyclerViewSettings.setLayoutManager(new LinearLayoutManager(this));
        // Optional: Thêm đường kẻ phân cách giữa các item
        // recyclerViewSettings.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));

        loadSettingsData();

        adapter = new SettingsAdapter(this, settingItems, this);
        recyclerViewSettings.setAdapter(adapter);
    }

    // Load menu (nút 3 chấm)
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.settings_menu, menu);
        return true;
    }

    // Xử lý sự kiện click menu item (bao gồm cả nút back trên toolbar)
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle arrow click here
        if (item.getItemId() == android.R.id.home) {
            finish(); // Đóng activity hiện tại
            return true;
        }
        // Xử lý các item khác trong menu (nếu có)
        if (item.getItemId() == R.id.action_more) {
            Toast.makeText(this, "More options clicked!", Toast.LENGTH_SHORT).show();
            // Thêm logic xử lý cho nút More ở đây
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    private void loadSettingsData() {
        settingItems = new ArrayList<>();

        // --- GENERAL SETTINGS ---
        settingItems.add(new SettingItem(getString(R.string.settings_general_header)));
        settingItems.add(new SettingItem(SettingItem.TYPE_NAVIGATION, R.drawable.ic_phone_24,
                getString(R.string.settings_contact_us_title), getString(R.string.settings_contact_us_subtitle), ACTION_CONTACT_US));
        settingItems.add(new SettingItem(SettingItem.TYPE_NAVIGATION, R.drawable.ic_share_24,
                getString(R.string.settings_share_title), getString(R.string.settings_share_subtitle), ACTION_SHARE_APP));
        settingItems.add(new SettingItem(SettingItem.TYPE_NAVIGATION, R.drawable.ic_star_24,
                getString(R.string.settings_rate_us_title), getString(R.string.settings_rate_us_subtitle), ACTION_RATE_US));

        // --- NOTIFICATION ---
        settingItems.add(new SettingItem(getString(R.string.settings_notification_header)));
        settingItems.add(new SettingItem(R.drawable.ic_notifications_24,
                getString(R.string.settings_app_notification_title), isAppNotificationEnabled(), ACTION_APP_NOTIFICATION));
        settingItems.add(new SettingItem(R.drawable.ic_dark_mode_24,
                getString(R.string.settings_dark_mode_title), isDarkModeEnabled(), ACTION_DARK_MODE));

        // --- ACCOUNT --- (Thêm section nếu cần)
        // settingItems.add(new SettingItem("ACCOUNT"));
        settingItems.add(new SettingItem(SettingItem.TYPE_ACTION, R.drawable.ic_logout_24,
                getString(R.string.settings_logout_title), null, ACTION_LOGOUT));

    }

    // --- Xử lý sự kiện từ Adapter ---

    @Override
    public void onItemClick(SettingItem item, int position) {
        if (item.getActionKey() == null) return;

        switch (item.getActionKey()) {
            case ACTION_CONTACT_US:
                // Ví dụ: Mở email client
                Intent emailIntent = new Intent(Intent.ACTION_SENDTO, Uri.fromParts(
                        "mailto","contact@example.com", null)); // Thay bằng email của bạn
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
                shareIntent.putExtra(Intent.EXTRA_TEXT, getString(R.string.share_text)); // Nhớ sửa link trong strings.xml
                startActivity(Intent.createChooser(shareIntent, getString(R.string.share_via)));
                break;
            case ACTION_RATE_US:
                // Mở Google Play Store (thay package name)
                final String appPackageName = getPackageName(); // Lấy package name của app
                try {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + appPackageName)));
                } catch (android.content.ActivityNotFoundException anfe) {
                    // Nếu không có Play Store, mở trình duyệt
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + appPackageName)));
                }
                break;
            case ACTION_LOGOUT:
                showLogoutConfirmationDialog();
                break;
            // Các action khác nếu có...
        }
    }

    @Override
    public void onSwitchChange(SettingItem item, int position, boolean isChecked) {
        if (item.getActionKey() == null) return;

        switch (item.getActionKey()) {
            case ACTION_APP_NOTIFICATION:
                saveAppNotificationPreference(isChecked);
                Toast.makeText(this, "Notifications " + (isChecked ? "Enabled" : "Disabled"), Toast.LENGTH_SHORT).show();
                // Thêm logic bật/tắt thông báo thực tế ở đây (ví dụ: đăng ký/hủy đăng ký với FCM)
                break;
            case ACTION_DARK_MODE:
                saveDarkModePreference(isChecked);
                applyDarkMode(isChecked);
                recreate(); // Tạo lại Activity để áp dụng theme mới
                break;
            // Các switch khác nếu có...
        }
    }

    // --- Helper Methods ---

    private void showLogoutConfirmationDialog() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.settings_logout_title)
                .setMessage("Are you sure you want to logout?")
                .setPositiveButton("Logout", (dialog, which) -> {
                    // Thực hiện logout: Xóa token, xóa SharedPreferences, quay về LoginActivity
                    Toast.makeText(this, "Logging out...", Toast.LENGTH_SHORT).show();
                    // Ví dụ: Xóa SharedPreferences liên quan đến user
                    // getSharedPreferences("UserPrefs", MODE_PRIVATE).edit().clear().apply();

                    // Ví dụ: Chuyển về LoginActivity và xóa các activity trước đó
                    // Intent intent = new Intent(SettingsActivity.this, LoginActivity.class);
                    // intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    // startActivity(intent);
                    // finish();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void saveAppNotificationPreference(boolean isEnabled) {
        sharedPreferences.edit().putBoolean(KEY_APP_NOTIFICATION, isEnabled).apply();
    }

    private boolean isAppNotificationEnabled() {
        // Mặc định là bật thông báo
        return sharedPreferences.getBoolean(KEY_APP_NOTIFICATION, true);
    }

    private void saveDarkModePreference(boolean isEnabled) {
        sharedPreferences.edit().putBoolean(KEY_DARK_MODE, isEnabled).apply();
    }

    private boolean isDarkModeEnabled() {
        // Mặc định là tắt dark mode
        return sharedPreferences.getBoolean(KEY_DARK_MODE, false);
    }

    private void applyDarkMode(boolean isEnabled) {
        AppCompatDelegate.setDefaultNightMode(
                isEnabled ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO
        );
    }
}