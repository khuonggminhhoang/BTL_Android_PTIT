package com.example.foodorderapp.activity; // Package của bạn

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment; // Import Fragment
import androidx.fragment.app.FragmentManager; // Import FragmentManager
import androidx.fragment.app.FragmentTransaction; // Import FragmentTransaction

import android.os.Bundle;

// Import các lớp Fragment mới
import com.example.foodorderapp.R;
import com.example.foodorderapp.fragment.ChatFragment;
import com.example.foodorderapp.fragment.FavoritesFragment;
import com.example.foodorderapp.fragment.HomeFragment;
import com.example.foodorderapp.fragment.ProfileFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {

    private BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bottomNavigationView = findViewById(R.id.bottomNavigationView);

        // Load Fragment mặc định khi Activity khởi chạy
        if (savedInstanceState == null) {
            loadFragment(new HomeFragment()); // Load HomeFragment ban đầu
        }

        // Thiết lập Bottom Navigation View để chuyển đổi Fragment
        setupBottomNavigation();
    }

    // Thiết lập Bottom Navigation View
    private void setupBottomNavigation() {
        bottomNavigationView.setOnItemSelectedListener(item -> {
            Fragment selectedFragment = null;
            int itemId = item.getItemId();

            if (itemId == R.id.navigation_home) {
                selectedFragment = new HomeFragment();
            } else if (itemId == R.id.navigation_favorites) {
                selectedFragment = new FavoritesFragment();
            } else if (itemId == R.id.navigation_chat) {
                selectedFragment = new ChatFragment();
            } else if (itemId == R.id.navigation_profile) {
                selectedFragment = new ProfileFragment();
            }

            if (selectedFragment != null) {
                loadFragment(selectedFragment); // Load fragment được chọn
                return true; // Trả về true để hiển thị item được chọn
            }
            return false;
        });

        // Đặt item mặc định được chọn (nếu cần, nhưng loadFragment ban đầu đã xử lý)
        // bottomNavigationView.setSelectedItemId(R.id.navigation_home);
    }

    // Phương thức để load (thay thế) Fragment vào container
    private void loadFragment(Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.fragment_container, fragment); // Thay thế fragment trong container
        // fragmentTransaction.addToBackStack(null); // Tùy chọn: Thêm vào back stack nếu muốn quay lại fragment trước đó bằng nút back
        fragmentTransaction.commit(); // Thực hiện thay đổi
    }

    // Các phương thức initData, setupCategoryRecyclerView, setupJobRecyclerView đã được chuyển vào HomeFragment
}
