package com.example.foodorderapp.features.main.ui.activity; // Package của bạn

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.os.Bundle;
import android.view.MenuItem; // Import MenuItem

import com.example.foodorderapp.R;
import com.example.foodorderapp.features.main.ui.fragment.FavoritesFragment;
import com.example.foodorderapp.features.main.ui.fragment.HomeFragment;
import com.example.foodorderapp.features.main.ui.fragment.NotificationFragment;
import com.example.foodorderapp.features.profile.ui.fragment.ProfileFragment; // Import ProfileFragment
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView; // Import đúng

public class MainActivity extends AppCompatActivity {

    private BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bottomNavigationView = findViewById(R.id.bottomNavigationView);

        // Load Fragment mặc định khi Activity khởi chạy
        if (savedInstanceState == null) {
            loadFragment(new HomeFragment(), "HOME_FRAGMENT"); // Load HomeFragment ban đầu với tag
        }

        // Thiết lập Bottom Navigation View để chuyển đổi Fragment
        setupBottomNavigation();
    }

    // Thiết lập Bottom Navigation View
    private void setupBottomNavigation() {
        // Sử dụng setOnItemSelectedListener thay vì setOnNavigationItemSelectedListener
        bottomNavigationView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                Fragment selectedFragment = null;
                String tag = null; // Tag để quản lý fragment
                int itemId = item.getItemId();

                if (itemId == R.id.navigation_home) {
                    selectedFragment = new HomeFragment();
                    tag = "HOME_FRAGMENT";
                } else if (itemId == R.id.navigation_favorites) {
                    selectedFragment = new FavoritesFragment();
                    tag = "FAVORITES_FRAGMENT";
                } else if (itemId == R.id.navigation_chat) {
                    selectedFragment = new NotificationFragment();
                    tag = "CHAT_FRAGMENT";
                } else if (itemId == R.id.navigation_profile) {
                    selectedFragment = new ProfileFragment(); // Sử dụng ProfileFragment mới
                    tag = "PROFILE_FRAGMENT";
                }

                if (selectedFragment != null) {
                    loadFragment(selectedFragment, tag); // Load fragment được chọn với tag
                    return true; // Trả về true để hiển thị item được chọn
                }
                return false;
            }
        });

        // Đặt item mặc định được chọn (nếu cần, nhưng loadFragment ban đầu đã xử lý)
        // bottomNavigationView.setSelectedItemId(R.id.navigation_home);
    }

    // Phương thức để load (thay thế) Fragment vào container
    private void loadFragment(Fragment fragment, String tag) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

        // Kiểm tra xem fragment đã tồn tại trong back stack chưa
        Fragment existingFragment = fragmentManager.findFragmentByTag(tag);

        if (existingFragment == null) {
            // Nếu chưa tồn tại, thay thế và thêm vào back stack (tùy chọn)
            fragmentTransaction.replace(R.id.fragment_container, fragment, tag);
            // fragmentTransaction.addToBackStack(tag); // Bỏ comment nếu muốn dùng nút back để quay lại fragment trước
        } else {
            // Nếu đã tồn tại, chỉ cần hiển thị nó (nếu đang bị ẩn) hoặc không làm gì nếu nó đang hiển thị
            // Để đơn giản, ta vẫn dùng replace ở đây. Nếu muốn tối ưu hơn, bạn có thể dùng show/hide.
            fragmentTransaction.replace(R.id.fragment_container, existingFragment, tag);
        }

        // Tối ưu: Chỉ commit nếu có sự thay đổi thực sự (ví dụ: fragment mới hoặc fragment khác được chọn)
        // if (!fragment.isVisible() || existingFragment == null) {
        fragmentTransaction.commit(); // Thực hiện thay đổi
        // }
    }
}
