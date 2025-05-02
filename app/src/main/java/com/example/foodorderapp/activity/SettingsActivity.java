package com.example.foodorderapp.activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate; // Import để đổi Dark Mode
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.content.Intent; // Import Intent
import android.net.Uri; // Import Uri
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;
import android.app.AlertDialog; // Import AlertDialog

import com.example.foodorderapp.R;
import com.example.foodorderapp.adapter.SettingsAdapter;
import com.example.foodorderapp.model.ClickableItem;
import com.example.foodorderapp.model.HeaderItem;
import com.example.foodorderapp.model.LogoutItem;
import com.example.foodorderapp.model.SettingsItem;
import com.example.foodorderapp.model.SwitchItem;

import java.util.ArrayList;
import java.util.List;

public class SettingsActivity extends AppCompatActivity {

    private Toolbar mToolbar;
    private RecyclerView mRecyclerViewSettings;
    private SettingsAdapter mAdapter;
    private List<SettingsItem> mSettingsItemsList;

    // TODO: Nên đọc các giá trị này từ SharedPreferences
    private boolean mIsAppNotificationEnabled = true;
    private boolean mIsDarkModeEnabled = false; // Kiểm tra trạng thái hiện tại của hệ thống nếu cần


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // TODO: Đọc trạng thái DarkMode từ SharedPreferences và áp dụng *trước* setContentView nếu cần
        // mIsDarkModeEnabled = readDarkModeStateFromPreferences();
        // applyDarkMode(mIsDarkModeEnabled); // Hàm helper để gọi AppCompatDelegate

        setContentView(R.layout.activity_settings);

        // --- Toolbar Setup ---
        mToolbar = findViewById(R.id.toolbarSettings);
        setSupportActionBar(mToolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        mToolbar.setNavigationOnClickListener(v -> onBackPressed());

        // --- RecyclerView Setup ---
        mRecyclerViewSettings = findViewById(R.id.recyclerViewSettings);

        // TODO: Đọc trạng thái Notification từ SharedPreferences
        // mIsAppNotificationEnabled = readNotificationStateFromPreferences();

        mSettingsItemsList = createSettingsList();
        mAdapter = new SettingsAdapter(mSettingsItemsList);

        mRecyclerViewSettings.setLayoutManager(new LinearLayoutManager(this));
        mRecyclerViewSettings.setAdapter(mAdapter);

        // Optional: Thêm đường kẻ phân cách
        // androidx.recyclerview.widget.DividerItemDecoration divider = new androidx.recyclerview.widget.DividerItemDecoration(this, LinearLayoutManager.VERTICAL);
        // mRecyclerViewSettings.addItemDecoration(divider);
    }

    // Hàm tạo danh sách dữ liệu hoàn chỉnh
    private List<SettingsItem> createSettingsList() {
        List<SettingsItem> items = new ArrayList<>();

        items.add(new HeaderItem(getString(R.string.settings_header_general)));
        items.add(new ClickableItem(R.drawable.ic_contact_us, getString(R.string.settings_contact_us_title), getString(R.string.settings_contact_us_subtitle),
                () -> {
                    // Action: Mở màn hình liên hệ hoặc thực hiện cuộc gọi/gửi email
                    Log.d("SettingsActivity", "Contact Us Clicked");
                    // Ví dụ: Intent intent = new Intent(Intent.ACTION_DIAL); // Hoặc ACTION_SENDTO cho email
                    // intent.setData(Uri.parse("tel:YOUR_PHONE_NUMBER")); // Thay số điện thoại
                    // startActivity(intent);
                    Toast.makeText(this, "Implement Contact Us action", Toast.LENGTH_SHORT).show();
                }
        ));
        items.add(new ClickableItem(R.drawable.ic_share, getString(R.string.settings_share_title), getString(R.string.settings_share_subtitle),
                () -> {
                    // Action: Mở Intent chia sẻ ứng dụng
                    Log.d("SettingsActivity", "Share Clicked");
                    try {
                        Intent shareIntent = new Intent(Intent.ACTION_SEND);
                        shareIntent.setType("text/plain");
                        shareIntent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.app_name));
                        String shareMessage= "\nLet me recommend you this application\n\n";
                        // TODO: Thay bằng link ứng dụng trên Play Store
                        shareMessage = shareMessage + "https://play.google.com/store/apps/details?id=" + getPackageName() +"\n\n";
                        shareIntent.putExtra(Intent.EXTRA_TEXT, shareMessage);
                        startActivity(Intent.createChooser(shareIntent, "Choose one"));
                    } catch(Exception e) {
                        Log.e("SettingsActivity", "Error sharing app", e);
                        Toast.makeText(this, "Could not initiate share.", Toast.LENGTH_SHORT).show();
                    }
                }
        ));
        items.add(new ClickableItem(R.drawable.ic_rate_us, getString(R.string.settings_rate_us_title), getString(R.string.settings_rate_us_subtitle),
                () -> {
                    // Action: Mở link đến Play Store để đánh giá
                    Log.d("SettingsActivity", "Rate Us Clicked");
                    try {
                        // TODO: Thay bằng link ứng dụng trên Play Store
                        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + getPackageName())));
                    } catch (android.content.ActivityNotFoundException anfe) {
                        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + getPackageName())));
                    } catch(Exception e) {
                        Log.e("SettingsActivity", "Error opening Play Store", e);
                        Toast.makeText(this, "Could not open Play Store.", Toast.LENGTH_SHORT).show();
                    }
                }
        ));

        items.add(new HeaderItem(getString(R.string.settings_header_notification)));
        items.add(new SwitchItem(R.drawable.ic_notification, getString(R.string.settings_app_notification_title), mIsAppNotificationEnabled,
                isChecked -> {
                    // Action: Lưu trạng thái Notification
                    mIsAppNotificationEnabled = isChecked;
                    // TODO: Lưu mIsAppNotificationEnabled vào SharedPreferences
                    Log.d("SettingsActivity", "App Notification toggled: " + isChecked);
                    // TODO: Có thể bật/tắt đăng ký nhận push notification ở đây nếu cần
                    Toast.makeText(this, "Notification setting " + (isChecked ? "ON" : "OFF"), Toast.LENGTH_SHORT).show();
                }
        ));
        items.add(new SwitchItem(R.drawable.ic_dark_mode, getString(R.string.settings_dark_mode_title), isNightModeActive(), // Lấy trạng thái hiện tại
                isChecked -> {
                    // Action: Áp dụng Dark Mode và lưu trạng thái
                    mIsDarkModeEnabled = isChecked;
                    applyDarkMode(mIsDarkModeEnabled);
                    // TODO: Lưu mIsDarkModeEnabled vào SharedPreferences để lần sau mở app áp dụng đúng
                    Log.d("SettingsActivity", "Dark Mode toggled: " + isChecked);
                }
        ));

        items.add(new LogoutItem(R.drawable.ic_logout, getString(R.string.settings_logout_title),
                () -> {
                    // Action: Hiển thị Dialog xác nhận Logout
                    Log.d("SettingsActivity", "Logout Clicked");
                    showLogoutConfirmationDialog();
                }
        ));

        return items;
    }

    // Hàm kiểm tra xem Dark Mode có đang bật không (dựa trên cài đặt hệ thống/AppCompat)
    private boolean isNightModeActive() {
        int nightModeFlags = getResources().getConfiguration().uiMode & android.content.res.Configuration.UI_MODE_NIGHT_MASK;
        return nightModeFlags == android.content.res.Configuration.UI_MODE_NIGHT_YES;
        // Hoặc đọc từ SharedPreferences nếu bạn lưu trạng thái riêng
        // return mIsDarkModeEnabled;
    }

    // Hàm áp dụng Dark Mode
    private void applyDarkMode(boolean enable) {
        AppCompatDelegate.setDefaultNightMode(
                enable ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO
        );
        // Không cần gọi recreate() ngay lập tức nếu bạn chỉ muốn thay đổi lần sau
        // Nếu muốn áp dụng ngay, bạn cần cơ chế để Activity tự khởi động lại (recreate())
        // hoặc xử lý thay đổi theme linh hoạt hơn.
        // Lưu trạng thái vào SharedPreferences ở đây
        // saveDarkModeStateToPreferences(enable);
        Toast.makeText(this,"Dark Mode " + (enable ? "ON" : "OFF") + ". Restart app may be needed.", Toast.LENGTH_LONG).show();

    }

    // Hàm hiển thị Dialog xác nhận Logout
    private void showLogoutConfirmationDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Logout")
                .setMessage("Are you sure you want to logout?")
                .setPositiveButton("Logout", (dialog, which) -> {
                    // TODO: Thực hiện Logout thực sự
                    // - Xóa session/token
                    // - Xóa dữ liệu người dùng tạm thời
                    // - Điều hướng về màn hình Login/Welcome
                    Log.d("SettingsActivity", "Logout Confirmed");
                    Toast.makeText(this, "Logging out...", Toast.LENGTH_SHORT).show();
                    // Ví dụ: navigateToLoginScreen();
                    // finish(); // Đóng màn hình Settings sau khi logout
                })
                .setNegativeButton("Cancel", (dialog, which) -> {
                    // Người dùng chọn Cancel, không làm gì cả
                    dialog.dismiss();
                })
                .setIcon(R.drawable.ic_logout) // Icon cho dialog
                .show();
    }

    // TODO: Implement helper methods to read/save settings from/to SharedPreferences
    // private boolean readDarkModeStateFromPreferences() { ... }
    // private void saveDarkModeStateToPreferences(boolean enabled) { ... }
    // private boolean readNotificationStateFromPreferences() { ... }
    // private void saveNotificationStateToPreferences(boolean enabled) { ... }
    // private void navigateToLoginScreen() { ... }

}