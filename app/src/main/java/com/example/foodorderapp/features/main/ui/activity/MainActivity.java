package com.example.foodorderapp.features.main.ui.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.os.Bundle;
import android.view.MenuItem;

import com.example.foodorderapp.R;
import com.example.foodorderapp.features.main.ui.fragment.FavoritesFragment;
import com.example.foodorderapp.features.main.ui.fragment.HomeFragment;
import com.example.foodorderapp.features.main.ui.fragment.NotificationFragment;
import com.example.foodorderapp.features.profile.ui.fragment.ProfileFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;

public class MainActivity extends AppCompatActivity {

    private BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bottomNavigationView = findViewById(R.id.bottomNavigationView);

        // Khởi tạo HomeFragment mặc định
        if (savedInstanceState == null) {
            loadFragment(new HomeFragment(), "HOME_FRAGMENT");
        }

        // Thiết lập điều hướng BottomNavigationView
        setupBottomNavigation();
    }

    // Cấu hình BottomNavigationView
    private void setupBottomNavigation() {
        bottomNavigationView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                Fragment selectedFragment = null;
                String tag = null;
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
                    selectedFragment = new ProfileFragment();
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

    // Thay thế Fragment vào container
    private void loadFragment(Fragment fragment, String tag) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

        // Kiểm tra Fragment đã tồn tại
        Fragment existingFragment = fragmentManager.findFragmentByTag(tag);

        if (existingFragment == null) {
            // Thay thế Fragment mới
            fragmentTransaction.replace(R.id.fragment_container, fragment, tag);
        } else {
            // Sử dụng Fragment hiện có
            fragmentTransaction.replace(R.id.fragment_container, existingFragment, tag);
        }

        fragmentTransaction.commit();
    }
}